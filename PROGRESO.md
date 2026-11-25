# Progreso de Implementación — Turinauta (Android)

> Archivo de consulta rápida para mantener trazabilidad por fases sin re-analizar el repositorio ni los contratos en cada sesión.
>
> **Fuentes de verdad:**
> - [AGENTS.md](AGENTS.md) — Sistema visual Material 3, accesibilidad WCAG 2.2 / TalkBack, arquitectura MVVM+UDF, reglas editoriales y estructura de paquetes.
> - `CONTRATO_API_MVP.md` (entregado por el equipo de backend el 24/11/2026) — Contrato real de Supabase (Data API, Auth Google OAuth, Storage `visit-photos`, RPC `record_visit` y `submit_content_report`).
>
> **Decisiones fijas del proyecto:**
> - Mantener `namespace = "com.unicauca.turinauta"` y `applicationId = "com.unicauca.turinauta"` sin cambios.
> - **Sin datos falsos (`Fake*RepositoryImpl` descartados):** se implementa directamente contra Supabase real leyendo `SUPABASE_URL` y `SUPABASE_PUBLISHABLE_KEY` desde `local.properties` vía `BuildConfig`.

---

## Reconciliación `AGENTS.md` + `CONTRATO_API_MVP.md`

| Área | `AGENTS.md` (diseño/UX base) | `CONTRATO_API_MVP.md` (backend Supabase real) | Decisión adoptada en el proyecto |
| --- | --- | --- | --- |
| **Catálogo público** | Categorías, lugares, búsqueda, rutas y colecciones sin cuenta; R5 (*«Horario por confirmar»*), R6 (`PUBLISHED`). | `GET /categories`, `GET /places`, `GET /available_routes`, `GET /route_places`, `GET /available_collections`, `GET /collection_stamps`, `GET /stamps`. | `SupabaseCatalogRepository`, `SupabaseRouteRepository` y `SupabaseCollectionRepository` alineados 1:1 con columnas de Supabase, preservando R5 y R6. |
| **Ordenamiento en Explorar** | Destacados y Nombre (A–Z) sin exigir GPS. | Permite además **Más cercanos** solicitando ubicación opcional sin guardarla ni abrir mapas externos. | `PlaceSortOrder` soporta `FEATURED`, `NAME_ASC` y `NEAREST` con cálculo Haversine en memoria. |
| **Autenticación** | Cuenta privada + limpieza al cerrar sesión (HU-25). | **Google OAuth en Supabase Auth** (`profiles`: `id`, `display_name`, `created_at`; correo desde Auth; sin registro correo/contraseña ni edición de perfil). | `SupabaseAuthRepository` usa `supabase.auth.signInWith(Google)`, deep link `turinauta://auth-callback`, consulta `profiles` y limpia estado en memoria al cerrar sesión. |
| **Registro de visita** | Idempotencia R4 (1 visita + 1 sello por cuenta/lugar), bloqueo doble toque, error con «Reintentar» (HU-18). | 1) Verificar `my_visit_history` para no subir otra foto; 2) Foto JPG/PNG/WebP ≤5 MB + GPS ≤200 m; 3) Subir a `visit-photos/<auth.uid()>/<uuid>.ext` (`upsert=false`); 4) `POST /rest/v1/rpc/record_visit`. | `SupabaseVisitRepository.registerVisitWithPhoto(...)` implementa exactamente los 4 pasos y mapea `VisitCreated`, `VisitDuplicate`, `VisitUnauthenticated`, `VisitOutOfRange`, `VisitInvalidPhoto` y `VisitNetworkError`. |
| **Historial retirado** | Conservar progreso/historial del usuario. | `GET /my_visit_history`: si un lugar se retira, `place_available=false`, `place_name="Lugar no disponible"` y conserva `stamp_title`. | `VisitRecord` + `MyVisitHistoryDto.toDomain()` respetan `placeAvailable` y `UNAVAILABLE_PLACE_NAME`. |
| **Reportar dato incorrecto (HU-24)** | Acción secundaria discreta en ficha, modal privado. | `POST /rest/v1/rpc/submit_content_report` funciona **sin cuenta**: `p_field` (`schedule`, `location`, `description`, `other`), `p_details` obligatorio (máx. 1500 chars), responde `{"status":"received"}`. | `SupabaseReportRepository` valida antes de enviar e invoca la RPC pública. |
| **Credenciales** | Sin secretos en código; nunca exponer `local.properties`. | Solo `SUPABASE_URL` y `SUPABASE_PUBLISHABLE_KEY` (`sb_publishable_...`), nunca `service_role`. | Variables declaradas en `local.properties` (ignorado en git) e inyectadas por `BuildConfig` en `SupabaseConfig`. |

