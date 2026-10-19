package com.example.gridime

import android.graphics.Color
import android.view.KeyEvent

/** How Dual-stick Absolute Selection is turned on. Disabled is the default. */
enum class DualStickActivationMode(val label: String) {
    DISABLED("Disabled"),
    HOLD("Hold"),
    TOGGLE("Toggle")
}

/** How far a stick throw travels toward the edge of its keyboard region. */
enum class DualStickCurve(val label: String) {
    LINEAR("Linear"),
    SOFT("Soft"),
    SHARP("Sharp")
}

enum class DualStickSide { LEFT, RIGHT }

/**
 * One analog stick's absolute selection.
 * Neutral (deadzone) starts with [active] = false and no key.
 */
data class AnalogSelection(
    val selectedKeyId: String?,
    val active: Boolean,
    val x: Float,
    val y: Float
) {
    companion object {
        val Inactive = AnalogSelection(selectedKeyId = null, active = false, x = 0.5f, y = 0.5f)
    }
}

/** Pixel bounds of one rendered key. Positions come from the live keyboard, not a grid. */
data class MappedKey(
    val id: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f
    val width: Float get() = right - left
    val height: Float get() = bottom - top

    fun contains(x: Float, y: Float): Boolean =
        x >= left && x <= right && y >= top && y <= bottom

    /** Distance from [x],[y] to the closest point on this key. Zero when inside. */
    fun distanceSquared(x: Float, y: Float): Float {
        val cx = x.coerceIn(left, right)
        val cy = y.coerceIn(top, bottom)
        val dx = x - cx
        val dy = y - cy
        return dx * dx + dy * dy
    }
}

/** Axis-aligned keyboard (or half-keyboard) rectangle in view pixels. */
data class KeyboardRegion(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2f

    fun contains(x: Float, y: Float): Boolean =
        x >= left && x <= right && y >= top && y <= bottom

    fun intersects(key: MappedKey): Boolean =
        key.left < right && key.right > left && key.top < bottom && key.bottom > top
}

enum class DualStickTransition {
    /** Button is not the activation button, or the feature is disabled. */
    IGNORED,
    /** Event was eaten so it cannot type or toggle again, but the mode did not change. */
    CONSUMED,
    ACTIVATED,
    DEACTIVATED
}

/** Buttons that can arm Dual-stick mode. R3 is unused by Classic Inlay. */
val DUAL_STICK_ACTIVATION_BUTTONS: List<ControllerButton> = listOf(
    ControllerButton.R3,
    ControllerButton.L3,
    ControllerButton.SELECT,
    ControllerButton.L2,
    ControllerButton.R2
)

fun ControllerButton.toKeyCode(): Int = when (this) {
    ControllerButton.A -> KeyEvent.KEYCODE_BUTTON_A
    ControllerButton.B -> KeyEvent.KEYCODE_BUTTON_B
    ControllerButton.X -> KeyEvent.KEYCODE_BUTTON_X
    ControllerButton.Y -> KeyEvent.KEYCODE_BUTTON_Y
    ControllerButton.L1 -> KeyEvent.KEYCODE_BUTTON_L1
    ControllerButton.R1 -> KeyEvent.KEYCODE_BUTTON_R1
    ControllerButton.L2 -> KeyEvent.KEYCODE_BUTTON_L2
    ControllerButton.R2 -> KeyEvent.KEYCODE_BUTTON_R2
    ControllerButton.SELECT -> KeyEvent.KEYCODE_BUTTON_SELECT
    ControllerButton.START -> KeyEvent.KEYCODE_BUTTON_START
    ControllerButton.L3 -> KeyEvent.KEYCODE_BUTTON_THUMBL
    ControllerButton.R3 -> KeyEvent.KEYCODE_BUTTON_THUMBR
}

fun ControllerButton.activationLabel(): String = when (this) {
    ControllerButton.R3 -> "RS"
    ControllerButton.L3 -> "LS"
    ControllerButton.SELECT -> "Select"
    ControllerButton.L2 -> "LT"
    ControllerButton.R2 -> "RT"
    else -> name
}

/** A second accent so the right-stick highlight is never the same colour as the left. */
fun secondaryAccent(accent: Int): Int {
    val hsv = FloatArray(3)
    Color.colorToHSV(accent, hsv)
    hsv[0] = (hsv[0] + 48f) % 360f
    return Color.HSVToColor(hsv)
}
