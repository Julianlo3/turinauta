package com.unicauca.turinauta

import android.app.Application
import android.content.Context
import com.unicauca.turinauta.core.location.DeviceLocationProvider
import com.unicauca.turinauta.core.location.LocationProvider
import com.unicauca.turinauta.core.network.createTurinautaSupabaseClient
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.CollectionRepository
import com.unicauca.turinauta.data.repository.ReportRepository
import com.unicauca.turinauta.data.repository.RouteRepository
import com.unicauca.turinauta.data.repository.VisitRepository
import com.unicauca.turinauta.data.supabase.SupabaseAuthRepository
import com.unicauca.turinauta.data.supabase.SupabaseCatalogRepository
import com.unicauca.turinauta.data.supabase.SupabaseCollectionRepository
import com.unicauca.turinauta.data.supabase.SupabaseReportRepository
import com.unicauca.turinauta.data.supabase.SupabaseRouteRepository
import com.unicauca.turinauta.data.supabase.SupabaseVisitRepository
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Contenedor manual de dependencias: todos los datos vienen de Supabase. */
class AppContainer(context: Context) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val supabase: SupabaseClient = createTurinautaSupabaseClient()
    val location: LocationProvider = DeviceLocationProvider(context)

    private val supabaseCatalog = SupabaseCatalogRepository(supabase)
    val catalog: CatalogRepository = supabaseCatalog
    val routes: RouteRepository = SupabaseRouteRepository(supabase, supabaseCatalog)
    val auth: AuthRepository = SupabaseAuthRepository(supabase, appScope)
    val visits: VisitRepository = SupabaseVisitRepository(supabase, context, auth, supabaseCatalog, appScope)
    val collections: CollectionRepository = SupabaseCollectionRepository(supabase, supabaseCatalog, visits)
    val reports: ReportRepository = SupabaseReportRepository(supabase)
}

class TurinautaApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
