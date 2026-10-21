/**
 * File:         AppDatabase.kt
 * Owner:        Caroline
 * Contributors:
 * Phase:        2
 *
 * Purpose:
 *   The Room database for the app.
 *
 * Used by:
 *   - DrawingRepository
 *
 * Notes:
 *   - Singleton: getInstance(context) so only one database is ever open.
 *
 * TODO(Caroline):
 *   - @Database(entities = [DrawingEntity::class], version = 1)
 *   - @TypeConverters(StrokeConverters::class)
 *   - getInstance(context) with @Volatile + synchronized
 */
package com.example.drawingapp.data

