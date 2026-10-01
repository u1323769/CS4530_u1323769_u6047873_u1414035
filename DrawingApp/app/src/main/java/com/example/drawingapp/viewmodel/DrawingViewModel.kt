package com.example.drawingapp.viewmodel

/**
 * File:         DrawingViewModel.kt
 * Owner:        Caroline
 * Contributors:
 * Phase:        1
 *
 * Purpose:
 *   The ViewModel in MVVM. Holds all drawing state (strokes, the
 *   stroke in progress, current pen) so it survives screen rotation.
 *
 * Public API (shared contract):
 *   State:   strokes, currentStroke, penSettings (read-only StateFlow)
 *   Drawing: startStroke(offset), addPoint(offset), endStroke(), clear()
 *   Pen:     setColor(color), setSize(size), setShape(shape)
 *
 * Used by:
 *   - DrawingScreen (Shea): drawing functions
 *   - PenToolbar via DrawingScreen (Serena): pen functions
 *
 * Notes:
 *   - UI never changes state directly; it calls these functions.
 *   - Keep MutableStateFlow private; expose StateFlow.
 *
 * TODO(Caroline):
 *   - Push a working version of the API ASAP so Shea and Serena aren't blocked
 *   - Test rotation on phone + tablet emulator
 */