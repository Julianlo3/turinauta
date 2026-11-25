package com.unicauca.turinauta.core.location

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceLocationPolicyTest {
    @Test fun mapAcceptsRecentPositionButRejectsStaleOne() {
        assertTrue(isUsableLocation(30_000, 100f, LocationPurpose.MAP))
        assertFalse(isUsableLocation(120_000, 20f, LocationPurpose.MAP))
    }

    @Test fun visitRequiresFreshAccuratePosition() {
        assertTrue(isUsableLocation(10_000, 30f, LocationPurpose.VISIT))
        assertFalse(isUsableLocation(10_000, 120f, LocationPurpose.VISIT))
        assertFalse(isUsableLocation(40_000, 10f, LocationPurpose.VISIT))
        assertFalse(isUsableLocation(10_000, null, LocationPurpose.VISIT))
    }
}
