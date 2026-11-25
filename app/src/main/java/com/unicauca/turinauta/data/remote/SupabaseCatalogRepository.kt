package com.unicauca.turinauta.data.remote

import com.unicauca.turinauta.core.model.Category
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.network.SupabaseClientProvider
import com.unicauca.turinauta.data.remote.dto.CategoryDto
import com.unicauca.turinauta.data.remote.dto.PlaceDto
import com.unicauca.turinauta.data.remote.mapper.toDomain
import com.unicauca.turinauta.data.repository.CatalogRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class SupabaseCatalogRepository(
    private val clientProvider: SupabaseClientProvider,
) : CatalogRepository {

    override suspend fun getCategories(): Result<List<Category>> = runCatching {
        val client = clientProvider.requireClient()
        client.postgrest["categories"]
            .select(columns = Columns.list("id", "name", "slug", "sort_order")) {
                order(column = "sort_order", order = Order.ASCENDING)
            }
            .decodeList<CategoryDto>()
            .map { it.toDomain() }
    }

    override suspend fun getPlaces(
        categoryId: String?,
        searchQuery: String?,
    ): Result<List<Place>> = runCatching {
        val client = clientProvider.requireClient()
        val categoriesById = getCategories().getOrElse { emptyList() }.associateBy { it.id }
        val cleanQuery = searchQuery?.trim()?.takeIf { it.isNotEmpty() }

        client.postgrest["places"]
            .select(columns = PLACE_COLUMNS) {
                filter {
                    if (!categoryId.isNullOrBlank()) {
                        eq("category_id", categoryId)
                    }
                    if (cleanQuery != null) {
                        ilike("name", "%$cleanQuery%")
                    }
                }
                order(column = "sort_order", order = Order.ASCENDING)
            }
            .decodeList<PlaceDto>()
            .map { it.toDomain(categoriesById) }
    }

    override suspend fun getPlaceBySlug(slug: String): Result<Place?> = runCatching {
        val client = clientProvider.requireClient()
        val categoriesById = getCategories().getOrElse { emptyList() }.associateBy { it.id }
        client.postgrest["places"]
            .select(columns = PLACE_COLUMNS) {
                filter { eq("slug", slug) }
                limit(1)
            }
            .decodeList<PlaceDto>()
            .firstOrNull()
            ?.toDomain(categoriesById)
    }

    override suspend fun getPlaceById(id: String): Result<Place?> = runCatching {
        val client = clientProvider.requireClient()
        val categoriesById = getCategories().getOrElse { emptyList() }.associateBy { it.id }
        client.postgrest["places"]
            .select(columns = PLACE_COLUMNS) {
                filter { eq("id", id) }
                limit(1)
            }
            .decodeList<PlaceDto>()
            .firstOrNull()
            ?.toDomain(categoriesById)
    }

    override suspend fun getPlacesByIds(ids: List<String>): Result<List<Place>> = runCatching {
        if (ids.isEmpty()) return@runCatching emptyList()
        val client = clientProvider.requireClient()
        val categoriesById = getCategories().getOrElse { emptyList() }.associateBy { it.id }
        client.postgrest["places"]
            .select(columns = PLACE_COLUMNS) {
                filter { isIn("id", ids) }
            }
            .decodeList<PlaceDto>()
            .map { it.toDomain(categoriesById) }
    }

    private companion object {
        val PLACE_COLUMNS = Columns.list(
            "id",
            "category_id",
            "name",
            "slug",
            "summary",
            "description",
            "location_reference",
            "latitude",
            "longitude",
            "opening_hours",
            "visit_conditions",
            "image_path",
            "sort_order",
        )
    }
}
