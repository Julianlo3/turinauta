package com.unicauca.turinauta.feature.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unicauca.turinauta.core.designsystem.CategoryOverlineTextStyle
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.OnSoftTealContainer
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateDark
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SoftTealContainer
import com.unicauca.turinauta.core.designsystem.SubtleOutline
import com.unicauca.turinauta.core.designsystem.SurfaceLowSand
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.designsystem.WarmIvory
import com.unicauca.turinauta.core.model.IllustrationKey
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.Route
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

@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle de ruta",
                        style = MaterialTheme.typography.titleMedium,
                        color = SlateDark,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(TurinautaDimens.MinTouchTarget),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
                            tint = SlateDark,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmIvory),
            )
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        start = TurinautaDimens.ScreenHorizontalPadding,
                        end = TurinautaDimens.ScreenHorizontalPadding,
                        top = 8.dp,
                        bottom = 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item(key = "route_info") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            if (!route.visitConditions.isNullOrBlank()) {
                                Surface(
                                    color = SurfaceLowSand,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Info,
                                            contentDescription = null,
                                            tint = DeepTeal,
                                            modifier = Modifier.size(20.dp),
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

                    item(key = "stops_header") {
                        Text(
                            text = "Paradas sugeridas (${uiState.stops.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = SlateDark,
                        )
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
                        items(
                            items = uiState.stops,
                            key = { "${it.orderNumber}_${it.place.id}" },
                        ) { stopItem ->
                            RouteStopCard(
                                stopItem = stopItem,
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
private fun RouteStopCard(
    stopItem: RouteStopItem,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
    val place = stopItem.place

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (stopItem.isVisited) DeepTeal else SoftTealContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${stopItem.orderNumber}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (stopItem.isVisited) PureWhite else OnSoftTealContainer,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (place.categoryName.isNotBlank()) {
                    Text(
                        text = place.categoryName.uppercase(Locale.forLanguageTag("es-CO")),
                        style = CategoryOverlineTextStyle,
                    )
                }
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = SlateDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
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
                if (stopItem.isVisited) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = DeepTeal,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = "Visita ya registrada",
                            style = MaterialTheme.typography.labelSmall,
                            color = DeepTeal,
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = SlateMedium,
            )
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
                    description = "Recorrido a pie por talleres y espacios culturales en el centro de Popayán.",
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
                            summary = "Telar manual tradicional.",
                            description = "Desc",
                            locationReference = "Centro",
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
                            summary = "Música y cultura local.",
                            description = "Desc",
                            locationReference = "Centro",
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
