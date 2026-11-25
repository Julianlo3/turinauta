# AGENTS.md — Instrucciones del Proyecto Turinauta (Android)

Este archivo define el contexto permanente, las reglas de arquitectura, el sistema de diseño y las restricciones estrictas del proyecto **Turinauta** para **Gemini en Android Studio** (Chat y Agent Mode). Sigue estas instrucciones en cada generación, refactorización o revisión de código.

---

## 1. Contexto del Producto y Fase Actual

- **Nombre oficial:** **Turinauta** (nunca usar nombres alternativos).
- **Qué es:** Aplicación móvil nativa **Android** de descubrimiento local gamificado enfocada inicialmente en un piloto en **Popayán, Colombia** (con diseño e identidad visual agnósticos a la ciudad para escalar a futuro).
- **Recorrido esencial (End-to-End):**
  `Explorar catálogo/categorías/rutas (sin cuenta)` → `Consultar ficha de lugar` → `Visitar` → `Iniciar sesión (si aplica)` → `Registrar visita básica` → `Recibir recompensa digital` → `Consultar colección, progreso e historial`.
- **Fase actual (Frontend Android puro):**
  - Construir el frontend completo en **Kotlin + Jetpack Compose + Material Design 3** usando una capa de datos simulada en memoria (`Fake*RepositoryImpl`) detrás de interfaces `Repository` limpias.
  - **NO integrar todavía** Supabase SDK, Room real, claves reales ni llamadas HTTP externas. La capa de UI y los ViewModels deben quedar 100% desacoplados para enchufar **Supabase (PostgreSQL + Auth + Storage) + Room + MapLibre/OSM** en la siguiente fase sin modificar pantallas.

---

## 2. Reglas de Negocio y Alcance (Obligatorias)

### Reglas funcionales (R1–R6 + HU-24)
- **R1 (Acceso público vs. privado):** Catálogo, categorías, búsqueda, ordenamiento, fichas y rutas son consultables **sin cuenta**. Registrar visitas, ver colección, progreso e historial requieren sesión activa.
- **R2 (Cuenta privada):** Registro e inicio de sesión por correo y contraseña. Al cerrar sesión (`logout()`), limpiar inmediatamente el estado privado del usuario en memoria (HU-25).
- **R3 (Registro básico de visita):** Desde la ficha, si no hay sesión, redirigir a acceso sin guardar aún. Si hay sesión, abrir `ModalBottomSheet` de confirmación básica con botón «Cancelar». **Nunca exigir foto, QR ni GPS.**
- **R4 (Idempotencia y no duplicados):** Máximo **1 visita válida y 1 recompensa digital por cuenta y lugar**. Bloquear doble toque en estado `Loading`. Si el lugar ya fue visitado, mostrar claramente **«Visita ya registrada»** en la ficha. Si ocurre un error de guardado/red, **nunca simular éxito**; mostrar error comprensible con botón **«Reintentar»** (HU-18).
- **R5 (Integridad editorial):** Toda ficha visible muestra nombre, descripción, categoría y ubicación. Si no hay horario confirmado, mostrar exactamente **«Horario por confirmar»**.
- **R6 (Visibilidad editorial):** Los repositorios solo exponen a la UI lugares y rutas con `EditorialStatus.PUBLISHED` (ocultar `DRAFT` y `WITHDRAWN`).
- **Ordenamiento sin GPS:** El control «Ordenar» en Explorar solo ofrece **Destacados** y **Nombre (A–Z)**. No pedir permiso de ubicación.
- **Reporte interno (HU-24):** En la ficha de lugar, incluir una acción secundaria discreta **«Reportar dato incorrecto»** que abra un modal privado. Nunca presentarlo como comentario o reseña pública.
- **Modo pruebas QA (solo fase frontend):** En Perfil, incluir una sección discreta colapsable **«Modo pruebas (Simulación)»** conectada a `FakeNetworkConfig` para activar/desactivar:
  1. Simulación de error/offline al guardar visita (probar HU-18).
  2. Simulación de catálogo o rutas vacías (probar estados vacíos).

### Prohibiciones estrictas (Fuera de alcance)
- **NO** agregar pagos, reservas, comercio electrónico, premios físicos, cupones, red social, mensajería, calificaciones con estrellas, reseñas públicas ni autogestión de negocios.
- **NO** solicitar permisos de GPS, cámara ni lectura de QR.
- **NO** usar Google Maps SDK de pago ni Cloudinary.
- **NO** dejar comentarios `// TODO` sin implementar ni textos «Lorem ipsum».
- **NO** inventar comercios reales de Popayán ni métricas reales; usar únicamente datos ilustrativos controlados.

---

## 3. Sistema de Diseño Visual (Material Design 3 — Light Theme)

Estética **clara, cálida, seria y contenida** (turismo cultural y exploración local; sin degradados llamativos, neón ni estilo infantil; sin escudos, banderas ni monumentos de Popayán en el branding).

