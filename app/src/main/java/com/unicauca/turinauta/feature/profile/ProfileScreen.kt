package com.unicauca.turinauta.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.unicauca.turinauta.core.designsystem.DeepTeal
import com.unicauca.turinauta.core.designsystem.ErrorContainerLight
import com.unicauca.turinauta.core.designsystem.ErrorRed
import com.unicauca.turinauta.core.designsystem.MutedOchre
import com.unicauca.turinauta.core.designsystem.OnDeepTeal
import com.unicauca.turinauta.core.designsystem.OnErrorContainerDark
import com.unicauca.turinauta.core.designsystem.OnSoftTealContainer
import com.unicauca.turinauta.core.designsystem.PureWhite
import com.unicauca.turinauta.core.designsystem.SlateDark
import com.unicauca.turinauta.core.designsystem.SlateMedium
import com.unicauca.turinauta.core.designsystem.SoftTealContainer
import com.unicauca.turinauta.core.designsystem.SubtleOutline
import com.unicauca.turinauta.core.designsystem.TurinautaDimens
import com.unicauca.turinauta.core.designsystem.TurinautaLogoMark
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.designsystem.WarmIvory
import com.unicauca.turinauta.core.model.UserProfile
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CollectionRepository
import com.unicauca.turinauta.data.repository.VisitRepository

