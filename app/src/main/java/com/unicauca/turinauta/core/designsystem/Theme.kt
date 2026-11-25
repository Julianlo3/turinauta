package com.unicauca.turinauta.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TurinautaColorScheme = lightColorScheme(
    primary = Petrol,
    onPrimary = OnPetrol,
    primaryContainer = PetrolContainer,
    onPrimaryContainer = OnPetrolContainer,
    secondary = Ochre,
    onSecondary = Slate,
    secondaryContainer = SandHigh,
    onSecondaryContainer = Slate,
    background = Ivory,
    onBackground = Slate,
    surface = SurfaceWhite,
    onSurface = Slate,
    surfaceVariant = SandLow,
    onSurfaceVariant = SlateMedium,
    surfaceContainerLowest = SurfaceWhite,
    surfaceContainerLow = SandLow,
    surfaceContainer = SandContainer,
    surfaceContainerHigh = SandHigh,
    surfaceContainerHighest = SandHigh,
    outline = OutlineWarm,
    outlineVariant = OutlineSoft,
    error = ErrorRed,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    inverseSurface = SnackbarDark,
    inverseOnSurface = SurfaceWhite,
    inversePrimary = PetrolContainer,
)

/** Modo oscuro: fondo petróleo profundo, verde petróleo claro y ocre más luminoso. */
val TurinautaDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8FC9C4),
    onPrimary = Color(0xFF0B2F31),
    primaryContainer = Color(0xFF1F4A4D),
    onPrimaryContainer = Color(0xFFD6E7E5),
    secondary = Color(0xFFD9A54E),
    onSecondary = Color(0xFF2A1C05),
    secondaryContainer = Color(0xFF3A3122),
    onSecondaryContainer = Color(0xFFF3E3C4),
    background = Color(0xFF111A1C),
    onBackground = Color(0xFFECE7DE),
    surface = Color(0xFF182326),
    onSurface = Color(0xFFECE7DE),
    surfaceVariant = Color(0xFF1E2B2E),
    onSurfaceVariant = Color(0xFFA9B4B6),
    surfaceContainerLowest = Color(0xFF0D1416),
    surfaceContainerLow = Color(0xFF1E2B2E),
    surfaceContainer = Color(0xFF1A2629),
    surfaceContainerHigh = Color(0xFF24353A),
    surfaceContainerHighest = Color(0xFF2B3E43),
    outline = Color(0xFF8A9497),
    outlineVariant = Color(0xFF2C3A3D),
    error = Color(0xFFF2B8B5),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    inverseSurface = Color(0xFFECE7DE),
    inverseOnSurface = Color(0xFF1C252C),
    inversePrimary = Petrol,
)

/** Sigue el tema del sistema: claro (marfil) u oscuro (petróleo profundo). */
@Composable
fun TurinautaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) TurinautaDarkColorScheme else TurinautaColorScheme,
        typography = TurinautaTypography,
        shapes = TurinautaShapes,
        content = content,
    )
}
