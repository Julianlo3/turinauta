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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.AltRoute
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.OnSoftTealContainer
import com.unicauca.turinauta.core.designsystem.PlaceIllustration
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateDark
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SoftTealContainer
import com.unicauca.turinauta.core.designsystem.SubtleOutline
import com.unicauca.turinauta.core.designsystem.SurfaceThumbBeige
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.designsystem.WarmIvory
import com.unicauca.turinauta.core.model.IllustrationKey
import com.unicauca.turinauta.core.model.Route
import com.unicauca.turinauta.core.model.RouteStop
import com.unicauca.turinauta.core.network.SupabaseConfig
import com.unicauca.turinauta.data.repository.RouteRepository

@Composable
fun RoutesRoute(
    routeRepository: RouteRepository,
    onRouteClick: (String) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: RoutesViewModel = viewModel(
        factory = RoutesViewModel.factory(routeRepository),
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
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "routes_header") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Rutas temáticas",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SlateDark,
                )
                Text(
                    text = "Recorridos curados para explorar Popayán paso a paso.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMedium,
                )
            }
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
                                text = "Cargando rutas disponibles...",
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

            uiState.routes.isEmpty() -> {
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
                    items = uiState.routes,
                    key = { it.id },
                ) { route ->
                    RouteListCard(
                        route = route,
                        onClick = { onRouteClick(route.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteListCard(
    route: Route,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
    val stopCount = route.stops.size
    val stopsLabel = when (stopCount) {
        0 -> "Recorrido sugerido"
        1 -> "1 parada sugerida"
        else -> "$stopCount paradas sugeridas"
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
            RouteCardMedia(imagePath = route.imagePath)

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    color = SoftTealContainer,
                    shape = RoundedCornerShape(TurinautaDimens.ChipCornerRadius),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.AltRoute,
                            contentDescription = null,
                            tint = OnSoftTealContainer,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = stopsLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSoftTealContainer,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = route.name,
                            style = MaterialTheme.typography.titleMedium,
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
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = SlateMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteCardMedia(imagePath: String?) {
    val resolvedUrl = remember(imagePath) {
        SupabaseConfig.resolvePublicMediaUrl(imagePath, defaultBucket = "routes")
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .background(SurfaceThumbBeige),
        contentAlignment = Alignment.Center,
    ) {
        if (resolvedUrl == null) {
            PlaceIllustration(
                illustrationKey = IllustrationKey.GENERIC,
                size = 88.dp,
                cornerRadius = 16.dp,
            )
        } else {
            SubcomposeAsyncImage(
                model = resolvedUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                loading = {
                    PlaceIllustration(illustrationKey = IllustrationKey.GENERIC, size = 88.dp)
                },
                error = {
                    PlaceIllustration(illustrationKey = IllustrationKey.GENERIC, size = 88.dp)
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
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
                routes = listOf(
                    Route(
                        id = "r1",
                        name = "Oficios y saberes locales",
                        slug = "oficios-saberes",
                        description = "Un recorrido por talleres artesanales y espacios culturales que mantienen vivas las técnicas tradicionales.",
                        stops = listOf(
                            RouteStop("r1", "p1", 1),
                            RouteStop("r1", "p2", 2),
                            RouteStop("r1", "p3", 3),
                        ),
                    ),
                    Route(
                        id = "r2",
                        name = "Sabores cotidianos de Popayán",
                        slug = "sabores-cotidianos",
                        description = "Paradas recomendadas para descubrir cocinas de barrio, amasijos y café de origen.",
                        stops = listOf(
                            RouteStop("r2", "p4", 1),
                            RouteStop("r2", "p5", 2),
                        ),
                    ),
                ),
                isLoading = false,
            ),
            onRouteClick = {},
            onRetryClick = {},
            onExploreClick = {},
        )
    }
}
