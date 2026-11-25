package com.unicauca.turinauta.feature.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
import com.unicauca.turinauta.core.model.CollectionProgress
import com.unicauca.turinauta.core.model.IllustrationKey
import com.unicauca.turinauta.core.model.RewardBadge
import com.unicauca.turinauta.core.network.SupabaseConfig
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CollectionRepository

@Composable
fun CollectionRoute(
    collectionRepository: CollectionRepository,
    authRepository: AuthRepository,
    onSignInClick: () -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: CollectionViewModel = viewModel(
        factory = CollectionViewModel.factory(
            collectionRepository = collectionRepository,
            authRepository = authRepository,
        ),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectionScreen(
        uiState = uiState,
        onSignInClick = onSignInClick,
        onExploreClick = onExploreClick,
        onRetryClick = viewModel::refresh,
        modifier = modifier,
    )
}

@Composable
fun CollectionScreen(
    uiState: CollectionUiState,
    onSignInClick: () -> Unit,
    onExploreClick: () -> Unit,
    onRetryClick: () -> Unit,
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
        item(key = "collection_header") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Colección",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SlateDark,
                )
                Text(
                    text = if (uiState.isAuthenticated) {
                        "Sigue tu avance y revisa los sellos desbloqueados en cada visita."
                    } else {
                        "Explora las colecciones disponibles e inicia sesión para guardar tus sellos."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMedium,
                )
            }
        }

        // Resumen superior: progreso si hay sesión, o invitación discreta si es anónimo (R1)
        item(key = "collection_summary") {
            if (uiState.isAuthenticated) {
                CollectionSummaryBanner(
                    earnedStamps = uiState.earnedStamps,
                    totalStamps = uiState.totalStamps,
                    progressPercent = uiState.overallProgressPercent,
                )
            } else {
                AnonymousCollectionBanner(onSignInClick = onSignInClick)
            }
        }

        when {
            uiState.isLoading -> {
                item(key = "collection_loading") {
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
                                text = "Cargando colecciones y sellos...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateMedium,
                            )
                        }
                    }
                }
            }

            uiState.errorMessage != null -> {
                item(key = "collection_error") {
                    CollectionMessageCard(
                        title = if (uiState.isBackendNotConfigured) {
                            "Falta conectar el backend"
                        } else {
                            "No pudimos cargar tu colección"
                        },
                        message = uiState.errorMessage,
                        actionLabel = "Reintentar",
                        onActionClick = onRetryClick,
                    )
                }
            }

            uiState.collections.isEmpty() -> {
                item(key = "collection_empty") {
                    CollectionMessageCard(
                        title = "Aún no hay colecciones publicadas",
                        message = "Mientras se habilitan nuevas colecciones, descubre los lugares de Popayán en el catálogo.",
                        actionLabel = "Volver al catálogo",
                        onActionClick = onExploreClick,
                    )
                }
            }

            else -> {
                items(
                    items = uiState.collections,
                    key = { it.id },
                ) { collection ->
                    CollectionProgressCard(collection = collection)
                }
            }
        }
    }
}

@Composable
private fun CollectionSummaryBanner(
    earnedStamps: Int,
    totalStamps: Int,
    progressPercent: Int,
) {
    Surface(
        color = SoftTealContainer,
        shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius),
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.WorkspacePremium,
                    contentDescription = null,
                    tint = MutedOchre,
                    modifier = Modifier.size(28.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Progreso general",
                        style = MaterialTheme.typography.labelLarge,
                        color = OnSoftTealContainer,
                    )
                    Text(
                        text = "$earnedStamps de $totalStamps sellos obtenidos ($progressPercent%)",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSoftTealContainer,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { (progressPercent.coerceIn(0, 100)) / 100f },
                color = DeepTeal,
                trackColor = PureWhite.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
            )
        }
    }
}

@Composable
private fun AnonymousCollectionBanner(
    onSignInClick: () -> Unit,
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
            Text(
                text = "Guarda tus sellos con tu cuenta",
                style = MaterialTheme.typography.titleMedium,
                color = SlateDark,
            )
            Text(
                text = "Puedes ver todas las colecciones sin cuenta. Inicia sesión con Google para registrar tus visitas y marcar sellos como obtenidos.",
                style = MaterialTheme.typography.bodySmall,
                color = SlateMedium,
            )
            Button(
                onClick = onSignInClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepTeal,
                    contentColor = OnDeepTeal,
                ),
                modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
            ) {
                Text("Iniciar sesión con Google")
            }
        }
    }
}

