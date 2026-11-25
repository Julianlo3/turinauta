package com.unicauca.turinauta.feature.explore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unicauca.turinauta.core.designsystem.CardShape
import com.unicauca.turinauta.core.designsystem.ChipShape
import com.unicauca.turinauta.core.designsystem.EmptyState
import com.unicauca.turinauta.core.designsystem.PillShape
import com.unicauca.turinauta.core.designsystem.PlaceCard
import com.unicauca.turinauta.core.designsystem.TurinautaLogo
import com.unicauca.turinauta.core.model.Category
import com.unicauca.turinauta.core.navigation.TurinautaViewModelFactory
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.outlined.Info
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.unicauca.turinauta.core.designsystem.LocalSnackbarHostState
import com.unicauca.turinauta.core.designsystem.NoteBox

@Composable
fun ExploreScreen(
    onOpenPlace: (String) -> Unit,
    onOpenRoutes: () -> Unit,
    viewModel: ExploreViewModel = viewModel(factory = TurinautaViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSort by rememberSaveable { mutableStateOf(false) }
    val snackbar = LocalSnackbarHostState.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        viewModel.onNearestPermissionResult(result.values.any { it })
    }

    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it, actionLabel = "Entendido"); viewModel.onMessageShown() }
    }

    // Refresca al volver a la pestaña (p. ej. tras cambiar el Modo pruebas).
    LaunchedEffect(Unit) { viewModel.load() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ExploreTopBar() }
        item { SearchField(state.query, viewModel::onQueryChange, viewModel::clearQuery) }
        item { CategoryChips(state.selectedCategory, viewModel::selectCategory) }

        if (!state.isSearching && state.selectedCategory == null && !state.isCatalogEmpty) {
            item { RoutesBanner(onOpenRoutes) }
        }
        if (state.locationUnavailable) {
            item {
                NoteBox(
                    Icons.Outlined.Info,
                    "No tenemos acceso a tu ubicación, así que mostramos el orden habitual. Puedes seguir explorando con normalidad.",
                )
            }
        }
        if (state.isLocating) {
            item { androidx.compose.material3.LinearProgressIndicator(Modifier.fillMaxWidth()) }
        }

        when {
            state.isLoading -> item {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
            state.isCatalogEmpty -> item {
                EmptyState(
                    icon = Icons.Outlined.Map,
                    title = "Aún no hay lugares disponibles",
                    message = "Estamos preparando el catálogo de esta ciudad. Vuelve a cargar más tarde para ver los lugares que se publiquen.",
                    primaryAction = "Volver a cargar" to viewModel::load,
                    outlinedPrimary = true,
                )
            }
            else -> placesSection(state, viewModel, onOpenPlace, onSort = { showSort = true })
        }
    }

    if (showSort) {
        SortSheet(
            current = state.sortOrder,
            onSelect = { viewModel.setSortOrder(it); showSort = false },
            onRequestNearest = {
                showSort = false
                if (viewModel.hasLocationPermission()) viewModel.onNearestPermissionResult(true)
                else permissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                )
            },
            onDismiss = { showSort = false },
        )
    }
}

