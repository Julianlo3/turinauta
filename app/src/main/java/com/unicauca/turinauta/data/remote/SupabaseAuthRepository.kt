package com.unicauca.turinauta.data.remote

import com.unicauca.turinauta.core.model.UserProfile
import com.unicauca.turinauta.core.network.SupabaseClientProvider
import com.unicauca.turinauta.data.remote.dto.ProfileDto
import com.unicauca.turinauta.data.remote.mapper.toDomain
import com.unicauca.turinauta.data.repository.AuthRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Autenticación real con Google OAuth vía Supabase Auth.
 * Escucha `sessionStatus` para refrescar `profiles` al iniciar sesión y limpiar
 * inmediatamente la información privada al cerrarla (HU-25).
 */
class SupabaseAuthRepository(
    private val clientProvider: SupabaseClientProvider,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : AuthRepository {

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    override val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    override val isAuthenticated: StateFlow<Boolean> = _currentUser
        .map { it != null }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    init {
        observeSessionStatusIfConfigured()
    }

    private fun observeSessionStatusIfConfigured() {
        val client = clientProvider.getClientOrNull() ?: return
        scope.launch {
            client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = status.session.user
                        if (user != null) {
                            // Provisión inmediata antes de consultar `profiles`
                            if (_currentUser.value?.id != user.id) {
                                _currentUser.value = UserProfile(
                                    id = user.id,
                                    displayName = UserProfile.DEFAULT_DISPLAY_NAME,
                                    email = user.email,
                                )
                            }
                            refreshProfile()
                        }
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _currentUser.value = null
                    }
                    else -> Unit
                }
            }
        }
    }

    override suspend fun signInWithGoogle(): Result<Unit> = runCatching {
        val client = clientProvider.requireClient()
        client.auth.signInWith(Google)
    }

    override suspend fun refreshProfile(): Result<UserProfile?> = runCatching {
        val client = clientProvider.requireClient()
        val authUser = client.auth.currentUserOrNull()
        if (authUser == null) {
            _currentUser.value = null
            return@runCatching null
        }

        val profileDto = client.postgrest["profiles"]
            .select(columns = Columns.list("id", "display_name", "created_at")) {
                filter { eq("id", authUser.id) }
                limit(1)
            }
            .decodeList<ProfileDto>()
            .firstOrNull()

        val profile = profileDto?.toDomain(email = authUser.email)
            ?: UserProfile(
                id = authUser.id,
                displayName = UserProfile.DEFAULT_DISPLAY_NAME,
                email = authUser.email,
            )

        _currentUser.value = profile
        profile
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        // Limpieza inmediata del estado privado en memoria (HU-25)
        _currentUser.value = null
        val client = clientProvider.getClientOrNull() ?: return@runCatching
        client.auth.signOut()
    }
}
