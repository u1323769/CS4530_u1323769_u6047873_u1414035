package com.example.drawingapp.model

/**
 * File:         PenSettings.kt
 * Owner:        Caroline
 * Contributors:
 * Phase:        1
 *
 * Purpose:
 *   Holds the current pen settings: color, size, and shape. Also defines
 *   the PenShape enum (CIRCLE, SQUARE, LINE).
 *
 * Used by:
 *   - DrawingViewModel (stores the current pen)
 *   - PenToolbar (shows and changes it)
 *   - DrawingCanvas (draws strokes with it)
 *
 * Notes:
 *   - Immutable data class. Change it with .copy().
 *   - SHARED CONTRACT: talk to the team before renaming or removing fields.
 *
 * TODO(Caroline):
 *   - Create PenShape enum and PenSettings data class with defaults
 */