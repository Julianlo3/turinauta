package com.unicauca.turinauta.data

import com.unicauca.turinauta.data.remote.OsrmWalkingRouteRepository
import com.unicauca.turinauta.data.repository.MapPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class OsrmWalkingRouteRepositoryTest {
    private val repository = OsrmWalkingRouteRepository()

    @Test fun usesLongitudeBeforeLatitudeInPedestrianRequest() {
        val url = repository.routeUrl(MapPoint(2.4419, -76.6063), MapPoint(2.4442, -76.6021))
        assertEquals(
            "https://routing.openstreetmap.de/routed-foot/route/v1/driving/" +
                "-76.606300,2.441900;-76.602100,2.444200?overview=full&geometries=geojson&steps=false",
            url,
        )
    }

    @Test fun parsesGeoJsonCoordinatesInLatitudeLongitudeOrder() {
        val route = repository.parseRoute(
            """{"code":"Ok","routes":[{"distance":250.5,"duration":180.0,"geometry":{"coordinates":[[-76.6063,2.4419],[-76.6021,2.4442]]}}]}""",
        )
        assertEquals(MapPoint(2.4419, -76.6063), route.points.first())
        assertEquals(MapPoint(2.4442, -76.6021), route.points.last())
        assertEquals(250.5, route.distanceMeters, 0.001)
    }

    @Test fun rejectsMissingRoute() {
        assertThrows(IllegalStateException::class.java) {
            repository.parseRoute("""{"code":"NoRoute","routes":[]}""")
        }
    }
}
