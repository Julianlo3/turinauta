# Turinauta

Aplicación móvil nativa para **Android** de descubrimiento local gamificado, enfocada inicialmente en un piloto en **Popayán, Colombia**, con identidad visual diseñada para escalar a otras ciudades.

Permite explorar lugares locales (gastronomía, artesanías, cultura y emprendimientos), recorrer rutas temáticas y coleccionar sellos digitales al validar cada visita en sitio.

- **Landing y descarga APK:** [julianlo3.github.io/turinautaWEB](https://julianlo3.github.io/turinautaWEB/)
- **Releases oficiales (APK):** [github.com/Julianlo3/turinautaWEB/releases](https://github.com/Julianlo3/turinautaWEB/releases)

---

## Funcionalidades principales

- **Exploración abierta sin cuenta:** consulta el catálogo publicado, filtra por categoría, busca por nombre, ordena por *Destacados*, *Nombre (A–Z)* o *Más cercanos* (ubicación opcional), revisa fichas de lugares, rutas temáticas y colecciones sin registrarte.
- **Rutas temáticas:** recorridos curados con paradas ordenadas e indicador de progreso sobre los lugares ya visitados.
- **Registro de visitas y sellos digitales:** al iniciar sesión con **Google**, puedes validar una visita presencial adjuntando una fotografía (JPG/PNG/WebP ≤ 5 MB) dentro de un radio de cercanía (≤ 200 m). Cada cuenta obtiene máximo una visita válida y un sello digital por lugar.
- **Colección e historial:** seguimiento de avance por colección, diferenciación accesible entre sellos obtenidos y pendientes (texto + icono) e historial fotográfico de visitas.
- **Reporte privado de datos:** opción discreta en cada ficha para reportar horarios, ubicaciones o descripciones desactualizadas sin necesidad de cuenta.

---

## Stack técnico y arquitectura

- **Lenguaje y UI:** Kotlin, Jetpack Compose y Material Design 3 (tema claro cálido).
- **Arquitectura:** MVVM + Flujo Unidireccional de Datos (UDF) con `StateFlow`, `Navigation Compose` y repositorios desacoplados.
- **Backend:** Supabase (`supabase-kt` + Ktor OkHttp):
  - **PostgreSQL / PostgREST** para catálogo editorial (`PUBLISHED`), rutas, colecciones, historial y funciones RPC (`record_visit`, `submit_content_report`).
  - **Auth** con Google OAuth y retorno mediante deep link (`turinauta://auth-callback`).
  - **Storage** (`visit-photos`) para evidencias fotográficas de visita.
- **Imágenes:** Coil Compose con ilustraciones vectoriales nativas de respaldo.
- **Compatibilidad:** Android 7.0 o superior (`minSdk = 24`).
- **Accesibilidad:** lineamientos WCAG 2.2 y TalkBack (objetivos táctiles ≥ 48 dp, contraste verificado y estados nunca dependientes solo del color).

---

## Estructura del proyecto

```text
app/src/main/java/com/unicauca/turinauta/
├── core/
│   ├── designsystem/   # Paleta Material 3, tipografía, formas, tema e ilustraciones Compose
│   ├── model/          # Modelos de dominio (Place, Route, RewardBadge, VisitRecord, etc.)
│   ├── navigation/     # Destinos, barra inferior y grafo de navegación
│   └── network/        # Configuración y cliente de Supabase
├── data/
│   ├── repository/     # Contratos/interfaces de repositorios
│   └── remote/         # DTOs, mappers, validadores e implementaciones con Supabase
└── feature/
    ├── auth/           # Inicio de sesión con Google OAuth
    ├── collection/     # Colecciones y sellos digitales
    ├── explore/        # Catálogo, filtros, detalle de lugar y mapa de ubicación
    ├── profile/        # Perfil, cierre de sesión e historial de visitas
    ├── reports/        # Modal privado para reportar datos incorrectos
    ├── routes/         # Rutas temáticas y detalle de paradas
    └── visits/         # Flujo de confirmación de visita (foto + cercanía)
```

---

## Configuración local

1. Clona el repositorio y ábrelo en **Android Studio**.
2. Agrega tus credenciales públicas de Supabase en el archivo `local.properties` en la raíz del proyecto (este archivo está ignorado en Git):

   ```properties
   SUPABASE_URL=https://tu-proyecto.supabase.co
   SUPABASE_PUBLISHABLE_KEY=sb_publishable_...
   ```

   > Usa únicamente la clave pública (`publishable`), nunca `service_role`.

3. En **Supabase Dashboard → Authentication → URL Configuration → Redirect URLs**, asegúrate de tener registrada la URL de retorno:

   ```text
   turinauta://auth-callback
   ```

---

## Compilación y pruebas

```bash
# Compilar APK de depuración
./gradlew assembleDebug

# Ejecutar pruebas unitarias (mappers, validadores y ViewModels)
./gradlew testDebugUnitTest
```

---

## Alcance editorial

Turinauta prioriza una experiencia tranquila y curada: no incluye anuncios, pagos dentro de la app, reservas, muros sociales ni calificaciones públicas con estrellas.
