package com.unicauca.turinauta.feature.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.unicauca.turinauta.core.designsystem.CategoryOverlineTextStyle
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.OnSoftTealContainer
import com.unicauca.turinauta.core.designsystem.PlaceIllustration
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateDark
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SoftTealContainer
import com.unicauca.turinauta.core.designsystem.SubtleOutline
import com.unicauca.turinauta.core.designsystem.SurfaceLowSand
import com.unicauca.turinauta.core.designsystem.SurfaceThumbBeige
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaBackTopBar
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.designsystem.WarmIvory
import com.unicauca.turinauta.core.model.IllustrationKey
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.Route
import com.unicauca.turinauta.core.network.SupabaseConfig
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.RouteRepository
import com.unicauca.turinauta.data.repository.VisitRepository
import java.util.Locale

@Composable
fun RouteDetailRoute(
    routeId: String,
    routeRepository: RouteRepository,
    catalogRepository: CatalogRepository,
    visitRepository: VisitRepository,
    authRepository: AuthRepository,
    onBackClick: () -> Unit,
    onPlaceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: RouteDetailViewModel = viewModel(
        key = "route_detail_$routeId",
        factory = RouteDetailViewModel.factory(
            routeId = routeId,
            routeRepository = routeRepository,
            catalogRepository = catalogRepository,
            visitRepository = visitRepository,
            authRepository = authRepository,
        ),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RouteDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetryClick = viewModel::loadRouteDetail,
        onPlaceClick = onPlaceClick,
        modifier = modifier,
    )
}

