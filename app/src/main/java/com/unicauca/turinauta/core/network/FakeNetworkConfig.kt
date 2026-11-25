package com.unicauca.turinauta.core.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Simulación de red mientras no exista conexión a Supabase (Modo pruebas QA del Perfil).
 * Se elimina cuando los repositorios Fake se reemplacen por los reales.
 */
class FakeNetworkConfig {

    data class State(
        val latencyMs: Long = 700,
        val failVisitSave: Boolean = false,
        val emptyCatalog: Boolean = false,
        val emptyRoutes: Boolean = false,
        /** Usa las coordenadas del lugar como ubicación actual (para probar en emulador). */
        val simulateAtPlace: Boolean = true,
        val failGoogleSignIn: Boolean = false,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun setFailVisitSave(value: Boolean) = _state.update { it.copy(failVisitSave = value) }
    fun setEmptyCatalog(value: Boolean) = _state.update { it.copy(emptyCatalog = value) }
    fun setEmptyRoutes(value: Boolean) = _state.update { it.copy(emptyRoutes = value) }
    fun setSimulateAtPlace(value: Boolean) = _state.update { it.copy(simulateAtPlace = value) }
    fun setFailGoogleSignIn(value: Boolean) = _state.update { it.copy(failGoogleSignIn = value) }

    suspend fun simulateLatency(factor: Float = 1f) = delay((_state.value.latencyMs * factor).toLong())
}
