package com.unicauca.turinauta.feature.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.model.CollectionProgress
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.RewardBadge
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.CollectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StampDetailUiState(
    val badge: RewardBadge? = null,
    val collection: CollectionProgress? = null,
    val place: Place? = null,
    val isLoading: Boolean = true,
    val isPlaceLoading: Boolean = false,
    val placeError: Boolean = false,
    val errorMessage: String? = null,
)

class StampDetailViewModel(
    private val stampId: String,
    private val collectionRepository: CollectionRepository,
    private val catalogRepository: CatalogRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(StampDetailUiState())
    val uiState = mutableState.asStateFlow()
    private var requestVersion = 0L
    private var selectedStampId = stampId

    init {
        viewModelScope.launch {
            authRepository.isAuthenticated.collectLatest { authenticated ->
                val version = ++requestVersion
                if (authenticated) loadStamp(version) else {
                    selectedStampId = stampId
                    mutableState.value = StampDetailUiState(
                        isLoading = false,
                        errorMessage = "Inicia sesión para ver tus sellos obtenidos.",
                    )
                }
            }
        }
    }

    fun refresh() {
        if (authRepository.isAuthenticated.value) {
            val version = ++requestVersion
            viewModelScope.launch { loadStamp(version) }
        }
    }

    fun selectStamp(id: String) {
        val collection = mutableState.value.collection ?: return
        val badge = collection.stamps.firstOrNull { it.id == id && it.isEarned } ?: return
        if (badge.id == mutableState.value.badge?.id) return
        selectedStampId = id
        val version = ++requestVersion
        showBadge(collection, badge)
        viewModelScope.launch { loadPlace(badge, version) }
    }

    private fun isCurrentRequest(version: Long): Boolean =
        version == requestVersion && authRepository.isAuthenticated.value

    private suspend fun loadStamp(version: Long) {
        mutableState.value = StampDetailUiState()
        val collectionsResult = collectionRepository.getMyCollectionProgress()
        if (!isCurrentRequest(version)) return
        val collections = collectionsResult.getOrElse {
            mutableState.value = StampDetailUiState(
                isLoading = false,
                errorMessage = "No pudimos cargar este sello. Verifica tu conexión e intenta de nuevo.",
            )
            return
        }
        val match = collections.firstNotNullOfOrNull { collection ->
            collection.stamps.firstOrNull { it.id == selectedStampId && it.isEarned }?.let { collection to it }
        }
        if (match == null) {
            mutableState.value = StampDetailUiState(
                isLoading = false,
                errorMessage = "Este sello todavía no está disponible en tu colección.",
            )
            return
        }
        val (collection, badge) = match
        showBadge(collection, badge)
        loadPlace(badge, version)
    }

    private fun showBadge(collection: CollectionProgress, badge: RewardBadge) {
        mutableState.value = StampDetailUiState(
            badge = badge,
            collection = collection,
            isLoading = false,
            isPlaceLoading = badge.placeId != null,
        )
    }

    private suspend fun loadPlace(badge: RewardBadge, version: Long) {
        val placeId = badge.placeId ?: return
        val result = catalogRepository.getPlaceById(placeId)
        if (!isCurrentRequest(version)) return
        mutableState.update {
            it.copy(place = result.getOrNull(), isPlaceLoading = false, placeError = result.isFailure)
        }
    }

    companion object {
        fun factory(
            stampId: String,
            collectionRepository: CollectionRepository,
            catalogRepository: CatalogRepository,
            authRepository: AuthRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = StampDetailViewModel(
                stampId, collectionRepository, catalogRepository, authRepository,
            ) as T
        }
    }
}
