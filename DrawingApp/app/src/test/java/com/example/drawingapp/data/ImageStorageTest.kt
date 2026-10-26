package com.example.drawingapp.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.File

class ImageStorageTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var storage: ImageStorage
    private val sampleBytes = byteArrayOf(1, 2, 3, 4, 5)

    @Before
    fun setUp() {
        storage = ImageStorage(tempFolder.newFolder("images"))
    }

    @Test
    fun saveThenLoad_returnsSameBytes() {
        val path = storage.saveBytes(sampleBytes)
        assertArrayEquals(sampleBytes, storage.loadBytes(path))
    }

    @Test
    fun save_returnsPngPathInsideStorage() {
        val path = storage.saveBytes(sampleBytes)
        assertTrue(path.endsWith(".png"))
        assertTrue(storage.isInStorage(path))
    }

    @Test
    fun twoSaves_getDifferentPaths() {
        assertNotEquals(storage.saveBytes(sampleBytes), storage.saveBytes(sampleBytes))
    }

    @Test
    fun saveWithExistingPath_overwritesFile() {
        val path = storage.saveBytes(sampleBytes)
        val newBytes = byteArrayOf(9, 9, 9)

        assertEquals(path, storage.saveBytes(newBytes, path))
        assertArrayEquals(newBytes, storage.loadBytes(path))
    }

    @Test
    fun loadMissingFile_returnsNull() {
        val missing = File(tempFolder.root, "images/missing.png").absolutePath
        assertNull(storage.loadBytes(missing))
    }

    @Test
    fun deleteImage_removesFile() {
        val path = storage.saveBytes(sampleBytes)
        assertTrue(storage.deleteImage(path))
        assertFalse(File(path).exists())
    }

    @Test
    fun deleteImage_missingFile_returnsFalse() {
        val missing = File(tempFolder.root, "images/missing.png").absolutePath
        assertFalse(storage.deleteImage(missing))
    }

    @Test
    fun deleteImage_outsideStorage_doesNothing() {
        val outside = tempFolder.newFile("outside.png")
        assertFalse(storage.deleteImage(outside.absolutePath))
        assertTrue(outside.exists())
    }

    @Test
    fun importFrom_copiesStreamContents() {
        val path = storage.importFrom(ByteArrayInputStream(sampleBytes))
        assertArrayEquals(sampleBytes, storage.loadBytes(path))
    }

    @Test
    fun fileFor_rejectsPathsOutsideStorage() {
        val sneaky = File(tempFolder.root, "images/../outside.png").path
        assertThrows(IllegalArgumentException::class.java) { storage.fileFor(sneaky) }
        assertThrows(IllegalArgumentException::class.java) { storage.fileFor("") }
    }
}