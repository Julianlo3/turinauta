package com.unicauca.turinauta.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unicauca.turinauta.core.model.IllustrationKey

/**
 * Emblema circular de Turinauta: fondo verde petróleo (#135054) con trazo de ruta
 * en marfil claro y punto de destino en ocre (#C48A2D).
 */
@Composable
fun TurinautaLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    contentDescription: String? = null,
) {
    val semanticsModifier = if (contentDescription != null) {
        modifier.semantics { this.contentDescription = contentDescription }
    } else {
        modifier
    }

    Canvas(modifier = semanticsModifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val radius = minOf(w, h) / 2f

        // Fondo circular verde petróleo
        drawCircle(
            color = DeepTeal,
            radius = radius,
            center = Offset(w / 2f, h / 2f),
        )

        // Trazo de ruta curvo en marfil
        val routePath = Path().apply {
            moveTo(w * 0.26f, h * 0.68f)
            cubicTo(
                w * 0.34f, h * 0.34f,
                w * 0.56f, h * 0.68f,
                w * 0.68f, h * 0.34f,
            )
        }
        drawPath(
            path = routePath,
            color = WarmIvory,
            style = Stroke(
                width = w * 0.085f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        // Punto de inicio sutil en marfil
        drawCircle(
            color = WarmIvory,
            radius = w * 0.055f,
            center = Offset(w * 0.26f, h * 0.68f),
        )

        // Punto de destino / hito en ocre discreto
        drawCircle(
            color = MutedOchre,
            radius = w * 0.11f,
            center = Offset(w * 0.70f, h * 0.32f),
        )
    }
}

/**
 * Miniatura ilustrativa vectorial en Compose sobre fondo `#EAE2D3` con trazos
 * `#135054` (verde petróleo) y acentos `#C48A2D` (ocre).
 */
@Composable
fun PlaceIllustration(
    illustrationKey: IllustrationKey,
    modifier: Modifier = Modifier,
    size: Dp = TurinautaDimens.ThumbnailSize,
    cornerRadius: Dp = TurinautaDimens.ThumbnailCornerRadius,
    backgroundColor: Color = SurfaceThumbBeige,
    contentDescription: String? = null,
) {
    val semanticsModifier = if (contentDescription != null) {
        modifier.semantics { this.contentDescription = contentDescription }
    } else {
        modifier
    }

    Box(
        modifier = semanticsModifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.14f)) {
            when (illustrationKey) {
                IllustrationKey.GASTRONOMIA -> drawGastronomiaIllustration()
                IllustrationKey.ARTESANIAS -> drawArtesaniasIllustration()
                IllustrationKey.CULTURA -> drawCulturaIllustration()
                IllustrationKey.AMASIJOS -> drawAmasijosIllustration()
                IllustrationKey.CERAMICA -> drawCeramicaIllustration()
                IllustrationKey.CAFE -> drawCafeIllustration()
                IllustrationKey.GENERIC -> drawGenericRouteIllustration()
            }
        }
    }
}

