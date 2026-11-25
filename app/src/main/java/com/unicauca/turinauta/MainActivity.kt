package com.unicauca.turinauta

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.unicauca.turinauta.core.designsystem.TurinautaTheme
import com.unicauca.turinauta.core.navigation.MainScaffold
import io.github.jan.supabase.auth.handleDeeplinks

class MainActivity : ComponentActivity() {

    private val supabase get() = (application as TurinautaApplication).container.supabase

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Retorno de Google: com.unicauca.turinauta://login-callback#access_token=…
        supabase.handleDeeplinks(intent)
        setContent {
            TurinautaTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScaffold()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        supabase.handleDeeplinks(intent)
    }
}
