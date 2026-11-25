package com.unicauca.turinauta.core.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.outlined.AltRoute
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Exactamente 4 destinos de primer nivel en la barra inferior (`AGENTS.md` §3).
 */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    EXPLORE(
        route = TurinautaRoutes.EXPLORE,
        label = "Explorar",
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore,
    ),
    ROUTES(
        route = TurinautaRoutes.ROUTES,
        label = "Rutas",
        selectedIcon = Icons.AutoMirrored.Filled.AltRoute,
        unselectedIcon = Icons.AutoMirrored.Outlined.AltRoute,
    ),
    COLLECTION(
        route = TurinautaRoutes.COLLECTION,
        label = "Colección",
        selectedIcon = Icons.Filled.CollectionsBookmark,
        unselectedIcon = Icons.Outlined.CollectionsBookmark,
    ),
    PROFILE(
        route = TurinautaRoutes.PROFILE,
        label = "Perfil",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
    ),
}

object TurinautaRoutes {
    const val EXPLORE = "explore"
    const val ROUTES = "routes"
    const val COLLECTION = "collection"
    const val PROFILE = "profile"

    const val ARG_PLACE_ID = "placeId"
    const val PLACE_DETAIL_PATTERN = "place_detail/{$ARG_PLACE_ID}"

    const val ARG_ROUTE_ID = "routeId"
    const val ROUTE_DETAIL_PATTERN = "route_detail/{$ARG_ROUTE_ID}"

    const val AUTH = "auth"
    const val VISIT_HISTORY = "visit_history"
    const val ARG_STAMP_ID = "stampId"
    const val STAMP_DETAIL_PATTERN = "stamp_detail/{$ARG_STAMP_ID}"

    fun placeDetail(placeId: String): String = "place_detail/${Uri.encode(placeId)}"
    fun routeDetail(routeId: String): String = "route_detail/${Uri.encode(routeId)}"
    fun stampDetail(stampId: String): String = "stamp_detail/${Uri.encode(stampId)}"

    val topLevelRoutes: Set<String> = setOf(EXPLORE, ROUTES, COLLECTION, PROFILE)
}
