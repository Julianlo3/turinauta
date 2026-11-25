package com.unicauca.turinauta.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.location.LocationProvider
import com.unicauca.turinauta.core.model.UserProfile
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.core.network.FakeNetworkConfig
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CollectionRepository
import com.unicauca.turinauta.data.repository.VisitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserProfile? = null,
    val visits: List<VisitRecord> = emptyList(),
    val earnedStamps: Int = 0,
    val qa: FakeNetworkConfig.State = FakeNetworkConfig.State(),
)

/** Perfil privado, historial (`my_visit_history`) y panel de pruebas. */
class ProfileViewModel(
    private val auth: AuthRepository,
    visits: VisitRepository,
    collections: CollectionRepository,
    private val network: FakeNetworkConfig,
    private val location: LocationProvider,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> =
        combine(auth.currentUser, visits.visits, collections.progress, network.state) { user, list, progress, qa ->
            ProfileUiState(user = user, visits = list, earnedStamps = progress.sumOf { it.earnedStamps }, qa = qa)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun hasLocationPermission(): Boolean = location.hasPermission()

    /** Cierra sesión: el historial y la colección dejan de estar disponibles en memoria. */
    fun signOut() {
        viewModelScope.launch {
            auth.signOut()
            _message.update { "Cerraste sesión." }
        }
    }

    fun onMessageShown() = _message.update { null }

    // Modo pruebas (Simulación): solo existe mientras no hay backend conectado.
    fun setFailVisitSave(value: Boolean) = network.setFailVisitSave(value)
    fun setEmptyCatalog(value: Boolean) = network.setEmptyCatalog(value)
    fun setEmptyRoutes(value: Boolean) = network.setEmptyRoutes(value)
    fun setSimulateAtPlace(value: Boolean) = network.setSimulateAtPlace(value)
    fun setFailGoogleSignIn(value: Boolean) = network.setFailGoogleSignIn(value)
}
