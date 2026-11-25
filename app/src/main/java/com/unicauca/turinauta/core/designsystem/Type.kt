package com.unicauca.turinauta.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import com.unicauca.turinauta.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val base = Typography()

/** Fuente de marca para títulos y nombres de sellos (Bricolage Grotesque, licencia OFL). */
val DisplayFontFamily = FontFamily(
    Font(R.font.bricolage_semibold, FontWeight.SemiBold),
    Font(R.font.bricolage_bold, FontWeight.Bold),
)

// Títulos con la fuente de marca; el cuerpo sigue en Roboto por legibilidad.
val TurinautaTypography = Typography(
    headlineMedium = base.headlineMedium.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontFamily = FontFamily.SansSerif),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = base.bodySmall.copy(fontFamily = FontFamily.SansSerif),
    labelLarge = base.labelLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
    labelMedium = base.labelMedium.copy(fontFamily = FontFamily.SansSerif),
    labelSmall = base.labelSmall.copy(fontFamily = FontFamily.SansSerif),
)

/** Overline de categoría en tarjetas: GASTRONOMÍA, ARTESANÍAS… */
val OverlineStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.8.sp,
)
