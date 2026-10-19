package com.example.gridime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DualStickControllerTest {

    private val keys = listOf(
        MappedKey("q", 0f, 0f, 10f, 10f),
        MappedKey("p", 90f, 0f, 100f, 10f),
        MappedKey("space", 20f, 12f, 80f, 22f)
    )
    private val bounds = AbsoluteKeyMapper.boundsOf(keys)!!
    private val regions = AbsoluteKeyMapper.splitRegions(bounds)

    private fun DualStickController.push(
        side: DualStickSide,
        x: Float,
        y: Float,
        keepLast: Boolean = false
    ) = update(side, x, y, keys, if (side == DualStickSide.LEFT) regions.first else regions.second, 0.15f, DualStickCurve.LINEAR, keepLast)

    @Test
    fun sticksSelectIndependently() {
        val c = DualStickController()
        c.push(DualStickSide.LEFT, -1f, -1f)
        c.push(DualStickSide.RIGHT, 1f, -1f)
        assertEquals("q", c.left.selectedKeyId)
        assertEquals("p", c.right.selectedKeyId)
        assertTrue(c.left.active)
        assertTrue(c.right.active)
    }

    @Test
    fun deadzoneClearsSelectionByDefault() {
        val c = DualStickController()
        c.push(DualStickSide.LEFT, -1f, -1f)
        c.push(DualStickSide.LEFT, 0f, 0f)
        assertFalse(c.left.active)
        assertNull(c.left.selectedKeyId)
    }

    @Test
    fun keepLastRetainsTheKeyInTheDeadzone() {
        val c = DualStickController()
        c.push(DualStickSide.LEFT, -1f, -1f, keepLast = true)
        c.push(DualStickSide.LEFT, 0f, 0f, keepLast = true)
        assertTrue(c.left.active)
        assertEquals("q", c.left.selectedKeyId)
    }

    @Test
    fun stationaryStickDoesNotAdvance() {
        val c = DualStickController()
        assertTrue(c.push(DualStickSide.LEFT, -1f, -1f))
        assertFalse(c.push(DualStickSide.LEFT, -1f, -1f))
        assertEquals("q", c.left.selectedKeyId)
    }

    @Test
    fun lastActiveFollowsTheStickThatChangedKey() {
        val c = DualStickController()
        c.push(DualStickSide.LEFT, -1f, -1f)
        c.push(DualStickSide.RIGHT, 1f, -1f)
        assertEquals(DualStickSide.RIGHT, c.lastActive)
        assertEquals(DualStickSide.RIGHT, c.confirmSide())
        c.reset()
        assertNull(c.lastActive)
        assertEquals(AnalogSelection.Inactive, c.left)
    }

    @Test
    fun confirmFallsBackWhenLastSideIsInactive() {
        val c = DualStickController()
        c.push(DualStickSide.LEFT, -1f, -1f)
        c.push(DualStickSide.RIGHT, 1f, -1f)
        c.push(DualStickSide.RIGHT, 0f, 0f)
        assertEquals(DualStickSide.LEFT, c.confirmSide())
    }
}
