/**
 * File:         DrawingViewModelTest.kt
 * Owner:        Caroline
 * Phase:        1, 2
 *
 *  Tests Phase 1: starting/adding/ending strokes, taps, clear(), pen changes
 *  (color, size clamping, shape), and that old strokes keep their own pen.
 *  Tests Phase 2: new vs. existing drawings, loading strokes/title/background,
 *  saving (insert first, then update), blank titles, importing a background.
 */
package com.example.drawingapp.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import com.example.drawingapp.data.DrawingRepository
import com.example.drawingapp.data.ImageStorage
import com.example.drawingapp.fakes.FakeDrawingDao
import com.example.drawingapp.model.NEW_DRAWING_ID
import com.example.drawingapp.model.PenSettings
import com.example.drawingapp.model.PenShape
import com.example.drawingapp.model.Stroke
import com.example.drawingapp.testutil.MainDispatcherRule
import com.example.drawingapp.ui.toolbar.PenOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream

@OptIn(ExperimentalCoroutinesApi::class)
class DrawingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var dao: FakeDrawingDao
    private lateinit var storage: ImageStorage
    private lateinit var repository: DrawingRepository
    private lateinit var vm: DrawingViewModel

    @Before
    fun setUp() {
        dao = FakeDrawingDao()
        storage = ImageStorage(tempFolder.newFolder("images"))
        repository = DrawingRepository(
            dao = dao,
            imageStorage = storage,
            ioDispatcher = mainDispatcherRule.dispatcher
        )
        vm = DrawingViewModel(repository, SavedStateHandle())
    }

    /** Opens an existing drawing the way navigation would. */
    private fun openDrawing(id: Long) =
        DrawingViewModel(repository, SavedStateHandle(mapOf(DrawingViewModel.DRAWING_ID_ARG to id)))

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
        vm.setShape(PenShape.TRIANGLE)
        assertEquals(PenShape.TRIANGLE, vm.penSettings.value.shape)
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

    @Test
    fun `new drawing starts empty with default title`() {
        assertEquals(NEW_DRAWING_ID, vm.drawingId.value)
        assertTrue(vm.strokes.value.isEmpty())
        assertEquals(DrawingViewModel.DEFAULT_TITLE, vm.title.value)
        assertNull(vm.backgroundPath.value)
    }

    @Test
    fun `setTitle changes the title`() {
        vm.setTitle("Sunset")
        assertEquals("Sunset", vm.title.value)
    }

    @Test
    fun `saving a new drawing gives it a real id`() {
        vm.startStroke(Offset.Zero); vm.endStroke()

        vm.save()

        assertNotEquals(NEW_DRAWING_ID, vm.drawingId.value)
        assertEquals(1, dao.count())
    }

    @Test
    fun `saving twice updates the same drawing`() {
        vm.save()
        val firstId = vm.drawingId.value

        vm.startStroke(Offset.Zero); vm.endStroke()
        vm.save()

        assertEquals(firstId, vm.drawingId.value)
        assertEquals(1, dao.count())
    }

    @Test
    fun `saved drawing has the strokes and title`() = runTest {
        vm.setTitle("Sunset")
        vm.startStroke(Offset(1f, 1f)); vm.addPoint(Offset(2f, 2f)); vm.endStroke()

        vm.save()

        val saved = repository.getDrawing(vm.drawingId.value)!!
        assertEquals("Sunset", saved.title)
        assertEquals(vm.strokes.value, saved.strokes)
    }

    @Test
    fun `blank title is saved as Untitled`() = runTest {
        vm.setTitle("   ")
        vm.save()

        assertEquals(DrawingViewModel.DEFAULT_TITLE, repository.getDrawing(vm.drawingId.value)!!.title)
    }

    @Test
    fun `opening an existing drawing loads its strokes title and background`() = runTest {
        val stroke = Stroke(listOf(Offset(5f, 5f)), PenSettings(color = Color.Red))
        val id = repository.saveDrawing(NEW_DRAWING_ID, "Cat", listOf(stroke), "bg.png", null)

        val opened = openDrawing(id)

        assertEquals(id, opened.drawingId.value)
        assertEquals(listOf(stroke), opened.strokes.value)
        assertEquals("Cat", opened.title.value)
        assertEquals("bg.png", opened.backgroundPath.value)
    }

    @Test
    fun `editing an opened drawing and saving updates it`() = runTest {
        val id = repository.saveDrawing(NEW_DRAWING_ID, "Cat", emptyList(), null, null)
        val opened = openDrawing(id)

        opened.startStroke(Offset(3f, 3f)); opened.endStroke()
        opened.save()

        assertEquals(1, dao.count())
        assertEquals(1, repository.getDrawing(id)!!.strokes.size)
    }

    @Test
    fun `opening a drawing that doesn't exist starts empty`() {
        val opened = openDrawing(999)
        assertTrue(opened.strokes.value.isEmpty())
        assertEquals(DrawingViewModel.DEFAULT_TITLE, opened.title.value)
    }

    @Test
    fun `importing a background stores the image and sets its path`() {
        vm.importBackground(ByteArrayInputStream(byteArrayOf(1, 2, 3)))

        val path = vm.backgroundPath.value
        assertNotNull(path)
        assertTrue(storage.isInStorage(path!!))
    }

    @Test
    fun `background is saved with the drawing`() = runTest {
        vm.importBackground(ByteArrayInputStream(byteArrayOf(1)))
        vm.save()

        assertEquals(vm.backgroundPath.value, repository.getDrawing(vm.drawingId.value)!!.backgroundPath)
    }
}
