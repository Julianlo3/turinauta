package com.unicauca.turinauta.data.remote

import com.unicauca.turinauta.core.model.ReportField
import com.unicauca.turinauta.core.model.ReportSubmitResult
import com.unicauca.turinauta.core.network.SupabaseClientProvider
import com.unicauca.turinauta.data.remote.dto.SubmitContentReportResponseDto
import com.unicauca.turinauta.data.remote.mapper.ContentReportValidator
import com.unicauca.turinauta.data.repository.ReportRepository
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Repositorio real para reportes internos de datos incorrectos (HU-24).
 * Invoca `POST /rest/v1/rpc/submit_content_report` (funciona sin sesión activa).
 */
class SupabaseReportRepository(
    private val clientProvider: SupabaseClientProvider,
) : ReportRepository {

    override suspend fun submitContentReport(
        placeId: String,
        field: ReportField,
        details: String,
    ): ReportSubmitResult {
        ContentReportValidator.validateDetails(details)?.let { return it }

        return try {
            val client = clientProvider.requireClient()
            val response = client.postgrest.rpc(
                function = "submit_content_report",
                parameters = buildJsonObject {
                    put("p_place_id", placeId)
                    put("p_field", field.apiValue)
                    put("p_details", details.trim())
                },
            ).decodeAs<SubmitContentReportResponseDto>()

            if (response.status.equals("received", ignoreCase = true)) {
                ReportSubmitResult.Received
            } else {
                ReportSubmitResult.NetworkError(
                    "No se pudo confirmar el envío del reporte (${response.status}).",
                )
            }
        } catch (t: Throwable) {
            ReportSubmitResult.NetworkError(
                "No pudimos enviar el reporte en este momento. Verifica tu conexión e intenta de nuevo.",
            )
        }
    }
}
