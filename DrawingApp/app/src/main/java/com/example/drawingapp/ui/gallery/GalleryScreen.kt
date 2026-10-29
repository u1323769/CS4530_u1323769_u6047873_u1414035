/**
 * File:         GalleryScreen.kt
 * Owner:        Shea
 * Contributors: Caroline (import via rememberImagePicker)
 * Phase:        2
 *
 * Main screen after the splash: shows all saved drawings and a button
 * to start a new one.
 */
package com.example.drawingapp.ui.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drawingapp.model.DrawingSummary
import com.example.drawingapp.share.rememberImagePicker
import com.example.drawingapp.ui.drawing.DrawingRenderer
import com.example.drawingapp.viewmodel.GalleryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

/**
 * @param onNewDrawing   navigate to an empty drawing
 * @param onOpenDrawing  navigate to the drawing with this id
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onNewDrawing: () -> Unit,
    onOpenDrawing: (Long) -> Unit,
    viewModel: GalleryViewModel = viewModel(factory = GalleryViewModel.Factory)
) {
    val drawings by viewModel.drawings.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<DrawingSummary?>(null) }

    val context = LocalContext.current
    val pickImage = rememberImagePicker { uri ->
        context.contentResolver.openInputStream(uri)?.let { input ->
            viewModel.importImage(input, onOpenDrawing)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Drawings") },
                actions = {
                    TextButton(onClick = pickImage) { Text("Import image") }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewDrawing,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New drawing") }
            )
        }
    ) { innerPadding ->
        if (drawings.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No drawings yet. Tap \"New drawing\" to start.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(drawings, key = { it.id }) { drawing ->
                    DrawingCard(
                        drawing = drawing,
                        onOpen = { onOpenDrawing(drawing.id) },
                        onDelete = { pendingDelete = drawing }
                    )
                }
            }
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete drawing?") },
            text = { Text("\"${target.title}\" will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDrawing(target.id)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DrawingCard(
    drawing: DrawingSummary,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Column {
            DrawingThumbnail(
                path = drawing.thumbnailPath,
                updatedAt = drawing.updatedAt,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
            )
            Row(
                Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        drawing.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        DateFormat.getDateInstance(DateFormat.SHORT).format(Date(drawing.updatedAt)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete ${drawing.title}")
                }
            }
        }
    }
}

/**
 * Shows the saved thumbnail file. updatedAt is a key so the image reloads
 * when the drawing is re-saved over the same file path.
 */
@Composable
private fun DrawingThumbnail(path: String?, updatedAt: Long, modifier: Modifier = Modifier) {
    val image by produceState<ImageBitmap?>(initialValue = null, path, updatedAt) {
        value = path?.let { withContext(Dispatchers.IO) { DrawingRenderer.loadImage(it, 512) } }
    }

    Box(modifier.background(Color.White), contentAlignment = Alignment.Center) {
        image?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}