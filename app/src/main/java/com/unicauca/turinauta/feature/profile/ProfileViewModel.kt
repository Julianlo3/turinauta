package com.unicauca.turinauta.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.model.UserProfile
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CollectionRepository
import com.unicauca.turinauta.data.repository.VisitRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserProfile? = null,
    val isAuthenticated: Boolean = false,
    val visitedPlacesCount: Int = 0,
    val earnedStampsCount: Int = 0,
    val isLoadingStats: Boolean = false,
    val isSigningOut: Boolean = false,
    val errorMessage: String? = null,
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val visitRepository: VisitRepository,
    private val collectionRepository: CollectionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            user = authRepository.currentUser.value,
            isAuthenticated = authRepository.isAuthenticated.value,
        ),
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user == null) {
                    // HU-25: limpiar inmediatamente el estado privado al cerrar sesión
                    _uiState.value = ProfileUiState(
                        user = null,
                        isAuthenticated = false,
                    )
                } else {
                    _uiState.update {
                        it.copy(
                            user = user,
                            isAuthenticated = true,
                            errorMessage = null,
                        )
                    }
                    loadPrivateStats()
                }
            }
        }
    }

    fun refresh() {
        if (!authRepository.isAuthenticated.value) return
        viewModelScope.launch {
            authRepository.refreshProfile()
            loadPrivateStats()
        }
    }

    private fun loadPrivateStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStats = true, errorMessage = null) }
            val visitsDeferred = async { visitRepository.getMyVisitHistory() }
            val collectionsDeferred = async { collectionRepository.getMyCollectionProgress() }

            val visitsResult = visitsDeferred.await()
            val collectionsResult = collectionsDeferred.await()

            val visits = visitsResult.getOrDefault(emptyList())
            val collections = collectionsResult.getOrDefault(emptyList())
            val failure = visitsResult.exceptionOrNull() ?: collectionsResult.exceptionOrNull()

            _uiState.update { current ->
                if (!current.isAuthenticated) {
                    ProfileUiState()
                } else {
                    current.copy(
                        visitedPlacesCount = visits.size,
                        earnedStampsCount = collections.sumOf { it.earnedStamps },
                        isLoadingStats = false,
                        errorMessage = if (failure != null) {
                            "No pudimos actualizar tu resumen privado. Intenta nuevamente."
                        } else {
                            null
                        },
                    )
                }
            }
        }
    }

    fun signOut() {
        if (_uiState.value.isSigningOut) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningOut = true) }
            authRepository.signOut()
            // Limpieza inmediata del estado privado en memoria (HU-25)
            _uiState.value = ProfileUiState(
                user = null,
                isAuthenticated = false,
                visitedPlacesCount = 0,
                earnedStampsCount = 0,
                isSigningOut = false,
            )
        }
    }

    companion object {
        fun factory(
            authRepository: AuthRepository,
            visitRepository: VisitRepository,
            collectionRepository: CollectionRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProfileViewModel(
                    authRepository = authRepository,
                    visitRepository = visitRepository,
                    collectionRepository = collectionRepository,
                ) as T
        }
    }
}
