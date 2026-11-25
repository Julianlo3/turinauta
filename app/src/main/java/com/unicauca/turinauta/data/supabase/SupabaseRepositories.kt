package com.unicauca.turinauta.data.supabase

import android.content.Context
import android.net.Uri
import com.unicauca.turinauta.core.location.distanceMetersTo
import com.unicauca.turinauta.core.model.AuthResult
import com.unicauca.turinauta.core.model.Category
import com.unicauca.turinauta.core.model.CollectionProgress
import com.unicauca.turinauta.core.model.ConditionType
import com.unicauca.turinauta.core.model.DataReport
import com.unicauca.turinauta.core.model.GeoPoint
import com.unicauca.turinauta.core.model.Illustration
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.RewardBadge
import com.unicauca.turinauta.core.model.Route
import com.unicauca.turinauta.core.model.Stamp
import com.unicauca.turinauta.core.model.ThematicCollection
import com.unicauca.turinauta.core.model.UserProfile
import com.unicauca.turinauta.core.model.VisitCondition
import com.unicauca.turinauta.core.model.VisitPhoto
import com.unicauca.turinauta.core.model.VisitRecord
import com.unicauca.turinauta.core.model.VisitResult
import com.unicauca.turinauta.data.repository.AuthRepository
import com.unicauca.turinauta.data.repository.CatalogRepository
import com.unicauca.turinauta.data.repository.CollectionRepository
import com.unicauca.turinauta.data.repository.ReportRepository
import com.unicauca.turinauta.data.repository.RouteRepository
import com.unicauca.turinauta.data.repository.VisitRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import java.time.OffsetDateTime
import java.util.UUID

/*
 * Repositorios conectados a Supabase según CONTRATO_API_MVP.md.
 * Las lecturas públicas funcionan sin sesión; las privadas usan el JWT de Supabase Auth.
 */

/** Mensaje entendible para errores de red o de la API. */
internal fun Throwable.friendlyMessage(): String = when (this) {
    is HttpRequestException -> "No pudimos conectarnos. Revisa tu conexión e inténtalo de nuevo."
    is RestException -> error.ifBlank { "El servidor no pudo completar la solicitud." }
    is java.io.IOException -> "No pudimos conectarnos. Revisa tu conexión e inténtalo de nuevo."
    else -> message ?: "Ocurrió un error inesperado."
}

/** `visit_conditions` es texto: se separa por oraciones para mostrarlo en filas. */
private fun String?.toConditions(): List<VisitCondition> =
    this.orEmpty().split(Regex("(?<=\\.)\\s+")).map { it.trim() }.filter { it.isNotEmpty() }.map { text ->
        val lower = text.lowercase()
        val type = when {
            "foto" in lower -> ConditionType.PHOTO
            "grupo" in lower || "personas" in lower -> ConditionType.GROUP
            "escal" in lower || "acceso" in lower -> ConditionType.ACCESS
            else -> ConditionType.INFO
        }
        VisitCondition(type, text)
    }

/** Catálogo público: `categories` activas y `places` publicados (los filtra la RLS). */
class SupabaseCatalogRepository(private val client: SupabaseClient) : CatalogRepository {
    private val mutex = Mutex()
    private var cache: List<Place>? = null

    /** Lugares ya cargados (para completar datos del historial sin otra consulta). */
    fun cachedPlaces(): List<Place> = cache.orEmpty()

    override suspend fun getPlaces(): List<Place> = mutex.withLock {
        val categories = client.from("categories")
            .select { order("sort_order", Order.ASCENDING) }
            .decodeList<CategoryDto>()
            .associate { it.id to Category(it.id, it.name, it.slug, it.sortOrder) }
        val places = client.from("places")
            .select { order("sort_order", Order.ASCENDING) }
            .decodeList<PlaceDto>()
            .mapNotNull { dto -> categories[dto.categoryId]?.let { dto.toPlace(it) } }
        cache = places
        places
    }

    override suspend fun getPlace(placeId: String): Place? =
        cache?.firstOrNull { it.id == placeId } ?: getPlaces().firstOrNull { it.id == placeId }

    private fun PlaceDto.toPlace(category: Category) = Place(
        id = id,
        slug = slug,
        name = name,
        category = category,
        summary = summary,
        description = description,
        schedule = openingHours?.takeIf { it.isNotBlank() },
        addressReference = locationReference,
        latitude = latitude,
        longitude = longitude,
        conditions = visitConditions.toConditions(),
        imagePath = imagePath,
        illustration = category.illustration,
        sortOrder = sortOrder,
    )
}

