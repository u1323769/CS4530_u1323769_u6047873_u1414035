/**
 * File:         DrawingCanvas.kt
 * Owner:        Shea
 * Phase:        1
 *
 * Captures drag gestures and draws each stroke with its own
 * color, size, and shape.
 */
package com.example.drawingapp.ui.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.example.drawingapp.model.Stroke

/** The drawing surface. */
@Composable
fun DrawingCanvas(
    strokes: List<Stroke>,
    onStrokeStart: (Offset) -> Unit,
    onStrokeMove: (Offset) -> Unit,
    onStrokeEnd: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundImage: ImageBitmap? = null,
    onCanvasSizeChanged: (IntSize) -> Unit = {}
) {
    val currentStart by rememberUpdatedState(onStrokeStart)
    val currentMove by rememberUpdatedState(onStrokeMove)
    val currentEnd by rememberUpdatedState(onStrokeEnd)

    Canvas(
        modifier = modifier
            .background(Color.White)
            .clipToBounds()
            .onSizeChanged(onCanvasSizeChanged)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    currentStart(down.position)

                    drag(down.id) { change ->
                        currentMove(change.position)
                        change.consume()
                    }
                    currentEnd()
                }
            }
    ) {
        drawBackgroundImage(backgroundImage)
        drawStrokes(strokes)
    }
}