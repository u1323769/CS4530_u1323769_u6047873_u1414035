/**
 * File:         RoutesTest.kt
 * Owner:        Serena
 * Phase:        2
 *
 * What this tests:
 *   Route strings for each screen and check that built routes match the pattern registered in the NavHost.
 *
 * How to run:
 *   ./gradlew testDebugUnitTest, or right-click > Run
 */
package com.example.drawingapp.navigation

import com.example.drawingapp.model.NEW_DRAWING_ID
import com.example.drawingapp.viewmodel.DrawingViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutesTest {

    private val idPlaceholder = "{${DrawingViewModel.DRAWING_ID_ARG}}"

    @Test
    fun drawing_withId_buildsPathRoute() {
        assertEquals("drawing/5", Routes.drawing(5))
    }

    @Test
    fun drawing_newDrawing_usesNewDrawingId() {
        assertEquals("drawing/$NEW_DRAWING_ID", Routes.drawing(NEW_DRAWING_ID))
    }

    @Test
    fun drawingPattern_containsIdPlaceholder() {
        assertTrue(Routes.DRAWING.contains(idPlaceholder))
    }

    @Test
    fun builtRoute_matchesRegisteredPattern() {
        // Filling the placeholder in the pattern must give the same route as drawing(id)
        assertEquals(Routes.drawing(7), Routes.DRAWING.replace(idPlaceholder, "7"))
    }

    @Test
    fun differentIds_giveDifferentRoutes() {
        assertNotEquals(Routes.drawing(1), Routes.drawing(2))
    }

    @Test
    fun newDrawingRoute_differsFromSavedDrawingRoute() {
        assertNotEquals(Routes.drawing(NEW_DRAWING_ID), Routes.drawing(1))
    }

    @Test
    fun routeNames_areUnique() {
        val routes = listOf(Routes.SPLASH, Routes.GALLERY, Routes.DRAWING)
        assertEquals(routes.size, routes.toSet().size)
    }
}