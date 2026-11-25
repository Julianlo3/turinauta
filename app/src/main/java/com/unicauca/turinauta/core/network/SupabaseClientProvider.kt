package com.unicauca.turinauta.core.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json

/**
 * Proveedor perezoso y seguro del [SupabaseClient] oficial de Turinauta.
 * Instala únicamente los módulos del contrato MVP: [Auth], [Postgrest] y [Storage].
 */
class SupabaseClientProvider(
    private val url: String = SupabaseConfig.url,
    private val publishableKey: String = SupabaseConfig.publishableKey,
) {
    val isConfigured: Boolean
        get() = url.startsWith("https://") && publishableKey.isNotBlank()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val clientLazy: Lazy<SupabaseClient> = lazy {
        if (!isConfigured) {
            throw SupabaseNotConfiguredException()
        }
        createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = publishableKey,
        ) {
            httpEngine = OkHttp.create()
            defaultSerializer = KotlinXSerializer(json)
            install(Auth) {
                scheme = SupabaseConfig.OAUTH_SCHEME
                host = SupabaseConfig.OAUTH_HOST
            }
            install(Postgrest)
            install(Storage)
        }
    }

    fun requireClient(): SupabaseClient = clientLazy.value

    fun getClientOrNull(): SupabaseClient? = if (isConfigured) clientLazy.value else null
}
