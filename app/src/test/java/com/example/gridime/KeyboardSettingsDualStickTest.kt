package com.example.gridime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class KeyboardSettingsDualStickTest {

    @Test
    fun defaultsLeaveClassicNavigationUnchanged() {
        val settings = KeyboardSettings()
        assertEquals(DualStickActivationMode.DISABLED, settings.dualStickMode)
        assertEquals(ControllerButton.R3, settings.dualStickButton)
        assertEquals(DualStickCurve.LINEAR, settings.dualStickCurve)
        assertFalse(settings.dualStickKeepLast)
    }
}
