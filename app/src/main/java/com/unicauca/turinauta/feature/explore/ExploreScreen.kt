package com.unicauca.turinauta.feature.explore

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.AltRoute
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.unicauca.turinauta.core.designsystem.CategoryOverlineTextStyle
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.OnSoftTealContainer
import com.unicauca.turinauta.core.designsystem.PlaceIllustration
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateDark
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SoftTealContainer
import com.unicauca.turinauta.core.designsystem.SubtleOutline
import com.unicauca.turinauta.core.designsystem.SurfaceLowSand
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaLogoMark
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.designsystem.WarmIvory
import com.unicauca.turinauta.core.designsystem.WarmOutline
import com.unicauca.turinauta.core.model.Category
import com.unicauca.turinauta.core.model.IllustrationKey
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.PlaceSortOrder
import com.unicauca.turinauta.core.network.SupabaseConfig
import com.unicauca.turinauta.data.repository.CatalogRepository
import java.util.Locale

@Composable
fun ExploreRoute(
    catalogRepository: CatalogRepository,
    onPlaceClick: (String) -> Unit,
    onRoutesBannerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ExploreViewModel = viewModel(
        factory = ExploreViewModel.factory(catalogRepository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val location = readLastKnownLocation(context)
            if (location != null) {
                viewModel.onUserLocationResolved(location.latitude, location.longitude)
            } else {
                viewModel.onLocationUnavailable(
                    "No obtuvimos una ubicación reciente. Activa el GPS o usa otro criterio de ordenamiento.",
                )
            }
        } else {
            viewModel.onLocationUnavailable(
                "Sin permiso de ubicación puedes seguir explorando por Destacados o Nombre (A–Z).",
            )
        }
    }

    ExploreScreen(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onCategorySelected = viewModel::onCategorySelected,
        onSortOrderSelected = { order ->
            if (order == PlaceSortOrder.NEAREST) {
                if (hasLocationPermission(context)) {
                    val location = readLastKnownLocation(context)
                    if (location != null) {
                        viewModel.onUserLocationResolved(location.latitude, location.longitude)
                    } else {
                        viewModel.onLocationUnavailable(
                            "No obtuvimos una ubicación reciente. Activa el GPS o elige otro orden.",
                        )
                    }
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        ),
                    )
                }
            } else {
                viewModel.onSortOrderSelected(order)
            }
        },
        onRoutesBannerClick = onRoutesBannerClick,
        onPlaceClick = onPlaceClick,
        onRetryClick = viewModel::loadCatalog,
        onClearFiltersClick = viewModel::clearFilters,
        onDismissLocationNotice = viewModel::dismissLocationNotice,
        modifier = modifier,
    )
}

@Composable
fun ExploreScreen(
    uiState: ExploreUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onSortOrderSelected: (PlaceSortOrder) -> Unit,
    onRoutesBannerClick: () -> Unit,
    onPlaceClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onClearFiltersClick: () -> Unit,
    onDismissLocationNotice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory),
        contentPadding = PaddingValues(
            start = TurinautaDimens.ScreenHorizontalPadding,
            end = TurinautaDimens.ScreenHorizontalPadding,
            top = 12.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 1. TopBar: logo + «Turinauta» (izq.) y píldora «Popayán» (der.)
        item(key = "top_bar") {
            ExploreTopBar()
        }

        // 2. Buscador pill (#F1EEE7)
        item(key = "search_bar") {
            ExploreSearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
            )
        }

        // 3. LazyRow de chips de categorías
        item(key = "category_chips") {
            CategoryChipsRow(
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                onCategorySelected = onCategorySelected,
            )
        }

        // 4. Banner «Rutas temáticas»
        item(key = "routes_banner") {
            ThemedRoutesBanner(onClick = onRoutesBannerClick)
        }

        // Aviso opcional de ubicación (solo si el usuario probó «Más cercanos» sin GPS)
        if (uiState.locationNotice != null) {
            item(key = "location_notice") {
                Surface(
                    color = SoftTealContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = uiState.locationNotice,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSoftTealContainer,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = onDismissLocationNotice,
                            modifier = Modifier.size(TurinautaDimens.MinTouchTarget),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Cerrar aviso",
                                tint = OnSoftTealContainer,
                            )
                        }
                    }
                }
            }
        }

        // 5. Cabecera «Lugares para descubrir» + «↑↓ Ordenar»
        item(key = "section_header") {
            PlacesSectionHeader(
                selectedSortOrder = uiState.sortOrder,
                onSortOrderSelected = onSortOrderSelected,
            )
        }

        // 6. Contenido: cargando / error / vacío / listado de tarjetas
        when {
            uiState.isLoading -> {
                item(key = "loading_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CircularProgressIndicator(color = DeepTeal)
                            Text(
                                text = "Cargando lugares de Popayán...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateMedium,
                            )
                        }
                    }
                }
            }

            uiState.errorMessage != null -> {
                item(key = "error_state") {
                    ExploreStateCard(
                        title = if (uiState.isBackendNotConfigured) {
                            "Falta conectar el backend"
                        } else {
                            "No pudimos cargar los lugares"
                        },
                        message = uiState.errorMessage,
                        actionLabel = "Reintentar",
                        onActionClick = onRetryClick,
                    )
                }
            }

            uiState.places.isEmpty() -> {
                item(key = "empty_state") {
                    ExploreStateCard(
                        title = if (uiState.hasActiveFilters) {
                            "Sin resultados para tu búsqueda"
                        } else {
                            "Aún no hay lugares publicados"
                        },
                        message = if (uiState.hasActiveFilters) {
                            "Prueba con otro nombre o vuelve a ver todas las categorías disponibles."
                        } else {
                            "Cuando el equipo editorial publique lugares aparecerán aquí."
                        },
                        actionLabel = if (uiState.hasActiveFilters) "Limpiar búsqueda" else "Reintentar",
                        onActionClick = if (uiState.hasActiveFilters) onClearFiltersClick else onRetryClick,
                    )
                }
            }

            else -> {
                items(
                    items = uiState.places,
                    key = { it.id },
                ) { place ->
                    PlaceListCard(
                        place = place,
                        onClick = { onPlaceClick(place.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExploreTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TurinautaLogoMark(size = 36.dp)
            Text(
                text = "Turinauta",
                style = MaterialTheme.typography.titleLarge,
                color = SlateDark,
            )
        }

        Surface(
            color = WarmIvory,
            shape = RoundedCornerShape(TurinautaDimens.PillCornerRadius),
            border = BorderStroke(1.dp, WarmOutline),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = DeepTeal,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Popayán",
                    style = MaterialTheme.typography.labelMedium,
                    color = SlateDark,
                )
            }
        }
    }
}

