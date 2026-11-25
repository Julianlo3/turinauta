# Progreso — Frontend Android de Turinauta

Registro del avance de las pantallas de la app.

- **Especificación:** `AGENTS.md` (diseño, pantallas, accesibilidad) y `CONTRATO_API_MVP.md` (backend Supabase). Cuando se contradicen, **manda el contrato** (decisión del equipo, 24/11/2026).
- **Prototipos:** `Turinauta_prototipos` (34 pantallas).
- **Rama de trabajo:** `Ventanas`.
- **Paquete:** `com.unicauca.turinauta`; dentro se sigue la estructura `core / data / feature` de AGENTS.md.
- **Datos:** simulados en memoria (repositorios `Fake*`) con las reglas del contrato; se cambian por Supabase en `AppContainer` (`TurinautaApplication.kt`) sin tocar la UI.

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
| Cuenta de prueba | El inicio con Google simulado devuelve «Explorador Demo» (`explorador@turinauta.app`), con 1 visita previa | AGENTS.md §6 |

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

## Pendiente (siguiente fase: conexión a Supabase)

- Cambiar los `Fake*` por implementaciones con `supabase-kt` (Auth con Google y deep link, PostgREST, Storage `visit-photos`, RPC `record_visit` y `submit_content_report`).
- Inyectar MapLibre en `PlaceLocationMap`.
- Cargar las imágenes autorizadas desde `image_path` cuando existan.

## Bitácora

| Fecha | Paso |
|---|---|
| 24/11/2026 | Clonado el repositorio y creada la rama local `Ventanas` desde `origin/Ventanas`. |
| 24/11/2026 | Fase 1 escrita; configuración Gradle compilada con éxito. |
| 24/11/2026 | Recibido `CONTRATO_API_MVP.md`; el equipo decide seguir el contrato. Modelos, repositorios y flujos ajustados (Google, foto + ubicación, cercanía, reporte obligatorio). |
| 24/11/2026 | Fases 2 y 3 completas; APK compilado y recorrido esencial probado en el emulador. |
| 24/11/2026 | `SUPABASE_URL` y `SUPABASE_PUBLISHABLE_KEY` leídas de `local.properties` hacia `BuildConfig` y `SupabaseConfig`. |
| 24/11/2026 | AGP bajado de 9.5.1 a 9.2.1 y Gradle de 9.6.0 a 9.4.1 (máximo soportado por Android Studio 2026.1); se activa `android.r8.gradual.support` para conservar `optimization { enable = true }` en release. |
