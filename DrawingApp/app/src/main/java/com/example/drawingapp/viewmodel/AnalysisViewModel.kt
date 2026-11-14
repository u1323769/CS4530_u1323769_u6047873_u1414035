/**
 * File:         AnalysisViewModel.kt
 * Owner:        Shea
 * Phase:        3
 *
 * Purpose:
 *   Loads the drawing's imported image, asks VisionRepository to analyze it,
 *   and exposes the result as UI state.
 *
 * Used by:
 *   - AnalysisScreen
 *
 * Notes:
 *   - Only talks to repositories (DrawingRepository + VisionRepository).
 *   - Drawing id comes from navigation (SavedStateHandle).
 *
 * Tested by:
 *   - AnalysisViewModelTest.kt
 *
 * TODO(Shea):
 *   - UiState: Loading, Success(objects), Empty, Error(message)
 *   - analyze(), retry(), Factory
 */
package com.example.drawingapp.viewmodel

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.drawingapp.data.DrawingRepository
import com.example.drawingapp.data.VisionRepository
import com.example.drawingapp.model.DetectedObject
import com.example.drawingapp.model.NEW_DRAWING_ID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

/** Everything the analysis screen can be showing. */
sealed interface AnalysisUiState {
    /** Waiting for the drawing to load or for Vision to answer. */
    data object Loading : AnalysisUiState

    /** Vision found at least one object. [objects] is sorted by confidence, highest first. */
    data class Success(val imagePath: String, val objects: List<DetectedObject>) : AnalysisUiState

    /** The request worked but nothing was recognized. */
    data class Empty(val imagePath: String) : AnalysisUiState

    /** The request failed. [message] is ready to show to the user. */
    data class Error(val message: String, val imagePath: String) : AnalysisUiState

    /** The drawing doesn't exist or has no imported image. */
    data object NoImage : AnalysisUiState
}

class AnalysisViewModel(
    private val drawingRepository: DrawingRepository,
    /** Sends an image file to Vision (VisionRepository::analyze in the app, a fake in tests). */
    private val analyzeImage: suspend (imagePath: String) -> List<DetectedObject>,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val drawingId: Long =
        savedStateHandle.get<Long>(DrawingViewModel.DRAWING_ID_ARG) ?: NEW_DRAWING_ID

    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Loading)
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    private val _selectedIndex = MutableStateFlow<Int?>(null)
    /** Index (in the sorted result list) of the highlighted object, or null. */
    val selectedIndex: StateFlow<Int?> = _selectedIndex.asStateFlow()

    private var job: Job? = null

    init {
        runAnalysis()
    }

    /** Try again after an error or an empty result. Ignored while a request is running. */
    fun retry() = runAnalysis()

    /** Tap an object to highlight it; tap it again to clear. */
    fun selectObject(index: Int) {
        _selectedIndex.value = if (_selectedIndex.value == index) null else index
    }

    private fun runAnalysis() {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            _uiState.value = AnalysisUiState.Loading
            _selectedIndex.value = null

            val imagePath = drawingRepository.getDrawing(drawingId)?.backgroundPath
            if (imagePath == null) {
                _uiState.value = AnalysisUiState.NoImage
                return@launch
            }

            _uiState.value = try {
                val objects = analyzeImage(imagePath).sortedByDescending { it.confidence }
                if (objects.isEmpty()) {
                    AnalysisUiState.Empty(imagePath)
                } else {
                    AnalysisUiState.Success(imagePath, objects)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                AnalysisUiState.Error(NETWORK_ERROR, imagePath)
            } catch (e: Exception) {
                AnalysisUiState.Error(e.message?.takeIf { it.isNotBlank() } ?: GENERIC_ERROR, imagePath)
            }
        }
    }

    companion object {
        const val NETWORK_ERROR = "Couldn't reach the Vision service. Check your internet connection."
        const val GENERIC_ERROR = "Something went wrong while analyzing the image."

        /** Use with viewModel(factory = AnalysisViewModel.Factory). */
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as Application
                val vision = VisionRepository.getInstance(app)   // match how Caroline exposes it
                AnalysisViewModel(
                    drawingRepository = DrawingRepository.getInstance(app),
                    analyzeImage = vision::analyze,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}