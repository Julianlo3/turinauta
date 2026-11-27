package com.unicauca.turinauta

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.imageLoader
import coil.request.ImageRequest
import com.unicauca.turinauta.core.designsystem.LaunchIntro
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.network.SupabaseConfig
import com.unicauca.turinauta.core.navigation.TurinautaApp
import com.unicauca.turinauta.feature.collection.CollectionViewModel
import com.unicauca.turinauta.feature.explore.ExploreViewModel
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : ComponentActivity() {

    private val appContainer by lazy { (application as TurinautaApplication).container }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
        )
        handleOAuthCallback(intent)

        val showIntroOnCreate = savedInstanceState == null && intent?.action != Intent.ACTION_VIEW
        setContent {
            TurinautaTheme {
                val exploreViewModel: ExploreViewModel = viewModel(
                    factory = ExploreViewModel.factory(appContainer.catalogRepository),
                )
                val collectionViewModel: CollectionViewModel = viewModel(
                    factory = CollectionViewModel.factory(
                        appContainer.collectionRepository,
                        appContainer.authRepository,
                    ),
                )
                var showIntro by remember { mutableStateOf(showIntroOnCreate) }
                var showApp by remember { mutableStateOf(!showIntroOnCreate) }

                LaunchedEffect(showIntroOnCreate) {
                    if (showIntroOnCreate) {
                        launch(Dispatchers.IO) {
                            val places = withTimeoutOrNull(8_000) {
                                exploreViewModel.uiState.first { !it.isLoading }.places
                            } ?: return@launch
                            prefetchImages(places.mapNotNull { it.imagePath }.distinct().take(4))
                        }
                        launch(Dispatchers.IO) {
                            val collections = withTimeoutOrNull(8_000) {
                                collectionViewModel.uiState.first {
                                    !it.isLoading && (it.collections.isNotEmpty() || it.errorMessage != null)
                                }.collections
                            } ?: return@launch
                            val stampImages = collections.sortedBy { it.sortOrder }
                                .flatMap { collection -> collection.stamps.sortedBy { it.sortOrder } }
                                .mapNotNull { it.imagePath }
                                .distinct()
                                .take(4)
                            prefetchImages(stampImages)
                        }
                    }
                }

                Box(Modifier.fillMaxSize()) {
                    if (showApp) {
                        TurinautaApp(
                            container = appContainer,
                            exploreViewModel = exploreViewModel,
                            collectionViewModel = collectionViewModel,
                        )
                    }
                    if (showIntro) {
                        LaunchIntro(
                            onReadyForApp = { showApp = true },
                            onFinished = {
                                showApp = true
                                showIntro = false
                            },
                        )
                    }
                }
            }
        }
    }

    private suspend fun prefetchImages(paths: List<String>) {
        val context = applicationContext
        val loader = context.imageLoader
        paths.mapNotNull { SupabaseConfig.resolvePublicMediaUrl(it) }.distinct().forEach { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .size(320, 320)
                .build()
            try {
                loader.execute(request)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // La precarga es opcional: la pantalla conserva su carga normal si falla.
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOAuthCallback(intent)
    }

    private fun handleOAuthCallback(intent: Intent?) {
        if (intent == null) return
        // Garantiza que AuthRepository ya esté escuchando sessionStatus antes de procesar el callback
        appContainer.authRepository
        val client = appContainer.supabaseClientProvider.getClientOrNull() ?: return
        runCatching { client.handleDeeplinks(intent) }
    }
}
