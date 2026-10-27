/**
 * File:         FakeDrawingDao.kt
 * Owner:        Caroline
 * Phase:        2
 *
 * In-memory DrawingDao for unit tests, so tests don't need a real
 *  Room database or an emulator.
 */
package com.example.drawingapp.fakes

import com.example.drawingapp.data.DrawingDao
import com.example.drawingapp.data.DrawingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeDrawingDao : DrawingDao {
    private val drawings = MutableStateFlow<List<DrawingEntity>>(emptyList())
    private var nextId = 1L

    override fun getAll(): Flow<List<DrawingEntity>> =
        drawings.map { list -> list.sortedByDescending { it.updatedAt } }

    override suspend fun getById(id: Long): DrawingEntity? =
        drawings.value.find { it.id == id }

    override suspend fun insert(drawing: DrawingEntity): Long {
        val id = nextId++
        drawings.update { it + drawing.copy(id = id) }
        return id
    }

    override suspend fun update(drawing: DrawingEntity) {
        drawings.update { list -> list.map { if (it.id == drawing.id) drawing else it } }
    }

    override suspend fun deleteById(id: Long) {
        drawings.update { list -> list.filterNot { it.id == id } }
    }

    /** Test helper: how many drawings are stored. */
    fun count(): Int = drawings.value.size
}