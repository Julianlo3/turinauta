package com.unicauca.turinauta.feature.visits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.WrongLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.unicauca.turinauta.core.designsystem.NoteBox
import com.unicauca.turinauta.core.designsystem.PillShape
import com.unicauca.turinauta.core.designsystem.ThumbnailShape
import com.unicauca.turinauta.core.designsystem.formatShortDate
import com.unicauca.turinauta.core.model.VisitPhoto
import com.unicauca.turinauta.core.model.VisitResult

/** Hoja para personas sin sesión: no se guarda nada hasta que entren con Google. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginRequiredSheet(
    placeName: String,
    onLogin: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 20.dp).navigationBarsPadding().padding(bottom = 16.dp)) {
            Box(
                Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.height(16.dp))
            Text("Inicia sesión para registrar tu visita", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal))
            Spacer(Modifier.height(8.dp))
            Text(
                "Con una cuenta guardamos tus visitas, tus sellos y tu progreso. Tu visita a $placeName aún no se ha registrado.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = onLogin, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = PillShape) { Text("Continuar con Google") }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Ahora no") }
        }
    }
}

/**
 * Pasos del registro (contrato API): 1) foto del lugar, 2) ubicación actual a ≤ 200 m.
 * La visita solo se guarda al pulsar «Confirmar visita»; «Cancelar» no guarda nada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterVisitSheet(
    placeName: String,
    photo: VisitPhoto?,
    photoError: String?,
    onTakePhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onCancel, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier.padding(horizontal = 20.dp).navigationBarsPadding().padding(bottom = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("Registrar tu visita", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal))
            Spacer(Modifier.height(8.dp))
            Text(
                buildAnnotatedString {
                    append("Vas a registrar tu visita a ")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(placeName) }
                    append(" con la fecha de hoy, ${formatShortDate(System.currentTimeMillis())}. Cada lugar se registra una sola vez por cuenta.")
                },
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(Modifier.height(16.dp))
            StepTitle(1, "Foto del lugar")
            Spacer(Modifier.height(8.dp))
            if (photo != null) {
                PhotoPreview(
                    photo.uri,
                    contentDescription = "Foto elegida para la visita",
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(ThumbnailShape),
                )
                Spacer(Modifier.height(8.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onTakePhoto, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                    Icon(Icons.Outlined.PhotoCamera, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (photo == null) "Tomar foto" else "Otra foto")
                }
                OutlinedButton(onClick = onPickPhoto, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                    Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Galería")
                }
            }
            if (photoError != null) {
                Spacer(Modifier.height(6.dp))
                Text(photoError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "JPG, PNG o WebP de hasta 5 MB. La foto queda en un enlace público: no incluyas rostros ni datos personales.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))
            StepTitle(2, "Tu ubicación")
            Spacer(Modifier.height(8.dp))
            NoteBox(
                Icons.Outlined.MyLocation,
                "Al confirmar comprobamos que estás a menos de 200 m del lugar. Solo usamos tu ubicación en este momento.",
            )

            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onCancel, modifier = Modifier.heightIn(min = 48.dp)) { Text("Cancelar") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onConfirm, enabled = photo != null, modifier = Modifier.heightIn(min = 48.dp), shape = PillShape) {
                    Text("Confirmar visita")
                }
            }
            if (photo == null) {
                Text(
                    "Agrega una foto para poder confirmar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun StepTitle(number: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(26.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Text("$number", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer) }
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.titleSmall)
    }
}

/** Estado de carga visible y no descartable: bloquea el doble toque. */
@Composable
fun BusyDialog(title: String, message: String) {
    Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column(
                Modifier.padding(28.dp).semantics { liveRegion = LiveRegionMode.Polite },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(20.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    }
}

/** El fallo se explica y, si aplica, se ofrece reintentar; nunca se muestra un éxito falso. */
@Composable
fun VisitErrorDialog(title: String, message: String, retryable: Boolean, onRetry: () -> Unit, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        icon = { Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(title, textAlign = TextAlign.Center) },
        text = {
            Column {
                Text(message)
                Spacer(Modifier.height(8.dp))
                Text("Tu visita aún no está registrada y no se ha otorgado ningún sello.")
            }
        },
        confirmButton = {
            if (retryable) {
                Button(onClick = onRetry, shape = PillShape, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Reintentar")
                }
            } else {
                Button(onClick = onClose, shape = PillShape, modifier = Modifier.heightIn(min = 48.dp)) { Text("Entendido") }
            }
        },
        dismissButton = if (retryable) {
            { TextButton(onClick = onClose, modifier = Modifier.heightIn(min = 48.dp)) { Text("Cancelar") } }
        } else null,
    )
}

/** `record_visit` rechaza la visita a más de 200 m del lugar. */
@Composable
fun TooFarDialog(distanceMeters: Int, placeName: String, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        icon = { Icon(Icons.Outlined.WrongLocation, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Aún no estás en el lugar", textAlign = TextAlign.Center) },
        text = {
            Text(
                "Para registrar tu visita a $placeName debes estar a menos de ${VisitResult.MAX_DISTANCE_METERS} m. " +
                    "Ahora estás a unos ${com.unicauca.turinauta.core.designsystem.formatDistance(distanceMeters.toDouble())}. " +
                    "Tu visita no se registró; puedes intentarlo cuando llegues.",
            )
        },
        confirmButton = { Button(onClick = onClose, shape = PillShape, modifier = Modifier.heightIn(min = 48.dp)) { Text("Entendido") } },
    )
}
