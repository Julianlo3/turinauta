package com.unicauca.turinauta.data.remote

import com.unicauca.turinauta.core.model.CollectionProgress
import com.unicauca.turinauta.core.network.SupabaseClientProvider
import com.unicauca.turinauta.data.remote.dto.AvailableCollectionDto
import com.unicauca.turinauta.data.remote.dto.CollectionStampDto
import com.unicauca.turinauta.data.remote.dto.MyCollectionProgressDto
import com.unicauca.turinauta.data.remote.dto.MyCollectionStampDto
import com.unicauca.turinauta.data.remote.dto.StampDto
import com.unicauca.turinauta.data.remote.mapper.toDomain
import com.unicauca.turinauta.data.repository.CollectionRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

/**
 * Repositorio real de colecciones públicas (`available_collections`, `collection_stamps`, `stamps`)
 * y progreso privado autenticado (`my_collection_progress`, `my_collection_stamps`).
 */
class SupabaseCollectionRepository(
    private val clientProvider: SupabaseClientProvider,
) : CollectionRepository {

    override suspend fun getPublicCollections(): Result<List<CollectionProgress>> = runCatching {
        val client = clientProvider.requireClient()
        val collections = client.postgrest["available_collections"]
            .select(columns = COLLECTION_COLUMNS) {
                order(column = "sort_order", order = Order.ASCENDING)
            }
            .decodeList<AvailableCollectionDto>()

        if (collections.isEmpty()) return@runCatching emptyList()

        val links = client.postgrest["collection_stamps"]
            .select(columns = Columns.list("collection_id", "stamp_id", "sort_order")) {
                filter { isIn("collection_id", collections.map { it.id }) }
                order(column = "sort_order", order = Order.ASCENDING)
            }
            .decodeList<CollectionStampDto>()

        val stampIds = links.map { it.stampId }.distinct()
        val stampsById = if (stampIds.isEmpty()) {
            emptyMap()
        } else {
            client.postgrest["stamps"]
                .select(columns = Columns.ALL) {
                    filter { isIn("id", stampIds) }
                }
                .decodeList<StampDto>()
                .associateBy { it.id }
        }

        val linksByCollection = links.groupBy { it.collectionId }
        collections.map { col ->
            val colStamps = linksByCollection[col.id].orEmpty().mapNotNull { link ->
                stampsById[link.stampId]?.toDomain(
                    collectionId = col.id,
                    sortOrder = link.sortOrder,
                    isEarned = false,
                )
            }
            col.toDomain(stamps = colStamps)
        }
    }

    override suspend fun getMyCollectionProgress(): Result<List<CollectionProgress>> = runCatching {
        val client = clientProvider.requireClient()
        if (client.auth.currentUserOrNull() == null) {
            return getPublicCollections()
        }

        val progressDtos = client.postgrest["my_collection_progress"]
            .select(columns = Columns.ALL)
            .decodeList<MyCollectionProgressDto>()

        val stampDtos = client.postgrest["my_collection_stamps"]
            .select(columns = Columns.ALL)
            .decodeList<MyCollectionStampDto>()

        val stampsByCollection = stampDtos
            .map { it.toDomain() }
            .groupBy { it.collectionId.orEmpty() }

        progressDtos
            .map { dto ->
                val colId = dto.collectionId ?: dto.id.orEmpty()
                dto.toDomain(stamps = stampsByCollection[colId].orEmpty())
            }
            .sortedBy { it.sortOrder }
    }

    private companion object {
        val COLLECTION_COLUMNS = Columns.list(
            "id",
            "name",
            "slug",
            "description",
            "image_path",
            "sort_order",
        )
    }
}
