package com.unicauca.turinauta.core.designsystem

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Superficies cálidas (Light Theme)
val WarmIvory = Color(0xFFFAF7F2)          // background
val PureWhite = Color(0xFFFFFFFF)          // surface (tarjetas principales)
val SurfaceLowSand = Color(0xFFF1EEE7)     // surfaceContainerLow / surfaceVariant (buscador, banner rutas)
val SurfaceNavSand = Color(0xFFF3EFE6)     // surfaceContainer (barra de navegación inferior)
val SurfaceThumbBeige = Color(0xFFEAE2D3)  // surfaceContainerHigh (fondo ilustrativo de miniaturas)

// Color primario — Verde petróleo profundo
val DeepTeal = Color(0xFF135054)           // primary
val OnDeepTeal = Color(0xFFFFFFFF)         // onPrimary
val SoftTealContainer = Color(0xFFD6E7E5)  // primaryContainer (chip activo, pill BottomBar, icono rutas)
val OnSoftTealContainer = Color(0xFF0E3A3D) // onPrimaryContainer

// Color secundario — Ocre discreto (solo hitos, insignias e ilustraciones; prohibido en texto pequeño sobre claro)
val MutedOchre = Color(0xFFC48A2D)         // secondary
val OnMutedOchre = Color(0xFFFFFFFF)       // onSecondary
val SoftOchreContainer = Color(0xFFF6E7CE) // secondaryContainer
val OnSoftOchreContainer = Color(0xFF3E2907) // onSecondaryContainer

// Texto y bordes
val SlateDark = Color(0xFF1C252C)          // onBackground / onSurface
val SlateMedium = Color(0xFF4A545C)        // onSurfaceVariant
val WarmOutline = Color(0xFF878278)        // outline (chips inactivos, píldora de ciudad)
val SubtleOutline = Color(0xFFE3DED3)      // outlineVariant (borde 1.dp en tarjetas blancas)

// Estados de error
val ErrorRed = Color(0xFFB3261E)           // error
val OnErrorWhite = Color(0xFFFFFFFF)       // onError
val ErrorContainerLight = Color(0xFFF9DEDC) // errorContainer
val OnErrorContainerDark = Color(0xFF410E0B) // onErrorContainer

val TurinautaLightColorScheme = lightColorScheme(
    primary = DeepTeal,
    onPrimary = OnDeepTeal,
    primaryContainer = SoftTealContainer,
    onPrimaryContainer = OnSoftTealContainer,
    secondary = MutedOchre,
    onSecondary = OnMutedOchre,
    secondaryContainer = SoftOchreContainer,
    onSecondaryContainer = OnSoftOchreContainer,
    background = WarmIvory,
    onBackground = SlateDark,
    surface = PureWhite,
    onSurface = SlateDark,
    surfaceVariant = SurfaceLowSand,
    onSurfaceVariant = SlateMedium,
    surfaceContainerLowest = PureWhite,
    surfaceContainerLow = SurfaceLowSand,
    surfaceContainer = SurfaceNavSand,
    surfaceContainerHigh = SurfaceThumbBeige,
    surfaceContainerHighest = SurfaceThumbBeige,
    outline = WarmOutline,
    outlineVariant = SubtleOutline,
    error = ErrorRed,
    onError = OnErrorWhite,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerDark,
)
