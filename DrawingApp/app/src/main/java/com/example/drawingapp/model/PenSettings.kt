/**
 * File:         PenSettings.kt
 * Owner:        Caroline
 * Contributors:
 * Phase:        1
 *
 * Purpose:
 *   Holds the current pen settings (color, size, shape) and defines PenShape.
 *
 * Used by:
 *   - DrawingViewModel, PenToolbar, DrawingCanvas
 *
 * Notes:
 *   - Immutable. Change with .copy().
 *   - SHARED CONTRACT: ask the team before renaming/removing fields.
 */
package com.example.drawingapp.model

import androidx.compose.ui.graphics.Color

/** Available pen tip shapes. */
enum class PenShape { CIRCLE, SQUARE, LINE }

/** The pen used for new strokes. */
data class PenSettings(
    val color: Color = Color.Black,
    val size: Float = 10f,
    val shape: PenShape = PenShape.CIRCLE
)