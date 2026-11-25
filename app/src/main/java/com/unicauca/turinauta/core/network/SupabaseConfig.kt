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

    /** false mientras local.properties no tenga ambos valores: la app sigue con los repositorios Fake. */
    val isConfigured: Boolean get() = url.isNotBlank() && publishableKey.isNotBlank()
}