---

## Estado por Fases

### Fase 1 — Configuración Compose, `core/model` y `core/designsystem` `[COMPLETADA]`
- [x] Habilitar Jetpack Compose y Material 3 en [gradle/libs.versions.toml](gradle/libs.versions.toml), [build.gradle.kts](build.gradle.kts) y [app/build.gradle.kts](app/build.gradle.kts) (AGP 9.5.1 + `kotlin-compose` + Compose BOM `2026.08.00` compatible con `compileSdk = 37`, manteniendo `namespace = "com.unicauca.turinauta"`).
- [x] Implementar `com.unicauca.turinauta.core.model` (`EditorialStatus`, `IllustrationKey`, `Category`, `Place`, `Route`, `UserProfile`, `RewardBadge`, `CollectionProgress`, `VisitRecord`, `DataReport`).
- [x] Implementar `com.unicauca.turinauta.core.designsystem` (`Color`, `Type`, `Shapes`, `Theme`, `Illustrations`).
- [x] Compilación verificada con `./gradlew assembleDebug` (0 errores, 0 advertencias).

### Fase 2 — `core/network`, `data/repository` y `data/remote` (Supabase Real) `[COMPLETADA]`
- [x] Declarar `SUPABASE_URL=` y `SUPABASE_PUBLISHABLE_KEY=` en `local.properties` (listos para llenar por el usuario) y exponerlos vía `BuildConfig` en [app/build.gradle.kts](app/build.gradle.kts).
- [x] Agregar `supabase-kt` (`3.9.0`: `auth-kt`, `postgrest-kt`, `storage-kt`), `ktor-client-okhttp` (`3.6.1`), `kotlinx-serialization-json` (`1.9.0`), `kotlinx-coroutines`, core library desugaring (`minSdk = 24`) y permiso `INTERNET`.
- [x] Implementar `core/network/`: `SupabaseConfig.kt` y `SupabaseClientProvider.kt` (scheme `turinauta://auth-callback`).
- [x] Implementar interfaces `data/repository/`: `CatalogRepository`, `RouteRepository`, `AuthRepository`, `VisitRepository`, `CollectionRepository`, `ReportRepository`.
- [x] Implementar `data/remote/`: `dto/SupabaseDtos.kt`, `mapper/SupabaseMappers.kt` (validadores de foto/reporte, mapeo RPC, Haversine) y los 6 repositorios reales (`SupabaseCatalogRepository`, `SupabaseRouteRepository`, `SupabaseAuthRepository`, `SupabaseVisitRepository`, `SupabaseCollectionRepository`, `SupabaseReportRepository`) + `data/AppContainer.kt`.
- [x] Crear pruebas unitarias en `SupabaseMappersAndValidatorsTest.kt`.
- [x] Verificado con `./gradlew assembleDebug testDebugUnitTest` (0 errores, 0 advertencias, pruebas en verde).

### Fase 3 — Navegación, `MainActivity`, Explorar y Reportes `[COMPLETADA]`
- [x] Agregar dependencias `navigation-compose` (`2.8.9`), `lifecycle-runtime-compose` / `lifecycle-viewmodel-compose` (`2.8.7`) y `coil-compose` (`2.7.0`).
- [x] `core/navigation/`: `Destinations.kt`, `MainScaffold.kt` (`NavigationBar` con Explorar, Rutas, Colección, Perfil), `TurinautaNavHost.kt` (`TurinautaApp`).
- [x] `TurinautaApplication.kt` + `MainActivity.kt` con edge-to-edge, `TurinautaTheme`, `supabase.handleDeeplinks(intent)` y `<intent-filter>` para `turinauta://auth-callback` en `AndroidManifest.xml`.
- [x] `feature/explore/`: `ExploreViewModel`, `ExploreScreen` (búsqueda, chips de categorías reales, banner rutas, ordenamiento Destacados / A-Z / Más cercanos opcional, tarjetas con Coil + fallback `PlaceIllustration`), `PlaceDetailViewModel`, `PlaceDetailScreen`, `PlaceLocationMap`.
- [x] `feature/reports/`: `ReportViewModel`, `DataReportSheet` (modal privado conectado a `submit_content_report` sin cuenta).
- [x] Pruebas unitarias en `ExploreAndReportViewModelTest.kt`.
- [x] Verificado con `./gradlew assembleDebug testDebugUnitTest` (0 errores, 0 advertencias, pruebas en verde) y guardado en commit en `vistasJulian`.

