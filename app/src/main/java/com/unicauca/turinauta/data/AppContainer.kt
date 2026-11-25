package com.unicauca.turinauta.data

import com.unicauca.turinauta.core.network.SupabaseClientProvider
import com.unicauca.turinauta.data.remote.SupabaseAuthRepository
import com.unicauca.turinauta.data.remote.SupabaseCatalogRepository
import com.unicauca.turinauta.data.remote.SupabaseCollectionRepository
import com.unicauca.turinauta.data.remote.SupabaseReportRepository
import com.unicauca.turinauta.data.remote.SupabaseRouteRepository
import com.unicauca.turinauta.data.remote.SupabaseVisitRepository
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.CollectionRepository
import com.unicauca.turinauta.data.repository.ReportRepository
import com.unicauca.turinauta.data.repository.RouteRepository
import com.unicauca.turinauta.data.repository.VisitRepository

/**
 * Contenedor de dependencias de la aplicación con repositorios reales de Supabase.
 * Mantiene la capa de UI y los ViewModels desacoplados mediante interfaces [Repository].
 */
class AppContainer(
    val supabaseClientProvider: SupabaseClientProvider = SupabaseClientProvider(),
) {
    val catalogRepository: CatalogRepository by lazy {
        SupabaseCatalogRepository(supabaseClientProvider)
    }

    val routeRepository: RouteRepository by lazy {
        SupabaseRouteRepository(supabaseClientProvider)
    }

    val authRepository: AuthRepository by lazy {
        SupabaseAuthRepository(supabaseClientProvider)
    }

    val visitRepository: VisitRepository by lazy {
        SupabaseVisitRepository(supabaseClientProvider)
    }

    val collectionRepository: CollectionRepository by lazy {
        SupabaseCollectionRepository(supabaseClientProvider)
    }

    val reportRepository: ReportRepository by lazy {
        SupabaseReportRepository(supabaseClientProvider)
    }
}
