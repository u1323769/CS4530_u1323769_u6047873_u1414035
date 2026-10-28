/**
 * File:         GalleryViewModelTest.kt
 * Owner:        Shea
 * Phase:        2
 *
 * What this tests:
 *   Gallery list shows saved drawings, delete removes one.
 *
 * What this does NOT test:
 *   - The gallery UI (checked manually)
 *
 * How to run:
 *   ./gradlew testDebugUnitTest, or right-click > Run
 */
package com.example.drawingapp.viewmodel

import com.example.drawingapp.data.DrawingRepository
import com.example.drawingapp.data.ImageStorage
import com.example.drawingapp.model.NEW_DRAWING_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class GalleryViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dispatcher = UnconfinedTestDispatcher()
    private var now = 1_000L
    private lateinit var repo: DrawingRepository
    private lateinit var vm: GalleryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = DrawingRepository(
            dao = FakeDrawingDao(),
            imageStorage = ImageStorage(tempFolder.newFolder()),
            ioDispatcher = dispatcher,
            clock = { now }
        )
        vm = GalleryViewModel(repo)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    /** The list is a WhileSubscribed StateFlow, so something must be collecting it. */
    private fun TestScope.collectDrawings() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.drawings.collect {} }
    }

    /** Saves a new drawing through the repository, the way DrawingViewModel would. */
    private suspend fun saveDrawing(title: String, at: Long): Long {
        now = at
        return repo.saveDrawing(NEW_DRAWING_ID, title, emptyList(), null, null)
    }

    // ---------- Gallery list shows saved drawings ----------

    @Test
    fun list_isEmptyWhenNothingSaved() = runTest {
        collectDrawings()

        assertTrue(vm.drawings.value.isEmpty())
    }

    @Test
    fun list_showsEverySavedDrawing() = runTest {
        collectDrawings()
        saveDrawing("Sunset", at = 100)
        saveDrawing("Portrait", at = 200)
        saveDrawing("Doodle", at = 300)

        assertEquals(3, vm.drawings.value.size)
        assertEquals(
            setOf("Sunset", "Portrait", "Doodle"),
            vm.drawings.value.map { it.title }.toSet()
        )
    }

    @Test
    fun list_showsMostRecentlyEditedFirst() = runTest {
        collectDrawings()
        saveDrawing("old", at = 100)
        saveDrawing("newest", at = 300)
        saveDrawing("middle", at = 200)

        assertEquals(listOf("newest", "middle", "old"), vm.drawings.value.map { it.title })
    }

    @Test
    fun list_updatesWhenADrawingIsSavedLater() = runTest {
        collectDrawings()
        assertTrue(vm.drawings.value.isEmpty())

        saveDrawing("Fresh", at = 1)

        assertEquals(listOf("Fresh"), vm.drawings.value.map { it.title })
    }

    // ---------- Delete removes one ----------

    @Test
    fun delete_removesThatDrawing() = runTest {
        collectDrawings()
        val id = saveDrawing("Doomed", at = 1)

        vm.deleteDrawing(id)

        assertTrue(vm.drawings.value.isEmpty())
    }

    @Test
    fun delete_keepsTheOtherDrawings() = runTest {
        collectDrawings()
        saveDrawing("Keep A", at = 100)
        val doomed = saveDrawing("Delete me", at = 200)
        saveDrawing("Keep B", at = 300)

        vm.deleteDrawing(doomed)

        assertEquals(listOf("Keep B", "Keep A"), vm.drawings.value.map { it.title })
    }

    @Test
    fun delete_unknownId_changesNothing() = runTest {
        collectDrawings()
        saveDrawing("Safe", at = 1)

        vm.deleteDrawing(9_999)

        assertEquals(listOf("Safe"), vm.drawings.value.map { it.title })
    }
}