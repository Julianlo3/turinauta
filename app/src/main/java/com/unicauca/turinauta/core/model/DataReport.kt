package com.unicauca.turinauta.core.model

/**
 * Campos admitidos por `POST /rest/v1/rpc/submit_content_report` (`p_field`).
 */
enum class ReportField(
    val apiValue: String,
    val labelEs: String,
) {
    SCHEDULE("schedule", "Horario"),
    LOCATION("location", "Ubicación"),
    DESCRIPTION("description", "Descripción"),
    OTHER("other", "Otro dato"),
}

/**
 * Reporte interno privado de dato incorrecto (HU-24).
 * Funciona sin cuenta autenticada contra `POST /rest/v1/rpc/submit_content_report`.
 */
data class DataReport(
    val id: String,
    val placeId: String,
    val field: ReportField,
    val details: String,
    val createdAtMillis: Long,
) {
    companion object {
        const val MAX_DETAILS_LENGTH = 1500
    }
}

/**
 * Resultado del envío del reporte interno de contenido.
 */
sealed interface ReportSubmitResult {
    data object Received : ReportSubmitResult
    data class ValidationError(val message: String) : ReportSubmitResult
    data class NetworkError(val message: String) : ReportSubmitResult
}