### Paleta de colores exacta (`core/designsystem/Color.kt`)
- `background`: `#FAF7F2` (Marfil cálido)
- `surface`: `#FFFFFF` (Tarjetas principales)
- `surfaceContainerLow` / `surfaceVariant`: `#F1EEE7` (Buscador, banner Rutas temáticas)
- `surfaceContainer`: `#F3EFE6` (Barra de navegación inferior)
- `surfaceContainerHigh`: `#EAE2D3` (Fondo ilustrativo de miniaturas)
- `primary`: `#135054` (Verde petróleo profundo — acciones principales, overlines, iconos activos)
- `onPrimary`: `#FFFFFF`
- `primaryContainer`: `#D6E7E5` (Chip seleccionado, pill activo en BottomBar, fondo icono Rutas)
- `onPrimaryContainer`: `#0E3A3D`
- `secondary`: `#C48A2D` (Ocre discreto — solo hitos, insignias e ilustraciones; **prohibido para texto pequeño sobre fondos claros**)
- `onBackground` / `onSurface`: `#1C252C` (Azul pizarra oscuro)
- `onSurfaceVariant`: `#4A545C` (Pizarra medio — descripciones, horarios, placeholders)
- `outline`: `#878278` (Bordes de chips no seleccionados y píldora de ciudad)
- `outlineVariant`: `#E3DED3` (Bordes sutiles `1.dp` de tarjetas)
- `error`: `#B3261E` | `errorContainer`: `#F9DEDC` | `onErrorContainer`: `#410E0B`

### Tipografía, formas y estructura de pantallas
- **Tipografía:** Escala Material 3 (Roboto / sistema). Overline de categoría en tarjetas: `LabelSmall`, `FontWeight.Bold`, `letterSpacing = 0.8.sp`, mayúsculas (`GASTRONOMÍA`, `ARTESANÍAS`, `CULTURA`), color `primary`.
- **Formas:** Tarjetas y banner de rutas `RoundedCornerShape(20.dp)` (borde `1.dp` `outlineVariant` en tarjetas blancas); miniatura cuadrada `96.dp` con `RoundedCornerShape(16.dp)` e ilustraciones vectoriales Compose en `#135054` y `#C48A2D`; buscador y píldora «Popayán» `RoundedCornerShape(28.dp)`; chips `RoundedCornerShape(12.dp)`.
- **Navegación inferior (`NavigationBar`):** Exactamente 4 destinos al mismo nivel: **Explorar**, **Rutas**, **Colección**, **Perfil**.
- **Pantalla Explorar (orden vertical):**
  1. TopBar: logo circular verde petróleo con trazo de ruta/punto ocre + «Turinauta» (izq.) y píldora outlined con pin + «Popayán» (der.).
  2. Buscador pill (`#F1EEE7`) con lupa, placeholder *"Buscar lugares por nombre"* y botón limpiar (`X`).
  3. `LazyRow` de chips: `✓ Todos` (seleccionado `#D6E7E5`), `Gastronomía`, `Artesanías`, `Cultura`, `Emprendimientos`.
  4. Banner «Rutas temáticas» (`#F1EEE7`) con icono de ruta en círculo `#D6E7E5`, subtítulo y chevron `>`.
  5. Cabecera *"Lugares para descubrir"* (izq.) + botón textual `↑↓ Ordenar` en `primary` (der.).
  6. `LazyColumn` de tarjetas blancas: miniatura izq. + categoría uppercase + nombre + descripción (máx. 2 líneas) + reloj con horario o *"Horario por confirmar"*.

---

## 4. Heurísticas Móviles y Accesibilidad (WCAG 2.2 + TalkBack)

Aplicar en todos los composables sin excepción:
- **Objetivos táctiles:** Mínimo **`48.dp x 48.dp`** en todo elemento interactivo.
- **Contraste:** Mínimo **4.5:1** en texto normal y **3:1** en texto grande o componentes.
- **No depender solo del color:** Distinguir recompensas **Obtenida** y **Pendiente** siempre con **texto + icono**.
- **Semántica TalkBack:** Usar `contentDescription` claro en iconos informativos, `null` en decorativos, y `Modifier.semantics(mergeDescendants = true)` en tarjetas y filas clicables.
- **Idioma y estados:** Español natural de Colombia/Latinoamérica. Todos los estados vacíos y de error deben explicar qué ocurrió y ofrecer una acción clara de salida («Limpiar búsqueda», «Reintentar», «Volver al catálogo»).
- **Edge-to-edge:** Respetar `Scaffold` y `WindowInsets` seguros en todas las pantallas.

---

## 5. Arquitectura y Estructura de Paquetes

Patrón **MVVM + Flujo Unidireccional de Datos (UDF)** con `StateFlow` y `CoroutineScope` (`viewModelScope`).

