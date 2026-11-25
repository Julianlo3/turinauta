package com.unicauca.turinauta.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unicauca.turinauta.core.model.Illustration

/*
 * Ilustraciones vectoriales dibujadas con Canvas: nunca dependen de imágenes remotas,
 * así no hay placeholders rotos. Solo usan verde petróleo, ocre y arena.
 */

@Composable
fun PlaceIllustration(
    illustration: Illustration,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.background(SandHigh)) {
        when (illustration) {
            Illustration.BOWL -> drawBowl()
            Illustration.WEAVE -> drawWeave(columns = 2)
            Illustration.CIRCLES -> drawConcentric(size.center(), size.minDimension * 0.34f)
            Illustration.CUP -> drawCup()
            Illustration.BREAD -> drawBread()
            Illustration.CLAY -> drawClay()
        }
    }
}

/** Cabecera grande de la ficha: la misma ilustración con elementos decorativos. */
@Composable
fun PlaceHeaderArt(illustration: Illustration, modifier: Modifier = Modifier) {
    Canvas(modifier.background(SandHigh)) {
        drawCircle(Petrol, radius = size.height * 0.16f, center = Offset(size.width * 0.12f, size.height * 0.8f), style = Stroke(size.height * 0.03f))
        drawCircle(Ochre, radius = size.height * 0.1f, center = Offset(size.width * 0.86f, size.height * 0.28f))
        val w = size.height * 1.1f
        clipRect {
            translate(left = (size.width - w) / 2f) {
                val sub = Size(w, size.height)
                drawIllustrationIn(illustration, sub)
            }
        }
    }
}

/** Franja repetida para tarjetas de rutas temáticas. */
@Composable
fun RouteBannerArt(illustration: Illustration, modifier: Modifier = Modifier) {
    Canvas(modifier.background(SandHigh)) {
        val tile = size.height
        var x = 0f
        while (x < size.width) {
            translate(left = x) { drawIllustrationIn(illustration, Size(tile, tile)) }
            x += tile
        }
    }
}

private fun DrawScope.drawIllustrationIn(illustration: Illustration, area: Size) {
    when (illustration) {
        Illustration.BOWL -> drawBowl(area)
        Illustration.WEAVE -> drawWeave(columns = 2, area = area)
        Illustration.CIRCLES -> drawConcentric(Offset(area.width / 2, area.height / 2), area.minDimension * 0.34f)
        Illustration.CUP -> drawCup(area)
        Illustration.BREAD -> drawBread(area)
        Illustration.CLAY -> drawClay(area)
    }
}

private fun Size.center() = Offset(width / 2f, height / 2f)

private fun DrawScope.drawBowl(area: Size = size) {
    val w = area.width; val h = area.height
    val stroke = h * 0.035f
    for (i in -1..1) {
        val x = w / 2 + i * w * 0.12f
        val p = Path().apply {
            moveTo(x, h * 0.42f)
            cubicTo(x - w * 0.05f, h * 0.35f, x + w * 0.05f, h * 0.30f, x, h * 0.22f)
        }
        drawPath(p, Ochre, style = Stroke(stroke, cap = StrokeCap.Round))
    }
    drawRoundRect(Petrol, topLeft = Offset(w * 0.18f, h * 0.48f), size = Size(w * 0.64f, h * 0.05f), cornerRadius = CornerRadius(h * 0.03f))
    val bowl = Path().apply {
        moveTo(w * 0.22f, h * 0.53f)
        cubicTo(w * 0.24f, h * 0.80f, w * 0.76f, h * 0.80f, w * 0.78f, h * 0.53f)
        close()
    }
    drawPath(bowl, Petrol)
    drawCircle(Ochre, radius = h * 0.035f, center = Offset(w / 2, h * 0.62f))
}

