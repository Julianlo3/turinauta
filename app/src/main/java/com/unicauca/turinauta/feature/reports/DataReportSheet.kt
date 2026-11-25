package com.unicauca.turinauta.feature.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unicauca.turinauta.core.model.DataReport
import com.unicauca.turinauta.core.model.ReportField
import com.unicauca.turinauta.core.navigation.TurinautaViewModelFactory
import com.unicauca.turinauta.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportUiState(val isSending: Boolean = false, val sent: Boolean = false, val error: String? = null)

class ReportViewModel(private val reports: ReportRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun send(placeId: String, field: ReportField, detail: String) {
        if (_uiState.value.isSending) return
        _uiState.update { it.copy(isSending = true, error = null) }
        viewModelScope.launch {
            val result = reports.sendReport(DataReport(placeId, field, detail.trim()))
            _uiState.update {
                if (result.isSuccess) ReportUiState(sent = true)
                else it.copy(
                    isSending = false,
                    error = result.exceptionOrNull()?.message ?: "No se pudo enviar el aviso. Inténtalo de nuevo.",
                )
            }
        }
    }

    fun reset() = _uiState.update { ReportUiState() }
}

/**
 * HU-24: aviso privado al equipo editorial (`rpc/submit_content_report`, funciona sin cuenta).
 * No se publica ni funciona como reseña. El detalle es obligatorio (máx. 1500 caracteres).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataReportSheet(
    placeId: String,
    onDismiss: () -> Unit,
    onSent: () -> Unit,
    viewModel: ReportViewModel = viewModel(factory = TurinautaViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var field by rememberSaveable { mutableStateOf(ReportField.SCHEDULE) }
    var detail by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.sent) {
        if (state.sent) { viewModel.reset(); onSent() }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!state.isSending) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.padding(horizontal = 20.dp).navigationBarsPadding().padding(bottom = 16.dp)) {
            Text("Reportar un dato incorrecto", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal))
            Spacer(Modifier.height(8.dp))
            Text(
                "Tu aviso llega al equipo que revisa el contenido. No se publica ni es una reseña.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Text("¿Qué dato no coincide?", style = MaterialTheme.typography.labelLarge)
            Column(Modifier.selectableGroup()) {
                ReportField.entries.forEach { option ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            .selectable(selected = field == option, role = Role.RadioButton, onClick = { field = option }),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = field == option, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Text(option.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = detail,
                onValueChange = { if (it.length <= DataReport.MAX_DETAILS) detail = it },
                label = { Text("Detalle") },
                placeholder = { Text("Ejemplo: el local también atiende los domingos.") },
                supportingText = { Text("Obligatorio · ${detail.length}/${DataReport.MAX_DETAILS}") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss, enabled = !state.isSending, modifier = Modifier.heightIn(min = 48.dp)) { Text("Cancelar") }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.send(placeId, field, detail) },
                    enabled = !state.isSending && detail.isNotBlank(),
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    if (state.isSending) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Enviar aviso")
                }
            }
        }
    }
}
