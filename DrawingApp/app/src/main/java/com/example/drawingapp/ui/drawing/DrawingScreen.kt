/**
 * File:         DrawingScreen.kt
 * Owner:        Shea
 * Contributors: Serena (toolbar wiring)
 * Phase:        1
 *
 * Purpose:
 *   The full drawing screen. The ONLY composable that talks to the
 *   ViewModel: passes state down to DrawingCanvas and PenToolbar and
 *   sends their events back up.
 *
 * Notes:
 *   - viewModel() returns the same instance after rotation.
 *   - Read state with collectAsStateWithLifecycle().
 *
 * TODO(Shea):
 *   - Canvas on top, PenToolbar at the bottom
 *   - Tablet/landscape layout
 */
package com.example.drawingapp.ui.drawing

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drawingapp.ui.toolbar.PenToolbar
import com.example.drawingapp.viewmodel.DrawingViewModel
import androidx.compose.runtime.remember

/** Drawing screen: connects the ViewModel to the canvas and toolbar. */
@Composable
fun DrawingScreen(viewModel: DrawingViewModel = viewModel()) {
    val penSettings by viewModel.penSettings.collectAsStateWithLifecycle()
    val finishedStrokes by viewModel.strokes.collectAsStateWithLifecycle()
    val currentStroke by viewModel.currentStroke.collectAsStateWithLifecycle()

    val strokes = remember(finishedStrokes, currentStroke) {
        currentStroke?.let { finishedStrokes + it } ?: finishedStrokes
    }

    val config = LocalConfiguration.current
    val useSideToolbar = config.orientation == Configuration.ORIENTATION_LANDSCAPE || config.screenWidthDp >= 600

    val canvas: @Composable (Modifier) -> Unit = { mod ->
        DrawingCanvas(
            strokes = strokes,
            onStrokeStart = viewModel::startStroke,
            onStrokeMove = viewModel::addPoint,
            onStrokeEnd = viewModel::endStroke,
            modifier = mod
        )
    }

    val toolbar: @Composable (Modifier) -> Unit = { mod ->
        PenToolbar(
            penSettings = penSettings,
            onColorSelected = viewModel::setColor,
            onSizeChanged = viewModel::setSize,
            onShapeSelected = viewModel::setShape,
            onClear = viewModel::clear
        )
    }

    Scaffold { innerPadding ->
        if(useSideToolbar) {
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
        }else{
            Column(Modifier.fillMaxSize().padding(innerPadding)) {
                canvas(Modifier.weight(1f).fillMaxWidth())
                toolbar(Modifier.fillMaxWidth())
            }
        }

    }
}