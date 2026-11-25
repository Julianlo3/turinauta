package com.unicauca.turinauta.feature.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.AltRoute
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.designsystem.WarmIvory
import com.unicauca.turinauta.core.model.IllustrationKey
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.Route
import com.unicauca.turinauta.core.model.RouteStop
import com.unicauca.turinauta.core.network.SupabaseConfig
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.RouteRepository
import com.unicauca.turinauta.data.repository.VisitRepository

@Composable
fun RoutesRoute(
    routeRepository: RouteRepository,
    catalogRepository: CatalogRepository,
    visitRepository: VisitRepository,
    authRepository: AuthRepository,
    onRouteClick: (String) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: RoutesViewModel = viewModel(
        factory = RoutesViewModel.factory(
            routeRepository = routeRepository,
            catalogRepository = catalogRepository,
            visitRepository = visitRepository,
            authRepository = authRepository,
        ),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RoutesScreen(
        uiState = uiState,
        onRouteClick = onRouteClick,
        onRetryClick = viewModel::loadRoutes,
        onExploreClick = onExploreClick,
        modifier = modifier,
    )
}

@Composable
fun RoutesScreen(
    uiState: RoutesUiState,
    onRouteClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory),
        contentPadding = PaddingValues(
            start = TurinautaDimens.ScreenHorizontalPadding,
            end = TurinautaDimens.ScreenHorizontalPadding,
            top = 16.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "routes_header") {
            RoutesEditorialHeader(
                routesCount = uiState.routeItems.size,
                placesCount = uiState.totalPlacesAcrossRoutes,
                showCounts = !uiState.isLoading && uiState.routeItems.isNotEmpty(),
            )
        }

        when {
            uiState.isLoading -> {
                item(key = "routes_loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CircularProgressIndicator(color = DeepTeal)
                            Text(
                                text = "Cargando rutas y paradas...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateMedium,
                            )
                        }
                    }
                }
            }

            uiState.errorMessage != null -> {
                item(key = "routes_error") {
                    RoutesMessageCard(
                        title = if (uiState.isBackendNotConfigured) {
                            "Falta conectar el backend"
                        } else {
                            "No pudimos cargar las rutas"
                        },
                        message = uiState.errorMessage,
                        actionLabel = "Reintentar",
                        onActionClick = onRetryClick,
                    )
                }
            }

            uiState.routeItems.isEmpty() -> {
                item(key = "routes_empty") {
                    RoutesMessageCard(
                        title = "Aún no hay rutas publicadas",
                        message = "Mientras se habilitan nuevos recorridos, puedes explorar los lugares del catálogo.",
                        actionLabel = "Volver al catálogo",
                        onActionClick = onExploreClick,
                    )
                }
            }

            else -> {
                items(
                    items = uiState.routeItems,
                    key = { it.route.id },
                ) { item ->
                    RouteRichCard(
                        item = item,
                        showVisitedProgress = uiState.isAuthenticated,
                        onClick = { onRouteClick(item.route.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutesEditorialHeader(
    routesCount: Int,
    placesCount: Int,
    showCounts: Boolean,
) {
    Surface(
        color = SurfaceLowSand,
        shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SoftTealContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.AltRoute,
                        contentDescription = null,
                        tint = DeepTeal,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column {
                    Text(
                        text = "Rutas temáticas",
                        style = MaterialTheme.typography.headlineSmall,
                        color = SlateDark,
                    )
                    Text(
                        text = "Recorridos curados para explorar Popayán",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMedium,
                    )
                }
            }

            if (showCounts) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeaderMetricPill(
                        icon = Icons.AutoMirrored.Rounded.AltRoute,
                        text = if (routesCount == 1) "1 recorrido" else "$routesCount recorridos",
                    )
                    if (placesCount > 0) {
                        HeaderMetricPill(
                            icon = Icons.Outlined.Place,
                            text = if (placesCount == 1) "1 lugar" else "$placesCount lugares",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderMetricPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Surface(
        color = PureWhite,
        shape = RoundedCornerShape(TurinautaDimens.ChipCornerRadius),
        modifier = Modifier.border(1.dp, SubtleOutline, RoundedCornerShape(TurinautaDimens.ChipCornerRadius)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DeepTeal,
                modifier = Modifier.size(15.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = SlateDark,
            )
        }
    }
}

@Composable
private fun RouteRichCard(
    item: RouteCardItem,
    showVisitedProgress: Boolean,
    onClick: () -> Unit,
) {
    val route = item.route
    val shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
    val stopCount = item.totalStopsCount
    val stopsLabel = when (stopCount) {
        0 -> "Recorrido sugerido"
        1 -> "1 parada"
        else -> "$stopCount paradas"
    }

    Surface(
        color = PureWhite,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .border(TurinautaDimens.CardBorderWidth, SubtleOutline, shape)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {},
    ) {
        Column {
            // Cabecera visual tipo mosaico editorial con fotos de la ruta y sus paradas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp),
            ) {
                RouteMediaCollage(
                    routeImagePath = route.imagePath,
                    stopPlaces = item.stopPlaces,
                    modifier = Modifier.fillMaxSize(),
                )

                // Etiquetas discretas con alto contraste sobre el mosaico
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        color = PureWhite.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(TurinautaDimens.ChipCornerRadius),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.AltRoute,
                                contentDescription = null,
                                tint = DeepTeal,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = stopsLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = SlateDark,
                            )
                        }
                    }

                    if (showVisitedProgress && stopCount > 0 && item.visitedStopsCount > 0) {
                        val completed = item.visitedStopsCount >= stopCount
                        Surface(
                            color = if (completed) SoftTealContainer.copy(alpha = 0.96f)
                                else SurfaceLowSand.copy(alpha = 0.96f),
                            shape = RoundedCornerShape(TurinautaDimens.ChipCornerRadius),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = if (completed) Icons.Rounded.CheckCircle else Icons.Rounded.Timelapse,
                                    contentDescription = null,
                                    tint = if (completed) OnSoftTealContainer else SlateDark,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = "${item.visitedStopsCount}/$stopCount visitadas",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (completed) OnSoftTealContainer else SlateDark,
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (item.categoriesOverline.isNotBlank()) {
                        Text(
                            text = item.categoriesOverline,
                            style = CategoryOverlineTextStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = route.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = SlateDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = route.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (!route.visitConditions.isNullOrBlank()) {
                    var conditionTruncated by remember(route.id, route.visitConditions) { mutableStateOf(false) }
                    Surface(
                        color = SurfaceLowSand,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = DeepTeal,
                                modifier = Modifier.size(16.dp),
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = route.visitConditions,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SlateDark,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    onTextLayout = { conditionTruncated = it.hasVisualOverflow },
                                )
                                if (conditionTruncated) {
                                    Text(
                                        text = "Ver condición completa al abrir la ruta",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DeepTeal,
                                    )
                                }
                            }
                        }
                    }
                }

                // Vista previa de las paradas del recorrido (miniatura y nombre)
                if (item.stopPlaces.isNotEmpty()) {
                    HorizontalDivider(color = SubtleOutline)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Lugares incluidos",
                            style = MaterialTheme.typography.labelLarge,
                            color = SlateDark,
                        )
                        item.stopPlaces.take(3).forEachIndexed { index, place ->
                            RouteStopPreviewRow(
                                orderNumber = index + 1,
                                place = place,
                            )
                        }
                        if (item.stopPlaces.size > 3) {
                            Text(
                                text = "+ ${item.stopPlaces.size - 3} parada(s) adicional(es) en este recorrido",
                                style = MaterialTheme.typography.bodySmall,
                                color = DeepTeal,
                            )
                        }
                    }
                }

                HorizontalDivider(color = SubtleOutline)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Ver recorrido completo",
                        style = MaterialTheme.typography.labelLarge,
                        color = DeepTeal,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = DeepTeal,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteStopPreviewRow(
    orderNumber: Int,
    place: Place,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(SoftTealContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$orderNumber",
                style = MaterialTheme.typography.labelMedium,
                color = OnSoftTealContainer,
            )
        }

        RouteMiniMediaTile(
            imagePath = place.imagePath,
            illustrationKey = place.illustrationKey,
            size = 48.dp,
            cornerRadius = 12.dp,
        )

        Text(
            text = place.name,
            style = MaterialTheme.typography.titleSmall,
            color = SlateDark,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Mosaico editorial reutilizable para `RoutesScreen` y `RouteDetailScreen`.
 * Combina la imagen principal de la ruta y las imágenes/ilustraciones de sus paradas
 * en un diseño de 1, 2 o 3 paneles sin depender de degradados estridentes.
 */
@Composable
internal fun RouteMediaCollage(
    routeImagePath: String?,
    stopPlaces: List<Place>,
    modifier: Modifier = Modifier,
) {
    val panels = remember(routeImagePath, stopPlaces) {
        buildList {
            if (!routeImagePath.isNullOrBlank()) {
                add(
                    CollageTile(
                        imagePath = routeImagePath,
                        illustrationKey = stopPlaces.firstOrNull()?.illustrationKey ?: IllustrationKey.GENERIC,
                    ),
                )
            }
            stopPlaces.forEach { place ->
                add(
                    CollageTile(
                        imagePath = place.imagePath,
                        illustrationKey = place.illustrationKey,
                    ),
                )
            }
            if (isEmpty()) {
                add(
                    CollageTile(
                        imagePath = null,
                        illustrationKey = IllustrationKey.GENERIC,
                    ),
                )
            }
        }.take(3)
    }

    when (panels.size) {
        1 -> {
            CollageImageTile(
                tile = panels[0],
                modifier = modifier.fillMaxSize(),
            )
        }

        2 -> {
            Row(
                modifier = modifier
                    .fillMaxSize()
                    .background(WarmIvory),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                CollageImageTile(
                    tile = panels[0],
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                CollageImageTile(
                    tile = panels[1],
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }

        else -> {
            Row(
                modifier = modifier
                    .fillMaxSize()
                    .background(WarmIvory),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                CollageImageTile(
                    tile = panels[0],
                    modifier = Modifier
                        .weight(1.35f)
                        .fillMaxHeight(),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    CollageImageTile(
                        tile = panels[1],
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                    CollageImageTile(
                        tile = panels[2],
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }
            }
        }
    }
}

private data class CollageTile(
    val imagePath: String?,
    val illustrationKey: IllustrationKey,
)

@Composable
private fun CollageImageTile(
    tile: CollageTile,
    modifier: Modifier = Modifier,
) {
    val resolvedUrl = remember(tile.imagePath) {
        SupabaseConfig.resolvePublicMediaUrl(tile.imagePath)
    }
    Box(
        modifier = modifier.background(SurfaceThumbBeige),
        contentAlignment = Alignment.Center,
    ) {
        if (resolvedUrl == null) {
            PlaceIllustration(
                illustrationKey = tile.illustrationKey,
                size = 76.dp,
                cornerRadius = 14.dp,
            )
        } else {
            SubcomposeAsyncImage(
                model = resolvedUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                loading = {
                    PlaceIllustration(illustrationKey = tile.illustrationKey, size = 76.dp, cornerRadius = 14.dp)
                },
                error = {
                    PlaceIllustration(illustrationKey = tile.illustrationKey, size = 76.dp, cornerRadius = 14.dp)
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
internal fun RouteMiniMediaTile(
    imagePath: String?,
    illustrationKey: IllustrationKey,
    size: Dp,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
) {
    val resolvedUrl = remember(imagePath) {
        SupabaseConfig.resolvePublicMediaUrl(imagePath)
    }
    if (resolvedUrl == null) {
        PlaceIllustration(
            illustrationKey = illustrationKey,
            size = size,
            cornerRadius = cornerRadius,
            modifier = modifier,
        )
    } else {
        SubcomposeAsyncImage(
            model = resolvedUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            loading = {
                PlaceIllustration(illustrationKey = illustrationKey, size = size, cornerRadius = cornerRadius)
            },
            error = {
                PlaceIllustration(illustrationKey = illustrationKey, size = size, cornerRadius = cornerRadius)
            },
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(cornerRadius))
                .background(SurfaceThumbBeige),
        )
    }
}

@Composable
private fun RoutesMessageCard(
    title: String,
    message: String,
    actionLabel: String,
    onActionClick: () -> Unit,
) {
    val shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
    Surface(
        color = PureWhite,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .border(TurinautaDimens.CardBorderWidth, SubtleOutline, shape),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = SlateDark,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = SlateMedium,
            )
            Spacer(modifier = Modifier.height(2.dp))
            OutlinedButton(
                onClick = onActionClick,
                modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
            ) {
                Text(actionLabel, color = DeepTeal)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoutesScreenPreview() {
    TurinautaTheme {
        RoutesScreen(
            uiState = RoutesUiState(
                isAuthenticated = true,
                isLoading = false,
                routeItems = listOf(
                    RouteCardItem(
                        route = Route(
                            id = "r1",
                            name = "Oficios y saberes locales",
                            slug = "oficios-saberes",
                            description = "Un recorrido por talleres artesanales y espacios culturales que mantienen vivas las técnicas tradicionales del Cauca.",
                            visitConditions = "Ideal para recorrer a pie entre martes y sábado en horario diurno.",
                            stops = listOf(
                                RouteStop("r1", "p1", 1),
                                RouteStop("r1", "p2", 2),
                                RouteStop("r1", "p3", 3),
                            ),
                        ),
                        stopPlaces = listOf(
                            Place(
                                id = "p1",
                                categoryId = "2",
                                categoryName = "Artesanías",
                                name = "Taller de tejido",
                                slug = "taller-de-tejido",
                                summary = "Piezas elaboradas en telar manual con fibras naturales.",
                                description = "Desc",
                                locationReference = "Centro",
                                latitude = 2.4442,
                                longitude = -76.6021,
                                illustrationKey = IllustrationKey.ARTESANIAS,
                            ),
                            Place(
                                id = "p2",
                                categoryId = "3",
                                categoryName = "Cultura",
                                name = "Espacio cultural La Esquina",
                                slug = "la-esquina",
                                summary = "Encuentros musicales, literatura y memoria barrial.",
                                description = "Desc",
                                locationReference = "Centro",
                                latitude = 2.4405,
                                longitude = -76.6085,
                                illustrationKey = IllustrationKey.CULTURA,
                            ),
                            Place(
                                id = "p3",
                                categoryId = "2",
                                categoryName = "Artesanías",
                                name = "Taller de cerámica ancestral",
                                slug = "ceramica",
                                summary = "Moldeado en barro y quema artesanal con saberes heredados.",
                                description = "Desc",
                                locationReference = "Centro",
                                latitude = 2.4431,
                                longitude = -76.6074,
                                illustrationKey = IllustrationKey.CERAMICA,
                            ),
                        ),
                        visitedStopsCount = 1,
                    ),
                ),
            ),
            onRouteClick = {},
            onRetryClick = {},
            onExploreClick = {},
        )
    }
}
