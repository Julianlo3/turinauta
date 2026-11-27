package com.unicauca.turinauta.feature.visits

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.MutedOchre
import com.unicauca.turinauta.core.designsystem.OnDeepTeal
import com.unicauca.turinauta.core.designsystem.PlaceIllustration
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateDark
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SoftTealContainer
import com.unicauca.turinauta.core.designsystem.OnSoftTealContainer
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.model.RewardBadge
import com.unicauca.turinauta.core.network.SupabaseConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Se muestra únicamente después de que el servidor confirma una visita con sello nuevo. */
@Composable
internal fun VisitStampSuccessDialog(
    visitId: String,
    placeName: String,
    reward: RewardBadge,
    onDismiss: () -> Unit,
    onViewStamp: () -> Unit,
) {
    val progress = remember(visitId) { Animatable(0f) }
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(visitId) {
        progress.snapTo(0f)
        delay(340)
        progress.animateTo(0.43f, tween(430, easing = FastOutSlowInEasing))
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        progress.animateTo(0.60f, tween(160, easing = LinearEasing))
        progress.animateTo(1f, tween(480, easing = FastOutSlowInEasing))
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
                Box {
                    SuccessConfetti(visitId = visitId, progress = progress, modifier = Modifier.matchParentSize())
                    Column(
                        modifier = Modifier.heightIn(max = 620.dp).verticalScroll(rememberScrollState()).padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "¡Visita registrada!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = SlateDark,
                            textAlign = TextAlign.Center,
                        )
                        StampRevealScene(reward = reward, progress = progress)
                        Surface(
                            color = SoftTealContainer,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.graphicsLayer { alpha = phase(progress.value, 0.60f, 0.86f) },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Icon(Icons.Outlined.LockOpen, contentDescription = null, tint = OnSoftTealContainer, modifier = Modifier.size(15.dp))
                                Text("Sello desbloqueado", style = MaterialTheme.typography.labelMedium, color = OnSoftTealContainer)
                            }
                        }
                        Text(
                            text = reward.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = SlateDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.graphicsLayer { alpha = phase(progress.value, 0.60f, 0.86f) },
                        )
                        Text(
                            text = "Tu visita a $placeName quedó registrada con éxito. ¡Sigue explorando para conseguir más sellos!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateMedium,
                            textAlign = TextAlign.Center,
                        )
                        Button(
                            onClick = onViewStamp,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepTeal, contentColor = OnDeepTeal),
                            modifier = Modifier.fillMaxWidth().heightIn(min = TurinautaDimens.MinTouchTarget),
                        ) {
                            Text("Ver detalles")
                        }
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, DeepTeal),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepTeal),
                            modifier = Modifier.fillMaxWidth().heightIn(min = TurinautaDimens.MinTouchTarget),
                        ) {
                            Text("Entendido")
                        }
                    }
                }
            }
        }
    }
}

private data class ConfettiPiece(
    val x: Float,
    val phaseOffset: Float,
    val angle: Float,
    val lengthDp: Float,
    val colorIndex: Int,
)

