/**
 * File:         PenOptions.kt
 * Owner:        Serena
 * Contributors:
 * Phase:        1
 *
 * Purpose:
 *   Non-UI pen constants: color palette, size range, clampSize().
 *
 * Used by:
 *   - PenToolbar, DrawingViewModel
 *
 * Tested by:
 *   - PenOptionsTest.kt
 *
 * TODO(Serena):
 *   - Pick palette colors and size range
 */
package com.example.drawingapp.ui.toolbar

import androidx.compose.ui.graphics.Color

object PenOptions {
    const val MIN_SIZE = 2f   // TODO(Serena): adjust
    const val MAX_SIZE = 80f  // TODO(Serena): adjust

    /** Colors shown in the color picker. */
    val palette: List<Color> = listOf(Color.Black) // TODO(Serena)

    /** Keeps a pen size inside MIN_SIZE..MAX_SIZE. */
    fun clampSize(size: Float): Float {
        // TODO(Serena)
        return size
    }
}