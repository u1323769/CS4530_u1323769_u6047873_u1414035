/**
 * File:         VisionModels.kt
 * Owner:        Caroline
 * Phase:        3
 *
 * Kotlin classes that match the Google Cloud Vision API's JSON, for the
 * images:annotate endpoint with OBJECT_LOCALIZATION. Retrofit turns these
 * into JSON for the request and back into objects for the response.
 */
package com.example.drawingapp.vision

import kotlinx.serialization.Serializable

/** The whole request body. */
@Serializable
data class VisionRequest(
    val requests: List<AnnotateImageRequest>
)

/** One image plus what we want the API to look for. */
@Serializable
data class AnnotateImageRequest(
    val image: VisionImage,
    val features: List<Feature>
)

/** The image itself. */
@Serializable
data class VisionImage(
    val content: String
)

/** What to detect.  */
@Serializable
data class Feature(
    val type: String = OBJECT_LOCALIZATION,
    val maxResults: Int = 10
) {
    companion object {
        const val OBJECT_LOCALIZATION = "OBJECT_LOCALIZATION"
    }
}

/** The whole response body */
@Serializable
data class VisionResponse(
    val responses: List<AnnotateImageResponse> = emptyList()
)

/** Results for one image. */
@Serializable
data class AnnotateImageResponse(
    val localizedObjectAnnotations: List<LocalizedObjectAnnotation> = emptyList(),
    val error: VisionError? = null
)

/** One detected object as the API describes it. */
@Serializable
data class LocalizedObjectAnnotation(
    val mid: String = "",
    val name: String = "",
    val score: Float = 0f,
    val boundingPoly: BoundingPoly = BoundingPoly()
)

/** The object's box, as corners given in fractions (0–1) of the image size. */
@Serializable
data class BoundingPoly(
    val normalizedVertices: List<NormalizedVertex> = emptyList()
)

/** One corner. The API leaves out x or y when it's 0, so both default to 0. */
@Serializable
data class NormalizedVertex(
    val x: Float = 0f,
    val y: Float = 0f
)

/** Error for one image, e.g. a bad image. */
@Serializable
data class VisionError(
    val code: Int = 0,
    val message: String = ""
)