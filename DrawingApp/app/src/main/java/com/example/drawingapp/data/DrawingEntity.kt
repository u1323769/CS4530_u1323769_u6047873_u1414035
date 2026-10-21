/**
 * File:         DrawingEntity.kt
 * Owner:        Caroline
 * Contributors:
 * Phase:        2
 *
 * Purpose:
 *   One row in the Room "drawings" table.
 *
 * Used by:
 *   - DrawingDao, DrawingRepository (the UI never sees this class)
 *
 * Notes:
 *   - Strokes are stored as JSON (see StrokeConverters).
 *   - Images are stored as files; only their paths are saved here.
 *
 * TODO(Caroline):
 *   - @Entity with id (autoGenerate), title, strokes, backgroundPath,
 *     thumbnailPath, updatedAt
 */
package com.example.drawingapp.data

