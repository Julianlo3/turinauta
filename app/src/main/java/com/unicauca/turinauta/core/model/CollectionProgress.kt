package com.unicauca.turinauta.core.model

/**
 * Colección temática con su progreso para la cuenta actual (o vista pública con earned = 0).
 * Alineada con `GET /available_collections` y `GET /my_collection_progress`.
 */
data class CollectionProgress(
    val id: String,
    val name: String,
    val slug: String,
    val description: String,
    val imagePath: String? = null,
    val sortOrder: Int = 0,
    val totalStamps: Int,
    val earnedStamps: Int,
    val progressPercent: Int = calculatePercent(earnedStamps, totalStamps),
    val stamps: List<RewardBadge> = emptyList(),
    val editorialStatus: EditorialStatus = EditorialStatus.PUBLISHED,
) {
    val isCompleted: Boolean
        get() = totalStamps > 0 && earnedStamps >= totalStamps

    companion object {
        fun calculatePercent(earned: Int, total: Int): Int =
            if (total <= 0) 0 else ((earned.coerceIn(0, total) * 100) / total)
    }
}
