package com.example.gridime

/**
 * Disabled / Hold / Toggle for Dual-stick Absolute Selection.
 *
 * Hold: the mode is on only while the activation button is down.
 * Toggle: one press (repeatCount == 0) enters, the next press leaves.
 * Repeats never toggle. Releasing the button never types a character.
 */
class DualStickActivation {

    var active: Boolean = false
        private set

    fun onKeyDown(mode: DualStickActivationMode, isActivationButton: Boolean, repeat: Boolean): DualStickTransition {
        if (!isActivationButton || mode == DualStickActivationMode.DISABLED) return DualStickTransition.IGNORED
        if (repeat) return DualStickTransition.CONSUMED
        return when (mode) {
            DualStickActivationMode.HOLD -> {
                if (active) DualStickTransition.CONSUMED
                else {
                    active = true
                    DualStickTransition.ACTIVATED
                }
            }
            DualStickActivationMode.TOGGLE -> {
                active = !active
                if (active) DualStickTransition.ACTIVATED else DualStickTransition.DEACTIVATED
            }
            DualStickActivationMode.DISABLED -> DualStickTransition.IGNORED
        }
    }

    fun onKeyUp(mode: DualStickActivationMode, isActivationButton: Boolean): DualStickTransition {
        if (!isActivationButton || mode == DualStickActivationMode.DISABLED) return DualStickTransition.IGNORED
        return when (mode) {
            DualStickActivationMode.HOLD -> {
                if (!active) DualStickTransition.CONSUMED
                else {
                    active = false
                    DualStickTransition.DEACTIVATED
                }
            }
            DualStickActivationMode.TOGGLE -> DualStickTransition.CONSUMED
            DualStickActivationMode.DISABLED -> DualStickTransition.IGNORED
        }
    }

    /** Focus lost, keyboard hidden, or input restarted: Hold must never stay stuck. */
    fun cancel(): DualStickTransition {
        if (!active) return DualStickTransition.IGNORED
        active = false
        return DualStickTransition.DEACTIVATED
    }

    fun reset() {
        active = false
    }
}