@Composable
fun ProfileRoute(
    authRepository: AuthRepository,
    visitRepository: VisitRepository,
    collectionRepository: CollectionRepository,
    onSignInClick: () -> Unit,
    onOpenVisitHistory: () -> Unit,
    onOpenCollection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(
            authRepository = authRepository,
            visitRepository = visitRepository,
            collectionRepository = collectionRepository,
        ),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSignOutDialog by remember { mutableStateOf(false) }

    ProfileScreen(
        uiState = uiState,
        onSignInClick = onSignInClick,
        onOpenVisitHistory = onOpenVisitHistory,
        onOpenCollection = onOpenCollection,
        onRetryClick = viewModel::refresh,
        onSignOutRequest = { showSignOutDialog = true },
        modifier = modifier,
    )

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = PureWhite,
            title = {
                Text(
                    text = "¿Cerrar sesión?",
                    style = MaterialTheme.typography.titleLarge,
                    color = SlateDark,
                )
            },
            text = {
                Text(
                    text = "Se cerrará tu cuenta en este dispositivo y se ocultará tu historial privado hasta que vuelvas a iniciar sesión con Google.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        viewModel.signOut()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed,
                        contentColor = PureWhite,
                    ),
                    modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSignOutDialog = false },
                    modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                ) {
                    Text("Cancelar", color = DeepTeal)
                }
            },
        )
    }
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onSignInClick: () -> Unit,
    onOpenVisitHistory: () -> Unit,
    onOpenCollection: () -> Unit,
    onRetryClick: () -> Unit,
    onSignOutRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory),
        contentPadding = PaddingValues(
            start = TurinautaDimens.ScreenHorizontalPadding,
            end = TurinautaDimens.ScreenHorizontalPadding,
            top = 16.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "profile_title") {
            Text(
                text = "Perfil",
                style = MaterialTheme.typography.headlineSmall,
                color = SlateDark,
            )
        }

        if (!uiState.isAuthenticated || uiState.user == null) {
            item(key = "signed_out_card") {
                val shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
                Surface(
                    color = PureWhite,
                    shape = shape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(TurinautaDimens.CardBorderWidth, SubtleOutline, shape),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TurinautaLogoMark(size = 56.dp)
                        Text(
                            text = "Tu espacio de exploración",
                            style = MaterialTheme.typography.titleLarge,
                            color = SlateDark,
                        )
                        Text(
                            text = "Inicia sesión con Google para registrar tus visitas, desbloquear sellos digitales y consultar tu historial personal.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateMedium,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = onSignInClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepTeal,
                                contentColor = OnDeepTeal,
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp),
                        ) {
                            Text("Iniciar sesión con Google")
                        }
                    }
                }
            }
            return@LazyColumn
        }

        val user = uiState.user

        // Tarjeta de identidad (solo lectura desde `profiles` + Auth email)
        item(key = "profile_identity") {
            val shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
            val initials = user.displayName
                .trim()
                .split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
                .ifBlank { "E" }

            Surface(
                color = PureWhite,
                shape = shape,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(TurinautaDimens.CardBorderWidth, SubtleOutline, shape),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfileAvatar(
                        avatarUrl = user.avatarUrl,
                        initials = initials,
                        displayName = user.displayName,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = user.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = SlateDark,
                        )
                        if (!user.email.isNullOrBlank()) {
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMedium,
                            )
                        }
                        Text(
                            text = "Cuenta vinculada con Google",
                            style = MaterialTheme.typography.labelMedium,
                            color = DeepTeal,
                        )
                    }
                }
            }
        }

        // Resumen de actividad privada
        item(key = "profile_stats") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ProfileStatCard(
                    label = "Visitas registradas",
                    value = if (uiState.isLoadingStats) "—" else "${uiState.visitedPlacesCount}",
                    icon = Icons.Outlined.History,
                    iconTint = DeepTeal,
                    modifier = Modifier.weight(1f),
                )
                ProfileStatCard(
                    label = "Sellos obtenidos",
                    value = if (uiState.isLoadingStats) "—" else "${uiState.earnedStampsCount}",
                    icon = Icons.Rounded.WorkspacePremium,
                    iconTint = MutedOchre,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (uiState.errorMessage != null) {
            item(key = "profile_error") {
                Surface(
                    color = ErrorContainerLight,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnErrorContainerDark,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = onRetryClick,
                            modifier = Modifier.heightIn(min = TurinautaDimens.MinTouchTarget),
                        ) {
                            Text("Reintentar", color = OnErrorContainerDark)
                        }
                    }
                }
            }
        }

        // Accesos privados
        item(key = "profile_actions") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileActionCard(
                    icon = Icons.Outlined.History,
                    title = "Historial de visitas",
                    subtitle = "Consulta tus lugares visitados, fotos y sellos conservados",
                    onClick = onOpenVisitHistory,
                )
                ProfileActionCard(
                    icon = Icons.Outlined.CollectionsBookmark,
                    title = "Colecciones y sellos",
                    subtitle = "Revisa tu avance en cada colección temática",
                    onClick = onOpenCollection,
                )
            }
        }

        // Cerrar sesión (HU-25)
        item(key = "sign_out_button") {
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = onSignOutRequest,
                enabled = !uiState.isSigningOut,
                border = BorderStroke(1.dp, ErrorRed),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = TurinautaDimens.MinTouchTarget),
            ) {
                if (uiState.isSigningOut) {
                    CircularProgressIndicator(
                        color = ErrorRed,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cerrar sesión",
                        style = MaterialTheme.typography.labelLarge,
                        color = ErrorRed,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    avatarUrl: String?,
    initials: String,
    displayName: String,
) {
    if (avatarUrl.isNullOrBlank()) {
        InitialsAvatar(initials = initials)
    } else {
        SubcomposeAsyncImage(
            model = avatarUrl,
            contentDescription = "Foto de perfil de $displayName",
            contentScale = ContentScale.Crop,
            loading = { InitialsAvatar(initials = initials) },
            error = { InitialsAvatar(initials = initials) },
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(SoftTealContainer),
        )
    }
}

@Composable
private fun InitialsAvatar(initials: String) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(SoftTealContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleLarge,
            color = OnSoftTealContainer,
        )
    }
}

@Composable
private fun ProfileStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
    Surface(
        color = PureWhite,
        shape = shape,
        modifier = modifier
            .border(TurinautaDimens.CardBorderWidth, SubtleOutline, shape)
            .semantics(mergeDescendants = true) {},
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = SlateDark,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = SlateMedium,
            )
        }
    }
}

@Composable
private fun ProfileActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(TurinautaDimens.CardCornerRadius)
    Surface(
        color = PureWhite,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .border(TurinautaDimens.CardBorderWidth, SubtleOutline, shape)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SoftTealContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DeepTeal,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = SlateDark,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMedium,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = SlateMedium,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenSignedInPreview() {
    TurinautaTheme {
        ProfileScreen(
            uiState = ProfileUiState(
                user = UserProfile(
                    id = "u1",
                    displayName = "Julián López",
                    email = "julian@unicauca.edu.co",
                ),
                isAuthenticated = true,
                visitedPlacesCount = 3,
                earnedStampsCount = 3,
            ),
            onSignInClick = {},
            onOpenVisitHistory = {},
            onOpenCollection = {},
            onRetryClick = {},
            onSignOutRequest = {},
        )
    }
}
