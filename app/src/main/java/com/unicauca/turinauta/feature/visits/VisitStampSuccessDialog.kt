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
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.model.RewardBadge
import com.unicauca.turinauta.core.network.SupabaseConfig
import kotlinx.coroutines.delay

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
        delay(340)
        progress.animateTo(0.43f, tween(470, easing = FastOutSlowInEasing))
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        progress.animateTo(0.60f, tween(220, easing = LinearEasing))
        progress.animateTo(1f, tween(680, easing = FastOutSlowInEasing))
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
                        modifier = Modifier.graphicsLayer { alpha = phase(progress.value, 0.60f, 0.86f) },
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
        modifier = Modifier.fillMaxWidth().size(height = 270.dp, width = 290.dp)
            .clipToBounds().background(PureWhite),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(208.dp).graphicsLayer {
                val reveal = phase(progress.value, 0.60f, 0.86f)
                alpha = reveal
                scaleX = 0.90f + 0.10f * reveal
                scaleY = scaleX
            },
            contentAlignment = Alignment.Center,
        ) {
            if (imageUrl == null) {
                PlaceIllustration(reward.illustrationKey, size = 200.dp, cornerRadius = 100.dp, backgroundColor = PureWhite)
            } else {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "Sello ${reward.title}",
                    contentScale = ContentScale.Fit,
                    loading = { PlaceIllustration(reward.illustrationKey, size = 200.dp, cornerRadius = 100.dp, backgroundColor = PureWhite) },
                    error = { PlaceIllustration(reward.illustrationKey, size = 200.dp, cornerRadius = 100.dp, backgroundColor = PureWhite) },
                    modifier = Modifier.size(200.dp),
                )
            }
        }
        Canvas(modifier = Modifier.fillMaxWidth().size(height = 270.dp, width = 290.dp)) {
            val burst = phase(progress.value, 0.43f, 0.73f)
            if (burst > 0f && burst < 1f) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = 118.dp.toPx() + 10.dp.toPx() * burst
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
            modifier = Modifier.size(270.dp).graphicsLayer {
                val down = phase(progress.value, 0f, 0.43f)
                val lift = phase(progress.value, 0.60f, 0.97f)
                val press = kotlin.math.sin(phase(progress.value, 0.43f, 0.60f) * kotlin.math.PI).toFloat()
                val scale = 1.20f - 0.20f * down + 0.28f * lift
                scaleX = scale * (1f + 0.05f * press)
                scaleY = scale * (1f - 0.10f * press)
                translationX = (-28f * (1f - down) - 24f * lift) * density
                translationY = (-20f * (1f - down) - 18f * lift) * density
                rotationZ = -7f + 4f * down - 4f * lift
                alpha = 1f - phase(progress.value, 0.75f, 0.98f)
            },
        )
    }
}

@Composable
private fun WoodenStamp(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawOval(
            color = SlateDark.copy(alpha = 0.16f),
            topLeft = Offset(w * 0.04f, h * 0.20f),
            size = Size(w * 0.94f, h * 0.76f),
        )
        drawOval(
            color = MedalInnerOchre,
            topLeft = Offset(w * 0.03f, h * 0.17f),
            size = Size(w * 0.94f, h * 0.76f),
        )
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(SoftOchreContainer, MutedOchre, MedalInnerOchre),
                center = Offset(w * 0.34f, h * 0.30f),
                radius = w * 0.63f,
            ),
            topLeft = Offset(w * 0.03f, h * 0.13f),
            size = Size(w * 0.94f, h * 0.76f),
        )
        drawOval(
            color = SoftOchreContainer.copy(alpha = 0.85f),
            topLeft = Offset(w * 0.07f, h * 0.17f),
            size = Size(w * 0.86f, h * 0.68f),
            style = Stroke(width = 2.dp.toPx()),
        )
        val neck = Path().apply {
            moveTo(w * 0.28f, h * 0.30f)
            cubicTo(w * 0.26f, h * 0.40f, w * 0.38f, h * 0.48f, w * 0.41f, h * 0.57f)
            lineTo(w * 0.58f, h * 0.57f)
            cubicTo(w * 0.49f, h * 0.43f, w * 0.53f, h * 0.32f, w * 0.45f, h * 0.26f)
            close()
        }
        drawPath(
            neck,
            Brush.horizontalGradient(listOf(SoftOchreContainer, MutedOchre, MedalInnerOchre)),
        )
        drawOval(
            color = MedalInnerOchre.copy(alpha = 0.55f),
            topLeft = Offset(w * 0.28f, h * 0.08f),
            size = Size(w * 0.36f, h * 0.37f),
        )
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(SoftOchreContainer, MutedOchre, MedalInnerOchre),
                center = Offset(w * 0.36f, h * 0.15f),
                radius = w * 0.35f,
            ),
            topLeft = Offset(w * 0.24f, h * 0.06f),
            size = Size(w * 0.36f, h * 0.37f),
        )
        drawOval(
            color = PureWhite.copy(alpha = 0.62f),
            topLeft = Offset(w * 0.33f, h * 0.11f),
            size = Size(w * 0.11f, h * 0.04f),
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
