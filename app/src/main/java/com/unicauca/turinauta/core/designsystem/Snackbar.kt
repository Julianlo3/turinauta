package com.unicauca.turinauta.core.designsystem

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

/** Host de snackbars del Scaffold principal, compartido por todas las pantallas. */
val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }
