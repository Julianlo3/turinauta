package com.unicauca.turinauta.core.model

/**
 * Estado editorial de lugares, rutas y colecciones (Regla R6).
 * Solo los elementos [PUBLISHED] se exponen a la interfaz pública.
 */
enum class EditorialStatus {
    DRAFT,
    PUBLISHED,
    WITHDRAWN,
}
