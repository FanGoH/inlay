package com.example.gridime

/**
 * Independent left/right absolute selections. Stick motion only updates the
 * highlight; it never commits a character.
 */
class DualStickController {

    var left: AnalogSelection = AnalogSelection.Inactive
        private set
    var right: AnalogSelection = AnalogSelection.Inactive
        private set
    var lastActive: DualStickSide? = null
        private set

    fun reset() {
        left = AnalogSelection.Inactive
        right = AnalogSelection.Inactive
        lastActive = null
    }

    fun selection(side: DualStickSide): AnalogSelection = if (side == DualStickSide.LEFT) left else right

    /**
     * Update one stick from raw −1…1 axes. Returns true when the selected key
     * (or active flag) changed, so the view can redraw without a move sound
     * on every analog sample.
     */
    fun update(
        side: DualStickSide,
        rawX: Float,
        rawY: Float,
        keys: List<MappedKey>,
        region: KeyboardRegion,
        deadzone: Float,
        curve: DualStickCurve,
        keepLast: Boolean
    ): Boolean {
        val prepared = AnalogStickMath.prepare(rawX, rawY, deadzone, curve)
        val current = selection(side)
        val next = if (prepared == null) {
            if (keepLast && current.selectedKeyId != null) {
                current.copy(active = true)
            } else {
                AnalogSelection.Inactive
            }
        } else {
            val nx = AnalogStickMath.normalize(prepared.first)
            val ny = AnalogStickMath.normalize(prepared.second)
            val (x, y) = AnalogStickMath.mapToRegion(nx, ny, region)
            val key = AbsoluteKeyMapper.nearest(x, y, keys, region)
            AnalogSelection(selectedKeyId = key?.id, active = key != null, x = x, y = y)
        }
        val changed = next.selectedKeyId != current.selectedKeyId || next.active != current.active
        if (prepared != null && next.selectedKeyId != null && next.selectedKeyId != current.selectedKeyId) {
            lastActive = side
        }
        if (side == DualStickSide.LEFT) left = next else right = next
        return changed
    }

    fun confirmSide(): DualStickSide? {
        lastActive?.let { side ->
            val sel = selection(side)
            if (sel.active && sel.selectedKeyId != null) return side
        }
        if (left.active && left.selectedKeyId != null) return DualStickSide.LEFT
        if (right.active && right.selectedKeyId != null) return DualStickSide.RIGHT
        return null
    }
}
