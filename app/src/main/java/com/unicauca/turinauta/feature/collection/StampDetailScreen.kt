package com.unicauca.turinauta.feature.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.PlaceIllustration
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateDark
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SurfaceLowSand
import com.unicauca.turinauta.core.designsystem.SubtleOutline
import com.unicauca.turinauta.core.designsystem.TurinautaBackTopBar
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.designsystem.WarmIvory
import com.unicauca.turinauta.core.model.CollectionProgress
import com.unicauca.turinauta.core.model.IllustrationKey
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.RewardBadge
import com.unicauca.turinauta.core.network.SupabaseConfig
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.CollectionRepository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StampDetailRoute(
    stampId: String,
    collectionRepository: CollectionRepository,
    catalogRepository: CatalogRepository,
    authRepository: AuthRepository,
    onBackClick: () -> Unit,
    onPlaceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: StampDetailViewModel = viewModel(
        key = "stamp_detail_$stampId",
        factory = StampDetailViewModel.factory(stampId, collectionRepository, catalogRepository, authRepository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StampDetailScreen(uiState, onBackClick, viewModel::refresh, onPlaceClick, viewModel::selectStamp, modifier)
}

@Composable
fun StampDetailScreen(
    uiState: StampDetailUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onPlaceClick: (String) -> Unit,
    onStampClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmIvory,
        topBar = { TurinautaBackTopBar("Detalle del sello", onBackClick) },
        bottomBar = {
            if (uiState.badge != null) {
                Button(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = TurinautaDimens.ScreenHorizontalPadding, vertical = 12.dp),
                ) { Text("Volver a mis colecciones") }
            }
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = DeepTeal) }

            uiState.badge == null -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(uiState.errorMessage ?: "Sello no disponible", color = SlateDark)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onBackClick) { Text("Volver") }
                        Button(onClick = onRetryClick) { Text("Reintentar") }
                    }
                }
            }

            else -> StampDetailContent(uiState, onPlaceClick, onRetryClick, onStampClick, Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun StampDetailContent(
    uiState: StampDetailUiState,
    onPlaceClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onStampClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val badge = uiState.badge ?: return
    val scrollState = rememberScrollState()
    LaunchedEffect(badge.id) { scrollState.animateScrollTo(0) }
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(scrollState)
            .padding(horizontal = TurinautaDimens.ScreenHorizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
            val imageUrl = remember(badge.imagePath) { SupabaseConfig.resolvePublicMediaUrl(badge.imagePath) }
            if (imageUrl == null) {
                PlaceIllustration(badge.illustrationKey, size = 280.dp, cornerRadius = 24.dp)
            } else {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "Sello ${badge.title}",
                    contentScale = ContentScale.Fit,
                    loading = { PlaceIllustration(badge.illustrationKey, size = 280.dp, cornerRadius = 24.dp) },
                    error = { PlaceIllustration(badge.illustrationKey, size = 280.dp, cornerRadius = 24.dp) },
                    modifier = Modifier.size(280.dp),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(badge.title, style = MaterialTheme.typography.headlineSmall, color = SlateDark)
            Text(
                badge.description.ifBlank { "Un recuerdo de tu visita a este lugar." },
                style = MaterialTheme.typography.bodyLarge,
                color = SlateMedium,
            )
            HorizontalDivider(color = SubtleOutline)
            MetadataRow(Icons.Outlined.CalendarToday, badge.earnedAtMillis?.let { "Obtenido el ${formatEarnedDate(it)}" }
                ?: "Fecha de obtención no disponible")
            HorizontalDivider(color = SubtleOutline)
            uiState.collection?.let { MetadataRow(Icons.Outlined.CollectionsBookmark, "Colección: ${it.name}") }
            HorizontalDivider(color = SubtleOutline)
        }

        val cardShape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
        Surface(
            color = PureWhite,
            shape = cardShape,
            modifier = Modifier.fillMaxWidth().border(TurinautaDimens.CardBorderWidth, SubtleOutline, cardShape),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Lugar donde lo obtuviste", style = MaterialTheme.typography.titleMedium, color = SlateDark)
                when {
                    uiState.isPlaceLoading -> CircularProgressIndicator(
                        color = DeepTeal, strokeWidth = 2.dp, modifier = Modifier.size(24.dp),
                    )
                    uiState.place != null -> {
                        val place = uiState.place
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PlaceDetailThumbnail(place)
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(place.name, style = MaterialTheme.typography.titleSmall, color = SlateDark)
                                Text(place.categoryName, style = MaterialTheme.typography.bodySmall, color = SlateMedium)
                                Text(place.summary, style = MaterialTheme.typography.bodySmall, color = SlateMedium)
                            }
                        }
                        Button(onClick = { onPlaceClick(place.id) }) { Text("Ver ficha del lugar") }
                    }
                    else -> {
                        Text(
                            if (uiState.placeError) "No pudimos cargar el lugar. Intenta de nuevo."
                            else "La ficha del lugar no está disponible en este momento.",
                            style = MaterialTheme.typography.bodyMedium, color = SlateMedium,
                        )
                        if (uiState.placeError) TextButton(onClick = onRetryClick) { Text("Reintentar") }
                    }
                }
            }
        }
        uiState.collection?.let { collection ->
            CollectionProgressCard(collection, badge.id, onStampClick)
        }
    }
}

