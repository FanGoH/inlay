package com.example.gridime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.hypot

class AnalogStickMathTest {

    @Test
    fun deadzoneSwallowsCentre() {
        val (x, y) = AnalogStickMath.applyDeadzone(0.05f, -0.04f, 0.2f)
        assertEquals(0f, x)
        assertEquals(0f, y)
        assertNull(AnalogStickMath.prepare(0.05f, -0.04f, 0.2f, DualStickCurve.LINEAR))
    }

    @Test
    fun deadzoneRemapsEdgeToFullThrow() {
        val (x, y) = AnalogStickMath.applyDeadzone(1f, 0f, 0.2f)
        assertEquals(1f, x, 0.001f)
        assertEquals(0f, y, 0.001f)
    }

    @Test
    fun valuesAreClampedBeforeNormalising() {
        val (x, y) = AnalogStickMath.applyDeadzone(3f, -4f, 0f)
        assertTrue(hypot(x, y) <= 1.001f)
        assertEquals(1f, AnalogStickMath.normalize(2f), 0.0001f)
        assertEquals(0f, AnalogStickMath.normalize(-2f), 0.0001f)
    }

    @Test
    fun normalizeMapsUpToTopOfRegion() {
        // Android joysticks: up is negative Y → top of the keyboard (normalized 0).
        assertEquals(0f, AnalogStickMath.normalize(-1f), 0.0001f)
        assertEquals(1f, AnalogStickMath.normalize(1f), 0.0001f)
        assertEquals(0.5f, AnalogStickMath.normalize(0f), 0.0001f)
    }

    @Test
    fun mapToRegionUsesLiveBounds() {
        val region = KeyboardRegion(10f, 20f, 110f, 80f)
        val (x, y) = AnalogStickMath.mapToRegion(0f, 0f, region)
        assertEquals(10f, x, 0.001f)
        assertEquals(20f, y, 0.001f)
        val (x2, y2) = AnalogStickMath.mapToRegion(1f, 1f, region)
        assertEquals(110f, x2, 0.001f)
        assertEquals(80f, y2, 0.001f)
    }

    @Test
    fun softCurveStaysNearCentreLonger() {
        val linear = AnalogStickMath.applyCurve(0.5f, DualStickCurve.LINEAR)
        val soft = AnalogStickMath.applyCurve(0.5f, DualStickCurve.SOFT)
        val sharp = AnalogStickMath.applyCurve(0.5f, DualStickCurve.SHARP)
        assertTrue(soft < linear)
        assertTrue(sharp > linear)
    }

    @Test
    fun pickRightStickPrefersTheMovingPair() {
        val xbox = AnalogStickMath.pickRightStick(0.8f, -0.1f, 0f, 0f)
        assertEquals(0.8f, xbox.first, 0.0001f)
        val generic = AnalogStickMath.pickRightStick(0f, 0f, -0.6f, 0.2f)
        assertEquals(-0.6f, generic.first, 0.0001f)
    }

    @Test
    fun deadzoneSliderStaysInRange() {
        assertTrue(AnalogStickMath.deadzoneFromSlider(0) > 0f)
        assertTrue(AnalogStickMath.deadzoneFromSlider(10) < 0.5f)
        assertTrue(AnalogStickMath.deadzoneFromSlider(3) in 0.14f..0.18f)
    }

    @Test
    fun prepareReturnsAxesAfterDeadzone() {
        val prepared = AnalogStickMath.prepare(1f, 0f, 0.15f, DualStickCurve.LINEAR)
        assertNotNull(prepared)
        assertTrue(abs(prepared!!.first) > 0.9f)
    }
}
