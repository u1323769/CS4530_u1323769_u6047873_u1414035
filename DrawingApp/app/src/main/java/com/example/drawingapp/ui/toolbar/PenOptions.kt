/**
 * File:         PenOptions.kt
 * Owner:        Serena
 * Phase:        1
 */
package com.example.drawingapp.ui.toolbar

import androidx.compose.ui.graphics.Color

object PenOptions {
    const val MIN_SIZE = 2f
    const val MAX_SIZE = 60f
    const val DEFAULT_SIZE = 8f

    /** Colors shown in the color picker. */
    val palette: List<Color> = listOf(
        Color(0xFF000000), // Black
        Color(0xFFFFFFFF), // White (acts as an eraser on a white canvas)
        Color(0xFFE53935), // Red
        Color(0xFFFB8C00), // Orange
        Color(0xFFFDD835), // Yellow
        Color(0xFF43A047), // Green
        Color(0xFF1E88E5), // Blue
        Color(0xFF8E24AA), // Purple
    )

    val DEFAULT_COLOR: Color = palette.first()

    /** Keeps a pen size inside MIN_SIZE-MAX_SIZE. NaN falls back to DEFAULT_SIZE. */
    fun clampSize(size: Float): Float {
        if (size.isNaN()) return DEFAULT_SIZE
        return size.coerceIn(MIN_SIZE, MAX_SIZE)
    }
}