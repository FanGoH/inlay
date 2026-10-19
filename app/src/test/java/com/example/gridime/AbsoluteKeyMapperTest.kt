package com.example.gridime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AbsoluteKeyMapperTest {

    private fun key(id: String, left: Float, top: Float, right: Float, bottom: Float) =
        MappedKey(id, left, top, right, bottom)

    /** Staggered QWERTY-ish row plus a wide space that straddles the split. */
    private fun staggeredKeyboard(): List<MappedKey> {
        val qwerty = "qwertyuiop".mapIndexed { i, c ->
            key(c.toString(), i * 10f, 0f, i * 10f + 10f, 12f)
        }
        val asdf = "asdfghjkl".mapIndexed { i, c ->
            key(c.toString(), 5f + i * 10f, 14f, 15f + i * 10f, 26f)
        }
        val space = key("space", 20f, 28f, 80f, 40f)
        return qwerty + asdf + space
    }

    @Test
    fun mappingRegionReachesInwardWithoutSharingKeys() {
        val bounds = KeyboardRegion(0f, 0f, 100f, 40f)
        val (left, right) = AbsoluteKeyMapper.splitRegions(bounds)
        val leftAim = AbsoluteKeyMapper.mappingRegion(left, DualStickSide.LEFT, 0.24f)
        val rightAim = AbsoluteKeyMapper.mappingRegion(right, DualStickSide.RIGHT, 0.24f)
        assertTrue(leftAim.right > left.right)
        assertTrue(rightAim.left < right.left)
        val keys = listOf(key("t", 40f, 0f, 50f, 10f), key("y", 50f, 0f, 60f, 10f))
        assertTrue(AbsoluteKeyMapper.eligibleKeys(keys, left, DualStickSide.LEFT).all { it.id == "t" })
        assertTrue(AbsoluteKeyMapper.eligibleKeys(keys, right, DualStickSide.RIGHT).all { it.id == "y" })
    }

    @Test
    fun splitMeetsAtTheCentreWithNoOverlap() {
        val bounds = KeyboardRegion(0f, 0f, 100f, 40f)
        val (left, right) = AbsoluteKeyMapper.splitRegions(bounds)
        assertEquals(0f, left.left, 0.001f)
        assertEquals(50f, left.right, 0.001f)
        assertEquals(50f, right.left, 0.001f)
        assertEquals(100f, right.right, 0.001f)
        assertEquals(left.right, right.left, 0.001f)
    }

    @Test
    fun leftStickTopLeftSelectsQ() {
        val keys = staggeredKeyboard()
        val bounds = AbsoluteKeyMapper.boundsOf(keys)!!
        val (left, _) = AbsoluteKeyMapper.splitRegions(bounds)
        val hit = AbsoluteKeyMapper.mapStick(0f, 0f, keys, left, DualStickSide.LEFT)
        assertEquals("q", hit?.id)
    }

    @Test
    fun rightStickTopRightSelectsP() {
        val keys = staggeredKeyboard()
        val bounds = AbsoluteKeyMapper.boundsOf(keys)!!
        val (_, right) = AbsoluteKeyMapper.splitRegions(bounds)
        val hit = AbsoluteKeyMapper.mapStick(1f, 0f, keys, right, DualStickSide.RIGHT)
        assertEquals("p", hit?.id)
    }

    @Test
    fun staggerDoesNotPickTheKeyDirectlyAbove() {
        val keys = staggeredKeyboard()
        val bounds = AbsoluteKeyMapper.boundsOf(keys)!!
        val (left, _) = AbsoluteKeyMapper.splitRegions(bounds)
        // Centre of asdf's "a" is at x=10, y=20. That is under qwerty's "w" column.
        val hit = AbsoluteKeyMapper.nearest(10f, 20f, keys, left, DualStickSide.LEFT)
        assertEquals("a", hit?.id)
    }

    @Test
    fun spaceBelongsToTheSideThatOwnsItsCentre() {
        val keys = staggeredKeyboard()
        val bounds = AbsoluteKeyMapper.boundsOf(keys)!!
        val (left, right) = AbsoluteKeyMapper.splitRegions(bounds)
        val leftHas = AbsoluteKeyMapper.eligibleKeys(keys, left, DualStickSide.LEFT).any { it.id == "space" }
        val rightHas = AbsoluteKeyMapper.eligibleKeys(keys, right, DualStickSide.RIGHT).any { it.id == "space" }
        assertTrue(leftHas != rightHas)
        assertTrue(leftHas || rightHas)
    }

    @Test
    fun emptyKeyboardHasNoHit() {
        val region = KeyboardRegion(0f, 0f, 10f, 10f)
        assertEquals(null, AbsoluteKeyMapper.nearest(5f, 5f, emptyList(), region, DualStickSide.LEFT))
        assertEquals(null, AbsoluteKeyMapper.boundsOf(emptyList()))
    }

    @Test
    fun layoutChangeRebuildsFromNewGeometry() {
        val portrait = listOf(key("a", 0f, 0f, 10f, 10f), key("b", 10f, 0f, 20f, 10f))
        val landscape = listOf(key("a", 0f, 0f, 40f, 8f), key("b", 40f, 0f, 80f, 8f))
        val portraitHit = AbsoluteKeyMapper.nearest(15f, 5f, portrait, AbsoluteKeyMapper.boundsOf(portrait)!!, DualStickSide.RIGHT)
        val landscapeHit = AbsoluteKeyMapper.nearest(15f, 5f, landscape, AbsoluteKeyMapper.boundsOf(landscape)!!, DualStickSide.LEFT)
        assertEquals("b", portraitHit?.id)
        assertEquals("a", landscapeHit?.id)
    }

    @Test
    fun nearestPrefersContainingKeyOverNeighbourCentre() {
        val wide = key("wide", 0f, 0f, 80f, 10f)
        val tiny = key("tiny", 70f, 0f, 80f, 10f)
        val region = KeyboardRegion(0f, 0f, 80f, 10f)
        assertEquals("wide", AbsoluteKeyMapper.nearest(20f, 5f, listOf(wide, tiny), region, DualStickSide.LEFT)?.id)
        assertNotNull(AbsoluteKeyMapper.nearest(75f, 5f, listOf(wide, tiny), region, DualStickSide.RIGHT))
    }
}
