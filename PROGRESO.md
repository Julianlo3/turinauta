# Progreso — Frontend Android de Turinauta

Registro del avance de las pantallas de la app.

- **Especificación:** `AGENTS.md` (diseño, pantallas, accesibilidad) y `CONTRATO_API_MVP.md` (backend Supabase). Cuando se contradicen, **manda el contrato** (decisión del equipo, 24/11/2026).
- **Prototipos:** `Turinauta_prototipos` (34 pantallas).
- **Rama de trabajo:** `Ventanas`.
- **Paquete:** `com.unicauca.turinauta`; dentro se sigue la estructura `core / data / feature` de AGENTS.md.
- **Datos:** desde Supabase (`data/supabase`), con supabase-kt 3.2.6. La versión simulada anterior está en el historial de Git (commit `033d760`).

## Decisiones tomadas

| Tema | Decisión | Fuente |
|---|---|---|
| Inicio de sesión | Solo «Continuar con Google» (Supabase Auth). No hay registro con correo y contraseña | Contrato (reemplaza AGENTS.md §2.1-4) |
| Registro de visita | Foto (cámara o galería; JPG/PNG/WebP ≤ 5 MB) + ubicación actual a ≤ 200 m del lugar → `record_visit` | Contrato (reemplaza «sin foto ni GPS» de AGENTS.md) |
| Visita repetida | Si ya existe, la ficha muestra «Visita ya registrada» y «Ver mi sello»; no se pide otra foto | Contrato |
| Ordenar catálogo | «Orden habitual», «Nombre A–Z» y «Más cercanos primero» (opcional; pide permiso y, si se niega, sigue el orden habitual con un aviso) | Contrato + prototipos A06/A07 |
| Reporte de dato incorrecto | Funciona sin cuenta; el detalle es obligatorio (máx. 1500 caracteres) | Contrato |
| Mapa de la ficha | Mapa estilizado con Canvas, sin red ni GPS; punto de inyección de MapLibre marcado en `PlaceLocationMap.kt` | AGENTS.md §5 |
| Permisos | Ubicación (opcional para ordenar, necesaria para registrar visita). Sin permiso de cámara: se usa la app de cámara del sistema y el selector de fotos | Contrato |
| Ilustraciones | Vectoriales en Canvas (verde petróleo y ocre), sin imágenes remotas | AGENTS.md §3.2 |
| Inyección de dependencias | Manual con `AppContainer` | Simplicidad para el piloto |

## Fase 1 — Base, diseño y capa de datos ✅

- [x] Gradle: Compose (BOM 2026.02.01), Material 3, Navigation Compose, Lifecycle, Icons Extended; plugin `kotlin-compose`.
- [x] `MainActivity` (edge-to-edge), `TurinautaApplication`, manifiesto con permisos de ubicación y `FileProvider` para la foto de la cámara.
- [x] Sistema de diseño (`core/designsystem`): colores, tipografía, formas, tema, ilustraciones (lugares, cabeceras, rutas, logo, sellos) y componentes compartidos.
- [x] Modelos alineados al contrato (`core/model`): lugares con `slug`/`sort_order`, rutas con orden sugerido, sellos, historial con `place_available` y `stamp_title`, progreso de colecciones, `VisitPhoto`, `VisitResult` (creada, ya registrada, muy lejos, foto inválida, error de red).
- [x] `LocationProvider` (LocationManager, sin Google Play Services) y cálculo de distancia.
- [x] 6 interfaces de repositorio + `Fake*RepositoryImpl` con datos ilustrativos y reglas del contrato (solo publicados, una visita por cuenta y lugar, ≤ 200 m, foto válida, reporte con detalle).
- [x] `FakeNetworkConfig` para el Modo pruebas.

## Fase 2 — Navegación, Explorar, Rutas y Ficha ✅

- [x] `MainScaffold` + `TurinautaNavHost` con la barra inferior de 4 destinos (Explorar, Rutas, Colección, Perfil).
- [x] `ExploreScreen` + `ExploreViewModel`: buscador en tiempo real (ignora tildes), chips de categoría, hoja «Ordenar», banner de rutas y estados vacíos.
- [x] `RoutesScreen`, `RouteDetailScreen`, `RoutesViewModel`: lista, propósito, condiciones, lugares y aviso de orden sugerido o libre.
- [x] `PlaceDetailScreen` + `PlaceDetailViewModel`: ficha completa, mapa, «Reportar un dato incorrecto», estado «Visita ya registrada».
- [x] `DataReportSheet` + `ReportViewModel`.

