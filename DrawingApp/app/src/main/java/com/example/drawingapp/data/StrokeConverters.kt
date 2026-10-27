/**
 * File:         StrokeConverters.kt
 * Owner:        Caroline
 * Phase:        2
 *
 * Converts List<Stroke> to JSON text and back so Room can store all of
 * a drawing's strokes in one column.
 */
package com.example.drawingapp.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.TypeConverter
import com.example.drawingapp.model.PenSettings
import com.example.drawingapp.model.PenShape
import com.example.drawingapp.model.Stroke
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class PointJson(val x: Float, val y: Float)

@Serializable
private data class StrokeJson(
    val points: List<PointJson>,
    val color: Int,
    val size: Float,
    val shape: String
)

private val json = Json { ignoreUnknownKeys = true }

class StrokeConverters {

    /** Strokes -> JSON text (for saving). */
    @TypeConverter
    fun strokesToJson(strokes: List<Stroke>): String =
        json.encodeToString(strokes.map { it.toJson() })

    /** JSON text -> strokes (for loading). Bad JSON returns an empty list. */
    @TypeConverter
    fun jsonToStrokes(text: String): List<Stroke> =
        try {
            json.decodeFromString<List<StrokeJson>>(text).map { it.toStroke() }
        } catch (e: Exception) {
            emptyList()
        }
}

private fun Stroke.toJson() = StrokeJson(
    points = points.map { PointJson(it.x, it.y) },
    color = penSettings.color.toArgb(),
    size = penSettings.size,
    shape = penSettings.shape.name
)

private fun StrokeJson.toStroke() = Stroke(
    points = points.map { Offset(it.x, it.y) },
    penSettings = PenSettings(
        color = Color(color),
        size = size,
        shape = PenShape.entries.find { it.name == shape } ?: PenShape.CIRCLE
    )
)