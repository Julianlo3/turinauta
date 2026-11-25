package com.unicauca.turinauta.feature.explore

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateDark
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SubtleOutline
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaBackTopBar
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.designsystem.WarmIvory
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.network.SupabaseConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/** Mapa público del lugar. No usa la ubicación del dispositivo ni solicita permisos. */
@Composable
fun PlaceLocationMap(
    latitude: Double,
    longitude: Double,
    placeName: String,
    addressReference: String,
    imagePath: String?,
    onOpenMapClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)

    Surface(
        color = PureWhite,
        shape = shape,
        modifier = modifier.fillMaxWidth().border(TurinautaDimens.CardBorderWidth, SubtleOutline, shape),
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(170.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            ) {
                OpenStreetMapView(latitude, longitude, placeName, imagePath, interactive = false)
                Box(
                    modifier = Modifier.matchParentSize()
                        .semantics { contentDescription = "Ampliar mapa de $placeName" }
                        .clickable(onClick = onOpenMapClick),
                )
                Surface(
                    color = PureWhite.copy(alpha = 0.96f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                ) {
                    Text(
                        "Ampliar mapa",
                        style = MaterialTheme.typography.labelMedium,
                        color = DeepTeal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
                MapAttribution(Modifier.align(Alignment.BottomEnd))
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = DeepTeal,
                    modifier = Modifier.size(20.dp),
                )
                Column {
                    Text(placeName, style = MaterialTheme.typography.titleSmall, color = SlateDark)
                    Text(
                        addressReference.ifBlank { "Ubicación del lugar" },
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun FullScreenPlaceMap(
    place: Place,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmIvory,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TurinautaBackTopBar(
                title = "Mapa de ${place.name}",
                onBackClick = onBackClick,
                backContentDescription = "Volver a la ficha",
            )
        },
    ) { innerPadding ->
        Surface(
            color = PureWhite,
            shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius),
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(12.dp),
        ) {
            Box {
                OpenStreetMapView(place.latitude, place.longitude, place.name, place.imagePath, interactive = true)
                MapAttribution(Modifier.align(Alignment.BottomEnd))
            }
        }
    }
}

@Composable
private fun OpenStreetMapView(
    latitude: Double,
    longitude: Double,
    placeName: String,
    imagePath: String?,
    interactive: Boolean,
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("Mapa de $placeName", color = SlateDark)
        }
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember(context, latitude, longitude, interactive) {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(interactive)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(if (interactive) 17.0 else 16.0)
            val point = GeoPoint(latitude, longitude)
            controller.setCenter(point)
            overlays.add(Marker(this).apply {
                position = point
                title = placeName
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = PlacePhotoMarker.create(context, null)
            })
        }
    }

    val imageUrl = remember(imagePath) { SupabaseConfig.resolvePublicMediaUrl(imagePath) }
    LaunchedEffect(mapView, imageUrl) {
        val photo = PlacePhotoMarker.loadPhoto(context, imageUrl)
        val marker = mapView.overlays.filterIsInstance<Marker>().firstOrNull() ?: return@LaunchedEffect
        marker.icon = PlacePhotoMarker.create(context, photo)
        mapView.invalidate()
    }

    DisposableEffect(mapView, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) mapView.onResume()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onDetach()
        }
    }

    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
}

@Composable
private fun MapAttribution(modifier: Modifier = Modifier) {
    Surface(
        color = PureWhite.copy(alpha = 0.96f),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.padding(8.dp),
    ) {
        Text(
            "© OpenStreetMap contributors",
            style = MaterialTheme.typography.labelSmall,
            color = SlateDark,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceLocationMapPreview() {
    TurinautaTheme {
        PlaceLocationMap(
            latitude = 2.4419,
            longitude = -76.6063,
            placeName = "Cocina del Barrio",
            addressReference = "Sector histórico, a dos cuadras del parque",
            imagePath = null,
            onOpenMapClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
