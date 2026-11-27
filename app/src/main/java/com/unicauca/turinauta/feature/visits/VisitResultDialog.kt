package com.unicauca.turinauta.feature.visits

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.ErrorRed
import com.unicauca.turinauta.core.designsystem.OnDeepTeal
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateDark
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.model.RewardBadge
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.core.model.VisitResult

/**
 * Diálogo de resultado del registro de visita (R4 + HU-18):
 * nunca simula éxito ante error y ofrece «Reintentar» en fallos de red/guardado.
 */
@Composable
fun VisitResultDialog(
    result: VisitResult,
    placeName: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onViewCollection: () -> Unit,
    onViewStamp: (String) -> Unit,
    onGoToAuth: () -> Unit,
) {
    when (result) {
        is VisitResult.VisitCreated -> {
            if (result.reward != null) {
                VisitStampSuccessDialog(
                    visitId = result.visit.id,
                    placeName = placeName,
                    reward = result.reward,
                    onDismiss = onDismiss,
                    onViewStamp = { onViewStamp(result.reward.id) },
                )
            } else AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = PureWhite,
                title = {
                    Text(
                        text = "¡Visita registrada!",
                        style = MaterialTheme.typography.titleLarge,
                        color = SlateDark,
                    )
                },
                text = {
                    Text(
                        text = "Tu visita a $placeName quedó guardada correctamente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMedium,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onViewCollection,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepTeal,
                            contentColor = OnDeepTeal,
                        ),
                        modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                    ) {
                        Text("Ver colección")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                    ) {
                        Text("Entendido", color = DeepTeal)
                    }
                },
            )
        }

        is VisitResult.VisitDuplicate -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = PureWhite,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = DeepTeal,
                        modifier = Modifier.size(34.dp),
                    )
                },
                title = {
                    Text(
                        text = "Visita ya registrada",
                        style = MaterialTheme.typography.titleLarge,
                        color = SlateDark,
                    )
                },
                text = {
                    Text(
                        text = "Ya tenías una visita válida en $placeName. Conservamos tu registro original y tu sello sin duplicarlos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMedium,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onViewCollection,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepTeal,
                            contentColor = OnDeepTeal,
                        ),
                        modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                    ) {
                        Text("Ver colección")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                    ) {
                        Text("Cerrar", color = DeepTeal)
                    }
                },
            )
        }

        is VisitResult.VisitOutOfRange -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = PureWhite,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.LocationOff,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(34.dp),
                    )
                },
                title = {
                    Text(
                        text = "Fuera del rango permitido",
                        style = MaterialTheme.typography.titleLarge,
                        color = SlateDark,
                    )
                },
                text = {
                    Text(
                        text = result.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMedium,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepTeal,
                            contentColor = OnDeepTeal,
                        ),
                        modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                    ) {
                        Text("Volver a verificar")
                    }
                },
            )
        }

        is VisitResult.VisitInvalidPhoto -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = PureWhite,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.PhotoCamera,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(34.dp),
                    )
                },
                title = {
                    Text(
                        text = "Revisa la fotografía",
                        style = MaterialTheme.typography.titleLarge,
                        color = SlateDark,
                    )
                },
                text = {
                    Text(
                        text = result.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMedium,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepTeal,
                            contentColor = OnDeepTeal,
                        ),
                        modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                    ) {
                        Text("Elegir otra foto")
                    }
                },
            )
        }

        VisitResult.VisitUnauthenticated -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = PureWhite,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = DeepTeal,
                        modifier = Modifier.size(34.dp),
                    )
                },
                title = {
                    Text(
                        text = "Sesión requerida",
                        style = MaterialTheme.typography.titleLarge,
                        color = SlateDark,
                    )
                },
                text = {
                    Text(
                        text = "Inicia sesión con tu cuenta de Google para registrar tu visita y conservar el sello.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMedium,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onGoToAuth,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepTeal,
                            contentColor = OnDeepTeal,
                        ),
                        modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                    ) {
                        Text("Iniciar sesión")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                    ) {
                        Text("Cancelar", color = DeepTeal)
                    }
                },
            )
        }

        is VisitResult.VisitNetworkError -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = PureWhite,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(34.dp),
                    )
                },
                title = {
                    Text(
                        text = "No pudimos guardar la visita",
                        style = MaterialTheme.typography.titleLarge,
                        color = SlateDark,
                    )
                },
                text = {
                    Text(
                        text = "${result.message} Puedes reintentar de forma segura sin duplicar el registro.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMedium,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepTeal,
                            contentColor = OnDeepTeal,
                        ),
                        modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                    ) {
                        Text("Reintentar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                    ) {
                        Text("Cancelar", color = DeepTeal)
                    }
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VisitResultDialogPreview() {
    TurinautaTheme {
        VisitResultDialog(
            result = VisitResult.VisitCreated(
                visit = VisitRecord(
                    id = "v1",
                    placeId = "p1",
                    placeName = "Cocina del Barrio",
                    visitedAtIso = "2026-11-24T20:00:00Z",
                ),
                reward = RewardBadge(
                    id = "s1",
                    title = "Sabores de Fogón",
                    description = "Otorgado por descubrir la cocina tradicional del barrio.",
                    isEarned = true,
                ),
            ),
            placeName = "Cocina del Barrio",
            onDismiss = {},
            onRetry = {},
            onViewCollection = {},
            onViewStamp = {},
            onGoToAuth = {},
        )
    }
}
