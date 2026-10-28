/**
 * File:         GalleryViewModel.kt
 * Owner:        Shea
 * Contributors:
 * Phase:        2
 *
 * Gives GalleryScreen the list of saved drawings and handles delete.
 */
package com.example.drawingapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.drawingapp.data.DrawingRepository
import com.example.drawingapp.model.DrawingSummary
import com.example.drawingapp.model.NEW_DRAWING_ID
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream

class GalleryViewModel(private val repository: DrawingRepository) : ViewModel() {

    /** Every saved drawing, most recently edited first. */
    val drawings: StateFlow<List<DrawingSummary>> = repository.allDrawings
        .map { list -> list.sortedByDescending { it.updatedAt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Remove a drawing (and its image files) from storage. */
    fun deleteDrawing(id: Long) {
        viewModelScope.launch { repository.deleteDrawing(id) }
    }

    /**
     * Copy the picked image into app storage, create a new drawing that uses it
     * as the background, then report the new id so the UI can open it.
     * The stream is closed when done.
     */
    fun importImage(input: InputStream, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val path = input.use { repository.importImage(it) }
            val id = repository.saveDrawing(
                id = NEW_DRAWING_ID,
                title = "Imported image",
                strokes = emptyList(),
                backgroundPath = path,
                thumbnail = null
            )
            onCreated(id)
        }
    }

    companion object {
        /** Builds the ViewModel with the app-wide repository instance. */
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as Application
                GalleryViewModel(DrawingRepository.getInstance(app))
            }
        }
    }
}