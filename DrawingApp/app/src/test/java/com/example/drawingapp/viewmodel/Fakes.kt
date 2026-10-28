/**
 * In-memory fakes so ViewModel tests can build a real DrawingRepository
 * without Room or the file system.
 *
 * Signatures are inferred from how DrawingRepository calls ImageStorage and
 * DrawingDao. If the compiler says "does not override anything" or
 * "abstract member not implemented", match these to the real declarations
 * (add/remove `suspend`, change a return type, add any missing method).
 *
 * Put this in the same test package as GalleryViewModelTest.
 */
package com.example.drawingapp.viewmodel

import com.example.drawingapp.data.DrawingDao
import com.example.drawingapp.data.DrawingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeDrawingDao : DrawingDao {
    private val store = MutableStateFlow<List<DrawingEntity>>(emptyList())
    private var nextId = 1L

    override fun getAll(): Flow<List<DrawingEntity>> =
        store.map { list -> list.sortedByDescending { it.updatedAt } }

    override suspend fun getById(id: Long): DrawingEntity? =
        store.value.firstOrNull { it.id == id }

    override suspend fun insert(entity: DrawingEntity): Long {
        val id = nextId++
        store.value = store.value + entity.copy(id = id)
        return id
    }

    override suspend fun update(entity: DrawingEntity) {
        store.value = store.value.map { if (it.id == entity.id) entity else it }
    }

    override suspend fun deleteById(id: Long) {
        store.value = store.value.filterNot { it.id == id }
    }
}