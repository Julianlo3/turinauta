package com.unicauca.turinauta.data

import com.unicauca.turinauta.core.model.Category
import com.unicauca.turinauta.core.model.CollectionProgress
import com.unicauca.turinauta.core.model.DataReport
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.ReportField
import com.unicauca.turinauta.core.model.RewardBadge
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.core.model.VisitResult
import com.unicauca.turinauta.data.remote.dto.MyVisitHistoryDto
import com.unicauca.turinauta.data.remote.dto.PlaceDto
import com.unicauca.turinauta.data.remote.dto.RecordVisitResponseDto
import com.unicauca.turinauta.data.remote.mapper.ContentReportValidator
import com.unicauca.turinauta.data.remote.mapper.RecordVisitResultMapper
import com.unicauca.turinauta.data.remote.mapper.VisitPhotoValidator
import com.unicauca.turinauta.data.remote.mapper.haversineDistanceMeters
import com.unicauca.turinauta.data.remote.mapper.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseMappersAndValidatorsTest {

    @Test
    fun placeWithoutOpeningHoursShowsRuleR5Fallback() {
        val category = Category(id = "cat-1", name = "Gastronomía", slug = "gastronomia", sortOrder = 1)
        val dto = PlaceDto(
            id = "place-1",
            categoryId = "cat-1",
            name = "Cocina del Barrio",
            slug = "cocina-del-barrio",
            summary = "Sabores tradicionales",
            description = "Cocina local",
            locationReference = "Centro histórico",
            latitude = 2.4419,
            longitude = -76.6063,
            openingHours = "   ",
        )

        val place = dto.toDomain(mapOf(category.id to category))

        assertEquals(Place.SCHEDULE_TO_CONFIRM, place.displayOpeningHours)
        assertEquals("Gastronomía", place.categoryName)
    }

    @Test
    fun withdrawnPlaceInVisitHistoryKeepsStampAndShowsUnavailableName() {
        val dto = MyVisitHistoryDto(
            visitId = "visit-1",
            placeId = "place-9",
            placeName = "Lugar retirado",
            placeAvailable = false,
            visitedAt = "2026-11-24T15:30:00Z",
            photoPath = "uid-1/photo-1.jpg",
            stampId = "stamp-1",
            stampTitle = "Sabores de Fogón",
        )

        val record = dto.toDomain(
            photoUrlBuilder = { path -> "https://demo.supabase.co/storage/v1/object/public/visit-photos/$path" },
        )

        assertFalse(record.placeAvailable)
        assertEquals(VisitRecord.UNAVAILABLE_PLACE_NAME, record.displayPlaceName)
        assertEquals("Sabores de Fogón", record.stampTitle)
        assertEquals(
            "https://demo.supabase.co/storage/v1/object/public/visit-photos/uid-1/photo-1.jpg",
            record.photoPublicUrl,
        )
        assertTrue(record.visitedAtMillis > 0L)
    }

    @Test
    fun visitPhotoValidatorAcceptsSupportedFormatsUpTo5Mb() {
        val validBytes = ByteArray(1024) { 1 }

        val jpegResult = VisitPhotoValidator.validate(validBytes, "JPEG")
        val pngResult = VisitPhotoValidator.validate(validBytes, ".png")
        val webpResult = VisitPhotoValidator.validate(validBytes, "webp")

        assertTrue(jpegResult is VisitPhotoValidator.ValidationResult.Valid)
        assertEquals("jpg", (jpegResult as VisitPhotoValidator.ValidationResult.Valid).normalizedExtension)
        assertTrue(pngResult is VisitPhotoValidator.ValidationResult.Valid)
        assertTrue(webpResult is VisitPhotoValidator.ValidationResult.Valid)
    }

    @Test
    fun visitPhotoValidatorRejectsEmptyOversizedOrUnsupportedFormat() {
        val emptyResult = VisitPhotoValidator.validate(ByteArray(0), "jpg")
        val oversizedResult = VisitPhotoValidator.validate(
            ByteArray((VisitRecord.MAX_VISIT_PHOTO_BYTES + 1).toInt()),
            "jpg",
        )
        val unsupportedResult = VisitPhotoValidator.validate(ByteArray(512) { 1 }, "gif")

        assertTrue(emptyResult is VisitPhotoValidator.ValidationResult.Invalid)
        assertTrue(oversizedResult is VisitPhotoValidator.ValidationResult.Invalid)
        assertTrue(unsupportedResult is VisitPhotoValidator.ValidationResult.Invalid)
    }

    @Test
    fun recordVisitMapperHandlesCreatedDuplicateAndErrorResponses() {
        val reward = RewardBadge(
            id = "stamp-1",
            title = "Hilos y Telar",
            description = "Sello artesanal",
            isEarned = true,
        )

        val created = RecordVisitResultMapper.mapRpcResponse(
            response = RecordVisitResponseDto(
                status = "created",
                visitId = "visit-100",
                stampId = "stamp-1",
                visitedAt = "2026-11-24T16:00:00Z",
            ),
            placeId = "place-2",
            placeName = "Taller de tejido",
            photoPath = "uid/abc.jpg",
            reward = reward,
            photoUrlBuilder = { null },
        )
        assertTrue(created is VisitResult.VisitCreated)
        assertEquals("visit-100", (created as VisitResult.VisitCreated).visit.id)

        val duplicate = RecordVisitResultMapper.mapRpcResponse(
            response = RecordVisitResponseDto(
                status = "already_recorded",
                visitId = "visit-100",
                stampId = "stamp-1",
            ),
            placeId = "place-2",
            placeName = "Taller de tejido",
            photoPath = "uid/abc.jpg",
            reward = null,
            photoUrlBuilder = { null },
        )
        assertTrue(duplicate is VisitResult.VisitDuplicate)

        val outOfRange = RecordVisitResultMapper.mapException(
            IllegalStateException("Distance exceeds 200 m limit"),
        )
        assertTrue(outOfRange is VisitResult.VisitOutOfRange)

        val unauthenticated = RecordVisitResultMapper.mapException(
            IllegalStateException("401 Unauthorized JWT"),
        )
        assertTrue(unauthenticated is VisitResult.VisitUnauthenticated)
    }

    @Test
    fun contentReportValidatorEnforcesTrimmedNonBlankAndMax1500Chars() {
        assertNotNull(ContentReportValidator.validateDetails("   \n\t "))
        assertNotNull(ContentReportValidator.validateDetails("a".repeat(DataReport.MAX_DETAILS_LENGTH + 1)))
        assertNull(ContentReportValidator.validateDetails("El horario cambió a las 10:00"))
        assertEquals(setOf("schedule", "location", "description", "other"), ReportField.entries.map { it.apiValue }.toSet())
    }

    @Test
    fun haversineDistanceAndCollectionProgressCalculateAccurately() {
        val samePoint = haversineDistanceMeters(2.4419, -76.6063, 2.4419, -76.6063)
        val nearbyPoint = haversineDistanceMeters(2.4419, -76.6063, 2.4428, -76.6063)

        assertEquals(0.0, samePoint, 0.01)
        assertTrue(nearbyPoint in 90.0..115.0)

        val progress = CollectionProgress(
            id = "col-1",
            name = "Sabores",
            slug = "sabores",
            description = "Colección",
            totalStamps = 3,
            earnedStamps = 3,
        )
        assertEquals(100, progress.progressPercent)
        assertTrue(progress.isCompleted)
    }
}
