package com.unicauca.turinauta.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.OnSoftTealContainer
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SoftTealContainer
import com.unicauca.turinauta.core.designsystem.SubtleOutline
import com.unicauca.turinauta.core.designsystem.SurfaceNavSand

/**
 * Contenedor principal con barra compacta de 4 destinos (Explorar, Rutas, Colección, Perfil)
 * y manejo seguro de `WindowInsets` edge-to-edge.
 */
@Composable
fun MainScaffold(
    currentRoute: String?,
    showBottomBar: Boolean,
    snackbarHostState: SnackbarHostState,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                ShortNavigationBar(
                    modifier = Modifier.drawBehind {
                        drawLine(
                            color = SubtleOutline,
                            start = Offset.Zero,
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx(),
                        )
                    },
                    containerColor = SurfaceNavSand,
                ) {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentRoute == destination.route
                        ShortNavigationBarItem(
                            selected = selected,
                            onClick = { onDestinationSelected(destination) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) {
                                        destination.selectedIcon
                                    } else {
                                        destination.unselectedIcon
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            label = {
                                Text(
                                    text = destination.label,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            colors = ShortNavigationBarItemDefaults.colors(
                                selectedIconColor = OnSoftTealContainer,
                                selectedTextColor = DeepTeal,
                                selectedIndicatorColor = SoftTealContainer,
                                unselectedIconColor = SlateMedium,
                                unselectedTextColor = SlateMedium,
                            ),
                        )
                    }
                }
            }
        },
        content = content,
    )
}
