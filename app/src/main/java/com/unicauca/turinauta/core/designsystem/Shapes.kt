package com.unicauca.turinauta.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val TurinautaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),      // Chips de categorías
    medium = RoundedCornerShape(16.dp),     // Miniaturas cuadradas (96.dp)
    large = RoundedCornerShape(20.dp),      // Tarjetas blancas y banner de Rutas temáticas
    extraLarge = RoundedCornerShape(28.dp), // Buscador y píldora «Popayán»
)

/**
 * Tokens dimensionales compartidos para consistencia visual y WCAG 2.2.
 */
object TurinautaDimens {
    val MinTouchTarget = 48.dp
    val CardBorderWidth = 1.dp
    val ThumbnailSize = 96.dp
    val ThumbnailCornerRadius = 16.dp
    val CardCornerRadius = 20.dp
    val ChipCornerRadius = 12.dp
    val PillCornerRadius = 28.dp
    val ScreenHorizontalPadding = 16.dp
}
