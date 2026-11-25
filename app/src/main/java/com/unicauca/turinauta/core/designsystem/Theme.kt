package com.unicauca.turinauta.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Tema visual oficial de Turinauta (Material Design 3 — Light Theme).
 * Estética clara, cálida, seria y contenida sin color dinámico que altere la paleta.
 */
@Composable
fun TurinautaTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TurinautaLightColorScheme,
        typography = TurinautaTypography,
        shapes = TurinautaShapes,
        content = content,
    )
}
