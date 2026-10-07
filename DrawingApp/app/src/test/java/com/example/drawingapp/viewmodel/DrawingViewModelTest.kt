/**
 * File:         DrawingViewModelTest.kt
 * Owner:        Caroline
 * Contributors:
 * Phase:        1
 *
 * What this tests:
 *   DrawingViewModel: starting/adding/ending strokes, taps, clear(),
 *   pen changes (color, size clamping, shape), and that old strokes keep
 *   their own pen settings.
 *
 * How to run:
 *   ./gradlew testDebugUnitTest, or right-click this file > Run
 */
package com.example.drawingapp.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.drawingapp.model.PenShape
import com.example.drawingapp.ui.toolbar.PenOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DrawingViewModelTest {

    private lateinit var vm: DrawingViewModel

    @Before
    fun setUp() {
        vm = DrawingViewModel()
    }

    @Test
    fun `starts with no strokes and default pen`() {
        assertTrue(vm.strokes.value.isEmpty())
        assertNull(vm.currentStroke.value)
        assertEquals(PenOptions.DEFAULT_COLOR, vm.penSettings.value.color)
        assertEquals(PenOptions.DEFAULT_SIZE, vm.penSettings.value.size, 0.0001f)
        assertEquals(PenShape.CIRCLE, vm.penSettings.value.shape)
    }

    @Test
    fun `startStroke creates a stroke in progress`() {
        vm.startStroke(Offset(1f, 2f))

        val current = vm.currentStroke.value
        assertNotNull(current)
        assertEquals(listOf(Offset(1f, 2f)), current!!.points)
        assertTrue(vm.strokes.value.isEmpty())
    }

    @Test
    fun `full stroke is moved to the finished list`() {
        vm.startStroke(Offset(0f, 0f))
        vm.addPoint(Offset(5f, 5f))
        vm.addPoint(Offset(10f, 10f))
        vm.endStroke()

        assertEquals(1, vm.strokes.value.size)
        assertEquals(
            listOf(Offset(0f, 0f), Offset(5f, 5f), Offset(10f, 10f)),
            vm.strokes.value[0].points
        )
        assertNull(vm.currentStroke.value)
    }

    @Test
    fun `a tap is kept as a one-point stroke`() {
        vm.startStroke(Offset(3f, 3f))
        vm.endStroke()

        assertEquals(1, vm.strokes.value.size)
        assertEquals(1, vm.strokes.value[0].points.size)
    }

    @Test
    fun `strokes are kept in the order they were drawn`() {
        vm.startStroke(Offset(1f, 1f)); vm.endStroke()
        vm.startStroke(Offset(2f, 2f)); vm.endStroke()

        assertEquals(Offset(1f, 1f), vm.strokes.value[0].points.first())
        assertEquals(Offset(2f, 2f), vm.strokes.value[1].points.first())
    }

    @Test
    fun `addPoint without a started stroke is ignored`() {
        vm.addPoint(Offset(5f, 5f))

        assertNull(vm.currentStroke.value)
        assertTrue(vm.strokes.value.isEmpty())
    }

    @Test
    fun `endStroke without a started stroke does nothing`() {
        vm.endStroke()
        assertTrue(vm.strokes.value.isEmpty())
    }

    @Test
    fun `starting a new stroke finishes the one in progress`() {
        vm.startStroke(Offset(0f, 0f))
        vm.startStroke(Offset(9f, 9f))

        assertEquals(1, vm.strokes.value.size)
        assertEquals(Offset(9f, 9f), vm.currentStroke.value!!.points.first())
    }

    @Test
    fun `clear removes all strokes and the stroke in progress`() {
        vm.startStroke(Offset.Zero); vm.endStroke()
        vm.startStroke(Offset(1f, 1f))

        vm.clear()

        assertTrue(vm.strokes.value.isEmpty())
        assertNull(vm.currentStroke.value)
    }

    @Test
    fun `clear keeps the pen settings`() {
        vm.setColor(Color.Red)
        vm.setShape(PenShape.SQUARE)

        vm.clear()

        assertEquals(Color.Red, vm.penSettings.value.color)
        assertEquals(PenShape.SQUARE, vm.penSettings.value.shape)
    }

    @Test
    fun `setColor changes the pen color`() {
        vm.setColor(Color.Blue)
        assertEquals(Color.Blue, vm.penSettings.value.color)
    }

    @Test
    fun `setShape changes the pen shape`() {
        vm.setShape(PenShape.LINE)
        assertEquals(PenShape.LINE, vm.penSettings.value.shape)
    }

    @Test
    fun `setSize within range is kept`() {
        vm.setSize(20f)
        assertEquals(20f, vm.penSettings.value.size, 0.0001f)
    }

    @Test
    fun `setSize is clamped to the allowed range`() {
        vm.setSize(9999f)
        assertEquals(PenOptions.MAX_SIZE, vm.penSettings.value.size, 0.0001f)

        vm.setSize(-5f)
        assertEquals(PenOptions.MIN_SIZE, vm.penSettings.value.size, 0.0001f)
    }

    @Test
    fun `new strokes use the current pen`() {
        vm.setColor(Color.Red)
        vm.setSize(30f)
        vm.setShape(PenShape.SQUARE)

        vm.startStroke(Offset.Zero)
        vm.endStroke()

        val pen = vm.strokes.value[0].penSettings
        assertEquals(Color.Red, pen.color)
        assertEquals(30f, pen.size, 0.0001f)
        assertEquals(PenShape.SQUARE, pen.shape)
    }

    @Test
    fun `changing the pen does not change strokes already drawn`() {
        vm.setColor(Color.Red)
        vm.startStroke(Offset.Zero); vm.endStroke()

        vm.setColor(Color.Blue)

        assertEquals(Color.Red, vm.strokes.value[0].penSettings.color)
    }

    @Test
    fun `changing the pen mid-stroke only affects the next stroke`() {
        vm.startStroke(Offset.Zero)
        vm.setColor(Color.Green)
        vm.addPoint(Offset(5f, 5f))
        vm.endStroke()

        assertEquals(PenOptions.DEFAULT_COLOR, vm.strokes.value[0].penSettings.color)
    }
}