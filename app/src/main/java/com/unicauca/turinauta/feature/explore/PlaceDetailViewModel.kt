package com.unicauca.turinauta.feature.explore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unicauca.turinauta.core.location.LocationProvider
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.Route
import com.unicauca.turinauta.core.model.UserProfile
import com.unicauca.turinauta.core.model.VisitPhoto
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.core.model.VisitResult
import com.unicauca.turinauta.core.navigation.Destinations
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.CollectionRepository
import com.unicauca.turinauta.core.model.RewardBadge
import com.unicauca.turinauta.core.model.ThematicCollection
import com.unicauca.turinauta.data.repository.RouteRepository
import com.unicauca.turinauta.data.repository.VisitRepository
import com.unicauca.turinauta.data.supabase.friendlyMessage
import com.unicauca.turinauta.feature.visits.RegisterVisitUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Pasos del registro de visita según el contrato: foto + ubicación + `record_visit`. */
sealed interface VisitFlow {
    data object Idle : VisitFlow
    /** Sin sesión: se invita a entrar con Google sin guardar nada todavía. */
    data object LoginRequired : VisitFlow
    /** Hoja con los pasos: elegir foto y confirmar ubicación. */
    data object Preparing : VisitFlow
    data object Locating : VisitFlow
    data object Saving : VisitFlow
    data class TooFar(val distanceMeters: Int) : VisitFlow
    /** [retryable] = false cuando el problema es de permiso o de la foto. */
    data class Failed(val title: String, val message: String, val retryable: Boolean) : VisitFlow
    data class Succeeded(val result: VisitResult.VisitCreated) : VisitFlow
}

data class PlaceDetailUiState(
    val isLoading: Boolean = true,
    val place: Place? = null,
    val route: Route? = null,
    val user: UserProfile? = null,
    val existingVisit: VisitRecord? = null,
    val visitFlow: VisitFlow = VisitFlow.Idle,
    val photo: VisitPhoto? = null,
    val photoError: String? = null,
    val loadError: String? = null,
    /** Sello que se obtiene al registrar la visita y la colección a la que pertenece. */
    val reward: RewardBadge? = null,
    val rewardCollection: ThematicCollection? = null,
    val message: String? = null,
) {
    val isBusy: Boolean get() = visitFlow == VisitFlow.Locating || visitFlow == VisitFlow.Saving
}

class PlaceDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val catalog: CatalogRepository,
    private val routes: RouteRepository,
    auth: AuthRepository,
    visits: VisitRepository,
    collections: CollectionRepository,
    private val location: LocationProvider,
    private val registerVisit: RegisterVisitUseCase,
) : ViewModel() {

    val placeId: String = checkNotNull(savedStateHandle[Destinations.ARG_PLACE_ID])
    private val routeId: String? = savedStateHandle[Destinations.ARG_ROUTE_ID]

    private val _uiState = MutableStateFlow(PlaceDetailUiState())
    val uiState: StateFlow<PlaceDetailUiState> = _uiState.asStateFlow()

    /** Se activa cuando la persona va a iniciar sesión desde esta ficha. */
    private var resumeAfterLogin = false

    init {
        viewModelScope.launch {
            collections.progress.collect { list ->
                val match = list.firstNotNullOfOrNull { p -> p.badges.firstOrNull { it.stamp.placeId == placeId }?.let { p.collection to it } }
                _uiState.update { it.copy(reward = match?.second, rewardCollection = match?.first) }
            }
        }
        loadPlace()
        viewModelScope.launch {
            combine(auth.currentUser, visits.visits) { user, list -> user to list.firstOrNull { it.placeId == placeId } }
                .collect { (user, visit) ->
                    val wasLoggedOut = _uiState.value.user == null
                    _uiState.update { it.copy(user = user, existingVisit = visit) }
                    // Al volver del inicio de sesión se retoma el registro, sin guardar aún.
                    if (wasLoggedOut && user != null && resumeAfterLogin) {
                        resumeAfterLogin = false
                        _uiState.update { it.copy(visitFlow = if (visit == null) VisitFlow.Preparing else VisitFlow.Idle) }
                    }
                }
        }
    }

    /** Carga la ficha; si falla la red se muestra el error con opción de reintentar. */
    fun loadPlace() {
        _uiState.update { it.copy(isLoading = true, loadError = null) }
        viewModelScope.launch {
            try {
                val place = catalog.getPlace(placeId)
                val route = routeId?.let { runCatching { routes.getRoute(it) }.getOrNull() }
                _uiState.update { it.copy(isLoading = false, place = place, route = route) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, loadError = e.friendlyMessage()) }
            }
        }
    }

    fun onRegisterVisitClick() {
        val state = _uiState.value
        // Si ya existe la visita se muestra el sello; no se sube otra foto.
        if (state.isBusy || state.existingVisit != null) return
        _uiState.update {
            it.copy(visitFlow = if (state.user == null) VisitFlow.LoginRequired else VisitFlow.Preparing, photoError = null)
        }
    }

    fun onGoToLogin() {
        resumeAfterLogin = true
        _uiState.update { it.copy(visitFlow = VisitFlow.Idle) }
    }

    fun onDismissLogin() = _uiState.update { it.copy(visitFlow = VisitFlow.Idle) }

    fun onPhotoSelected(photo: VisitPhoto?) {
        if (photo == null) return
        val error = when {
            photo.mimeType !in VisitPhoto.ALLOWED_TYPES -> "La foto debe ser JPG, PNG o WebP."
            photo.sizeBytes > VisitPhoto.MAX_BYTES -> "La foto pesa más de 5 MB. Elige otra o tómala de nuevo."
            else -> null
        }
        _uiState.update { if (error == null) it.copy(photo = photo, photoError = null) else it.copy(photoError = error) }
    }

    fun onCancelVisit() = _uiState.update {
        it.copy(visitFlow = VisitFlow.Idle, photo = null, photoError = null, message = "No se registró la visita.")
    }

    fun needsLocationPermission(): Boolean = !location.hasPermission()

    /** Se llama al pulsar «Confirmar visita» (tras pedir el permiso si hacía falta). */
    fun onConfirmVisit(locationGranted: Boolean = true) {
        val state = _uiState.value
        val place = state.place ?: return
        val photo = state.photo ?: return
        if (state.isBusy) return
        if (!locationGranted) {
            _uiState.update {
                it.copy(visitFlow = VisitFlow.Failed(
                    title = "Necesitamos tu ubicación",
                    message = "Para registrar la visita confirmamos que estás a menos de 200 m del lugar. Puedes activar el permiso en Ajustes de Android.",
                    retryable = false,
                ))
            }
            return
        }
        _uiState.update { it.copy(visitFlow = VisitFlow.Locating) }
        viewModelScope.launch {
            val here = location.currentLocation()
            if (here == null) {
                _uiState.update {
                    it.copy(visitFlow = VisitFlow.Failed(
                        title = "No pudimos obtener tu ubicación",
                        message = "Activa la ubicación del teléfono e inténtalo de nuevo.",
                        retryable = true,
                    ))
                }
                return@launch
            }
            _uiState.update { it.copy(visitFlow = VisitFlow.Saving) }
            val newFlow = when (val result = registerVisit(placeId, photo, here)) {
                null -> return@launch // ya había un registro en curso
                is VisitResult.VisitCreated -> VisitFlow.Succeeded(result)
                is VisitResult.VisitDuplicate -> VisitFlow.Idle
                VisitResult.VisitUnauthenticated -> VisitFlow.LoginRequired
                is VisitResult.VisitTooFar -> VisitFlow.TooFar(result.distanceMeters)
                is VisitResult.VisitInvalidPhoto -> VisitFlow.Failed("Revisa la foto", result.message, retryable = false)
                // Nunca se simula éxito: se ofrece reintentar (la RPC es idempotente).
                is VisitResult.VisitNetworkError -> VisitFlow.Failed("No se pudo guardar la visita", result.message, retryable = true)
            }
            _uiState.update {
                it.copy(
                    visitFlow = newFlow,
                    photo = if (newFlow is VisitFlow.Succeeded) null else it.photo,
                    message = if (newFlow == VisitFlow.Idle) "Este lugar ya estaba registrado en tu cuenta." else it.message,
                )
            }
        }
    }

    fun onRetry() = onConfirmVisit()

    /** Cierra un error y vuelve a la hoja con la foto elegida. */
    fun onBackToPreparing() = _uiState.update { it.copy(visitFlow = VisitFlow.Preparing) }

    fun onDismissSuccess() = _uiState.update { it.copy(visitFlow = VisitFlow.Idle) }

    fun showMessage(text: String) = _uiState.update { it.copy(message = text) }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }
}
