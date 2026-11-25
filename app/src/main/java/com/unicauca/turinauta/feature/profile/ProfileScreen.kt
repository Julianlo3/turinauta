package com.unicauca.turinauta.feature.profile

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unicauca.turinauta.core.designsystem.BackTopBar
import com.unicauca.turinauta.core.designsystem.EmptyState
import com.unicauca.turinauta.core.designsystem.LocalSnackbarHostState
import com.unicauca.turinauta.core.designsystem.NoteBox
import com.unicauca.turinauta.core.designsystem.PlaceIllustration
import com.unicauca.turinauta.core.designsystem.ThumbnailShape
import com.unicauca.turinauta.core.designsystem.formatMonthYear
import com.unicauca.turinauta.core.designsystem.formatShortDate
import com.unicauca.turinauta.core.navigation.TurinautaViewModelFactory

@Composable
fun ProfileScreen(
    onLogin: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenCollection: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = TurinautaViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current
    val context = LocalContext.current
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it); viewModel.onMessageShown() } }

    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())) {
        Text("Perfil", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal), modifier = Modifier.padding(16.dp))
        val user = state.user
        if (user == null) {
            EmptyState(
                icon = Icons.Outlined.Lock,
                title = "Inicia sesión para ver tu perfil",
                message = "Tu historial y tu colección son privados y solo se muestran con tu cuenta. Puedes seguir explorando lugares y rutas sin iniciar sesión.",
                primaryAction = "Continuar con Google" to onLogin,
            )
        } else {
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(60.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(user.displayName.take(1).uppercase(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(user.displayName, style = MaterialTheme.typography.titleLarge)
                    Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(16.dp))
            NoteBox(
                Icons.Outlined.Shield,
                "Tu perfil es privado: solo tú ves tu historial y tu colección. El nombre viene de tu cuenta de Google.",
                Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))
            ProfileRow(Icons.Outlined.History, "Historial de visitas", "${state.visits.size} ${if (state.visits.size == 1) "lugar visitado" else "lugares visitados"}", onOpenHistory)
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            ProfileRow(Icons.Outlined.WorkspacePremium, "Mi colección", "${state.earnedStamps} ${if (state.earnedStamps == 1) "sello obtenido" else "sellos obtenidos"}", onOpenCollection)
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            ProfileRow(
                Icons.Outlined.MyLocation,
                "Permisos",
                "Ubicación: ${if (viewModel.hasLocationPermission()) "concedida" else "no concedida"}",
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                    )
                },
            )
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            ProfileRow(Icons.AutoMirrored.Outlined.Logout, "Cerrar sesión", null, viewModel::signOut, destructive = true)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, title: String, subtitle: String?, onClick: () -> Unit, destructive: Boolean = false) {
    val color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (destructive) color else MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = color)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!destructive) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
}

/** `my_visit_history`: lugar, fecha y acceso a la ficha; si se retiró, «Lugar no disponible». */
@Composable
fun VisitHistoryScreen(
    onBack: () -> Unit,
    onOpenPlace: (String) -> Unit,
    onExplore: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = TurinautaViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        BackTopBar("Historial de visitas", onBack = onBack)
        if (state.visits.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.History,
                title = "Aún no has registrado visitas",
                message = "Cuando registres una visita desde la ficha de un lugar, aparecerá aquí con su fecha.",
                primaryAction = "Explorar lugares" to onExplore,
            )
            return@Column
        }
        LazyColumn(Modifier.fillMaxSize()) {
            state.visits.groupBy { formatMonthYear(it.visitedAt) }.forEach { (month, visits) ->
                item {
                    Text(month, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp))
                }
                items(visits, key = { it.visitId }) { visit ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 64.dp)
                            .then(if (visit.placeAvailable) Modifier.clickable { onOpenPlace(visit.placeId) } else Modifier)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .semantics(mergeDescendants = true) {},
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlaceIllustration(visit.illustration, Modifier.size(48.dp).clip(ThumbnailShape))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(visit.placeName, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${visit.category.label} · Visitado el ${formatShortDate(visit.visitedAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(visit.stampTitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        if (visit.placeAvailable) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
            item {
                Text(
                    "Toca un lugar para abrir su ficha.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
