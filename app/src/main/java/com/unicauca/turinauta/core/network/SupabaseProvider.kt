package com.unicauca.turinauta.core.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.ExternalAuthAction
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/** Esquema y host del deep link que Supabase usa para devolver la sesión de Google. */
const val AUTH_CALLBACK_SCHEME = "com.unicauca.turinauta"
const val AUTH_CALLBACK_HOST = "login-callback"
const val AUTH_REDIRECT_URL = "$AUTH_CALLBACK_SCHEME://$AUTH_CALLBACK_HOST"

/**
 * Cliente único de Supabase. La URL y la clave publishable vienen de local.properties
 * (ver [SupabaseConfig]). La sesión se guarda en el dispositivo y se renueva sola.
 */
fun createTurinautaSupabaseClient(): SupabaseClient {
    check(SupabaseConfig.isConfigured) {
        "Falta SUPABASE_URL o SUPABASE_PUBLISHABLE_KEY en local.properties."
    }
    return createSupabaseClient(
        supabaseUrl = SupabaseConfig.url.trimEnd('/'),
        supabaseKey = SupabaseConfig.publishableKey,
    ) {
        install(Auth) {
            // Flujo del contrato: Supabase → Google → callback de Supabase → deep link a la app.
            scheme = AUTH_CALLBACK_SCHEME
            host = AUTH_CALLBACK_HOST
            // PKCE: el deep link trae un código de un solo uso que la app canjea por la sesión.
            flowType = FlowType.PKCE
            // Pestaña del navegador dentro de la app (Custom Tab) en lugar de abrir Chrome aparte.
            defaultExternalAuthAction = ExternalAuthAction.CustomTabs()
        }
        install(Postgrest)
        install(Storage)
    }
}
