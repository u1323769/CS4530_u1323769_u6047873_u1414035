/**
 * File:         DrawingDao.kt
 * Owner:        Caroline
 * Phase:        2
 *
 * Room queries for the drawings table.
 *
 */
package com.example.drawingapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawingDao {

    /** All drawings, most recently edited first. */
    @Query("SELECT * FROM drawings ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<DrawingEntity>>

    /** One drawing, or null if it doesn't exist. */
    @Query("SELECT * FROM drawings WHERE id = :id")
    suspend fun getById(id: Long): DrawingEntity?

    /** Adds a new drawing and returns its generated id. */
    @Insert
    suspend fun insert(drawing: DrawingEntity): Long

    /** Replaces an existing drawing (matched by id). */
    @Update
    suspend fun update(drawing: DrawingEntity)

    /** Removes a drawing. */
    @Query("DELETE FROM drawings WHERE id = :id")
    suspend fun deleteById(id: Long)
}