package com.unicauca.turinauta.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.model.UserProfile
import com.unicauca.turinauta.core.network.SupabaseNotConfiguredException
import com.unicauca.turinauta.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val user: UserProfile? = null,
    val isAuthenticated: Boolean = false,
    val isLaunchingGoogle: Boolean = false,
    val errorMessage: String? = null,
    val isBackendNotConfigured: Boolean = false,
)

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            user = authRepository.currentUser.value,
            isAuthenticated = authRepository.isAuthenticated.value,
        ),
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { profile ->
                _uiState.update { current ->
                    current.copy(
                        user = profile,
                        isAuthenticated = profile != null,
                        isLaunchingGoogle = if (profile != null) false else current.isLaunchingGoogle,
                        errorMessage = if (profile != null) null else current.errorMessage,
                    )
                }
            }
        }
    }

    fun onContinueWithGoogleClick() {
        if (_uiState.value.isLaunchingGoogle) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLaunchingGoogle = true,
                    errorMessage = null,
                    isBackendNotConfigured = false,
                )
            }

            authRepository.signInWithGoogle()
                .onSuccess {
                    _uiState.update { it.copy(isLaunchingGoogle = false) }
                }
                .onFailure { error ->
                    val notConfigured = error is SupabaseNotConfiguredException
                    _uiState.update {
                        it.copy(
                            isLaunchingGoogle = false,
                            isBackendNotConfigured = notConfigured,
                            errorMessage = if (notConfigured) {
                                "Agrega SUPABASE_URL y SUPABASE_PUBLISHABLE_KEY en local.properties para iniciar sesión con Google."
                            } else {
                                "No pudimos abrir el inicio de sesión con Google. Verifica tu conexión e intenta nuevamente."
                            },
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AuthViewModel(authRepository) as T
            }
    }
}
