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
import androidx.compose.ui.geometry.lerp
import kotlin.math.ceil

object StrokeGeometry {

    /** Adds points between touches so no two neighbors are farther apart than [spacing]. */
    fun interpolate(points: List<Offset>, spacing: Float): List<Offset> {
        if (points.size < 2 || spacing <= 0f) return points

        val result = ArrayList<Offset>(points.size)
        result.add(points[0])

        for(i in 1 until points.size) {
            val a = points[i - 1]
            val b = points[i]
            val segments = ceil((b - a).getDistance() / spacing).toInt()
            // Insert evenly spaced points between a and b
            for(segment in 1 until segments) {
                result.add(lerp(a, b, segment / segments.toFloat()))
            }
            result.add(b)
        }
        return result
    }
}