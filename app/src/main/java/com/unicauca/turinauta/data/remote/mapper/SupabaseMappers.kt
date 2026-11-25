package com.unicauca.turinauta.data.remote.mapper

import com.unicauca.turinauta.core.model.Category
import com.unicauca.turinauta.core.model.CollectionProgress
import com.unicauca.turinauta.core.model.DataReport
import com.unicauca.turinauta.core.model.IllustrationKey
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.ReportSubmitResult
import com.unicauca.turinauta.core.model.RewardBadge
import com.unicauca.turinauta.core.model.Route
import com.unicauca.turinauta.core.model.RouteStop
import com.unicauca.turinauta.core.model.UserProfile
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.core.model.VisitResult
import com.unicauca.turinauta.core.network.SupabaseConfig
import com.unicauca.turinauta.data.remote.dto.AvailableCollectionDto
import com.unicauca.turinauta.data.remote.dto.AvailableRouteDto
import com.unicauca.turinauta.data.remote.dto.CategoryDto
import com.unicauca.turinauta.data.remote.dto.MyCollectionProgressDto
import com.unicauca.turinauta.data.remote.dto.MyCollectionStampDto
import com.unicauca.turinauta.data.remote.dto.MyVisitHistoryDto
import com.unicauca.turinauta.data.remote.dto.PlaceDto
import com.unicauca.turinauta.data.remote.dto.ProfileDto
import com.unicauca.turinauta.data.remote.dto.RecordVisitResponseDto
import com.unicauca.turinauta.data.remote.dto.RouteStopDto
import com.unicauca.turinauta.data.remote.dto.StampDto
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun CategoryDto.toDomain(): Category = Category(
    id = id,
    name = name,
    slug = slug,
    sortOrder = sortOrder,
)

fun PlaceDto.toDomain(categoriesById: Map<String, Category>): Place {
    val category = categoriesById[categoryId]
    return Place(
        id = id,
        categoryId = categoryId,
        categoryName = category?.name.orEmpty(),
        name = name,
        slug = slug,
        summary = summary?.trim()?.takeIf { it.isNotEmpty() }
            ?: description?.trim().orEmpty(),
        description = description?.trim().orEmpty(),
        locationReference = locationReference?.trim().orEmpty(),
        latitude = latitude,
        longitude = longitude,
        openingHours = openingHours,
        visitConditions = visitConditions?.trim()?.takeIf { it.isNotEmpty() },
        imagePath = imagePath?.trim()?.takeIf { it.isNotEmpty() },
        sortOrder = sortOrder,
        illustrationKey = inferIllustrationKey(
            categorySlug = category?.slug,
            titleOrSlug = "$name $slug",
        ),
        isFeatured = sortOrder in 1..3,
    )
}

fun AvailableRouteDto.toDomain(stops: List<RouteStop> = emptyList()): Route = Route(
    id = id,
    name = name,
    slug = slug,
    description = description?.trim().orEmpty(),
    visitConditions = visitConditions?.trim()?.takeIf { it.isNotEmpty() },
    imagePath = imagePath?.trim()?.takeIf { it.isNotEmpty() },
    sortOrder = sortOrder,
    stops = stops.sortedBy { it.suggestedOrder },
)

fun RouteStopDto.toDomain(): RouteStop = RouteStop(
    routeId = routeId,
    placeId = placeId,
    suggestedOrder = suggestedOrder,
)

fun ProfileDto.toDomain(email: String?): UserProfile = UserProfile(
    id = id,
    displayName = displayName?.trim()?.takeIf { it.isNotEmpty() }
        ?: UserProfile.DEFAULT_DISPLAY_NAME,
    email = email,
    createdAtIso = createdAt,
)

fun MyVisitHistoryDto.toDomain(
    photoUrlBuilder: (String?) -> String? = SupabaseConfig::publicVisitPhotoUrl,
): VisitRecord {
    val resolvedName = if (placeAvailable && !placeName.isNullOrBlank()) {
        placeName.trim()
    } else {
        VisitRecord.UNAVAILABLE_PLACE_NAME
    }
    return VisitRecord(
        id = id ?: visitId ?: "$placeId-$visitedAt",
        placeId = placeId,
        placeName = resolvedName,
        placeAvailable = placeAvailable,
        visitedAtIso = visitedAt,
        visitedAtMillis = parseIsoTimestampToMillis(visitedAt),
        photoPath = photoPath?.trim()?.takeIf { it.isNotEmpty() },
        photoPublicUrl = photoUrlBuilder(photoPath),
        stampId = stampId,
        stampTitle = stampTitle?.trim()?.takeIf { it.isNotEmpty() },
        stampImagePath = stampImagePath?.trim()?.takeIf { it.isNotEmpty() },
    )
}

