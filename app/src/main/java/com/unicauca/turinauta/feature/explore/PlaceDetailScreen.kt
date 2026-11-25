package com.unicauca.turinauta.feature.explore

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.unicauca.turinauta.core.designsystem.CategoryOverlineTextStyle
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.OnDeepTeal
import com.unicauca.turinauta.core.designsystem.OnSoftTealContainer
import com.unicauca.turinauta.core.designsystem.PlaceIllustration
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateDark
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SoftTealContainer
import com.unicauca.turinauta.core.designsystem.SubtleOutline
import com.unicauca.turinauta.core.designsystem.SurfaceThumbBeige
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaBackTopBar
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.designsystem.WarmIvory
import com.unicauca.turinauta.core.model.IllustrationKey
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.core.model.VisitResult
import com.unicauca.turinauta.core.network.SupabaseConfig
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.ReportRepository
import com.unicauca.turinauta.data.repository.VisitRepository
import com.unicauca.turinauta.data.repository.WalkingRouteRepository
import com.unicauca.turinauta.feature.reports.DataReportSheet
import com.unicauca.turinauta.feature.reports.ReportViewModel
import com.unicauca.turinauta.feature.visits.ConfirmVisitSheet
import com.unicauca.turinauta.feature.visits.VisitRegistrationViewModel
import com.unicauca.turinauta.feature.visits.VisitResultDialog
import java.util.Locale

