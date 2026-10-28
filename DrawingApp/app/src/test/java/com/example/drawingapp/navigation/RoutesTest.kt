/**
 * File:         RoutesTest.kt
 * Owner:        Serena
 * Phase:        2
 *
 * What this tests:
 *   Route strings for each screen and converting the nav argument back to a drawing id.
 *
 * How to run:
 *   ./gradlew testDebugUnitTest, or right-click > Run
 */
package com.example.drawingapp.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutesTest {

    @Test
    fun drawing_withoutId_isNewDrawingRoute() {
        assertEquals("drawing", Routes.drawing())
        assertEquals("drawing", Routes.drawing(null))
    }

    @Test
    fun drawing_withId_includesIdArgument() {
        assertEquals("drawing?drawingId=5", Routes.drawing(5))
    }

    @Test
    fun drawingPattern_containsIdPlaceholder() {
        assertTrue(Routes.DRAWING.contains("{${Routes.DRAWING_ID_ARG}}"))
    }

    @Test
    fun drawingPattern_matchesBuiltRoutes() {
        // The pattern and the built routes must share the same base and arg name
        val base = Routes.DRAWING.substringBefore("?")
        assertTrue(Routes.drawing().startsWith(base))
        assertTrue(Routes.drawing(7).startsWith("$base?${Routes.DRAWING_ID_ARG}="))
    }

    @Test
    fun toDrawingIdOrNull_positiveId_returnsId() {
        assertEquals(42L, Routes.toDrawingIdOrNull(42L))
    }

    @Test
    fun toDrawingIdOrNull_defaultValue_returnsNull() {
        assertNull(Routes.toDrawingIdOrNull(Routes.NO_DRAWING_ID))
    }

    @Test
    fun toDrawingIdOrNull_zero_returnsNull() {
        // Room uses 0 for "not saved yet", so it is never a real id
        assertNull(Routes.toDrawingIdOrNull(0L))
    }

    @Test
    fun routeNames_areUnique() {
        val routes = listOf(Routes.SPLASH, Routes.GALLERY, Routes.DRAWING)
        assertEquals(routes.size, routes.toSet().size)
    }
}