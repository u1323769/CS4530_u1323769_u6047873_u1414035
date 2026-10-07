/**
 * File:         Stroke.kt
 * Owner:        Caroline
 * Contributors:
 * Phase:        1
 *
 * Purpose:
 *   One continuous mark on the canvas: every point touched between finger
 *   down and finger up, plus the pen settings used to draw it.
 *
 * Used by:
 *   - DrawingViewModel (finished strokes + the one in progress)
 *   - DrawingCanvas (renders them)
 */
package com.example.drawingapp.model

import androidx.compose.ui.geometry.Offset

/** A single stroke on the canvas. */
data class Stroke(
    val points: List<Offset> = emptyList(),
    val penSettings: PenSettings = PenSettings()
)