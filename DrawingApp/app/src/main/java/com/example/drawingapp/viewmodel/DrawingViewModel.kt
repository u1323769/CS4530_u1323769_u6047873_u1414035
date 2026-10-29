/**
 * File:         DrawingViewModel.kt
 * Owner:        Caroline
 * Phase:        1, 2
 *
 * Holds all drawing state (strokes, the stroke in
 * progress, the current pen, title, background) so it survives rotation,
 * and loads/saves drawings through DrawingRepository.
 */
package com.example.drawingapp.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.drawingapp.data.DrawingRepository
import com.example.drawingapp.model.NEW_DRAWING_ID
import com.example.drawingapp.model.PenSettings
import com.example.drawingapp.model.PenShape
import com.example.drawingapp.model.Stroke
import com.example.drawingapp.ui.toolbar.PenOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class DrawingViewModel(
    private val repository: DrawingRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _drawingId =
        MutableStateFlow(savedStateHandle.get<Long>(DRAWING_ID_ARG) ?: NEW_DRAWING_ID)
    /** This drawing's id, or NEW_DRAWING_ID until it's saved. */
    val drawingId: StateFlow<Long> = _drawingId.asStateFlow()

    private val _strokes = MutableStateFlow<List<Stroke>>(emptyList())
    /** All finished strokes, oldest first. */
    val strokes: StateFlow<List<Stroke>> = _strokes.asStateFlow()

    private val _currentStroke = MutableStateFlow<Stroke?>(null)
    /** The stroke being drawn right now, or null if the finger is up. */
    val currentStroke: StateFlow<Stroke?> = _currentStroke.asStateFlow()

    private val _penSettings = MutableStateFlow(PenSettings())
    /** The pen that new strokes will use. */
    val penSettings: StateFlow<PenSettings> = _penSettings.asStateFlow()

    private val _title = MutableStateFlow(DEFAULT_TITLE)
    /** The drawing's title, shown in the gallery. */
    val title: StateFlow<String> = _title.asStateFlow()

    private val _backgroundPath = MutableStateFlow<String?>(null)
    /** File path of an imported background image, or null for none. */
    val backgroundPath: StateFlow<String?> = _backgroundPath.asStateFlow()

    init {
        val id = _drawingId.value
        if (id != NEW_DRAWING_ID) {
            viewModelScope.launch {
                repository.getDrawing(id)?.let { drawing ->
                    _strokes.value = drawing.strokes
                    _title.value = drawing.title
                    _backgroundPath.value = drawing.backgroundPath
                }
            }
        }
    }

    /** Finger down: start a new stroke with the current pen. */
    fun startStroke(start: Offset) {
        if (_currentStroke.value != null) endStroke()
        _currentStroke.value = Stroke(points = listOf(start), penSettings = _penSettings.value)
    }

    /** Finger drag: add a point to the stroke in progress. Ignored if none. */
    fun addPoint(point: Offset) {
        _currentStroke.update { stroke -> stroke?.copy(points = stroke.points + point) }
    }

    /** Finger up: move the stroke in progress into the finished list. */
    fun endStroke() {
        val finished = _currentStroke.value ?: return
        _strokes.update { it + finished }
        _currentStroke.value = null
    }

    /** Erase all strokes. Pen settings, title, and background stay. */
    fun clear() {
        _strokes.value = emptyList()
        _currentStroke.value = null
    }

    /** Change the pen color for new strokes. */
    fun setColor(color: Color) {
        _penSettings.update { it.copy(color = color) }
    }

    /** Change the pen size for new strokes, kept inside PenOptions' range. */
    fun setSize(size: Float) {
        _penSettings.update { it.copy(size = PenOptions.clampSize(size)) }
    }

    /** Change the pen shape for new strokes. */
    fun setShape(shape: PenShape) {
        _penSettings.update { it.copy(shape = shape) }
    }

    /** Only one save runs at a time.*/
    private val saveMutex = Mutex()

    /**
     * Save the drawing.
     */
    fun save(thumbnail: ImageBitmap? = null) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                saveMutex.withLock {
                    val isNew = _drawingId.value == NEW_DRAWING_ID
                    val isEmpty = _strokes.value.isEmpty() && _backgroundPath.value == null
                    if (isNew && isEmpty) return@withLock

                    val newId = repository.saveDrawing(
                        id = _drawingId.value,
                        title = _title.value.ifBlank { DEFAULT_TITLE },
                        strokes = _strokes.value,
                        backgroundPath = _backgroundPath.value,
                        thumbnail = thumbnail
                    )
                    _drawingId.value = newId
                    savedStateHandle[DRAWING_ID_ARG] = newId
                }
            }
        }
    }

    companion object {
        /** Must match the navigation argument name in AppNavigation. */
        const val DRAWING_ID_ARG = "drawingId"
        const val DEFAULT_TITLE = "Untitled"

        /** Use with viewModel(factory = DrawingViewModel.Factory). */
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                DrawingViewModel(DrawingRepository.getInstance(app), createSavedStateHandle())
            }
        }
    }
}
