package com.unicauca.turinauta

import android.app.Application
import com.unicauca.turinauta.data.AppContainer

/**
 * Punto de entrada de la aplicación Turinauta.
 * Mantiene una única instancia de [AppContainer] durante el ciclo de vida del proceso.
 */
class TurinautaApplication : Application() {
    val container: AppContainer by lazy { AppContainer() }
}
