/**
 * File:         DrawingRepository.kt
 * Owner:        Caroline
 * Contributors:
 * Phase:        2
 *
 * Purpose:
 *   Middle layer between the ViewModels and the Room database / image
 *   files (repository pattern).
 *
 * Used by:
 *   - DrawingViewModel, GalleryViewModel
 *
 * Notes:
 *   - Singleton: getInstance(context) always returns the same instance.
 *   - ViewModels never touch the DAO or database directly.
 *
 * TODO(Caroline):
 *   - allDrawings: Flow<List<DrawingSummary>>
 *   - getDrawing(id), saveDrawing(...): Long, deleteDrawing(id)
 *   - getInstance(context)
 */
package com.example.drawingapp.data

