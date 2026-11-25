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
        return "$url/storage/v1/object/public/${VisitRecord.VISIT_PHOTOS_BUCKET}/${cleaned.trimStart('/')}"
    }

    /**
     * Resuelve la URL pública de un recurso en Storage si el backend devuelve una ruta relativa
     * o devuelve la URL directa si ya viene completa.
     */
    fun resolvePublicMediaUrl(pathOrUrl: String?, defaultBucket: String): String? {
        val cleaned = pathOrUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) return cleaned
        if (url.isBlank()) return null
        val trimmed = cleaned.trimStart('/')
        return if (trimmed.startsWith("storage/v1/")) {
            "$url/$trimmed"
        } else {
            "$url/storage/v1/object/public/$defaultBucket/$trimmed"
        }
    }
}

class SupabaseNotConfiguredException : IllegalStateException(
    "Faltan SUPABASE_URL y/o SUPABASE_PUBLISHABLE_KEY en local.properties. " +
        "Configúralas antes de realizar peticiones al backend.",
)
