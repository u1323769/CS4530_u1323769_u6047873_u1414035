/**
 * File:         DrawingEntity.kt
 * Owner:        Caroline
 * Phase:        2
 *
 * One row in the Room "drawings" table.
 */
package com.example.drawingapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.drawingapp.model.Stroke

@Entity(tableName = "drawings")
data class DrawingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val strokes: List<Stroke>,
    val backgroundPath: String?,
    val thumbnailPath: String?,
    val updatedAt: Long
)