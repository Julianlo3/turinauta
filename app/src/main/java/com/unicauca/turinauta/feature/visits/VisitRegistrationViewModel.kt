package com.unicauca.turinauta.feature.visits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.core.model.VisitResult
import com.unicauca.turinauta.data.remote.mapper.VisitPhotoValidator
import com.unicauca.turinauta.data.remote.mapper.haversineDistanceMeters
import com.unicauca.turinauta.data.repository.VisitRepository
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Límite temporal para pruebas. El límite visual/original de 200 m se conserva en VisitRecord.
internal const val TEMP_VISIT_TEST_RADIUS_METERS = 10_000

data class VisitRegistrationUiState(
    val selectedPhotoUri: String? = null,
    val photoBytes: ByteArray? = null,
    val photoExtension: String? = null,
    val photoSizeKb: Int? = null,
    val photoError: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distanceMeters: Int? = null,
    val locationAccuracyMeters: Int? = null,
    val isFetchingLocation: Boolean = false,
    val locationError: String? = null,
    val isSubmitting: Boolean = false,
    val visitResult: VisitResult? = null,
) {
    val hasValidPhoto: Boolean
        get() = photoBytes != null && photoExtension != null && photoError == null

    val hasLocation: Boolean
        get() = latitude != null && longitude != null

    val isWithinRange: Boolean
        get() = distanceMeters != null && distanceMeters <= VisitRecord.MAX_VISIT_DISTANCE_METERS

    val canSubmit: Boolean
        get() = !isSubmitting && hasValidPhoto && hasLocation &&
            distanceMeters != null && distanceMeters <= TEMP_VISIT_TEST_RADIUS_METERS

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VisitRegistrationUiState) return false
        return selectedPhotoUri == other.selectedPhotoUri &&
            photoBytes.contentEquals(other.photoBytes) &&
            photoExtension == other.photoExtension &&
            photoSizeKb == other.photoSizeKb &&
            photoError == other.photoError &&
            latitude == other.latitude &&
            longitude == other.longitude &&
            distanceMeters == other.distanceMeters &&
            locationAccuracyMeters == other.locationAccuracyMeters &&
            isFetchingLocation == other.isFetchingLocation &&
            locationError == other.locationError &&
            isSubmitting == other.isSubmitting &&
            visitResult == other.visitResult
    }

    override fun hashCode(): Int {
        var result = selectedPhotoUri?.hashCode() ?: 0
        result = 31 * result + (photoBytes?.contentHashCode() ?: 0)
        result = 31 * result + (photoExtension?.hashCode() ?: 0)
        result = 31 * result + (photoSizeKb ?: 0)
        result = 31 * result + (photoError?.hashCode() ?: 0)
        result = 31 * result + (latitude?.hashCode() ?: 0)
        result = 31 * result + (longitude?.hashCode() ?: 0)
        result = 31 * result + (distanceMeters ?: 0)
        result = 31 * result + (locationAccuracyMeters ?: 0)
        result = 31 * result + isFetchingLocation.hashCode()
        result = 31 * result + (locationError?.hashCode() ?: 0)
        result = 31 * result + isSubmitting.hashCode()
        result = 31 * result + (visitResult?.hashCode() ?: 0)
        return result
    }
}

class VisitRegistrationViewModel(
    private val registerVisitUseCase: RegisterVisitUseCase,
    private val onVisitCreated: () -> Unit = {},
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisitRegistrationUiState())
    val uiState: StateFlow<VisitRegistrationUiState> = _uiState.asStateFlow()

    fun onPhotoSelected(
        previewUri: String,
        bytes: ByteArray,
        extension: String,
    ) {
        when (val validation = VisitPhotoValidator.validate(bytes, extension)) {
            is VisitPhotoValidator.ValidationResult.Valid -> {
                _uiState.update {
                    it.copy(
                        selectedPhotoUri = previewUri,
                        photoBytes = bytes,
                        photoExtension = validation.normalizedExtension,
                        photoSizeKb = (bytes.size / 1024).coerceAtLeast(1),
                        photoError = null,
                    )
                }
            }
            is VisitPhotoValidator.ValidationResult.Invalid -> {
                _uiState.update {
                    it.copy(
                        selectedPhotoUri = null,
                        photoBytes = null,
                        photoExtension = null,
                        photoSizeKb = null,
                        photoError = validation.message,
                    )
                }
            }
        }
    }

    fun onPhotoError(message: String) {
        _uiState.update {
            it.copy(
                selectedPhotoUri = null,
                photoBytes = null,
                photoExtension = null,
                photoSizeKb = null,
                photoError = message,
            )
        }
    }

    fun onLocationLoading() {
        _uiState.update {
            it.copy(
                isFetchingLocation = true,
                latitude = null,
                longitude = null,
                distanceMeters = null,
                locationAccuracyMeters = null,
                locationError = null,
            )
        }
    }

    fun onLocationResolved(
        userLat: Double,
        userLon: Double,
        accuracyMeters: Float,
        placeLat: Double,
        placeLon: Double,
    ) {
        val distance = haversineDistanceMeters(userLat, userLon, placeLat, placeLon).roundToInt()
        _uiState.update {
            it.copy(
                latitude = userLat,
                longitude = userLon,
                distanceMeters = distance,
                locationAccuracyMeters = accuracyMeters.roundToInt(),
                isFetchingLocation = false,
                locationError = null,
            )
        }
    }

    fun onLocationError(message: String) {
        _uiState.update {
            it.copy(
                isFetchingLocation = false,
                latitude = null,
                longitude = null,
                distanceMeters = null,
                locationAccuracyMeters = null,
                locationError = message,
            )
        }
    }

    fun submitVisit(placeId: String) {
        val current = _uiState.value
        if (current.isSubmitting) return

        val bytes = current.photoBytes
        val ext = current.photoExtension
        val lat = current.latitude
        val lon = current.longitude

        if (bytes == null || ext == null) {
            _uiState.update { it.copy(photoError = "Selecciona o toma una foto del lugar para continuar.") }
            return
        }
        if (lat == null || lon == null) {
            _uiState.update { it.copy(locationError = "Verifica tu ubicación actual antes de confirmar la visita.") }
            return
        }
        if (!current.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, visitResult = null) }
            val result = registerVisitUseCase(
                placeId = placeId,
                photoBytes = bytes,
                fileExtension = ext,
                latitude = lat,
                longitude = lon,
            )
            if (result is VisitResult.VisitCreated) onVisitCreated()
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    visitResult = result,
                )
            }
        }
    }

    fun dismissResult() {
        _uiState.update { it.copy(visitResult = null) }
    }

    fun reset() {
        _uiState.value = VisitRegistrationUiState()
    }

    companion object {
        fun factory(
            visitRepository: VisitRepository,
            onVisitCreated: () -> Unit = {},
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    VisitRegistrationViewModel(RegisterVisitUseCase(visitRepository), onVisitCreated) as T
            }
    }
}
