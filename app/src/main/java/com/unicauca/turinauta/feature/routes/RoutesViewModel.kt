package com.unicauca.turinauta.feature.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.Route
import com.unicauca.turinauta.data.repository.RouteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoutesUiState(
    val isLoading: Boolean = true,
    val routes: List<Route> = emptyList(),
)

data class RouteDetailUiState(
    val isLoading: Boolean = true,
    val route: Route? = null,
    val places: List<Place> = emptyList(),
)

/** Rutas temáticas publicadas (HU-08 a HU-10). */
class RoutesViewModel(private val repository: RouteRepository) : ViewModel() {

    private val _list = MutableStateFlow(RoutesUiState())
    val list: StateFlow<RoutesUiState> = _list.asStateFlow()

    private val _detail = MutableStateFlow(RouteDetailUiState())
    val detail: StateFlow<RouteDetailUiState> = _detail.asStateFlow()

    fun loadRoutes() {
        viewModelScope.launch {
            _list.update { it.copy(isLoading = it.routes.isEmpty()) }
            val routes = repository.getRoutes()
            _list.update { RoutesUiState(isLoading = false, routes = routes) }
        }
    }

    fun loadRoute(routeId: String) {
        if (_detail.value.route?.id == routeId) return
        viewModelScope.launch {
            _detail.update { RouteDetailUiState(isLoading = true) }
            val route = repository.getRoute(routeId)
            val places = if (route != null) repository.getRoutePlaces(routeId) else emptyList()
            _detail.update { RouteDetailUiState(isLoading = false, route = route, places = places) }
        }
    }
}
