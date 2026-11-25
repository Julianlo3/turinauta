package com.unicauca.turinauta.feature.explore

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.data.repository.MapPoint
import com.unicauca.turinauta.data.repository.WalkingRoute
import com.unicauca.turinauta.data.repository.WalkingRouteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class PlaceMapUiState(
    val userLocation: MapPoint? = null,
    val route: WalkingRoute? = null,
    val loading: Boolean = false,
    val message: String? = null,
)

class PlaceMapViewModel(private val repository: WalkingRouteRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(PlaceMapUiState())
    val state = mutableState.asStateFlow()

    fun load(context: Context, place: Place) {
        if (mutableState.value.loading) return
        viewModelScope.launch {
            mutableState.update { it.copy(loading = true, route = null, message = null) }
            val location = runCatching { currentLocation(context) }.getOrNull()
            if (location == null) {
                mutableState.update { it.copy(loading = false, message = "No obtuvimos tu ubicación. Activa el GPS e intenta de nuevo.") }
                return@launch
            }
            val origin = MapPoint(location.latitude, location.longitude)
            mutableState.update { it.copy(userLocation = origin) }
            runCatching { repository.getRoute(origin, MapPoint(place.latitude, place.longitude)) }
                .onSuccess { route -> mutableState.update { it.copy(route = route, loading = false) } }
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

private suspend fun currentLocation(context: Context): Location? {
    val finePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasPermission = finePermission || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!hasPermission) return null
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    val fallback = providers.mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
        .filter { System.currentTimeMillis() - it.time in 0..120_000 }
        .maxByOrNull { it.time }
    val preferredProviders = if (finePermission) providers else listOf(LocationManager.NETWORK_PROVIDER)
    val provider = preferredProviders.firstOrNull { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
        ?: return fallback
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return fallback
    return suspendCancellableCoroutine { continuation ->
        try {
            manager.getCurrentLocation(provider, null, ContextCompat.getMainExecutor(context)) { location ->
                if (continuation.isActive) continuation.resume(location ?: fallback)
            }
        } catch (_: SecurityException) {
            if (continuation.isActive) continuation.resume(fallback)
        }
    }
}
