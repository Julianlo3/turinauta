package com.unicauca.turinauta.core.model

/**
 * Perfil privado del usuario autenticado.
 * Alineado con `GET /profiles?select=id,display_name,created_at` + correo obtenido de Supabase Auth (Google OAuth).
 * El móvil solo consulta el perfil y no lo edita.
 */
data class UserProfile(
    val id: String,
    val displayName: String,
    val email: String? = null,
    val createdAtIso: String? = null,
) {
    companion object {
        const val DEFAULT_DISPLAY_NAME = "Explorador"
    }
}
