/**
 * File:         DetectedObject.kt
 * Owner:        Caroline
 * Phase:        3
 *
 * One object the Vision API found in an image: its name, how confident the
 * API is (0–1), and its bounding box.
 */
package com.example.drawingapp.model

/**
 * @property name       what the object is
 * @property confidence how sure the API is, from 0.0 to 1.0
 * @property left       left edge of the box, 0.0–1.0 of the image width
 * @property top        top edge of the box, 0.0–1.0 of the image height
 * @property right      right edge of the box, 0.0–1.0 of the image width
 * @property bottom     bottom edge of the box, 0.0–1.0 of the image height
 */
data class DetectedObject(
    val name: String,
    val confidence: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    /** Confidence as a whole-number percent for display */
    val confidencePercent: Int
        get() = (confidence * 100).toInt()
}