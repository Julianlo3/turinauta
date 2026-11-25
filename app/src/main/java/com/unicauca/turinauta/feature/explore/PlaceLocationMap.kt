package com.unicauca.turinauta.feature.explore

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.unicauca.turinauta.core.designsystem.CardShape
import com.unicauca.turinauta.core.designsystem.SandHigh
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMapOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import java.util.Locale

/** Estilo de mapa abierto y gratuito (teselas de OpenStreetMap servidas por OpenFreeMap, sin API key). */
private const val MAP_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

/**
 * Mapa de ubicación del lugar con MapLibre, centrado en (latitude, longitude).
 * No pide permiso de ubicación. Es estático (sin gestos) y el marcador queda fijo al centro;
 * mientras cargan las teselas, o sin conexión, se ve un plano dibujado de respaldo.
 */
@Composable
fun PlaceLocationMap(
    latitude: Double,
    longitude: Double,
    placeName: String,
    addressReference: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            Box(
                Modifier.fillMaxWidth().height(180.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .semantics(mergeDescendants = true) { contentDescription = "Mapa con la ubicación de $placeName" },
                contentAlignment = Alignment.Center,
            ) {
                StylizedMap()
                MapLibreView(latitude, longitude, Modifier.fillMaxSize())
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(42.dp).offset(y = (-16).dp),
                )
            }
            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text(addressReference, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Popayán · " + String.format(Locale.US, "%.4f, %.4f", latitude, longitude) + " · © OpenStreetMap",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MapLibreView(latitude: Double, longitude: Double, modifier: Modifier) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember {
        initMapLibre(context)
        // textureMode permite recortar el mapa con las esquinas redondeadas de la tarjeta.
        MapView(context, MapLibreMapOptions.createFromAttributes(context).textureMode(true)).apply {
            onCreate(null)
            getMapAsync { map ->
                map.uiSettings.setAllGesturesEnabled(false)
                map.uiSettings.isAttributionEnabled = false
                map.uiSettings.isLogoEnabled = false
                map.cameraPosition = CameraPosition.Builder().target(LatLng(latitude, longitude)).zoom(15.5).build()
                map.setStyle(MAP_STYLE_URL)
            }
        }
    }
    // El MapView necesita seguir el ciclo de vida de la pantalla.
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.onStart()
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) mapView.onResume()
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }
    AndroidView(factory = { mapView }, modifier = modifier)
}

private fun initMapLibre(context: Context) {
    MapLibre.getInstance(context.applicationContext)
}

/** Plano dibujado de respaldo (se ve mientras cargan las teselas o sin conexión). */
@Composable
private fun StylizedMap() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Color(0xFFF1EEE7))
        val street = Color.White
        val w = size.width; val h = size.height
        drawRect(SandHigh, topLeft = Offset(w * 0.08f, h * 0.12f), size = Size(w * 0.18f, h * 0.28f))
        drawRect(Color(0xFFD6E7E5), topLeft = Offset(w * 0.62f, h * 0.14f), size = Size(w * 0.16f, h * 0.30f))
        listOf(0.30f, 0.55f, 0.82f).forEach { x -> drawLine(street, Offset(w * x, 0f), Offset(w * x, h), strokeWidth = h * 0.08f) }
        drawLine(street, Offset(0f, h * 0.45f), Offset(w, h * 0.45f), strokeWidth = h * 0.08f)
        val river = Path().apply {
            moveTo(0f, h * 0.86f)
            cubicTo(w * 0.3f, h * 0.76f, w * 0.6f, h * 0.98f, w, h * 0.84f)
        }
        drawPath(river, Color(0xFFBFD7DF), style = Stroke(h * 0.06f))
    }
}
