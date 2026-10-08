/**
 * File:         PenOptionsTest.kt
 * Owner:        Caroline
 * Phase:        1
 *
 * Tests PenOptions class
 */
package com.example.drawingapp.ui.toolbar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PenOptionsTest {

    private val delta = 0.0001f

    @Test
    fun `size in range is unchanged`() {
        assertEquals(20f, PenOptions.clampSize(20f), delta)
    }

    @Test
    fun `size below minimum is raised to minimum`() {
        assertEquals(PenOptions.MIN_SIZE, PenOptions.clampSize(-5f), delta)
    }

    @Test
    fun `size above maximum is lowered to maximum`() {
        assertEquals(PenOptions.MAX_SIZE, PenOptions.clampSize(500f), delta)
    }

    @Test
    fun `exact min and max are allowed`() {
        assertEquals(PenOptions.MIN_SIZE, PenOptions.clampSize(PenOptions.MIN_SIZE), delta)
        assertEquals(PenOptions.MAX_SIZE, PenOptions.clampSize(PenOptions.MAX_SIZE), delta)
    }

    @Test
    fun `NaN falls back to default size`() {
        assertEquals(PenOptions.DEFAULT_SIZE, PenOptions.clampSize(Float.NaN), delta)
    }

    @Test
    fun `infinity is clamped to the range`() {
        assertEquals(PenOptions.MAX_SIZE, PenOptions.clampSize(Float.POSITIVE_INFINITY), delta)
        assertEquals(PenOptions.MIN_SIZE, PenOptions.clampSize(Float.NEGATIVE_INFINITY), delta)
    }

    @Test
    fun `size range is valid`() {
        assertTrue(PenOptions.MIN_SIZE < PenOptions.MAX_SIZE)
    }

    @Test
    fun `default size is inside the range`() {
        assertTrue(PenOptions.DEFAULT_SIZE in PenOptions.MIN_SIZE..PenOptions.MAX_SIZE)
    }

    @Test
    fun `default color is in the palette`() {
        assertTrue(PenOptions.DEFAULT_COLOR in PenOptions.palette)
    }

    @Test
    fun `palette is not empty`() {
        assertTrue(PenOptions.palette.isNotEmpty())
    }

    @Test
    fun `palette has no duplicate colors`() {
        assertEquals(PenOptions.palette.size, PenOptions.palette.toSet().size)
    }
}