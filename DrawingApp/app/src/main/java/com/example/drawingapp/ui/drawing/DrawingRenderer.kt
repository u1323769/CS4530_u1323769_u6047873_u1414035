/**
 * File:         DrawingRenderer.kt
 * Owner:        Shea
 * Contributors:
 * Phase:        2
 *
 * Draws strokes (and any imported background) onto an image, off
 * screen, so the drawing can be saved as a thumbnail or shared.
 */
package com.example.drawingapp.ui.drawing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.example.drawingapp.model.PenSettings
import com.example.drawingapp.model.PenShape
import com.example.drawingapp.model.Stroke
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Paints every stroke, oldest first. */
fun DrawScope.drawStrokes(strokes: List<Stroke>) {
    strokes.forEach { drawPenStroke(it) }
}

/** Paints the image scaled to fit the scope, centered. */
fun DrawScope.drawBackgroundImage(image: ImageBitmap?) {
    if (image == null) return
    val scale = min(size.width / image.width, size.height / image.height)
    val w = (image.width * scale).roundToInt()
    val h = (image.height * scale).roundToInt()
    drawImage(
        image = image,
        dstOffset = IntOffset(
            ((size.width - w) / 2f).roundToInt(),
            ((size.height - h) / 2f).roundToInt()
        ),
        dstSize = IntSize(w, h)
    )
}

private fun DrawScope.drawPenStroke(stroke: Stroke) {
    val pts = stroke.points
    val pen = stroke.penSettings
    if (pts.isEmpty()) return

    // Single tap -> dot
    if (pts.size == 1) {
        drawDot(pen, pts[0])
        return
    }

    when (pen.shape) {
        PenShape.CIRCLE -> drawPath(
            path = smoothPath(pts),
            color = pen.color,
            style = DrawStroke(width = pen.size, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        PenShape.TRIANGLE -> drawPath(
            path = smoothPath(pts),
            color = pen.color,
            style = DrawStroke(width = pen.size, cap = StrokeCap.Butt, join = StrokeJoin.Miter)
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

/** Smooths the polyline with quadratic curves through segment midpoints. */
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

object DrawingRenderer {

    /** Draws white paper, the optional background image, then the strokes into a Bitmap. */
    fun renderToBitmap(
        strokes: List<Stroke>,
        width: Int,
        height: Int,
        background: ImageBitmap? = null
    ): Bitmap {
        val target = ImageBitmap(max(1, width), max(1, height))
        val canvas = Canvas(target)
        CanvasDrawScope().draw(
            Density(1f),
            LayoutDirection.Ltr,
            canvas,
            Size(target.width.toFloat(), target.height.toFloat())
        ) {
            drawRect(Color.White)
            drawBackgroundImage(background)
            drawStrokes(strokes)
        }
        return target.asAndroidBitmap()
    }

    /**
     * Small preview for the gallery: the whole canvas
     * shrunk so its longest side is maxSide px.
     */
    fun renderThumbnail(
        strokes: List<Stroke>,
        canvasWidth: Int,
        canvasHeight: Int,
        background: ImageBitmap? = null,
        maxSide: Int = 256
    ): ImageBitmap {
        val w = max(1, canvasWidth)
        val h = max(1, canvasHeight)
        val scale = min(1f, maxSide / max(w, h).toFloat())
        val target = ImageBitmap(max(1, (w * scale).roundToInt()), max(1, (h * scale).roundToInt()))
        CanvasDrawScope().draw(
            Density(1f),
            LayoutDirection.Ltr,
            Canvas(target),
            Size(w.toFloat(), h.toFloat())   // draw in full-size coordinates...
        ) {
            withTransform({ scale(scale, scale, pivot = Offset.Zero) }) {   // ...shrunk into the bitmap
                drawRect(Color.White)
                drawBackgroundImage(background)
                drawStrokes(strokes)
            }
        }
        return target
    }

    /** Loads an image file, downsampled so its longest side is about maxDimension px. */
    fun loadImage(path: String, maxDimension: Int = 2048): ImageBitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > maxDimension) sample *= 2

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeFile(path, options)?.asImageBitmap()
    }
}