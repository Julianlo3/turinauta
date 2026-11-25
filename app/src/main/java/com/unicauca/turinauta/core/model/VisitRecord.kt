package com.unicauca.turinauta.core.model

/**
 * Registro histórico de una visita del usuario.
 * Alineado con `GET /my_visit_history?select=*&order=visited_at.desc`.
 * Si la ficha fue retirada, `placeAvailable = false`, `placeName = "Lugar no disponible"`
 * y se conserva `stampTitle`.
 */
data class VisitRecord(
    val id: String,
    val placeId: String,
    val placeName: String,
    val placeAvailable: Boolean = true,
    val visitedAtIso: String,
    val visitedAtMillis: Long = 0L,
    val photoPath: String? = null,
    val photoPublicUrl: String? = null,
    val stampId: String? = null,
    val stampTitle: String? = null,
    val stampImagePath: String? = null,
) {
    val displayPlaceName: String
        get() = if (placeAvailable && placeName.isNotBlank()) placeName else UNAVAILABLE_PLACE_NAME

    companion object {
        const val UNAVAILABLE_PLACE_NAME = "Lugar no disponible"
        const val VISIT_PHOTOS_BUCKET = "visit-photos"
        const val MAX_VISIT_DISTANCE_METERS = 200
        const val MAX_VISIT_PHOTO_BYTES = 5L * 1024L * 1024L
        val ALLOWED_PHOTO_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")
    }
}

/**
 * Parámetros para invocar `POST /rest/v1/rpc/record_visit`.
 * `photoPath` sigue el formato `<auth.uid()>/<uuid-aleatorio>.(jpg|png|webp)` en `visit-photos`.
 */
data class RecordVisitRequest(
    val placeId: String,
    val photoPath: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

/**
 * Resultado tipado del registro de visita (Regla R4 + contrato RPC `record_visit`).
 */
sealed interface VisitResult {
    data class VisitCreated(
        val visit: VisitRecord,
        val reward: RewardBadge?,
    ) : VisitResult

    data class VisitDuplicate(
        val visitId: String? = null,
        val stampId: String? = null,
        val visitedAtMillis: Long? = null,
        val visitedAtIso: String? = null,
    ) : VisitResult

    data object VisitUnauthenticated : VisitResult

    data class VisitOutOfRange(
        val maxDistanceMeters: Int = VisitRecord.MAX_VISIT_DISTANCE_METERS,
        val message: String,
    ) : VisitResult

    data class VisitInvalidPhoto(
        val message: String,
    ) : VisitResult

    data class VisitNetworkError(
        val message: String,
    ) : VisitResult
}
