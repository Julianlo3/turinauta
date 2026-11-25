package com.unicauca.turinauta.feature.explore

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.location.LocationPurpose
import com.unicauca.turinauta.core.location.getDeviceLocation
import com.unicauca.turinauta.data.repository.MapPoint
import com.unicauca.turinauta.data.repository.WalkingRoute
import com.unicauca.turinauta.data.repository.WalkingRouteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaceMapUiState(
    val userLocation: MapPoint? = null,
    val route: WalkingRoute? = null,
    val loading: Boolean = false,
    val locating: Boolean = false,
    val message: String? = null,
)

class PlaceMapViewModel(private val repository: WalkingRouteRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(PlaceMapUiState())
    val state = mutableState.asStateFlow()
    private var lastRouteAtMillis = 0L

    fun load(context: Context, place: Place) {
        if (mutableState.value.loading) return
        if (mutableState.value.route != null && System.currentTimeMillis() - lastRouteAtMillis < 60_000) return
        viewModelScope.launch {
            mutableState.update { it.copy(loading = true, locating = true, route = null, message = null) }
            val location = runCatching { getDeviceLocation(context, LocationPurpose.MAP) }.getOrNull()
            if (location == null) {
                mutableState.update { it.copy(loading = false, locating = false, userLocation = null, message = "No obtuvimos una ubicación reciente. Activa el GPS e intenta de nuevo.") }
                return@launch
            }
            val origin = MapPoint(location.latitude, location.longitude)
            mutableState.update { it.copy(userLocation = origin, locating = false) }
            runCatching { repository.getRoute(origin, MapPoint(place.latitude, place.longitude)) }
                .onSuccess { route ->
                    lastRouteAtMillis = System.currentTimeMillis()
                    mutableState.update { it.copy(route = route, loading = false) }
                }
                .onFailure { error -> mutableState.update {
                    it.copy(loading = false, message = error.message ?: "No pudimos cargar la ruta a pie. Intenta de nuevo.")
                } }
        }
    }

    companion object {
        fun factory(repository: WalkingRouteRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = PlaceMapViewModel(repository) as T
            }
    }
}
