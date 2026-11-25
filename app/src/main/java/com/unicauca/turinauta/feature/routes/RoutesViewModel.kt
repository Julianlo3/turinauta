package com.unicauca.turinauta.feature.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.model.EditorialStatus
import com.unicauca.turinauta.core.model.Route
import com.unicauca.turinauta.core.network.SupabaseNotConfiguredException
import com.unicauca.turinauta.data.repository.RouteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoutesUiState(
    val routes: List<Route> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isBackendNotConfigured: Boolean = false,
)

class RoutesViewModel(
    private val routeRepository: RouteRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutesUiState())
    val uiState: StateFlow<RoutesUiState> = _uiState.asStateFlow()

    init {
        loadRoutes()
    }

    fun loadRoutes() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    isBackendNotConfigured = false,
                )
            }

            routeRepository.getAvailableRoutes()
                .onSuccess { list ->
                    val visible = list
                        .filter { it.editorialStatus == EditorialStatus.PUBLISHED }
                        .sortedBy { it.sortOrder }
                    _uiState.update {
                        it.copy(
                            routes = visible,
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
                                "Agrega SUPABASE_URL y SUPABASE_PUBLISHABLE_KEY en local.properties para cargar las rutas temáticas."
                            } else {
                                "No pudimos cargar las rutas temáticas. Verifica tu conexión e intenta nuevamente."
                            },
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(routeRepository: RouteRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    RoutesViewModel(routeRepository) as T
            }
    }
}
