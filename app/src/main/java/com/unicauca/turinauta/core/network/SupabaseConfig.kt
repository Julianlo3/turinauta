package com.unicauca.turinauta.core.network

import com.unicauca.turinauta.BuildConfig

/**
 * Datos de conexión a Supabase (CONTRATO_API_MVP.md), tomados de local.properties
 * al compilar. Solo se usa la clave publishable; nunca la secret ni service_role.
 */
object SupabaseConfig {
    /** `https://<project-ref>.supabase.co` */
    val url: String = BuildConfig.SUPABASE_URL

    /** Clave `sb_publishable_...`. */
    val publishableKey: String = BuildConfig.SUPABASE_PUBLISHABLE_KEY

    /** Base de la Data API: `<SUPABASE_URL>/rest/v1/`. */
    val restUrl: String get() = url.trimEnd('/') + "/rest/v1/"

    /** Bucket público con las imágenes autorizadas del catálogo (`image_path`). */
    const val CATALOG_IMAGES_BUCKET = "catalog-images"

    /** URL pública de una imagen del catálogo, o null si no tiene. */
    fun catalogImageUrl(imagePath: String?): String? =
        imagePath?.takeIf { it.isNotBlank() }?.let { url.trimEnd('/') + "/storage/v1/object/public/$CATALOG_IMAGES_BUCKET/" + it.trimStart('/') }

    /** false mientras local.properties no tenga ambos valores. */
    val isConfigured: Boolean get() = url.isNotBlank() && publishableKey.isNotBlank()
}