@Composable
fun PlaceDetailRoute(
    placeId: String,
    catalogRepository: CatalogRepository,
    visitRepository: VisitRepository,
    authRepository: AuthRepository,
    reportRepository: ReportRepository,
    walkingRouteRepository: WalkingRouteRepository,
    onBackClick: () -> Unit,
    onRequireAuth: () -> Unit,
    onNavigateToCollection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PlaceDetailViewModel = viewModel(
        key = "place_detail_$placeId",
        factory = PlaceDetailViewModel.factory(
            placeId = placeId,
            catalogRepository = catalogRepository,
            visitRepository = visitRepository,
            authRepository = authRepository,
        ),
    )
    val reportViewModel: ReportViewModel = viewModel(
        key = "report_$placeId",
        factory = ReportViewModel.factory(
            placeId = placeId,
            reportRepository = reportRepository,
        ),
    )
    val visitViewModel: VisitRegistrationViewModel = viewModel(
        key = "visit_$placeId",
        factory = VisitRegistrationViewModel.factory(visitRepository),
    )
    val mapViewModel: PlaceMapViewModel = viewModel(
        key = "place_map_$placeId",
        factory = PlaceMapViewModel.factory(walkingRouteRepository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val reportUiState by reportViewModel.uiState.collectAsStateWithLifecycle()
    val visitUiState by visitViewModel.uiState.collectAsStateWithLifecycle()
    var showReportSheet by remember { mutableStateOf(false) }
    var showVisitSheet by remember { mutableStateOf(false) }
    var showMap by rememberSaveable(placeId) { mutableStateOf(false) }
    BackHandler(enabled = showMap) { showMap = false }

    LaunchedEffect(visitUiState.visitResult) {
        when (visitUiState.visitResult) {
            is VisitResult.VisitCreated,
            is VisitResult.VisitDuplicate,
            -> {
                showVisitSheet = false
                viewModel.refreshExistingVisit()
            }
            else -> Unit
        }
    }

    val mapPlace = uiState.place
    if (showMap && mapPlace != null) {
        FullScreenPlaceMap(
            place = mapPlace,
            viewModel = mapViewModel,
            onBackClick = { showMap = false },
            modifier = modifier,
        )
    } else {
        PlaceDetailScreen(
            uiState = uiState,
            onBackClick = onBackClick,
            onRetryClick = viewModel::loadPlaceDetail,
            onOpenMapClick = { showMap = true },
            onRegisterVisitClick = {
                if (uiState.isAuthenticated) {
                    visitViewModel.reset()
                    showVisitSheet = true
                } else {
                    onRequireAuth()
                }
            },
            onReportIncorrectDataClick = {
                reportViewModel.reset()
                showReportSheet = true
            },
            modifier = modifier,
        )
    }

    val currentPlace = uiState.place
    if (showReportSheet && currentPlace != null) {
        DataReportSheet(
            placeName = currentPlace.name,
            uiState = reportUiState,
            onFieldSelected = reportViewModel::onFieldSelected,
            onDetailsChange = reportViewModel::onDetailsChange,
            onSubmitClick = reportViewModel::submitReport,
            onDismiss = {
                showReportSheet = false
                reportViewModel.reset()
            },
        )
    }

    if (showVisitSheet && currentPlace != null) {
        ConfirmVisitSheet(
            place = currentPlace,
            uiState = visitUiState,
            onPhotoSelected = visitViewModel::onPhotoSelected,
            onPhotoError = visitViewModel::onPhotoError,
            onLocationLoading = visitViewModel::onLocationLoading,
            onLocationResolved = { lat, lon ->
                visitViewModel.onLocationResolved(
                    userLat = lat,
                    userLon = lon,
                    placeLat = currentPlace.latitude,
                    placeLon = currentPlace.longitude,
                )
            },
            onLocationError = visitViewModel::onLocationError,
            onConfirmClick = { visitViewModel.submitVisit(currentPlace.id) },
            onDismiss = {
                showVisitSheet = false
                visitViewModel.reset()
            },
        )
    }

    val visitResult = visitUiState.visitResult
    if (visitResult != null && currentPlace != null) {
        VisitResultDialog(
            result = visitResult,
            placeName = currentPlace.name,
            onDismiss = {
                visitViewModel.dismissResult()
                if (visitResult is VisitResult.VisitCreated || visitResult is VisitResult.VisitDuplicate) {
                    visitViewModel.reset()
                }
            },
            onRetry = {
                visitViewModel.dismissResult()
                visitViewModel.submitVisit(currentPlace.id)
            },
            onViewCollection = {
                visitViewModel.reset()
                showVisitSheet = false
                onNavigateToCollection()
            },
            onGoToAuth = {
                visitViewModel.reset()
                showVisitSheet = false
                onRequireAuth()
            },
        )
    }
}

@Composable
fun PlaceDetailScreen(
    uiState: PlaceDetailUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onOpenMapClick: () -> Unit,
    onRegisterVisitClick: (Place) -> Unit,
    onReportIncorrectDataClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmIvory,
        topBar = {
            TurinautaBackTopBar(title = "Ficha del lugar", onBackClick = onBackClick)
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = DeepTeal)
                }
            }

            uiState.place == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Lugar no encontrado",
                            style = MaterialTheme.typography.bodyLarge,
                            color = SlateDark,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = onBackClick,
                                modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                            ) {
                                Text("Volver al catálogo")
                            }
                            Button(
                                onClick = onRetryClick,
                                modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }

            else -> {
                PlaceDetailContent(
                    place = uiState.place,
                    existingVisit = uiState.existingVisit,
                    isAuthenticated = uiState.isAuthenticated,
                    onRegisterVisitClick = { onRegisterVisitClick(uiState.place) },
                    onReportIncorrectDataClick = onReportIncorrectDataClick,
                    onOpenMapClick = onOpenMapClick,
                    contentPadding = innerPadding,
                )
            }
        }
    }
}

