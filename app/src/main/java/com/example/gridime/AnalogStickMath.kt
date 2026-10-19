package com.example.gridime

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sqrt

/**
 * Pure analog-stick maths used by Dual-stick Absolute Selection.
 * No Android types, so unit tests can run on the JVM.
 */
object AnalogStickMath {

    fun magnitude(x: Float, y: Float): Float = hypot(x, y)

    /**
     * Radial deadzone, then remap the remaining throw to −1…1.
     * Returns (0, 0) when the stick is inside the deadzone.
     */
    fun applyDeadzone(x: Float, y: Float, deadzone: Float): Pair<Float, Float> {
        val cx = x.coerceIn(-1f, 1f)
        val cy = y.coerceIn(-1f, 1f)
        val zone = deadzone.coerceIn(0f, 0.95f)
        val mag = hypot(cx, cy)
        if (mag < zone || mag == 0f) return 0f to 0f
        val scaled = ((mag - zone) / (1f - zone)).coerceIn(0f, 1f)
        return (cx / mag) * scaled to (cy / mag) * scaled
    }

    fun applyCurve(value: Float, curve: DualStickCurve): Float {
        val magnitude = abs(value).coerceIn(0f, 1f)
        val shaped = when (curve) {
            DualStickCurve.LINEAR -> magnitude
            DualStickCurve.SOFT -> magnitude * magnitude
            DualStickCurve.SHARP -> sqrt(magnitude)
        }
        return sign(value) * shaped
    }

    fun applyCurve(x: Float, y: Float, curve: DualStickCurve): Pair<Float, Float> {
        val mag = hypot(x, y)
        if (mag == 0f) return 0f to 0f
        val shaped = applyCurve(mag.coerceIn(0f, 1f), curve)
        return (x / mag) * shaped to (y / mag) * shaped
    }

    /**
     * Deadzone, then the chosen response curve. Null when the stick is at rest
     * so callers do not treat the centre as a real aim point.
     */
    fun prepare(rawX: Float, rawY: Float, deadzone: Float, curve: DualStickCurve): Pair<Float, Float>? {
        val (x, y) = applyDeadzone(rawX, rawY, deadzone)
        if (x == 0f && y == 0f) return null
        return applyCurve(x, y, curve)
    }

    /** Map a −1…1 stick sample onto 0…1. Up is negative on Android joysticks. */
    fun normalize(stick: Float): Float = ((stick + 1f) / 2f).coerceIn(0f, 1f)

    fun mapToRegion(normalizedX: Float, normalizedY: Float, region: KeyboardRegion): Pair<Float, Float> {
        val x = region.left + normalizedX.coerceIn(0f, 1f) * region.width
        val y = region.top + normalizedY.coerceIn(0f, 1f) * region.height
        return x to y
    }

    /**
     * Android Xbox pads usually put the right stick on Z / RZ.
     * Generic pads sometimes use RX / RY. Prefer the pair that is actually moving.
     */
    fun pickRightStick(z: Float, rz: Float, rx: Float, ry: Float): Pair<Float, Float> {
        val xbox = hypot(z, rz)
        val generic = hypot(rx, ry)
        return if (generic > xbox) rx to ry else z to rz
    }

    /** Slider 0…10 → radial deadzone. Default 3 ≈ 0.16. */
    fun deadzoneFromSlider(level: Int): Float = 0.05f + level.coerceIn(0, 10) * 0.035f

    /** Slider 0…10 → extra inward aim travel. Default 6 ≈ 24 %. */
    fun innerReachFromSlider(level: Int): Float = level.coerceIn(0, 10) * 0.04f

    fun almostEqual(a: Float, b: Float, epsilon: Float = 0.0001f): Boolean = abs(a - b) < epsilon
}
