package com.unicauca.turinauta.data.fake

import com.unicauca.turinauta.core.model.Category
import com.unicauca.turinauta.core.model.ConditionType
import com.unicauca.turinauta.core.model.EditorialStatus
import com.unicauca.turinauta.core.model.Illustration
import com.unicauca.turinauta.core.model.Place
import com.unicauca.turinauta.core.model.Route
import com.unicauca.turinauta.core.model.Stamp
import com.unicauca.turinauta.core.model.ThematicCollection
import com.unicauca.turinauta.core.model.VisitCondition

/**
 * Datos ILUSTRATIVOS del piloto (AGENTS.md §6). No representan negocios reales
 * ni convenios; se reemplazan por el catálogo autorizado cargado en Supabase.
 */
object IllustrativeData {

    val places = listOf(
        Place(
            id = "cocina-del-barrio",
            slug = "cocina-del-barrio",
            name = "Cocina del Barrio",
            category = Category.GASTRONOMIA,
            summary = "Cocina familiar con recetas tradicionales de la región.",
            description = "Cocina familiar que prepara recetas tradicionales de la región con ingredientes de productores cercanos. El menú cambia según la temporada.",
            schedule = null,
            addressReference = "Sector Centro Histórico, referencia a dos cuadras del parque principal",
            latitude = 2.4419, longitude = -76.6063,
            conditions = listOf(
                VisitCondition(ConditionType.GROUP, "Espacio familiar y pequeño; a la hora del almuerzo puede haber espera."),
                VisitCondition(ConditionType.PHOTO, "Pide permiso antes de fotografiar a quienes trabajan aquí."),
            ),
            illustration = Illustration.BOWL,
            sortOrder = 1,
        ),
        Place(
            id = "taller-de-tejido",
            slug = "taller-de-tejido",
            name = "Taller de tejido",
            category = Category.ARTESANIAS,
            summary = "Taller familiar de tejido en telar manual.",
            description = "Taller familiar donde se tejen piezas en telar manual. Puedes conocer el proceso y las piezas terminadas, que son autoría de quienes trabajan en el taller.",
            schedule = "Lun a vie, 9:00–12:00",
            addressReference = "Barrio El Empedrado, casa de portón verde",
            latitude = 2.4442, longitude = -76.6021,
            conditions = listOf(
                VisitCondition(ConditionType.GROUP, "Espacio de trabajo familiar; se recomienda visita tranquila."),
                VisitCondition(ConditionType.PHOTO, "Pide autorización antes de fotografiar piezas o personas."),
            ),
            illustration = Illustration.WEAVE,
            sortOrder = 2,
        ),
        Place(
            id = "espacio-la-esquina",
            slug = "espacio-la-esquina",
            name = "Espacio cultural La Esquina",
            category = Category.CULTURA,
            summary = "Casa de música y lectura abierta al barrio.",
            description = "Casa de música y lectura abierta al barrio, con encuentros de lectura en voz alta y presentaciones de músicos locales.",
            schedule = "Mar a sáb, 14:00–19:00",
            addressReference = "Sector Caldas, esquina sur de la plazuela",
            latitude = 2.4405, longitude = -76.6085,
            conditions = listOf(
                VisitCondition(ConditionType.INFO, "Está en zona residencial; mantén un volumen moderado al llegar y salir."),
            ),
            illustration = Illustration.CIRCLES,
            sortOrder = 3,
        ),
        Place(
            id = "amasijos-de-la-abuela",
            slug = "amasijos-de-la-abuela",
            name = "Amasijos de la Abuela",
            category = Category.GASTRONOMIA,
            summary = "Horno artesanal de envueltos, empanadas y bebidas de maíz.",
            description = "Horno artesanal donde se preparan envueltos, empanadas y bebidas de maíz siguiendo recetas de familia.",
            schedule = "Mié a dom, 15:00–19:30",
            addressReference = "Barrio Bolívar, pasaje peatonal",
            latitude = 2.4460, longitude = -76.6092,
            conditions = listOf(
                VisitCondition(ConditionType.ACCESS, "El acceso es por un pasaje peatonal con escalones."),
            ),
            illustration = Illustration.BREAD,
            sortOrder = 4,
        ),
        Place(
            id = "taller-de-ceramica",
            slug = "taller-de-ceramica",
            name = "Taller de cerámica ancestral",
            category = Category.ARTESANIAS,
            summary = "Piezas utilitarias moldeadas a mano con arcilla local.",
            description = "Taller donde se moldean a mano piezas utilitarias con arcilla local, siguiendo técnicas aprendidas de generaciones anteriores.",
            schedule = null,
            addressReference = "Sector San Francisco, calle empedrada",
            latitude = 2.4431, longitude = -76.6074,
            conditions = listOf(
                VisitCondition(ConditionType.GROUP, "Es un espacio de trabajo: visita en grupos pequeños y sin interrumpir las labores."),
                VisitCondition(ConditionType.PHOTO, "Pide autorización antes de fotografiar piezas o personas."),
            ),
            illustration = Illustration.CLAY,
            sortOrder = 5,
        ),
        Place(
            id = "tostadora-de-cafe",
            slug = "tostadora-de-cafe",
            name = "Tostadora de café de origen",
            category = Category.EMPRENDIMIENTOS,
            summary = "Pequeño emprendimiento local de tostión y cata de café caucano.",
            description = "Pequeño emprendimiento local dedicado a la tostión y la cata de café caucano de productores cercanos.",
            schedule = "Lun a sáb, 8:30–18:00",
            addressReference = "Sector Santo Domingo, local interior",
            latitude = 2.4412, longitude = -76.6050,
            conditions = listOf(
                VisitCondition(ConditionType.INFO, "Las catas son para grupos pequeños; pregunta en el local por el siguiente turno."),
            ),
            illustration = Illustration.CUP,
            sortOrder = 6,
        ),
        // Oculto por R6: nunca debe aparecer en la interfaz.
        Place(
            id = "galeria-en-preparacion",
            slug = "galeria-en-preparacion",
            name = "Galería en preparación",
            category = Category.CULTURA,
            summary = "Contenido en borrador.",
            description = "Contenido en borrador.",
            schedule = null,
            addressReference = "Por definir",
            latitude = 2.4420, longitude = -76.6060,
            conditions = emptyList(),
            illustration = Illustration.CIRCLES,
            sortOrder = 99,
            status = EditorialStatus.DRAFT,
        ),
    )

