package com.unicauca.turinauta.feature.explore

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import com.unicauca.turinauta.data.repository.MapPoint
import com.unicauca.turinauta.data.repository.WalkingRoute
import androidx.compose.ui.graphics.toArgb

/** Mapa público del lugar. La ubicación y la ruta solo se cargan en pantalla completa. */
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
    viewModel: PlaceMapViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    val hasPermission = permissions.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.any { it }) viewModel.load(context, place)
    }
    LaunchedEffect(place.id) {
        if (hasPermission) viewModel.load(context, place) else permissionLauncher.launch(permissions)
    }
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
                OpenStreetMapView(
                    place.latitude, place.longitude, place.name, place.imagePath,
                    interactive = true, userLocation = state.userLocation, route = state.route,
                )
                Surface(
                    color = PureWhite.copy(alpha = 0.96f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.TopCenter).padding(12.dp),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("Ruta a pie", style = MaterialTheme.typography.titleSmall, color = SlateDark)
                        when {
                            state.loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Text("  Calculando recorrido…", style = MaterialTheme.typography.bodySmall)
                            }
                            state.route != null -> Text(
                                "${formatDistance(state.route!!.distanceMeters)} · ${kotlin.math.ceil(state.route!!.durationSeconds / 60).toInt()} min aprox.",
                                style = MaterialTheme.typography.bodySmall, color = SlateMedium,
                            )
                            else -> Text(
                                if (!hasPermission) "Permite la ubicación para ver la ruta." else state.message ?: "Buscando tu ubicación…",
                                style = MaterialTheme.typography.bodySmall, color = SlateMedium,
                            )
                        }
                        if (!state.loading && state.route == null) {
                            TextButton(onClick = {
                                if (hasPermission) viewModel.load(context, place) else permissionLauncher.launch(permissions)
                            }) { Text(if (hasPermission) "Reintentar" else "Permitir ubicación") }
                        }
                        Text("Ruta: FOSSGIS. Se envía tu ubicación para calcularla.",
                            style = MaterialTheme.typography.labelSmall, color = SlateMedium)
                    }
                }
                if (state.userLocation != null) {
                    Surface(
                        color = PureWhite.copy(alpha = 0.96f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.align(Alignment.BottomStart).padding(8.dp).padding(bottom = 22.dp),
                    ) {
                        Text("● Tú   📍 ${place.name}", style = MaterialTheme.typography.labelSmall,
                            color = SlateDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                }
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
    userLocation: MapPoint? = null,
    route: WalkingRoute? = null,
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
    LaunchedEffect(mapView, userLocation, route) {
        mapView.overlays.removeAll { overlay ->
            overlay is Polyline || (overlay is Marker && overlay.title == "Tu ubicación")
        }
        if (route != null) {
            mapView.overlays.add(0, Polyline(mapView).apply {
                setPoints(route.points.map { GeoPoint(it.latitude, it.longitude) })
                outlinePaint.color = DeepTeal.toArgb()
                outlinePaint.strokeWidth = 10f * context.resources.displayMetrics.density
            })
        }
        if (userLocation != null) {
            val size = (24 * context.resources.displayMetrics.density).toInt()
            val circle = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(AndroidColor.rgb(24, 112, 205))
                setStroke((3 * context.resources.displayMetrics.density).toInt(), AndroidColor.WHITE)
                setSize(size, size)
            }
            mapView.overlays.add(Marker(mapView).apply {
                position = GeoPoint(userLocation.latitude, userLocation.longitude)
                title = "Tu ubicación"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon = circle
            })
            val points = (route?.points ?: listOf(MapPoint(latitude, longitude), userLocation))
                .map { GeoPoint(it.latitude, it.longitude) }
            mapView.post { mapView.zoomToBoundingBox(BoundingBox.fromGeoPoints(points), true, 80) }
        }
        mapView.invalidate()
    }
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

private fun formatDistance(meters: Double): String =
    if (meters < 1000) "${meters.toInt()} m" else String.format(java.util.Locale.forLanguageTag("es-CO"), "%.1f km", meters / 1000)

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
