package com.unicauca.turinauta.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

enum class LocationPurpose(val maxAgeMillis: Long, val maxAccuracyMeters: Float) {
    MAP(90_000, 200f),
    VISIT(30_000, 50f),
}

internal fun isUsableLocation(ageMillis: Long, accuracyMeters: Float?, purpose: LocationPurpose): Boolean =
    ageMillis in 0..purpose.maxAgeMillis && accuracyMeters != null && accuracyMeters <= purpose.maxAccuracyMeters

/** Uses a recent accurate fix immediately, then a short network request, then GPS if needed. */
@SuppressLint("MissingPermission")
suspend fun getDeviceLocation(context: Context, purpose: LocationPurpose): Location? {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!fine && !coarse) return null
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val providers = buildList {
        if (isEnabled(manager, LocationManager.NETWORK_PROVIDER)) add(LocationManager.NETWORK_PROVIDER)
        if (fine && isEnabled(manager, LocationManager.GPS_PROVIDER)) add(LocationManager.GPS_PROVIDER)
    }
    if (providers.isEmpty()) return null

    val cached = providers.mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
        .filter { isUsableLocation(System.currentTimeMillis() - it.time, if (it.hasAccuracy()) it.accuracy else null, purpose) }
        .minByOrNull { it.accuracy }
    if (cached != null) return cached

    for (provider in providers) {
        val timeout = if (provider == LocationManager.NETWORK_PROVIDER) 3_000L else 7_000L
        val location = withTimeoutOrNull(timeout) { requestFix(context, manager, provider) }
        if (location != null && isUsableLocation(
                System.currentTimeMillis() - location.time,
                if (location.hasAccuracy()) location.accuracy else null, purpose,
            )) return location
    }
    return null
}

private fun isEnabled(manager: LocationManager, provider: String): Boolean =
    runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false)

@SuppressLint("MissingPermission")
private suspend fun requestFix(context: Context, manager: LocationManager, provider: String): Location? =
    suspendCancellableCoroutine { continuation ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val signal = CancellationSignal()
            continuation.invokeOnCancellation { signal.cancel() }
            try {
                manager.getCurrentLocation(provider, signal, ContextCompat.getMainExecutor(context)) { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
            } catch (_: SecurityException) {
                if (continuation.isActive) continuation.resume(null)
            }
        } else {
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    manager.removeUpdates(this)
                    if (continuation.isActive) continuation.resume(location)
                }
            }
            continuation.invokeOnCancellation { manager.removeUpdates(listener) }
            try {
                manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            } catch (_: SecurityException) {
                if (continuation.isActive) continuation.resume(null)
            }
        }
    }
