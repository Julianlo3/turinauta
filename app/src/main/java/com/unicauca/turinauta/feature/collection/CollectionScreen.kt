package com.unicauca.turinauta.feature.collection

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.MutedOchre
import com.unicauca.turinauta.core.designsystem.OnDeepTeal
import com.unicauca.turinauta.core.designsystem.OnSoftOchreContainer
import com.unicauca.turinauta.core.designsystem.OnSoftTealContainer
import com.unicauca.turinauta.core.designsystem.PlaceIllustration
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateDark
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SoftTealContainer
import com.unicauca.turinauta.core.designsystem.SoftOchreContainer
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.launch

@Composable
fun CollectionRoute(
    collectionRepository: CollectionRepository,
    authRepository: AuthRepository,
    onSignInClick: () -> Unit,
    onExploreClick: () -> Unit,
    onStampClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: CollectionViewModel = viewModel(
        factory = CollectionViewModel.factory(
            collectionRepository = collectionRepository,
            authRepository = authRepository,
        ),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onScreenVisible() }

    CollectionScreen(
        uiState = uiState,
        onSignInClick = onSignInClick,
        onExploreClick = onExploreClick,
        onRetryClick = viewModel::refresh,
        onStampClick = onStampClick,
        modifier = modifier,
    )
}

@Composable
fun CollectionScreen(
    uiState: CollectionUiState,
    onSignInClick: () -> Unit,
    onExploreClick: () -> Unit,
    onRetryClick: () -> Unit,
    onStampClick: (String) -> Unit,
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
                    CollectionProgressCard(collection = collection, onStampClick = onStampClick)
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
                        text = "Progreso general en Popayán",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSoftTealContainer,
                    )
                    Text(
                        text = "$earnedStamps de $totalStamps sellos obtenidos",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMedium,
                    )
                }
                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DeepTeal,
                )
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
    onStampClick: (String) -> Unit,
) {
    val shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
    val completed = collection.isCompleted
    Surface(
        color = if (completed) SoftOchreContainer.copy(alpha = 0.55f) else PureWhite,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                TurinautaDimens.CardBorderWidth,
                if (completed) MutedOchre.copy(alpha = 0.55f) else SubtleOutline,
                shape,
            ),
    ) {
        Box {
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.padding(end = if (completed) 92.dp else 0.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
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
                        if (!completed) {
                            Text(
                                text = "${collection.earnedStamps}/${collection.totalStamps}",
                                style = MaterialTheme.typography.labelLarge,
                                color = DeepTeal,
                            )
                        }
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
                        color = if (completed) MutedOchre else DeepTeal,
                        trackColor = if (completed) PureWhite else SurfaceLowSand,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                    )
                    Text(
                        text = if (completed) {
                            "Colección completada (${collection.progressPercent}%)"
                        } else {
                            "${collection.progressPercent}% completado"
                        },
                        modifier = Modifier.padding(end = if (completed) 92.dp else 0.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMedium,
                    )
                }

                if (completed) Spacer(modifier = Modifier.height(32.dp))

                if (collection.stamps.isNotEmpty()) {
                    HorizontalDivider(color = if (completed) MutedOchre.copy(alpha = 0.3f) else SubtleOutline)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        collection.stamps.forEach { badge ->
                            RewardBadgeRow(badge = badge, onStampClick = onStampClick)
                        }
                    }
                }
            }
            if (completed) {
                CompletedCollectionMedal(
                    earnedStamps = collection.earnedStamps,
                    totalStamps = collection.totalStamps,
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun CompletedCollectionMedal(
    earnedStamps: Int,
    totalStamps: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.width(96.dp).height(112.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Colección completada, $earnedStamps de $totalStamps sellos obtenidos"
            },
        contentAlignment = Alignment.TopCenter,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val leftTail = Path().apply {
                moveTo(w * 0.23f, h * 0.48f)
                lineTo(w * 0.51f, h * 0.54f)
                lineTo(w * 0.39f, h)
                lineTo(w * 0.28f, h * 0.87f)
                lineTo(w * 0.08f, h * 0.94f)
                close()
            }
            val rightTail = Path().apply {
                moveTo(w * 0.49f, h * 0.54f)
                lineTo(w * 0.77f, h * 0.48f)
                lineTo(w * 0.92f, h * 0.94f)
                lineTo(w * 0.72f, h * 0.87f)
                lineTo(w * 0.61f, h)
                close()
            }
            drawPath(leftTail, DeepTeal)
            drawPath(rightTail, DeepTeal)

            val centerX = w * 0.5f
            val centerY = h * 0.34f
            val radius = w * 0.39f
            val medal = Path().apply {
                for (step in 0..240) {
                    val angle = step * 2.0 * PI / 240.0 - PI / 2.0
                    val scallop = 1f + 0.045f * cos(12.0 * angle).toFloat()
                    val x = centerX + radius * scallop * cos(angle).toFloat()
                    val y = centerY + radius * scallop * sin(angle).toFloat()
                    if (step == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(medal, MutedOchre)
            drawCircle(
                color = PureWhite.copy(alpha = 0.85f),
                radius = radius * 0.76f,
                center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                style = Stroke(width = 1.8.dp.toPx()),
            )
        }
        Box(
            modifier = Modifier.width(80.dp).height(76.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$earnedStamps de $totalStamps",
                color = OnSoftOchreContainer,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
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
    onStampClick: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val shake = remember(badge.id) { Animatable(0f) }
    val shift = with(LocalDensity.current) { 8.dp.toPx() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button) {
                if (badge.isEarned) {
                    onStampClick(badge.id)
                } else {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        shake.snapTo(0f)
                        for (target in listOf(-shift, shift, -shift * 0.7f, shift * 0.7f, 0f)) {
                            shake.animateTo(target, tween(durationMillis = 55))
                        }
                    }
                }
            }
            .semantics(mergeDescendants = true) {
                contentDescription = if (badge.isEarned) {
                    "Sello ${badge.title}, obtenido. Abrir detalle"
                } else {
                    "Sello ${badge.title}, pendiente. Bloqueado"
                }
            },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StampThumbnail(
            imagePath = badge.imagePath,
            illustrationKey = badge.illustrationKey,
            isEarned = badge.isEarned,
        )

        Text(
            text = badge.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            color = if (badge.isEarned) SlateDark else SlateMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        // Insignia de estado con icono + texto explícito (nunca solo color)
        Surface(
            color = if (badge.isEarned) SoftTealContainer else SurfaceLowSand,
            shape = RoundedCornerShape(TurinautaDimens.ChipCornerRadius),
            modifier = Modifier.offset { IntOffset(shake.value.roundToInt(), 0) },
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
            onStampClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CompletedCollectionCardPreview() {
    TurinautaTheme {
        Box(modifier = Modifier.background(WarmIvory).padding(16.dp)) {
            CollectionProgressCard(
                collection = CollectionProgress(
                    id = "col-complete",
                    name = "Sabores de la Ciudad",
                    slug = "sabores-de-la-ciudad",
                    description = "Descubre los sabores locales.",
                    totalStamps = 3,
                    earnedStamps = 3,
                    stamps = listOf(
                        RewardBadge("s1", "Sello del Café del Patio Blanco", "", isEarned = true),
                        RewardBadge("s2", "Sello de la Esquina del Maíz", "", isEarned = true),
                        RewardBadge("s3", "Sello de Cocina Local", "", isEarned = true),
                    ),
                ),
                onStampClick = {},
            )
        }
    }
}
