package com.unicauca.turinauta.feature.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.model.CollectionProgress
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CollectionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CollectionUiState(
    val isLoading: Boolean = true,
    val isSignedIn: Boolean = false,
    val collections: List<CollectionProgress> = emptyList(),
) {
    val earned: Int get() = collections.sumOf { it.earnedStamps }
    val pending: Int get() = collections.sumOf { it.totalStamps } - earned
}

/** `my_collection_progress` y `my_collection_stamps` (HU-20 a HU-22). */
class CollectionViewModel(auth: AuthRepository, collections: CollectionRepository) : ViewModel() {
    val uiState: StateFlow<CollectionUiState> =
        combine(auth.currentUser, collections.progress) { user, progress ->
            CollectionUiState(isLoading = false, isSignedIn = user != null, collections = if (user == null) emptyList() else progress)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionUiState())
}