/** `available_routes` y sus paradas visibles en `route_places`. */
class SupabaseRouteRepository(
    private val client: SupabaseClient,
    private val catalog: SupabaseCatalogRepository,
) : RouteRepository {

    override suspend fun getRoutes(): List<Route> {
        val routes = client.from("available_routes")
            .select { order("sort_order", Order.ASCENDING) }
            .decodeList<RouteDto>()
        val stops = client.from("route_places").select().decodeList<RoutePlaceDto>().groupBy { it.routeId }
        val places = catalog.cachedPlaces().ifEmpty { catalog.getPlaces() }.associateBy { it.id }
        return routes.map { dto ->
            val routeStops = stops[dto.id].orEmpty().filter { it.placeId in places }
                .sortedWith(compareBy(nullsLast()) { it.suggestedOrder })
            Route(
                id = dto.id,
                slug = dto.slug,
                name = dto.name,
                summary = dto.description,
                purpose = dto.description,
                conditions = dto.visitConditions.toConditions(),
                placeIds = routeStops.map { it.placeId },
                hasSuggestedOrder = routeStops.any { it.suggestedOrder != null },
                illustration = routeStops.firstNotNullOfOrNull { places[it.placeId]?.illustration } ?: Illustration.WEAVE,
                sortOrder = dto.sortOrder,
            )
        }.filter { it.placeIds.isNotEmpty() }
    }

    override suspend fun getRoute(routeId: String): Route? = getRoutes().firstOrNull { it.id == routeId }

    override suspend fun getRoutePlaces(routeId: String): List<Place> {
        val route = getRoute(routeId) ?: return emptyList()
        val places = catalog.cachedPlaces().associateBy { it.id }
        return route.placeIds.mapNotNull { places[it] }
    }
}

/** Supabase Auth con Google. El perfil (`profiles`) solo guarda el nombre mostrado. */
class SupabaseAuthRepository(
    private val client: SupabaseClient,
    scope: CoroutineScope,
) : AuthRepository {

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    override val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    init {
        scope.launch {
            client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = status.session.user ?: return@collect
                        val meta = user.userMetadata
                        val fallbackName = listOf("full_name", "name").firstNotNullOfOrNull { key ->
                            (meta?.get(key) as? JsonPrimitive)?.contentOrNull
                        } ?: "Explorador"
                        if (_currentUser.value?.id != user.id) {
                            _currentUser.value = UserProfile(user.id, fallbackName, user.email.orEmpty())
                        }
                        // El nombre definitivo está en profiles (lo crea un trigger al registrarse).
                        runCatching {
                            client.from("profiles").select().decodeSingleOrNull<ProfileDto>()
                        }.getOrNull()?.displayName?.takeIf { it.isNotBlank() }?.let { name ->
                            _currentUser.value = _currentUser.value?.copy(displayName = name)
                        }
                    }
                    is SessionStatus.NotAuthenticated -> _currentUser.value = null
                    else -> Unit
                }
            }
        }
    }

    override suspend fun signInWithGoogle(): AuthResult = try {
        // Abre el navegador; Supabase devuelve la sesión por el deep link login-callback.
        client.auth.signInWith(Google)
        AuthResult.Started
    } catch (e: Exception) {
        AuthResult.Error(e.friendlyMessage())
    }

    override suspend fun signOut() {
        runCatching { client.auth.signOut() }
        _currentUser.value = null
    }
}

