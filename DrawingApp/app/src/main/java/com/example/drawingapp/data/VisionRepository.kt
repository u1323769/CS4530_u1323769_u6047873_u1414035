/**
 * File:         VisionRepository.kt
 * Owner:        Caroline
 * Phase:        3
 *
 * Purpose:
 *   Middle layer between the ViewModel and the Vision API
 *   (ViewModel -> Repository -> Vision API).
 *
 * Used by:
 *   - AnalysisViewModel
 *
 * Notes:
 *   - Singleton: getInstance(context).
 *   - Never throws: returns Result.success(list) or Result.failure(error)
 *     so a bad network never crashes the app.
 *
 * Tested by:
 *   - VisionRepositoryTest.kt
 *
 * TODO(Caroline):
 *   - suspend fun analyze(imagePath): Result<List<DetectedObject>>
 *   - Read file -> base64 -> API call -> VisionResponseMapper
 */
package com.example.drawingapp.data

