package com.unicauca.turinauta.feature.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.Route
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.RouteRepository
import com.unicauca.turinauta.data.repository.VisitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RouteStopItem(
    val orderNumber: Int,
    val place: Place,
    val isVisited: Boolean,
)

data class RouteDetailUiState(
    val route: Route? = null,
    val stops: List<RouteStopItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class RouteDetailViewModel(
    private val routeId: String,
    private val routeRepository: RouteRepository,
    private val catalogRepository: CatalogRepository,
    private val visitRepository: VisitRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteDetailUiState())
    val uiState: StateFlow<RouteDetailUiState> = _uiState.asStateFlow()

    init {
        loadRouteDetail()
    }

    fun loadRouteDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val routeResult = routeRepository.getRouteById(routeId)
            if (routeResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No pudimos cargar el detalle de la ruta. Verifica tu conexión e intenta de nuevo.",
                    )
                }
                return@launch
            }

            val route = routeResult.getOrNull()
            if (route == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        route = null,
                        errorMessage = "Esta ruta ya no está disponible.",
                    )
                }
                return@launch
            }

            val orderedIds = route.orderedPlaceIds
            val placesResult = catalogRepository.getPlacesByIds(orderedIds)
            if (placesResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        route = route,
                        errorMessage = "No pudimos cargar las paradas de esta ruta. Intenta nuevamente.",
                    )
                }
                return@launch
            }

            val visitedIds = if (authRepository.isAuthenticated.value) {
                visitRepository.getMyVisitHistory()
                    .getOrDefault(emptyList())
                    .map { it.placeId }
                    .toSet()
            } else {
                emptySet()
            }

            val placesById = placesResult.getOrDefault(emptyList()).associateBy { it.id }
            val orderedStops = route.stops
                .sortedBy { it.suggestedOrder }
                .mapIndexedNotNull { index, stop ->
                    val place = placesById[stop.placeId] ?: return@mapIndexedNotNull null
                    RouteStopItem(
                        orderNumber = index + 1,
                        place = place,
                        isVisited = place.id in visitedIds,
                    )
                }

            _uiState.update {
                it.copy(
                    route = route,
                    stops = orderedStops,
                    isLoading = false,
                    errorMessage = null,
                )
            }
        }
    }

    companion object {
        fun factory(
            routeId: String,
            routeRepository: RouteRepository,
            catalogRepository: CatalogRepository,
            visitRepository: VisitRepository,
            authRepository: AuthRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RouteDetailViewModel(
                    routeId = routeId,
                    routeRepository = routeRepository,
                    catalogRepository = catalogRepository,
                    visitRepository = visitRepository,
                    authRepository = authRepository,
                ) as T
        }
    }
}
