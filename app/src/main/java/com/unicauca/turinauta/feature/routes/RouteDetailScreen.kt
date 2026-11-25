package com.unicauca.turinauta.feature.routes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unicauca.turinauta.core.designsystem.BackTopBar
import com.unicauca.turinauta.core.designsystem.EmptyState
import com.unicauca.turinauta.core.designsystem.InfoRow
import com.unicauca.turinauta.core.designsystem.LoadingState
import com.unicauca.turinauta.core.designsystem.NoteBox
import com.unicauca.turinauta.core.designsystem.OverlineStyle
import com.unicauca.turinauta.core.designsystem.PlaceCard
import com.unicauca.turinauta.core.designsystem.RouteBannerArt
import com.unicauca.turinauta.core.designsystem.icon
import com.unicauca.turinauta.core.navigation.TurinautaViewModelFactory

/** Detalle de ruta: propósito, condiciones y lugares (cada uno abre su ficha). */
@Composable
fun RouteDetailScreen(
    routeId: String,
    onBack: () -> Unit,
    onOpenPlace: (placeId: String, routeId: String) -> Unit,
    viewModel: RoutesViewModel = viewModel(factory = TurinautaViewModelFactory),
) {
    val state by viewModel.detail.collectAsStateWithLifecycle()
    LaunchedEffect(routeId) { viewModel.loadRoute(routeId) }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        BackTopBar(title = state.route?.name ?: "Ruta temática", onBack = onBack)
        val route = state.route
        when {
            state.isLoading -> LoadingState()
            route == null -> EmptyState(
                icon = Icons.Outlined.SearchOff,
                title = "Esta ruta no está disponible",
                message = "Es posible que se haya retirado mientras se revisa su contenido.",
                primaryAction = "Ver rutas temáticas" to onBack,
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { RouteBannerArt(route.illustration, Modifier.fillMaxWidth().height(120.dp)) }
                item {
                    Column(Modifier.padding16()) {
                        Text("RUTA TEMÁTICA", style = OverlineStyle, color = MaterialTheme.colorScheme.primary)
                        Text(
                            route.name,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.semantics { heading() },
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(route.purpose, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (route.conditions.isNotEmpty()) {
                    item {
                        Column(Modifier.padding16()) {
                            Text("Antes de visitar", style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                            route.conditions.forEachIndexed { index, condition ->
                                InfoRow(condition.type.icon(), label = null, value = condition.text)
                                if (index < route.conditions.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.padding16(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Lugares de la ruta",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f).semantics { heading() },
                        )
                        Text("${state.places.size} lugares", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                item {
                    NoteBox(
                        Icons.Outlined.Route,
                        if (route.hasSuggestedOrder) "Te sugerimos este orden, pero puedes visitar los lugares como prefieras."
                        else "Esta ruta no tiene un orden establecido. Elige los lugares que quieras visitar.",
                        Modifier.padding16(),
                    )
                }
                itemsIndexed(state.places, key = { _, p -> p.id }) { index, place ->
                    Column(Modifier.padding16()) {
                        if (route.hasSuggestedOrder) {
                            Text("Parada ${index + 1}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        PlaceCard(place, onClick = { onOpenPlace(place.id, route.id) })
                    }
                }
            }
        }
    }
}

private fun Modifier.padding16(): Modifier = this.fillMaxWidth().padding(horizontal = 16.dp)
