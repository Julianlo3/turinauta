package com.unicauca.turinauta.feature.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.model.CollectionProgress
import com.unicauca.turinauta.core.network.SupabaseNotConfiguredException
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CollectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CollectionUiState(
    val collections: List<CollectionProgress> = emptyList(),
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isBackendNotConfigured: Boolean = false,
) {
    val totalStamps: Int
        get() = collections.sumOf { it.totalStamps }

    val earnedStamps: Int
        get() = collections.sumOf { it.earnedStamps }

    val overallProgressPercent: Int
        get() = CollectionProgress.calculatePercent(earnedStamps, totalStamps)
}

class CollectionViewModel(
    private val collectionRepository: CollectionRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private var hasBeenVisible = false
    private var requestVersion = 0L

    private val _uiState = MutableStateFlow(
        CollectionUiState(isAuthenticated = authRepository.isAuthenticated.value),
    )
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.isAuthenticated.collectLatest { authenticated ->
                _uiState.update { it.copy(isAuthenticated = authenticated) }
                loadCollections(authenticated)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            loadCollections(authRepository.isAuthenticated.value)
        }
    }

    fun onScreenVisible() {
        if (hasBeenVisible) refresh() else hasBeenVisible = true
    }

    private suspend fun loadCollections(authenticated: Boolean) {
        val version = ++requestVersion
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                isBackendNotConfigured = false,
            )
        }

        val result = if (authenticated) {
            collectionRepository.getMyCollectionProgress()
        } else {
            collectionRepository.getPublicCollections()
        }
        if (version != requestVersion || authRepository.isAuthenticated.value != authenticated) return

        result
            .onSuccess { list ->
                _uiState.update {
                    it.copy(
                        collections = list.sortedBy { col -> col.sortOrder },
                        isLoading = false,
                        errorMessage = null,
                        isBackendNotConfigured = false,
                    )
                }
            }
            .onFailure { error ->
                val notConfigured = error is SupabaseNotConfiguredException
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isBackendNotConfigured = notConfigured,
                        errorMessage = if (notConfigured) {
                            "Agrega SUPABASE_URL y SUPABASE_PUBLISHABLE_KEY en local.properties para consultar tus colecciones."
                        } else {
                            "No pudimos cargar las colecciones en este momento. Verifica tu conexión e intenta nuevamente."
                        },
                    )
                }
            }
    }

    companion object {
        fun factory(
            collectionRepository: CollectionRepository,
            authRepository: AuthRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CollectionViewModel(
                    collectionRepository = collectionRepository,
                    authRepository = authRepository,
                ) as T
        }
    }
}
