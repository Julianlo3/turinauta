package com.unicauca.turinauta.data.repository

import com.unicauca.turinauta.core.model.RecordVisitRequest
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.core.model.VisitResult

/**
 * Repositorio privado para historial y registro de visitas (JWT requerido).
 * Respeta el flujo idempotente de 4 pasos de `CONTRATO_API_MVP.md` y la regla R4.
 */
interface VisitRepository {
    suspend fun getMyVisitHistory(): Result<List<VisitRecord>>

    suspend fun getVisitForPlace(placeId: String): Result<VisitRecord?>

    suspend fun uploadVisitPhoto(
        photoBytes: ByteArray,
        fileExtension: String,
    ): Result<String>

    suspend fun recordVisit(request: RecordVisitRequest): VisitResult

    /**
     * Orquesta el flujo completo:
     * 1) Verifica en `my_visit_history` si el lugar ya fue visitado (evita subir otra foto).
     * 2) Valida formato JPG/PNG/WebP y tamaño ≤ 5 MB.
     * 3) Sube a `visit-photos/<auth.uid()>/<uuid>.<ext>` con `upsert = false`.
     * 4) Llama a `POST /rest/v1/rpc/record_visit`.
     */
    suspend fun registerVisitWithPhoto(
        placeId: String,
        photoBytes: ByteArray,
        fileExtension: String,
        latitude: Double,
        longitude: Double,
    ): VisitResult
}
