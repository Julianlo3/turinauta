package com.unicauca.turinauta.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.unicauca.turinauta.data.AppContainer
import com.unicauca.turinauta.feature.auth.AuthRoute
import com.unicauca.turinauta.feature.collection.CollectionRoute
import com.unicauca.turinauta.feature.collection.StampDetailRoute
import com.unicauca.turinauta.feature.explore.ExploreRoute
import com.unicauca.turinauta.feature.explore.PlaceDetailRoute
import com.unicauca.turinauta.feature.profile.ProfileRoute
import com.unicauca.turinauta.feature.profile.AboutScreen
import com.unicauca.turinauta.feature.profile.VisitHistoryRoute
import com.unicauca.turinauta.feature.routes.RouteDetailRoute
import com.unicauca.turinauta.feature.routes.RoutesRoute

@Composable
fun TurinautaApp(
    container: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in TurinautaRoutes.topLevelRoutes
    val snackbarHostState = remember { SnackbarHostState() }

    fun navigateToTopLevel(destination: TopLevelDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    MainScaffold(
        currentRoute = currentRoute,
        showBottomBar = showBottomBar,
        snackbarHostState = snackbarHostState,
        onDestinationSelected = ::navigateToTopLevel,
    ) { innerPadding ->
        TurinautaNavHost(
            navController = navController,
            container = container,
            onNavigateToTopLevel = ::navigateToTopLevel,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun TurinautaNavHost(
    navController: NavHostController,
    container: AppContainer,
    onNavigateToTopLevel: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TurinautaRoutes.EXPLORE,
        modifier = modifier,
    ) {
        composable(TurinautaRoutes.EXPLORE) {
            ExploreRoute(
                catalogRepository = container.catalogRepository,
                onRoutesBannerClick = { onNavigateToTopLevel(TopLevelDestination.ROUTES) },
                onPlaceClick = { placeId ->
                    navController.navigate(TurinautaRoutes.placeDetail(placeId))
                },
            )
        }

        composable(
            route = TurinautaRoutes.PLACE_DETAIL_PATTERN,
            arguments = listOf(navArgument(TurinautaRoutes.ARG_PLACE_ID) { type = NavType.StringType }),
        ) { entry ->
            val placeId = entry.arguments?.getString(TurinautaRoutes.ARG_PLACE_ID).orEmpty()
            PlaceDetailRoute(
                placeId = placeId,
                catalogRepository = container.catalogRepository,
                visitRepository = container.visitRepository,
                authRepository = container.authRepository,
                reportRepository = container.reportRepository,
                walkingRouteRepository = container.walkingRouteRepository,
                onBackClick = { navController.popBackStack() },
                onRequireAuth = { navController.navigate(TurinautaRoutes.AUTH) },
                onNavigateToCollection = { onNavigateToTopLevel(TopLevelDestination.COLLECTION) },
            )
        }

        composable(TurinautaRoutes.ROUTES) {
            RoutesRoute(
                routeRepository = container.routeRepository,
                catalogRepository = container.catalogRepository,
                visitRepository = container.visitRepository,
                authRepository = container.authRepository,
                onRouteClick = { routeId ->
                    navController.navigate(TurinautaRoutes.routeDetail(routeId))
                },
                onExploreClick = { onNavigateToTopLevel(TopLevelDestination.EXPLORE) },
            )
        }

        composable(
            route = TurinautaRoutes.ROUTE_DETAIL_PATTERN,
            arguments = listOf(navArgument(TurinautaRoutes.ARG_ROUTE_ID) { type = NavType.StringType }),
        ) { entry ->
            val routeId = entry.arguments?.getString(TurinautaRoutes.ARG_ROUTE_ID).orEmpty()
            RouteDetailRoute(
                routeId = routeId,
                routeRepository = container.routeRepository,
                catalogRepository = container.catalogRepository,
                visitRepository = container.visitRepository,
                authRepository = container.authRepository,
                onBackClick = { navController.popBackStack() },
                onPlaceClick = { placeId ->
                    navController.navigate(TurinautaRoutes.placeDetail(placeId))
                },
            )
        }

        composable(TurinautaRoutes.COLLECTION) {
            CollectionRoute(
                collectionRepository = container.collectionRepository,
                authRepository = container.authRepository,
                onSignInClick = { navController.navigate(TurinautaRoutes.AUTH) },
                onExploreClick = { onNavigateToTopLevel(TopLevelDestination.EXPLORE) },
                onStampClick = { stampId -> navController.navigate(TurinautaRoutes.stampDetail(stampId)) },
            )
        }

        composable(
            route = TurinautaRoutes.STAMP_DETAIL_PATTERN,
            arguments = listOf(navArgument(TurinautaRoutes.ARG_STAMP_ID) { type = NavType.StringType }),
        ) { entry ->
            val stampId = entry.arguments?.getString(TurinautaRoutes.ARG_STAMP_ID).orEmpty()
            StampDetailRoute(
                stampId = stampId,
                collectionRepository = container.collectionRepository,
                catalogRepository = container.catalogRepository,
                authRepository = container.authRepository,
                onBackClick = { navController.popBackStack() },
                onPlaceClick = { placeId -> navController.navigate(TurinautaRoutes.placeDetail(placeId)) },
            )
        }

        composable(TurinautaRoutes.PROFILE) {
            ProfileRoute(
                authRepository = container.authRepository,
                visitRepository = container.visitRepository,
                collectionRepository = container.collectionRepository,
                onSignInClick = { navController.navigate(TurinautaRoutes.AUTH) },
                onOpenVisitHistory = { navController.navigate(TurinautaRoutes.VISIT_HISTORY) },
                onOpenCollection = { onNavigateToTopLevel(TopLevelDestination.COLLECTION) },
                onOpenAbout = { navController.navigate(TurinautaRoutes.ABOUT) },
            )
        }

        composable(TurinautaRoutes.ABOUT) {
            AboutScreen(onBackClick = { navController.popBackStack() })
        }

        composable(TurinautaRoutes.VISIT_HISTORY) {
            VisitHistoryRoute(
                visitRepository = container.visitRepository,
                authRepository = container.authRepository,
                onBackClick = { navController.popBackStack() },
                onPlaceClick = { placeId ->
                    navController.navigate(TurinautaRoutes.placeDetail(placeId))
                },
                onExploreClick = { onNavigateToTopLevel(TopLevelDestination.EXPLORE) },
                onSignInClick = { navController.navigate(TurinautaRoutes.AUTH) },
            )
        }

        composable(TurinautaRoutes.AUTH) {
            AuthRoute(
                authRepository = container.authRepository,
                onBackClick = { navController.popBackStack() },
                onAuthenticated = { navController.popBackStack() },
            )
        }
    }
}
