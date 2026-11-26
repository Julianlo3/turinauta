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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
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
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/** Intro de apertura inspirada en la secuencia del logo y el nombre del video de referencia. */
@Composable
fun LaunchIntro(onReadyForApp: () -> Unit, onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val timeline = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        timeline.animateTo(0.95f, animationSpec = tween(durationMillis = 2850, easing = LinearEasing))
        onReadyForApp()
        delay(500)
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
    val patternWave = phase(progress, 0.54f, 0.82f)
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
        IntroDoodleBackdrop(wave = patternWave)
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

@Composable
private fun IntroDoodleBackdrop(wave: Float) {
    val spots = remember { introDoodleSpots() }
    Canvas(Modifier.fillMaxSize()) {
        val doodleWidth = 29.dp.toPx()
        val inkWidth = 1.5.dp.toPx()
        // El punto del logo está a la derecha y algo arriba del centro al aparecer.
        val origin = Offset(size.width / 2f + 48.dp.toPx(), size.height / 2f - 30.dp.toPx())
        val maxRadius = hypot(
            maxOf(origin.x, size.width - origin.x),
            maxOf(origin.y, size.height - origin.y),
        ) + doodleWidth
        val waveRadius = maxRadius * wave
        if (waveRadius > 0f) {
            drawCircle(DeepTeal.copy(alpha = 0.025f), radius = waveRadius, center = origin)
            val revealArea = Path().apply {
                addOval(Rect(origin, waveRadius))
            }
            clipPath(revealArea) {
                spots.forEach { spot ->
                    val x = spot.x * size.width
                    val y = spot.y * size.height
                    if (hypot(x - origin.x, y - origin.y) > waveRadius + doodleWidth) return@forEach
                    val color = (if (spot.motif == 3 || spot.motif == 4) MutedOchre else DeepTeal)
                        .copy(alpha = 0.17f)
                    translate(left = x, top = y) {
                        rotate(spot.rotation) {
                            drawIntroDoodle(spot.motif, doodleWidth, inkWidth, color)
                            drawCircle(color, radius = inkWidth, center = Offset(doodleWidth * 0.76f, -doodleWidth * 0.67f))
                        }
                    }
                }
            }
            if (wave < 1f) {
                drawCircle(
                    color = DeepTeal.copy(alpha = 0.19f * (1f - 0.45f * wave)),
                    radius = waveRadius,
                    center = origin,
                    style = Stroke(width = 2.5.dp.toPx()),
                )
            }
        }
    }
}

private data class IntroDoodleSpot(val x: Float, val y: Float, val motif: Int, val rotation: Float)

private fun introDoodleSpots(): List<IntroDoodleSpot> {
    val random = Random(20261126)
    val spots = ArrayList<IntroDoodleSpot>(105)
    repeat(3500) {
        if (spots.size == 105) return spots
        val x = random.nextFloat() * 1.10f - 0.05f
        val y = random.nextFloat() * 1.10f - 0.05f
        if (spots.any { hypot((it.x - x) * 400f, (it.y - y) * 850f) < 43f }) return@repeat
        spots += IntroDoodleSpot(x, y, random.nextInt(6), random.nextInt(-25, 26).toFloat())
    }
    return spots
}

private fun DrawScope.drawIntroDoodle(motif: Int, width: Float, inkWidth: Float, color: Color) {
    val r = width / 2f
    val outline = Stroke(width = inkWidth, cap = StrokeCap.Round)
    when (motif) {
        0 -> { // Punto de un lugar
            val pin = Path().apply {
                moveTo(0f, r)
                cubicTo(-r * 0.32f, r * 0.50f, -r * 0.73f, 0f, -r * 0.73f, -r * 0.32f)
                cubicTo(-r * 0.73f, -r * 1.32f, r * 0.73f, -r * 1.32f, r * 0.73f, -r * 0.32f)
                cubicTo(r * 0.73f, 0f, r * 0.32f, r * 0.50f, 0f, r)
                close()
            }
            drawPath(pin, color, style = outline)
            drawCircle(color, radius = r * 0.22f, center = Offset(0f, -r * 0.36f), style = outline)
        }
        1 -> { // Camino entre dos paradas
            val route = Path().apply {
                moveTo(-r, r * 0.55f)
                cubicTo(-r * 0.15f, r * 0.60f, -r * 0.10f, -r * 0.12f, r * 0.45f, -r * 0.18f)
                cubicTo(r * 0.82f, -r * 0.22f, r * 0.80f, -r * 0.62f, r, -r * 0.70f)
            }
            drawPath(route, color, style = outline)
            drawCircle(color, radius = r * 0.16f, center = Offset(-r, r * 0.55f), style = outline)
            drawCircle(color, radius = r * 0.16f, center = Offset(r, -r * 0.70f), style = outline)
        }
        2 -> { // Fotografía de una visita
            drawRoundRect(
                color = color,
                topLeft = Offset(-r, -r * 0.55f),
                size = Size(width, r * 1.25f),
                cornerRadius = CornerRadius(r * 0.16f),
                style = outline,
            )
            drawCircle(color, radius = r * 0.40f, center = Offset(0f, r * 0.07f), style = outline)
            drawLine(color, Offset(-r * 0.52f, -r * 0.75f), Offset(-r * 0.10f, -r * 0.75f), inkWidth)
        }
        3 -> { // Sello coleccionable
            drawCircle(color, radius = r * 0.88f, style = outline)
            drawCircle(color, radius = r * 0.62f, style = outline)
            drawCircle(color, radius = r * 0.16f)
            drawLine(color, Offset(-r * 0.42f, r * 1.07f), Offset(0f, r * 1.31f), inkWidth)
            drawLine(color, Offset(r * 0.42f, r * 1.07f), Offset(0f, r * 1.31f), inkWidth)
        }
        4 -> { // Fachada de un lugar local
            drawRect(color, topLeft = Offset(-r * 0.80f, -r * 0.20f), size = Size(r * 1.60f, r * 1.10f), style = outline)
            val roof = Path().apply {
                moveTo(-r, -r * 0.20f)
                lineTo(-r * 0.76f, -r * 0.77f)
                lineTo(r * 0.76f, -r * 0.77f)
                lineTo(r, -r * 0.20f)
            }
            drawPath(roof, color, style = outline)
            drawRect(color, topLeft = Offset(-r * 0.21f, r * 0.27f), size = Size(r * 0.42f, r * 0.63f), style = outline)
        }
        else -> { // Brújula de exploración
            drawCircle(color, radius = r * 0.92f, style = outline)
            val needle = Path().apply {
                moveTo(r * 0.42f, -r * 0.57f)
                lineTo(r * 0.14f, r * 0.18f)
                lineTo(-r * 0.42f, r * 0.57f)
                lineTo(-r * 0.14f, -r * 0.18f)
                close()
            }
            drawPath(needle, color, style = outline)
        }
    }
}

private fun phase(value: Float, start: Float, end: Float): Float =
    ((value - start) / (end - start)).coerceIn(0f, 1f)
