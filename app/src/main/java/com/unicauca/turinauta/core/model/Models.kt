package com.unicauca.turinauta.core.model

/*
 * Modelos de dominio alineados con CONTRATO_API_MVP.md (Supabase Data API).
 * Los nombres de columna del contrato se indican en cada campo.
 */

/** Estado editorial: el móvil solo recibe contenido publicado (RLS). */
enum class EditorialStatus { DRAFT, PUBLISHED, WITHDRAWN }

/** `categories(id, name, slug, sort_order)`: solo categorías activas. */
data class Category(
    val id: String,
    val label: String,
    val slug: String,
    val sortOrder: Int,
) {
    /** Ilustración local por categoría mientras no haya imágenes autorizadas. */
    val illustration: Illustration
        get() = when (slug) {
            "gastronomia" -> Illustration.BOWL
            "artesanias" -> Illustration.WEAVE
            "emprendimientos" -> Illustration.CUP
            "cultura" -> Illustration.CIRCLES
            else -> Illustration.CLAY
        }
}

/** Ilustración vectorial local mientras `image_path` no tenga imágenes autorizadas. */
enum class Illustration { BOWL, WEAVE, CIRCLES, CUP, BREAD, CLAY }

enum class ConditionType { GROUP, ACCESS, PHOTO, INFO }

data class VisitCondition(val type: ConditionType, val text: String)

data class GeoPoint(val latitude: Double, val longitude: Double)

/** `places(id, category_id, name, slug, summary, description, location_reference, latitude, longitude, opening_hours, visit_conditions, image_path, sort_order)`. */
data class Place(
    val id: String,
    val slug: String,
    val name: String,
    val category: Category,
    val summary: String,
    val description: String,
    /** `opening_hours`; null = «Horario por confirmar». */
    val schedule: String?,
    /** `location_reference`. */
    val addressReference: String,
    val latitude: Double,
    val longitude: Double,
    /** `visit_conditions`. */
    val conditions: List<VisitCondition>,
    val imagePath: String? = null,
    /** URL pública de `image_path` en el bucket `catalog-images`. */
    val imageUrl: String? = null,
    val illustration: Illustration,
    val sortOrder: Int,
    val status: EditorialStatus = EditorialStatus.PUBLISHED,
) {
    val location: GeoPoint get() = GeoPoint(latitude, longitude)
}

/** `available_routes` + `route_places(route_id, place_id, suggested_order)`. */
data class Route(
    val id: String,
    val slug: String,
    val name: String,
    val summary: String,
    /** `description`: propósito cultural o comunitario. */
    val purpose: String,
    val conditions: List<VisitCondition>,
    /** Paradas en el orden devuelto; si [hasSuggestedOrder] es false se pueden visitar en cualquier orden. */
    val placeIds: List<String>,
    val hasSuggestedOrder: Boolean,
    val illustration: Illustration,
    val imageUrl: String? = null,
    val sortOrder: Int,
    val status: EditorialStatus = EditorialStatus.PUBLISHED,
)

/** `stamps`: sello digital asociado a un lugar. */
data class Stamp(
    val id: String,
    val placeId: String,
    val title: String,
    val description: String,
    val illustration: Illustration,
    val imageUrl: String? = null,
)

/** `profiles(id, display_name)` + correo desde Supabase Auth. */
data class UserProfile(
    val id: String,
    val displayName: String,
    val email: String,
)

/** Fila de `my_visit_history`. */
data class VisitRecord(
    val visitId: String,
    val placeId: String,
    /** «Lugar no disponible» si la ficha se retiró. */
    val placeName: String,
    val placeAvailable: Boolean,
    val category: Category,
    val illustration: Illustration,
    val imageUrl: String? = null,
    val stampTitle: String,
    val photoPath: String,
    /** `visited_at`, hora del servidor (milisegundos). */
    val visitedAt: Long,
)

/** Fila de `my_collection_stamps`. */
data class RewardBadge(
    val stamp: Stamp,
    val collectionId: String,
    val placeName: String,
    val earned: Boolean,
    val visitedAt: Long? = null,
)

/** `available_collections`. */
data class ThematicCollection(
    val id: String,
    val slug: String,
    val name: String,
    val description: String,
    val illustration: Illustration,
    val stampIds: List<String>,
    val sortOrder: Int,
)

/** `my_collection_progress` + sus sellos. */
data class CollectionProgress(
    val collection: ThematicCollection,
    val badges: List<RewardBadge>,
) {
    val earnedStamps: Int get() = badges.count { it.earned }
    val totalStamps: Int get() = badges.size
    val progressPercent: Int get() = if (totalStamps == 0) 0 else earnedStamps * 100 / totalStamps
    val fraction: Float get() = progressPercent / 100f
}

/** `p_field` de `submit_content_report`. */
enum class ReportField(val label: String, val apiValue: String) {
    SCHEDULE("Horario", "schedule"),
    LOCATION("Ubicación", "location"),
    DESCRIPTION("Descripción", "description"),
    OTHER("Otro", "other"),
}

/** Aviso privado al equipo editorial (no es una reseña). `p_details` es obligatorio (máx. 1500). */
data class DataReport(
    val placeId: String,
    val field: ReportField,
    val details: String,
) {
    companion object { const val MAX_DETAILS = 1500 }
}

/** Foto elegida en el móvil para la visita: JPG, PNG o WebP de hasta 5 MB. */
data class VisitPhoto(
    val uri: String,
    val mimeType: String,
    val sizeBytes: Long,
) {
    companion object {
        const val MAX_BYTES = 5L * 1024 * 1024
        val ALLOWED_TYPES = setOf("image/jpeg", "image/png", "image/webp")
    }
}

/** Resultado de subir la foto y llamar a `rpc/record_visit`. */
sealed interface VisitResult {
    /** `{"status":"created"}` */
    data class VisitCreated(val visit: VisitRecord, val badge: RewardBadge, val progress: CollectionProgress?) : VisitResult
    /** `{"status":"already_recorded"}`: se conserva la visita original. */
    data class VisitDuplicate(val visit: VisitRecord) : VisitResult
    data object VisitUnauthenticated : VisitResult
    /** La RPC exige estar a 200 m o menos del lugar. */
    data class VisitTooFar(val distanceMeters: Int) : VisitResult
    data class VisitInvalidPhoto(val message: String) : VisitResult
    data class VisitNetworkError(val message: String) : VisitResult

    companion object { const val MAX_DISTANCE_METERS = 200 }
}

sealed interface AuthResult {
    data class Success(val user: UserProfile) : AuthResult
    /** Se abrió el navegador con Google; la sesión llega por el deep link. */
    data object Started : AuthResult
    data object Cancelled : AuthResult
    data class Error(val message: String) : AuthResult
}
