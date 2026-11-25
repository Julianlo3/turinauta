package com.unicauca.turinauta.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val CardShape = RoundedCornerShape(20.dp)
val ThumbnailShape = RoundedCornerShape(16.dp)
val PillShape = RoundedCornerShape(28.dp)
val ChipShape = RoundedCornerShape(12.dp)

val TurinautaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = ChipShape,
    medium = ThumbnailShape,
    large = CardShape,
    extraLarge = PillShape,
)
