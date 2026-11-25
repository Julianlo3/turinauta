package com.unicauca.turinauta.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.model.Category
import com.unicauca.turinauta.core.model.EditorialStatus
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.PlaceSortOrder
import com.unicauca.turinauta.core.network.SupabaseNotConfiguredException
import com.unicauca.turinauta.data.remote.mapper.haversineDistanceMeters
import com.unicauca.turinauta.data.repository.CatalogRepository
import java.text.Collator
import java.util.Locale
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExploreUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val sortOrder: PlaceSortOrder = PlaceSortOrder.FEATURED,
    val places: List<Place> = emptyList(),
    val distancesMetersByPlaceId: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isBackendNotConfigured: Boolean = false,
    val locationNotice: String? = null,
) {
    val hasActiveFilters: Boolean
        get() = selectedCategoryId != null || searchQuery.isNotBlank()
}

class ExploreViewModel(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private var allPublishedPlaces: List<Place> = emptyList()
    private var userCoordinates: Pair<Double, Double>? = null

    init {
        loadCatalog()
    }

    fun loadCatalog() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    isBackendNotConfigured = false,
                )
            }

            val categoriesDeferred = async { catalogRepository.getCategories() }
            val placesDeferred = async { catalogRepository.getPlaces() }

            val categoriesResult = categoriesDeferred.await()
            val placesResult = placesDeferred.await()

            val failure = placesResult.exceptionOrNull() ?: categoriesResult.exceptionOrNull()
            if (failure != null) {
                val notConfigured = failure is SupabaseNotConfiguredException
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isBackendNotConfigured = notConfigured,
                        errorMessage = if (notConfigured) {
                            "Agrega SUPABASE_URL y SUPABASE_PUBLISHABLE_KEY en local.properties y vuelve a compilar para conectar con el catálogo real."
                        } else {
                            "No pudimos conectar con el catálogo de Turinauta. Verifica tu conexión e intenta nuevamente."
                        },
                    )
                }
                return@launch
            }

            val categories = categoriesResult.getOrDefault(emptyList())
                .sortedBy { it.sortOrder }
            allPublishedPlaces = placesResult.getOrDefault(emptyList())
                .filter { it.editorialStatus == EditorialStatus.PUBLISHED }

            _uiState.update { current ->
                applyFiltersAndSort(
                    state = current.copy(
                        categories = categories,
                        isLoading = false,
                        errorMessage = null,
                        isBackendNotConfigured = false,
                    ),
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { current ->
            applyFiltersAndSort(current.copy(searchQuery = query))
        }
    }

    fun onCategorySelected(categoryId: String?) {
        _uiState.update { current ->
            applyFiltersAndSort(current.copy(selectedCategoryId = categoryId))
        }
    }

    fun onSortOrderSelected(order: PlaceSortOrder) {
        _uiState.update { current ->
            applyFiltersAndSort(
                current.copy(
                    sortOrder = order,
                    locationNotice = if (order != PlaceSortOrder.NEAREST) null else current.locationNotice,
                ),
            )
        }
    }

    fun onUserLocationResolved(latitude: Double, longitude: Double) {
        userCoordinates = latitude to longitude
        _uiState.update { current ->
            applyFiltersAndSort(
                current.copy(
                    sortOrder = PlaceSortOrder.NEAREST,
                    locationNotice = null,
                ),
            )
        }
    }

    fun onLocationUnavailable(message: String) {
        _uiState.update { it.copy(locationNotice = message) }
    }

    fun dismissLocationNotice() {
        _uiState.update { it.copy(locationNotice = null) }
    }

    fun clearFilters() {
        _uiState.update { current ->
            applyFiltersAndSort(
                current.copy(
                    selectedCategoryId = null,
                    searchQuery = "",
                ),
            )
        }
    }

    private fun applyFiltersAndSort(state: ExploreUiState): ExploreUiState {
        val query = state.searchQuery.trim().lowercase(Locale.forLanguageTag("es-CO"))
        val filtered = allPublishedPlaces.filter { place ->
            val matchesCategory = state.selectedCategoryId == null ||
                place.categoryId == state.selectedCategoryId
            val matchesQuery = query.isEmpty() ||
                place.name.lowercase(Locale.forLanguageTag("es-CO")).contains(query) ||
                place.summary.lowercase(Locale.forLanguageTag("es-CO")).contains(query)
            matchesCategory && matchesQuery
        }

        val coords = userCoordinates
        val distances = if (coords != null) {
            filtered.associate { place ->
                place.id to haversineDistanceMeters(
                    lat1 = coords.first,
                    lon1 = coords.second,
                    lat2 = place.latitude,
                    lon2 = place.longitude,
                )
            }
        } else {
            emptyMap()
        }

        val spanishCollator = Collator.getInstance(Locale.forLanguageTag("es-CO")).apply {
            strength = Collator.PRIMARY
        }

        val sorted = when (state.sortOrder) {
            PlaceSortOrder.FEATURED -> filtered.sortedWith(
                compareBy<Place>({ it.sortOrder }, { it.name.lowercase(Locale.forLanguageTag("es-CO")) }),
            )
            PlaceSortOrder.NAME_ASC -> filtered.sortedWith { a, b ->
                spanishCollator.compare(a.name, b.name)
            }
            PlaceSortOrder.NEAREST -> if (coords != null) {
                filtered.sortedBy { distances[it.id] ?: Double.MAX_VALUE }
            } else {
                filtered.sortedBy { it.sortOrder }
            }
        }

        return state.copy(
            places = sorted,
            distancesMetersByPlaceId = distances,
        )
    }

    companion object {
        fun factory(catalogRepository: CatalogRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ExploreViewModel(catalogRepository) as T
                }
            }
    }
}
