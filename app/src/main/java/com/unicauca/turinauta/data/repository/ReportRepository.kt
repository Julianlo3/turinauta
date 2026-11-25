package com.unicauca.turinauta.data.repository

import com.unicauca.turinauta.core.model.ReportField
import com.unicauca.turinauta.core.model.ReportSubmitResult

/**
 * Repositorio para reportes internos de datos incorrectos (HU-24).
 * Funciona sin cuenta autenticada contra `POST /rest/v1/rpc/submit_content_report`.
 */
interface ReportRepository {
    suspend fun submitContentReport(
        placeId: String,
        field: ReportField,
        details: String,
    ): ReportSubmitResult
}