@Composable
private fun MetadataRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = DeepTeal, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = SlateMedium)
    }
}

@Composable
private fun CollectionProgressCard(
    collection: CollectionProgress,
    selectedStampId: String,
    onStampClick: (String) -> Unit,
) {
    val cardShape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
    Surface(
        color = PureWhite,
        shape = cardShape,
        modifier = Modifier.fillMaxWidth().border(TurinautaDimens.CardBorderWidth, SubtleOutline, cardShape),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Progreso de la colección: ${collection.name}", style = MaterialTheme.typography.titleMedium, color = SlateDark)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${collection.earnedStamps}/${collection.totalStamps} sellos", style = MaterialTheme.typography.bodyMedium, color = SlateMedium)
                Text("${collection.progressPercent}%", style = MaterialTheme.typography.bodyMedium, color = DeepTeal)
            }
            LinearProgressIndicator(
                progress = { collection.progressPercent.coerceIn(0, 100) / 100f },
                color = DeepTeal,
                trackColor = SurfaceLowSand,
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
            )
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                collection.stamps.sortedBy { it.sortOrder }.forEach { stamp ->
                    ProgressStamp(stamp, selectedStampId == stamp.id, onStampClick)
                }
            }
        }
    }
}

@Composable
private fun ProgressStamp(badge: RewardBadge, selected: Boolean, onStampClick: (String) -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    val imageUrl = remember(badge.imagePath) { SupabaseConfig.resolvePublicMediaUrl(badge.imagePath) }
    Box(
        modifier = Modifier.size(62.dp)
            .clip(shape)
            .background(SurfaceLowSand)
            .then(if (badge.isEarned) Modifier.clickable { onStampClick(badge.id) } else Modifier)
            .border(if (selected) 2.dp else 1.dp, if (selected) DeepTeal else SubtleOutline, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl == null) {
            PlaceIllustration(badge.illustrationKey, size = 56.dp, cornerRadius = 10.dp,
                modifier = Modifier.alpha(if (badge.isEarned) 1f else 0.5f))
        } else {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = if (badge.isEarned) badge.title else null,
                contentScale = ContentScale.Crop,
                loading = { PlaceIllustration(badge.illustrationKey, size = 56.dp, cornerRadius = 10.dp) },
                error = { PlaceIllustration(badge.illustrationKey, size = 56.dp, cornerRadius = 10.dp) },
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp))
                    .alpha(if (badge.isEarned) 1f else 0.5f),
            )
        }
        if (!badge.isEarned) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Lock, contentDescription = "${badge.title}, pendiente", tint = PureWhite,
                    modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun PlaceDetailThumbnail(place: Place) {
    val imageUrl = remember(place.imagePath) { SupabaseConfig.resolvePublicMediaUrl(place.imagePath) }
    if (imageUrl == null) {
        PlaceIllustration(place.illustrationKey, size = 88.dp, cornerRadius = 16.dp)
    } else {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            loading = { PlaceIllustration(place.illustrationKey, size = 88.dp, cornerRadius = 16.dp) },
            error = { PlaceIllustration(place.illustrationKey, size = 88.dp, cornerRadius = 16.dp) },
            modifier = Modifier.size(88.dp).clip(RoundedCornerShape(16.dp)),
        )
    }
}

private fun formatEarnedDate(millis: Long): String = DateTimeFormatter
    .ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CO"))
    .format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))

@Preview(showBackground = true)
@Composable
private fun StampDetailPreview() {
    TurinautaTheme {
        StampDetailScreen(
            uiState = StampDetailUiState(
                badge = RewardBadge("stamp-1", "Sabores de Fogón", "Un recuerdo de los sabores tradicionales de Popayán.",
                    placeId = "place-1", illustrationKey = IllustrationKey.GASTRONOMIA, isEarned = true),
                collection = CollectionProgress("c1", "Sabores y Café", "sabores-y-cafe", "", totalStamps = 2,
                    earnedStamps = 1, stamps = listOf(
                        RewardBadge("stamp-1", "Sabores de Fogón", "", isEarned = true),
                        RewardBadge("stamp-2", "Café", ""),
                    )),
                place = Place("place-1", "cat-1", "Gastronomía", "Cocina del Barrio", "cocina-del-barrio",
                    "Cocina local tradicional", "Cocina local tradicional", "Centro de Popayán", 2.4419, -76.6063),
                isLoading = false,
            ),
            onBackClick = {}, onRetryClick = {}, onPlaceClick = {}, onStampClick = {},
        )
    }
}
