package com.unicauca.turinauta.data.remote

import com.unicauca.turinauta.core.model.UserProfile
import com.unicauca.turinauta.core.network.SupabaseClientProvider
import com.unicauca.turinauta.data.remote.dto.ProfileDto
import com.unicauca.turinauta.data.remote.mapper.toDomain
import com.unicauca.turinauta.data.repository.AuthRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

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
                            // Provisión inmediata con metadatos de Google antes de consultar `profiles`
                            if (_currentUser.value?.id != user.id || _currentUser.value?.avatarUrl == null) {
                                _currentUser.value = UserProfile(
                                    id = user.id,
                                    displayName = user.extractGoogleDisplayName()
                                        ?: UserProfile.DEFAULT_DISPLAY_NAME,
                                    email = user.email,
                                    avatarUrl = user.extractGoogleAvatarUrl(),
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

        val avatarUrl = authUser.extractGoogleAvatarUrl()
        val fallbackName = authUser.extractGoogleDisplayName() ?: UserProfile.DEFAULT_DISPLAY_NAME

        val profile = profileDto?.toDomain(
            email = authUser.email,
            avatarUrl = avatarUrl,
        ) ?: UserProfile(
            id = authUser.id,
            displayName = fallbackName,
            email = authUser.email,
            avatarUrl = avatarUrl,
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

    private fun UserInfo.extractGoogleAvatarUrl(): String? {
        val fromMeta = userMetadata.firstHttpUrl("avatar_url", "picture")
        if (fromMeta != null) return fromMeta
        return identities
            ?.firstNotNullOfOrNull { it.identityData.firstHttpUrl("avatar_url", "picture") }
    }

    private fun UserInfo.extractGoogleDisplayName(): String? {
        val fromMeta = userMetadata.firstNonBlankString("full_name", "name")
        if (fromMeta != null) return fromMeta
        return identities
            ?.firstNotNullOfOrNull { it.identityData.firstNonBlankString("full_name", "name") }
    }

    private fun JsonObject?.firstHttpUrl(vararg keys: String): String? {
        if (this == null) return null
        return keys.firstNotNullOfOrNull { key ->
            runCatching { this[key]?.jsonPrimitive?.contentOrNull?.trim() }
                .getOrNull()
                ?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
        }
    }

    private fun JsonObject?.firstNonBlankString(vararg keys: String): String? {
        if (this == null) return null
        return keys.firstNotNullOfOrNull { key ->
            runCatching { this[key]?.jsonPrimitive?.contentOrNull?.trim() }
                .getOrNull()
                ?.takeIf { it.isNotEmpty() }
        }
    }
}
