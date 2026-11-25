package com.unicauca.turinauta.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.model.DataReport
import com.unicauca.turinauta.core.model.ReportField
import com.unicauca.turinauta.core.model.ReportSubmitResult
import com.unicauca.turinauta.data.remote.mapper.ContentReportValidator
import com.unicauca.turinauta.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportUiState(
    val selectedField: ReportField = ReportField.SCHEDULE,
    val details: String = "",
    val validationError: String? = null,
    val networkError: String? = null,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
) {
    val maxDetailsLength: Int
        get() = DataReport.MAX_DETAILS_LENGTH
}

class ReportViewModel(
    private val placeId: String,
    private val reportRepository: ReportRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun onFieldSelected(field: ReportField) {
        _uiState.update { it.copy(selectedField = field, networkError = null) }
    }

    fun onDetailsChange(value: String) {
        val clipped = if (value.length <= DataReport.MAX_DETAILS_LENGTH + 50) {
            value
        } else {
            value.take(DataReport.MAX_DETAILS_LENGTH + 50)
        }
        _uiState.update {
            it.copy(
                details = clipped,
                validationError = null,
                networkError = null,
            )
        }
    }

    fun submitReport() {
        val current = _uiState.value
        if (current.isSubmitting) return

        ContentReportValidator.validateDetails(current.details)?.let { validation ->
            _uiState.update { it.copy(validationError = validation.message) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    validationError = null,
                    networkError = null,
                )
            }

            when (
                val result = reportRepository.submitContentReport(
                    placeId = placeId,
                    field = current.selectedField,
                    details = current.details.trim(),
                )
            ) {
                ReportSubmitResult.Received -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isSubmitted = true,
                            networkError = null,
                        )
                    }
                }
                is ReportSubmitResult.ValidationError -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            validationError = result.message,
                        )
                    }
                }
                is ReportSubmitResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            networkError = result.message,
                        )
                    }
                }
            }
        }
    }

    fun reset() {
        _uiState.value = ReportUiState()
    }

    companion object {
        fun factory(
            placeId: String,
            reportRepository: ReportRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ReportViewModel(placeId = placeId, reportRepository = reportRepository) as T
        }
    }
}
