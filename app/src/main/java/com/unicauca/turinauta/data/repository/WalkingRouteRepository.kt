package com.unicauca.turinauta.data.repository

data class MapPoint(val latitude: Double, val longitude: Double)

data class WalkingRoute(val points: List<MapPoint>, val distanceMeters: Double, val durationSeconds: Double)

interface WalkingRouteRepository {
    suspend fun getRoute(from: MapPoint, to: MapPoint): WalkingRoute
}
