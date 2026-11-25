package com.unicauca.turinauta.feature.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.ErrorContainerLight
import com.unicauca.turinauta.core.designsystem.ErrorRed
import com.unicauca.turinauta.core.designsystem.OnDeepTeal
import com.unicauca.turinauta.core.designsystem.OnErrorContainerDark
import com.unicauca.turinauta.core.designsystem.OnSoftTealContainer
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateDark
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SoftTealContainer
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.designsystem.WarmIvory
import com.unicauca.turinauta.core.designsystem.WarmOutline
import com.unicauca.turinauta.core.model.ReportField

/**
 * Modal privado para reportar datos incorrectos al equipo editorial (HU-24).
 * Funciona sin cuenta autenticada contra `POST /rest/v1/rpc/submit_content_report`.
 * Nunca se presenta como reseña ni comentario público.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataReportSheet(
    placeName: String,
    uiState: ReportUiState,
    onFieldSelected: (ReportField) -> Unit,
    onDetailsChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { if (!uiState.isSubmitting) onDismiss() },
        sheetState = sheetState,
        containerColor = WarmIvory,
    ) {
        DataReportSheetContent(
            placeName = placeName,
            uiState = uiState,
            onFieldSelected = onFieldSelected,
            onDetailsChange = onDetailsChange,
            onSubmitClick = onSubmitClick,
            onDismiss = onDismiss,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DataReportSheetContent(
    placeName: String,
    uiState: ReportUiState,
    onFieldSelected: (ReportField) -> Unit,
    onDetailsChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (uiState.isSubmitted) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = DeepTeal,
                    modifier = Modifier.size(44.dp),
                )
                Text(
                    text = "Aviso enviado",
                    style = MaterialTheme.typography.titleLarge,
                    color = SlateDark,
                )
                Text(
                    text = "Gracias por ayudarnos a mantener actualizada la información de $placeName. El equipo editorial revisará tu reporte de forma interna.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMedium,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepTeal,
                        contentColor = OnDeepTeal,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = TurinautaDimens.MinTouchTarget),
                ) {
                    Text("Entendido")
                }
            }
            return
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Reportar dato incorrecto",
                style = MaterialTheme.typography.titleLarge,
                color = SlateDark,
            )
            Text(
                text = "Aviso interno para $placeName. No se publicará como comentario ni reseña.",
                style = MaterialTheme.typography.bodySmall,
                color = SlateMedium,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "¿Qué dato necesita corrección?",
                style = MaterialTheme.typography.labelLarge,
                color = SlateDark,
            )
            FlowRow(
                modifier = Modifier.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ReportField.entries.forEach { field ->
                    val selected = uiState.selectedField == field
                    Surface(
                        color = if (selected) SoftTealContainer else PureWhite,
                        shape = RoundedCornerShape(TurinautaDimens.ChipCornerRadius),
                        border = if (selected) null else BorderStroke(1.dp, WarmOutline),
                        modifier = Modifier
                            .heightIn(min = TurinautaDimens.MinTouchTarget)
                            .clip(RoundedCornerShape(TurinautaDimens.ChipCornerRadius))
                            .selectable(
                                selected = selected,
                                enabled = !uiState.isSubmitting,
                                role = Role.RadioButton,
                                onClick = { onFieldSelected(field) },
                            )
                            .semantics(mergeDescendants = true) {},
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = OnSoftTealContainer,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Text(
                                text = field.labelEs,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) OnSoftTealContainer else SlateDark,
                            )
                        }
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedTextField(
                value = uiState.details,
                onValueChange = onDetailsChange,
                enabled = !uiState.isSubmitting,
                label = { Text("Describe qué está desactualizado o incorrecto") },
                placeholder = { Text("Ejemplo: Ahora abren de martes a sábado desde las 10:00.") },
                isError = uiState.validationError != null,
                minLines = 3,
                maxLines = 5,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = uiState.validationError ?: "Incluye un detalle breve y concreto.",
                            color = if (uiState.validationError != null) ErrorRed else SlateMedium,
                        )
                        Text(
                            text = "${uiState.details.length}/${uiState.maxDetailsLength}",
                            color = if (uiState.details.length > uiState.maxDetailsLength) {
                                ErrorRed
                            } else {
                                SlateMedium
                            },
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (uiState.networkError != null) {
            Surface(
                color = ErrorContainerLight,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = uiState.networkError,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnErrorContainerDark,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !uiState.isSubmitting,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = TurinautaDimens.MinTouchTarget),
            ) {
                Text("Cancelar", color = DeepTeal)
            }
            Button(
                onClick = onSubmitClick,
                enabled = !uiState.isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepTeal,
                    contentColor = OnDeepTeal,
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = TurinautaDimens.MinTouchTarget),
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        color = OnDeepTeal,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Text(if (uiState.networkError != null) "Reintentar" else "Enviar aviso")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun DataReportSheetContentPreview() {
    TurinautaTheme {
        Surface(color = WarmIvory) {
            DataReportSheetContent(
                placeName = "Cocina del Barrio",
                uiState = ReportUiState(
                    selectedField = ReportField.SCHEDULE,
                    details = "Los domingos ya no atienden en la tarde.",
                ),
                onFieldSelected = {},
                onDetailsChange = {},
                onSubmitClick = {},
                onDismiss = {},
            )
        }
    }
}
