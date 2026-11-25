package com.unicauca.turinauta.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import android.os.CancellationSignal
import com.unicauca.turinauta.core.model.GeoPoint
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Ubicación actual del dispositivo. Solo se consulta en el momento en que hace falta. */
interface LocationProvider {
    fun hasPermission(): Boolean
    /** null si no hay permiso, el GPS está apagado o no responde a tiempo. */
    suspend fun currentLocation(): GeoPoint?
}

/** Implementación con LocationManager (sin Google Play Services). */
class DeviceLocationProvider(private val context: Context) : LocationProvider {

    override fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    override suspend fun currentLocation(): GeoPoint? {
        if (!hasPermission()) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .firstOrNull { manager.isProviderEnabled(it) } ?: return null
        val location: Location? = withTimeoutOrNull(15_000) {
            suspendCancellableCoroutine { cont ->
                val signal = CancellationSignal()
                cont.invokeOnCancellation { signal.cancel() }
                LocationManagerCompat.getCurrentLocation(manager, provider, signal, Executors.newSingleThreadExecutor()) { loc ->
                    if (cont.isActive) cont.resume(loc)
                }
            }
        } ?: manager.getLastKnownLocation(provider)
        return location?.let { GeoPoint(it.latitude, it.longitude) }
    }
}

/** Distancia en metros entre dos puntos (fórmula del haversine). */
fun GeoPoint.distanceMetersTo(other: GeoPoint): Double {
    val r = 6_371_000.0
    val dLat = Math.toRadians(other.latitude - latitude)
    val dLon = Math.toRadians(other.longitude - longitude)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(latitude)) * cos(Math.toRadians(other.latitude)) * sin(dLon / 2) * sin(dLon / 2)
    return 2 * r * atan2(sqrt(a), sqrt(1 - a))
}