fun StampDto.toDomain(
    collectionId: String? = null,
    sortOrder: Int = 0,
    isEarned: Boolean = false,
    earnedAtIso: String? = null,
): RewardBadge = RewardBadge(
    id = id,
    title = title,
    description = description?.trim().orEmpty(),
    placeId = placeId,
    collectionId = collectionId,
    imagePath = imagePath?.trim()?.takeIf { it.isNotEmpty() },
    illustrationKey = inferIllustrationKey(categorySlug = null, titleOrSlug = title),
    sortOrder = sortOrder,
    isEarned = isEarned,
    earnedAtIso = earnedAtIso,
    earnedAtMillis = earnedAtIso?.let(::parseIsoTimestampToMillis),
)

fun MyCollectionStampDto.toDomain(): RewardBadge {
    val resolvedTitle = title?.takeIf { it.isNotBlank() }
        ?: stampTitle?.takeIf { it.isNotBlank() }
        ?: "Sello"
    return RewardBadge(
        id = stampId,
        title = resolvedTitle,
        description = (description ?: stampDescription)?.trim().orEmpty(),
        placeId = placeId,
        collectionId = collectionId,
        imagePath = (imagePath ?: stampImagePath)?.trim()?.takeIf { it.isNotEmpty() },
        illustrationKey = inferIllustrationKey(categorySlug = null, titleOrSlug = resolvedTitle),
        sortOrder = sortOrder,
        isEarned = earned,
        earnedAtIso = visitedAt,
        earnedAtMillis = visitedAt?.let(::parseIsoTimestampToMillis),
    )
}

fun AvailableCollectionDto.toDomain(stamps: List<RewardBadge>): CollectionProgress =
    CollectionProgress(
        id = id,
        name = name,
        slug = slug,
        description = description?.trim().orEmpty(),
        imagePath = imagePath?.trim()?.takeIf { it.isNotEmpty() },
        sortOrder = sortOrder,
        totalStamps = stamps.size,
        earnedStamps = stamps.count { it.isEarned },
        stamps = stamps.sortedBy { it.sortOrder },
    )

fun MyCollectionProgressDto.toDomain(stamps: List<RewardBadge>): CollectionProgress {
    val resolvedId = collectionId ?: id.orEmpty()
    val resolvedName = name?.takeIf { it.isNotBlank() }
        ?: collectionName?.takeIf { it.isNotBlank() }
        ?: "Colección"
    val resolvedSlug = slug ?: collectionSlug.orEmpty()
    val resolvedTotal = if (totalStamps > 0) totalStamps else stamps.size
    val resolvedEarned = earnedStamps.coerceIn(0, resolvedTotal.coerceAtLeast(earnedStamps))
    return CollectionProgress(
        id = resolvedId,
        name = resolvedName,
        slug = resolvedSlug,
        description = description?.trim().orEmpty(),
        imagePath = imagePath?.trim()?.takeIf { it.isNotEmpty() },
        sortOrder = sortOrder,
        totalStamps = resolvedTotal,
        earnedStamps = resolvedEarned,
        progressPercent = progressPercent ?: CollectionProgress.calculatePercent(resolvedEarned, resolvedTotal),
        stamps = stamps.sortedBy { it.sortOrder },
    )
}

/**
 * Infiere una ilustración vectorial local sobria como respaldo cuando `image_path` es nulo.
 */
fun inferIllustrationKey(categorySlug: String?, titleOrSlug: String): IllustrationKey {
    val normalized = "${categorySlug.orEmpty()} $titleOrSlug".lowercase()
    return when {
        "cafe" in normalized || "café" in normalized || "tostadora" in normalized || "grano" in normalized ->
            IllustrationKey.CAFE
        "ceramica" in normalized || "cerámica" in normalized || "barro" in normalized ->
            IllustrationKey.CERAMICA
        "amasijo" in normalized || "maiz" in normalized || "maíz" in normalized ->
            IllustrationKey.AMASIJOS
        "gastronomia" in normalized || "gastronomía" in normalized || "cocina" in normalized || "sabor" in normalized ->
            IllustrationKey.GASTRONOMIA
        "artesania" in normalized || "artesanía" in normalized || "tejido" in normalized || "telar" in normalized ->
            IllustrationKey.ARTESANIAS
        "cultura" in normalized || "esquina" in normalized || "acorde" in normalized || "musica" in normalized ->
            IllustrationKey.CULTURA
        else -> IllustrationKey.GENERIC
    }
}

/**
 * Parsea fechas ISO-8601 de PostgreSQL/Supabase (`timestamptz`) a milisegundos epoch.
 */
fun parseIsoTimestampToMillis(isoString: String?): Long {
    if (isoString.isNullOrBlank()) return 0L
    return runCatching { Instant.parse(isoString).toEpochMilli() }
        .recoverCatching { OffsetDateTime.parse(isoString).toInstant().toEpochMilli() }
        .getOrDefault(0L)
}

/**
 * Valida la foto antes de subirla a `visit-photos` (JPG, PNG o WebP y ≤ 5 MB).
 */
