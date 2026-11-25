package com.unicauca.turinauta.feature.explore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.unicauca.turinauta.core.designsystem.CardShape
import com.unicauca.turinauta.core.designsystem.SandHigh
import java.util.Locale
import kotlin.math.abs

/**
 * Mapa de ubicación del lugar, centrado en (latitude, longitude). No pide permiso de
 * ubicación ni hace llamadas de red: hoy dibuja un mapa estilizado con el marcador.
 */
@Composable
fun PlaceLocationMap(
    latitude: Double,
    longitude: Double,
    placeName: String,
    addressReference: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            Box(
                Modifier.fillMaxWidth().height(160.dp)
                    .semantics { contentDescription = "Mapa con la ubicación de $placeName" },
                contentAlignment = Alignment.Center,
            ) {
                // ── PUNTO DE INYECCIÓN DE MapLibreMap ─────────────────────────────────
                // Siguiente fase: reemplazar este Canvas por MapLibreMap (teselas de
                // OpenStreetMap, sin API keys de pago) centrado en LatLng(latitude, longitude)
                // con zoom ~16 y un único marcador. Mantener sin permiso de GPS.
                // ──────────────────────────────────────────────────────────────────────
                StylizedMap(seed = abs(latitude * 1000 + longitude * 1000).toInt())
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp).offset(y = (-14).dp),
                )
            }
            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text(addressReference, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Mapa ilustrativo · Popayán · " + String.format(Locale.US, "%.4f, %.4f", latitude, longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StylizedMap(seed: Int) {
    Canvas(Modifier.fillMaxWidth().height(160.dp)) {
        drawRect(Color(0xFFF1EEE7))
        val street = Color.White
        val w = size.width; val h = size.height
        val shift = (seed % 7) * 6f
        // Manzanas y parque
        drawRect(SandHigh, topLeft = Offset(w * 0.08f, h * 0.12f), size = Size(w * 0.18f, h * 0.28f))
        drawRect(Color(0xFFD6E7E5), topLeft = Offset(w * 0.62f + shift, h * 0.14f), size = Size(w * 0.16f, h * 0.30f))
        drawRect(SandHigh, topLeft = Offset(w * 0.34f, h * 0.60f), size = Size(w * 0.18f, h * 0.22f))
        // Calles en cuadrícula (trazado colonial)
        listOf(0.30f, 0.55f, 0.82f).forEach { x -> drawLine(street, Offset(w * x + shift, 0f), Offset(w * x + shift, h), strokeWidth = h * 0.08f) }
        listOf(0.45f).forEach { y -> drawLine(street, Offset(0f, h * y), Offset(w, h * y), strokeWidth = h * 0.08f) }
        // Río
        val river = Path().apply {
            moveTo(0f, h * 0.86f)
            cubicTo(w * 0.3f, h * 0.76f, w * 0.6f, h * 0.98f, w, h * 0.84f)
        }
        drawPath(river, Color(0xFFBFD7DF), style = Stroke(h * 0.06f))
    }
}
