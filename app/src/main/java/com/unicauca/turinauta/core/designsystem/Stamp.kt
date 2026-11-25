package com.unicauca.turinauta.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.unicauca.turinauta.core.model.Illustration
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Perfil del borde: onda suave, festón de lóbulos redondos o puntas de estrella. */
enum class StampProfile { WAVE, SCALLOP, STAR }

/**
 * Las seis formas de borde de la propuesta visual. Cada colección usa una
 * (ver [stampStyleFor]) para que la forma también indique a qué colección pertenece el sello.
 */
enum class StampStyle(val lobes: Int, val depth: Float, val profile: StampProfile) {
    FLOWER(10, 0.10f, StampProfile.SCALLOP),
    STAR(14, 0.12f, StampProfile.STAR),
    WAVE(12, 0.05f, StampProfile.WAVE),
    FINE_STAR(18, 0.10f, StampProfile.STAR),
    FESTOON(14, 0.08f, StampProfile.SCALLOP),
    FINE_WAVE(16, 0.045f, StampProfile.WAVE),
}

/** Borde por colección según su orden: 1.ª flor, 2.ª estrella, 3.ª onda… */
fun stampStyleFor(collectionIndex: Int): StampStyle =
    StampStyle.entries[((collectionIndex % StampStyle.entries.size) + StampStyle.entries.size) % StampStyle.entries.size]

/** Contorno del sello: un círculo cuyo radio sube y baja alrededor del borde. */
fun stampPath(style: StampStyle, center: Offset, radius: Float, steps: Int = 360): Path {
    val r = radius * (1 - style.depth)
    return Path().apply {
        for (i in 0..steps) {
            val a = 2 * PI * i / steps - PI / 2
            val k = when (style.profile) {
                StampProfile.WAVE -> cos(style.lobes * a)
                StampProfile.SCALLOP -> 2 * abs(sin(style.lobes * a / 2)) - 1
                StampProfile.STAR -> {
                    val t = ((style.lobes * a / PI) % 2 + 2) % 2
                    1 - 2 * abs(t - 1)
                }
            }
            val rr = r * (1 + style.depth * k)
            val x = center.x + (rr * cos(a)).toFloat()
            val y = center.y + (rr * sin(a)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}

/** Forma reutilizable (recortar, bordes, áreas táctiles) con el contorno del sello. */
class StampShape(private val style: StampStyle) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Generic(stampPath(style, Offset(size.width / 2, size.height / 2), size.minDimension / 2))
}

/**
 * Sello coleccionable.
 * - Obtenido: papel claro, borde ocre, perforación punteada interior y la ilustración a color.
 * - Pendiente: mismo borde en línea punteada gris, ilustración apagada y candado.
 * El estado nunca depende solo del color: la pantalla añade además el texto «Obtenido» / «Pendiente».
 */
@Composable
fun StampArt(
    illustration: Illustration,
    obtained: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    style: StampStyle = StampStyle.FLOWER,
    /** Colección completa: papel dorado y doble borde ocre. */
    golden: Boolean = false,
) {
    Canvas(modifier.size(size)) {
        val c = Offset(this.size.width / 2, this.size.height / 2)
        val radius = this.size.minDimension / 2 - this.size.minDimension * 0.03f
        val stroke = this.size.minDimension * 0.028f
        val outline = stampPath(style, c, radius)

        if (obtained) {
            drawPath(outline, if (golden) GoldenPaper else PaperColor)
            drawPath(outline, Ochre, style = Stroke(stroke, join = StrokeJoin.Round))
            if (golden) drawPath(stampPath(style, c, radius * 0.93f), Ochre, style = Stroke(stroke * 0.6f, join = StrokeJoin.Round))
            drawPath(
                stampPath(style, c, radius * 0.83f),
                Ochre.copy(alpha = 0.9f),
                style = Stroke(stroke * 0.35f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(stroke * 0.8f, stroke * 0.9f))),
            )
        } else {
            drawPath(outline, SandLow)
            drawPath(
                outline,
                OutlineWarm,
                style = Stroke(stroke * 0.8f, join = StrokeJoin.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(stroke * 1.8f, stroke * 1.4f))),
            )
        }

        val inner = this.size.minDimension * 0.46f
        translate(left = (this.size.width - inner) / 2, top = (this.size.height - inner) / 2 - this.size.minDimension * 0.02f) {
            if (obtained) {
                drawIllustrationIn(illustration, Size(inner, inner))
            } else {
                // Ilustración apagada: se dibuja y se cubre con el papel semitransparente.
                drawIllustrationIn(illustration, Size(inner, inner))
                drawRect(SandLow.copy(alpha = 0.72f), size = Size(inner, inner))
            }
        }
        if (!obtained) drawLock(Offset(c.x, c.y + radius * 0.58f), this.size.minDimension * 0.13f)
    }
}

private val PaperColor = Color(0xFFFBF6EC)
private val GoldenPaper = Color(0xFFF6E4B8)

private fun DrawScope.drawLock(center: Offset, w: Float) {
    val h = w * 0.75f
    drawRoundRect(
        OutlineWarm,
        topLeft = Offset(center.x - w / 2, center.y - h / 2),
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.18f),
    )
    drawArc(
        OutlineWarm,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x - w * 0.3f, center.y - h / 2 - w * 0.32f),
        size = Size(w * 0.6f, w * 0.64f),
        style = Stroke(w * 0.14f),
    )
}
