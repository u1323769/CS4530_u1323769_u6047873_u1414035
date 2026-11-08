/**
 * File:         VisionApiService.kt
 * Owner:        Caroline
 * Phase:        3
 *
 * Retrofit interface for the Google Cloud Vision API. Retrofit turns this
 * into the real HTTP call.
 */
package com.example.drawingapp.vision

import retrofit2.http.Body
import retrofit2.http.POST

interface VisionApiService {

    /** Sends one or more images to be analyzed. */
    @POST("v1/images:annotate")
    suspend fun annotate(@Body request: VisionRequest): VisionResponse
}