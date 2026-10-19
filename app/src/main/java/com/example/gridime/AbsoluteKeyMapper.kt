package com.example.gridime

/**
 * Maps a stick position onto the live keyboard geometry.
 * Left and right sticks each own one horizontal region; keys that straddle
 * the split (Space, a wide Enter) can be reached from both sides.
 */
object AbsoluteKeyMapper {

    const val DEFAULT_OVERLAP = 0.08f

    fun splitRegions(bounds: KeyboardRegion, overlapFraction: Float = DEFAULT_OVERLAP): Pair<KeyboardRegion, KeyboardRegion> {
        val overlap = bounds.width * overlapFraction.coerceIn(0f, 0.45f)
        val split = bounds.centerX
        val left = KeyboardRegion(bounds.left, bounds.top, (split + overlap).coerceAtMost(bounds.right), bounds.bottom)
        val right = KeyboardRegion((split - overlap).coerceAtLeast(bounds.left), bounds.top, bounds.right, bounds.bottom)
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

    fun eligibleKeys(keys: List<MappedKey>, region: KeyboardRegion): List<MappedKey> =
        keys.filter { region.intersects(it) }

    /**
     * Nearest eligible key to [x],[y]. Prefers a key that contains the point;
     * otherwise the key whose rectangle is closest (handles staggered rows
     * and wide keys such as Space).
     */
    fun nearest(x: Float, y: Float, keys: List<MappedKey>, region: KeyboardRegion): MappedKey? {
        val candidates = eligibleKeys(keys, region)
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
        region: KeyboardRegion
    ): MappedKey? {
        val (x, y) = AnalogStickMath.mapToRegion(normalizedX, normalizedY, region)
        return nearest(x, y, keys, region)
    }

    private fun hypotCentre(key: MappedKey, x: Float, y: Float): Float {
        val dx = x - key.centerX
        val dy = y - key.centerY
        return dx * dx + dy * dy
    }
}
