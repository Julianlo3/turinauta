package com.unicauca.turinauta.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.VisitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaceDetailUiState(
    val place: Place? = null,
    val existingVisit: VisitRecord? = null,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val isAlreadyVisited: Boolean
        get() = existingVisit != null
}

class PlaceDetailViewModel(
    private val placeId: String,
    private val catalogRepository: CatalogRepository,
    private val visitRepository: VisitRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PlaceDetailUiState(isAuthenticated = authRepository.isAuthenticated.value),
    )
    val uiState: StateFlow<PlaceDetailUiState> = _uiState.asStateFlow()

    init {
        observeAuth()
        loadPlaceDetail()
    }

    private fun observeAuth() {
        viewModelScope.launch {
            authRepository.isAuthenticated.collect { authenticated ->
                _uiState.update { it.copy(isAuthenticated = authenticated) }
                if (authenticated) {
                    refreshExistingVisit()
                } else {
                    _uiState.update { it.copy(existingVisit = null) }
                }
            }
        }
    }

    fun loadPlaceDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val placeResult = catalogRepository.getPlaceById(placeId)
            val place = placeResult.getOrNull()
            if (placeResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No pudimos cargar la ficha del lugar. Verifica tu conexión e intenta nuevamente.",
                    )
                }
                return@launch
            }
            if (place == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        place = null,
                        errorMessage = "Este lugar ya no se encuentra disponible en el catálogo.",
                    )
                }
                return@launch
            }

            val existingVisit = if (authRepository.isAuthenticated.value) {
                visitRepository.getVisitForPlace(placeId).getOrNull()
            } else {
                null
            }

            _uiState.update {
                it.copy(
                    place = place,
                    existingVisit = existingVisit,
                    isLoading = false,
                    errorMessage = null,
                )
            }
        }
    }

    fun refreshExistingVisit() {
        viewModelScope.launch {
            if (!authRepository.isAuthenticated.value) {
                _uiState.update { it.copy(existingVisit = null) }
                return@launch
            }
            val existing = visitRepository.getVisitForPlace(placeId).getOrNull()
            _uiState.update { it.copy(existingVisit = existing) }
        }
    }

    companion object {
        fun factory(
            placeId: String,
            catalogRepository: CatalogRepository,
            visitRepository: VisitRepository,
            authRepository: AuthRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PlaceDetailViewModel(
                    placeId = placeId,
                    catalogRepository = catalogRepository,
                    visitRepository = visitRepository,
                    authRepository = authRepository,
                ) as T
        }
    }
}
