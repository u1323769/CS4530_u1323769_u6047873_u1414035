package com.example.drawingapp.ui.drawing

/**
 * File:         DrawingCanvas.kt
 * Owner:        Shea
 * Contributors:
 * Phase:        1
 *
 * Purpose:
 *   The area the user draws on. Captures touch/drag gestures and draws
 *   each stroke with its own color, size, and shape.
 *
 * Inputs / Outputs:
 *   - In:  list of strokes to draw
 *   - Out: onStrokeStart / onStrokeMove / onStrokeEnd callbacks
 *          (does NOT talk to the ViewModel directly)
 *
 * Used by:
 *   - DrawingScreen
 *
 * TODO(Shea):
 *   - Canvas + pointerInput drag detection
 *   - Draw CIRCLE, SQUARE, and LINE pens
 *   - Single tap leaves a dot
 */