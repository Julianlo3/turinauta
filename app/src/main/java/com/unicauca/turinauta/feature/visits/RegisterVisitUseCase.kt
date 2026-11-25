package com.unicauca.turinauta.feature.visits

import com.unicauca.turinauta.core.model.GeoPoint
import com.unicauca.turinauta.core.model.VisitPhoto
import com.unicauca.turinauta.core.model.VisitResult
import com.unicauca.turinauta.data.repository.VisitRepository
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Registro de visita: sube la foto y llama a `record_visit` con la ubicación actual.
 * Ignora toques repetidos mientras hay una solicitud en curso (anti doble toque);
 * la unicidad por cuenta y lugar la garantiza el backend.
 */
class RegisterVisitUseCase(private val visits: VisitRepository) {
    private val inFlight = AtomicBoolean(false)

    /** Devuelve null si ya hay un registro en curso. */
    suspend operator fun invoke(placeId: String, photo: VisitPhoto, location: GeoPoint): VisitResult? {
        if (!inFlight.compareAndSet(false, true)) return null
        return try {
            visits.recordVisit(placeId, photo, location)
        } finally {
            inFlight.set(false)
        }
    }
}