    val routes = listOf(
        Route(
            id = "oficios-y-saberes",
            slug = "oficios-y-saberes",
            name = "Oficios y saberes locales",
            summary = "Talleres y espacios donde se conserva el trabajo manual y la memoria del barrio.",
            purpose = "Reúne lugares donde se trabaja a mano y se cuida la memoria del barrio. Busca dar a conocer estos oficios y a las personas que los mantienen.",
            conditions = listOf(
                VisitCondition(ConditionType.GROUP, "Son espacios de trabajo: visita en grupos pequeños y dentro del horario de cada lugar."),
                VisitCondition(ConditionType.PHOTO, "Pide permiso antes de fotografiar piezas o personas."),
                VisitCondition(ConditionType.INFO, "Algunos lugares están en zona residencial; mantén un volumen moderado."),
            ),
            placeIds = listOf("taller-de-tejido", "taller-de-ceramica", "espacio-la-esquina"),
            hasSuggestedOrder = false,
            illustration = Illustration.WEAVE,
            sortOrder = 1,
        ),
        Route(
            id = "sabores-cotidianos",
            slug = "sabores-cotidianos",
            name = "Sabores cotidianos de Popayán",
            summary = "Cocinas y bebidas tradicionales que mantienen recetas de la región.",
            purpose = "Recorrido por cocinas, hornos y bebidas tradicionales que mantienen recetas y saberes de la región.",
            conditions = listOf(
                VisitCondition(ConditionType.GROUP, "En horas de almuerzo puede haber espera; respeta el turno de los clientes habituales."),
                VisitCondition(ConditionType.PHOTO, "Pide permiso antes de fotografiar a quienes trabajan."),
            ),
            placeIds = listOf("cocina-del-barrio", "amasijos-de-la-abuela", "tostadora-de-cafe"),
            hasSuggestedOrder = true,
            illustration = Illustration.BOWL,
            sortOrder = 2,
        ),
    )

    /** Un sello por lugar publicado. */
    val stamps = listOf(
        Stamp("stamp-cocina-del-barrio", "cocina-del-barrio", "Sello Sabores de Fogón", "Recuerda tu visita a una cocina familiar que mantiene recetas tradicionales.", Illustration.BOWL),
        Stamp("stamp-taller-de-tejido", "taller-de-tejido", "Sello Hilos y Telar", "Recuerda tu visita a un taller familiar de tejido en telar manual.", Illustration.WEAVE),
        Stamp("stamp-espacio-la-esquina", "espacio-la-esquina", "Sello Acordes del Barrio", "Recuerda tu visita a una casa de música y lectura del barrio.", Illustration.CIRCLES),
        Stamp("stamp-amasijos-de-la-abuela", "amasijos-de-la-abuela", "Sello Maíz y Tradición", "Recuerda tu visita a un horno artesanal de amasijos de maíz.", Illustration.BREAD),
        Stamp("stamp-taller-de-ceramica", "taller-de-ceramica", "Sello Barro Vivo", "Recuerda tu visita a un taller de cerámica hecha a mano.", Illustration.CLAY),
        Stamp("stamp-tostadora-de-cafe", "tostadora-de-cafe", "Sello Grano de Altura", "Recuerda tu visita a un emprendimiento de café caucano.", Illustration.CUP),
    )

    val collections = listOf(
        ThematicCollection(
            id = "sabores-y-cafe",
            slug = "sabores-y-cafe",
            name = "Colección Sabores y Café",
            description = "Cocinas, hornos y café de la región",
            illustration = Illustration.BOWL,
            stampIds = listOf("stamp-cocina-del-barrio", "stamp-amasijos-de-la-abuela", "stamp-tostadora-de-cafe"),
            sortOrder = 1,
        ),
        ThematicCollection(
            id = "oficios-y-cultura",
            slug = "oficios-y-cultura",
            name = "Colección Oficios y Cultura",
            description = "Talleres y espacios culturales del barrio",
            illustration = Illustration.WEAVE,
            stampIds = listOf("stamp-taller-de-tejido", "stamp-taller-de-ceramica", "stamp-espacio-la-esquina"),
            sortOrder = 2,
        ),
    )

    /** Cuenta Google simulada que devuelve el inicio de sesión de prueba. */
    const val DEMO_EMAIL = "explorador@turinauta.app"
    const val DEMO_NAME = "Explorador Demo"
}
