/**
 * File:         Stroke.kt
 * Owner:        Caroline
 * Contributors:
 * Phase:        1
 *
 * Purpose:
 *   One continuous mark: the points touched plus the pen used to draw it.
 *
 * Used by:
 *   - DrawingViewModel, DrawingCanvas
 *
 * Notes:
 *   - Each stroke keeps its own PenSettings so old strokes don't change
 *     when the pen changes.
 *   - SHARED CONTRACT. Phase 2 will save these.
 */
package com.example.drawingapp.model

import androidx.compose.ui.geometry.Offset

/** A single stroke on the canvas. */
data class Stroke(
    val points: List<Offset> = emptyList(),
    val penSettings: PenSettings = PenSettings()
)