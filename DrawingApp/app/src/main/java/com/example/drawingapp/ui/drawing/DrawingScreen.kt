/**
 * File:         DrawingScreen.kt
 * Owner:        Shea
 * Contributors: Caroline (ViewModel factory)
 * Phase:        1
 *
 * The full drawing screen.
 */
package com.example.drawingapp.ui.drawing

import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drawingapp.ui.toolbar.PenToolbar
import com.example.drawingapp.viewmodel.DrawingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The ViewModel loads the drawing itself from the navigation argument
 * (DrawingViewModel.DRAWING_ID_ARG), so no id is passed here.
 *
 * @param onBack called after the drawing has been saved
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen(
    onBack: () -> Unit,
    viewModel: DrawingViewModel = viewModel(factory = DrawingViewModel.Factory)
) {
    val penSettings by viewModel.penSettings.collectAsStateWithLifecycle()
    val finishedStrokes by viewModel.strokes.collectAsStateWithLifecycle()
    val currentStroke by viewModel.currentStroke.collectAsStateWithLifecycle()
    val backgroundPath by viewModel.backgroundPath.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()

    // Finished strokes plus the live one, so drawing shows up immediately
    val strokes = remember(finishedStrokes, currentStroke) {
        currentStroke?.let { finishedStrokes + it } ?: finishedStrokes
    }

    // Decode the imported image off the main thread
    val background by produceState<ImageBitmap?>(initialValue = null, backgroundPath) {
        value = backgroundPath?.let { path ->
            withContext(Dispatchers.IO) { DrawingRenderer.loadImage(path) }
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // Save the drawing plus a small preview image for the gallery.
    val saveDrawing: () -> Unit = {
        val thumbnail = if (canvasSize.width > 0 && canvasSize.height > 0) {
            DrawingRenderer.renderThumbnail(strokes, canvasSize.width, canvasSize.height, background)
        } else null
        viewModel.save(thumbnail)
    }

    // Save whenever the app leaves the screen, including being closed.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { saveDrawing() }

    BackHandler {
        saveDrawing()
        onBack()
    }

    val shareDrawing: () -> Unit = {
        if (canvasSize.width > 0 && canvasSize.height > 0) {
            scope.launch {
                val uri = DrawingRenderer.exportToCache(
                    context, strokes, canvasSize.width, canvasSize.height, background
                )
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(send, "Share drawing"))
            }
        }
    }

    val config = LocalConfiguration.current
    val useSideToolbar = config.orientation == Configuration.ORIENTATION_LANDSCAPE ||
            config.screenWidthDp >= 600

    val canvas: @Composable (Modifier) -> Unit = { mod ->
        DrawingCanvas(
            strokes = strokes,
            onStrokeStart = viewModel::startStroke,
            onStrokeMove = viewModel::addPoint,
            onStrokeEnd = viewModel::endStroke,
            modifier = mod,
            backgroundImage = background,
            onCanvasSizeChanged = { canvasSize = it }
        )
    }

    val toolbar: @Composable (Modifier) -> Unit = { _ ->
        PenToolbar(
            penSettings = penSettings,
            onColorSelected = viewModel::setColor,
            onSizeChanged = viewModel::setSize,
            onShapeSelected = viewModel::setShape,
            onClear = viewModel::clear
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = {
                        saveDrawing()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { TextButton(onClick = shareDrawing) { Text("Share") } }
            )
        }
    ) { innerPadding ->
        if (useSideToolbar) {
            // Landscape / tablet: canvas on the left, toolbar on the right
            Row(Modifier.fillMaxSize().padding(innerPadding)) {
                canvas(Modifier.weight(1f).fillMaxHeight())
                Column(
                    Modifier
                        .width(300.dp)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    toolbar(Modifier.fillMaxWidth())
                }
            }
        } else {
            // Portrait phone: canvas on top, toolbar at the bottom
            Column(Modifier.fillMaxSize().padding(innerPadding)) {
                canvas(Modifier.weight(1f).fillMaxWidth())
                toolbar(Modifier.fillMaxWidth())
            }
        }
    }
}