/** `my_visit_history`, Storage `visit-photos` y `rpc/record_visit`. */
class SupabaseVisitRepository(
    private val client: SupabaseClient,
    private val context: Context,
    private val auth: AuthRepository,
    private val catalog: SupabaseCatalogRepository,
    scope: CoroutineScope,
) : VisitRepository {

    private val _visits = MutableStateFlow<List<VisitRecord>>(emptyList())
    override val visits: Flow<List<VisitRecord>> = _visits.asStateFlow()
    private val mutex = Mutex()

    init {
        scope.launch {
            auth.currentUser.distinctUntilChangedBy { it?.id }.collect { user ->
                if (user == null) _visits.value = emptyList() else runCatching { refresh() }
            }
        }
    }

    /** Vuelve a leer el historial del usuario en sesión. */
    suspend fun refresh() {
        if (auth.currentUser.value == null) { _visits.value = emptyList(); return }
        val places = catalog.cachedPlaces().ifEmpty { runCatching { catalog.getPlaces() }.getOrDefault(emptyList()) }
            .associateBy { it.id }
        val rows = client.from("my_visit_history")
            .select { order("visited_at", Order.DESCENDING) }
            .decodeList<JsonObject>()
        _visits.value = rows.map { it.toVisitRecord(places) }
    }

    override suspend fun recordVisit(placeId: String, photo: VisitPhoto, location: GeoPoint): VisitResult = mutex.withLock {
        val user = auth.currentUser.value ?: return VisitResult.VisitUnauthenticated
        try {
            // 1. Si ya existe la visita no se sube otra foto.
            refresh()
            _visits.value.firstOrNull { it.placeId == placeId }?.let { return VisitResult.VisitDuplicate(it) }

            val place = catalog.getPlace(placeId) ?: return VisitResult.VisitNetworkError("Este lugar ya no está disponible.")
            if (photo.mimeType !in VisitPhoto.ALLOWED_TYPES) return VisitResult.VisitInvalidPhoto("La foto debe ser JPG, PNG o WebP.")
            if (photo.sizeBytes > VisitPhoto.MAX_BYTES) return VisitResult.VisitInvalidPhoto("La foto pesa más de 5 MB. Elige otra o tómala de nuevo.")

            // Se valida la distancia antes de subir para no dejar fotos sin asociar.
            val distance = location.distanceMetersTo(place.location)
            if (distance > VisitResult.MAX_DISTANCE_METERS) return VisitResult.VisitTooFar(distance.toInt())

            // 2. Subir la foto a visit-photos/<uid>/<uuid>.<ext> (upsert = false).
            val extension = when (photo.mimeType) { "image/png" -> "png"; "image/webp" -> "webp"; else -> "jpg" }
            val path = "${user.id}/${UUID.randomUUID()}.$extension"
            val bytes = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(Uri.parse(photo.uri))?.use { it.readBytes() }
            } ?: return VisitResult.VisitInvalidPhoto("No pudimos leer la foto. Elige otra o tómala de nuevo.")
            client.storage.from("visit-photos").upload(path, bytes) {
                upsert = false
                contentType = ContentType.parse(photo.mimeType)
            }

            // 3. Registrar con la RPC (la base valida distancia, foto y unicidad).
            val response = client.postgrest.rpc(
                "record_visit",
                buildJsonObject {
                    put("p_place_id", placeId)
                    put("p_photo_path", path)
                    put("p_latitude", location.latitude)
                    put("p_longitude", location.longitude)
                },
            ).decodeAs<RecordVisitResponse>()

            refresh()
            val visit = _visits.value.firstOrNull { it.placeId == placeId }
                ?: return VisitResult.VisitNetworkError("La visita se guardó, pero no pudimos leer tu historial. Vuelve a abrir la ficha.")
            if (response.status == "already_recorded") return VisitResult.VisitDuplicate(visit)

            val collections = SupabaseCollectionRepository.loadCollections(client)
            val progress = collections.firstOrNull { c -> c.stamps.any { it.placeId == placeId } }
                ?.let { buildProgress(it, _visits.value, catalog.cachedPlaces()) }
            val badge = progress?.badges?.firstOrNull { it.stamp.placeId == placeId } ?: RewardBadge(
                stamp = Stamp(response.stampId.orEmpty(), placeId, visit.stampTitle, "", place.illustration),
                collectionId = "",
                placeName = place.name,
                earned = true,
                visitedAt = visit.visitedAt,
            )
            VisitResult.VisitCreated(visit, badge, progress)
        } catch (e: RestException) {
            val text = (e.error + " " + (e.description ?: "")).lowercase()
            when {
                "distan" in text || "200" in text || "lejos" in text ->
                    VisitResult.VisitTooFar(catalog.cachedPlaces().firstOrNull { it.id == placeId }?.location?.distanceMetersTo(location)?.toInt() ?: 0)
                "foto" in text || "photo" in text || "image" in text -> VisitResult.VisitInvalidPhoto(e.friendlyMessage())
                else -> VisitResult.VisitNetworkError(e.friendlyMessage())
            }
        } catch (e: Exception) {
            // Una respuesta perdida no significa que la visita falló: al reintentar, la RPC responde already_recorded.
            VisitResult.VisitNetworkError(e.friendlyMessage())
        }
    }

    private fun JsonObject.str(vararg keys: String): String? =
        keys.firstNotNullOfOrNull { (this[it] as? JsonPrimitive)?.contentOrNull }

    private fun JsonObject.toVisitRecord(places: Map<String, Place>): VisitRecord {
        val placeId = str("place_id").orEmpty()
        val place = places[placeId]
        val available = (this["place_available"] as? JsonPrimitive)?.booleanOrNull ?: (place != null)
        return VisitRecord(
            visitId = str("visit_id", "id") ?: "$placeId-${str("visited_at")}",
            placeId = placeId,
            placeName = str("place_name") ?: place?.name ?: "Lugar no disponible",
            placeAvailable = available && place != null,
            category = place?.category ?: Category("", "Lugar", "", 0),
            illustration = place?.illustration ?: Illustration.CLAY,
            stampTitle = str("stamp_title") ?: "Sello",
            photoPath = str("photo_path").orEmpty(),
            visitedAt = str("visited_at")?.let { runCatching { OffsetDateTime.parse(it).toInstant().toEpochMilli() }.getOrNull() }
                ?: System.currentTimeMillis(),
        )
    }
}

