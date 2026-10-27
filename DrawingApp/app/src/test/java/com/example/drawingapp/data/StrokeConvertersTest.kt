/**
 * File:         StrokeConvertersTest.kt
 * Owner:        Caroline
 * Phase:        2
 *
 * Tests strokes (points, colors, sizes, every shape) are the same after
 * converting to JSON and back; empty lists; bad JSON and unknown shape
 * names don't crash.
 */
package com.example.drawingapp.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.drawingapp.model.PenSettings
import com.example.drawingapp.model.PenShape
import com.example.drawingapp.model.Stroke
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrokeConvertersTest {

    private val converters = StrokeConverters()

    private fun roundTrip(strokes: List<Stroke>) =
        converters.jsonToStrokes(converters.strokesToJson(strokes))

    @Test
    fun `empty list round trips`() {
        assertTrue(roundTrip(emptyList()).isEmpty())
    }

    @Test
    fun `points color size and shape survive a round trip`() {
        val stroke = Stroke(
            points = listOf(Offset(1.5f, 2.5f), Offset(10f, 20f)),
            penSettings = PenSettings(color = Color(0xFFE53935), size = 24f, shape = PenShape.SQUARE)
        )
        assertEquals(listOf(stroke), roundTrip(listOf(stroke)))
    }

    @Test
    fun `every shape survives a round trip`() {
        PenShape.entries.forEach { shape ->
            val stroke = Stroke(listOf(Offset.Zero), PenSettings(shape = shape))
            assertEquals(shape, roundTrip(listOf(stroke)).single().penSettings.shape)
        }
    }

    @Test
    fun `several strokes keep their order`() {
        val strokes = listOf(
            Stroke(listOf(Offset(1f, 1f)), PenSettings(color = Color.Red)),
            Stroke(listOf(Offset(2f, 2f)), PenSettings(color = Color.Blue)),
            Stroke(listOf(Offset(3f, 3f)), PenSettings(color = Color.Green))
        )
        assertEquals(strokes, roundTrip(strokes))
    }

    @Test
    fun `a tap stroke with one point round trips`() {
        val tap = Stroke(listOf(Offset(5f, 5f)), PenSettings())
        assertEquals(listOf(tap), roundTrip(listOf(tap)))
    }

    @Test
    fun `bad JSON gives an empty list instead of crashing`() {
        assertTrue(converters.jsonToStrokes("not json").isEmpty())
        assertTrue(converters.jsonToStrokes("").isEmpty())
    }

    @Test
    fun `unknown shape name falls back to circle`() {
        val oldJson = """[{"points":[{"x":1.0,"y":2.0}],"color":-16777216,"size":8.0,"shape":"LINE"}]"""
        assertEquals(PenShape.CIRCLE, converters.jsonToStrokes(oldJson).single().penSettings.shape)
    }
}