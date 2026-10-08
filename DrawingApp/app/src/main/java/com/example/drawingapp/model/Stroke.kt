/**
 * File:         Stroke.kt
 * Owner:        Caroline
 * Phase:        1
 *
 * One continuous mark on the canvas: every point touched between finger
 * down and finger up, plus the pen settings used to draw it.
 */
package com.example.drawingapp.model

import androidx.compose.ui.geometry.Offset

/** A single stroke on the canvas. */
data class Stroke(
    val points: List<Offset> = emptyList(),
    val penSettings: PenSettings = PenSettings()
)