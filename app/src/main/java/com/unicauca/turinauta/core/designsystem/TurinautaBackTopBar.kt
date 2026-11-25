package com.unicauca.turinauta.core.designsystem

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

/** Barra compacta para pantallas secundarias; el contenedor principal ya aplica el área segura. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurinautaBackTopBar(
    title: String,
    onBackClick: () -> Unit,
    backContentDescription: String = "Volver",
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = SlateDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(TurinautaDimens.MinTouchTarget),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = backContentDescription,
                    tint = SlateDark,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmIvory),
        windowInsets = WindowInsets(0, 0, 0, 0),
    )
}
