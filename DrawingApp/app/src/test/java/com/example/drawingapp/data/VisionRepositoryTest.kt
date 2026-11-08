/**
 * File:         VisionRepositoryTest.kt
 * Owner:        Caroline
 * Phase:        3
 *
 * What this tests:
 *   API communication using a fake server (MockWebServer): request is sent
 *   correctly, success returns objects, empty response returns an empty list,
 *   server error / bad JSON returns a failure instead of crashing.
 *
 * What this does NOT test:
 *   - The real Google Vision API (needs a key and internet)
 *
 * How to run:
 *   ./gradlew testDebugUnitTest, or right-click > Run
 */
package com.example.drawingapp.data

