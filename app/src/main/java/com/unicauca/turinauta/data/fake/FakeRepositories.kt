package com.unicauca.turinauta.data.fake

import com.unicauca.turinauta.core.location.distanceMetersTo
import com.unicauca.turinauta.core.model.AuthResult
import com.unicauca.turinauta.core.model.CollectionProgress
import com.unicauca.turinauta.core.model.DataReport
import com.unicauca.turinauta.core.model.EditorialStatus
import com.unicauca.turinauta.core.model.GeoPoint
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.RewardBadge
import com.unicauca.turinauta.core.model.Route
import com.unicauca.turinauta.core.model.ThematicCollection
import com.unicauca.turinauta.core.model.UserProfile
import com.unicauca.turinauta.core.model.VisitPhoto
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.core.model.VisitResult
import com.unicauca.turinauta.core.network.FakeNetworkConfig
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.CollectionRepository
import com.unicauca.turinauta.data.repository.ReportRepository
import com.unicauca.turinauta.data.repository.RouteRepository
import com.unicauca.turinauta.data.repository.VisitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/*
 * Implementaciones en memoria que imitan las reglas del backend descrito en
 * CONTRATO_API_MVP.md (RLS, record_visit, submit_content_report).
 */

private const val DAY_MS = 24L * 60 * 60 * 1000

private fun publishedPlaces() =
    IllustrativeData.places.filter { it.status == EditorialStatus.PUBLISHED }.sortedBy { it.sortOrder }

class FakeCatalogRepositoryImpl(private val network: FakeNetworkConfig) : CatalogRepository {
    override suspend fun getPlaces(): List<Place> {
        network.simulateLatency()
        if (network.state.value.emptyCatalog) return emptyList()
        return publishedPlaces()
    }

    override suspend fun getPlace(placeId: String): Place? {
        network.simulateLatency(0.4f)
        return publishedPlaces().firstOrNull { it.id == placeId }
    }
}

class FakeRouteRepositoryImpl(private val network: FakeNetworkConfig) : RouteRepository {
    /** `available_routes`: publicadas y con al menos una parada visible. */
    private fun availableRoutes(): List<Route> {
        val visible = publishedPlaces().map { it.id }.toSet()
        return IllustrativeData.routes
            .filter { it.status == EditorialStatus.PUBLISHED && it.placeIds.any { id -> id in visible } }
            .sortedBy { it.sortOrder }
    }

    override suspend fun getRoutes(): List<Route> {
        network.simulateLatency()
        if (network.state.value.emptyRoutes) return emptyList()
        return availableRoutes()
    }

    override suspend fun getRoute(routeId: String): Route? {
        network.simulateLatency(0.4f)
        return availableRoutes().firstOrNull { it.id == routeId }
    }

    override suspend fun getRoutePlaces(routeId: String): List<Place> {
        val route = availableRoutes().firstOrNull { it.id == routeId } ?: return emptyList()
        val published = publishedPlaces().associateBy { it.id }
        return route.placeIds.mapNotNull { published[it] }
    }
}

/** Simula `supabase.auth.signInWith(Google)` devolviendo la cuenta de prueba. */
class FakeAuthRepositoryImpl(private val network: FakeNetworkConfig) : AuthRepository {
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    override val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    override suspend fun signInWithGoogle(): AuthResult {
        network.simulateLatency(1.4f)
        if (network.state.value.failGoogleSignIn) {
            return AuthResult.Error("No pudimos conectar con Google. Revisa tu conexión e inténtalo de nuevo.")
        }
        val user = UserProfile(id = "demo-user", displayName = IllustrativeData.DEMO_NAME, email = IllustrativeData.DEMO_EMAIL)
        _currentUser.value = user
        return AuthResult.Success(user)
    }

    override suspend fun signOut() {
        _currentUser.value = null
    }
}

