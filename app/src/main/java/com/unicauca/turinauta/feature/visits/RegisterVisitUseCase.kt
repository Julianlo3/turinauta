package com.unicauca.turinauta.feature.visits

import com.unicauca.turinauta.core.model.VisitResult
import com.unicauca.turinauta.data.remote.mapper.VisitPhotoValidator
import com.unicauca.turinauta.data.repository.VisitRepository

/**
 * Caso de uso para registrar una visita validando primero los requisitos de foto
 * y delegando el flujo idempotente de 4 pasos a [VisitRepository].
 */
class RegisterVisitUseCase(
    private val visitRepository: VisitRepository,
) {
    suspend operator fun invoke(
        placeId: String,
        photoBytes: ByteArray,
        fileExtension: String,
        latitude: Double,
        longitude: Double,
    ): VisitResult {
        when (val validation = VisitPhotoValidator.validate(photoBytes, fileExtension)) {
            is VisitPhotoValidator.ValidationResult.Invalid -> {
                return VisitResult.VisitInvalidPhoto(validation.message)
            }
            is VisitPhotoValidator.ValidationResult.Valid -> {
                return visitRepository.registerVisitWithPhoto(
                    placeId = placeId,
                    photoBytes = photoBytes,
                    fileExtension = validation.normalizedExtension,
                    latitude = latitude,
                    longitude = longitude,
                )
            }
        }
    }
}
