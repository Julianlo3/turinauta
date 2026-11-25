package com.unicauca.turinauta.data.repository

import com.unicauca.turinauta.core.model.AuthResult
import com.unicauca.turinauta.core.model.CollectionProgress
import com.unicauca.turinauta.core.model.DataReport
import com.unicauca.turinauta.core.model.GeoPoint
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.Route
import com.unicauca.turinauta.core.model.UserProfile
import com.unicauca.turinauta.core.model.VisitPhoto
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.core.model.VisitResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/*
 * Contratos de datos (ver CONTRATO_API_MVP.md). La UI y los ViewModels solo conocen
 * estas interfaces: las implementaciones Fake* se reemplazan por Supabase sin tocar la UI.
 */

/** Lectura pública: `categories`, `places` (solo publicados de categorías activas). */
interface CatalogRepository {
    suspend fun getPlaces(): List<Place>
    suspend fun getPlace(placeId: String): Place?
}

/** `available_routes` y `route_places`. */
interface RouteRepository {
    suspend fun getRoutes(): List<Route>
    suspend fun getRoute(routeId: String): Route?
    suspend fun getRoutePlaces(routeId: String): List<Place>
}

/** Supabase Auth con Google OAuth; `profiles` guarda solo el nombre mostrado. */
interface AuthRepository {
    val currentUser: StateFlow<UserProfile?>
    /** `supabase.auth.signInWith(Google)`: crea o recupera la cuenta. */
    suspend fun signInWithGoogle(): AuthResult
    /** Cierra sesión y limpia el estado privado observable. */
    suspend fun signOut()
}

/** `my_visit_history` + Storage `visit-photos` + `rpc/record_visit`. */
interface VisitRepository {
    /** Historial del usuario en sesión, más reciente primero; vacío sin sesión. */
    val visits: Flow<List<VisitRecord>>
    /**
     * Sube la foto a `visit-photos/<uid>/<uuid>.<ext>` y llama a `record_visit`
     * con la ubicación actual. La RPC valida distancia ≤ 200 m y una visita por lugar.
     */
    suspend fun recordVisit(placeId: String, photo: VisitPhoto, location: GeoPoint): VisitResult
}

/** `available_collections`, `my_collection_progress`, `my_collection_stamps`. */
interface CollectionRepository {
    val progress: Flow<List<CollectionProgress>>
}

/** `rpc/submit_content_report` (funciona sin cuenta). */
interface ReportRepository {
    suspend fun sendReport(report: DataReport): Result<Unit>
}
