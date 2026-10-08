/**
 * File:         StrokeGeometryTest.kt
 * Owner:        Shea
 * Contributors: Caroline (wrote initial tests)
 * Phase:        1
 *
 * What this tests:
 *   StrokeGeometry.interpolate(): filling gaps between touch points so
 *   stamped shapes (SQUARE, TRIANGLE) don't look dotted. Checks that
 *   gaps are never bigger than the spacing, the original points and
 *   their order are kept, new points stay on the line, and edge cases
 *   (empty, single point, duplicate points) don't break.
 *
 * How to run:
 *   ./gradlew testDebugUnitTest, or right-click this file > Run
 */
package com.example.drawingapp.ui.drawing

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrokeGeometryTest {

    private val delta = 0.001f

    /** True if two points are the same, allowing for tiny float rounding. */
    private fun close(a: Offset, b: Offset) = (a - b).getDistance() < delta

    /** Fails if any two neighboring points are farther apart than [spacing]. */
    private fun assertNoGapsBiggerThan(points: List<Offset>, spacing: Float) {
        points.zipWithNext().forEach { (a, b) ->
            val gap = (b - a).getDistance()
            assertTrue("Gap of $gap between $a and $b is bigger than $spacing", gap <= spacing + delta)
        }
    }

    @Test
    fun `empty list stays empty`() {
        assertTrue(StrokeGeometry.interpolate(emptyList(), spacing = 2f).isEmpty())
    }

    @Test
    fun `single point is returned unchanged`() {
        val points = listOf(Offset(5f, 5f))
        assertEquals(points, StrokeGeometry.interpolate(points, spacing = 2f))
    }

    @Test
    fun `points already close together get no extra points`() {
        val points = listOf(Offset(0f, 0f), Offset(1f, 0f))
        assertEquals(2, StrokeGeometry.interpolate(points, spacing = 5f).size)
    }

    @Test
    fun `duplicate points do not crash`() {
        val points = listOf(Offset(3f, 3f), Offset(3f, 3f), Offset(3f, 3f))
        val result = StrokeGeometry.interpolate(points, spacing = 2f)

        assertTrue(result.isNotEmpty())
        result.forEach { assertTrue(close(it, Offset(3f, 3f))) }
    }

    @Test
    fun `gaps on a horizontal line are filled`() {
        val result = StrokeGeometry.interpolate(listOf(Offset(0f, 0f), Offset(10f, 0f)), spacing = 2f)

        assertTrue(result.size > 2)
        assertNoGapsBiggerThan(result, 2f)
    }

    @Test
    fun `gaps on a diagonal line are filled`() {
        val result = StrokeGeometry.interpolate(listOf(Offset(0f, 0f), Offset(30f, 40f)), spacing = 5f)
        assertNoGapsBiggerThan(result, 5f)
    }

    @Test
    fun `gaps are filled across several segments`() {
        val points = listOf(Offset(0f, 0f), Offset(10f, 0f), Offset(10f, 10f), Offset(0f, 10f))
        val result = StrokeGeometry.interpolate(points, spacing = 3f)

        assertNoGapsBiggerThan(result, 3f)
    }

    @Test
    fun `smaller spacing adds more points`() {
        val points = listOf(Offset(0f, 0f), Offset(100f, 0f))

        val coarse = StrokeGeometry.interpolate(points, spacing = 20f)
        val fine = StrokeGeometry.interpolate(points, spacing = 5f)

        assertTrue(fine.size > coarse.size)
    }

    @Test
    fun `first and last points are kept`() {
        val start = Offset(0f, 0f)
        val end = Offset(30f, 40f)
        val result = StrokeGeometry.interpolate(listOf(start, end), spacing = 5f)

        assertTrue(close(start, result.first()))
        assertTrue(close(end, result.last()))
    }

    @Test
    fun `every original point is still in the result`() {
        val points = listOf(Offset(0f, 0f), Offset(10f, 0f), Offset(10f, 10f))
        val result = StrokeGeometry.interpolate(points, spacing = 3f)

        points.forEach { original ->
            assertTrue("Missing original point $original", result.any { close(it, original) })
        }
    }

    @Test
    fun `new points stay on the line between touches`() {
        // Every point on the line from (0,0) to (10,10) has x == y
        val result = StrokeGeometry.interpolate(listOf(Offset(0f, 0f), Offset(10f, 10f)), spacing = 2f)

        result.forEach { assertEquals(it.x, it.y, delta) }
    }

    @Test
    fun `points stay in drawing order`() {
        // Moving left to right, x should never go backwards
        val result = StrokeGeometry.interpolate(listOf(Offset(0f, 0f), Offset(20f, 0f)), spacing = 3f)

        result.zipWithNext().forEach { (a, b) -> assertTrue(b.x >= a.x - delta) }
    }
}