package com.unicauca.turinauta.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.VisitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VisitHistoryUiState(
    val visits: List<VisitRecord> = emptyList(),
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class VisitHistoryViewModel(
    private val visitRepository: VisitRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        VisitHistoryUiState(isAuthenticated = authRepository.isAuthenticated.value),
    )
    val uiState: StateFlow<VisitHistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.isAuthenticated.collect { authenticated ->
                if (!authenticated) {
                    _uiState.value = VisitHistoryUiState(
                        visits = emptyList(),
                        isAuthenticated = false,
                        isLoading = false,
                    )
                } else {
                    _uiState.update { it.copy(isAuthenticated = true) }
                    loadHistory()
                }
            }
        }
    }

    fun loadHistory() {
        if (!authRepository.isAuthenticated.value) {
            _uiState.value = VisitHistoryUiState(isAuthenticated = false, isLoading = false)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            visitRepository.getMyVisitHistory()
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(
                            visits = list,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No pudimos cargar tu historial de visitas. Verifica tu conexión e intenta nuevamente.",
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(
            visitRepository: VisitRepository,
            authRepository: AuthRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                VisitHistoryViewModel(
                    visitRepository = visitRepository,
                    authRepository = authRepository,
                ) as T
        }
    }
}
