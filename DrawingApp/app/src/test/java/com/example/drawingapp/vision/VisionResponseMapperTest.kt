/**
 * File:         VisionResponseMapperTest.kt
 * Owner:        Serena
 * Phase:        3
 *
 * What this tests:
 *   Converting Vision API responses into DetectedObject: names, confidence,
 *   bounding boxes, sorting, skipping bad data, empty responses, errors,
 *   and parsing a real JSON response end to end.
 *
 * What this does NOT test:
 *   - The actual network call to Google (needs the internet and a key;
 *     covered by the repository tests and manual testing)
 *
 * How to run:
 *   ./gradlew testDebugUnitTest, or right-click > Run
 */
package com.example.drawingapp.vision

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VisionResponseMapperTest {

    private val delta = 0.0001f

    // ---------- helpers ----------

    private fun box(left: Float, top: Float, right: Float, bottom: Float) = BoundingPoly(
        normalizedVertices = listOf(
            NormalizedVertex(left, top),
            NormalizedVertex(right, top),
            NormalizedVertex(right, bottom),
            NormalizedVertex(left, bottom)
        )
    )

    private fun annotation(
        name: String = "Cat",
        score: Float = 0.9f,
        poly: BoundingPoly = box(0.1f, 0.2f, 0.5f, 0.6f)
    ) = LocalizedObjectAnnotation(name = name, score = score, boundingPoly = poly)

    private fun responseOf(vararg annotations: LocalizedObjectAnnotation) = VisionResponse(
        responses = listOf(AnnotateImageResponse(localizedObjectAnnotations = annotations.toList()))
    )

    // ---------- normal results ----------

    @Test
    fun singleObject_mapsNameConfidenceAndBox() {
        val result = VisionResponseMapper.toDetectedObjects(responseOf(annotation()))

        assertEquals(1, result.size)
        val obj = result[0]
        assertEquals("Cat", obj.name)
        assertEquals(0.9f, obj.confidence, delta)
        assertEquals(0.1f, obj.left, delta)
        assertEquals(0.2f, obj.top, delta)
        assertEquals(0.5f, obj.right, delta)
        assertEquals(0.6f, obj.bottom, delta)
    }

    @Test
    fun multipleObjects_sortedByConfidenceHighestFirst() {
        val result = VisionResponseMapper.toDetectedObjects(
            responseOf(
                annotation(name = "Tree", score = 0.5f),
                annotation(name = "Dog", score = 0.95f),
                annotation(name = "Car", score = 0.7f)
            )
        )
        assertEquals(listOf("Dog", "Car", "Tree"), result.map { it.name })
    }

    @Test
    fun cornersInAnyOrder_giveOuterEdges() {
        val poly = BoundingPoly(
            listOf(
                NormalizedVertex(0.8f, 0.9f),
                NormalizedVertex(0.2f, 0.3f),
                NormalizedVertex(0.8f, 0.3f),
                NormalizedVertex(0.2f, 0.9f)
            )
        )
        val obj = VisionResponseMapper.toDetectedObject(annotation(poly = poly))!!

        assertEquals(0.2f, obj.left, delta)
        assertEquals(0.3f, obj.top, delta)
        assertEquals(0.8f, obj.right, delta)
        assertEquals(0.9f, obj.bottom, delta)
    }

    @Test
    fun missingCoordinate_treatedAsZero() {
        // The API leaves out x or y when it is 0
        val poly = BoundingPoly(
            listOf(
                NormalizedVertex(y = 0.4f),          // x missing -> 0
                NormalizedVertex(x = 0.5f),          // y missing -> 0
                NormalizedVertex(0.5f, 0.4f),
                NormalizedVertex()
            )
        )
        val obj = VisionResponseMapper.toDetectedObject(annotation(poly = poly))!!

        assertEquals(0f, obj.left, delta)
        assertEquals(0f, obj.top, delta)
        assertEquals(0.5f, obj.right, delta)
        assertEquals(0.4f, obj.bottom, delta)
    }

    @Test
    fun outOfRangeValues_areClampedToZeroToOne() {
        val obj = VisionResponseMapper.toDetectedObject(
            annotation(score = 1.5f, poly = box(-0.2f, -0.1f, 1.3f, 1.2f))
        )!!

        assertEquals(1f, obj.confidence, delta)
        assertEquals(0f, obj.left, delta)
        assertEquals(0f, obj.top, delta)
        assertEquals(1f, obj.right, delta)
        assertEquals(1f, obj.bottom, delta)
    }

    @Test
    fun nameIsTrimmed() {
        val obj = VisionResponseMapper.toDetectedObject(annotation(name = "  Person "))!!
        assertEquals("Person", obj.name)
    }

    // ---------- bad or partial data is skipped ----------

    @Test
    fun blankName_isSkipped() {
        assertNull(VisionResponseMapper.toDetectedObject(annotation(name = "   ")))
    }

    @Test
    fun noCorners_isSkipped() {
        assertNull(VisionResponseMapper.toDetectedObject(annotation(poly = BoundingPoly())))
    }

    @Test
    fun zeroAreaBox_isSkipped() {
        assertNull(VisionResponseMapper.toDetectedObject(annotation(poly = box(0.3f, 0.3f, 0.3f, 0.6f))))
    }

    @Test
    fun invalidObjects_areDropped_validOnesKept() {
        val result = VisionResponseMapper.toDetectedObjects(
            responseOf(annotation(name = ""), annotation(name = "Cup"))
        )
        assertEquals(listOf("Cup"), result.map { it.name })
    }

    // ---------- empty responses and errors ----------

    @Test
    fun emptyResponse_givesEmptyList() {
        assertTrue(VisionResponseMapper.toDetectedObjects(VisionResponse()).isEmpty())
    }

    @Test
    fun noDetections_givesEmptyListAndNoError() {
        val response = responseOf()
        assertTrue(VisionResponseMapper.toDetectedObjects(response).isEmpty())
        assertNull(VisionResponseMapper.errorMessage(response))
    }

    @Test
    fun apiError_returnsMessage() {
        val response = VisionResponse(
            listOf(AnnotateImageResponse(error = VisionError(code = 3, message = "Bad image data.")))
        )
        assertEquals("Bad image data.", VisionResponseMapper.errorMessage(response))
        assertTrue(VisionResponseMapper.toDetectedObjects(response).isEmpty())
    }

    @Test
    fun apiErrorWithoutMessage_returnsFallbackWithCode() {
        val response = VisionResponse(listOf(AnnotateImageResponse(error = VisionError(code = 7))))
        assertEquals("Vision API error (code 7)", VisionResponseMapper.errorMessage(response))
    }

    // ---------- real JSON, end to end ----------

    @Test
    fun realJsonResponse_parsesAndMaps() {
        // Shape of an actual images:annotate response (x/y of 0 are omitted by the API)
        val json = """
            {
              "responses": [{
                "localizedObjectAnnotations": [{
                  "mid": "/m/01yrx",
                  "name": "Cat",
                  "score": 0.92,
                  "boundingPoly": {
                    "normalizedVertices": [
                      { "x": 0.1, "y": 0.2 },
                      { "x": 0.7, "y": 0.2 },
                      { "x": 0.7, "y": 0.9 },
                      { "y": 0.9 }
                    ]
                  }
                }]
              }]
            }
        """.trimIndent()

        val response = Json { ignoreUnknownKeys = true }.decodeFromString<VisionResponse>(json)
        val result = VisionResponseMapper.toDetectedObjects(response)

        assertEquals(1, result.size)
        assertEquals("Cat", result[0].name)
        assertEquals(92, result[0].confidencePercent)
        assertEquals(0f, result[0].left, delta)   // missing x on last corner -> 0
        assertEquals(0.2f, result[0].top, delta)
        assertEquals(0.7f, result[0].right, delta)
        assertEquals(0.9f, result[0].bottom, delta)
    }
}