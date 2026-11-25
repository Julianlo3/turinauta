package com.unicauca.turinauta.feature.routes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unicauca.turinauta.core.designsystem.CardShape
import com.unicauca.turinauta.core.designsystem.CatalogImage
import com.unicauca.turinauta.core.designsystem.EmptyState
import com.unicauca.turinauta.core.designsystem.LoadingState
import com.unicauca.turinauta.core.designsystem.PillShape
import com.unicauca.turinauta.core.designsystem.RouteBannerArt
import com.unicauca.turinauta.core.model.Route
import com.unicauca.turinauta.core.navigation.TurinautaViewModelFactory

@Composable
fun RoutesScreen(
    onOpenRoute: (String) -> Unit,
    onExplore: () -> Unit,
    viewModel: RoutesViewModel = viewModel(factory = TurinautaViewModelFactory),
) {
    val state by viewModel.list.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadRoutes() }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            "Rutas temáticas",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        )
        when {
            state.isLoading -> LoadingState()
            state.error != null -> EmptyState(
                icon = Icons.Outlined.CloudOff,
                title = "No pudimos cargar las rutas",
                message = state.error!!,
                primaryAction = "Reintentar" to viewModel::loadRoutes,
            )
            state.routes.isEmpty() -> EmptyState(
                icon = Icons.Outlined.Route,
                title = "Todavía no hay rutas disponibles",
                message = "Las rutas temáticas aparecerán aquí cuando estén listas. Mientras tanto, puedes descubrir lugares en el catálogo.",
                primaryAction = "Explorar lugares" to onExplore,
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Text(
                        "Cada ruta reúne lugares alrededor de un tema. Puedes visitarlos en el orden que prefieras.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(state.routes, key = { it.id }) { route -> RouteCard(route) { onOpenRoute(route.id) } }
            }
        }
    }
}

@Composable
private fun RouteCard(route: Route, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
    ) {
        Column {
            CatalogImage(
                route.imageUrl,
                route.illustration,
                Modifier.fillMaxWidth().height(120.dp),
                fallback = { RouteBannerArt(route.illustration, it) },
            )
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(route.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
                Text(route.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Surface(shape = PillShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Place, contentDescription = null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${route.placeIds.size} lugares",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }
    }
}
