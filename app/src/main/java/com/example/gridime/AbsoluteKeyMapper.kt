package com.example.gridime

/**
 * Maps a stick position onto the live keyboard geometry.
 * The two halves meet at the centre with no overlap. A key belongs to the
 * side that contains its centre, so a wide Space is not shared.
 */
object AbsoluteKeyMapper {

    fun splitRegions(bounds: KeyboardRegion): Pair<KeyboardRegion, KeyboardRegion> {
        val split = bounds.centerX
        val left = KeyboardRegion(bounds.left, bounds.top, split, bounds.bottom)
        val right = KeyboardRegion(split, bounds.top, bounds.right, bounds.bottom)
        return left to right
    }

    fun boundsOf(keys: List<MappedKey>): KeyboardRegion? {
        if (keys.isEmpty()) return null
        var left = Float.POSITIVE_INFINITY
        var top = Float.POSITIVE_INFINITY
        var right = Float.NEGATIVE_INFINITY
        var bottom = Float.NEGATIVE_INFINITY
        keys.forEach { key ->
            if (key.left < left) left = key.left
            if (key.top < top) top = key.top
            if (key.right > right) right = key.right
            if (key.bottom > bottom) bottom = key.bottom
        }
        return KeyboardRegion(left, top, right, bottom)
    }

    /** A key is owned by the half that contains its centre, not by rectangle overlap. */
    fun eligibleKeys(keys: List<MappedKey>, region: KeyboardRegion, side: DualStickSide): List<MappedKey> =
        keys.filter { key ->
            val cx = key.centerX
            when (side) {
                DualStickSide.LEFT -> cx >= region.left && cx < region.right
                DualStickSide.RIGHT -> cx >= region.left && cx <= region.right
            }
        }

    /**
     * Nearest eligible key to [x],[y]. Prefers a key that contains the point;
     * otherwise the key whose rectangle is closest (handles staggered rows).
     */
    fun nearest(
        x: Float,
        y: Float,
        keys: List<MappedKey>,
        region: KeyboardRegion,
        side: DualStickSide
    ): MappedKey? {
        val candidates = eligibleKeys(keys, region, side)
        if (candidates.isEmpty()) return null
        val inside = candidates.filter { it.contains(x, y) }
        if (inside.size == 1) return inside[0]
        if (inside.size > 1) {
            return inside.minWith(compareBy<MappedKey> { hypotCentre(it, x, y) }.thenBy { it.id })
        }
        return candidates.minWith(compareBy<MappedKey> { it.distanceSquared(x, y) }.thenBy { hypotCentre(it, x, y) }.thenBy { it.id })
    }

    fun mapStick(
        normalizedX: Float,
        normalizedY: Float,
        keys: List<MappedKey>,
        region: KeyboardRegion,
        side: DualStickSide
    ): MappedKey? {
        val (x, y) = AnalogStickMath.mapToRegion(normalizedX, normalizedY, region)
        return nearest(x, y, keys, region, side)
    }

    private fun hypotCentre(key: MappedKey, x: Float, y: Float): Float {
        val dx = x - key.centerX
        val dy = y - key.centerY
        return dx * dx + dy * dy
    }
}
