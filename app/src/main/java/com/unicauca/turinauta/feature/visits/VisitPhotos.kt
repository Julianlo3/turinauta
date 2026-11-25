package com.unicauca.turinauta.feature.visits

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.unicauca.turinauta.core.model.VisitPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** Archivo temporal en caché donde la cámara guarda la foto de la visita. */
fun Context.newCameraPhotoUri(): Uri {
    val dir = File(cacheDir, "visit_photos").apply { mkdirs() }
    val file = File(dir, "visita-${UUID.randomUUID()}.jpg")
    return FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
}

/** Lee tipo y tamaño de la imagen elegida (para validar JPG/PNG/WebP ≤ 5 MB). */
fun Context.toVisitPhoto(uri: Uri): VisitPhoto? {
    val type = contentResolver.getType(uri) ?: return null
    var size = -1L
    contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst() && !cursor.isNull(0)) size = cursor.getLong(0)
    }
    if (size <= 0) size = contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0
    if (size <= 0) return null
    return VisitPhoto(uri = uri.toString(), mimeType = type, sizeBytes = size)
}

/** Vista previa liviana de la foto elegida (submuestreada para no gastar memoria). */
@Composable
fun PhotoPreview(uri: String, contentDescription: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(null, uri) {
        value = withContext(Dispatchers.IO) { decodeSampled(context, Uri.parse(uri), 600) }
    }
    bitmap?.let {
        Image(it.asImageBitmap(), contentDescription = contentDescription, modifier = modifier, contentScale = ContentScale.Crop)
    }
}

private fun decodeSampled(context: Context, uri: Uri, maxSide: Int): Bitmap? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (bounds.outWidth / sample > maxSide || bounds.outHeight / sample > maxSide) sample *= 2
    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
}.getOrNull()