## Fase 3 — Cuenta, visitas, colección y perfil ✅

- [x] `AuthScreen` + `AuthViewModel` («Continuar con Google» simulado, error legible).
- [x] Flujo de visita: `LoginRequiredSheet` → `RegisterVisitSheet` (foto + ubicación) → `BusyDialog` → `VisitResultDialog`, `TooFarDialog` o `VisitErrorDialog` (Reintentar). Al volver del inicio de sesión se retoma el registro.
- [x] `RegisterVisitUseCase` (anti doble toque).
- [x] `CollectionScreen` + detalle con cuadrícula de sellos «Obtenido» / «Pendiente» (texto + icono).
- [x] `ProfileScreen` (perfil privado, historial, colección, permisos, cerrar sesión) y `VisitHistoryScreen`.
- [x] Panel «Modo pruebas (Simulación)»: error al guardar, simular estar junto al lugar, catálogo vacío, rutas vacías y error de Google.

## Verificación

- `./gradlew :app:assembleDebug` → **BUILD SUCCESSFUL**, sin errores ni advertencias de Kotlin.
- Probado en el emulador (Medium Phone, Android 16) el recorrido esencial: Explorar → ficha de Cocina del Barrio → «Registrar visita» sin sesión → «Continuar con Google» → vuelve sola a la hoja de registro → foto desde la galería → «Guardando tu visita…» → «Sello Sabores de Fogón» con progreso 2 de 3 → Mi colección (2 obtenidos, 4 pendientes) → Perfil → Rutas → detalle de «Oficios y saberes locales».
- **No probado todavía en el emulador:** tomar la foto con la cámara, «Más cercanos» con permiso real, distancia mayor a 200 m con GPS real (el Modo pruebas simula estar junto al lugar por defecto), los estados de error del Modo pruebas y TalkBack.

## Configuración de Supabase

Cada integrante agrega en su `local.properties` (no se sube al repositorio):

```properties
SUPABASE_URL=https://<project-ref>.supabase.co
SUPABASE_PUBLISHABLE_KEY=sb_publishable_...
```

`app/build.gradle.kts` las pasa a `BuildConfig.SUPABASE_URL` y `BuildConfig.SUPABASE_PUBLISHABLE_KEY`, y la app las lee desde `core/network/SupabaseConfig.kt`. Si están vacías, `SupabaseConfig.isConfigured` es `false` y la app sigue con los datos simulados. Nunca poner la clave secret ni `service_role`.

## Conexión a Supabase ✅

- Dependencias: supabase-kt 3.2.6 (Auth, Postgrest, Storage), Ktor 3.3.1 y kotlinx.serialization. Se eligió 3.2.6 porque las versiones nuevas exigen Kotlin 2.3+ y el proyecto usa Kotlin 2.2.10 (AGP 9.2.1).
- Repositorios reales en `data/supabase/SupabaseRepositories.kt`:
  - Catálogo: `categories` + `places`; categorías dinámicas (hoy: Gastronomía, Artesanías, Emprendimientos).
  - Rutas: `available_routes` + `route_places` (orden sugerido).
  - Colecciones: `available_collections` + `collection_stamps` + `stamps`; lo obtenido se calcula con el historial.
  - Cuenta: Google con `signInWith(Google)`; la sesión vuelve por `com.unicauca.turinauta://login-callback` (manifiesto + `handleDeeplinks` en `MainActivity`); nombre desde `profiles.display_name`.
  - Visitas: `my_visit_history`; foto a Storage `visit-photos/<uid>/<uuid>.<ext>`; `rpc/record_visit`. La distancia se revisa antes de subir la foto para no dejar archivos sin asociar.
  - Reportes: `rpc/submit_content_report`.
- Eliminados: repositorios `Fake*`, datos ilustrativos, `FakeNetworkConfig` y el panel «Modo pruebas».
- Estados de error de red con «Reintentar» en Explorar, Rutas y Ficha.
- Fechas con `java.time` (desugaring activado para Android 7).

**Verificado:** lectura de columnas reales de todas las tablas públicas; la app muestra en el emulador los 8 lugares, las categorías y las 2 rutas desde Supabase; «Continuar con Google» abre el navegador y Supabase redirige a Google.

