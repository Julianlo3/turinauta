package com.unicauca.turinauta.data.remote

import com.unicauca.turinauta.core.model.Route
import com.unicauca.turinauta.core.model.RouteStop
import com.unicauca.turinauta.core.network.SupabaseClientProvider
import com.unicauca.turinauta.data.remote.dto.AvailableRouteDto
import com.unicauca.turinauta.data.remote.dto.RouteStopDto
import com.unicauca.turinauta.data.remote.mapper.toDomain
import com.unicauca.turinauta.data.repository.RouteRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class SupabaseRouteRepository(
    private val clientProvider: SupabaseClientProvider,
) : RouteRepository {

    override suspend fun getAvailableRoutes(): Result<List<Route>> = runCatching {
        val client = clientProvider.requireClient()
        val routeDtos = client.postgrest["available_routes"]
            .select(columns = ROUTE_COLUMNS) {
                order(column = "sort_order", order = Order.ASCENDING)
            }
            .decodeList<AvailableRouteDto>()

        if (routeDtos.isEmpty()) return@runCatching emptyList()

        val allStops = client.postgrest["route_places"]
            .select(columns = STOP_COLUMNS) {
                filter { isIn("route_id", routeDtos.map { it.id }) }
                order(column = "suggested_order", order = Order.ASCENDING)
            }
            .decodeList<RouteStopDto>()
            .map { it.toDomain() }
            .groupBy { it.routeId }

        routeDtos.map { dto -> dto.toDomain(stops = allStops[dto.id].orEmpty()) }
    }

    override suspend fun getRouteById(routeId: String): Result<Route?> = runCatching {
        val client = clientProvider.requireClient()
        val routeDto = client.postgrest["available_routes"]
            .select(columns = ROUTE_COLUMNS) {
                filter { eq("id", routeId) }
                limit(1)
            }
            .decodeList<AvailableRouteDto>()
            .firstOrNull() ?: return@runCatching null

        val stops = getRouteStops(routeId).getOrDefault(emptyList())
        routeDto.toDomain(stops = stops)
    }

    override suspend fun getRouteStops(routeId: String): Result<List<RouteStop>> = runCatching {
        val client = clientProvider.requireClient()
        client.postgrest["route_places"]
            .select(columns = STOP_COLUMNS) {
                filter { eq("route_id", routeId) }
                order(column = "suggested_order", order = Order.ASCENDING)
            }
            .decodeList<RouteStopDto>()
            .map { it.toDomain() }
    }

    private companion object {
        val ROUTE_COLUMNS = Columns.list(
            "id",
            "name",
            "slug",
            "description",
            "visit_conditions",
            "image_path",
            "sort_order",
        )
        val STOP_COLUMNS = Columns.list(
            "route_id",
            "place_id",
            "suggested_order",
        )
    }
}
