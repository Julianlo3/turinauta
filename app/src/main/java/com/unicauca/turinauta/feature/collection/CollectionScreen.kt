package com.unicauca.turinauta.feature.collection

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unicauca.turinauta.core.designsystem.BackTopBar
import com.unicauca.turinauta.core.designsystem.CardShape
import com.unicauca.turinauta.core.designsystem.EmptyState
import com.unicauca.turinauta.core.designsystem.LoadingState
import com.unicauca.turinauta.core.designsystem.PillShape
import com.unicauca.turinauta.core.designsystem.StampArt
import com.unicauca.turinauta.core.model.CollectionProgress
import com.unicauca.turinauta.core.model.RewardBadge
import com.unicauca.turinauta.core.navigation.TurinautaViewModelFactory

@Composable
fun CollectionScreen(
    onOpenCollection: (String) -> Unit,
    onLogin: () -> Unit,
    onOpenRoutes: () -> Unit,
    viewModel: CollectionViewModel = viewModel(factory = TurinautaViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            "Mi colección",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
            modifier = Modifier.padding(16.dp),
        )
        when {
            state.isLoading -> LoadingState()
            !state.isSignedIn -> EmptyState(
                icon = Icons.Outlined.Lock,
                title = "Inicia sesión para ver tu colección",
                message = "Tus sellos y tu progreso son privados y se guardan con tu cuenta. Puedes seguir explorando lugares y rutas sin iniciar sesión.",
                primaryAction = "Continuar con Google" to onLogin,
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(state.earned, "sellos obtenidos", highlighted = true, modifier = Modifier.weight(1f))
                        StatCard(state.pending, "sellos pendientes", highlighted = false, modifier = Modifier.weight(1f))
                    }
                }
                if (state.earned == 0) {
                    item {
                        EmptyState(
                            icon = Icons.Outlined.WorkspacePremium,
                            title = "Tu colección está vacía",
                            message = "Registra tu primera visita para obtener un sello. Los sellos pendientes se marcan con borde punteado y la etiqueta «Pendiente».",
                            primaryAction = "Ver rutas temáticas" to onOpenRoutes,
                        )
                    }
                }
                item { Text("Colecciones temáticas", style = MaterialTheme.typography.titleMedium) }
                items(state.collections, key = { it.collection.id }) { progress ->
                    CollectionCard(progress) { onOpenCollection(progress.collection.id) }
                }
            }
        }
    }
}

@Composable
private fun StatCard(value: Int, label: String, highlighted: Boolean, modifier: Modifier = Modifier) {
    Surface(
        shape = CardShape,
        color = if (highlighted) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.semantics(mergeDescendants = true) {},
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("$value", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CollectionCard(progress: CollectionProgress, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            StampArt(progress.collection.illustration, obtained = progress.earnedStamps > 0, size = 56.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(progress.collection.name, style = MaterialTheme.typography.titleSmall)
                Text(progress.collection.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress.fraction },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${progress.earnedStamps} de ${progress.totalStamps} sellos obtenidos",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

/** Detalle de una colección: cuadrícula de sellos obtenidos y pendientes. */
@Composable
fun CollectionDetailScreen(
    collectionId: String,
    onBack: () -> Unit,
    onOpenPlace: (String) -> Unit,
    viewModel: CollectionViewModel = viewModel(factory = TurinautaViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val progress = state.collections.firstOrNull { it.collection.id == collectionId }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        BackTopBar(progress?.collection?.name ?: "Colección", onBack = onBack)
        when {
            state.isLoading -> LoadingState()
            progress == null -> EmptyState(
                icon = Icons.Outlined.SearchOff,
                title = "Esta colección no está disponible",
                message = "Inicia sesión o vuelve a tu colección para ver tu progreso.",
                primaryAction = "Volver" to onBack,
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(span = { GridItemSpan(2) }) {
                    Column {
                        Row {
                            Text("Tu progreso", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text("${progress.earnedStamps} de ${progress.totalStamps} sellos", style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress.fraction },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("Toca un sello para abrir la ficha del lugar.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(progress.badges, key = { it.stamp.id }) { badge -> BadgeTile(badge) { onOpenPlace(badge.stamp.placeId) } }
            }
        }
    }
}

/** Obtenido/Pendiente se comunica con texto + icono, nunca solo con color. */
@Composable
private fun BadgeTile(badge: RewardBadge, onClick: () -> Unit) {
    val status = if (badge.earned) "Obtenido" else "Pendiente"
    Surface(
        onClick = onClick,
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "${badge.stamp.title}, ${badge.placeName}, $status"
        },
    ) {
        Column(Modifier.padding(14.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            StampArt(badge.stamp.illustration, obtained = badge.earned, size = 76.dp)
            Spacer(Modifier.height(10.dp))
            Text(badge.placeName, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
            Text(badge.stamp.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = PillShape,
                color = if (badge.earned) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = if (badge.earned) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (badge.earned) Icons.Filled.Check else Icons.Outlined.Lock,
                        contentDescription = null,
                        Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(status, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

