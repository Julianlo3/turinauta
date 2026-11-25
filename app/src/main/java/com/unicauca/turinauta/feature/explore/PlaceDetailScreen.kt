package com.unicauca.turinauta.feature.explore

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.DisposableEffect
import androidx.core.view.WindowCompat
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unicauca.turinauta.core.designsystem.CatalogImage
import com.unicauca.turinauta.core.designsystem.DisplayFontFamily
import com.unicauca.turinauta.core.designsystem.StampArt
import com.unicauca.turinauta.core.designsystem.stampStyleFor
import com.unicauca.turinauta.core.model.RewardBadge
import com.unicauca.turinauta.core.model.ThematicCollection
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.unicauca.turinauta.core.designsystem.EmptyState
import com.unicauca.turinauta.core.designsystem.InfoRow
import com.unicauca.turinauta.core.designsystem.LoadingState
import com.unicauca.turinauta.core.designsystem.LocalSnackbarHostState
import com.unicauca.turinauta.core.designsystem.PillShape
import com.unicauca.turinauta.core.designsystem.PlaceHeaderArt
import com.unicauca.turinauta.core.designsystem.PrimaryWideButton
import com.unicauca.turinauta.core.designsystem.SCHEDULE_UNCONFIRMED
import com.unicauca.turinauta.core.designsystem.ThumbnailShape
import com.unicauca.turinauta.core.designsystem.formatShortDate
import com.unicauca.turinauta.core.designsystem.icon
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.navigation.TurinautaViewModelFactory
import com.unicauca.turinauta.feature.reports.DataReportSheet
import com.unicauca.turinauta.feature.visits.BusyDialog
import com.unicauca.turinauta.feature.visits.LoginRequiredSheet
import com.unicauca.turinauta.feature.visits.RegisterVisitSheet
import com.unicauca.turinauta.feature.visits.TooFarDialog
import com.unicauca.turinauta.feature.visits.VisitErrorDialog
import com.unicauca.turinauta.feature.visits.VisitResultDialog
import com.unicauca.turinauta.feature.visits.newCameraPhotoUri
import com.unicauca.turinauta.feature.visits.toVisitPhoto

@Composable
fun PlaceDetailScreen(
    onBack: () -> Unit,
    onLogin: (placeName: String) -> Unit,
    onOpenCollection: () -> Unit,
    viewModel: PlaceDetailViewModel = viewModel(factory = TurinautaViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    var showReport by rememberSaveable { mutableStateOf(false) }
    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val uri = pendingCameraUri
        if (saved && uri != null) viewModel.onPhotoSelected(context.toVisitPhoto(Uri.parse(uri)))
    }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onPhotoSelected(context.toVisitPhoto(uri))
    }
    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        viewModel.onConfirmVisit(locationGranted = result.values.any { it })
    }

    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.onMessageShown() }
    }

    // La cabecera es una foto oscura: íconos claros en la barra de estado mientras se ve la ficha.
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? android.app.Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previous = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose { if (previous != null) controller.isAppearanceLightStatusBars = previous }
    }

    val place = state.place
    when {
        state.isLoading -> LoadingState()
        state.loadError != null -> Column(Modifier.fillMaxSize().statusBarsPadding()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
            EmptyState(
                icon = Icons.Outlined.CloudOff,
                title = "No pudimos cargar la ficha",
                message = state.loadError!!,
                primaryAction = "Reintentar" to viewModel::loadPlace,
            )
        }
        place == null -> Column(Modifier.fillMaxSize().statusBarsPadding()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
            EmptyState(
                icon = Icons.Outlined.SearchOff,
                title = "Este lugar no está disponible",
                message = "Es posible que la ficha se haya retirado mientras se revisa su información.",
                primaryAction = "Volver al catálogo" to onBack,
            )
        }
        else -> PlaceContent(
            place = place,
            state = state,
            onBack = onBack,
            onRegister = viewModel::onRegisterVisitClick,
            onOpenCollection = onOpenCollection,
            onReport = { showReport = true },
        )
    }

    if (place != null) {
        when (val flow = state.visitFlow) {
            VisitFlow.LoginRequired -> LoginRequiredSheet(
                placeName = place.name,
                onLogin = { viewModel.onGoToLogin(); onLogin(place.name) },
                onDismiss = viewModel::onDismissLogin,
            )
            VisitFlow.Preparing -> RegisterVisitSheet(
                placeName = place.name,
                photo = state.photo,
                photoError = state.photoError,
                onTakePhoto = {
                    val uri = context.newCameraPhotoUri()
                    pendingCameraUri = uri.toString()
                    takePicture.launch(uri)
                },
                onPickPhoto = { pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onConfirm = {
                    if (viewModel.needsLocationPermission()) {
                        locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    } else viewModel.onConfirmVisit()
                },
                onCancel = viewModel::onCancelVisit,
            )
            VisitFlow.Locating -> BusyDialog("Comprobando tu ubicación…", "Verificamos que estés cerca del lugar.")
            VisitFlow.Saving -> BusyDialog("Guardando tu visita…", "Subimos tu foto y registramos la visita. Esto tardará solo un momento.")
            is VisitFlow.TooFar -> TooFarDialog(flow.distanceMeters, place.name, onClose = viewModel::onCancelVisit)
            is VisitFlow.Failed -> VisitErrorDialog(
                title = flow.title,
                message = flow.message,
                retryable = flow.retryable,
                onRetry = viewModel::onRetry,
                onClose = if (flow.retryable) viewModel::onCancelVisit else viewModel::onBackToPreparing,
            )
            is VisitFlow.Succeeded -> VisitResultDialog(
                result = flow.result,
                onOpenCollection = { viewModel.onDismissSuccess(); onOpenCollection() },
                onBackToPlace = viewModel::onDismissSuccess,
            )
            VisitFlow.Idle -> Unit
        }
        if (showReport) {
            DataReportSheet(
                placeId = place.id,
                onDismiss = { showReport = false },
                onSent = { showReport = false; viewModel.showMessage("Gracias. Revisaremos el dato que señalaste.") },
            )
        }
    }
}

