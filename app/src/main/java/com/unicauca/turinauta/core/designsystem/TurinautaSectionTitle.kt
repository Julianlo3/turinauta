package com.unicauca.turinauta.core.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unicauca.turinauta.R

private val SectionTitleStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 30.sp,
)

@Composable
fun TurinautaSectionTitle(title: String, onOpenAbout: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = title, style = SectionTitleStyle, color = DeepTeal, modifier = Modifier.weight(1f))
        IconButton(
            onClick = onOpenAbout,
            modifier = Modifier.size(TurinautaDimens.MinTouchTarget)
                .semantics { contentDescription = "Acerca de Turinauta" },
        ) {
            Image(
                painter = painterResource(R.drawable.turinauta_logo),
                contentDescription = null,
                modifier = Modifier.size(38.dp),
            )
        }
    }
}
