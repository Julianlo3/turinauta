package com.unicauca.turinauta.feature.explore

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.MutedOchre
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateDark
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SubtleOutline
import com.unicauca.turinauta.core.designsystem.SurfaceLowSand
import com.unicauca.turinauta.core.designsystem.SurfaceThumbBeige
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import java.util.Locale

/**
 * Componente desacoplado de ubicación del lugar sin permisos de GPS ni dependencias externas.
 * Listo para sustituir su interior por MapLibre/OpenStreetMap sin modificar las pantallas.
 */
@Composable
fun PlaceLocationMap(
    latitude: Double,
    longitude: Double,
    placeName: String,
    addressReference: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
    val coordinatesText = String.format(Locale.US, "%.4f, %.4f", latitude, longitude)

    Surface(
        color = PureWhite,
        shape = shape,
        modifier = modifier
            .fillMaxWidth()
            .border(TurinautaDimens.CardBorderWidth, SubtleOutline, shape)
            .semantics(mergeDescendants = true) {},
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(SurfaceThumbBeige),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Cuadrícula editorial suave
                    listOf(0.22f, 0.50f, 0.78f).forEach { factor ->
                        drawLine(
                            color = SurfaceLowSand,
                            start = Offset(0f, h * factor),
                            end = Offset(w, h * factor),
                            strokeWidth = 6f,
                        )
                    }
                    listOf(0.20f, 0.48f, 0.76f).forEach { factor ->
                        drawLine(
                            color = SurfaceLowSand,
                            start = Offset(w * factor, 0f),
                            end = Offset(w * factor, h),
                            strokeWidth = 6f,
                        )
                    }

                    // Trazo de ruta hacia el punto del lugar
                    val routePath = Path().apply {
                        moveTo(w * 0.14f, h * 0.80f)
                        cubicTo(w * 0.28f, h * 0.54f, w * 0.42f, h * 0.68f, w * 0.52f, h * 0.46f)
                    }
                    drawPath(
                        path = routePath,
                        color = DeepTeal.copy(alpha = 0.75f),
                        style = Stroke(width = 7f, cap = StrokeCap.Round),
                    )

                    // Marcador circular del lugar (verde petróleo + centro ocre)
                    val pinCenter = Offset(w * 0.52f, h * 0.46f)
                    drawCircle(
                        color = DeepTeal.copy(alpha = 0.18f),
                        radius = 34f,
                        center = pinCenter,
                    )
                    drawCircle(
                        color = DeepTeal,
                        radius = 18f,
                        center = pinCenter,
                    )
                    drawCircle(
                        color = MutedOchre,
                        radius = 7f,
                        center = pinCenter,
                    )
                }

                Surface(
                    color = PureWhite.copy(alpha = 0.94f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp),
                ) {
                    Text(
                        text = coordinatesText,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = DeepTeal,
                    modifier = Modifier.size(20.dp),
                )
                Column {
                    Text(
                        text = placeName,
                        style = MaterialTheme.typography.titleSmall,
                        color = SlateDark,
                    )
                    Text(
                        text = addressReference.ifBlank { "Referencia en Popayán" },
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMedium,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceLocationMapPreview() {
    TurinautaTheme {
        PlaceLocationMap(
            latitude = 2.4419,
            longitude = -76.6063,
            placeName = "Cocina del Barrio",
            addressReference = "Sector histórico, a dos cuadras del parque",
            modifier = Modifier.padding(16.dp),
        )
    }
}
