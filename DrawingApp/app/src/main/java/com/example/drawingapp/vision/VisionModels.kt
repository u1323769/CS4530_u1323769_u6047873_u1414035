/**
 * File:         VisionModels.kt
 * Owner:        Caroline
 * Phase:        3
 *
 * Purpose:
 *   @Serializable classes matching the Vision API's JSON request and response
 *   (images:annotate with OBJECT_LOCALIZATION).
 *
 * Used by:
 *   - VisionApiService, VisionResponseMapper
 *
 * TODO(Caroline):
 *   - Request: image content (base64) + features
 *   - Response: localizedObjectAnnotations (name, score, boundingPoly)
 */
package com.example.drawingapp.vision

