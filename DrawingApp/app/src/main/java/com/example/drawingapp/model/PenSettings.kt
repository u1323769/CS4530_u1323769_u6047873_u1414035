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
 *   - DrawingViewModel (stores the current pen)
 *   - PenToolbar (shows and changes it)
 *   - DrawingCanvas (draws each stroke with it)
 *
 */
package com.example.drawingapp.model

import androidx.compose.ui.graphics.Color
import com.example.drawingapp.ui.toolbar.PenOptions

enum class PenShape { CIRCLE, SQUARE, TRIANGLE }

/**
 * The pen used for new strokes.
 *
 * @property color stroke color
 * @property size  pen tip size in pixels (PenOptions.MIN_SIZE..MAX_SIZE)
 * @property shape pen tip shape
 */
data class PenSettings(
    val color: Color = PenOptions.DEFAULT_COLOR,
    val size: Float = PenOptions.DEFAULT_SIZE,
    val shape: PenShape = PenShape.CIRCLE
)