package com.unicauca.turinauta.core.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector

/** Los 4 destinos de primer nivel de la NavigationBar. */
enum class TopLevelDestination(val route: String, val label: String, val icon: ImageVector) {
    EXPLORE("explore", "Explorar", Icons.Outlined.Explore),
    ROUTES("routes", "Rutas", Icons.Outlined.Route),
    COLLECTION("collection", "Colección", Icons.Outlined.WorkspacePremium),
    PROFILE("profile", "Perfil", Icons.Outlined.Person),
}

object Destinations {
    const val PLACE = "place/{placeId}?routeId={routeId}"
    const val ROUTE_DETAIL = "route/{routeId}"
    const val AUTH = "auth?placeName={placeName}"
    const val COLLECTION_DETAIL = "collection/{collectionId}"
    const val HISTORY = "history"

    const val ARG_PLACE_ID = "placeId"
    const val ARG_ROUTE_ID = "routeId"
    const val ARG_PLACE_NAME = "placeName"
    const val ARG_COLLECTION_ID = "collectionId"

    fun place(placeId: String, routeId: String? = null) =
        "place/$placeId" + (routeId?.let { "?routeId=$it" } ?: "")

    fun routeDetail(routeId: String) = "route/$routeId"

    fun auth(placeName: String? = null) =
        "auth" + (placeName?.let { "?placeName=${Uri.encode(it)}" } ?: "")

    fun collectionDetail(collectionId: String) = "collection/$collectionId"

    /** Pantallas secundarias que conservan la barra inferior. */
    val withBottomBar = setOf(ROUTE_DETAIL, COLLECTION_DETAIL, HISTORY) + TopLevelDestination.entries.map { it.route }
}