### Fase 4 — Rutas, Autenticación Google OAuth y Registro de Visitas `[COMPLETADA]`
- [x] `feature/routes/`: `RoutesViewModel`, `RoutesScreen`, `RouteDetailViewModel`, `RouteDetailScreen` (paradas ordenadas por `suggested_order` con insignia de lugar ya visitado).
- [x] `feature/auth/`: `AuthViewModel`, `AuthScreen` (flujo real Google OAuth con `signInWithGoogle()` y retorno por deep link `turinauta://auth-callback`).
- [x] `feature/visits/`: `RegisterVisitUseCase`, `VisitRegistrationViewModel`, `ConfirmVisitSheet` (cámara con `FileProvider` + galería, compresión segura ≤5 MB + lectura GPS fresca y cálculo de distancia ≤200 m), `VisitResultDialog` (éxito con sello, ya registrada, >200 m, foto inválida, sin sesión y error de red con «Reintentar» HU-18).
- [x] Conexión completa en `PlaceDetailScreen.kt` y `TurinautaNavHost.kt` + pruebas unitarias en `Phase4ViewModelsTest.kt`.
- [x] Verificado con `./gradlew assembleDebug testDebugUnitTest` (0 errores, 0 advertencias, pruebas en verde) y guardado en commit en `vistasJulian`.

### Fase 5 — Colección, Perfil, Historial y Pulido Final `[COMPLETADA]`
- [x] `feature/collection/`: `CollectionViewModel`, `CollectionScreen` (vista pública sin cuenta + progreso autenticado, sellos **Obtenida** / **Pendiente** siempre con icono + texto, nunca solo color).
- [x] `feature/profile/`: `ProfileViewModel`, `ProfileScreen` (consulta `profiles` + correo Auth + diálogo de cierre de sesión con limpieza inmediata HU-25), `VisitHistoryViewModel`, `VisitHistoryScreen` (fotos públicas de `visit-photos` + soporte `placeAvailable = false` → *«Lugar no disponible»* conservando `stampTitle`).
- [x] Conexión total en `TurinautaNavHost.kt` (sin pantallas provisionales) + inicialización temprana de `authRepository` en `MainActivity.handleOAuthCallback` para retorno de Google OAuth (`turinauta://auth-callback`).
- [x] Pruebas unitarias en `Phase5ViewModelsTest.kt`.
- [x] Verificado con `./gradlew assembleDebug testDebugUnitTest` (0 errores, 0 advertencias, pruebas en verde) y guardado en commit en `vistasJulian`.

---

## Configuración obligatoria para Google OAuth (Android ↔ Supabase)
1. **Google Cloud Console (ya configurado por backend):** `https://<project-ref>.supabase.co/auth/v1/callback`
2. **Supabase Dashboard → Authentication → URL Configuration → Redirect URLs:** agregar exactamente:
   - `turinauta://auth-callback`
3. **Android (ya configurado en `AndroidManifest.xml` y `SupabaseConfig.kt`):** `<data android:scheme="turinauta" android:host="auth-callback" />` + `MainActivity.handleDeeplinks(intent)`.

---

## Registro de Cambios
- **2026-11-24:** Fase 1 completada (`core/model`, `core/designsystem`, Compose en Gradle).
- **2026-11-24:** Fase 2 implementada directamente contra Supabase real (`local.properties`, `BuildConfig`, `core/network`, `data/repository`, `data/remote`, `AppContainer` y pruebas unitarias).
- **2026-11-24:** Fase 3 completada en rama `vistasJulian` (`TurinautaApplication`, `MainActivity` con OAuth deep link, `core/navigation`, `feature/explore`, `feature/reports` y pruebas unitarias).
- **2026-11-24:** Fase 4 completada en rama `vistasJulian` (`feature/routes`, `feature/auth`, `feature/visits` con `FileProvider` + GPS ≤200 m, conexión en `PlaceDetailScreen`/`TurinautaNavHost` y pruebas unitarias).
- **2026-11-24:** Fase 5 completada en rama `vistasJulian` (`feature/collection`, `feature/profile`, `VisitHistoryScreen`, cierre de sesión HU-25, pulido OAuth deep link `turinauta://auth-callback` y pruebas unitarias). MVP Android completo.
