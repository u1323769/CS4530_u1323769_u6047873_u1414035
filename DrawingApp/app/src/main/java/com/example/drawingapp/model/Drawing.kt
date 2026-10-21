/**
 * File:         Drawing.kt
 * Owner:        Caroline
 * Contributors:
 * Phase:        2
 *
 * Purpose:
 *   App-level drawing models. Drawing holds everything needed to edit a
 *   drawing; DrawingSummary holds just what the gallery needs.
 *
 * Used by:
 *   - DrawingRepository, DrawingViewModel, GalleryViewModel, GalleryScreen
 *
 * Notes:
 *   - NEW_DRAWING_ID (-1) means "not saved yet".
 *   - SHARED CONTRACT: ask the team before changing.
 *
 * TODO(Caroline):
 *   - Drawing(id, title, strokes, backgroundPath)
 *   - DrawingSummary(id, title, thumbnailPath, updatedAt)
 *   - const val NEW_DRAWING_ID = -1L
 */
package com.example.drawingapp.model