@Composable
private fun ExploreSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Surface(
        color = SurfaceLowSand,
        shape = RoundedCornerShape(TurinautaDimens.PillCornerRadius),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = SlateMedium,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Buscar lugares por nombre",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = SlateDark),
                    cursorBrush = SolidColor(DeepTeal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(TurinautaDimens.MinTouchTarget),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Limpiar búsqueda",
                        tint = SlateMedium,
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}

@Composable
private fun CategoryChipsRow(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "cat_all") {
            CategoryFilterPill(
                label = "Todos",
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
            )
        }
        items(
            items = categories,
            key = { it.id },
        ) { category ->
            CategoryFilterPill(
                label = category.name,
                selected = selectedCategoryId == category.id,
                onClick = { onCategorySelected(category.id) },
            )
        }
    }
}

@Composable
private fun CategoryFilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (selected) SoftTealContainer else PureWhite
    val contentColor = if (selected) OnSoftTealContainer else SlateDark
    val border = if (selected) null else BorderStroke(1.dp, WarmOutline)

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(TurinautaDimens.ChipCornerRadius),
        border = border,
        modifier = Modifier
            .heightIn(min = TurinautaDimens.MinTouchTarget)
            .clip(RoundedCornerShape(TurinautaDimens.ChipCornerRadius))
            .clickable(role = Role.Tab, onClick = onClick)
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
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun ThemedRoutesBanner(
    onClick: () -> Unit,
) {
    Surface(
        color = SurfaceLowSand,
        shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TurinautaDimens.CardCornerRadius))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SoftTealContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.AltRoute,
                    contentDescription = null,
                    tint = DeepTeal,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Rutas temáticas",
                    style = MaterialTheme.typography.titleMedium,
                    color = SlateDark,
                )
                Text(
                    text = "Recorridos sugeridos para descubrir oficios y sabores",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = SlateMedium,
            )
        }
    }
}

@Composable
private fun PlacesSectionHeader(
    selectedSortOrder: PlaceSortOrder,
    onSortOrderSelected: (PlaceSortOrder) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Lugares para descubrir",
            style = MaterialTheme.typography.titleMedium,
            color = SlateDark,
        )

        Box {
            TextButton(
                onClick = { expanded = true },
                modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
            ) {
                Icon(
                    imageVector = Icons.Rounded.SwapVert,
                    contentDescription = null,
                    tint = DeepTeal,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Ordenar",
                    style = MaterialTheme.typography.labelLarge,
                    color = DeepTeal,
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = PureWhite,
            ) {
                SortOptionMenuItem(
                    label = "Destacados",
                    selected = selectedSortOrder == PlaceSortOrder.FEATURED,
                    onClick = {
                        expanded = false
                        onSortOrderSelected(PlaceSortOrder.FEATURED)
                    },
                )
                SortOptionMenuItem(
                    label = "Nombre (A–Z)",
                    selected = selectedSortOrder == PlaceSortOrder.NAME_ASC,
                    onClick = {
                        expanded = false
                        onSortOrderSelected(PlaceSortOrder.NAME_ASC)
                    },
                )
                SortOptionMenuItem(
                    label = "Más cercanos",
                    selected = selectedSortOrder == PlaceSortOrder.NEAREST,
                    onClick = {
                        expanded = false
                        onSortOrderSelected(PlaceSortOrder.NEAREST)
                    },
                )
            }
        }
    }
}

