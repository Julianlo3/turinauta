package com.unicauca.turinauta.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.unicauca.turinauta.core.model.Illustration

/**
 * Foto del catálogo (bucket `catalog-images`) con la ilustración de la categoría debajo:
 * se ve mientras carga, si falla la red o si el lugar aún no tiene imagen.
 * Coil la reduce al tamaño de la vista y la guarda en caché de disco.
 */
@Composable
fun CatalogImage(
    imageUrl: String?,
    illustration: Illustration,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    fallback: @Composable (Modifier) -> Unit = { PlaceIllustration(illustration, it) },
) {
    Box(modifier) {
        fallback(Modifier.matchParentSize())
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}