/** Colecciones públicas (`available_collections`, `collection_stamps`, `stamps`) + visitas propias. */
class SupabaseCollectionRepository(
    private val client: SupabaseClient,
    private val catalog: SupabaseCatalogRepository,
    visits: VisitRepository,
) : CollectionRepository {

    internal data class LoadedCollection(val collection: ThematicCollection, val stamps: List<Stamp>)

    private val loaded = MutableStateFlow<List<LoadedCollection>>(emptyList())

    override val progress: Flow<List<CollectionProgress>> =
        combine(loaded, visits.visits) { collections, visitList ->
            if (collections.isEmpty()) runCatching { loaded.value = loadCollections(client) }
            loaded.value.map { buildProgress(it, visitList, catalog.cachedPlaces()) }
        }

    companion object {
        internal suspend fun loadCollections(client: SupabaseClient): List<LoadedCollection> {
            val collections = client.from("available_collections")
                .select { order("sort_order", Order.ASCENDING) }.decodeList<CollectionDto>()
            val links = client.from("collection_stamps").select().decodeList<CollectionStampDto>()
            val stamps = client.from("stamps").select().decodeList<StampDto>().associateBy { it.id }
            return collections.map { c ->
                val stampList = links.filter { it.collectionId == c.id }.sortedBy { it.sortOrder }
                    .mapNotNull { stamps[it.stampId] }
                    .map { Stamp(it.id, it.placeId, it.title, it.description, Illustration.CLAY) }
                LoadedCollection(
                    ThematicCollection(c.id, c.slug, c.name, c.description, Illustration.BOWL, stampList.map { it.id }, c.sortOrder),
                    stampList,
                )
            }
        }
    }
}

internal fun buildProgress(
    loaded: SupabaseCollectionRepository.LoadedCollection,
    visits: List<VisitRecord>,
    places: List<Place>,
): CollectionProgress {
    val byPlace = visits.associateBy { it.placeId }
    val placeById = places.associateBy { it.id }
    val badges = loaded.stamps.map { stamp ->
        val place = placeById[stamp.placeId]
        val illustration = place?.illustration ?: stamp.illustration
        val visit = byPlace[stamp.placeId]
        RewardBadge(
            stamp = stamp.copy(illustration = illustration),
            collectionId = loaded.collection.id,
            placeName = place?.name ?: visit?.placeName ?: "Lugar no disponible",
            earned = visit != null,
            visitedAt = visit?.visitedAt,
        )
    }
    val cover = badges.firstOrNull()?.stamp?.illustration ?: loaded.collection.illustration
    return CollectionProgress(loaded.collection.copy(illustration = cover), badges)
}

/** `rpc/submit_content_report`: funciona sin cuenta. */
class SupabaseReportRepository(private val client: SupabaseClient) : ReportRepository {
    override suspend fun sendReport(report: DataReport): Result<Unit> {
        val details = report.details.trim()
        if (details.isEmpty() || details.length > DataReport.MAX_DETAILS) {
            return Result.failure(IllegalArgumentException("Describe qué dato está mal (máximo ${DataReport.MAX_DETAILS} caracteres)."))
        }
        return try {
            client.postgrest.rpc(
                "submit_content_report",
                buildJsonObject {
                    put("p_place_id", report.placeId)
                    put("p_field", report.field.apiValue)
                    put("p_details", details)
                },
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(e.friendlyMessage()))
        }
    }
}
