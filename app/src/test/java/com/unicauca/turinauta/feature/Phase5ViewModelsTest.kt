package com.unicauca.turinauta.feature

import com.unicauca.turinauta.core.model.CollectionProgress
import com.unicauca.turinauta.core.model.Category
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.RecordVisitRequest
import com.unicauca.turinauta.core.model.RewardBadge
import com.unicauca.turinauta.core.model.UserProfile
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.core.model.VisitResult
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.CollectionRepository
import com.unicauca.turinauta.data.repository.VisitRepository
import com.unicauca.turinauta.feature.collection.CollectionViewModel
import com.unicauca.turinauta.feature.collection.StampDetailViewModel
import com.unicauca.turinauta.feature.profile.ProfileViewModel
import com.unicauca.turinauta.feature.profile.VisitHistoryViewModel
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class Phase5ViewModelsTest {

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
    fun collectionViewModelShowsPublicWhenSignedOutAndUpdatesOnAuthChange() = runTest(testDispatcher) {
        val authRepo = MutableAuthRepo(initialUser = null)
        val collectionRepo = StubCollectionRepo()

        val vm = CollectionViewModel(collectionRepo, authRepo)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isAuthenticated)
        assertEquals(0, vm.uiState.value.earnedStamps)
        assertEquals(2, vm.uiState.value.totalStamps)

        // Al iniciar sesión con Google, carga el progreso privado
        authRepo.signInWithGoogle()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isAuthenticated)
        assertEquals(1, vm.uiState.value.earnedStamps)
        assertEquals(50, vm.uiState.value.overallProgressPercent)

        // Al cerrar sesión (HU-25), vuelve al estado público sin sellos obtenidos
        authRepo.signOut()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isAuthenticated)
        assertEquals(0, vm.uiState.value.earnedStamps)
    }

    @Test
    fun profileViewModelLoadsStatsAndClearsPrivateStateOnSignOut() = runTest(testDispatcher) {
        val authRepo = MutableAuthRepo(
            initialUser = UserProfile("u1", "Julián", "julian@unicauca.edu.co"),
        )
        val visitRepo = StubVisitRepo(
            listOf(
                VisitRecord(
                    id = "v1",
                    placeId = "p1",
                    placeName = "Cocina del Barrio",
                    visitedAtIso = "2026-11-24T19:00:00Z",
                    stampTitle = "Sabores de Fogón",
                ),
            ),
        )
        val collectionRepo = StubCollectionRepo()

        val vm = ProfileViewModel(authRepo, visitRepo, collectionRepo)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isAuthenticated)
        assertEquals(1, vm.uiState.value.visitedPlacesCount)
        assertEquals(1, vm.uiState.value.earnedStampsCount)

        // HU-25: al cerrar sesión limpia perfil, visitas y sellos inmediatamente
        vm.signOut()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isAuthenticated)
        assertNull(vm.uiState.value.user)
        assertEquals(0, vm.uiState.value.visitedPlacesCount)
        assertEquals(0, vm.uiState.value.earnedStampsCount)
    }

    @Test
    fun visitHistoryViewModelPreservesWithdrawnPlaceAndStamp() = runTest(testDispatcher) {
        val authRepo = MutableAuthRepo(
            initialUser = UserProfile("u1", "Julián", "julian@unicauca.edu.co"),
        )
        val visitRepo = StubVisitRepo(
            listOf(
                VisitRecord(
                    id = "v-retirado",
                    placeId = "p99",
                    placeName = "",
                    placeAvailable = false,
                    visitedAtIso = "2026-11-20T14:00:00Z",
                    stampTitle = "Hilos y Telar",
                ),
            ),
        )

        val vm = VisitHistoryViewModel(visitRepo, authRepo)
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.visits.size)
        val item = vm.uiState.value.visits.first()
        assertFalse(item.placeAvailable)
        assertEquals(VisitRecord.UNAVAILABLE_PLACE_NAME, item.displayPlaceName)
        assertEquals("Hilos y Telar", item.stampTitle)
    }

    @Test
    fun stampDetailOnlyOpensEarnedBadgeAndClearsItOnSignOut() = runTest(testDispatcher) {
        val authRepo = MutableAuthRepo(
            initialUser = UserProfile("u1", "Julián", "julian@unicauca.edu.co"),
        )
        val collectionRepo = StubCollectionRepo()
        val catalogRepo = StubCatalogRepo()
        val earned = StampDetailViewModel("s1", collectionRepo, catalogRepo, authRepo)
        val pending = StampDetailViewModel("s2", collectionRepo, catalogRepo, authRepo)
        advanceUntilIdle()

        assertEquals("Sello 1", earned.uiState.value.badge?.title)
        assertEquals("Sabores", earned.uiState.value.collectionName)
        assertNull(pending.uiState.value.badge)
        assertTrue(pending.uiState.value.errorMessage?.contains("todavía") == true)

        authRepo.signOut()
        advanceUntilIdle()
        assertNull(earned.uiState.value.badge)
        assertNull(pending.uiState.value.badge)
    }

    private class MutableAuthRepo(initialUser: UserProfile?) : AuthRepository {
        private val _user = MutableStateFlow(initialUser)
        private val _auth = MutableStateFlow(initialUser != null)
        override val currentUser: StateFlow<UserProfile?> = _user.asStateFlow()
        override val isAuthenticated: StateFlow<Boolean> = _auth.asStateFlow()

        override suspend fun signInWithGoogle(): Result<Unit> {
            _user.value = UserProfile("u1", "Julián", "julian@unicauca.edu.co")
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

    private class StubCollectionRepo : CollectionRepository {
        override suspend fun getPublicCollections(): Result<List<CollectionProgress>> = Result.success(
            listOf(
                CollectionProgress(
                    id = "c1",
                    name = "Sabores",
                    slug = "sabores",
                    description = "Desc",
                    totalStamps = 2,
                    earnedStamps = 0,
                    stamps = listOf(
                        RewardBadge("s1", "Sello 1", "Desc 1", isEarned = false),
                        RewardBadge("s2", "Sello 2", "Desc 2", isEarned = false),
                    ),
                ),
            ),
        )

        override suspend fun getMyCollectionProgress(): Result<List<CollectionProgress>> = Result.success(
            listOf(
                CollectionProgress(
                    id = "c1",
                    name = "Sabores",
                    slug = "sabores",
                    description = "Desc",
                    totalStamps = 2,
                    earnedStamps = 1,
                    stamps = listOf(
                        RewardBadge("s1", "Sello 1", "Desc 1", isEarned = true),
                        RewardBadge("s2", "Sello 2", "Desc 2", isEarned = false),
                    ),
                ),
            ),
        )
    }

    private class StubVisitRepo(private val history: List<VisitRecord>) : VisitRepository {
        override suspend fun getMyVisitHistory(): Result<List<VisitRecord>> = Result.success(history)
        override suspend fun getVisitForPlace(placeId: String): Result<VisitRecord?> =
            Result.success(history.find { it.placeId == placeId })
        override suspend fun uploadVisitPhoto(photoBytes: ByteArray, fileExtension: String): Result<String> =
            Result.success("u1/a.jpg")
        override suspend fun recordVisit(request: RecordVisitRequest): VisitResult =
            VisitResult.VisitUnauthenticated
        override suspend fun registerVisitWithPhoto(
            placeId: String,
            photoBytes: ByteArray,
            fileExtension: String,
            latitude: Double,
            longitude: Double,
        ): VisitResult = VisitResult.VisitUnauthenticated
    }

    private class StubCatalogRepo : CatalogRepository {
        override suspend fun getCategories(): Result<List<Category>> = Result.success(emptyList())
        override suspend fun getPlaces(categoryId: String?, searchQuery: String?): Result<List<Place>> =
            Result.success(emptyList())
        override suspend fun getPlaceBySlug(slug: String): Result<Place?> = Result.success(null)
        override suspend fun getPlaceById(id: String): Result<Place?> = Result.success(null)
        override suspend fun getPlacesByIds(ids: List<String>): Result<List<Place>> = Result.success(emptyList())
    }
}
