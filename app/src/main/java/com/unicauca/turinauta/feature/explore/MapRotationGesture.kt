package com.unicauca.turinauta.feature.explore

import kotlin.math.abs
import kotlin.math.sign

/** Keeps incidental twists during pinch zoom from changing the map bearing. */
internal class MapRotationGesture(private val deadZoneDegrees: Float = 12f) {
    private var previousTouchAngle = 0f
    private var startingMapBearing = 0f
    private var totalTurn = 0f
    private var active = false
    private var rotating = false

    fun begin(touchAngle: Float, mapBearing: Float) {
        previousTouchAngle = touchAngle
        startingMapBearing = mapBearing
        totalTurn = 0f
        active = true
        rotating = false
    }

    /** Returns a new map bearing only after the gesture crosses the dead zone. */
    fun move(touchAngle: Float): Float? {
        if (!active) return null
        totalTurn += ((touchAngle - previousTouchAngle + 540f) % 360f) - 180f
        previousTouchAngle = touchAngle
        if (!rotating && abs(totalTurn) <= deadZoneDegrees) return null
        rotating = true
        val effectiveTurn = if (abs(totalTurn) <= deadZoneDegrees) 0f
        else totalTurn - deadZoneDegrees * sign(totalTurn)
        return startingMapBearing + effectiveTurn
    }

    fun end() {
        active = false
        rotating = false
    }
}
