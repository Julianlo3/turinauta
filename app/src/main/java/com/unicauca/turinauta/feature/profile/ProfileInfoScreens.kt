package com.unicauca.turinauta.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SubtleOutline
import com.unicauca.turinauta.core.designsystem.TurinautaBackTopBar
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.designsystem.WarmIvory

@Composable
fun HowItWorksScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    ProfileInformationScreen(title = "Cómo funciona Turinauta", onBackClick = onBackClick, modifier = modifier) {
        Text(
            text = "Explora lugares y rutas temáticas. Al registrar tus visitas, puedes obtener sellos y seguir tu progreso en las colecciones.",
            style = MaterialTheme.typography.bodyLarge,
            color = SlateMedium,
        )
        InformationCard("1. Descubre") {
            Text("Consulta los lugares, sus fichas y las rutas disponibles sin iniciar sesión.", style = MaterialTheme.typography.bodyMedium, color = SlateMedium)
        }
        InformationCard("2. Visita y registra") {
            Text("Inicia sesión con Google y, desde la ficha del lugar, adjunta una foto y verifica tu ubicación para confirmar la visita.", style = MaterialTheme.typography.bodyMedium, color = SlateMedium)
        }
        InformationCard("3. Colecciona") {
            Text("Cuando se registra una visita válida, el sello del lugar aparece en tu colección. Consulta también el avance de cada colección y tu historial de visitas.", style = MaterialTheme.typography.bodyMedium, color = SlateMedium)
        }
    }
}

@Composable
fun PhotosAndLocationScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    ProfileInformationScreen(title = "Uso de fotos y ubicación", onBackClick = onBackClick, modifier = modifier) {
        Text(
            text = "Al registrar una visita, Turinauta solicita una fotografía del lugar y tu ubicación actual.",
            style = MaterialTheme.typography.bodyLarge,
            color = SlateMedium,
        )
        InformationCard("Fotografía de la visita") {
            Text("Puedes tomarla con la cámara o elegirla de la galería. La foto se sube al registrar la visita y es pública en Turinauta: evita incluir rostros, documentos u otros datos personales.", style = MaterialTheme.typography.bodyMedium, color = SlateMedium)
        }
        InformationCard("Ubicación") {
            Text("Se usa para comprobar la distancia al lugar al registrar la visita. En el mapa ampliado también permite mostrar tu posición y calcular una ruta a pie hacia el lugar.", style = MaterialTheme.typography.bodyMedium, color = SlateMedium)
        }
        InformationCard("Permisos") {
            Text("Android te pedirá acceso a la ubicación cuando la necesites. Si eliges tomar una foto, también puede solicitar acceso a la cámara. Puedes cambiar estos permisos en los ajustes del teléfono.", style = MaterialTheme.typography.bodyMedium, color = SlateMedium)
        }
    }
}

@Composable
private fun ProfileInformationScreen(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize().background(WarmIvory)) {
        TurinautaBackTopBar(title = title, onBackClick = onBackClick)
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = TurinautaDimens.ScreenHorizontalPadding, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun InformationCard(title: String, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
    Surface(
        color = PureWhite,
        shape = shape,
        modifier = Modifier.fillMaxWidth().border(TurinautaDimens.CardBorderWidth, SubtleOutline, shape),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = DeepTeal)
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HowItWorksPreview() {
    TurinautaTheme { HowItWorksScreen(onBackClick = {}) }
}

@Preview(showBackground = true)
@Composable
private fun PhotosAndLocationPreview() {
    TurinautaTheme { PhotosAndLocationScreen(onBackClick = {}) }
}
