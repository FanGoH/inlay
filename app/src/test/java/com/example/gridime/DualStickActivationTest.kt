package com.example.gridime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DualStickActivationTest {

    private fun down(
        activation: DualStickActivation,
        mode: DualStickActivationMode,
        repeat: Boolean = false,
        isActivation: Boolean = true
    ) = activation.onKeyDown(mode, isActivation, repeat)

    @Test
    fun disabledNeverActivates() {
        val a = DualStickActivation()
        assertEquals(DualStickTransition.IGNORED, down(a, DualStickActivationMode.DISABLED))
        assertEquals(DualStickTransition.IGNORED, a.onKeyUp(DualStickActivationMode.DISABLED, true))
        assertFalse(a.active)
    }

    @Test
    fun holdIsActiveOnlyWhileTheButtonIsDown() {
        val a = DualStickActivation()
        assertEquals(DualStickTransition.ACTIVATED, down(a, DualStickActivationMode.HOLD))
        assertTrue(a.active)
        assertEquals(DualStickTransition.CONSUMED, down(a, DualStickActivationMode.HOLD, repeat = true))
        assertTrue(a.active)
        assertEquals(DualStickTransition.DEACTIVATED, a.onKeyUp(DualStickActivationMode.HOLD, true))
        assertFalse(a.active)
    }

    @Test
    fun toggleUsesOnePressPerTransition() {
        val a = DualStickActivation()
        assertEquals(DualStickTransition.ACTIVATED, down(a, DualStickActivationMode.TOGGLE))
        assertEquals(DualStickTransition.CONSUMED, down(a, DualStickActivationMode.TOGGLE, repeat = true))
        assertTrue(a.active)
        assertEquals(DualStickTransition.CONSUMED, a.onKeyUp(DualStickActivationMode.TOGGLE, true))
        assertTrue(a.active)
        assertEquals(DualStickTransition.DEACTIVATED, down(a, DualStickActivationMode.TOGGLE))
        assertFalse(a.active)
    }

    @Test
    fun repeatsDoNotRetriggerHoldOrToggle() {
        val hold = DualStickActivation()
        down(hold, DualStickActivationMode.HOLD)
        repeat(8) {
            assertEquals(DualStickTransition.CONSUMED, down(hold, DualStickActivationMode.HOLD, repeat = true))
        }
        assertTrue(hold.active)

        val toggle = DualStickActivation()
        down(toggle, DualStickActivationMode.TOGGLE)
        repeat(8) {
            assertEquals(DualStickTransition.CONSUMED, down(toggle, DualStickActivationMode.TOGGLE, repeat = true))
        }
        assertTrue(toggle.active)
    }

    @Test
    fun unrelatedButtonsAreIgnored() {
        val a = DualStickActivation()
        assertEquals(DualStickTransition.IGNORED, down(a, DualStickActivationMode.HOLD, isActivation = false))
        assertEquals(DualStickTransition.IGNORED, a.onKeyUp(DualStickActivationMode.HOLD, false))
        assertFalse(a.active)
    }

    @Test
    fun cancelClearsAStuckHold() {
        val a = DualStickActivation()
        down(a, DualStickActivationMode.HOLD)
        assertEquals(DualStickTransition.DEACTIVATED, a.cancel())
        assertFalse(a.active)
        assertEquals(DualStickTransition.IGNORED, a.cancel())
    }

    @Test
    fun cancelAlsoLeavesToggleSoFocusLossCannotStick() {
        val a = DualStickActivation()
        down(a, DualStickActivationMode.TOGGLE)
        assertEquals(DualStickTransition.DEACTIVATED, a.cancel())
        assertFalse(a.active)
    }
}
