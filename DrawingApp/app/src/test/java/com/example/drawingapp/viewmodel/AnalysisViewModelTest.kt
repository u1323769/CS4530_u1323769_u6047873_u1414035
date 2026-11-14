/**
 * File:         AnalysisViewModelTest.kt
 * Owner:        Shea
 * Phase:        3
 *
 * What this tests:
 *   State changes: Loading -> Success, Empty when nothing is found, Error when
 *   the API fails, and retry() trying again.
 *
 * What this does NOT test:
 *   - The screen and boxes (checked manually)
 *
 * How to run:
 *   ./gradlew testDebugUnitTest, or right-click > Run
 */
package com.example.drawingapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.drawingapp.data.DrawingRepository
import com.example.drawingapp.data.ImageStorage
import com.example.drawingapp.model.DetectedObject
import com.example.drawingapp.model.NEW_DRAWING_ID
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var drawings: DrawingRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        drawings = DrawingRepository(
            dao = FakeDrawingDao(),
            imageStorage = ImageStorage(tempFolder.newFolder()),
            ioDispatcher = dispatcher,
            clock = { 1L }
        )
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun obj(name: String, confidence: Float) =
        DetectedObject(name, confidence, 0.1f, 0.1f, 0.5f, 0.5f)

    /** Saves a drawing with (or without) an imported image and returns its id. */
    private suspend fun savedDrawing(imagePath: String? = "/images/photo.img"): Long =
        drawings.saveDrawing(NEW_DRAWING_ID, "Test", emptyList(), imagePath, null)

    private fun viewModel(
        drawingId: Long,
        analyze: suspend (String) -> List<DetectedObject>
    ) = AnalysisViewModel(
        drawingRepository = drawings,
        analyzeImage = analyze,
        savedStateHandle = SavedStateHandle(mapOf(DrawingViewModel.DRAWING_ID_ARG to drawingId))
    )

    // ---------- Success ----------

    @Test
    fun whileVisionIsAnswering_stateIsLoading() = runTest {
        val pending = CompletableDeferred<List<DetectedObject>>()
        val vm = viewModel(savedDrawing()) { pending.await() }

        assertEquals(AnalysisUiState.Loading, vm.uiState.value)
    }

    @Test
    fun success_showsObjectsHighestConfidenceFirst() = runTest {
        val vm = viewModel(savedDrawing()) {
            listOf(obj("Cup", 0.60f), obj("Dog", 0.95f), obj("Chair", 0.80f))
        }

        val state = vm.uiState.value as AnalysisUiState.Success
        assertEquals(listOf("Dog", "Chair", "Cup"), state.objects.map { it.name })
    }

    @Test
    fun success_sendsTheDrawingsImageToVision() = runTest {
        var sentPath: String? = null
        val vm = viewModel(savedDrawing("/images/cat.img")) {
            sentPath = it
            listOf(obj("Cat", 0.9f))
        }

        assertEquals("/images/cat.img", sentPath)
        assertEquals("/images/cat.img", (vm.uiState.value as AnalysisUiState.Success).imagePath)
    }

    // ---------- Empty ----------

    @Test
    fun emptyResult_showsEmptyState() = runTest {
        val vm = viewModel(savedDrawing()) { emptyList() }

        assertTrue(vm.uiState.value is AnalysisUiState.Empty)
    }

    // ---------- Failures ----------

    @Test
    fun networkFailure_showsNetworkMessage() = runTest {
        val vm = viewModel(savedDrawing()) { throw IOException("timeout") }

        val state = vm.uiState.value as AnalysisUiState.Error
        assertEquals(AnalysisViewModel.NETWORK_ERROR, state.message)
    }

    @Test
    fun otherFailure_showsItsMessage() = runTest {
        val vm = viewModel(savedDrawing()) { throw IllegalStateException("Bad API key") }

        val state = vm.uiState.value as AnalysisUiState.Error
        assertEquals("Bad API key", state.message)
    }

    @Test
    fun failureWithoutMessage_showsGenericMessage() = runTest {
        val vm = viewModel(savedDrawing()) { throw RuntimeException() }

        val state = vm.uiState.value as AnalysisUiState.Error
        assertEquals(AnalysisViewModel.GENERIC_ERROR, state.message)
    }

    // ---------- Nothing to analyze ----------

    @Test
    fun drawingWithoutImage_showsNoImage_andNeverCallsVision() = runTest {
        var calls = 0
        val vm = viewModel(savedDrawing(imagePath = null)) {
            calls++
            emptyList()
        }

        assertEquals(AnalysisUiState.NoImage, vm.uiState.value)
        assertEquals(0, calls)
    }

    @Test
    fun unknownDrawing_showsNoImage() = runTest {
        val vm = viewModel(9_999L) { listOf(obj("Cat", 0.9f)) }

        assertEquals(AnalysisUiState.NoImage, vm.uiState.value)
    }

    // ---------- Retry ----------

    @Test
    fun retryAfterFailure_canSucceed() = runTest {
        var attempt = 0
        val vm = viewModel(savedDrawing()) {
            if (++attempt == 1) throw IOException("offline") else listOf(obj("Cat", 0.9f))
        }
        assertTrue(vm.uiState.value is AnalysisUiState.Error)

        vm.retry()

        assertTrue(vm.uiState.value is AnalysisUiState.Success)
        assertEquals(2, attempt)
    }

    @Test
    fun retryWhileLoading_isIgnored() = runTest {
        var calls = 0
        val pending = CompletableDeferred<List<DetectedObject>>()
        val vm = viewModel(savedDrawing()) {
            calls++
            pending.await()
        }

        vm.retry()
        vm.retry()

        assertEquals(1, calls)
    }

    // ---------- Highlighting ----------

    @Test
    fun selectObject_highlightsIt_andTappingAgainClears() = runTest {
        val vm = viewModel(savedDrawing()) { listOf(obj("Cat", 0.9f), obj("Dog", 0.8f)) }
        assertNull(vm.selectedIndex.value)

        vm.selectObject(1)
        assertEquals(1, vm.selectedIndex.value)

        vm.selectObject(1)
        assertNull(vm.selectedIndex.value)
    }

    @Test
    fun retry_clearsTheHighlight() = runTest {
        var attempt = 0
        val vm = viewModel(savedDrawing()) {
            if (++attempt == 1) listOf(obj("Cat", 0.9f)) else emptyList()
        }
        vm.selectObject(0)

        vm.retry()

        assertNull(vm.selectedIndex.value)
    }
}