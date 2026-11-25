package com.unicauca.turinauta.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.OnSoftTealContainer
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SoftTealContainer
import com.unicauca.turinauta.core.designsystem.SurfaceNavSand

/**
 * Contenedor principal con `NavigationBar` de 4 destinos (Explorar, Rutas, Colección, Perfil)
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
                NavigationBar(
                    containerColor = SurfaceNavSand,
                    tonalElevation = 0.dp,
                ) {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onDestinationSelected(destination) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) {
                                        destination.selectedIcon
                                    } else {
                                        destination.unselectedIcon
                                    },
                                    contentDescription = destination.label,
                                )
                            },
                            label = {
                                Text(
                                    text = destination.label,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OnSoftTealContainer,
                                selectedTextColor = DeepTeal,
                                indicatorColor = SoftTealContainer,
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
