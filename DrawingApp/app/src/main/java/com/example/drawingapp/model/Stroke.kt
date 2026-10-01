package com.example.drawingapp.model

/**
 * File:         Stroke.kt
 * Owner:        Caroline
 * Contributors:
 * Phase:        1
 *
 * Purpose:
 *   One continuous mark on the canvas: the list of points the finger
 *   touched, plus the PenSettings used to draw it.
 *
 * Used by:
 *   - DrawingViewModel (list of finished strokes + the one in progress)
 *   - DrawingCanvas (renders them)
 *
 * Notes:
 *   - Each stroke keeps its own PenSettings so changing the pen later
 *     doesn't recolor old strokes.
 *   - Phase 2 will save these, so keep it a simple data class.
 *   - SHARED CONTRACT: talk to the team before changing.
 *
 * TODO(Caroline):
 *   - Create Stroke data class (points: List<Offset>, penSettings)
 */