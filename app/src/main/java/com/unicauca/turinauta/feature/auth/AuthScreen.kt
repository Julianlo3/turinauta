package com.unicauca.turinauta.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unicauca.turinauta.core.designsystem.BackTopBar
import com.unicauca.turinauta.core.designsystem.NoteBox
import com.unicauca.turinauta.core.designsystem.PillShape
import com.unicauca.turinauta.core.designsystem.ThumbnailShape
import com.unicauca.turinauta.core.designsystem.TurinautaLogo
import com.unicauca.turinauta.core.model.AuthResult
import com.unicauca.turinauta.core.navigation.Destinations
import com.unicauca.turinauta.core.navigation.TurinautaViewModelFactory
import com.unicauca.turinauta.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val placeName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val signedIn: Boolean = false,
)

/** Inicio de sesión con Google a través de Supabase Auth (contrato API). */
class AuthViewModel(savedStateHandle: SavedStateHandle, private val auth: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(placeName = savedStateHandle.get<String>(Destinations.ARG_PLACE_NAME)?.takeIf { it.isNotBlank() }),
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signInWithGoogle() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = auth.signInWithGoogle()) {
                is AuthResult.Success -> _uiState.update { it.copy(isLoading = false, signedIn = true) }
                AuthResult.Cancelled -> _uiState.update { it.copy(isLoading = false) }
                is AuthResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}

@Composable
fun AuthScreen(
    onBack: () -> Unit,
    onSignedIn: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = TurinautaViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.signedIn) { if (state.signedIn) onSignedIn() }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        BackTopBar("Iniciar sesión", onBack = onBack)
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            TurinautaLogo(size = 72.dp)
            Spacer(Modifier.height(20.dp))
            Text("Te damos la bienvenida a Turinauta", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal))
            Spacer(Modifier.height(8.dp))
            Text(
                state.placeName?.let { "Inicia sesión para registrar tu visita a $it." }
                    ?: "Con tu cuenta guardamos tus visitas, tus sellos y el progreso de tus colecciones.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            if (state.error != null) {
                Row(
                    Modifier.fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .semantics { liveRegion = LiveRegionMode.Assertive },
                ) {
                    Surface(shape = ThumbnailShape, color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(14.dp)) {
                            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(10.dp))
                            Text(state.error!!, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = viewModel::signInWithGoogle,
                enabled = !state.isLoading,
                shape = PillShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Text("Conectando con Google…")
                } else {
                    GoogleMark()
                    Spacer(Modifier.width(12.dp))
                    Text("Continuar con Google", color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(Modifier.height(16.dp))
            NoteBox(
                Icons.Outlined.Shield,
                "Tu perfil es privado: solo tú ves tu historial y tu colección. No creamos contraseñas; usamos tu cuenta de Google y solo guardamos tu nombre para mostrarlo.",
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Marca circular de cuatro colores (decorativa; el texto del botón ya nombra a Google). */
@Composable
private fun GoogleMark() {
    Canvas(Modifier.size(20.dp)) {
        val stroke = size.width * 0.22f
        val colors = listOf(Color(0xFF4285F4), Color(0xFF34A853), Color(0xFFFBBC05), Color(0xFFEA4335))
        colors.forEachIndexed { i, c ->
            drawArc(
                c, startAngle = -45f + i * 90f, sweepAngle = 90f, useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
                style = androidx.compose.ui.graphics.drawscope.Stroke(stroke),
            )
        }
    }
}
