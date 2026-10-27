/**
 * File:         DrawingRepository.kt
 * Owner:        Caroline
 * Phase:        2
 *
 *   Middle layer between the ViewModels and the Room database / image
 *   files (repository pattern).
 */
package com.example.drawingapp.data

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.example.drawingapp.model.Drawing
import com.example.drawingapp.model.DrawingSummary
import com.example.drawingapp.model.NEW_DRAWING_ID
import com.example.drawingapp.model.Stroke
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.InputStream

class DrawingRepository internal constructor(
    private val dao: DrawingDao,
    private val imageStorage: ImageStorage,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {

    /** Every saved drawing for the gallery, most recently edited first. */
    val allDrawings: Flow<List<DrawingSummary>> =
        dao.getAll().map { list -> list.map { it.toSummary() } }

    /** Loads one drawing to edit, or null if it doesn't exist. */
    suspend fun getDrawing(id: Long): Drawing? = dao.getById(id)?.toDrawing()

    /**
     * Saves a drawing and returns its id.
     * New drawings (NEW_DRAWING_ID) are inserted; existing ones are updated.
     * If [thumbnail] is null, the old thumbnail is kept.
     */
    suspend fun saveDrawing(
        id: Long,
        title: String,
        strokes: List<Stroke>,
        backgroundPath: String?,
        thumbnail: ImageBitmap?
    ): Long {
        val existing = if (id == NEW_DRAWING_ID) null else dao.getById(id)

        val thumbnailPath = if (thumbnail != null) {
            withContext(ioDispatcher) {
                // Overwrite the old thumbnail file if there is one, so files don't pile up
                val oldPath = existing?.thumbnailPath?.takeIf { imageStorage.isInStorage(it) }
                imageStorage.saveImage(thumbnail.asAndroidBitmap(), oldPath)
            }
        } else {
            existing?.thumbnailPath
        }

        val entity = DrawingEntity(
            id = existing?.id ?: 0L,
            title = title,
            strokes = strokes,
            backgroundPath = backgroundPath,
            thumbnailPath = thumbnailPath,
            updatedAt = clock()
        )

        return if (existing == null) {
            dao.insert(entity)
        } else {
            dao.update(entity)
            existing.id
        }
    }

    /** Deletes a drawing and its image files. Does nothing if it doesn't exist. */
    suspend fun deleteDrawing(id: Long) {
        val drawing = dao.getById(id) ?: return
        dao.deleteById(id)
        withContext(ioDispatcher) {
            drawing.thumbnailPath?.let { imageStorage.deleteImage(it) }
            drawing.backgroundPath?.let { imageStorage.deleteImage(it) }
        }
    }

    /** Copies an imported image into app storage and returns its file path. */
    suspend fun importImage(input: InputStream): String =
        withContext(ioDispatcher) { imageStorage.importFrom(input) }

    companion object {
        @Volatile
        private var INSTANCE: DrawingRepository? = null

        /** Returns the one shared repository, creating it the first time. */
        fun getInstance(context: Context): DrawingRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: DrawingRepository(
                    dao = AppDatabase.getInstance(context).drawingDao(),
                    imageStorage = ImageStorage.create(context.applicationContext)
                ).also { INSTANCE = it }
            }
    }
}

private fun DrawingEntity.toSummary() = DrawingSummary(id, title, thumbnailPath, updatedAt)

private fun DrawingEntity.toDrawing() = Drawing(id, title, strokes, backgroundPath)