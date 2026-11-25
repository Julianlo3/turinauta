package com.unicauca.turinauta.feature.visits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.unicauca.turinauta.core.designsystem.CardShape
import com.unicauca.turinauta.core.designsystem.OverlineStyle
import com.unicauca.turinauta.core.designsystem.PillShape
import com.unicauca.turinauta.core.designsystem.PrimaryWideButton
import com.unicauca.turinauta.core.designsystem.StampArt
import com.unicauca.turinauta.core.designsystem.StampStyle
import com.unicauca.turinauta.core.designsystem.stampStyleFor
import com.unicauca.turinauta.core.model.Illustration
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.unicauca.turinauta.core.designsystem.formatShortDate
import com.unicauca.turinauta.core.model.VisitResult

/** Resultado exitoso: visita registrada y sello obtenido (HU-18, HU-20). */
@Composable
fun VisitResultDialog(
    result: VisitResult.VisitCreated,
    onOpenCollection: () -> Unit,
    onBackToPlace: () -> Unit,
) {
    Dialog(onDismissRequest = onBackToPlace, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.safeDrawingPadding().padding(horizontal = 20.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onBackToPlace) { Icon(Icons.Outlined.Close, contentDescription = "Cerrar") }
                }
                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(shape = PillShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Visita registrada · ${formatShortDate(result.visit.visitedAt)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    StampReveal(result.badge.stamp.illustration, stampStyleFor(result.progress?.collection?.styleIndex ?: 0))
                    Spacer(Modifier.height(20.dp))
                    Text("SELLO OBTENIDO", style = OverlineStyle, color = MaterialTheme.colorScheme.primary)
                    Text(
                        result.badge.stamp.title,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Este sello recuerda tu visita a ${result.visit.placeName}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    result.progress?.let { progress ->
                        Spacer(Modifier.height(24.dp))
                        Surface(
                            shape = CardShape,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(progress.collection.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                    Text("${progress.earnedStamps} de ${progress.totalStamps}", style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { progress.fraction },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Colección actualizada con esta visita.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                PrimaryWideButton("Ver mi colección", onClick = onOpenCollection)
                TextButton(onClick = onBackToPlace, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(bottom = 8.dp)) {
                    Text("Volver a la ficha")
                }
            }
        }
    }
}

/**
 * Animación de «estampado»: el sello entra grande y girado y se asienta con un rebote corto,
 * seguido de una vibración leve. Si el teléfono tiene las animaciones desactivadas, aparece quieto.
 */
@Composable
private fun StampReveal(illustration: Illustration, style: StampStyle) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val reduceMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val progress = remember { Animatable(if (reduceMotion) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!reduceMotion) progress.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 260f))
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    StampArt(
        illustration = illustration,
        obtained = true,
        size = 170.dp,
        style = style,
        modifier = Modifier.graphicsLayer {
            val p = progress.value
            val scale = 1.35f - 0.35f * p
            scaleX = scale
            scaleY = scale
            rotationZ = -14f * (1f - p)
            alpha = p.coerceIn(0f, 1f)
        },
    )
}