**Inicio de sesión con Google (diagnóstico 25/11/2026):**
- La app abre Google en una pestaña integrada (Custom Tab) con flujo PKCE y `redirect_to=com.unicauca.turinauta://login-callback`, como pide el contrato.
- Al terminar en Google, Supabase devuelve a `http://localhost:3000` (su *Site URL*) en lugar de a la app: **la dirección de retorno no está permitida en el proyecto**.
- Comprobado que la app sí recibe y procesa `com.unicauca.turinauta://login-callback`.
- Se agregó `com.unicauca.turinauta://login-callback` en *Authentication → URL Configuration → Redirect URLs* de Supabase (el «retorno a Android» del contrato).
- **Verificado en el emulador:** el inicio con Google vuelve a la app con la sesión; el Perfil muestra el nombre de `profiles` y el correo de Auth; `my_visit_history` responde sin error (0 visitas) y la Colección muestra «Sabores de la ciudad» (0 de 3) y «Hecho en Popayán» (0 de 5).

**Sin verificar (requiere una cuenta Google real en el dispositivo):**
- Columnas exactas de `my_visit_history` (sin sesión responde «permission denied»); se leen de forma tolerante (`place_id`, `place_name`, `place_available`, `stamp_title`, `photo_path`, `visited_at`).
- Subida de foto + `record_visit` (exige estar a ≤ 200 m del lugar con GPS real).

## Imágenes del catálogo ✅

- `image_path` (p. ej. `places/<slug>/cover.jpg`) se sirve desde el bucket público `catalog-images`: `<SUPABASE_URL>/storage/v1/object/public/catalog-images/<image_path>`.
- Se cargan con Coil 3.3.0 (compatible con Kotlin 2.2) en `CatalogImage`: tarjetas del catálogo, cabecera de la ficha, tarjetas y cabecera de rutas (si la ruta no tiene imagen, se usa la de su primera parada) e historial.
- Mientras carga, si falla la red o si no hay imagen, se ve la ilustración de la categoría. La etiqueta «Imagen ilustrativa» solo aparece cuando no hay foto.
- Coil reduce cada foto al tamaño de la vista y la guarda en caché de disco.

## Pendiente

- Probar el registro de una visita en un teléfono real en Popayán (y con ella confirmar las columnas de `my_visit_history` con datos).
- Inyectar MapLibre en `PlaceLocationMap`.
- Pedir al backend versiones livianas de las fotos (hoy pesan entre 1,5 y 4,5 MB y el plan gratuito de Supabase no permite redimensionarlas al vuelo).

## Bitácora

| Fecha | Paso |
|---|---|
| 24/11/2026 | Clonado el repositorio y creada la rama local `Ventanas` desde `origin/Ventanas`. |
| 24/11/2026 | Fase 1 escrita; configuración Gradle compilada con éxito. |
| 24/11/2026 | Recibido `CONTRATO_API_MVP.md`; el equipo decide seguir el contrato. Modelos, repositorios y flujos ajustados (Google, foto + ubicación, cercanía, reporte obligatorio). |
| 24/11/2026 | Fases 2 y 3 completas; APK compilado y recorrido esencial probado en el emulador. |
| 24/11/2026 | `SUPABASE_URL` y `SUPABASE_PUBLISHABLE_KEY` leídas de `local.properties` hacia `BuildConfig` y `SupabaseConfig`. |
| 24/11/2026 | AGP bajado de 9.5.1 a 9.2.1 y Gradle de 9.6.0 a 9.4.1 (máximo soportado por Android Studio 2026.1); se activa `android.r8.gradual.support` para conservar `optimization { enable = true }` en release. |
| 25/11/2026 | Datos simulados reemplazados por Supabase (catálogo, rutas, colecciones, Google, visitas, reportes); verificado el catálogo real en el emulador. |
| 25/11/2026 | Inicio con Google en Custom Tab + PKCE; diagnóstico: Supabase redirige a localhost:3000 porque falta la Redirect URL de la app. |
| 25/11/2026 | Redirect URL agregada en Supabase; inicio con Google verificado en el emulador (perfil, historial vacío y colecciones reales). |
| 25/11/2026 | Fotos reales del bucket `catalog-images` en catálogo, ficha, rutas e historial (Coil). |
