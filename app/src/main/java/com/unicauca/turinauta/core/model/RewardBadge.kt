package com.unicauca.turinauta.core.model

/**
 * Sello / recompensa digital otorgada por visitar un lugar.
 * Alineado con `GET /stamps`, `GET /collection_stamps` y `GET /my_collection_stamps`.
 */
data class RewardBadge(
    val id: String,
    val title: String,
    val description: String,
    val placeId: String? = null,
    val collectionId: String? = null,
    val imagePath: String? = null,
    val illustrationKey: IllustrationKey = IllustrationKey.GENERIC,
    val sortOrder: Int = 0,
    val isEarned: Boolean = false,
    val earnedAtIso: String? = null,
    val earnedAtMillis: Long? = null,
)
