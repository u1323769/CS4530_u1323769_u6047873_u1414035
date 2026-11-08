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
 *
 * Tested by:
 *   - VisionResponseMapperTest.kt
 *
 * TODO(Serena):
 *   - fun toDetectedObjects(response): List<DetectedObject>
 */
package com.example.drawingapp.vision