@Composable
fun RouteDetailScreen(
    uiState: RouteDetailUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onPlaceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmIvory,
        topBar = {
            TurinautaBackTopBar(title = "Detalle de ruta", onBackClick = onBackClick)
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = DeepTeal)
                }
            }

            uiState.route == null || uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Ruta no encontrada",
                            style = MaterialTheme.typography.bodyLarge,
                            color = SlateDark,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = onBackClick,
                                modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                            ) {
                                Text("Volver", color = DeepTeal)
                            }
                            Button(
                                onClick = onRetryClick,
                                modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }

            else -> {
                val route = uiState.route
                val stopPlaces = uiState.stops.map { it.place }
                val visitedCount = uiState.stops.count { it.isVisited }
                val categoriesLabel = remember(stopPlaces) {
                    stopPlaces
                        .mapNotNull { it.categoryName.trim().takeIf { name -> name.isNotEmpty() } }
                        .distinct()
                        .joinToString(" · ") { it.uppercase(Locale.forLanguageTag("es-CO")) }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        start = TurinautaDimens.ScreenHorizontalPadding,
                        end = TurinautaDimens.ScreenHorizontalPadding,
                        top = 8.dp,
                        bottom = 28.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // 1. Hero visual tipo mosaico de imágenes de la ruta y paradas
                    item(key = "route_hero") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(204.dp)
                                .clip(RoundedCornerShape(TurinautaDimens.CardCornerRadius))
                                .border(
                                    TurinautaDimens.CardBorderWidth,
                                    SubtleOutline,
                                    RoundedCornerShape(TurinautaDimens.CardCornerRadius),
                                ),
                        ) {
                            RouteMediaCollage(
                                routeImagePath = route.imagePath,
                                stopPlaces = stopPlaces,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }

                    // 2. Tarjeta resumen editorial de la ruta
                    item(key = "route_overview") {
                        val cardShape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
                        Surface(
                            color = PureWhite,
                            shape = cardShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(TurinautaDimens.CardBorderWidth, SubtleOutline, cardShape),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                if (categoriesLabel.isNotBlank()) {
                                    Text(
                                        text = categoriesLabel,
                                        style = CategoryOverlineTextStyle,
                                    )
                                }
                                Text(
                                    text = route.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = SlateDark,
                                )
                                Text(
                                    text = route.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = SlateMedium,
                                )

                                if (uiState.stops.isNotEmpty()) {
                                    val progressFraction = visitedCount.toFloat() / uiState.stops.size.toFloat()
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                text = "Avance en la ruta",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = SlateMedium,
                                            )
                                            Text(
                                                text = "$visitedCount de ${uiState.stops.size} paradas visitadas",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = DeepTeal,
                                            )
                                        }
                                        LinearProgressIndicator(
                                            progress = { progressFraction },
                                            color = DeepTeal,
                                            trackColor = SurfaceLowSand,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(7.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                        )
                                    }
                                }

                                if (!route.visitConditions.isNullOrBlank()) {
                                    Surface(
                                        color = SurfaceLowSand,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Info,
                                                contentDescription = null,
                                                tint = DeepTeal,
                                                modifier = Modifier.size(18.dp),
                                            )
                                            Text(
                                                text = route.visitConditions,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SlateDark,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Galería visual horizontal de paradas (cuando hay 2 o más lugares)
                    if (uiState.stops.size >= 2) {
                        item(key = "route_gallery") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Postales del recorrido",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SlateDark,
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    items(
                                        items = uiState.stops,
                                        key = { "gallery_${it.place.id}" },
                                    ) { stopItem ->
                                        RouteGalleryCard(
                                            stopItem = stopItem,
                                            onClick = { onPlaceClick(stopItem.place.id) },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Itinerario paso a paso con miniaturas y breve descripción de cada lugar
                    item(key = "stops_header") {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Itinerario sugerido (${uiState.stops.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = SlateDark,
                            )
                            Text(
                                text = "Toca cualquier parada para ver su ficha completa y registrar tu visita.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMedium,
                            )
                        }
                    }

                    if (uiState.stops.isEmpty()) {
                        item(key = "empty_stops") {
                            Text(
                                text = "Esta ruta no tiene paradas publicadas en este momento.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateMedium,
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = uiState.stops,
                            key = { _, item -> "${item.orderNumber}_${item.place.id}" },
                        ) { index, stopItem ->
                            RouteTimelineStopItem(
                                stopItem = stopItem,
                                isLast = index == uiState.stops.lastIndex,
                                onClick = { onPlaceClick(stopItem.place.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteGalleryCard(
    stopItem: RouteStopItem,
    onClick: () -> Unit,
) {
    val place = stopItem.place
    val resolvedUrl = remember(place.imagePath) {
        SupabaseConfig.resolvePublicMediaUrl(place.imagePath)
    }
    val shape = RoundedCornerShape(16.dp)

    Surface(
        color = PureWhite,
        shape = shape,
        modifier = Modifier
            .width(156.dp)
            .border(TurinautaDimens.CardBorderWidth, SubtleOutline, shape)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {},
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(94.dp)
                    .background(SurfaceThumbBeige),
                contentAlignment = Alignment.Center,
            ) {
                if (resolvedUrl == null) {
                    PlaceIllustration(
                        illustrationKey = place.illustrationKey,
                        size = 64.dp,
                        cornerRadius = 12.dp,
                    )
                } else {
                    SubcomposeAsyncImage(
                        model = resolvedUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        loading = {
                            PlaceIllustration(illustrationKey = place.illustrationKey, size = 64.dp)
                        },
                        error = {
                            PlaceIllustration(illustrationKey = place.illustrationKey, size = 64.dp)
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Text(
                text = "${stopItem.orderNumber}. ${place.name}",
                style = MaterialTheme.typography.labelMedium,
                color = SlateDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun RouteTimelineStopItem(
    stopItem: RouteStopItem,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Indicador vertical de ruta (círculo numerado + conector)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight(),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (stopItem.isVisited) DeepTeal else SoftTealContainer),
                contentAlignment = Alignment.Center,
            ) {
                if (stopItem.isVisited) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Parada visitada",
                        tint = PureWhite,
                        modifier = Modifier.size(18.dp),
                    )
                } else {
                    Text(
                        text = "${stopItem.orderNumber}",
                        style = MaterialTheme.typography.labelLarge,
                        color = OnSoftTealContainer,
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .padding(vertical = 4.dp)
                        .background(if (stopItem.isVisited) DeepTeal.copy(alpha = 0.45f) else SubtleOutline),
                )
            }
        }

        // Tarjeta detallada del lugar (foto + categoría + nombre + breve descripción + ubicación/horario)
        RouteDetailedStopCard(
            stopItem = stopItem,
            onClick = onClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RouteDetailedStopCard(
    stopItem: RouteStopItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
    val place = stopItem.place

    Surface(
        color = PureWhite,
        shape = shape,
        modifier = modifier
            .fillMaxWidth()
            .border(TurinautaDimens.CardBorderWidth, SubtleOutline, shape)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {},
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                RouteMiniMediaTile(
                    imagePath = place.imagePath,
                    illustrationKey = place.illustrationKey,
                    size = 86.dp,
                    cornerRadius = 14.dp,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (place.categoryName.isNotBlank()) {
                        Text(
                            text = place.categoryName.uppercase(Locale.forLanguageTag("es-CO")),
                            style = CategoryOverlineTextStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = SlateDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = place.summary.ifBlank { place.description },
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (place.locationReference.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = DeepTeal,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = place.locationReference,
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = SlateMedium,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = place.displayOpeningHours,
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = SlateMedium,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            if (stopItem.isVisited) {
                Surface(
                    color = SoftTealContainer,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = OnSoftTealContainer,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = "Visita ya registrada",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSoftTealContainer,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RouteDetailScreenPreview() {
    TurinautaTheme {
        RouteDetailScreen(
            uiState = RouteDetailUiState(
                route = Route(
                    id = "r1",
                    name = "Oficios y saberes locales",
                    slug = "oficios-saberes",
                    description = "Recorrido a pie por talleres y espacios culturales en el centro histórico de Popayán.",
                    visitConditions = "Recomendado en horario diurno de martes a sábado.",
                ),
                stops = listOf(
                    RouteStopItem(
                        orderNumber = 1,
                        place = Place(
                            id = "p1",
                            categoryId = "2",
                            categoryName = "Artesanías",
                            name = "Taller de tejido",
                            slug = "taller-de-tejido",
                            summary = "Piezas elaboradas en telar manual con lana y fibras teñidas de forma natural.",
                            description = "Desc",
                            locationReference = "Calle 4 # 6-18, Centro histórico",
                            latitude = 2.4442,
                            longitude = -76.6021,
                            openingHours = "Lun a vie, 9:00–12:00",
                            illustrationKey = IllustrationKey.ARTESANIAS,
                        ),
                        isVisited = true,
                    ),
                    RouteStopItem(
                        orderNumber = 2,
                        place = Place(
                            id = "p2",
                            categoryId = "3",
                            categoryName = "Cultura",
                            name = "Espacio cultural La Esquina",
                            slug = "la-esquina",
                            summary = "Agenda comunitaria de música tradicional, lectura y exposiciones locales.",
                            description = "Desc",
                            locationReference = "Esquina Parque Caldas",
                            latitude = 2.4405,
                            longitude = -76.6085,
                            openingHours = null,
                            illustrationKey = IllustrationKey.CULTURA,
                        ),
                        isVisited = false,
                    ),
                ),
                isLoading = false,
            ),
            onBackClick = {},
            onRetryClick = {},
            onPlaceClick = {},
        )
    }
}
