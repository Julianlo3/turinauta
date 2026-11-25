package com.unicauca.turinauta

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

        setContent {
            TurinautaTheme {
                TurinautaApp(container = appContainer)
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
        val client = appContainer.supabaseClientProvider.getClientOrNull() ?: return
        runCatching { client.handleDeeplinks(intent) }
    }
}
