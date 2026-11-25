package com.unicauca.turinauta.feature.profile

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.unicauca.turinauta.core.designsystem.MutedOchre
import com.unicauca.turinauta.core.designsystem.OnDeepTeal
import com.unicauca.turinauta.core.designsystem.OnSoftTealContainer
import com.unicauca.turinauta.core.designsystem.PlaceIllustration
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
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.VisitRepository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun VisitHistoryRoute(
    visitRepository: VisitRepository,
    authRepository: AuthRepository,
    onBackClick: () -> Unit,
    onPlaceClick: (String) -> Unit,
    onExploreClick: () -> Unit,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: VisitHistoryViewModel = viewModel(
        factory = VisitHistoryViewModel.factory(
            visitRepository = visitRepository,
            authRepository = authRepository,
        ),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    VisitHistoryScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onPlaceClick = onPlaceClick,
        onExploreClick = onExploreClick,
        onSignInClick = onSignInClick,
        onRetryClick = viewModel::loadHistory,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitHistoryScreen(
    uiState: VisitHistoryUiState,
    onBackClick: () -> Unit,
    onPlaceClick: (String) -> Unit,
    onExploreClick: () -> Unit,
    onSignInClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmIvory,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Historial de visitas",
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
            !uiState.isAuthenticated -> {
                HistoryCenteredMessage(
                    title = "Inicia sesión para ver tu historial",
                    message = "Tu historial de visitas y fotos se guarda de forma privada en tu cuenta.",
                    actionLabel = "Iniciar sesión con Google",
                    onActionClick = onSignInClick,
                    modifier = Modifier.padding(innerPadding),
                )
            }

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

            uiState.errorMessage != null -> {
                HistoryCenteredMessage(
                    title = "No pudimos cargar tu historial",
                    message = uiState.errorMessage,
                    actionLabel = "Reintentar",
                    onActionClick = onRetryClick,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            uiState.visits.isEmpty() -> {
                HistoryCenteredMessage(
                    title = "Aún no tienes visitas registradas",
                    message = "Explora los lugares de Popayán y registra tu primera visita para comenzar tu colección.",
                    actionLabel = "Explorar lugares",
                    onActionClick = onExploreClick,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            else -> {
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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = uiState.visits,
                        key = { it.id },
                    ) { visit ->
                        VisitHistoryCard(
                            visit = visit,
                            onClick = if (visit.placeAvailable) {
                                { onPlaceClick(visit.placeId) }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VisitHistoryCard(
    visit: VisitRecord,
    onClick: (() -> Unit)?,
) {
    val shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
    val formattedDate = remember(visit.visitedAtMillis, visit.visitedAtIso) {
        formatVisitDate(visit.visitedAtMillis, visit.visitedAtIso)
    }

    val clickableModifier = if (onClick != null) {
        Modifier
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }

    Surface(
        color = PureWhite,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .border(TurinautaDimens.CardBorderWidth, SubtleOutline, shape)
            .then(clickableModifier)
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (!visit.photoPublicUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = visit.photoPublicUrl,
                    contentDescription = "Foto registrada en ${visit.displayPlaceName}",
                    contentScale = ContentScale.Crop,
                    loading = {
                        PlaceIllustration(
                            illustrationKey = IllustrationKey.GENERIC,
                            size = 84.dp,
                            cornerRadius = 14.dp,
                        )
                    },
                    error = {
                        PlaceIllustration(
                            illustrationKey = IllustrationKey.GENERIC,
                            size = 84.dp,
                            cornerRadius = 14.dp,
                        )
                    },
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceLowSand),
                )
            } else {
                PlaceIllustration(
                    illustrationKey = IllustrationKey.GENERIC,
                    size = 84.dp,
                    cornerRadius = 14.dp,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = visit.displayPlaceName,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (visit.placeAvailable) SlateDark else SlateMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (!visit.placeAvailable) {
                    Surface(
                        color = SurfaceLowSand,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = "Ficha retirada · sello conservado",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }

                if (!visit.stampTitle.isNullOrBlank()) {
                    Surface(
                        color = SoftTealContainer,
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WorkspacePremium,
                                contentDescription = null,
                                tint = MutedOchre,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = visit.stampTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = OnSoftTealContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Event,
                        contentDescription = null,
                        tint = SlateMedium,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCenteredMessage(
    title: String,
    message: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
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
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepTeal,
                        contentColor = OnDeepTeal,
                    ),
                    modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

private fun formatVisitDate(millis: Long, fallbackIso: String): String {
    if (millis <= 0L) return fallbackIso.ifBlank { "Fecha registrada" }
    return runCatching {
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy · h:mm a", Locale.forLanguageTag("es-CO"))
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }.getOrDefault(fallbackIso)
}

@Preview(showBackground = true)
@Composable
private fun VisitHistoryScreenPreview() {
    TurinautaTheme {
        VisitHistoryScreen(
            uiState = VisitHistoryUiState(
                isAuthenticated = true,
                isLoading = false,
                visits = listOf(
                    VisitRecord(
                        id = "v1",
                        placeId = "p1",
                        placeName = "Cocina del Barrio",
                        placeAvailable = true,
                        visitedAtIso = "2026-11-24T19:30:00Z",
                        visitedAtMillis = 1795548600000L,
                        stampTitle = "Sabores de Fogón",
                    ),
                    VisitRecord(
                        id = "v2",
                        placeId = "p9",
                        placeName = "Lugar no disponible",
                        placeAvailable = false,
                        visitedAtIso = "2026-11-20T14:15:00Z",
                        visitedAtMillis = 1795184100000L,
                        stampTitle = "Hilos y Telar",
                    ),
                ),
            ),
            onBackClick = {},
            onPlaceClick = {},
            onExploreClick = {},
            onSignInClick = {},
            onRetryClick = {},
        )
    }
}