@Composable
private fun PlaceContent(
    place: Place,
    state: PlaceDetailUiState,
    onBack: () -> Unit,
    onRegister: () -> Unit,
    onOpenCollection: () -> Unit,
    onReport: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().height(320.dp)) {
                CatalogImage(
                    imageUrl = place.imageUrl,
                    illustration = place.illustration,
                    contentDescription = "Foto de ${place.name}",
                    modifier = Modifier.fillMaxSize(),
                    fallback = { PlaceHeaderArt(place.illustration, it) },
                )
                // Degradado para que el nombre se lea sobre cualquier foto.
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(0.45f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.72f)),
                    ),
                )
                Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            place.category.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        place.name,
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.semantics { heading() },
                    )
                }
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.statusBarsPadding().padding(8.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                // La etiqueta solo aparece cuando aún no hay foto autorizada del lugar.
                if (place.imageUrl == null) {
                    Surface(
                        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.75f),
                        shape = PillShape,
                        modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(14.dp),
                    ) {
                        Text("Imagen ilustrativa", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.inverseOnSurface, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                }
            }
            Column(Modifier.padding(16.dp)) {
                state.route?.let { route ->
                    Row(
                        Modifier.fillMaxWidth().clip(ThumbnailShape).background(MaterialTheme.colorScheme.surfaceContainerLow).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Text("Parte de la ruta ${route.name}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(12.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Place, contentDescription = null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text("Popayán · ${place.category.label}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                state.existingVisit?.let { visit ->
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth().clip(ThumbnailShape).background(MaterialTheme.colorScheme.primaryContainer).padding(14.dp),
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Ya registraste este lugar el ${formatShortDate(visit.visitedAt)}. Cada lugar cuenta una vez, así que no se sumará otro sello.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(place.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                InfoRow(
                    Icons.Outlined.Schedule,
                    label = "Horario",
                    value = place.schedule ?: SCHEDULE_UNCONFIRMED,
                    note = if (place.schedule == null) "Te recomendamos consultarlo antes de ir." else null,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (place.conditions.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Condiciones de visita", style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                    place.conditions.forEachIndexed { index, condition ->
                        InfoRow(condition.type.icon(), label = null, value = condition.text)
                        if (index < place.conditions.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Ubicación", style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(8.dp))
                PlaceLocationMap(place.latitude, place.longitude, place.name, place.addressReference)
                Text(
                    "Para ver el mapa no necesitas compartir tu ubicación.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )

                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onReport, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Outlined.Flag, contentDescription = null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    Text("Reportar un dato incorrecto", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 6.dp) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp)) {
                state.reward?.let { reward -> RewardPreview(reward, state.rewardCollection, earned = state.existingVisit != null) }
                if (state.existingVisit != null) {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = PillShape,
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Visita ya registrada", fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = onOpenCollection, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ver mi sello")
                    }
                } else {
                    PrimaryWideButton(
                        text = "Registrar visita",
                        onClick = onRegister,
                        enabled = !state.isBusy,
                        leadingIcon = Icons.Outlined.CheckCircle,
                    )
                }
            }
        }
    }
}

/** Muestra el sello que se gana (o que ya se ganó) junto al botón de registrar. */
@Composable
private fun RewardPreview(reward: RewardBadge, collection: ThematicCollection?, earned: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 8.dp).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StampArt(
            reward.stamp.illustration,
            obtained = earned,
            size = 44.dp,
            style = stampStyleFor(collection?.styleIndex ?: 0),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (earned) "Tu sello" else "Al registrar la visita obtienes",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(reward.stamp.title, style = MaterialTheme.typography.titleSmall)
            collection?.let {
                Text("Colección ${it.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
