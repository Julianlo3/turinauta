package com.unicauca.turinauta.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unicauca.turinauta.data.AppContainer
import com.unicauca.turinauta.feature.auth.AuthRoute
import com.unicauca.turinauta.feature.collection.CollectionRoute
import com.unicauca.turinauta.feature.collection.CollectionViewModel
import com.unicauca.turinauta.feature.collection.StampDetailRoute
import com.unicauca.turinauta.feature.explore.ExploreRoute
import com.unicauca.turinauta.feature.explore.PlaceDetailRoute
import com.unicauca.turinauta.feature.profile.ProfileRoute
import com.unicauca.turinauta.feature.profile.AboutScreen
import com.unicauca.turinauta.feature.profile.HowItWorksScreen
import com.unicauca.turinauta.feature.profile.PhotosAndLocationScreen
import com.unicauca.turinauta.feature.profile.VisitHistoryRoute
import com.unicauca.turinauta.feature.routes.RouteDetailRoute
import com.unicauca.turinauta.feature.routes.RoutesRoute
import kotlinx.coroutines.launch

@Composable
fun TurinautaApp(
    container: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val pagerState = rememberPagerState(pageCount = { TopLevelDestination.entries.size })
    val scope = rememberCoroutineScope()
    val showBottomBar = currentRoute == TurinautaRoutes.EXPLORE
    val selectedRoute = if (showBottomBar) TopLevelDestination.entries[pagerState.currentPage].route else null
    val snackbarHostState = remember { SnackbarHostState() }
    val collectionViewModel: CollectionViewModel = viewModel(
        factory = CollectionViewModel.factory(
            collectionRepository = container.collectionRepository,
            authRepository = container.authRepository,
        ),
    )

    fun navigateToTopLevel(destination: TopLevelDestination) {
        if (currentRoute != TurinautaRoutes.EXPLORE) {
            navController.popBackStack(TurinautaRoutes.EXPLORE, inclusive = false)
        }
        scope.launch { pagerState.animateScrollToPage(destination.ordinal) }
    }

    MainScaffold(
        currentRoute = selectedRoute,
        showBottomBar = showBottomBar,
        snackbarHostState = snackbarHostState,
        onDestinationSelected = ::navigateToTopLevel,
    ) { innerPadding ->
        TurinautaNavHost(
            navController = navController,
            container = container,
            collectionViewModel = collectionViewModel,
            pagerState = pagerState,
            onNavigateToTopLevel = ::navigateToTopLevel,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun TurinautaNavHost(
    navController: NavHostController,
    container: AppContainer,
    collectionViewModel: CollectionViewModel,
    pagerState: PagerState,
    onNavigateToTopLevel: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TurinautaRoutes.EXPLORE,
        modifier = modifier,
    ) {
        composable(TurinautaRoutes.EXPLORE) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pagerState,
                    snapPositionalThreshold = 0.4f,
                ),
            ) { page ->
                when (TopLevelDestination.entries[page]) {
                    TopLevelDestination.EXPLORE -> ExploreRoute(
                        catalogRepository = container.catalogRepository,
                        onRoutesBannerClick = { onNavigateToTopLevel(TopLevelDestination.ROUTES) },
                        onOpenAbout = { navController.navigate(TurinautaRoutes.ABOUT) },
                        onPlaceClick = { placeId ->
                            navController.navigate(TurinautaRoutes.placeDetail(placeId))
                        },
                    )

                    TopLevelDestination.ROUTES -> RoutesRoute(
                        routeRepository = container.routeRepository,
                        catalogRepository = container.catalogRepository,
                        visitRepository = container.visitRepository,
                        authRepository = container.authRepository,
                        onRouteClick = { routeId ->
                            navController.navigate(TurinautaRoutes.routeDetail(routeId))
                        },
                        onExploreClick = { onNavigateToTopLevel(TopLevelDestination.EXPLORE) },
                        onOpenAbout = { navController.navigate(TurinautaRoutes.ABOUT) },
                    )

                    TopLevelDestination.COLLECTION -> CollectionRoute(
                        viewModel = collectionViewModel,
                        onSignInClick = { navController.navigate(TurinautaRoutes.AUTH) },
                        onExploreClick = { onNavigateToTopLevel(TopLevelDestination.EXPLORE) },
                        onStampClick = { stampId -> navController.navigate(TurinautaRoutes.stampDetail(stampId)) },
                        onOpenAbout = { navController.navigate(TurinautaRoutes.ABOUT) },
                    )

                    TopLevelDestination.PROFILE -> ProfileRoute(
                        authRepository = container.authRepository,
                        visitRepository = container.visitRepository,
                        collectionRepository = container.collectionRepository,
                        onSignInClick = { navController.navigate(TurinautaRoutes.AUTH) },
                        onOpenVisitHistory = { navController.navigate(TurinautaRoutes.VISIT_HISTORY) },
                        onOpenCollection = { onNavigateToTopLevel(TopLevelDestination.COLLECTION) },
                        onOpenHowItWorks = { navController.navigate(TurinautaRoutes.HOW_IT_WORKS) },
                        onOpenPhotosLocation = { navController.navigate(TurinautaRoutes.PHOTOS_AND_LOCATION) },
                        onOpenAbout = { navController.navigate(TurinautaRoutes.ABOUT) },
                    )
                }
            }
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
                onVisitCreated = collectionViewModel::refresh,
                onBackClick = { navController.popBackStack() },
                onRequireAuth = { navController.navigate(TurinautaRoutes.AUTH) },
                onNavigateToCollection = { onNavigateToTopLevel(TopLevelDestination.COLLECTION) },
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

        composable(TurinautaRoutes.ABOUT) {
            AboutScreen(onBackClick = { navController.popBackStack() })
        }

        composable(TurinautaRoutes.HOW_IT_WORKS) {
            HowItWorksScreen(onBackClick = { navController.popBackStack() })
        }

        composable(TurinautaRoutes.PHOTOS_AND_LOCATION) {
            PhotosAndLocationScreen(onBackClick = { navController.popBackStack() })
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
