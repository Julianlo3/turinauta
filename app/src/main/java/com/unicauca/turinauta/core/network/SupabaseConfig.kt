package com.unicauca.turinauta.core.network

import com.unicauca.turinauta.BuildConfig
import com.unicauca.turinauta.core.model.VisitRecord

/**
 * Configuración del cliente Supabase inyectada desde `local.properties` vía [BuildConfig].
 * Nunca incluye claves `service_role` ni secretos OAuth de Google Cloud.
 */
object SupabaseConfig {
    val url: String
        get() = BuildConfig.SUPABASE_URL.trim().removeSuffix("/")

    val publishableKey: String
        get() = BuildConfig.SUPABASE_PUBLISHABLE_KEY.trim()

    const val OAUTH_SCHEME = "turinauta"
    const val OAUTH_HOST = "auth-callback"
    const val CATALOG_IMAGES_BUCKET = "catalog-images"

    /**
     * Deep link exacto que debe estar registrado en Supabase Dashboard:
     * Authentication -> URL Configuration -> Redirect URLs.
     */
    const val OAUTH_REDIRECT_URL = "$OAUTH_SCHEME://$OAUTH_HOST"

    val isConfigured: Boolean
        get() = url.startsWith("https://") && publishableKey.isNotBlank()

    /**
     * Construye la URL pública de una foto de visita en el bucket `visit-photos`.
     */
    fun publicVisitPhotoUrl(photoPath: String?): String? {
        val cleaned = photoPath?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) return cleaned
        if (url.isBlank()) return null
        val trimmed = cleaned.trimStart('/')
        val withoutBucket = trimmed.removePrefix("${VisitRecord.VISIT_PHOTOS_BUCKET}/")
        return "$url/storage/v1/object/public/${VisitRecord.VISIT_PHOTOS_BUCKET}/$withoutBucket"
    }

    /**
     * Resuelve la URL pública de una imagen del catálogo en Storage (`catalog-images`).
     * Ejemplo: `places/fogon-de-la-plazuela/cover.jpg` ->
     * `<SUPABASE_URL>/storage/v1/object/public/catalog-images/places/fogon-de-la-plazuela/cover.jpg`.
     */
    fun resolvePublicMediaUrl(
        pathOrUrl: String?,
        defaultBucket: String = CATALOG_IMAGES_BUCKET,
    ): String? {
        val cleaned = pathOrUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) return cleaned
        if (url.isBlank()) return null
        val trimmed = cleaned.trimStart('/')
        return when {
            trimmed.startsWith("storage/v1/") -> "$url/$trimmed"
            trimmed.startsWith("$defaultBucket/") -> "$url/storage/v1/object/public/$trimmed"
            else -> "$url/storage/v1/object/public/$defaultBucket/$trimmed"
        }
    }
}

class SupabaseNotConfiguredException : IllegalStateException(
    "Faltan SUPABASE_URL y/o SUPABASE_PUBLISHABLE_KEY en local.properties. " +
        "Configúralas antes de realizar peticiones al backend.",
)
