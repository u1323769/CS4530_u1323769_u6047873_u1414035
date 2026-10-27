/**
 * File:         Drawing.kt
 * Owner:        Caroline
 * Phase:        2
 *
 * App-level drawing models. Drawing holds everything needed to edit a
 * drawing; DrawingSummary holds just what the gallery needs.
 */
package com.example.drawingapp.model

const val NEW_DRAWING_ID = -1L

// A full drawing, used when opening one to edit.
data class Drawing(
    val id: Long,
    val title: String,
    val strokes: List<Stroke>,
    val backgroundPath: String?
)

// Just what the gallery grid needs to show one drawing.
data class DrawingSummary(
    val id: Long,
    val title: String,
    val thumbnailPath: String?,
    val updatedAt: Long
)
