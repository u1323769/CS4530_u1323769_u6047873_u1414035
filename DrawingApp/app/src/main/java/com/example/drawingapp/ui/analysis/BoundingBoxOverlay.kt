/**
 * File:         BoundingBoxOverlay.kt
 * Owner:        Shea
 * Phase:        3
 *
 * Purpose:
 *   Draws a labeled box around each detected object, scaled to the image's
 *   size on screen.
 *
 * Used by:
 *   - AnalysisScreen
 *
 * TODO(Shea):
 *   - Canvas: box + "name 92%" label for each DetectedObject
 */
package com.example.drawingapp.ui.analysis

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drawingapp.model.DetectedObject
import kotlin.math.roundToInt

private val BoxColors = listOf(
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047),
    Color(0xFFFB8C00), Color(0xFF8E24AA), Color(0xFF00ACC1)
)

/** The color used for the object at [index], shared by the box and its list row. */
fun boxColor(index: Int): Color = BoxColors[index % BoxColors.size]

/** 0.874f -> "87%" */
fun confidenceLabel(confidence: Float): String = "${(confidence * 100).roundToInt()}%"

@Composable
fun BoundingBoxOverlay(
    objects: List<DetectedObject>,
    selectedIndex: Int?,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier) {
        objects.forEachIndexed { index, obj ->
            val color = boxColor(index)
            val selected = index == selectedIndex
            val topLeft = Offset(obj.left * size.width, obj.top * size.height)
            val boxSize = Size(
                (obj.right - obj.left) * size.width,
                (obj.bottom - obj.top) * size.height
            )

            drawRect(
                color = color,
                topLeft = topLeft,
                size = boxSize,
                style = Stroke(width = if (selected) 6.dp.toPx() else 3.dp.toPx())
            )

            // Label chip above the box (or inside the top edge if the box is at the image edge)
            val label = textMeasurer.measure(
                text = "${obj.name} ${confidenceLabel(obj.confidence)}",
                style = TextStyle(color = Color.White, fontSize = 12.sp)
            )
            val chipSize = Size(label.size.width.toFloat(), label.size.height.toFloat())
            val chipTop = (topLeft.y - chipSize.height).coerceAtLeast(0f)
            drawRect(color = color, topLeft = Offset(topLeft.x, chipTop), size = chipSize)
            drawText(label, topLeft = Offset(topLeft.x, chipTop))
        }
    }
}