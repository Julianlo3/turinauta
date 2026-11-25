package com.unicauca.turinauta.data.repository

import com.unicauca.turinauta.core.model.CollectionProgress

/**
 * Repositorio para colecciones temáticas públicas y progreso privado con JWT.
 */
interface CollectionRepository {
    suspend fun getPublicCollections(): Result<List<CollectionProgress>>

    suspend fun getMyCollectionProgress(): Result<List<CollectionProgress>>
}
