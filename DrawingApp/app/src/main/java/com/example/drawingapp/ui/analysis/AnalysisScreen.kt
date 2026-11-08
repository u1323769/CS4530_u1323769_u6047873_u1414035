/**
 * File:         AnalysisScreen.kt
 * Owner:        Shea
 * Phase:        3
 *
 * Purpose:
 *   Shows the imported image with boxes around detected objects, plus a list
 *   of objects and their confidence scores.
 *
 * Inputs / Outputs:
 *   - In:  AnalysisViewModel state (loading / results / empty / error)
 *   - Out: onBack, onRetry, onOpenDrawing
 *
 * TODO(Shea):
 *   - Loading spinner, "No objects found", error message + Retry
 *   - Image + BoundingBoxOverlay on top, list below (side by side on tablets)
 */
package com.example.drawingapp.ui.analysis

