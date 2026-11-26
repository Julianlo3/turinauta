package com.unicauca.turinauta.feature.visits

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
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
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
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
    val imageUrl = remember(reward.imagePath) { SupabaseConfig.resolvePublicMediaUrl(reward.imagePath) }
    Box(
        modifier = Modifier.fillMaxWidth().size(height = 240.dp, width = 280.dp)
            .clipToBounds().background(PureWhite),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(164.dp).graphicsLayer {
                val reveal = phase(progress.value, 0.52f, 0.83f)
                alpha = reveal
                scaleX = 0.88f + 0.12f * reveal
                scaleY = scaleX
            },
            contentAlignment = Alignment.Center,
        ) {
            if (imageUrl == null) {
                PlaceIllustration(reward.illustrationKey, size = 156.dp, cornerRadius = 78.dp, backgroundColor = PureWhite)
            } else {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "Sello ${reward.title}",
                    contentScale = ContentScale.Fit,
                    loading = { PlaceIllustration(reward.illustrationKey, size = 156.dp, cornerRadius = 78.dp, backgroundColor = PureWhite) },
                    error = { PlaceIllustration(reward.illustrationKey, size = 156.dp, cornerRadius = 78.dp, backgroundColor = PureWhite) },
                    modifier = Modifier.size(156.dp),
                )
            }
        }
        Canvas(modifier = Modifier.fillMaxWidth().size(height = 240.dp, width = 280.dp)) {
            val burst = phase(progress.value, 0.43f, 0.69f)
            if (burst > 0f && burst < 1f) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = 112.dp.toPx() + 12.dp.toPx() * burst
                repeat(10) { index ->
                    val angle = index * (2.0 * kotlin.math.PI / 10.0)
                    val direction = Offset(kotlin.math.cos(angle).toFloat(), kotlin.math.sin(angle).toFloat())
                    val start = center + direction * radius
                    val end = center + direction * (radius + 12.dp.toPx())
                    val sparkColor = if (index % 2 == 0) MutedOchre else DeepTeal
                    drawLine(sparkColor.copy(alpha = 1f - burst), start, end, strokeWidth = 3.dp.toPx())
                    drawCircle(sparkColor.copy(alpha = 1f - burst), radius = 2.dp.toPx(), center = end)
                }
            }
        }
        WoodenStamp(
            modifier = Modifier.size(240.dp).graphicsLayer {
                val down = phase(progress.value, 0f, 0.43f)
                val lift = phase(progress.value, 0.52f, 0.95f)
                val scale = 1.22f - 0.22f * down + 0.27f * lift
                scaleX = scale
                scaleY = scale
                alpha = 1f - phase(progress.value, 0.72f, 0.98f)
            },
        )
    }
}

@Composable
private fun WoodenStamp(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w * 0.50f, h * 0.50f)
        drawCircle(color = SlateDark.copy(alpha = 0.14f), radius = w * 0.46f, center = center + Offset(0f, h * 0.035f))
        drawCircle(color = MedalInnerOchre, radius = w * 0.46f, center = center)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SoftOchreContainer, MutedOchre, MedalInnerOchre),
                center = Offset(w * 0.38f, h * 0.32f),
                radius = w * 0.58f,
            ),
            radius = w * 0.42f,
            center = center,
        )
        drawCircle(color = MedalInnerOchre.copy(alpha = 0.65f), radius = w * 0.29f, center = center + Offset(0f, h * 0.02f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SoftOchreContainer, MutedOchre, MedalInnerOchre),
                center = Offset(w * 0.41f, h * 0.36f),
                radius = w * 0.34f,
            ),
            radius = w * 0.24f,
            center = center - Offset(0f, h * 0.06f),
        )
        drawOval(
            color = PureWhite.copy(alpha = 0.55f),
            topLeft = Offset(w * 0.39f, h * 0.32f),
            size = Size(w * 0.09f, h * 0.035f),
        )
        drawCircle(
            color = SoftOchreContainer.copy(alpha = 0.85f),
            radius = w * 0.39f,
            center = center,
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
