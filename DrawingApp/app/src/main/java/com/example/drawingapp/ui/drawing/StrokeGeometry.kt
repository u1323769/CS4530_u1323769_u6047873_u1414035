/**
 * File:         StrokeGeometry.kt
 * Owner:        Shea
 * Contributors:
 * Phase:        1
 *
 * Purpose:
 *   Plain math helpers (no UI) so they can be unit tested.
 *
 * Used by:
 *   - DrawingCanvas
 *
 * Tested by:
 *   - StrokeGeometryTest.kt
 */
package com.example.drawingapp.ui.drawing

import androidx.compose.ui.geometry.Offset

object StrokeGeometry {

    /** Adds points between touches so no two neighbors are farther apart than [spacing]. */
    fun interpolate(points: List<Offset>, spacing: Float): List<Offset> {
        // TODO(Shea)
        return points
    }
}