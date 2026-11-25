package com.unicauca.turinauta

import android.app.Application
import com.unicauca.turinauta.data.AppContainer
import org.osmdroid.config.Configuration
import java.io.File

/**
 * Punto de entrada de la aplicación Turinauta.
 * Mantiene una única instancia de [AppContainer] durante el ciclo de vida del proceso.
 */
class TurinautaApplication : Application() {
    val container: AppContainer by lazy { AppContainer() }

    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().apply {
            load(this@TurinautaApplication, getSharedPreferences("osmdroid", MODE_PRIVATE))
            userAgentValue = "Turinauta/1.0 (com.unicauca.turinauta)"
            osmdroidBasePath = File(filesDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
        }
    }
}