private fun DrawScope.drawCup(area: Size = size) {
    val w = area.width; val h = area.height
    val stroke = h * 0.035f
    for (i in 0..1) {
        val x = w * 0.42f + i * w * 0.12f
        drawLine(Ochre, Offset(x, h * 0.22f), Offset(x, h * 0.36f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
    drawRoundRect(Petrol, topLeft = Offset(w * 0.28f, h * 0.42f), size = Size(w * 0.38f, h * 0.32f), cornerRadius = CornerRadius(h * 0.08f))
    drawCircle(Petrol, radius = h * 0.09f, center = Offset(w * 0.68f, h * 0.55f), style = Stroke(stroke * 1.4f))
    drawRoundRect(Petrol, topLeft = Offset(w * 0.2f, h * 0.78f), size = Size(w * 0.6f, h * 0.04f), cornerRadius = CornerRadius(h * 0.02f))
}

private fun DrawScope.drawBread(area: Size = size) {
    val w = area.width; val h = area.height
    drawOval(Ochre, topLeft = Offset(w * 0.18f, h * 0.36f), size = Size(w * 0.64f, h * 0.34f))
    for (i in -1..1) {
        val x = w / 2 + i * w * 0.12f
        drawLine(Petrol, Offset(x - w * 0.04f, h * 0.44f), Offset(x + w * 0.04f, h * 0.60f), strokeWidth = h * 0.03f, cap = StrokeCap.Round)
    }
    drawRoundRect(Petrol, topLeft = Offset(w * 0.14f, h * 0.74f), size = Size(w * 0.72f, h * 0.04f), cornerRadius = CornerRadius(h * 0.02f))
}

private fun DrawScope.drawClay(area: Size = size) {
    val w = area.width; val h = area.height
    val vase = Path().apply {
        moveTo(w * 0.40f, h * 0.22f)
        lineTo(w * 0.60f, h * 0.22f)
        cubicTo(w * 0.58f, h * 0.34f, w * 0.80f, h * 0.44f, w * 0.72f, h * 0.70f)
        lineTo(w * 0.28f, h * 0.70f)
        cubicTo(w * 0.20f, h * 0.44f, w * 0.42f, h * 0.34f, w * 0.40f, h * 0.22f)
        close()
    }
    drawPath(vase, Petrol)
    drawLine(Ochre, Offset(w * 0.30f, h * 0.48f), Offset(w * 0.70f, h * 0.48f), strokeWidth = h * 0.035f)
    drawRoundRect(Petrol, topLeft = Offset(w * 0.2f, h * 0.76f), size = Size(w * 0.6f, h * 0.04f), cornerRadius = CornerRadius(h * 0.02f))
}

private fun DrawScope.drawWeave(columns: Int, area: Size = size) {
    val w = area.width; val h = area.height
    val cell = w / columns
    val stroke = cell * 0.08f
    for (cx in 0 until columns) for (cy in 0 until columns) {
        val c = Offset(cell * cx + cell / 2, h / columns * cy + h / columns / 2)
        val r = cell * 0.42f
        val diamond = Path().apply {
            moveTo(c.x, c.y - r); lineTo(c.x + r, c.y); lineTo(c.x, c.y + r); lineTo(c.x - r, c.y); close()
        }
        drawPath(diamond, Petrol, style = Stroke(stroke))
        rotate(45f, pivot = c) {
            drawRect(Ochre, topLeft = Offset(c.x - r * 0.28f, c.y - r * 0.28f), size = Size(r * 0.56f, r * 0.56f))
        }
    }
}

private fun DrawScope.drawConcentric(center: Offset, radius: Float) {
    drawCircle(Petrol, radius, center, style = Stroke(radius * 0.16f))
    drawCircle(Petrol, radius * 0.62f, center, style = Stroke(radius * 0.12f))
    drawCircle(Ochre, radius * 0.32f, center)
}

/** Logo: círculo verde petróleo con trazo de ruta y punto ocre. */
@Composable
fun TurinautaLogo(modifier: Modifier = Modifier, size: Dp = 32.dp) {
    Canvas(modifier.size(size)) {
        drawCircle(Petrol)
        val p = Path().apply {
            moveTo(this@Canvas.size.width * 0.28f, this@Canvas.size.height * 0.70f)
            cubicTo(
                this@Canvas.size.width * 0.45f, this@Canvas.size.height * 0.62f,
                this@Canvas.size.width * 0.40f, this@Canvas.size.height * 0.40f,
                this@Canvas.size.width * 0.66f, this@Canvas.size.height * 0.32f,
            )
        }
        drawPath(p, Color.White, style = Stroke(this.size.width * 0.07f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))))
        drawCircle(Ochre, radius = this.size.width * 0.1f, center = Offset(this.size.width * 0.68f, this.size.height * 0.30f))
    }
}

/** Sello coleccionable: relleno si se obtuvo; borde punteado si está pendiente. */
@Composable
fun StampArt(illustration: Illustration, obtained: Boolean, modifier: Modifier = Modifier, size: Dp = 72.dp) {
    Box(modifier.size(size).clip(CircleShape)) {
        Canvas(Modifier.size(size)) {
            if (obtained) {
                drawCircle(SandHigh)
                drawCircle(Ochre, style = Stroke(this.size.width * 0.05f))
                drawCircle(Ochre, radius = this.size.width * 0.40f, style = Stroke(this.size.width * 0.015f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))))
            } else {
                drawCircle(SandLow)
                drawCircle(OutlineWarm, style = Stroke(this.size.width * 0.03f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))))
            }
            val inner = this.size.width * 0.56f
            translate(left = (this.size.width - inner) / 2, top = (this.size.height - inner) / 2) {
                if (obtained) drawIllustrationIn(illustration, Size(inner, inner))
                else drawCircle(OutlineWarm.copy(alpha = 0.5f), radius = inner * 0.28f, center = Offset(inner / 2, inner / 2))
            }
        }
    }
}
