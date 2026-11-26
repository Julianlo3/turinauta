package com.unicauca.turinauta.feature.visits

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.MedalInnerOchre
import com.unicauca.turinauta.core.designsystem.MutedOchre
import com.unicauca.turinauta.core.designsystem.OnDeepTeal
import com.unicauca.turinauta.core.designsystem.PlaceIllustration
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateDark
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SoftOchreContainer
import com.unicauca.turinauta.core.designsystem.SubtleOutline
import com.unicauca.turinauta.core.designsystem.SurfaceLowSand
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.designsystem.WarmIvory
import com.unicauca.turinauta.core.model.RewardBadge
import com.unicauca.turinauta.core.network.SupabaseConfig

/** Se muestra únicamente después de que el servidor confirma una visita con sello nuevo. */
@Composable
internal fun VisitStampSuccessDialog(
    visitId: String,
    placeName: String,
    reward: RewardBadge,
    onDismiss: () -> Unit,
    onViewCollection: () -> Unit,
) {
    val progress = remember(visitId) { Animatable(0f) }
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(visitId) {
        progress.snapTo(0f)
        progress.animateTo(0.43f, tween(390, easing = FastOutSlowInEasing))
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        progress.animateTo(0.52f, tween(130, easing = LinearEasing))
        progress.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(),
                color = PureWhite,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 12.dp,
            ) {
                Column(
                    modifier = Modifier.heightIn(max = 620.dp).verticalScroll(rememberScrollState()).padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "¡Visita registrada!",
                        style = MaterialTheme.typography.titleLarge,
                        color = SlateDark,
                        textAlign = TextAlign.Center,
                    )
                    StampRevealScene(reward = reward, progress = progress)
                    Text(
                        text = "Sello desbloqueado",
                        style = MaterialTheme.typography.labelLarge,
                        color = DeepTeal,
                    )
                    Text(
                        text = reward.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = SlateDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer { alpha = phase(progress.value, 0.55f, 0.85f) },
                    )
                    Text(
                        text = "Tu visita a $placeName quedó guardada correctamente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMedium,
                        textAlign = TextAlign.Center,
                    )
                    Button(
                        onClick = onViewCollection,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepTeal, contentColor = OnDeepTeal),
                        modifier = Modifier.fillMaxWidth().heightIn(min = TurinautaDimens.MinTouchTarget),
                    ) {
                        Text("Ver colección")
                    }
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().heightIn(min = TurinautaDimens.MinTouchTarget),
                    ) {
                        Text("Entendido", color = DeepTeal)
                    }
                }
            }
        }
    }
}

@Composable
private fun StampRevealScene(reward: RewardBadge, progress: Animatable<Float, *>) {
    val density = LocalDensity.current.density
    val imageUrl = remember(reward.imagePath) { SupabaseConfig.resolvePublicMediaUrl(reward.imagePath) }
    Box(
        modifier = Modifier.fillMaxWidth().height(222.dp)
            .clip(RoundedCornerShape(18.dp)).background(SurfaceLowSand),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 12.dp)
                .height(158.dp).clip(RoundedCornerShape(12.dp))
                .background(WarmIvory)
                .border(1.dp, SubtleOutline, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.size(124.dp).graphicsLayer {
                    val reveal = phase(progress.value, 0.53f, 0.84f)
                    alpha = reveal
                    scaleX = 0.84f + 0.16f * reveal
                    scaleY = scaleX
                },
                contentAlignment = Alignment.Center,
            ) {
                if (imageUrl == null) {
                    PlaceIllustration(reward.illustrationKey, size = 116.dp, cornerRadius = 58.dp)
                } else {
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = "Sello ${reward.title}",
                        contentScale = ContentScale.Fit,
                        loading = { PlaceIllustration(reward.illustrationKey, size = 116.dp, cornerRadius = 58.dp) },
                        error = { PlaceIllustration(reward.illustrationKey, size = 116.dp, cornerRadius = 58.dp) },
                        modifier = Modifier.size(116.dp),
                    )
                }
            }
        }
        WoodenStamp(
            modifier = Modifier.align(Alignment.TopCenter).size(160.dp).graphicsLayer {
                val down = phase(progress.value, 0f, 0.43f)
                val up = phase(progress.value, 0.52f, 1f)
                translationY = (-142f + 142f * down - 150f * up) * density
                val compression = 1f - 0.05f * (1f - kotlin.math.abs(progress.value - 0.48f) / 0.08f).coerceIn(0f, 1f)
                scaleY = compression
            },
        )
    }
}

@Composable
private fun WoodenStamp(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stem = Path().apply {
            moveTo(w * 0.35f, h * 0.28f)
            cubicTo(w * 0.28f, h * 0.40f, w * 0.44f, h * 0.46f, w * 0.43f, h * 0.68f)
            lineTo(w * 0.57f, h * 0.68f)
            cubicTo(w * 0.56f, h * 0.46f, w * 0.72f, h * 0.40f, w * 0.65f, h * 0.28f)
            close()
        }
        drawPath(stem, MutedOchre)
        drawOval(MedalInnerOchre, Offset(w * 0.31f, h * 0.08f), Size(w * 0.38f, h * 0.33f))
        drawOval(MutedOchre, Offset(w * 0.29f, h * 0.05f), Size(w * 0.38f, h * 0.33f))
        drawOval(SoftOchreContainer.copy(alpha = 0.7f), Offset(w * 0.38f, h * 0.09f), Size(w * 0.10f, h * 0.10f))
        drawRoundRect(
            color = MedalInnerOchre,
            topLeft = Offset(w * 0.11f, h * 0.75f),
            size = Size(w * 0.78f, h * 0.15f),
            cornerRadius = CornerRadius(w * 0.08f),
        )
        drawOval(MutedOchre, Offset(w * 0.11f, h * 0.65f), Size(w * 0.78f, h * 0.19f))
        drawOval(
            color = SoftOchreContainer.copy(alpha = 0.85f),
            topLeft = Offset(w * 0.19f, h * 0.68f),
            size = Size(w * 0.62f, h * 0.11f),
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}

private fun phase(value: Float, start: Float, end: Float): Float =
    ((value - start) / (end - start)).coerceIn(0f, 1f)

@Preview(showBackground = true)
@Composable
private fun VisitStampSuccessPreview() {
    TurinautaTheme {
        VisitStampSuccessDialog(
            visitId = "preview",
            placeName = "La Esquina del Maíz",
            reward = RewardBadge("s1", "Sello de La Esquina del Maíz", "", isEarned = true),
            onDismiss = {},
            onViewCollection = {},
        )
    }
}
