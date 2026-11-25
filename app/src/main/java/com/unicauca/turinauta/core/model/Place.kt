package com.unicauca.turinauta.core.model

/**
 * Ficha de un lugar del catálogo público.
 * Alineada con `GET /places?select=id,category_id,name,slug,summary,description,location_reference,latitude,longitude,opening_hours,visit_conditions,image_path,sort_order`
 * y las reglas editoriales R5 y R6.
 */
data class Place(
    val id: String,
    val categoryId: String,
    val categoryName: String,
    val name: String,
    val slug: String,
    val summary: String,
    val description: String,
    val locationReference: String,
    val latitude: Double,
    val longitude: Double,
    val openingHours: String? = null,
    val visitConditions: String? = null,
    val imagePath: String? = null,
    val sortOrder: Int = 0,
    val rewardBadgeId: String? = null,
    val illustrationKey: IllustrationKey = IllustrationKey.GENERIC,
    val editorialStatus: EditorialStatus = EditorialStatus.PUBLISHED,
    val isFeatured: Boolean = false,
) {
    /**
     * Regla R5: si no hay horario confirmado, mostrar exactamente «Horario por confirmar».
     */
    val displayOpeningHours: String
        get() = openingHours?.trim()?.takeIf { it.isNotEmpty() } ?: SCHEDULE_TO_CONFIRM

    companion object {
        const val SCHEDULE_TO_CONFIRM = "Horario por confirmar"
    }
}

/**
 * Criterios de ordenamiento disponibles en la pantalla Explorar.
 * [NEAREST] requiere permiso opcional de ubicación solo para ordenar en memoria.
 */
enum class PlaceSortOrder {
    FEATURED,
    NAME_ASC,
    NEAREST,
}
