package com.unicauca.turinauta.core.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Intro de apertura inspirada en la secuencia del logo y el nombre del video de referencia. */
@Composable
fun LaunchIntro(onReadyForApp: () -> Unit, onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val timeline = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        timeline.animateTo(0.95f, animationSpec = tween(durationMillis = 2850, easing = LinearEasing))
        onReadyForApp()
        delay(900)
        timeline.animateTo(1f, animationSpec = tween(durationMillis = 220, easing = LinearEasing))
        onFinished()
    }

    val progress = timeline.value
    val circleGrowth = FastOutSlowInEasing.transform(phase(progress, 0f, 0.32f))
    val logoScale = 0.06f + 0.94f * circleGrowth
    val roadProgress = phase(progress, 0.32f, 0.54f)
    val dotPop = FastOutSlowInEasing.transform(phase(progress, 0.53f, 0.55f))
    val dotSettle = FastOutSlowInEasing.transform(phase(progress, 0.55f, 0.60f))
    val splashProgress = phase(progress, 0.55f, 0.66f)
    val titleProgress = FastOutSlowInEasing.transform(phase(progress, 0.66f, 0.82f))
    val verticalShift = (1f - titleProgress) * 52f
    val overlayAlpha = 1f - phase(progress, 0.95f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
            .graphicsLayer { alpha = overlayAlpha }
            .pointerInput(Unit) { detectTapGestures(onTap = {}) }
            .semantics { contentDescription = "Introducción de Turinauta" },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.offset(y = verticalShift.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.size(208.dp).graphicsLayer {
                    scaleX = logoScale
                    scaleY = logoScale
                },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val diameter = size.minDimension
                    val center = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(color = DeepTeal, radius = diameter / 2f, center = center)

                    val road = Path().apply {
                        moveTo(diameter * 0.19f, diameter * 0.75f)
                        cubicTo(diameter * 0.37f, diameter * 0.68f, diameter * 0.65f, diameter * 0.58f, diameter * 0.61f, diameter * 0.51f)
                        cubicTo(diameter * 0.57f, diameter * 0.44f, diameter * 0.39f, diameter * 0.42f, diameter * 0.40f, diameter * 0.35f)
                        cubicTo(diameter * 0.40f, diameter * 0.30f, diameter * 0.53f, diameter * 0.28f, diameter * 0.65f, diameter * 0.285f)
                    }
                    val measure = PathMeasure().apply { setPath(road, false) }
                    val visibleLength = measure.length * roadProgress
                    val roadShape = Path().apply {
                        moveTo(diameter * 0.16f, diameter * 0.70f)
                        cubicTo(diameter * 0.33f, diameter * 0.66f, diameter * 0.50f, diameter * 0.58f, diameter * 0.56f, diameter * 0.54f)
                        cubicTo(diameter * 0.59f, diameter * 0.51f, diameter * 0.58f, diameter * 0.49f, diameter * 0.51f, diameter * 0.45f)
                        cubicTo(diameter * 0.44f, diameter * 0.42f, diameter * 0.36f, diameter * 0.40f, diameter * 0.37f, diameter * 0.35f)
                        cubicTo(diameter * 0.38f, diameter * 0.29f, diameter * 0.52f, diameter * 0.27f, diameter * 0.65f, diameter * 0.27f)
                        lineTo(diameter * 0.65f, diameter * 0.30f)
                        cubicTo(diameter * 0.53f, diameter * 0.31f, diameter * 0.44f, diameter * 0.33f, diameter * 0.43f, diameter * 0.35f)
                        cubicTo(diameter * 0.42f, diameter * 0.37f, diameter * 0.50f, diameter * 0.40f, diameter * 0.56f, diameter * 0.43f)
                        cubicTo(diameter * 0.65f, diameter * 0.47f, diameter * 0.69f, diameter * 0.52f, diameter * 0.66f, diameter * 0.57f)
                        cubicTo(diameter * 0.63f, diameter * 0.63f, diameter * 0.42f, diameter * 0.71f, diameter * 0.22f, diameter * 0.81f)
                        cubicTo(diameter * 0.20f, diameter * 0.78f, diameter * 0.18f, diameter * 0.75f, diameter * 0.16f, diameter * 0.70f)
                        close()
                    }
                    val revealedRoad = Path()
                    measure.getSegment(0f, visibleLength, revealedRoad, true)
                    clipPath(roadShape) {
                        drawPath(
                            path = revealedRoad,
                            color = Color.White,
                            style = Stroke(width = diameter * 0.28f, cap = StrokeCap.Round),
                        )
                    }
                    if (dotPop > 0f) {
                        val dotCenter = Offset(diameter * 0.73f, diameter * 0.27f)
                        if (splashProgress > 0f && splashProgress < 1f) {
                            val spread = FastOutSlowInEasing.transform(splashProgress)
                            val splashAlpha = sin(PI * splashProgress).toFloat() * 0.85f
                            val rayStart = diameter * (0.105f + 0.055f * spread)
                            for (degrees in listOf(-155.0, -110.0, -65.0, -20.0, 30.0, 75.0, 145.0)) {
                                val angle = Math.toRadians(degrees)
                                val direction = Offset(cos(angle).toFloat(), sin(angle).toFloat())
                                drawLine(
                                    color = MutedOchre.copy(alpha = splashAlpha),
                                    start = dotCenter + direction * rayStart,
                                    end = dotCenter + direction * (rayStart + diameter * 0.028f),
                                    strokeWidth = diameter * 0.011f,
                                    cap = StrokeCap.Round,
                                )
                            }
                            for (degrees in listOf(-130.0, -42.0, 55.0)) {
                                val angle = Math.toRadians(degrees)
                                val direction = Offset(cos(angle).toFloat(), sin(angle).toFloat())
                                drawCircle(
                                    color = MutedOchre.copy(alpha = splashAlpha),
                                    radius = diameter * 0.007f,
                                    center = dotCenter + direction * diameter * (0.14f + 0.07f * spread),
                                )
                            }
                        }
                        val dotScale = if (progress < 0.55f) 1.12f * dotPop else 1.12f - 0.12f * dotSettle
                        drawCircle(
                            color = MutedOchre,
                            radius = diameter * 0.083f * dotScale,
                            center = dotCenter,
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                text = "Turinauta",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 46.sp,
                    lineHeight = 54.sp,
                ),
                color = DeepTeal,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .graphicsLayer { alpha = titleProgress }
                    .drawWithContent {
                        clipRect(right = size.width * titleProgress) { this@drawWithContent.drawContent() }
                    },
            )
        }
    }
}

private fun phase(value: Float, start: Float, end: Float): Float =
    ((value - start) / (end - start)).coerceIn(0f, 1f)
