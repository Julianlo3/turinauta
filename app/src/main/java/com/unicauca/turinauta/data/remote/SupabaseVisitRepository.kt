package com.unicauca.turinauta.data.remote

import com.unicauca.turinauta.core.model.RecordVisitRequest
import com.unicauca.turinauta.core.model.RewardBadge
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.core.model.VisitResult
import com.unicauca.turinauta.core.network.SupabaseClientProvider
import com.unicauca.turinauta.data.remote.dto.MyVisitHistoryDto
import com.unicauca.turinauta.data.remote.dto.RecordVisitResponseDto
import com.unicauca.turinauta.data.remote.dto.StampDto
import com.unicauca.turinauta.data.remote.mapper.RecordVisitResultMapper
import com.unicauca.turinauta.data.remote.mapper.VisitPhotoValidator
import com.unicauca.turinauta.data.remote.mapper.toDomain
import com.unicauca.turinauta.data.repository.VisitRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.util.UUID
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Repositorio real de visitas contra Supabase (`my_visit_history`, bucket `visit-photos`
 * y `POST /rest/v1/rpc/record_visit`).
 */
class SupabaseVisitRepository(
    private val clientProvider: SupabaseClientProvider,
) : VisitRepository {

    override suspend fun getMyVisitHistory(): Result<List<VisitRecord>> = runCatching {
        val client = clientProvider.requireClient()
        if (client.auth.currentUserOrNull() == null) {
            return@runCatching emptyList()
        }
        client.postgrest["my_visit_history"]
            .select(columns = Columns.ALL) {
                order(column = "visited_at", order = Order.DESCENDING)
            }
            .decodeList<MyVisitHistoryDto>()
            .map { it.toDomain() }
    }

    override suspend fun getVisitForPlace(placeId: String): Result<VisitRecord?> = runCatching {
        val client = clientProvider.requireClient()
        if (client.auth.currentUserOrNull() == null) {
            return@runCatching null
        }
        client.postgrest["my_visit_history"]
            .select(columns = Columns.ALL) {
                filter { eq("place_id", placeId) }
                order(column = "visited_at", order = Order.DESCENDING)
                limit(1)
            }
            .decodeList<MyVisitHistoryDto>()
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun uploadVisitPhoto(
        photoBytes: ByteArray,
        fileExtension: String,
    ): Result<String> = runCatching {
        val client = clientProvider.requireClient()
        val userId = client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("unauthenticated: inicia sesión para subir la foto.")

        val normalizedExt = when (val validation = VisitPhotoValidator.validate(photoBytes, fileExtension)) {
            is VisitPhotoValidator.ValidationResult.Valid -> validation.normalizedExtension
            is VisitPhotoValidator.ValidationResult.Invalid ->
                throw IllegalArgumentException("photo: ${validation.message}")
        }

        val storagePath = "$userId/${UUID.randomUUID()}.$normalizedExt"
        val mimeType = when (normalizedExt) {
            "png" -> ContentType.Image.PNG
            "webp" -> ContentType("image", "webp")
            else -> ContentType.Image.JPEG
        }

        client.storage
            .from(VisitRecord.VISIT_PHOTOS_BUCKET)
            .upload(path = storagePath, data = photoBytes) {
                upsert = false
                contentType = mimeType
            }

        storagePath
    }

    override suspend fun recordVisit(request: RecordVisitRequest): VisitResult {
        val client = runCatching { clientProvider.requireClient() }
            .getOrElse { return RecordVisitResultMapper.mapException(it) }

        if (client.auth.currentUserOrNull() == null) {
            return VisitResult.VisitUnauthenticated
        }

        val photoPath = request.photoPath?.trim()?.takeIf { it.isNotEmpty() }
            ?: return VisitResult.VisitInvalidPhoto("Debes adjuntar una foto válida del lugar.")

        val lat = request.latitude
            ?: return VisitResult.VisitOutOfRange(
                message = "Necesitamos tu ubicación actual para verificar que estás en el lugar.",
            )
        val lon = request.longitude
            ?: return VisitResult.VisitOutOfRange(
                message = "Necesitamos tu ubicación actual para verificar que estás en el lugar.",
            )

        return try {
            val response = client.postgrest.rpc(
                function = "record_visit",
                parameters = buildJsonObject {
                    put("p_place_id", request.placeId)
                    put("p_photo_path", photoPath)
                    put("p_latitude", lat)
                    put("p_longitude", lon)
                },
            ).decodeAs<RecordVisitResponseDto>()

            val reward = response.stampId?.let { fetchStampOrNull(it) }
            val latestVisit = getVisitForPlace(request.placeId).getOrNull()

            if (response.status.equals("created", ignoreCase = true) && latestVisit != null) {
                VisitResult.VisitCreated(visit = latestVisit, reward = reward)
            } else {
                RecordVisitResultMapper.mapRpcResponse(
                    response = response,
                    placeId = request.placeId,
                    placeName = latestVisit?.displayPlaceName.orEmpty(),
                    photoPath = photoPath,
                    reward = reward,
                )
            }
        } catch (t: Throwable) {
            RecordVisitResultMapper.mapException(t)
        }
    }

    override suspend fun registerVisitWithPhoto(
        placeId: String,
        photoBytes: ByteArray,
        fileExtension: String,
        latitude: Double,
        longitude: Double,
    ): VisitResult {
        val client = runCatching { clientProvider.requireClient() }
            .getOrElse { return RecordVisitResultMapper.mapException(it) }

        if (client.auth.currentUserOrNull() == null) {
            return VisitResult.VisitUnauthenticated
        }

        // Paso 1 del contrato: consultar si ya existe visita para NO subir otra foto
        val existingVisit = getVisitForPlace(placeId).getOrNull()
        if (existingVisit != null) {
            return VisitResult.VisitDuplicate(
                visitId = existingVisit.id,
                stampId = existingVisit.stampId,
                visitedAtIso = existingVisit.visitedAtIso,
                visitedAtMillis = existingVisit.visitedAtMillis,
            )
        }

        // Paso 2: validar foto antes de subir
        when (val validation = VisitPhotoValidator.validate(photoBytes, fileExtension)) {
            is VisitPhotoValidator.ValidationResult.Invalid ->
                return VisitResult.VisitInvalidPhoto(validation.message)
            is VisitPhotoValidator.ValidationResult.Valid -> Unit
        }

        // Paso 3: subir foto a Storage (`visit-photos/<auth.uid()>/<uuid>.<ext>`, upsert = false)
        val photoPath = uploadVisitPhoto(photoBytes, fileExtension).getOrElse { error ->
            return RecordVisitResultMapper.mapException(error)
        }

        // Paso 4: invocar RPC `record_visit`
        return recordVisit(
            RecordVisitRequest(
                placeId = placeId,
                photoPath = photoPath,
                latitude = latitude,
                longitude = longitude,
            ),
        )
    }

    private suspend fun fetchStampOrNull(stampId: String): RewardBadge? = runCatching {
        val client = clientProvider.requireClient()
        client.postgrest["stamps"]
            .select(columns = Columns.ALL) {
                filter { eq("id", stampId) }
                limit(1)
            }
            .decodeList<StampDto>()
            .firstOrNull()
            ?.toDomain(isEarned = true)
    }.getOrNull()
}
