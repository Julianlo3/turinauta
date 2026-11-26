package com.unicauca.turinauta.feature

import com.unicauca.turinauta.core.model.Category
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.RecordVisitRequest
import com.unicauca.turinauta.core.model.RewardBadge
import com.unicauca.turinauta.core.model.Route
import com.unicauca.turinauta.core.model.RouteStop
import com.unicauca.turinauta.core.model.UserProfile
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.core.model.VisitResult
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.RouteRepository
import com.unicauca.turinauta.data.repository.VisitRepository
import com.unicauca.turinauta.feature.auth.AuthViewModel
import com.unicauca.turinauta.feature.routes.RouteDetailViewModel
import com.unicauca.turinauta.feature.routes.RoutesViewModel
import com.unicauca.turinauta.feature.visits.RegisterVisitUseCase
import com.unicauca.turinauta.feature.visits.VisitRegistrationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class Phase4ViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun routesAndRouteDetailViewModelsOrderStopsAndMarkVisited() = runTest(testDispatcher) {
        val route = Route(
            id = "r1",
            name = "Oficios y saberes",
            slug = "oficios",
            description = "Recorrido cultural",
            sortOrder = 1,
            stops = listOf(
                RouteStop("r1", "p2", suggestedOrder = 2),
                RouteStop("r1", "p1", suggestedOrder = 1),
            ),
        )
        val places = listOf(
            samplePlace("p1", "Taller de tejido"),
            samplePlace("p2", "Cerámica ancestral"),
        )
        val routeRepo = StubRouteRepository(listOf(route))
        val catalogRepo = StubCatalogRepository(places)
        val authRepo = StubAuthRepository(UserProfile("u1", "Julián", "julian@unicauca.edu.co"))
        val visitRepo = StubVisitRepository(
            history = listOf(
                VisitRecord(
                    id = "v1",
                    placeId = "p1",
                    placeName = "Taller de tejido",
                    visitedAtIso = "2026-11-24T19:00:00Z",
                ),
            ),
        )

        val routesVm = RoutesViewModel(
            routeRepository = routeRepo,
            catalogRepository = catalogRepo,
            visitRepository = visitRepo,
            authRepository = authRepo,
        )
        advanceUntilIdle()
        assertEquals(1, routesVm.uiState.value.routes.size)
        assertEquals(listOf("p1", "p2"), routesVm.uiState.value.routeItems.first().stopPlaces.map { it.id })
        assertEquals(1, routesVm.uiState.value.routeItems.first().visitedStopsCount)

        val detailVm = RouteDetailViewModel(
            routeId = "r1",
            routeRepository = routeRepo,
            catalogRepository = catalogRepo,
            visitRepository = visitRepo,
            authRepository = authRepo,
        )
        advanceUntilIdle()

        val stops = detailVm.uiState.value.stops
        assertEquals(listOf("p1", "p2"), stops.map { it.place.id })
        assertTrue(stops[0].isVisited)
        assertFalse(stops[1].isVisited)
    }

    @Test
    fun authViewModelTriggersGoogleSignInAndUpdatesAuthenticatedState() = runTest(testDispatcher) {
        val authRepo = StubAuthRepository(initialUser = null)
        val vm = AuthViewModel(authRepo)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isAuthenticated)

        vm.onContinueWithGoogleClick()
        advanceUntilIdle()

        assertEquals(1, authRepo.signInCalls)
        assertTrue(vm.uiState.value.isAuthenticated)
    }

    @Test
    fun visitRegistrationViewModelValidatesPhotoSubmitsAndSupportsRetry() = runTest(testDispatcher) {
        val visitRepo = StubVisitRepository(
            nextResult = VisitResult.VisitNetworkError("Sin conexión"),
        )
        var collectionRefreshes = 0
        val vm = VisitRegistrationViewModel(RegisterVisitUseCase(visitRepo)) { collectionRefreshes++ }

        // Rechaza foto inválida antes de subir
        vm.onPhotoSelected("content://bad", ByteArray(100) { 1 }, "gif")
        assertNotNull(vm.uiState.value.photoError)
        assertFalse(vm.uiState.value.canSubmit)

        // Foto válida + ubicación dentro de 200 m
        vm.onPhotoSelected("content://ok", ByteArray(2048) { 1 }, "jpg")
        vm.onLocationResolved(2.4419, -76.6063, 100f, 2.4420, -76.6063)
        assertTrue(vm.uiState.value.canSubmit)
        assertTrue(vm.uiState.value.isWithinRange)

        // Un nuevo intento no puede reutilizar una posición previa si la lectura falla.
        vm.onLocationLoading()
        assertFalse(vm.uiState.value.hasLocation)
        vm.onLocationError("Sin señal")
        assertFalse(vm.uiState.value.canSubmit)
        vm.onLocationResolved(2.4419, -76.6063, 100f, 2.4420, -76.6063)

        // Primer intento falla con error de red (HU-18)
        vm.submitVisit("p1")
        advanceUntilIdle()
        assertTrue(vm.uiState.value.visitResult is VisitResult.VisitNetworkError)
        assertEquals(0, collectionRefreshes)

        // Reintento exitoso con sello
        visitRepo.nextResult = VisitResult.VisitCreated(
            visit = VisitRecord(
                id = "v2",
                placeId = "p1",
                placeName = "Cocina del Barrio",
                visitedAtIso = "2026-11-24T20:15:00Z",
            ),
            reward = RewardBadge(
                id = "s1",
                title = "Sabores de Fogón",
                description = "Sello gastronómico",
                isEarned = true,
            ),
        )
        vm.submitVisit("p1")
        advanceUntilIdle()
        assertTrue(vm.uiState.value.visitResult is VisitResult.VisitCreated)
        assertEquals(2, visitRepo.registerCalls)
        assertEquals(1, collectionRefreshes)
    }

    @Test
    fun temporaryVisitRadiusAllowsFiveKilometersButBlocksBeyondTen() = runTest(testDispatcher) {
        val visitRepo = StubVisitRepository()
        val vm = VisitRegistrationViewModel(RegisterVisitUseCase(visitRepo))
        vm.onPhotoSelected("content://test", ByteArray(2048) { 1 }, "jpg")

        vm.onLocationResolved(2.4869, -76.6063, 100f, 2.4419, -76.6063)
        assertFalse(vm.uiState.value.isWithinRange) // El aviso visual conserva los 200 m.
        assertTrue(vm.uiState.value.canSubmit)
        assertTrue(vm.uiState.value.copy(distanceMeters = 10_000).canSubmit)
        assertFalse(vm.uiState.value.copy(distanceMeters = 10_001).canSubmit)
        vm.submitVisit("p1")
        advanceUntilIdle()
        assertEquals(1, visitRepo.registerCalls)

        vm.onLocationResolved(2.5419, -76.6063, 100f, 2.4419, -76.6063)
        assertFalse(vm.uiState.value.canSubmit)
        vm.submitVisit("p1")
        advanceUntilIdle()
        assertEquals(1, visitRepo.registerCalls)
    }

    private fun samplePlace(id: String, name: String) = Place(
        id = id,
        categoryId = "c1",
        categoryName = "Artesanías",
        name = name,
        slug = id,
        summary = "Resumen",
        description = "Descripción",
        locationReference = "Popayán",
        latitude = 2.4419,
        longitude = -76.6063,
    )

    private class StubRouteRepository(private val routes: List<Route>) : RouteRepository {
        override suspend fun getAvailableRoutes(): Result<List<Route>> = Result.success(routes)
        override suspend fun getRouteById(routeId: String): Result<Route?> =
            Result.success(routes.find { it.id == routeId })
        override suspend fun getRouteStops(routeId: String): Result<List<RouteStop>> =
            Result.success(routes.find { it.id == routeId }?.stops.orEmpty())
    }

    private class StubCatalogRepository(private val places: List<Place>) : CatalogRepository {
        override suspend fun getCategories(): Result<List<Category>> = Result.success(emptyList())
        override suspend fun getPlaces(categoryId: String?, searchQuery: String?): Result<List<Place>> =
            Result.success(places)
        override suspend fun getPlaceBySlug(slug: String): Result<Place?> =
            Result.success(places.find { it.slug == slug })
        override suspend fun getPlaceById(id: String): Result<Place?> =
            Result.success(places.find { it.id == id })
        override suspend fun getPlacesByIds(ids: List<String>): Result<List<Place>> =
            Result.success(places.filter { it.id in ids })
    }

    private class StubAuthRepository(initialUser: UserProfile?) : AuthRepository {
        private val _user = MutableStateFlow(initialUser)
        private val _auth = MutableStateFlow(initialUser != null)
        override val currentUser: StateFlow<UserProfile?> = _user.asStateFlow()
        override val isAuthenticated: StateFlow<Boolean> = _auth.asStateFlow()
        var signInCalls = 0

        override suspend fun signInWithGoogle(): Result<Unit> {
            signInCalls++
            _user.value = UserProfile("u-google", "Explorador Google", "demo@gmail.com")
            _auth.value = true
            return Result.success(Unit)
        }

        override suspend fun refreshProfile(): Result<UserProfile?> = Result.success(_user.value)

        override suspend fun signOut(): Result<Unit> {
            _user.value = null
            _auth.value = false
            return Result.success(Unit)
        }
    }

    private class StubVisitRepository(
        private val history: List<VisitRecord> = emptyList(),
        var nextResult: VisitResult = VisitResult.VisitUnauthenticated,
    ) : VisitRepository {
        var registerCalls = 0
        override suspend fun getMyVisitHistory(): Result<List<VisitRecord>> = Result.success(history)
        override suspend fun getVisitForPlace(placeId: String): Result<VisitRecord?> =
            Result.success(history.find { it.placeId == placeId })
        override suspend fun uploadVisitPhoto(photoBytes: ByteArray, fileExtension: String): Result<String> =
            Result.success("u1/test.$fileExtension")
        override suspend fun recordVisit(request: RecordVisitRequest): VisitResult = nextResult
        override suspend fun registerVisitWithPhoto(
            placeId: String,
            photoBytes: ByteArray,
            fileExtension: String,
            latitude: Double,
            longitude: Double,
        ): VisitResult {
            registerCalls++
            return nextResult
        }
    }
}
