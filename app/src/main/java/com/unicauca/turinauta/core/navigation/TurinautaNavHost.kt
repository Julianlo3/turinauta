package com.unicauca.turinauta.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.OnDeepTeal
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaLogoMark
import com.unicauca.turinauta.data.AppContainer
import com.unicauca.turinauta.feature.auth.AuthRoute
import com.unicauca.turinauta.feature.explore.ExploreRoute
import com.unicauca.turinauta.feature.explore.PlaceDetailRoute
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
                onBackClick = { navController.popBackStack() },
                onRequireAuth = { navController.navigate(TurinautaRoutes.AUTH) },
                onNavigateToCollection = { onNavigateToTopLevel(TopLevelDestination.COLLECTION) },
            )
        }

        composable(TurinautaRoutes.ROUTES) {
            RoutesRoute(
                routeRepository = container.routeRepository,
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

        composable(TurinautaRoutes.AUTH) {
            AuthRoute(
                authRepository = container.authRepository,
                onBackClick = { navController.popBackStack() },
                onAuthenticated = { navController.popBackStack() },
            )
        }

        composable(TurinautaRoutes.COLLECTION) {
            PhasePendingSection(
                title = "Tu colección de sellos",
                description = "Aquí podrás consultar el progreso de tus colecciones y recompensas digitales (Fase 5).",
                primaryActionLabel = "Volver a Explorar",
                onPrimaryAction = { onNavigateToTopLevel(TopLevelDestination.EXPLORE) },
            )
        }

        composable(TurinautaRoutes.PROFILE) {
            PhasePendingSection(
                title = "Perfil e historial",
                description = "Aquí gestionarás tu sesión con Google y tu historial de visitas registradas (Fase 5).",
                primaryActionLabel = "Iniciar sesión con Google",
                onPrimaryAction = { navController.navigate(TurinautaRoutes.AUTH) },
            )
        }
    }
}

@Composable
private fun PhasePendingSection(
    title: String,
    description: String,
    primaryActionLabel: String,
    onPrimaryAction: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TurinautaLogoMark(size = 56.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onPrimaryAction,
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepTeal,
                contentColor = OnDeepTeal,
            ),
            modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
        ) {
            Text(primaryActionLabel)
        }
    }
}
