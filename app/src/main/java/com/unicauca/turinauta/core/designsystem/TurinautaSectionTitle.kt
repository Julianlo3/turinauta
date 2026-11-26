package com.unicauca.turinauta.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.unicauca.turinauta.R

private val SectionTitleFont = FontFamily(Font(R.font.kalam_bold, weight = FontWeight.Bold))

private val SectionTitleStyle = TextStyle(
    fontFamily = SectionTitleFont,
    fontWeight = FontWeight.Bold,
    fontSize = 30.sp,
    lineHeight = 38.sp,
)

@Composable
fun TurinautaSectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(text = title, style = SectionTitleStyle, color = DeepTeal, modifier = modifier)
}
