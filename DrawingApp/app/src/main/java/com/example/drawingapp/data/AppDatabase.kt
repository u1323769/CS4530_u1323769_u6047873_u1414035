/**
 * File:         AppDatabase.kt
 * Owner:        Caroline
 * Phase:        2
 *
 * The Room database for the app.
 */
package com.example.drawingapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [DrawingEntity::class], version = 1, exportSchema = false)
@TypeConverters(StrokeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun drawingDao(): DrawingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Returns the one shared database, creating it the first time. */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "drawing_app.db"
                ).build().also { INSTANCE = it }
            }
    }
}