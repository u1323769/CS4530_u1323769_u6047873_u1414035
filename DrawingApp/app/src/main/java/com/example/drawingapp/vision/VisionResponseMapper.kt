/**
 * File:         VisionResponseMapper.kt
 * Owner:        Serena
 * Phase:        3
 *
 * Purpose:
 *   Turns the raw Vision API response into a clean List<DetectedObject>.
 *
 * Used by:
 *   - VisionRepository
 *
 * Notes:
 *   - Handles missing fields, empty results, and boxes with missing corners.
 *   - Sorts by confidence, highest first.
 *   - Pure Kotlin (no Android classes) and never throws: bad or partial
 *     data is skipped instead of crashing the app.
 *
 * Tested by:
 *   - VisionResponseMapperTest.kt
 */
package com.example.drawingapp.vision

import com.example.drawingapp.model.DetectedObject

object VisionResponseMapper {

    /**
     * All usable objects from the response, most confident first.
     * Returns an empty list if there are no detections (or only invalid ones).
     */
    fun toDetectedObjects(response: VisionResponse): List<DetectedObject> =
        response.responses
            .flatMap { it.localizedObjectAnnotations }
            .mapNotNull { toDetectedObject(it) }
            .sortedByDescending { it.confidence }

    /**
     * The API's error message if it reported one for the image, otherwise null.
     * (Errors can arrive inside a successful HTTP response.)
     */
    fun errorMessage(response: VisionResponse): String? =
        response.responses
            .firstNotNullOfOrNull { it.error }
            ?.let { error -> error.message.ifBlank { "Vision API error (code ${error.code})" } }

    /**
     * Converts one annotation, or returns null if it can't be shown
     * (no name, no corners, or a box with no area).
     */
    fun toDetectedObject(annotation: LocalizedObjectAnnotation): DetectedObject? {
        val name = annotation.name.trim()
        val vertices = annotation.boundingPoly.normalizedVertices
        if (name.isEmpty() || vertices.isEmpty()) return null

        // The API gives the box as corner points; take the outer edges.
        // Missing x/y already default to 0 in NormalizedVertex.
        val left = vertices.minOf { it.x }.clamp01()
        val right = vertices.maxOf { it.x }.clamp01()
        val top = vertices.minOf { it.y }.clamp01()
        val bottom = vertices.maxOf { it.y }.clamp01()
        if (right <= left || bottom <= top) return null

        return DetectedObject(
            name = name,
            confidence = annotation.score.clamp01(),
            left = left,
            top = top,
            right = right,
            bottom = bottom
        )
    }

    private fun Float.clamp01(): Float = if (isNaN()) 0f else coerceIn(0f, 1f)
}