/**
 * File:         VisionApiClient.kt
 * Owner:        Caroline
 * Phase:        3
 *
 * Purpose:
 *   Builds the one Retrofit/OkHttp instance and adds the API key to every
 *   request.
 *
 * Used by:
 *   - VisionRepository.getInstance()
 *
 * Notes:
 *   - The key comes from BuildConfig (set up by Serena), never hard-coded.
 *
 * TODO(Caroline):
 *   - Retrofit + kotlinx-serialization converter + key interceptor
 */
package com.example.drawingapp.vision

