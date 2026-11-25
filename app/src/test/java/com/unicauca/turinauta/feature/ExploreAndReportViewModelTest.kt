package com.unicauca.turinauta.feature

import com.unicauca.turinauta.core.model.Category
import com.unicauca.turinauta.core.model.EditorialStatus
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.PlaceSortOrder
import com.unicauca.turinauta.core.model.ReportField
import com.unicauca.turinauta.core.model.ReportSubmitResult
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.ReportRepository
import com.unicauca.turinauta.feature.explore.ExploreViewModel
import com.unicauca.turinauta.feature.reports.ReportViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreAndReportViewModelTest {

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
    fun exploreViewModelFiltersByCategorySearchAndSortsCorrectly() = runTest(testDispatcher) {
        val repo = TestCatalogRepository(
            categories = listOf(
                Category("c1", "Gastronomía", "gastronomia", 1),
                Category("c2", "Artesanías", "artesanias", 2),
            ),
            places = listOf(
                Place(
                    id = "p1",
                    categoryId = "c1",
                    categoryName = "Gastronomía",
                    name = "Cocina del Barrio",
                    slug = "cocina",
                    summary = "Sabor local",
                    description = "Desc",
                    locationReference = "Centro",
                    latitude = 2.4419,
                    longitude = -76.6063,
                    sortOrder = 2,
                ),
                Place(
                    id = "p2",
                    categoryId = "c2",
                    categoryName = "Artesanías",
                    name = "Amasijos y Telar",
                    slug = "telar",
                    summary = "Tejido tradicional",
                    description = "Desc",
                    locationReference = "Norte",
                    latitude = 2.4500,
                    longitude = -76.6000,
                    sortOrder = 1,
                ),
                Place(
                    id = "p3",
                    categoryId = "c1",
                    categoryName = "Gastronomía",
                    name = "Borrador oculto",
                    slug = "draft",
                    summary = "No visible",
                    description = "Desc",
                    locationReference = "N/A",
                    latitude = 2.44,
                    longitude = -76.60,
                    editorialStatus = EditorialStatus.DRAFT,
                ),
            ),
        )

        val vm = ExploreViewModel(repo)
        advanceUntilIdle()

        // R6: oculta DRAFT y ordena por FEATURED (sortOrder)
        assertEquals(listOf("p2", "p1"), vm.uiState.value.places.map { it.id })

        // Ordenar A-Z
        vm.onSortOrderSelected(PlaceSortOrder.NAME_ASC)
        assertEquals(listOf("p2", "p1"), vm.uiState.value.places.map { it.id })

        // Ordenar por más cercanos (cerca de p1)
        vm.onUserLocationResolved(2.4419, -76.6063)
        assertEquals(listOf("p1", "p2"), vm.uiState.value.places.map { it.id })

        // Filtrar por categoría c1
        vm.onCategorySelected("c1")
        assertEquals(listOf("p1"), vm.uiState.value.places.map { it.id })

        // Buscar sin coincidencias y limpiar filtros
        vm.onSearchQueryChange("inexistente")
        assertTrue(vm.uiState.value.places.isEmpty())
        vm.clearFilters()
        assertEquals(2, vm.uiState.value.places.size)
        assertNull(vm.uiState.value.selectedCategoryId)
        assertEquals("", vm.uiState.value.searchQuery)
    }

    @Test
    fun reportViewModelValidatesDetailsAndSubmitsReport() = runTest(testDispatcher) {
        val repo = TestReportRepository()
        val vm = ReportViewModel(placeId = "p1", reportRepository = repo)

        // Rechaza texto vacío / espacios
        vm.onDetailsChange("    ")
        vm.submitReport()
        assertNotNull(vm.uiState.value.validationError)
        assertFalse(vm.uiState.value.isSubmitted)

        // Envío válido con campo LOCATION
        vm.onFieldSelected(ReportField.LOCATION)
        vm.onDetailsChange("La dirección correcta es calle 4 # 5-12")
        vm.submitReport()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isSubmitted)
        assertNull(vm.uiState.value.validationError)
        assertNull(vm.uiState.value.networkError)
        assertEquals(1, repo.submittedCount)
    }

    private class TestCatalogRepository(
        private val categories: List<Category>,
        private val places: List<Place>,
    ) : CatalogRepository {
        override suspend fun getCategories(): Result<List<Category>> = Result.success(categories)
        override suspend fun getPlaces(categoryId: String?, searchQuery: String?): Result<List<Place>> =
            Result.success(places)
        override suspend fun getPlaceBySlug(slug: String): Result<Place?> =
            Result.success(places.find { it.slug == slug })
        override suspend fun getPlaceById(id: String): Result<Place?> =
            Result.success(places.find { it.id == id })
        override suspend fun getPlacesByIds(ids: List<String>): Result<List<Place>> =
            Result.success(places.filter { it.id in ids })
    }

    private class TestReportRepository : ReportRepository {
        var submittedCount = 0
        override suspend fun submitContentReport(
            placeId: String,
            field: ReportField,
            details: String,
        ): ReportSubmitResult {
            submittedCount++
            return ReportSubmitResult.Received
        }
    }
}
