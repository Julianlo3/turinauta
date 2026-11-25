package com.unicauca.turinauta.feature.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.model.EditorialStatus
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.Route
import com.unicauca.turinauta.core.network.SupabaseNotConfiguredException
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.RouteRepository
import com.unicauca.turinauta.data.repository.VisitRepository
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Modelo enriquecido para mostrar cada ruta con sus paradas reales, fotos y progreso.
 */
data class RouteCardItem(
    val route: Route,
    val stopPlaces: List<Place> = emptyList(),
    val visitedStopsCount: Int = 0,
) {
    val totalStopsCount: Int
        get() = maxOf(route.stops.size, stopPlaces.size)

    val categoriesOverline: String
        get() = stopPlaces
            .mapNotNull { it.categoryName.trim().takeIf { name -> name.isNotEmpty() } }
            .distinct()
            .take(3)
            .joinToString(" · ") { it.uppercase(Locale.forLanguageTag("es-CO")) }
}

data class RoutesUiState(
    val routeItems: List<RouteCardItem> = emptyList(),
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isBackendNotConfigured: Boolean = false,
) {
    val routes: List<Route>
        get() = routeItems.map { it.route }

    val totalPlacesAcrossRoutes: Int
        get() = routeItems.flatMap { item ->
            item.stopPlaces.map { it.id }.ifEmpty { item.route.orderedPlaceIds }
        }.distinct().size
}

class RoutesViewModel(
    private val routeRepository: RouteRepository,
    private val catalogRepository: CatalogRepository? = null,
    private val visitRepository: VisitRepository? = null,
    private val authRepository: AuthRepository? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RoutesUiState(isAuthenticated = authRepository?.isAuthenticated?.value == true),
    )
    val uiState: StateFlow<RoutesUiState> = _uiState.asStateFlow()

    init {
        loadRoutes()
        observeAuthChanges()
    }

    private fun observeAuthChanges() {
        val auth = authRepository ?: return
        viewModelScope.launch {
            var initialized = false
            auth.isAuthenticated.collect { authenticated ->
                _uiState.update { it.copy(isAuthenticated = authenticated) }
                if (initialized) {
                    loadRoutes()
                }
                initialized = true
            }
        }
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

                    val allPlaceIds = visible
                        .flatMap { it.orderedPlaceIds }
                        .distinct()

                    val placesById = if (catalogRepository != null && allPlaceIds.isNotEmpty()) {
                        catalogRepository.getPlacesByIds(allPlaceIds)
                            .getOrDefault(emptyList())
                            .associateBy { it.id }
                    } else {
                        emptyMap()
                    }

                    val authenticated = authRepository?.isAuthenticated?.value == true
                    val visitedPlaceIds = if (authenticated && visitRepository != null) {
                        visitRepository.getMyVisitHistory()
                            .getOrDefault(emptyList())
                            .map { it.placeId }
                            .toSet()
                    } else {
                        emptySet()
                    }

                    val items = visible.map { route ->
                        val orderedPlaces = route.orderedPlaceIds.mapNotNull { placesById[it] }
                        val visitedCount = route.orderedPlaceIds.count { it in visitedPlaceIds }
                        RouteCardItem(
                            route = route,
                            stopPlaces = orderedPlaces,
                            visitedStopsCount = visitedCount,
                        )
                    }

                    _uiState.update {
                        it.copy(
                            routeItems = items,
                            isAuthenticated = authenticated,
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
        fun factory(
            routeRepository: RouteRepository,
            catalogRepository: CatalogRepository,
            visitRepository: VisitRepository,
            authRepository: AuthRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RoutesViewModel(
                    routeRepository = routeRepository,
                    catalogRepository = catalogRepository,
                    visitRepository = visitRepository,
                    authRepository = authRepository,
                ) as T
        }
    }
}
