package com.unicauca.turinauta.data.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Filas de la Data API tal como las devuelve Supabase (nombres de columna en snake_case).
 * Comprobadas contra el proyecto real el 24/11/2026.
 */

@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val slug: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
data class PlaceDto(
    val id: String,
    @SerialName("category_id") val categoryId: String,
    val name: String,
    val slug: String,
    val summary: String = "",
    val description: String = "",
    @SerialName("location_reference") val locationReference: String = "",
    val latitude: Double,
    val longitude: Double,
    @SerialName("opening_hours") val openingHours: String? = null,
    @SerialName("visit_conditions") val visitConditions: String? = null,
    @SerialName("image_path") val imagePath: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
data class RouteDto(
    val id: String,
    val name: String,
    val slug: String,
    val description: String = "",
    @SerialName("visit_conditions") val visitConditions: String? = null,
    @SerialName("image_path") val imagePath: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
data class RoutePlaceDto(
    @SerialName("route_id") val routeId: String,
    @SerialName("place_id") val placeId: String,
    @SerialName("suggested_order") val suggestedOrder: Int? = null,
)

@Serializable
data class CollectionDto(
    val id: String,
    val name: String,
    val slug: String,
    val description: String = "",
    @SerialName("image_path") val imagePath: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
data class CollectionStampDto(
    @SerialName("collection_id") val collectionId: String,
    @SerialName("stamp_id") val stampId: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
data class StampDto(
    val id: String,
    @SerialName("place_id") val placeId: String,
    val title: String,
    val description: String = "",
    @SerialName("image_path") val imagePath: String? = null,
)

@Serializable
data class ProfileDto(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
)

/** Respuesta de `rpc/record_visit`: `created` o `already_recorded`. */
@Serializable
data class RecordVisitResponse(
    val status: String,
    @SerialName("visit_id") val visitId: String? = null,
    @SerialName("stamp_id") val stampId: String? = null,
)