```text
app/src/main/java/com/turinauta/app/
├── core/
│   ├── designsystem/   # Color.kt, Type.kt, Shapes.kt, Theme.kt, Illustrations.kt
│   ├── model/          # Place, Category, Route, UserProfile, VisitRecord, RewardBadge, CollectionProgress, DataReport, EditorialStatus
│   ├── network/        # FakeNetworkConfig.kt (latencia, offline, estados vacíos)
│   └── navigation/     # Destinations.kt, TurinautaNavHost.kt, MainScaffold.kt
├── data/
│   ├── repository/     # Interfaces: CatalogRepository, RouteRepository, AuthRepository, VisitRepository, CollectionRepository, ReportRepository
│   └── fake/           # Fake*RepositoryImpl en memoria con MutableStateFlow + SeedData.kt
└── feature/
    ├── explore/        # ExploreScreen, ExploreViewModel, PlaceDetailScreen, PlaceDetailViewModel, PlaceLocationMap
    ├── routes/         # RoutesScreen, RouteDetailScreen, RoutesViewModel
    ├── auth/           # AuthScreen, AuthViewModel
    ├── visits/         # ConfirmVisitSheet, VisitResultDialog, RegisterVisitUseCase
    ├── collection/     # CollectionScreen, CollectionViewModel
    ├── profile/        # ProfileScreen, VisitHistoryScreen, ProfileViewModel
    └── reports/        # DataReportSheet, ReportViewModel
```

### Contratos preparados para Supabase (no romper al implementar)
- `EditorialStatus`: `DRAFT`, `PUBLISHED`, `WITHDRAWN`.
- `VisitRepository.registerVisit(placeId: String): VisitResult` donde `VisitResult` es `sealed interface` / `sealed class`:
  - `VisitCreated(visit: VisitRecord, reward: RewardBadge?)`
  - `VisitDuplicate(visitedAt: Long)`
  - `VisitUnauthenticated`
  - `VisitNetworkError(message: String)`
  *(Refleja el contrato exacto de la futura función RPC `register_visit` en PostgreSQL).*
- `PlaceLocationMap(latitude: Double, longitude: Double, placeName: String, addressReference: String)`: composable desacoplado sin permisos GPS, listo para reemplazar su interior por `MapLibreMap` (OpenStreetMap / OpenFreeMap) en la fase de integración.

---

## 6. Datos Ilustrativos del Piloto (`SeedData.kt`)

Usar exclusivamente estos datos ficticios ilustrativos (6 lugares `PUBLISHED` + 1 `DRAFT` oculto por R6, 2 rutas, 2 colecciones):
- **Categorías:** `Gastronomía`, `Artesanías`, `Cultura`, `Emprendimientos`.
- **Lugares publicados:**
  1. *Cocina del Barrio* (`Gastronomía`, horario `null` → *"Horario por confirmar"*, `2.4419, -76.6063`, sello *Sabores de Fogón*).
  2. *Taller de tejido* (`Artesanías`, *"Lun a vie, 9:00–12:00"*, `2.4442, -76.6021`, sello *Hilos y Telar*).
  3. *Espacio cultural La Esquina* (`Cultura`, *"Mar a sáb, 14:00–19:00"*, `2.4405, -76.6085`, sello *Acordes del Barrio*).
  4. *Amasijos de la Abuela* (`Gastronomía`, *"Mié a dom, 15:00–19:30"*, `2.4460, -76.6092`, sello *Maíz y Tradición*).
  5. *Taller de cerámica ancestral* (`Artesanías`, horario `null` → *"Horario por confirmar"*, `2.4431, -76.6074`, sello *Barro Vivo*).
  6. *Tostadora de café de origen* (`Emprendimientos`, *"Lun a sáb, 8:30–18:00"*, `2.4412, -76.6050`, sello *Grano de Altura*).
  7. *(Oculto por R6)* *Galería en preparación* (`Cultura`, `EditorialStatus.DRAFT`).
- **Rutas temáticas:** *Oficios y saberes locales* y *Sabores cotidianos de Popayán*.
- **Colecciones:** *Colección Sabores y Café* (3 sellos) y *Colección Oficios y Cultura* (3 sellos).
- **Cuenta demo en memoria:** `explorador@turinauta.app` / `Turinauta123` (permitiendo también crear nuevas cuentas).

---

## 7. Convenciones de Trabajo para el Agente (Android Studio)

- **Al crear o editar pantallas:** Mantener los composables *stateless* recibiendo `uiState` y callbacks; toda lógica y mutación de estado vive en el `ViewModel` o `UseCase`.
- **Previews:** Incluir `@Preview(showBackground = true)` envueltos en `TurinautaTheme` en cada pantalla principal.
- **Verificación antes de dar una tarea por terminada:**
  - Compilar con `./gradlew assembleDebug` para asegurar cero errores de compilación o imports rotos.
  - Ejecutar pruebas unitarias con `./gradlew testDebugUnitTest` cuando se modifiquen `UseCase`, `Repository` o `ViewModel` (especialmente reglas R3, R4 y R6).
- **Seguridad:** No hardcodear credenciales reales ni modificar `.aiexclude` para exponer `local.properties`.