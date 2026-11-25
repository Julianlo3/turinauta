package com.unicauca.turinauta.core.model

import java.util.Locale

/**
 * Categoría activa del catálogo público.
 * Alineada con `GET /categories?select=id,name,slug,sort_order&order=sort_order.asc`.
 */
data class Category(
    val id: String,
    val name: String,
    val slug: String,
    val sortOrder: Int = 0,
) {
    val uppercaseLabel: String
        get() = name.uppercase(Locale.forLanguageTag("es-CO"))
}
