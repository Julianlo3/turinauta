package com.unicauca.turinauta.core.navigation

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/** Cambia de pestaña solo después de un gesto horizontal deliberado en una pantalla principal. */
internal fun Modifier.topLevelSwipe(
    current: TopLevelDestination?,
    onDestinationSelected: (TopLevelDestination) -> Unit,
): Modifier {
    if (current == null) return this

    return pointerInput(current) {
        val minimumDistance = maxOf(80.dp.toPx(), size.width * 0.22f)
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            var distanceX = 0f
            var distanceY = 0f
            var handledByChild = false

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Final)
                if (event.changes.any { it.id != down.id && it.pressed }) {
                    handledByChild = true
                }
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                val movement = change.position - change.previousPosition
                distanceX += movement.x
                distanceY += movement.y

                // Un carrusel o una lista desplazable conserva su propio gesto.
                if (change.pressed && change.isConsumed && movement != androidx.compose.ui.geometry.Offset.Zero) {
                    handledByChild = true
                }
                if (!change.pressed) {
                    if (!handledByChild && abs(distanceX) >= minimumDistance &&
                        abs(distanceX) > abs(distanceY) * 1.4f
                    ) {
                        val nextIndex = current.ordinal + if (distanceX < 0f) 1 else -1
                        TopLevelDestination.entries.getOrNull(nextIndex)?.let(onDestinationSelected)
                    }
                    break
                }
            }
        }
    }
}
