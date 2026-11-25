package com.unicauca.turinauta.feature.explore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapRotationGestureTest {
    @Test fun pinchWithSmallTwistDoesNotRotateMap() {
        val gesture = MapRotationGesture()
        gesture.begin(0f, 30f)
        assertNull(gesture.move(5f))
        assertNull(gesture.move(11f))
        assertEquals(33f, gesture.move(15f)!!, 0.001f)
        assertEquals(43f, gesture.move(25f)!!, 0.001f)
        assertEquals(30f, gesture.move(10f)!!, 0.001f)
    }

    @Test fun crossingAngleWrapAndNewGestureStartWithoutJump() {
        val gesture = MapRotationGesture()
        gesture.begin(179f, 60f)
        assertNull(gesture.move(-179f))
        assertEquals(64f, gesture.move(-165f)!!, 0.001f)
        gesture.end()
        assertNull(gesture.move(-150f))
        gesture.begin(10f, 64f)
        assertNull(gesture.move(8f))
    }
}
