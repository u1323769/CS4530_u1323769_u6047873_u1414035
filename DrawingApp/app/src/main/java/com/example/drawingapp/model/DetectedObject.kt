/**
 * File:         DetectedObject.kt
 * Owner:        Caroline
 * Phase:        3
 *
 * Purpose:
 *   One object the Vision API found: name, confidence (0–1), and its
 *   bounding box as fractions of the image (0–1), so it works at any size.
 *
 * Used by:
 *   - VisionRepository, VisionResponseMapper, AnalysisViewModel, AnalysisScreen
 *
 * Notes:
 *   - SHARED CONTRACT: ask the team before changing.
 *
 * TODO(Caroline):
 *   - data class DetectedObject(name, confidence, left, top, right, bottom)
 */
package com.example.drawingapp.model

