/**
 * File:         PenSettings.kt
 * Owner:        Caroline
 * Phase:        1
 *
 * Holds the current pen settings (color, size, shape) and defines PenShape.
 */
package com.example.drawingapp.model

import androidx.compose.ui.graphics.Color
import com.example.drawingapp.ui.toolbar.PenOptions

enum class PenShape { CIRCLE, SQUARE, TRIANGLE }

/**
 * The pen used for new strokes.
 *
 * @property color stroke color
 * @property size  pen tip size in pixels
 * @property shape pen tip shape
 */
data class PenSettings(
    val color: Color = PenOptions.DEFAULT_COLOR,
    val size: Float = PenOptions.DEFAULT_SIZE,
    val shape: PenShape = PenShape.CIRCLE
)