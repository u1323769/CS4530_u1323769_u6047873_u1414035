/**
 * File:         DrawingViewModel.kt
 * Owner:        Caroline
 * Contributors:
 * Phase:        1
 *
 * Purpose:
 *   The ViewModel in MVVM. Holds all drawing state (finished strokes, the
 *   stroke in progress, and the current pen) so it survives screen rotation.
 *
 * Public API (SHARED CONTRACT):
 *   State:   strokes, currentStroke, penSettings  (read-only StateFlow)
 *   Drawing: startStroke(), addPoint(), endStroke(), clear()
 *   Pen:     setColor(), setSize(), setShape()
 *
 * Used by:
 *   - DrawingScreen (Shea): drawing functions
 *   - PenToolbar via DrawingScreen (Serena): pen functions
 *
 * Tested by:
 *   - DrawingViewModelTest.kt
 */
package com.example.drawingapp.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.drawingapp.model.PenSettings
import com.example.drawingapp.model.PenShape
import com.example.drawingapp.model.Stroke
import com.example.drawingapp.ui.toolbar.PenOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DrawingViewModel : ViewModel() {

    private val _strokes = MutableStateFlow<List<Stroke>>(emptyList())
    /** All finished strokes, oldest first (drawn in this order). */
    val strokes: StateFlow<List<Stroke>> = _strokes.asStateFlow()

    private val _currentStroke = MutableStateFlow<Stroke?>(null)
    /** The stroke being drawn right now, or null if the finger is up. */
    val currentStroke: StateFlow<Stroke?> = _currentStroke.asStateFlow()

    private val _penSettings = MutableStateFlow(PenSettings())
    /** The pen that new strokes will use. */
    val penSettings: StateFlow<PenSettings> = _penSettings.asStateFlow()

    /**
     * Finger down: start a new stroke with the current pen.
     * If a stroke was somehow still in progress, it's finished first so it isn't lost.
     */
    fun startStroke(start: Offset) {
        if (_currentStroke.value != null) endStroke()
        _currentStroke.value = Stroke(points = listOf(start), penSettings = _penSettings.value)
    }

    /** Finger drag: add a point to the stroke in progress. Ignored if no stroke started. */
    fun addPoint(point: Offset) {
        _currentStroke.update { stroke -> stroke?.copy(points = stroke.points + point) }
    }

    /** Finger up: move the stroke in progress into the finished list. No-op if none. */
    fun endStroke() {
        val finished = _currentStroke.value ?: return
        _strokes.update { it + finished }
        _currentStroke.value = null
    }

    /** Erase everything on the canvas. Pen settings stay the same. */
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
}