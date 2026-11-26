package com.unicauca.turinauta.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val SectionTitleStyle = TextStyle(
    fontFamily = FontFamily.Cursive,
    fontStyle = FontStyle.Italic,
    fontWeight = FontWeight.Bold,
    fontSize = 30.sp,
    lineHeight = 38.sp,
)

@Composable
fun TurinautaSectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(text = title, style = SectionTitleStyle, color = DeepTeal, modifier = modifier)
}
