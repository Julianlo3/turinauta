package com.unicauca.turinauta.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.location.LocationProvider
import com.unicauca.turinauta.core.location.distanceMetersTo
import com.unicauca.turinauta.core.model.Category
import com.unicauca.turinauta.core.model.GeoPoint
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.VisitRepository
import com.unicauca.turinauta.data.supabase.friendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Collator
import java.text.Normalizer
import java.util.Locale

enum class SortOrder(val label: String, val description: String) {
    FEATURED("Orden habitual", "Así se muestra el catálogo por defecto"),
    NAME_AZ("Nombre A–Z", "Orden alfabético por nombre del lugar"),
    /** Opcional: Android ordena con la ubicación del dispositivo; no se guarda (contrato API). */
    NEAREST("Más cercanos primero", "Opcional · requiere tu ubicación aproximada"),
}

data class ExploreUiState(
    val isLoading: Boolean = true,
    val allPlaces: List<Place> = emptyList(),
    val categories: List<Category> = emptyList(),
    val loadError: String? = null,
    val query: String = "",
    val selectedCategory: Category? = null,
    val sortOrder: SortOrder = SortOrder.FEATURED,
    val userLocation: GeoPoint? = null,
    val isLocating: Boolean = false,
    /** Banner informativo cuando se negó la ubicación: se sigue con el orden habitual. */
    val locationUnavailable: Boolean = false,
    val message: String? = null,
    /** Lugares que la cuenta ya registró (vacío sin sesión). */
    val visitedPlaceIds: Set<String> = emptySet(),
) {
    val visiblePlaces: List<Place>
        get() {
            val collator = Collator.getInstance(Locale.forLanguageTag("es-CO")).apply { strength = Collator.PRIMARY }
            val q = query.trim().normalized()
            return allPlaces
                .filter { selectedCategory == null || it.category == selectedCategory }
                .filter { q.isEmpty() || it.name.normalized().contains(q) }
                .let { list ->
                    when (sortOrder) {
                        SortOrder.FEATURED -> list.sortedBy { it.sortOrder }
                        SortOrder.NAME_AZ -> list.sortedWith { a, b -> collator.compare(a.name, b.name) }
                        SortOrder.NEAREST -> userLocation?.let { me -> list.sortedBy { me.distanceMetersTo(it.location) } }
                            ?: list.sortedBy { it.sortOrder }
                    }
                }
        }

    fun distanceTo(place: Place): Double? =
        if (sortOrder == SortOrder.NEAREST) userLocation?.distanceMetersTo(place.location) else null

    val isCatalogEmpty: Boolean get() = !isLoading && loadError == null && allPlaces.isEmpty()
    val isSearching: Boolean get() = query.isNotBlank()

    /** El primer lugar se destaca en grande solo en la vista inicial del catálogo. */
    val showFeatured: Boolean get() = !isSearching && selectedCategory == null && sortOrder == SortOrder.FEATURED
}

/** Compara sin mayúsculas ni tildes («cafe» encuentra «Café»). */
private fun String.normalized(): String =
    Normalizer.normalize(lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")

class ExploreViewModel(
    private val catalog: CatalogRepository,
    private val location: LocationProvider,
    visits: VisitRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            visits.visits.collect { list -> _uiState.update { it.copy(visitedPlaceIds = list.map { v -> v.placeId }.toSet()) } }
        }
    }

    /** Recarga el catálogo; solo muestra carga a pantalla completa si aún no hay datos. */
    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.allPlaces.isEmpty(), loadError = null) }
            try {
                val places = catalog.getPlaces()
                val categories = places.map { it.category }.distinctBy { it.id }.sortedBy { it.sortOrder }
                _uiState.update { it.copy(isLoading = false, allPlaces = places, categories = categories) }
            } catch (e: Exception) {
                // Si ya había datos se conservan; si no, se muestra el error con «Reintentar».
                _uiState.update {
                    if (it.allPlaces.isEmpty()) it.copy(isLoading = false, loadError = e.friendlyMessage())
                    else it.copy(isLoading = false, message = e.friendlyMessage())
                }
            }
        }
    }

    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query) }
    fun clearQuery() = _uiState.update { it.copy(query = "") }
    fun selectCategory(category: Category?) = _uiState.update { it.copy(selectedCategory = category) }

    fun setSortOrder(order: SortOrder) = _uiState.update { it.copy(sortOrder = order) }

    fun hasLocationPermission(): Boolean = location.hasPermission()

    /** Resultado de la solicitud de permiso para «Más cercanos». */
    fun onNearestPermissionResult(granted: Boolean) {
        if (!granted) {
            _uiState.update {
                it.copy(sortOrder = SortOrder.FEATURED, locationUnavailable = true, message = "Ubicación no concedida")
            }
            return
        }
        _uiState.update { it.copy(isLocating = true, locationUnavailable = false) }
        viewModelScope.launch {
            val point = location.currentLocation()
            _uiState.update {
                if (point == null) it.copy(
                    isLocating = false, sortOrder = SortOrder.FEATURED, locationUnavailable = true,
                    message = "No pudimos obtener tu ubicación",
                )
                else it.copy(isLocating = false, sortOrder = SortOrder.NEAREST, userLocation = point)
            }
        }
    }

    fun dismissLocationBanner() = _uiState.update { it.copy(locationUnavailable = false) }
    fun onMessageShown() = _uiState.update { it.copy(message = null) }
}
