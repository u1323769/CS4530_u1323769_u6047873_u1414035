/**
 * File:         DrawingCanvas.kt
 * Owner:        Shea
 * Contributors:
 * Phase:        1
 *
 * Purpose:
 *   Captures drag gestures and draws each stroke with its own
 *   color, size, and shape.
 *
 * Inputs / Outputs:
 *   - In:  strokes to draw
 *   - Out: onStrokeStart / onStrokeMove / onStrokeEnd
 *          (does NOT talk to the ViewModel directly)
 *
 * TODO(Shea):
 *   - Canvas + pointerInput drag detection
 *   - Draw CIRCLE, SQUARE, LINE pens
 *   - Single tap leaves a dot
 */
package com.example.drawingapp.ui.drawing

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.example.drawingapp.model.Stroke

/** The drawing surface. */
@Composable
fun DrawingCanvas(
    strokes: List<Stroke>,
    onStrokeStart: (Offset) -> Unit,
    onStrokeMove: (Offset) -> Unit,
    onStrokeEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO(Shea)
}