package com.unicauca.turinauta.core.model

/**
 * Parada ordenada dentro de una ruta temática.
 * Alineada con `GET /route_places?select=route_id,place_id,suggested_order&route_id=eq.<uuid>`.
 */
data class RouteStop(
    val routeId: String,
    val placeId: String,
    val suggestedOrder: Int,
)

/**
 * Ruta temática publicada con al menos una parada visible.
 * Alineada con `GET /available_routes?select=id,name,slug,description,visit_conditions,image_path,sort_order`.
 */
data class Route(
    val id: String,
    val name: String,
    val slug: String,
    val description: String,
    val visitConditions: String? = null,
    val imagePath: String? = null,
    val sortOrder: Int = 0,
    val stops: List<RouteStop> = emptyList(),
    val editorialStatus: EditorialStatus = EditorialStatus.PUBLISHED,
) {
    val orderedPlaceIds: List<String>
        get() = stops.sortedBy { it.suggestedOrder }.map { it.placeId }
}
