package com.unicauca.turinauta.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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

/** Tema claro único: la identidad es cálida y contenida, sin variante oscura en el piloto. */
@Composable
fun TurinautaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TurinautaColorScheme,
        typography = TurinautaTypography,
        shapes = TurinautaShapes,
        content = content,
    )
}