private fun DrawScope.drawGastronomiaIllustration() {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Ondas de vapor en ocre
    listOf(0.34f, 0.50f, 0.66f).forEach { xFactor ->
        val steam = Path().apply {
            moveTo(w * xFactor, h * 0.36f)
            quadraticTo(w * (xFactor - 0.05f), h * 0.27f, w * xFactor, h * 0.18f)
        }
        drawPath(steam, color = MutedOchre, style = Stroke(width = w * 0.055f, cap = StrokeCap.Round))
    }

    // Tapa y asas de olla de fogón
    drawLine(
        color = DeepTeal,
        start = Offset(w * 0.18f, h * 0.48f),
        end = Offset(w * 0.82f, h * 0.48f),
        strokeWidth = w * 0.07f,
        cap = StrokeCap.Round,
    )
    drawCircle(
        color = MutedOchre,
        radius = w * 0.06f,
        center = Offset(w * 0.50f, h * 0.42f),
    )

    // Cuerpo de la olla
    drawRoundRect(
        color = DeepTeal,
        topLeft = Offset(w * 0.22f, h * 0.50f),
        size = Size(w * 0.56f, h * 0.34f),
        cornerRadius = CornerRadius(w * 0.14f, w * 0.14f),
        style = stroke,
    )

    // Base del fogón en ocre
    drawLine(
        color = MutedOchre,
        start = Offset(w * 0.28f, h * 0.90f),
        end = Offset(w * 0.72f, h * 0.90f),
        strokeWidth = w * 0.06f,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawArtesaniasIllustration() {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Marco del telar
    drawRoundRect(
        color = DeepTeal,
        topLeft = Offset(w * 0.18f, h * 0.16f),
        size = Size(w * 0.64f, h * 0.68f),
        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
        style = stroke,
    )

    // Hilos verticales de urdimbre
    listOf(0.34f, 0.50f, 0.66f).forEach { x ->
        drawLine(
            color = DeepTeal.copy(alpha = 0.45f),
            start = Offset(w * x, h * 0.20f),
            end = Offset(w * x, h * 0.80f),
            strokeWidth = w * 0.035f,
        )
    }

    // Motivo geométrico tejido en ocre (rombo)
    val diamond = Path().apply {
        moveTo(w * 0.50f, h * 0.30f)
        lineTo(w * 0.70f, h * 0.50f)
        lineTo(w * 0.50f, h * 0.70f)
        lineTo(w * 0.30f, h * 0.50f)
        close()
    }
    drawPath(diamond, color = MutedOchre, style = stroke)
    drawCircle(
        color = DeepTeal,
        radius = w * 0.055f,
        center = Offset(w * 0.50f, h * 0.50f),
    )
}

private fun DrawScope.drawCulturaIllustration() {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Arco arquitectónico cultural
    val arch = Path().apply {
        moveTo(w * 0.20f, h * 0.84f)
        lineTo(w * 0.20f, h * 0.46f)
        cubicTo(w * 0.20f, h * 0.14f, w * 0.80f, h * 0.14f, w * 0.80f, h * 0.46f)
        lineTo(w * 0.80f, h * 0.84f)
    }
    drawPath(arch, color = DeepTeal, style = stroke)

    // Base del escenario
    drawLine(
        color = DeepTeal,
        start = Offset(w * 0.14f, h * 0.84f),
        end = Offset(w * 0.86f, h * 0.84f),
        strokeWidth = w * 0.065f,
        cap = StrokeCap.Round,
    )

    // Ondas acústicas / acordes en ocre
    drawArc(
        color = MutedOchre,
        startAngle = 210f,
        sweepAngle = 120f,
        useCenter = false,
        topLeft = Offset(w * 0.30f, h * 0.40f),
        size = Size(w * 0.40f, h * 0.40f),
        style = Stroke(width = w * 0.055f, cap = StrokeCap.Round),
    )
    drawCircle(
        color = MutedOchre,
        radius = w * 0.08f,
        center = Offset(w * 0.50f, h * 0.62f),
    )
}

private fun DrawScope.drawAmasijosIllustration() {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Espiga / brote de maíz en ocre
    drawCircle(
        color = MutedOchre,
        radius = w * 0.16f,
        center = Offset(w * 0.50f, h * 0.42f),
    )
    drawCircle(
        color = MutedOchre.copy(alpha = 0.78f),
        radius = w * 0.12f,
        center = Offset(w * 0.33f, h * 0.48f),
    )
    drawCircle(
        color = MutedOchre.copy(alpha = 0.78f),
        radius = w * 0.12f,
        center = Offset(w * 0.67f, h * 0.48f),
    )

    // Canasta artesanal en verde petróleo
    val basket = Path().apply {
        moveTo(w * 0.16f, h * 0.54f)
        lineTo(w * 0.84f, h * 0.54f)
        lineTo(w * 0.74f, h * 0.84f)
        lineTo(w * 0.26f, h * 0.84f)
        close()
    }
    drawPath(basket, color = SurfaceThumbBeige)
    drawPath(basket, color = DeepTeal, style = stroke)

    // Trama de la canasta
    drawLine(
        color = DeepTeal,
        start = Offset(w * 0.22f, h * 0.69f),
        end = Offset(w * 0.78f, h * 0.69f),
        strokeWidth = w * 0.05f,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawCeramicaIllustration() {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Vasija ancestral de barro
    val vessel = Path().apply {
        moveTo(w * 0.36f, h * 0.20f)
        lineTo(w * 0.64f, h * 0.20f)
        lineTo(w * 0.60f, h * 0.34f)
        cubicTo(w * 0.84f, h * 0.44f, w * 0.82f, h * 0.74f, w * 0.64f, h * 0.84f)
        lineTo(w * 0.36f, h * 0.84f)
        cubicTo(w * 0.18f, h * 0.74f, w * 0.16f, h * 0.44f, w * 0.40f, h * 0.34f)
        close()
    }
    drawPath(vessel, color = DeepTeal, style = stroke)

    // Franja decorativa ancestral en ocre
    drawLine(
        color = MutedOchre,
        start = Offset(w * 0.27f, h * 0.56f),
        end = Offset(w * 0.73f, h * 0.56f),
        strokeWidth = w * 0.06f,
        cap = StrokeCap.Round,
    )
    listOf(0.38f, 0.50f, 0.62f).forEach { x ->
        drawCircle(
            color = MutedOchre,
            radius = w * 0.038f,
            center = Offset(w * x, h * 0.67f),
        )
    }
}

private fun DrawScope.drawCafeIllustration() {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Aroma de café en ocre
    val aromaLeft = Path().apply {
        moveTo(w * 0.38f, h * 0.34f)
        quadraticTo(w * 0.33f, h * 0.25f, w * 0.38f, h * 0.16f)
    }
    val aromaRight = Path().apply {
        moveTo(w * 0.54f, h * 0.34f)
        quadraticTo(w * 0.49f, h * 0.25f, w * 0.54f, h * 0.16f)
    }
    drawPath(aromaLeft, color = MutedOchre, style = Stroke(width = w * 0.055f, cap = StrokeCap.Round))
    drawPath(aromaRight, color = MutedOchre, style = Stroke(width = w * 0.055f, cap = StrokeCap.Round))

    // Taza de café de origen
    drawRoundRect(
        color = DeepTeal,
        topLeft = Offset(w * 0.20f, h * 0.42f),
        size = Size(w * 0.48f, h * 0.38f),
        cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
        style = stroke,
    )

    // Asa de la taza
    drawArc(
        color = DeepTeal,
        startAngle = -85f,
        sweepAngle = 170f,
        useCenter = false,
        topLeft = Offset(w * 0.60f, h * 0.47f),
        size = Size(w * 0.20f, h * 0.24f),
        style = stroke,
    )

    // Plato base
    drawLine(
        color = MutedOchre,
        start = Offset(w * 0.16f, h * 0.86f),
        end = Offset(w * 0.74f, h * 0.86f),
        strokeWidth = w * 0.06f,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawGenericRouteIllustration() {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    val trail = Path().apply {
        moveTo(w * 0.22f, h * 0.76f)
        cubicTo(w * 0.30f, h * 0.34f, w * 0.62f, h * 0.72f, w * 0.76f, h * 0.28f)
    }
    drawPath(trail, color = DeepTeal, style = stroke)

    drawCircle(
        color = DeepTeal,
        radius = w * 0.07f,
        center = Offset(w * 0.22f, h * 0.76f),
    )
    drawCircle(
        color = MutedOchre,
        radius = w * 0.11f,
        center = Offset(w * 0.76f, h * 0.28f),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
private fun TurinautaDesignSystemPreview() {
    TurinautaTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TurinautaLogoMark(size = 36.dp)
                    Column {
                        Text(
                            text = "Turinauta",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "GASTRONOMÍA · ARTESANÍAS · CULTURA",
                            style = CategoryOverlineTextStyle,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    IllustrationKey.entries.forEach { key ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PlaceIllustration(illustrationKey = key, size = 72.dp)
                            Text(
                                text = key.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
