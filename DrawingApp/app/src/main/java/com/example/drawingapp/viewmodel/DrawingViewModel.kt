/**
 * File:         DrawingViewModel.kt
 * Owner:        Caroline
 * Contributors:
 * Phase:        1
 *
 * Purpose:
 *   The ViewModel in MVVM. Holds all drawing state so it survives rotation.
 *
 * Public API (SHARED CONTRACT):
 *   State:   strokes, currentStroke, penSettings
 *   Drawing: startStroke(), addPoint(), endStroke(), clear()
 *   Pen:     setColor(), setSize(), setShape()
 *
 * Used by:
 *   - DrawingScreen (Shea), PenToolbar via DrawingScreen (Serena)
 *
 * Notes:
 *   - UI never changes state directly; it calls these functions.
 *   - MutableStateFlow stays private; only StateFlow is exposed.
 *
 * TODO(Caroline):
 *   - Implement every function below
 *   - Test rotation on phone + tablet
 */
package com.example.drawingapp.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.drawingapp.model.PenSettings
import com.example.drawingapp.model.PenShape
import com.example.drawingapp.model.Stroke
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DrawingViewModel : ViewModel() {

    private val _strokes = MutableStateFlow<List<Stroke>>(emptyList())
    /** All finished strokes, oldest first. */
    val strokes: StateFlow<List<Stroke>> = _strokes.asStateFlow()

    private val _currentStroke = MutableStateFlow<Stroke?>(null)
    /** The stroke being drawn right now, or null if the finger is up. */
    val currentStroke: StateFlow<Stroke?> = _currentStroke.asStateFlow()

    private val _penSettings = MutableStateFlow(PenSettings())
    /** The pen that new strokes will use. */
    val penSettings: StateFlow<PenSettings> = _penSettings.asStateFlow()

    /** Finger down: start a new stroke with the current pen. */
    fun startStroke(start: Offset) {
        // TODO(Caroline)
    }

    /** Finger drag: add a point to the stroke in progress. */
    fun addPoint(point: Offset) {
        // TODO(Caroline)
    }

    /** Finger up: move the stroke in progress into the finished list. */
    fun endStroke() {
        // TODO(Caroline)
    }

    /** Erase all strokes. Pen settings stay the same. */
    fun clear() {
        // TODO(Caroline)
    }

    /** Change the pen color for new strokes. */
    fun setColor(color: Color) {
        // TODO(Caroline)
    }

    /** Change the pen size for new strokes (clamp with PenOptions). */
    fun setSize(size: Float) {
        // TODO(Caroline)
    }

    /** Change the pen shape for new strokes. */
    fun setShape(shape: PenShape) {
        // TODO(Caroline)
    }
}