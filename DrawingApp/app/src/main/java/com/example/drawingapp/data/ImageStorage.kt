/**
 * File:         ImageStorage.kt
 * Owner:        Serena
 * Phase:        2
 *
 * Purpose:
 *   Saves and deletes image files (thumbnails, imported images) in the
 *   app's storage. The database only stores their file paths.
 *
 * Used by:
 *   - DrawingRepository
 *   - ShareHelper (fileFor() to share an image)
 * Notes:
 *   - Core logic works on bytes and files so it can be unit tested
 *     without an emulator. Bitmap/Uri helpers are thin wrappers.
 */
package com.example.drawingapp.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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

    // Loads the image at path, or null if it doesn't exist.
    fun loadImage(path: String): Bitmap? =
        loadBytes(path)?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }

    // Copies a gallery image into storage. Returns its path, or null on failure.
    fun importImage(context: Context, uri: Uri): String? =
        context.contentResolver.openInputStream(uri)?.let { importFrom(it) }

    // The File for a stored image (ShareHelper uses this).
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