private fun LazyListScope.placesSection(
    state: ExploreUiState,
    viewModel: ExploreViewModel,
    onOpenPlace: (String) -> Unit,
    onSort: () -> Unit,
) {
    val places = state.visiblePlaces
    item {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val title = when {
                state.isSearching -> "${places.size} ${if (places.size == 1) "lugar coincide" else "lugares coinciden"} con «${state.query.trim()}»"
                state.selectedCategory != null -> state.selectedCategory.label
                else -> "Lugares para descubrir"
            }
            Text(
                title,
                style = if (state.isSearching) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
                color = if (state.isSearching) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (state.selectedCategory != null && !state.isSearching) {
                TextButton(onClick = { viewModel.selectCategory(null) }) {
                    Icon(Icons.Outlined.Close, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Quitar filtro")
                }
            } else if (places.isNotEmpty()) {
                TextButton(onClick = onSort) {
                    Icon(Icons.Outlined.SwapVert, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (state.sortOrder == SortOrder.FEATURED) "Ordenar" else state.sortOrder.label.removeSuffix(" primero"))
                }
            }
        }
    }

    if (places.isEmpty()) {
        item {
            if (state.isSearching) {
                EmptyState(
                    icon = Icons.Outlined.Search,
                    title = "No encontramos «${state.query.trim()}»",
                    message = "Ningún lugar disponible tiene ese nombre. Revisa la escritura, prueba con otra palabra o explora por categoría.",
                    primaryAction = "Borrar búsqueda" to viewModel::clearQuery,
                    outlinedPrimary = true,
                    secondaryAction = "Explorar por categoría" to { viewModel.clearQuery(); viewModel.selectCategory(null) },
                )
            } else {
                EmptyState(
                    icon = Icons.Outlined.Explore,
                    title = "Aún no hay lugares en ${state.selectedCategory?.label ?: "esta categoría"}",
                    message = "Esta categoría todavía no tiene lugares disponibles. Mientras tanto, puedes explorar el catálogo completo.",
                    primaryAction = "Volver al catálogo" to { viewModel.selectCategory(null) },
                )
            }
        }
    } else {
        items(places, key = { it.id }) { place ->
            PlaceCard(place, onClick = { onOpenPlace(place.id) }, distanceMeters = state.distanceTo(place))
        }
    }
}

@Composable
private fun ExploreTopBar() {
    Row(
        Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TurinautaLogo(size = 34.dp)
        Spacer(Modifier.width(10.dp))
        Text("Turinauta", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        Surface(
            shape = PillShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.semantics(mergeDescendants = true) {},
        ) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Place, contentDescription = "Ciudad", Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Popayán", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit, onClear: () -> Unit) {
    val focus = LocalFocusManager.current
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).clip(PillShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text("Buscar lugares por nombre", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            BasicTextField(
                value = query,
                onValueChange = onChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                modifier = Modifier.fillMaxWidth().semantics { },
            )
        }
        if (query.isNotEmpty()) {
            IconButton(onClick = onClear) { Icon(Icons.Outlined.Close, contentDescription = "Limpiar búsqueda") }
        } else {
            Spacer(Modifier.size(48.dp))
        }
    }
}

@Composable
private fun CategoryChips(selected: Category?, onSelect: (Category?) -> Unit) {
    val options = listOf<Category?>(null) + Category.entries
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options) { category ->
            val isSelected = category == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(category) },
                label = { Text(category?.label ?: "Todos") },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(18.dp)) }
                } else null,
                shape = ChipShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                modifier = Modifier.heightIn(min = 48.dp),
            )
        }
    }
}

@Composable
private fun RoutesBanner(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Rutas temáticas", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Recorridos por sabores, oficios y cultura local.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortSheet(
    current: SortOrder,
    onSelect: (SortOrder) -> Unit,
    onRequestNearest: () -> Unit,
    onDismiss: () -> Unit,
) {
    var pending by rememberSaveable { mutableStateOf(current) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 20.dp).navigationBarsPadding().padding(bottom = 16.dp)) {
            Text("Ordenar lugares", style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal))
            Spacer(Modifier.height(12.dp))
            Column(Modifier.selectableGroup()) {
                SortOrder.entries.forEach { order ->
                    Row(
                        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
                            .background(if (order == pending) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                            .selectable(selected = order == pending, role = Role.RadioButton, onClick = {
                                pending = order
                                if (order != SortOrder.NEAREST) onSelect(order)
                            })
                            .heightIn(min = 56.dp).padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = order == pending, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(order.label, style = MaterialTheme.typography.bodyLarge)
                            Text(order.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
            if (pending == SortOrder.NEAREST && current != SortOrder.NEAREST) {
                NoteBox(
                    Icons.Outlined.Info,
                    "Para ordenar por cercanía, Android te pedirá permiso de ubicación. Solo la usamos mientras ves el catálogo y no la guardamos. Si no la concedes, puedes seguir explorando con el orden habitual.",
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) { Text("Ahora no") }
                    Spacer(Modifier.width(8.dp))
                    androidx.compose.material3.Button(onClick = onRequestNearest, modifier = Modifier.heightIn(min = 48.dp)) { Text("Continuar") }
                }
            }
        }
    }
}
