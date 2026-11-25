package com.unicauca.turinauta.data.repository

import com.unicauca.turinauta.core.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

/**
 * Repositorio de autenticación privada mediante Google OAuth en Supabase Auth.
 * Al cerrar sesión limpia inmediatamente [currentUser] en memoria (HU-25).
 */
interface AuthRepository {
    val currentUser: StateFlow<UserProfile?>
    val isAuthenticated: StateFlow<Boolean>

    suspend fun signInWithGoogle(): Result<Unit>

    suspend fun refreshProfile(): Result<UserProfile?>

    suspend fun signOut(): Result<Unit>
}
