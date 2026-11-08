/**
 * File:         VisionResponseMapperTest.kt
 * Owner:        Serena
 * Phase:        3
 *
 * What this tests:
 *   Parsing sample Vision JSON: names, confidence, bounding boxes; empty
 *   response gives an empty list; missing fields don't crash; sorted by
 *   confidence.
 *
 * What this does NOT test:
 *   - Network calls (see VisionRepositoryTest)
 *
 * How to run:
 *   ./gradlew testDebugUnitTest, or right-click > Run
 */
package com.example.drawingapp.vision

