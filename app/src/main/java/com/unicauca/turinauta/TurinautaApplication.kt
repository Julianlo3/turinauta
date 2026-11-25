package com.unicauca.turinauta

import android.app.Application
import android.content.Context
import com.unicauca.turinauta.core.location.DeviceLocationProvider
import com.unicauca.turinauta.core.location.LocationProvider
import com.unicauca.turinauta.core.network.FakeNetworkConfig
import com.unicauca.turinauta.data.fake.FakeAuthRepositoryImpl
import com.unicauca.turinauta.data.fake.FakeCatalogRepositoryImpl
import com.unicauca.turinauta.data.fake.FakeCollectionRepositoryImpl
import com.unicauca.turinauta.data.fake.FakeReportRepositoryImpl
import com.unicauca.turinauta.data.fake.FakeRouteRepositoryImpl
import com.unicauca.turinauta.data.fake.FakeVisitRepositoryImpl
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.CollectionRepository
import com.unicauca.turinauta.data.repository.ReportRepository
import com.unicauca.turinauta.data.repository.RouteRepository
import com.unicauca.turinauta.data.repository.VisitRepository

/**
 * Contenedor manual de dependencias. Para conectar Supabase basta con cambiar
 * aquí las implementaciones Fake* por las reales (ver CONTRATO_API_MVP.md).
 */
class AppContainer(context: Context) {
    val network = FakeNetworkConfig()
    val location: LocationProvider = DeviceLocationProvider(context)
    val catalog: CatalogRepository = FakeCatalogRepositoryImpl(network)
    val routes: RouteRepository = FakeRouteRepositoryImpl(network)
    val auth: AuthRepository = FakeAuthRepositoryImpl(network)
    val visits: VisitRepository = FakeVisitRepositoryImpl(network, auth)
    val collections: CollectionRepository = FakeCollectionRepositoryImpl(visits)
    val reports: ReportRepository = FakeReportRepositoryImpl(network)
}

class TurinautaApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