class FakeVisitRepositoryImpl(
    private val network: FakeNetworkConfig,
    private val auth: AuthRepository,
) : VisitRepository {

    // Visitas por usuario; la cuenta de prueba trae una visita previa para mostrar progreso.
    private val store = MutableStateFlow(
        mapOf(
            "demo-user" to listOf(
                IllustrativeData.places.first { it.id == "amasijos-de-la-abuela" }
                    .toVisit(at = System.currentTimeMillis() - 7 * DAY_MS, photoPath = "demo-user/ejemplo.jpg"),
            ),
        ),
    )
    private val mutex = Mutex()

    override val visits: Flow<List<VisitRecord>> =
        combine(auth.currentUser, store) { user, all ->
            if (user == null) emptyList() else all[user.id].orEmpty().sortedByDescending { it.visitedAt }
        }

    override suspend fun recordVisit(placeId: String, photo: VisitPhoto, location: GeoPoint): VisitResult = mutex.withLock {
        val user = auth.currentUser.value ?: return VisitResult.VisitUnauthenticated

        // Reintento: la RPC devuelve already_recorded sin modificar la visita original.
        store.value[user.id].orEmpty().firstOrNull { it.placeId == placeId }?.let {
            return VisitResult.VisitDuplicate(it)
        }
        if (photo.mimeType !in VisitPhoto.ALLOWED_TYPES) {
            return VisitResult.VisitInvalidPhoto("La foto debe ser JPG, PNG o WebP.")
        }
        if (photo.sizeBytes > VisitPhoto.MAX_BYTES) {
            return VisitResult.VisitInvalidPhoto("La foto pesa más de 5 MB. Elige otra o tómala de nuevo.")
        }

        network.simulateLatency(2f) // subida a Storage + RPC
        if (network.state.value.failVisitSave) {
            return VisitResult.VisitNetworkError("Revisa tu conexión e inténtalo de nuevo.")
        }
        val place = publishedPlaces().firstOrNull { it.id == placeId }
            ?: return VisitResult.VisitNetworkError("Este lugar ya no está disponible.")

        val distance = location.distanceMetersTo(place.location)
        if (distance > VisitResult.MAX_DISTANCE_METERS) {
            return VisitResult.VisitTooFar(distance.toInt())
        }

        val extension = when (photo.mimeType) { "image/png" -> "png"; "image/webp" -> "webp"; else -> "jpg" }
        val visit = place.toVisit(at = System.currentTimeMillis(), photoPath = "${user.id}/${UUID.randomUUID()}.$extension")
        store.update { it + (user.id to (it[user.id].orEmpty() + visit)) }

        val userVisits = store.value[user.id].orEmpty()
        val collection = IllustrativeData.collections.firstOrNull { c -> c.stampIds.any { stampFor(it)?.placeId == placeId } }
        val progress = collection?.let { buildProgress(it, userVisits) }
        val badge = progress?.badges?.first { it.stamp.placeId == placeId }
            ?: RewardBadge(IllustrativeData.stamps.first { it.placeId == placeId }, "", place.name, earned = true, visitedAt = visit.visitedAt)
        VisitResult.VisitCreated(visit, badge, progress)
    }
}

class FakeCollectionRepositoryImpl(visitRepository: VisitRepository) : CollectionRepository {
    override val progress: Flow<List<CollectionProgress>> =
        visitRepository.visits.map { visits ->
            IllustrativeData.collections.sortedBy { it.sortOrder }.map { buildProgress(it, visits) }
        }
}

class FakeReportRepositoryImpl(private val network: FakeNetworkConfig) : ReportRepository {
    private val inbox = mutableListOf<DataReport>()

    override suspend fun sendReport(report: DataReport): Result<Unit> {
        network.simulateLatency()
        val details = report.details.trim()
        if (details.isEmpty() || details.length > DataReport.MAX_DETAILS) {
            return Result.failure(IllegalArgumentException("Describe qué dato está mal (máximo ${DataReport.MAX_DETAILS} caracteres)."))
        }
        inbox += report.copy(details = details)
        return Result.success(Unit) // {"status":"received"}
    }
}

private fun stampFor(stampId: String) = IllustrativeData.stamps.firstOrNull { it.id == stampId }

private fun Place.toVisit(at: Long, photoPath: String) = VisitRecord(
    visitId = UUID.randomUUID().toString(),
    placeId = id,
    placeName = name,
    placeAvailable = status == EditorialStatus.PUBLISHED,
    category = category,
    illustration = illustration,
    stampTitle = IllustrativeData.stamps.first { it.placeId == id }.title,
    photoPath = photoPath,
    visitedAt = at,
)

internal fun buildProgress(collection: ThematicCollection, visits: List<VisitRecord>): CollectionProgress {
    val byPlace = visits.associateBy { it.placeId }
    val places = publishedPlaces().associateBy { it.id }
    val badges = collection.stampIds.mapNotNull { stampId ->
        val stamp = stampFor(stampId) ?: return@mapNotNull null
        val place = places[stamp.placeId] ?: return@mapNotNull null
        val visit = byPlace[stamp.placeId]
        RewardBadge(stamp, collection.id, place.name, earned = visit != null, visitedAt = visit?.visitedAt)
    }
    return CollectionProgress(collection, badges)
}
