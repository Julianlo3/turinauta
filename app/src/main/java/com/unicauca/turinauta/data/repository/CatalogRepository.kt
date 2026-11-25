package com.unicauca.turinauta.data.repository

import com.unicauca.turinauta.core.model.Category
import com.unicauca.turinauta.core.model.Place

/**
 * Repositorio de lectura pública del catálogo (sin cuenta requerida — Regla R1).
 */
interface CatalogRepository {
    suspend fun getCategories(): Result<List<Category>>

    suspend fun getPlaces(
        categoryId: String? = null,
        searchQuery: String? = null,
    ): Result<List<Place>>

    suspend fun getPlaceBySlug(slug: String): Result<Place?>

    suspend fun getPlaceById(id: String): Result<Place?>

    suspend fun getPlacesByIds(ids: List<String>): Result<List<Place>>
}
