package com.unicauca.turinauta.feature.visits

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.SystemClock
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.ErrorContainerLight
import com.unicauca.turinauta.core.designsystem.OnDeepTeal
import com.unicauca.turinauta.core.designsystem.OnErrorContainerDark
import com.unicauca.turinauta.core.designsystem.OnSoftOchreContainer
import com.unicauca.turinauta.core.designsystem.OnSoftTealContainer
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateDark
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SoftOchreContainer
import com.unicauca.turinauta.core.designsystem.SoftTealContainer
import com.unicauca.turinauta.core.designsystem.SubtleOutline
import com.unicauca.turinauta.core.designsystem.SurfaceLowSand
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.designsystem.WarmIvory
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.core.location.LocationPurpose
import com.unicauca.turinauta.core.location.getDeviceLocation
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class LocationButtonFeedback { IDLE, LOADING, UPDATED }

/**
 * ModalBottomSheet de confirmación y envío de visita (`CONTRATO_API_MVP.md` + R3/R4):
 * - Foto del lugar (JPG/PNG/WebP ≤ 5 MB)
 * - Ubicación actual del dispositivo (≤ 200 m)
 * - Botón «Cancelar» siempre accesible y bloqueo de doble toque al enviar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmVisitSheet(
    place: Place,
    uiState: VisitRegistrationUiState,
    onPhotoSelected: (previewUri: String, bytes: ByteArray, extension: String) -> Unit,
    onPhotoError: (String) -> Unit,
    onLocationLoading: () -> Unit,
    onLocationResolved: (Double, Double, Float) -> Unit,
    onLocationError: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var locationFeedback by remember { mutableStateOf(LocationButtonFeedback.IDLE) }
    var locationRequestId by remember { mutableIntStateOf(0) }

    fun handlePickedUri(uri: Uri) {
        scope.launch {
            val prepared = withContext(Dispatchers.IO) { prepareVisitPhoto(context, uri) }
            prepared.onSuccess { (bytes, ext) ->
                onPhotoSelected(uri.toString(), bytes, ext)
            }.onFailure { err ->
                onPhotoError(err.message ?: "No pudimos leer la imagen seleccionada.")
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) handlePickedUri(uri)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            handlePickedUri(uri)
        }
    }

    fun requestCurrentLocation() {
        val requestId = ++locationRequestId
        locationFeedback = LocationButtonFeedback.LOADING
        onLocationLoading()
        scope.launch {
            val startedAt = SystemClock.elapsedRealtime()
            val location = getDeviceLocation(context, LocationPurpose.VISIT)
            if (location != null) {
                onLocationResolved(location.latitude, location.longitude, location.accuracy)
            } else {
                onLocationError("No obtuvimos una ubicación reciente. Activa la ubicación y vuelve a intentar.")
            }
            // Keep the in-button indicator visible even when Android returns a cached fix instantly.
            delay((600L - (SystemClock.elapsedRealtime() - startedAt)).coerceAtLeast(0L))
            if (requestId == locationRequestId) {
                locationFeedback = if (location != null) LocationButtonFeedback.UPDATED else LocationButtonFeedback.IDLE
                if (location != null) {
                    delay(1_200L)
                    if (requestId == locationRequestId) locationFeedback = LocationButtonFeedback.IDLE
                }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            requestCurrentLocation()
        } else {
            onLocationError(
                "Necesitamos permiso de ubicación para comprobar que estás a menos de ${VisitRecord.MAX_VISIT_DISTANCE_METERS} m del lugar.",
            )
        }
    }

    // Si ya tiene permiso de ubicación al abrir el modal, intenta leerla automáticamente.
    LaunchedEffect(place.id) {
        if (!uiState.hasLocation && hasVisitLocationPermission(context)) {
            requestCurrentLocation()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!uiState.isSubmitting) onDismiss() },
        sheetState = sheetState,
        containerColor = WarmIvory,
    ) {
        ConfirmVisitSheetContent(
            placeName = place.name,
            uiState = uiState,
            locationFeedback = locationFeedback,
            onPickFromGalleryClick = { galleryLauncher.launch("image/*") },
            onTakePhotoClick = {
                runCatching { createCameraOutputUri(context) }
                    .onSuccess { uri ->
                        pendingCameraUri = uri
                        cameraLauncher.launch(uri)
                    }
                    .onFailure {
                        onPhotoError("No pudimos abrir la cámara en este dispositivo.")
                    }
            },
            onVerifyLocationClick = {
                if (hasVisitLocationPermission(context)) {
                    requestCurrentLocation()
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                }
            },
            onConfirmClick = onConfirmClick,
            onCancelClick = onDismiss,
        )
    }
}

@Composable
fun ConfirmVisitSheetContent(
    placeName: String,
    uiState: VisitRegistrationUiState,
    locationFeedback: LocationButtonFeedback = LocationButtonFeedback.IDLE,
    onPickFromGalleryClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onVerifyLocationClick: () -> Unit,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Registrar visita",
                style = MaterialTheme.typography.titleLarge,
                color = SlateDark,
            )
            Text(
                text = "Confirma tu paso por $placeName adjuntando una fotografía del lugar y tu ubicación actual.",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateMedium,
            )
        }

        // Paso 1: Fotografía del lugar (JPG/PNG/WebP ≤ 5 MB)
        val sectionShape = RoundedCornerShape(16.dp)
        Surface(
            color = PureWhite,
            shape = sectionShape,
            modifier = Modifier
                .fillMaxWidth()
                .border(TurinautaDimens.CardBorderWidth, SubtleOutline, sectionShape),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "1. Fotografía del lugar",
                    style = MaterialTheme.typography.titleSmall,
                    color = SlateDark,
                )

                if (uiState.selectedPhotoUri != null && uiState.hasValidPhoto) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = uiState.selectedPhotoUri,
                            contentDescription = "Vista previa de foto seleccionada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceLowSand),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = DeepTeal,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = "Foto lista para subir",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = DeepTeal,
                                )
                            }
                            Text(
                                text = "${uiState.photoExtension?.uppercase().orEmpty()} · ${uiState.photoSizeKb ?: 0} KB (máx. 5 MB)",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMedium,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onTakePhotoClick,
                        enabled = !uiState.isSubmitting,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = TurinautaDimens.MinTouchTarget),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = null,
                            tint = DeepTeal,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cámara", color = DeepTeal)
                    }
                    OutlinedButton(
                        onClick = onPickFromGalleryClick,
                        enabled = !uiState.isSubmitting,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = TurinautaDimens.MinTouchTarget),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoLibrary,
                            contentDescription = null,
                            tint = DeepTeal,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Galería", color = DeepTeal)
                    }
                }

                if (uiState.photoError != null) {
                    Surface(
                        color = ErrorContainerLight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = uiState.photoError,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnErrorContainerDark,
                            modifier = Modifier.padding(10.dp),
                        )
                    }
                }

                Text(
                    text = "Acepta JPG, PNG o WebP hasta 5 MB. Las fotos de visita son públicas en Turinauta; evita mostrar datos personales.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMedium,
                )
            }
        }

        // Paso 2: Verificación de ubicación (≤ 200 m)
        Surface(
            color = PureWhite,
            shape = sectionShape,
            modifier = Modifier
                .fillMaxWidth()
                .border(TurinautaDimens.CardBorderWidth, SubtleOutline, sectionShape),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "2. Ubicación actual (máx. ${VisitRecord.MAX_VISIT_DISTANCE_METERS} m)",
                    style = MaterialTheme.typography.titleSmall,
                    color = SlateDark,
                )

                when {
                    uiState.isFetchingLocation -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            CircularProgressIndicator(
                                color = DeepTeal,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "Obteniendo ubicación actual...",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMedium,
                            )
                        }
                    }

                    uiState.hasLocation -> {
                        val withinRange = uiState.isWithinRange
                        Surface(
                            color = if (withinRange) SoftTealContainer else SoftOchreContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = if (withinRange) {
                                        Icons.Rounded.CheckCircle
                                    } else {
                                        Icons.Rounded.WarningAmber
                                    },
                                    contentDescription = null,
                                    tint = if (withinRange) OnSoftTealContainer else OnSoftOchreContainer,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = if (withinRange) {
                                        "Distancia estimada: ${uiState.distanceMeters} m del lugar (precisión aproximada: ${uiState.locationAccuracyMeters} m). El servidor confirmará el límite de ${VisitRecord.MAX_VISIT_DISTANCE_METERS} m."
                                    } else {
                                        "Parece que estás lejos de este lugar. Acércate o actualiza tu ubicación si crees que no es correcta."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (withinRange) OnSoftTealContainer else OnSoftOchreContainer,
                                )
                            }
                        }
                    }

                    uiState.locationError != null -> {
                        Surface(
                            color = ErrorContainerLight,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = uiState.locationError,
                                style = MaterialTheme.typography.bodySmall,
                                color = OnErrorContainerDark,
                                modifier = Modifier.padding(10.dp),
                            )
                        }
                    }

                    else -> {
                        Text(
                            text = "Presiona el botón para validar tu posición antes de confirmar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMedium,
                        )
                    }
                }

                val buttonColor by animateColorAsState(
                    targetValue = if (locationFeedback == LocationButtonFeedback.IDLE) PureWhite else SoftTealContainer,
                    label = "Estado del botón de ubicación",
                )
                OutlinedButton(
                    onClick = onVerifyLocationClick,
                    enabled = !uiState.isSubmitting && !uiState.isFetchingLocation &&
                        locationFeedback != LocationButtonFeedback.LOADING,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = buttonColor,
                        disabledContainerColor = buttonColor,
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (locationFeedback == LocationButtonFeedback.IDLE) SubtleOutline else DeepTeal,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = TurinautaDimens.MinTouchTarget)
                        .semantics {
                            stateDescription = when (locationFeedback) {
                                LocationButtonFeedback.LOADING -> "Actualizando ubicación"
                                LocationButtonFeedback.UPDATED -> "Ubicación actualizada"
                                LocationButtonFeedback.IDLE -> "Listo para actualizar ubicación"
                            }
                        },
                ) {
                    if (locationFeedback == LocationButtonFeedback.LOADING) {
                        CircularProgressIndicator(
                            color = DeepTeal,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                        )
                    } else {
                        Icon(
                            imageVector = if (locationFeedback == LocationButtonFeedback.UPDATED) {
                                Icons.Rounded.CheckCircle
                            } else Icons.Outlined.MyLocation,
                            contentDescription = null,
                            tint = DeepTeal,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.hasLocation) "Actualizar ubicación" else "Verificar ubicación",
                        color = DeepTeal,
                    )
                }
            }
        }

        // Botones de acción (R3: siempre incluir «Cancelar»; R4: bloquear doble toque)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onCancelClick,
                enabled = !uiState.isSubmitting,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = TurinautaDimens.MinTouchTarget),
            ) {
                Text("Cancelar", color = DeepTeal)
            }
            Button(
                onClick = onConfirmClick,
                enabled = uiState.canSubmit,
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
                    Text("Confirmar visita")
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
    }
}

private fun createCameraOutputUri(context: Context): Uri {
    val dir = File(context.cacheDir, "visit_photos").apply { mkdirs() }
    val file = File(dir, "visit_${UUID.randomUUID()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}

private fun prepareVisitPhoto(context: Context, uri: Uri): Result<Pair<ByteArray, String>> =
    runCatching {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri)
        val inferredExt = mimeType
            ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
            ?.lowercase()
            ?: uri.lastPathSegment?.substringAfterLast('.', "")?.lowercase()
            ?: "jpg"

        val rawBytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("No se pudo abrir la imagen seleccionada.")

        if (rawBytes.isEmpty()) {
            throw IllegalArgumentException("La imagen seleccionada está vacía.")
        }

        // Si supera 5 MB o no es jpg/png/webp, comprime de forma segura a JPEG ≤ 5 MB.
        val normalizedExt = if (inferredExt == "jpeg") "jpg" else inferredExt
        if (rawBytes.size.toLong() <= VisitRecord.MAX_VISIT_PHOTO_BYTES &&
            normalizedExt in setOf("jpg", "png", "webp")
        ) {
            return@runCatching rawBytes to normalizedExt
        }

        val bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
            ?: throw IllegalArgumentException("Formato de imagen no compatible. Usa JPG, PNG o WebP.")

        val maxSide = 1600
        val largest = max(bitmap.width, bitmap.height)
        val scaled = if (largest > maxSide) {
            val ratio = maxSide.toFloat() / largest.toFloat()
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt().coerceAtLeast(1),
                (bitmap.height * ratio).toInt().coerceAtLeast(1),
                true,
            )
        } else {
            bitmap
        }

        var quality = 85
        var compressed: ByteArray
        do {
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            compressed = out.toByteArray()
            quality -= 12
        } while (compressed.size.toLong() > VisitRecord.MAX_VISIT_PHOTO_BYTES && quality >= 35)

        if (scaled !== bitmap) scaled.recycle()
        bitmap.recycle()

        if (compressed.size.toLong() > VisitRecord.MAX_VISIT_PHOTO_BYTES) {
            throw IllegalArgumentException("La foto supera el límite máximo de 5 MB.")
        }

        compressed to "jpg"
    }

private fun hasVisitLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

@Preview(showBackground = true)
@Composable
private fun ConfirmVisitSheetContentPreview() {
    TurinautaTheme {
        Surface(color = WarmIvory) {
            ConfirmVisitSheetContent(
                placeName = "Cocina del Barrio",
                uiState = VisitRegistrationUiState(
                    latitude = 2.4419,
                    longitude = -76.6063,
                    distanceMeters = 42,
                ),
                onPickFromGalleryClick = {},
                onTakePhotoClick = {},
                onVerifyLocationClick = {},
                onConfirmClick = {},
                onCancelClick = {},
            )
        }
    }
}