@Composable
private fun SortOptionMenuItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) DeepTeal else SlateDark,
            )
        },
        trailingIcon = {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Seleccionado",
                    tint = DeepTeal,
                    modifier = Modifier.size(18.dp),
                )
            }
        },
        onClick = onClick,
    )
}

@Composable
private fun PlaceListCard(
    place: Place,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
    Surface(
        color = PureWhite,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .border(TurinautaDimens.CardBorderWidth, SubtleOutline, shape)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            PlaceThumbnail(
                imagePath = place.imagePath,
                illustrationKey = place.illustrationKey,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (place.categoryName.isNotBlank()) {
                    Text(
                        text = place.categoryName.uppercase(Locale.forLanguageTag("es-CO")),
                        style = CategoryOverlineTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = SlateDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = place.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = SlateMedium,
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = place.displayOpeningHours,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceThumbnail(
    imagePath: String?,
    illustrationKey: IllustrationKey,
    modifier: Modifier = Modifier,
) {
    val resolvedUrl = remember(imagePath) {
        SupabaseConfig.resolvePublicMediaUrl(imagePath)
    }
    if (resolvedUrl == null) {
        PlaceIllustration(
            illustrationKey = illustrationKey,
            size = TurinautaDimens.ThumbnailSize,
            cornerRadius = TurinautaDimens.ThumbnailCornerRadius,
            modifier = modifier,
        )
    } else {
        SubcomposeAsyncImage(
            model = resolvedUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            loading = {
                PlaceIllustration(
                    illustrationKey = illustrationKey,
                    size = TurinautaDimens.ThumbnailSize,
                    cornerRadius = TurinautaDimens.ThumbnailCornerRadius,
                )
            },
            error = {
                PlaceIllustration(
                    illustrationKey = illustrationKey,
                    size = TurinautaDimens.ThumbnailSize,
                    cornerRadius = TurinautaDimens.ThumbnailCornerRadius,
                )
            },
            modifier = modifier
                .size(TurinautaDimens.ThumbnailSize)
                .clip(RoundedCornerShape(TurinautaDimens.ThumbnailCornerRadius)),
        )
    }
}

@Composable
private fun ExploreStateCard(
    title: String,
    message: String,
    actionLabel: String,
    onActionClick: () -> Unit,
) {
    val shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
    Surface(
        color = PureWhite,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .border(TurinautaDimens.CardBorderWidth, SubtleOutline, shape),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = SlateDark,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = SlateMedium,
            )
            Spacer(modifier = Modifier.height(2.dp))
            OutlinedButton(
                onClick = onActionClick,
                modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
            ) {
                Text(text = actionLabel)
            }
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

@SuppressLint("MissingPermission")
private fun readLastKnownLocation(context: Context): Location? {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    return providers
        .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
        .maxByOrNull { it.time }
}

@Preview(showBackground = true)
@Composable
private fun ExploreScreenPreview() {
    TurinautaTheme {
        ExploreScreen(
            uiState = ExploreUiState(
                categories = listOf(
                    Category("1", "Gastronomía", "gastronomia", 1),
                    Category("2", "Artesanías", "artesanias", 2),
                    Category("3", "Cultura", "cultura", 3),
                    Category("4", "Emprendimientos", "emprendimientos", 4),
                ),
                places = listOf(
                    Place(
                        id = "p1",
                        categoryId = "1",
                        categoryName = "Gastronomía",
                        name = "Cocina del Barrio",
                        slug = "cocina-del-barrio",
                        summary = "Recetas tradicionales de cocina caucana preparadas en fogón de barrio.",
                        description = "Descripción completa",
                        locationReference = "Sector histórico",
                        latitude = 2.4419,
                        longitude = -76.6063,
                        openingHours = null,
                        illustrationKey = IllustrationKey.GASTRONOMIA,
                    ),
                    Place(
                        id = "p2",
                        categoryId = "2",
                        categoryName = "Artesanías",
                        name = "Taller de tejido",
                        slug = "taller-de-tejido",
                        summary = "Piezas elaboradas en telar manual con fibras naturales de la región.",
                        description = "Descripción completa",
                        locationReference = "Calle de los oficios",
                        latitude = 2.4442,
                        longitude = -76.6021,
                        openingHours = "Lun a vie, 9:00–12:00",
                        illustrationKey = IllustrationKey.ARTESANIAS,
                    ),
                ),
                isLoading = false,
            ),
            onSearchQueryChange = {},
            onCategorySelected = {},
            onSortOrderSelected = {},
            onRoutesBannerClick = {},
            onPlaceClick = {},
            onRetryClick = {},
            onClearFiltersClick = {},
            onDismissLocationNotice = {},
        )
    }
}
