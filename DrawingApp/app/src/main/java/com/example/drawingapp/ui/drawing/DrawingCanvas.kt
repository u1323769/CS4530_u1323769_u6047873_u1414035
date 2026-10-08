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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.drawingapp.model.PenSettings
import com.example.drawingapp.model.PenShape
import com.example.drawingapp.model.Stroke
import kotlin.math.max

/** The drawing surface. */
@Composable
fun DrawingCanvas(
    strokes: List<Stroke>,
    onStrokeStart: (Offset) -> Unit,
    onStrokeMove: (Offset) -> Unit,
    onStrokeEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val start by rememberUpdatedState(onStrokeStart)
    val move by rememberUpdatedState(onStrokeMove)
    val end by rememberUpdatedState(onStrokeEnd)

    Canvas(
        modifier = modifier
            .background(Color.White)
            .clipToBounds()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    start(down.position)

                    drag(down.id) { change ->
                        move(change.position)
                        change.consume()
                    }
                    end()
                }
            }
    ) {
        strokes.forEach { drawPenStroke(it) }
    }
}

private fun DrawScope.drawPenStroke(stroke: Stroke) {
    val pts = stroke.points
    val pen = stroke.penSettings
    if (pts.isEmpty()) return

    if (pts.size == 1) {
        drawDot(pen, pts[0],)
        return
    }

    when (pen.shape) {
        PenShape.CIRCLE -> drawPath(
            path = smoothPath(pts),
            color = pen.color,
            style = DrawStroke(
                width = pen.size,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        PenShape.LINE -> drawPath(
            path = smoothPath(pts),
            color = pen.color,
            style = DrawStroke(
                width = pen.size,
                cap = StrokeCap.Butt,
                join = StrokeJoin.Miter
            )
        )

        PenShape.SQUARE -> {
            val step = max(1f, pen.size / 3f)
            StrokeGeometry.interpolate(pts, step).forEach { drawSquare(pen, it) }
        }
    }
}

private fun DrawScope.drawDot(pen: PenSettings, center: Offset) {
    when (pen.shape) {
        PenShape.SQUARE -> drawSquare(pen, center)
        else -> drawCircle(color = pen.color, radius = pen.size / 2f, center = center)
    }
}

private fun DrawScope.drawSquare(pen: PenSettings, center: Offset) {
    val half = pen.size / 2f
    drawRect(
        color = pen.color,
        topLeft = Offset(center.x - half, center.y - half),
        size = Size(pen.size, pen.size)
    )
}

private fun smoothPath(points: List<Offset>): Path {
    val path = Path()
    path.moveTo(points[0].x, points[0].y)
    for (i in 1 until points.size) {
        val prev = points[i - 1]
        val cur = points[i]
        path.quadraticTo(prev.x, prev.y, (prev.x + cur.x) / 2f, (prev.y + cur.y) / 2f)
    }
    path.lineTo(points.last().x, points.last().y)
    return path
}