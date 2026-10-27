/**
 * File:         DrawingRepositoryTest.kt
 * Owner:        Caroline
 * Phase:        2
 *
 * Tets saving new and existing drawings, loading, the gallery list,
 * timestamps, deleting (including image files), and importing images.
 */
package com.example.drawingapp.data

import androidx.compose.ui.geometry.Offset
import com.example.drawingapp.fakes.FakeDrawingDao
import com.example.drawingapp.model.NEW_DRAWING_ID
import com.example.drawingapp.model.PenSettings
import com.example.drawingapp.model.Stroke
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DrawingRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dao = FakeDrawingDao()
    private lateinit var storage: ImageStorage
    private var now = 1000L

    @Before
    fun setUp() {
        storage = ImageStorage(tempFolder.newFolder("images"))
    }

    private fun TestScope.newRepository() = DrawingRepository(
        dao = dao,
        imageStorage = storage,
        ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        clock = { now }
    )

    private val stroke = Stroke(listOf(Offset(1f, 2f), Offset(3f, 4f)), PenSettings())

    @Test
    fun `saving a new drawing inserts it and returns an id`() = runTest {
        val repo = newRepository()

        val id = repo.saveDrawing(NEW_DRAWING_ID, "Cat", listOf(stroke), null, null)

        assertNotEquals(NEW_DRAWING_ID, id)
        val loaded = repo.getDrawing(id)!!
        assertEquals("Cat", loaded.title)
        assertEquals(listOf(stroke), loaded.strokes)
    }

    @Test
    fun `saving an existing drawing updates it instead of adding a copy`() = runTest {
        val repo = newRepository()
        val id = repo.saveDrawing(NEW_DRAWING_ID, "Cat", listOf(stroke), null, null)

        val sameId = repo.saveDrawing(id, "Dog", emptyList(), null, null)

        assertEquals(id, sameId)
        assertEquals(1, dao.count())
        assertEquals("Dog", repo.getDrawing(id)!!.title)
    }

    @Test
    fun `background path is saved and loaded`() = runTest {
        val repo = newRepository()
        val id = repo.saveDrawing(NEW_DRAWING_ID, "Bg", emptyList(), "bg.png", null)

        assertEquals("bg.png", repo.getDrawing(id)!!.backgroundPath)
    }

    @Test
    fun `loading a drawing that doesn't exist returns null`() = runTest {
        assertNull(newRepository().getDrawing(999))
    }

    @Test
    fun `gallery list has a summary for each saved drawing`() = runTest {
        val repo = newRepository()
        repo.saveDrawing(NEW_DRAWING_ID, "One", emptyList(), null, null)
        repo.saveDrawing(NEW_DRAWING_ID, "Two", emptyList(), null, null)

        val titles = repo.allDrawings.first().map { it.title }

        assertEquals(2, titles.size)
        assertTrue(titles.containsAll(listOf("One", "Two")))
    }

    @Test
    fun `saving updates the timestamp`() = runTest {
        val repo = newRepository()
        now = 1000L
        val id = repo.saveDrawing(NEW_DRAWING_ID, "Cat", emptyList(), null, null)

        now = 2000L
        repo.saveDrawing(id, "Cat", emptyList(), null, null)

        assertEquals(2000L, repo.allDrawings.first().single().updatedAt)
    }

    @Test
    fun `saving without a thumbnail keeps the old thumbnail`() = runTest {
        val repo = newRepository()
        val thumbPath = storage.saveBytes(byteArrayOf(1, 2, 3))
        val id = dao.insert(
            DrawingEntity(title = "Cat", strokes = emptyList(), backgroundPath = null,
                thumbnailPath = thumbPath, updatedAt = 0)
        )

        repo.saveDrawing(id, "Cat", emptyList(), null, thumbnail = null)

        assertEquals(thumbPath, repo.allDrawings.first().single().thumbnailPath)
        assertTrue(File(thumbPath).exists())
    }

    @Test
    fun `deleting removes the drawing and its image files`() = runTest {
        val repo = newRepository()
        val bgPath = storage.saveBytes(byteArrayOf(1))
        val thumbPath = storage.saveBytes(byteArrayOf(2))
        val id = dao.insert(
            DrawingEntity(title = "Cat", strokes = emptyList(), backgroundPath = bgPath,
                thumbnailPath = thumbPath, updatedAt = 0)
        )

        repo.deleteDrawing(id)

        assertNull(repo.getDrawing(id))
        assertFalse(File(bgPath).exists())
        assertFalse(File(thumbPath).exists())
    }

    @Test
    fun `deleting one drawing leaves other drawings alone`() = runTest {
        val repo = newRepository()
        val keepId = repo.saveDrawing(NEW_DRAWING_ID, "Keep", emptyList(), null, null)
        val deleteId = repo.saveDrawing(NEW_DRAWING_ID, "Delete", emptyList(), null, null)

        repo.deleteDrawing(deleteId)

        assertEquals("Keep", repo.getDrawing(keepId)!!.title)
        assertEquals(1, dao.count())
    }

    @Test
    fun `deleting a drawing that doesn't exist does nothing`() = runTest {
        val repo = newRepository()
        repo.saveDrawing(NEW_DRAWING_ID, "Cat", emptyList(), null, null)

        repo.deleteDrawing(999)

        assertEquals(1, dao.count())
    }

    @Test
    fun `importing an image copies it into storage`() = runTest {
        val bytes = byteArrayOf(1, 2, 3)

        val path = newRepository().importImage(ByteArrayInputStream(bytes))

        assertTrue(storage.isInStorage(path))
        assertArrayEquals(bytes, storage.loadBytes(path))
    }
}
