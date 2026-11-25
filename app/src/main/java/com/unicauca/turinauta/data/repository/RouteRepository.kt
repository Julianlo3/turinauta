package com.unicauca.turinauta.data.repository

import com.unicauca.turinauta.core.model.Route
import com.unicauca.turinauta.core.model.RouteStop

/**
 * Repositorio de lectura pública de rutas temáticas y sus paradas visibles.
 */
interface RouteRepository {
    suspend fun getAvailableRoutes(): Result<List<Route>>

    suspend fun getRouteById(routeId: String): Result<Route?>

    suspend fun getRouteStops(routeId: String): Result<List<RouteStop>>
}
