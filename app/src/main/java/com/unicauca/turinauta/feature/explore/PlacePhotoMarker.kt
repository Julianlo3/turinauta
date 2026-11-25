package com.unicauca.turinauta.feature.explore

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.CancellationException
import kotlin.math.min
import kotlin.math.roundToInt

/** Dibuja un pin oscuro con la foto del lugar dentro de su círculo superior. */
internal object PlacePhotoMarker {
    suspend fun loadPhoto(context: Context, imageUrl: String?): Bitmap? {
        if (imageUrl == null) return null
        return try {
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .size(128)
                .allowHardware(false)
                .build()
            (context.imageLoader.execute(request) as? SuccessResult)?.drawable?.toBitmap()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            null
        }
    }

    fun create(context: Context, photo: Bitmap?): BitmapDrawable {
        val density = context.resources.displayMetrics.density
        val bitmap = Bitmap.createBitmap(
            (64 * density).roundToInt(),
            (86 * density).roundToInt(),
            Bitmap.Config.ARGB_8888,
        )
        val canvas = Canvas(bitmap)
        canvas.scale(density, density)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }
        val pin = Path().apply {
            moveTo(32f, 84f)
            cubicTo(21f, 68f, 3f, 49f, 3f, 32f)
            cubicTo(3f, 15f, 16f, 3f, 32f, 3f)
            cubicTo(48f, 3f, 61f, 15f, 61f, 32f)
            cubicTo(61f, 49f, 43f, 68f, 32f, 84f)
            close()
        }
        canvas.drawPath(pin, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(32f, 32f, 25f, paint)

        if (photo != null) {
            val softwarePhoto = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && photo.config == Bitmap.Config.HARDWARE) {
                photo.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                photo
            }
            canvas.save()
            val circle = Path().apply { addCircle(32f, 32f, 21f, Path.Direction.CW) }
            canvas.clipPath(circle)
            val crop = min(softwarePhoto.width, softwarePhoto.height)
            val source = Rect(
                (softwarePhoto.width - crop) / 2,
                (softwarePhoto.height - crop) / 2,
                (softwarePhoto.width + crop) / 2,
                (softwarePhoto.height + crop) / 2,
            )
            canvas.drawBitmap(softwarePhoto, source, RectF(11f, 11f, 53f, 53f), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            canvas.restore()
        } else {
            paint.color = Color.BLACK
            canvas.drawCircle(32f, 32f, 7f, paint)
        }
        return BitmapDrawable(context.resources, bitmap)
    }
}
