package com.example.drawingapp.ui.analysis

/**
 * File:         AnalysisScreen.kt
 * Owner:        Shea
 * Phase:        3
 *
 * Purpose:
 *   Shows the imported image with boxes around detected objects, plus a list
 *   of objects and their confidence scores.
 *
 * Inputs / Outputs:
 *   - In:  AnalysisViewModel state (loading / results / empty / error)
 *   - Out: onBack, onRetry, onOpenDrawing
 *
 * TODO(Shea):
 *   - Loading spinner, "No objects found", error message + Retry
 *   - Image + BoundingBoxOverlay on top, list below (side by side on tablets)
 */
import android.content.res.Configuration
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drawingapp.model.DetectedObject
import com.example.drawingapp.ui.drawing.DrawingRenderer
import com.example.drawingapp.viewmodel.AnalysisUiState
import com.example.drawingapp.viewmodel.AnalysisViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** @param onBack navigate back to the gallery */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    onBack: () -> Unit,
    viewModel: AnalysisViewModel = viewModel(factory = AnalysisViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedIndex by viewModel.selectedIndex.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image analysis") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (val s = state) {
                AnalysisUiState.Loading -> LoadingContent()

                is AnalysisUiState.Success -> ResultsContent(
                    imagePath = s.imagePath,
                    objects = s.objects,
                    selectedIndex = selectedIndex,
                    onSelect = viewModel::selectObject
                )

                is AnalysisUiState.Empty -> MessageContent(
                    title = "No objects found",
                    detail = "Vision didn't recognize anything in this image.",
                    imagePath = s.imagePath,
                    onRetry = viewModel::retry
                )

                is AnalysisUiState.Error -> MessageContent(
                    title = "Analysis failed",
                    detail = s.message,
                    imagePath = s.imagePath,
                    onRetry = viewModel::retry
                )

                AnalysisUiState.NoImage -> MessageContent(
                    title = "No image to analyze",
                    detail = "Only drawings with an imported image can be analyzed.",
                    imagePath = null,
                    onRetry = null
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text(
            "Analyzing image…",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

/** Used for empty, error and no-image states: optional picture, message, optional retry. */
@Composable
private fun MessageContent(
    title: String,
    detail: String,
    imagePath: String?,
    onRetry: (() -> Unit)?
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (imagePath != null) {
            AnalyzedImage(
                imagePath = imagePath,
                objects = emptyList(),
                selectedIndex = null,
                modifier = Modifier.weight(1f, fill = false).fillMaxWidth()
            )
            Box(Modifier.height(16.dp))
        }
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(
            detail,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        if (onRetry != null) {
            Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) { Text("Try again") }
        }
    }
}

@Composable
private fun ResultsContent(
    imagePath: String,
    objects: List<DetectedObject>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit
) {
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val image: @Composable (Modifier) -> Unit = { mod ->
        AnalyzedImage(imagePath, objects, selectedIndex, mod.padding(12.dp))
    }
    val list: @Composable (Modifier) -> Unit = { mod ->
        ObjectList(objects, selectedIndex, onSelect, mod)
    }

    if (landscape) {
        Row(Modifier.fillMaxSize()) {
            image(Modifier.weight(1f).fillMaxSize())
            list(Modifier.width(320.dp).fillMaxSize())
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            image(Modifier.weight(1f).fillMaxWidth())
            list(Modifier.weight(1f).fillMaxWidth())
        }
    }
}

/**
 * The image with boxes on top. The inner Box takes the image's exact aspect
 * ratio, so the overlay and the picture cover the same area and the
 * normalized box coordinates line up.
 */
@Composable
private fun AnalyzedImage(
    imagePath: String,
    objects: List<DetectedObject>,
    selectedIndex: Int?,
    modifier: Modifier = Modifier
) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, imagePath) {
        value = withContext(Dispatchers.IO) { DrawingRenderer.loadImage(imagePath) }
    }

    Box(modifier, contentAlignment = Alignment.Center) {
        bitmap?.let { image ->
            Box(Modifier.aspectRatio(image.width.toFloat() / image.height.toFloat())) {
                Image(
                    bitmap = image,
                    contentDescription = "Imported image",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
                BoundingBoxOverlay(objects, selectedIndex, Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun ObjectList(
    objects: List<DetectedObject>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier, contentPadding = PaddingValues(12.dp)) {
        item {
            Text(
                "Found ${objects.size} ${if (objects.size == 1) "object" else "objects"}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        itemsIndexed(objects) { index, obj ->
            val selected = index == selectedIndex
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(14.dp).clip(CircleShape).background(boxColor(index)))
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(obj.name, style = MaterialTheme.typography.bodyLarge)
                    LinearProgressIndicator(
                        progress = { obj.confidence.coerceIn(0f, 1f) },
                        color = boxColor(index),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                }
                Text(confidenceLabel(obj.confidence), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