object VisitPhotoValidator {
    sealed interface ValidationResult {
        data class Valid(val normalizedExtension: String) : ValidationResult
        data class Invalid(val message: String) : ValidationResult
    }

    fun validate(photoBytes: ByteArray, fileExtension: String): ValidationResult {
        if (photoBytes.isEmpty()) {
            return ValidationResult.Invalid("La foto seleccionada está vacía. Elige una imagen válida.")
        }
        if (photoBytes.size.toLong() > VisitRecord.MAX_VISIT_PHOTO_BYTES) {
            return ValidationResult.Invalid("La foto supera el límite máximo de 5 MB.")
        }
        val cleanExt = fileExtension.trim().lowercase().removePrefix(".")
        if (cleanExt !in VisitRecord.ALLOWED_PHOTO_EXTENSIONS) {
            return ValidationResult.Invalid("Formato no compatible. Usa una imagen JPG, PNG o WebP.")
        }
        val storageExt = if (cleanExt == "jpeg") "jpg" else cleanExt
        return ValidationResult.Valid(storageExt)
    }
}

/**
 * Valida el detalle de `submit_content_report` (HU-24): obligatorio tras trim y máximo 1500 caracteres.
 */
object ContentReportValidator {
    fun validateDetails(rawDetails: String): ReportSubmitResult.ValidationError? {
        val trimmed = rawDetails.trim()
        return when {
            trimmed.isEmpty() ->
                ReportSubmitResult.ValidationError("Describe brevemente qué dato está incorrecto.")
            trimmed.length > DataReport.MAX_DETAILS_LENGTH ->
                ReportSubmitResult.ValidationError(
                    "El detalle supera el máximo de ${DataReport.MAX_DETAILS_LENGTH} caracteres.",
                )
            else -> null
        }
    }
}

/**
 * Traduce respuestas y errores de la RPC `record_visit` a [VisitResult].
 */
object RecordVisitResultMapper {
    fun mapRpcResponse(
        response: RecordVisitResponseDto,
        placeId: String,
        placeName: String,
        photoPath: String?,
        reward: RewardBadge?,
        photoUrlBuilder: (String?) -> String? = SupabaseConfig::publicVisitPhotoUrl,
    ): VisitResult {
        return when (response.status.lowercase()) {
            "created" -> {
                val visitedAt = response.visitedAt.orEmpty()
                val visit = VisitRecord(
                    id = response.visitId.orEmpty(),
                    placeId = placeId,
                    placeName = placeName,
                    placeAvailable = true,
                    visitedAtIso = visitedAt,
                    visitedAtMillis = parseIsoTimestampToMillis(visitedAt),
                    photoPath = photoPath,
                    photoPublicUrl = photoUrlBuilder(photoPath),
                    stampId = response.stampId ?: reward?.id,
                    stampTitle = reward?.title,
                    stampImagePath = reward?.imagePath,
                )
                VisitResult.VisitCreated(visit = visit, reward = reward)
            }
            "already_recorded" -> VisitResult.VisitDuplicate(
                visitId = response.visitId,
                stampId = response.stampId,
                visitedAtIso = response.visitedAt,
                visitedAtMillis = response.visitedAt?.let(::parseIsoTimestampToMillis),
            )
            else -> VisitResult.VisitNetworkError(
                "Respuesta inesperada del servidor al registrar la visita (${response.status}).",
            )
        }
    }

    fun mapException(throwable: Throwable): VisitResult {
        val raw = throwable.message.orEmpty().lowercase()
        return when {
            "200" in raw || "distance" in raw || "distancia" in raw || "far" in raw || "lejos" in raw || "range" in raw ->
                VisitResult.VisitOutOfRange(
                    maxDistanceMeters = VisitRecord.MAX_VISIT_DISTANCE_METERS,
                    message = "Debes estar a menos de ${VisitRecord.MAX_VISIT_DISTANCE_METERS} metros del lugar para registrar tu visita.",
                )
            "photo" in raw || "foto" in raw || "storage" in raw || "object" in raw ->
                VisitResult.VisitInvalidPhoto(
                    message = "No se pudo validar la foto de la visita. Intenta tomar o seleccionar otra imagen.",
                )
            "jwt" in raw || "unauthenticated" in raw || "unauthorized" in raw || "401" in raw || "session" in raw ->
                VisitResult.VisitUnauthenticated
            else ->
                VisitResult.VisitNetworkError(
                    message = "No pudimos completar el registro de la visita. Verifica tu conexión e intenta nuevamente.",
                )
        }
    }
}

/**
 * Calcula la distancia aproximada en metros entre dos coordenadas (fórmula Haversine).
 * Se usa únicamente en memoria para ordenar por «Más cercanos» sin persistir la ubicación.
 */
fun haversineDistanceMeters(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double,
): Double {
    val earthRadiusMeters = 6_371_000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    val c = 2 * asin(sqrt(a.coerceIn(0.0, 1.0)))
    return earthRadiusMeters * c
}
