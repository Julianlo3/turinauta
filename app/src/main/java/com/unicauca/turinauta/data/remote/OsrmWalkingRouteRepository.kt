package com.unicauca.turinauta.data.remote

import com.unicauca.turinauta.data.repository.MapPoint
import com.unicauca.turinauta.data.repository.WalkingRoute
import com.unicauca.turinauta.data.repository.WalkingRouteRepository
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Public FOSSGIS pedestrian OSRM instance. Calls are made only after opening the full map. */
class OsrmWalkingRouteRepository : WalkingRouteRepository {
    override suspend fun getRoute(from: MapPoint, to: MapPoint): WalkingRoute = withContext(Dispatchers.IO) {
        val url = routeUrl(from, to)
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 6_000
            connection.readTimeout = 8_000
            connection.setRequestProperty("User-Agent", "Turinauta/1.0 (com.unicauca.turinauta)")
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                error("El servicio de rutas no está disponible (${connection.responseCode}).")
            }
            parseRoute(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }

    internal fun routeUrl(from: MapPoint, to: MapPoint): String {
        fun coordinate(point: MapPoint) = String.format(Locale.US, "%.6f,%.6f", point.longitude, point.latitude)
        return "https://routing.openstreetmap.de/routed-foot/route/v1/driving/" +
            "${coordinate(from)};${coordinate(to)}?overview=full&geometries=geojson&steps=false"
    }

    internal fun parseRoute(body: String): WalkingRoute {
        val root = Json.parseToJsonElement(body).jsonObject
        check(root["code"]?.jsonPrimitive?.content == "Ok") { "No encontramos una ruta a pie para este lugar." }
        val route = root["routes"]?.jsonArray?.firstOrNull()?.jsonObject
            ?: error("No encontramos una ruta a pie para este lugar.")
        val points = route["geometry"]!!.jsonObject["coordinates"]!!.jsonArray.map { coordinate ->
            val pair = coordinate.jsonArray
            MapPoint(pair[1].jsonPrimitive.double, pair[0].jsonPrimitive.double)
        }
        check(points.size >= 2) { "La ruta recibida está incompleta." }
        return WalkingRoute(points, route["distance"]!!.jsonPrimitive.double, route["duration"]!!.jsonPrimitive.double)
    }
}
