package com.unicauca.turinauta.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.ErrorContainerLight
import com.unicauca.turinauta.core.designsystem.OnDeepTeal
import com.unicauca.turinauta.core.designsystem.OnErrorContainerDark
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateDark
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SubtleOutline
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaBackTopBar
import com.unicauca.turinauta.core.designsystem.TurinautaLogoMark
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.designsystem.WarmIvory
import com.unicauca.turinauta.data.repository.AuthRepository

@Composable
fun AuthRoute(
    authRepository: AuthRepository,
    onBackClick: () -> Unit,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.factory(authRepository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onAuthenticated()
        }
    }

    AuthScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onContinueWithGoogleClick = viewModel::onContinueWithGoogleClick,
        modifier = modifier,
    )
}

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onBackClick: () -> Unit,
    onContinueWithGoogleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmIvory,
        topBar = {
            TurinautaBackTopBar(title = "Acceso a cuenta", onBackClick = onBackClick)
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmIvory)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TurinautaLogoMark(size = 64.dp)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Inicia sesión en Turinauta",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SlateDark,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Puedes explorar lugares y rutas sin cuenta. Inicia sesión con Google cuando quieras registrar visitas y guardar tus sellos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMedium,
                    textAlign = TextAlign.Center,
                )
            }

            val cardShape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
            Surface(
                color = PureWhite,
                shape = cardShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(TurinautaDimens.CardBorderWidth, SubtleOutline, cardShape),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AuthBenefitRow(
                        icon = Icons.Outlined.VerifiedUser,
                        text = "Registra tus visitas presenciales en lugares locales.",
                    )
                    AuthBenefitRow(
                        icon = Icons.Outlined.CollectionsBookmark,
                        text = "Desbloquea sellos digitales y completa colecciones temáticas.",
                    )
                    AuthBenefitRow(
                        icon = Icons.Outlined.History,
                        text = "Conserva tu historial personal incluso si un lugar cambia.",
                    )
                }
            }

            if (uiState.errorMessage != null) {
                Surface(
                    color = ErrorContainerLight,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnErrorContainerDark,
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }

            Button(
                onClick = onContinueWithGoogleClick,
                enabled = !uiState.isLaunchingGoogle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepTeal,
                    contentColor = OnDeepTeal,
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
            ) {
                if (uiState.isLaunchingGoogle) {
                    CircularProgressIndicator(
                        color = OnDeepTeal,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Abriendo Google...")
                } else {
                    Text(
                        text = if (uiState.errorMessage != null) {
                            "Reintentar con Google"
                        } else {
                            "Continuar con Google"
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            TextButton(
                onClick = onBackClick,
                enabled = !uiState.isLaunchingGoogle,
                modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
            ) {
                Text(
                    text = "Seguir explorando sin cuenta",
                    style = MaterialTheme.typography.labelLarge,
                    color = DeepTeal,
                )
            }

            Text(
                text = "El acceso se realiza mediante Google a través de Supabase Auth. Tu cuenta crea automáticamente un perfil privado con tu nombre.",
                style = MaterialTheme.typography.bodySmall,
                color = SlateMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
    }
}

@Composable
private fun AuthBenefitRow(
    icon: ImageVector,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DeepTeal,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = SlateDark,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    TurinautaTheme {
        AuthScreen(
            uiState = AuthUiState(),
            onBackClick = {},
            onContinueWithGoogleClick = {},
        )
    }
}
