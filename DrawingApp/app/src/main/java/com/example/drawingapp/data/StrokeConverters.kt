/**
 * File:         StrokeConverters.kt
 * Owner:        Caroline
 * Contributors:
 * Phase:        2
 *
 * Purpose:
 *   Converts List<Stroke> to JSON and back so Room can store strokes in
 *   one column.
 *
 * Used by:
 *   - AppDatabase (@TypeConverters)
 *
 * Tested by:
 *   - StrokeConvertersTest.kt
 *
 * TODO(Caroline):
 *   - strokesToJson() / jsonToStrokes()
 *   - Save colors as ARGB ints and points as x/y
 */
package com.example.drawingapp.data

