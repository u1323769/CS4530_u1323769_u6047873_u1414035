/**
 * File:         ImageStorage.kt
 * Owner:        Serena
 * Phase:        2
 *
 *  Saves and deletes image files (thumbnails, imported images) in the
 *  app's storage. The database only stores their file paths.
 */
package com.example.drawingapp.data

import android.content.Context
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.UUID

class ImageStorage(directory: File) {

    private val directory: File = directory.apply { mkdirs() }.canonicalFile

    // Main API (used by DrawingRepository)

    // Saves bitmap as a PNG and returns its file path.
    // Pass existingPath to overwrite that image instead of creating a new file.

    fun saveImage(bitmap: Bitmap, existingPath: String? = null): String =
        saveBytes(bitmap.toPngBytes(), existingPath)

    // Deletes the image at path. Returns true if a file was deleted.
    fun deleteImage(path: String): Boolean =
        isInStorage(path) && File(path).delete()

    // The File for a stored image. Only paths inside this folder are allowed.
    fun fileFor(path: String): File {
        require(isInStorage(path)) { "Path is not in image storage: $path" }
        return File(path)
    }

    // Byte/stream versions (unit tested)

    fun saveBytes(bytes: ByteArray, existingPath: String? = null): String {
        val file = if (existingPath != null) fileFor(existingPath) else newFile()
        file.writeBytes(bytes)
        return file.absolutePath
    }

    fun loadBytes(path: String): ByteArray? {
        if (!isInStorage(path)) return null
        val file = File(path)
        return if (file.exists()) file.readBytes() else null
    }

    fun importFrom(input: InputStream): String {
        val file = newFile()
        input.use { source -> file.outputStream().use { target -> source.copyTo(target) } }
        return file.absolutePath
    }

    // True only for files directly inside this storage folder.
    fun isInStorage(path: String): Boolean {
        if (path.isBlank()) return false
        return File(path).canonicalFile.parentFile == directory
    }

    private fun newFile(): File = File(directory, "image_${UUID.randomUUID()}.png")

    companion object {
        const val FOLDER_NAME = "images"

        // Standard instance using the app's private folder.
        fun create(context: Context): ImageStorage =
            ImageStorage(File(context.filesDir, FOLDER_NAME))
    }
}

// Converts a Bitmap to PNG bytes.
fun Bitmap.toPngBytes(): ByteArray =
    ByteArrayOutputStream().use { out ->
        compress(Bitmap.CompressFormat.PNG, 100, out)
        out.toByteArray()
    }