@Composable
private fun CollectionProgressCard(
    collection: CollectionProgress,
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = collection.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = SlateDark,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${collection.earnedStamps}/${collection.totalStamps}",
                        style = MaterialTheme.typography.labelLarge,
                        color = DeepTeal,
                    )
                }
                if (collection.description.isNotBlank()) {
                    Text(
                        text = collection.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMedium,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { (collection.progressPercent.coerceIn(0, 100)) / 100f },
                    color = DeepTeal,
                    trackColor = SurfaceLowSand,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                )
                Text(
                    text = if (collection.isCompleted) {
                        "Colección completada (${collection.progressPercent}%)"
                    } else {
                        "${collection.progressPercent}% completado"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMedium,
                )
            }

            if (collection.stamps.isNotEmpty()) {
                HorizontalDivider(color = SubtleOutline)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    collection.stamps.forEach { badge ->
                        RewardBadgeRow(badge = badge)
                    }
                }
            }
        }
    }
}

/**
 * Fila de sello digital: distingue **Obtenida** y **Pendiente** siempre con icono + texto
 * (regla WCAG 2.2 / TalkBack de `AGENTS.md` §4).
 */
@Composable
private fun RewardBadgeRow(
    badge: RewardBadge,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StampThumbnail(
            imagePath = badge.imagePath,
            illustrationKey = badge.illustrationKey,
            isEarned = badge.isEarned,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = badge.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (badge.isEarned) SlateDark else SlateMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (badge.description.isNotBlank()) {
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // Insignia de estado con icono + texto explícito (nunca solo color)
        Surface(
            color = if (badge.isEarned) SoftTealContainer else SurfaceLowSand,
            shape = RoundedCornerShape(TurinautaDimens.ChipCornerRadius),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = if (badge.isEarned) {
                        Icons.Rounded.CheckCircle
                    } else {
                        Icons.Outlined.Lock
                    },
                    contentDescription = null,
                    tint = if (badge.isEarned) OnSoftTealContainer else SlateMedium,
                    modifier = Modifier.size(15.dp),
                )
                Text(
                    text = if (badge.isEarned) "Obtenida" else "Pendiente",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (badge.isEarned) OnSoftTealContainer else SlateMedium,
                )
            }
        }
    }
}

@Composable
private fun StampThumbnail(
    imagePath: String?,
    illustrationKey: IllustrationKey,
    isEarned: Boolean,
) {
    val resolvedUrl = remember(imagePath) {
        SupabaseConfig.resolvePublicMediaUrl(imagePath)
    }
    val visualAlpha = if (isEarned) 1f else 0.55f

    if (resolvedUrl == null) {
        PlaceIllustration(
            illustrationKey = illustrationKey,
            size = 56.dp,
            cornerRadius = 14.dp,
            modifier = Modifier.alpha(visualAlpha),
        )
    } else {
        SubcomposeAsyncImage(
            model = resolvedUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            loading = {
                PlaceIllustration(illustrationKey = illustrationKey, size = 56.dp, cornerRadius = 14.dp)
            },
            error = {
                PlaceIllustration(illustrationKey = illustrationKey, size = 56.dp, cornerRadius = 14.dp)
            },
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .alpha(visualAlpha),
        )
    }
}

@Composable
private fun CollectionMessageCard(
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
private fun CollectionScreenPreview() {
    TurinautaTheme {
        CollectionScreen(
            uiState = CollectionUiState(
                isAuthenticated = true,
                isLoading = false,
                collections = listOf(
                    CollectionProgress(
                        id = "col-1",
                        name = "Sabores y Café",
                        slug = "sabores-cafe",
                        description = "Sellos gastronómicos y de café de origen en Popayán.",
                        totalStamps = 2,
                        earnedStamps = 1,
                        progressPercent = 50,
                        stamps = listOf(
                            RewardBadge(
                                id = "s1",
                                title = "Sabores de Fogón",
                                description = "Visita a Cocina del Barrio",
                                illustrationKey = IllustrationKey.GASTRONOMIA,
                                isEarned = true,
                            ),
                            RewardBadge(
                                id = "s2",
                                title = "Grano de Altura",
                                description = "Visita a Tostadora de café de origen",
                                illustrationKey = IllustrationKey.CAFE,
                                isEarned = false,
                            ),
                        ),
                    ),
                ),
            ),
            onSignInClick = {},
            onExploreClick = {},
            onRetryClick = {},
        )
    }
}