@Composable
private fun PlaceDetailContent(
    place: Place,
    existingVisit: VisitRecord?,
    isAuthenticated: Boolean,
    onRegisterVisitClick: () -> Unit,
    onReportIncorrectDataClick: () -> Unit,
    onOpenMapClick: () -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = TurinautaDimens.ScreenHorizontalPadding, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Imagen o ilustración destacada del lugar
        PlaceHeroMedia(
            imagePath = place.imagePath,
            illustrationKey = place.illustrationKey,
        )

        // Encabezado editorial
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (place.categoryName.isNotBlank()) {
                Text(
                    text = place.categoryName.uppercase(Locale.forLanguageTag("es-CO")),
                    style = CategoryOverlineTextStyle,
                )
            }
            Text(
                text = place.name,
                style = MaterialTheme.typography.headlineSmall,
                color = SlateDark,
            )
            Text(
                text = place.description.ifBlank { place.summary },
                style = MaterialTheme.typography.bodyLarge,
                color = SlateMedium,
            )
        }

        // Tarjeta de información práctica (Horario R5, Ubicación, Condiciones)
        val infoShape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
        Surface(
            color = PureWhite,
            shape = infoShape,
            modifier = Modifier
                .fillMaxWidth()
                .border(TurinautaDimens.CardBorderWidth, SubtleOutline, infoShape),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailInfoRow(
                    icon = Icons.Outlined.Schedule,
                    label = "Horario de atención",
                    value = place.displayOpeningHours,
                )
                HorizontalDivider(
                    color = SubtleOutline,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
                DetailInfoRow(
                    icon = Icons.Outlined.LocationOn,
                    label = "Referencia de ubicación",
                    value = place.locationReference.ifBlank { "Popayán, Cauca" },
                )
                if (!place.visitConditions.isNullOrBlank()) {
                    HorizontalDivider(
                        color = SubtleOutline,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                    DetailInfoRow(
                        icon = Icons.Outlined.Info,
                        label = "Condiciones para la visita",
                        value = place.visitConditions,
                    )
                }
            }
        }

        // Vista previa pública: la ruta y el GPS se cargan al ampliar el mapa.
        PlaceLocationMap(
            latitude = place.latitude,
            longitude = place.longitude,
            placeName = place.name,
            addressReference = place.locationReference,
            imagePath = place.imagePath,
            onOpenMapClick = onOpenMapClick,
        )

        // Estado de visita (R4) o acción principal «Registrar visita»
        if (existingVisit != null) {
            Surface(
                color = SoftTealContainer,
                shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {},
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = DeepTeal,
                        modifier = Modifier.size(28.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Visita ya registrada",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSoftTealContainer,
                        )
                        if (!existingVisit.stampTitle.isNullOrBlank()) {
                            Text(
                                text = "Sello obtenido: ${existingVisit.stampTitle}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSoftTealContainer,
                            )
                        }
                    }
                }
            }
        } else {
            Button(
                onClick = onRegisterVisitClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepTeal,
                    contentColor = OnDeepTeal,
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
            ) {
                Text(
                    text = if (isAuthenticated) "Registrar visita" else "Iniciar sesión para registrar visita",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        // Acción secundaria discreta HU-24: Reportar dato incorrecto (funciona sin sesión)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            TextButton(
                onClick = onReportIncorrectDataClick,
                modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = null,
                    tint = SlateMedium,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Reportar dato incorrecto",
                    style = MaterialTheme.typography.labelLarge,
                    color = SlateMedium,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun PlaceHeroMedia(
    imagePath: String?,
    illustrationKey: IllustrationKey,
) {
    val resolvedUrl = remember(imagePath) {
        SupabaseConfig.resolvePublicMediaUrl(imagePath)
    }
    val shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(shape)
            .background(SurfaceThumbBeige),
        contentAlignment = Alignment.Center,
    ) {
        if (resolvedUrl == null) {
            PlaceIllustration(
                illustrationKey = illustrationKey,
                size = 136.dp,
                cornerRadius = 20.dp,
            )
        } else {
            SubcomposeAsyncImage(
                model = resolvedUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                loading = {
                    PlaceIllustration(illustrationKey = illustrationKey, size = 136.dp)
                },
                error = {
                    PlaceIllustration(illustrationKey = illustrationKey, size = 136.dp)
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DeepTeal,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = SlateMedium,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = SlateDark,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceDetailScreenPreview() {
    TurinautaTheme {
        PlaceDetailScreen(
            uiState = PlaceDetailUiState(
                place = Place(
                    id = "p1",
                    categoryId = "1",
                    categoryName = "Gastronomía",
                    name = "Cocina del Barrio",
                    slug = "cocina-del-barrio",
                    summary = "Recetas tradicionales de cocina caucana.",
                    description = "Espacio familiar dedicado a conservar recetas tradicionales de Popayán con ingredientes de productores locales.",
                    locationReference = "Sector histórico, calle 5 # 4-20",
                    latitude = 2.4419,
                    longitude = -76.6063,
                    openingHours = null,
                    visitConditions = "Toma una fotografía del espacio estando a menos de 200 m.",
                    illustrationKey = IllustrationKey.GASTRONOMIA,
                ),
                isAuthenticated = true,
                isLoading = false,
            ),
            onBackClick = {},
            onRetryClick = {},
            onOpenMapClick = {},
            onRegisterVisitClick = {},
            onReportIncorrectDataClick = {},
        )
    }
}