@Composable
private fun SuccessConfetti(visitId: String, progress: Animatable<Float, *>, modifier: Modifier = Modifier) {
    val fall = remember(visitId) { Animatable(0f) }
    LaunchedEffect(visitId) {
        snapshotFlow { progress.value }.first { it >= 0.43f }
        while (true) {
            fall.animateTo(fall.value + 1f, tween(6000, easing = LinearEasing))
        }
    }
    val pieces = remember(visitId) {
        val random = Random(visitId.hashCode())
        List(60) { index ->
            ConfettiPiece(
                x = random.nextFloat(),
                phaseOffset = (index + random.nextFloat() * 0.35f) / 60f,
                angle = random.nextFloat() * (2f * kotlin.math.PI.toFloat()),
                lengthDp = 5f + random.nextFloat() * 5f,
                colorIndex = random.nextInt(4),
            )
        }
    }
    val colors = remember { listOf(MutedOchre, DeepTeal, Color(0xFF88A884), Color(0xFFC98562)) }
    Canvas(modifier.clipToBounds()) {
        val opacity = phase(progress.value, 0.43f, 0.58f)
        if (opacity == 0f) return@Canvas
        val fadeStartY = 270.dp.toPx()
        val fadeEndY = 390.dp.toPx()
        pieces.forEach { piece ->
            val elapsed = fall.value - piece.phaseOffset
            if (elapsed < 0f) return@forEach
            val travel = elapsed % 1f
            val center = Offset(
                piece.x * size.width,
                -14.dp.toPx() + travel * (size.height + 28.dp.toPx()),
            )
            val fade = ((fadeEndY - center.y) / (fadeEndY - fadeStartY)).coerceIn(0f, 1f)
            if (fade == 0f) return@forEach
            val halfLength = piece.lengthDp.dp.toPx() / 2f
            val direction = Offset(cos(piece.angle) * halfLength, sin(piece.angle) * halfLength)
            drawLine(
                color = colors[piece.colorIndex].copy(alpha = 0.74f * opacity * fade),
                start = center - direction,
                end = center + direction,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun StampRevealScene(reward: RewardBadge, progress: Animatable<Float, *>) {
    val density = LocalDensity.current.density
    val imageUrl = remember(reward.imagePath) { SupabaseConfig.resolvePublicMediaUrl(reward.imagePath) }
    Box(
        modifier = Modifier.fillMaxWidth().size(height = 270.dp, width = 290.dp)
            .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(254.dp).graphicsLayer {
                val reveal = phase(progress.value, 0.60f, 0.86f)
                alpha = reveal
                scaleX = 0.90f + 0.10f * reveal
                scaleY = scaleX
            },
            contentAlignment = Alignment.Center,
        ) {
            if (imageUrl == null) {
                PlaceIllustration(reward.illustrationKey, size = 246.dp, cornerRadius = 123.dp, backgroundColor = Color.Transparent)
            } else {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "Sello ${reward.title}",
                    contentScale = ContentScale.Fit,
                    loading = { PlaceIllustration(reward.illustrationKey, size = 246.dp, cornerRadius = 123.dp, backgroundColor = Color.Transparent) },
                    error = { PlaceIllustration(reward.illustrationKey, size = 246.dp, cornerRadius = 123.dp, backgroundColor = Color.Transparent) },
                    modifier = Modifier.size(246.dp),
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
            modifier = Modifier.size(330.dp).graphicsLayer {
                val down = phase(progress.value, 0f, 0.43f)
                val lift = phase(progress.value, 0.60f, 0.97f)
                val press = kotlin.math.sin(phase(progress.value, 0.43f, 0.60f) * kotlin.math.PI).toFloat()
                val scale = 1.10f - 0.10f * down - 0.04f * press + 0.16f * lift
                scaleX = scale
                scaleY = scale
                translationX = (460f - 427f * down + 300f * lift) * density
                translationY = (-220f + 220f * down + 4f * press - 180f * lift) * density
                rotationZ = -10f + 10f * down - 8f * lift
            },
        )
    }
}

@Composable
private fun WoodenStamp(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val woodDark = Color(0xFF70462D)
        val woodMid = Color(0xFFAE6D35)
        val woodLight = Color(0xFFEBC18A)
        val woodGold = Color(0xFFD9954D)
        val baseCenter = Offset(w * 0.40f, h * 0.50f)
        val baseRadius = w * 0.39f // Diámetro de 257 dp, cercano a los 246 dp de la imagen.
        drawCircle(
            color = SlateDark.copy(alpha = 0.16f),
            radius = baseRadius,
            center = baseCenter + Offset(w * 0.018f, h * 0.028f),
        )
        drawCircle(
            brush = Brush.horizontalGradient(listOf(woodDark, woodGold, woodMid)),
            radius = baseRadius,
            center = baseCenter,
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(woodLight, woodGold, woodMid, woodDark),
                center = Offset(w * 0.30f, h * 0.34f),
                radius = w * 0.49f,
            ),
            radius = baseRadius * 0.91f,
            center = baseCenter + Offset(w * 0.018f, -h * 0.008f),
        )
        drawCircle(
            color = woodDark.copy(alpha = 0.42f),
            radius = baseRadius * 0.83f,
            center = baseCenter + Offset(w * 0.018f, -h * 0.008f),
            style = Stroke(width = 2.dp.toPx()),
        )
        drawCircle(
            color = woodLight.copy(alpha = 0.40f),
            radius = baseRadius * 0.74f,
            center = baseCenter + Offset(w * 0.018f, -h * 0.008f),
            style = Stroke(width = 1.5.dp.toPx()),
        )
        val handle = Path().apply {
            moveTo(w * 0.48f, h * 0.40f)
            cubicTo(w * 0.68f, h * 0.36f, w * 0.88f, h * 0.31f, w * 1.20f, h * 0.28f)
            lineTo(w * 1.20f, h * 0.51f)
            cubicTo(w * 0.88f, h * 0.52f, w * 0.68f, h * 0.57f, w * 0.48f, h * 0.58f)
            close()
        }
        drawPath(
            handle,
            Brush.verticalGradient(listOf(woodLight, woodGold, woodMid)),
        )
        drawOval(
            color = woodLight.copy(alpha = 0.56f),
            topLeft = Offset(w * 0.62f, h * 0.37f),
            size = Size(w * 0.24f, h * 0.035f),
        )
        drawCircle(
            color = woodDark.copy(alpha = 0.52f),
            radius = w * 0.125f,
            center = Offset(w * 0.47f, h * 0.49f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(woodLight, woodGold, woodMid),
                center = Offset(w * 0.43f, h * 0.43f),
                radius = w * 0.16f,
            ),
            radius = w * 0.105f,
            center = Offset(w * 0.47f, h * 0.47f),
        )
        drawCircle(
            color = woodLight.copy(alpha = 0.55f),
            radius = w * 0.035f,
            center = Offset(w * 0.44f, h * 0.43f),
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
            onViewStamp = {},
        )
    }
}
