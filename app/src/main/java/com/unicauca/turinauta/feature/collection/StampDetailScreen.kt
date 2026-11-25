package com.unicauca.turinauta.feature.collection

import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.MutedOchre
import com.unicauca.turinauta.core.designsystem.OnSoftTealContainer
import com.unicauca.turinauta.core.designsystem.PlaceIllustration
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateDark
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SoftTealContainer
import com.unicauca.turinauta.core.designsystem.SubtleOutline
import com.unicauca.turinauta.core.designsystem.TurinautaBackTopBar
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.designsystem.WarmIvory
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
    StampDetailScreen(uiState, onBackClick, viewModel::refresh, onPlaceClick, modifier)
}

@Composable
fun StampDetailScreen(
    uiState: StampDetailUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onPlaceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmIvory,
        topBar = { TurinautaBackTopBar("Detalle del sello", onBackClick) },
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

            else -> StampDetailContent(uiState, onPlaceClick, onRetryClick, Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun StampDetailContent(
    uiState: StampDetailUiState,
    onPlaceClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val badge = uiState.badge ?: return
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = TurinautaDimens.ScreenHorizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Surface(
            color = SoftTealContainer,
            shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
                val imageUrl = remember(badge.imagePath) { SupabaseConfig.resolvePublicMediaUrl(badge.imagePath) }
                if (imageUrl == null) {
                    PlaceIllustration(badge.illustrationKey, size = 220.dp, cornerRadius = 24.dp)
                } else {
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = "Sello ${badge.title}",
                        contentScale = ContentScale.Fit,
                        loading = { PlaceIllustration(badge.illustrationKey, size = 220.dp, cornerRadius = 24.dp) },
                        error = { PlaceIllustration(badge.illustrationKey, size = 220.dp, cornerRadius = 24.dp) },
                        modifier = Modifier.size(240.dp).clip(RoundedCornerShape(20.dp)),
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Icon(Icons.Rounded.WorkspacePremium, contentDescription = null,
                    tint = MutedOchre, modifier = Modifier.size(20.dp))
                Text("SELLO OBTENIDO", style = MaterialTheme.typography.labelLarge, color = DeepTeal)
            }
            Text(badge.title, style = MaterialTheme.typography.headlineSmall, color = SlateDark)
            Text(
                badge.description.ifBlank { "Un recuerdo de tu visita a este lugar." },
                style = MaterialTheme.typography.bodyLarge,
                color = SlateMedium,
            )
            if (uiState.collectionName.isNotBlank()) {
                Text("Colección: ${uiState.collectionName}", style = MaterialTheme.typography.bodySmall, color = DeepTeal)
            }
            badge.earnedAtMillis?.let { earnedAt ->
                Text("Obtenido el ${formatEarnedDate(earnedAt)}", style = MaterialTheme.typography.bodySmall,
                    color = SlateMedium)
            }
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
                        TextButton(onClick = { onPlaceClick(place.id) }) { Text("Ver ficha del lugar") }
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
                collectionName = "Sabores y Café",
                place = Place("place-1", "cat-1", "Gastronomía", "Cocina del Barrio", "cocina-del-barrio",
                    "Cocina local tradicional", "Cocina local tradicional", "Centro de Popayán", 2.4419, -76.6063),
                isLoading = false,
            ),
            onBackClick = {}, onRetryClick = {}, onPlaceClick = {},
        )
    }
}
