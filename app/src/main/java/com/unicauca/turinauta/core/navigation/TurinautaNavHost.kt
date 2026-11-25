package com.unicauca.turinauta.core.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.unicauca.turinauta.core.designsystem.LocalSnackbarHostState
import com.unicauca.turinauta.feature.auth.AuthScreen
import com.unicauca.turinauta.feature.collection.CollectionDetailScreen
import com.unicauca.turinauta.feature.collection.CollectionScreen
import com.unicauca.turinauta.feature.explore.ExploreScreen
import com.unicauca.turinauta.feature.explore.PlaceDetailScreen
import com.unicauca.turinauta.feature.profile.ProfileScreen
import com.unicauca.turinauta.feature.profile.VisitHistoryScreen
import com.unicauca.turinauta.feature.routes.RouteDetailScreen
import com.unicauca.turinauta.feature.routes.RoutesScreen

/** Scaffold principal: barra inferior de 4 destinos y snackbars compartidos. */
@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in Destinations.withBottomBar

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = { if (showBottomBar) TurinautaBottomBar(navController) },
        ) { padding ->
            TurinautaNavHost(navController, Modifier.padding(padding).consumeWindowInsets(padding))
        }
    }
}

@Composable
private fun TurinautaBottomBar(navController: NavHostController) {
    val backStack by navController.currentBackStackEntryAsState()
    val destination = backStack?.destination
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        TopLevelDestination.entries.forEach { top ->
            // Las pantallas secundarias resaltan su pestaña de origen.
            val selected = destination?.hierarchy?.any { it.route == top.route } == true ||
                (top == TopLevelDestination.ROUTES && destination?.route == Destinations.ROUTE_DETAIL) ||
                (top == TopLevelDestination.COLLECTION && destination?.route == Destinations.COLLECTION_DETAIL) ||
                (top == TopLevelDestination.PROFILE && destination?.route == Destinations.HISTORY)
            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigateToTopLevel(top) },
                icon = { Icon(top.icon, contentDescription = null) },
                label = { Text(top.label) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

private fun NavHostController.navigateToTopLevel(top: TopLevelDestination) {
    navigate(top.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun TurinautaNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val openPlace: (String) -> Unit = { navController.navigate(Destinations.place(it)) }
    val openLogin: () -> Unit = { navController.navigate(Destinations.auth()) }

    NavHost(navController, startDestination = TopLevelDestination.EXPLORE.route, modifier = modifier) {
        composable(TopLevelDestination.EXPLORE.route) {
            ExploreScreen(
                onOpenPlace = openPlace,
                onOpenRoutes = { navController.navigateToTopLevel(TopLevelDestination.ROUTES) },
            )
        }
        composable(TopLevelDestination.ROUTES.route) {
            RoutesScreen(
                onOpenRoute = { navController.navigate(Destinations.routeDetail(it)) },
                onExplore = { navController.navigateToTopLevel(TopLevelDestination.EXPLORE) },
            )
        }
        composable(TopLevelDestination.COLLECTION.route) {
            CollectionScreen(
                onOpenCollection = { navController.navigate(Destinations.collectionDetail(it)) },
                onLogin = openLogin,
                onOpenRoutes = { navController.navigateToTopLevel(TopLevelDestination.ROUTES) },
            )
        }
        composable(TopLevelDestination.PROFILE.route) {
            ProfileScreen(
                onLogin = openLogin,
                onOpenHistory = { navController.navigate(Destinations.HISTORY) },
                onOpenCollection = { navController.navigateToTopLevel(TopLevelDestination.COLLECTION) },
            )
        }
        composable(
            Destinations.PLACE,
            arguments = listOf(
                navArgument(Destinations.ARG_PLACE_ID) { type = NavType.StringType },
                navArgument(Destinations.ARG_ROUTE_ID) { type = NavType.StringType; nullable = true; defaultValue = null },
            ),
        ) {
            PlaceDetailScreen(
                onBack = { navController.popBackStack() },
                onLogin = { navController.navigate(Destinations.auth(it)) },
                onOpenCollection = { navController.navigateToTopLevel(TopLevelDestination.COLLECTION) },
            )
        }
        composable(
            Destinations.ROUTE_DETAIL,
            arguments = listOf(navArgument(Destinations.ARG_ROUTE_ID) { type = NavType.StringType }),
        ) { entry ->
            RouteDetailScreen(
                routeId = entry.arguments?.getString(Destinations.ARG_ROUTE_ID).orEmpty(),
                onBack = { navController.popBackStack() },
                onOpenPlace = { placeId, routeId -> navController.navigate(Destinations.place(placeId, routeId)) },
            )
        }
        composable(
            Destinations.AUTH,
            arguments = listOf(navArgument(Destinations.ARG_PLACE_NAME) { type = NavType.StringType; nullable = true; defaultValue = null }),
        ) {
            AuthScreen(
                onBack = { navController.popBackStack() },
                onSignedIn = { navController.popBackStack() },
            )
        }
        composable(
            Destinations.COLLECTION_DETAIL,
            arguments = listOf(navArgument(Destinations.ARG_COLLECTION_ID) { type = NavType.StringType }),
        ) { entry ->
            CollectionDetailScreen(
                collectionId = entry.arguments?.getString(Destinations.ARG_COLLECTION_ID).orEmpty(),
                onBack = { navController.popBackStack() },
                onOpenPlace = openPlace,
            )
        }
        composable(Destinations.HISTORY) {
            VisitHistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenPlace = openPlace,
                onExplore = { navController.navigateToTopLevel(TopLevelDestination.EXPLORE) },
            )
        }
    }
}
