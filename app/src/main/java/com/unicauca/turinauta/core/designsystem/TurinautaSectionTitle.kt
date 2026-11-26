package com.unicauca.turinauta.core.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.turinauta_header_mark),
            contentDescription = null,
            modifier = Modifier.size(width = 44.dp, height = 38.dp),
        )
        Text(text = title, style = SectionTitleStyle, color = DeepTeal)
    }
}
