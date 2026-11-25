package com.unicauca.turinauta.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("slug") val slug: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
data class PlaceDto(
    @SerialName("id") val id: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("name") val name: String,
    @SerialName("slug") val slug: String,
    @SerialName("summary") val summary: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("location_reference") val locationReference: String? = null,
    @SerialName("latitude") val latitude: Double = 0.0,
    @SerialName("longitude") val longitude: Double = 0.0,
    @SerialName("opening_hours") val openingHours: String? = null,
    @SerialName("visit_conditions") val visitConditions: String? = null,
    @SerialName("image_path") val imagePath: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
data class AvailableRouteDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("slug") val slug: String,
    @SerialName("description") val description: String? = null,
    @SerialName("visit_conditions") val visitConditions: String? = null,
    @SerialName("image_path") val imagePath: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
data class RouteStopDto(
    @SerialName("route_id") val routeId: String,
    @SerialName("place_id") val placeId: String,
    @SerialName("suggested_order") val suggestedOrder: Int = 0,
)

@Serializable
data class AvailableCollectionDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("slug") val slug: String,
    @SerialName("description") val description: String? = null,
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
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("image_path") val imagePath: String? = null,
    @SerialName("place_id") val placeId: String? = null,
)

@Serializable
data class ProfileDto(
    @SerialName("id") val id: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class MyVisitHistoryDto(
    @SerialName("id") val id: String? = null,
    @SerialName("visit_id") val visitId: String? = null,
    @SerialName("place_id") val placeId: String,
    @SerialName("place_name") val placeName: String? = null,
    @SerialName("place_available") val placeAvailable: Boolean = true,
    @SerialName("visited_at") val visitedAt: String,
    @SerialName("photo_path") val photoPath: String? = null,
    @SerialName("stamp_id") val stampId: String? = null,
    @SerialName("stamp_title") val stampTitle: String? = null,
    @SerialName("stamp_image_path") val stampImagePath: String? = null,
)

@Serializable
data class MyCollectionProgressDto(
    @SerialName("id") val id: String? = null,
    @SerialName("collection_id") val collectionId: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("collection_name") val collectionName: String? = null,
    @SerialName("slug") val slug: String? = null,
    @SerialName("collection_slug") val collectionSlug: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("image_path") val imagePath: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("total_stamps") val totalStamps: Int = 0,
    @SerialName("earned_stamps") val earnedStamps: Int = 0,
    @SerialName("progress_percent") val progressPercent: Int? = null,
)

@Serializable
data class MyCollectionStampDto(
    @SerialName("collection_id") val collectionId: String,
    @SerialName("stamp_id") val stampId: String,
    @SerialName("title") val title: String? = null,
    @SerialName("stamp_title") val stampTitle: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("stamp_description") val stampDescription: String? = null,
    @SerialName("image_path") val imagePath: String? = null,
    @SerialName("stamp_image_path") val stampImagePath: String? = null,
    @SerialName("place_id") val placeId: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("earned") val earned: Boolean = false,
    @SerialName("visited_at") val visitedAt: String? = null,
)

@Serializable
data class RecordVisitParamsDto(
    @SerialName("p_place_id") val placeId: String,
    @SerialName("p_photo_path") val photoPath: String,
    @SerialName("p_latitude") val latitude: Double,
    @SerialName("p_longitude") val longitude: Double,
)

@Serializable
data class RecordVisitResponseDto(
    @SerialName("status") val status: String,
    @SerialName("visit_id") val visitId: String? = null,
    @SerialName("stamp_id") val stampId: String? = null,
    @SerialName("visited_at") val visitedAt: String? = null,
)

@Serializable
data class SubmitContentReportParamsDto(
    @SerialName("p_place_id") val placeId: String,
    @SerialName("p_field") val field: String,
    @SerialName("p_details") val details: String,
)

@Serializable
data class SubmitContentReportResponseDto(
    @SerialName("status") val status: String,
)
