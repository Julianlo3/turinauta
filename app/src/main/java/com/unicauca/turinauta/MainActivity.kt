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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.unicauca.turinauta.core.designsystem.LaunchIntro
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.navigation.TurinautaApp
import io.github.jan.supabase.auth.handleDeeplinks

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
                var showIntro by remember { mutableStateOf(showIntroOnCreate) }
                Box(Modifier.fillMaxSize()) {
                    TurinautaApp(container = appContainer)
                    if (showIntro) {
                        LaunchIntro(onFinished = { showIntro = false })
                    }
                }
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
