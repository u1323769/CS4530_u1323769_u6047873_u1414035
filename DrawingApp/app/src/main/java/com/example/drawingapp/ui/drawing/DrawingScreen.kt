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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drawingapp.ui.toolbar.PenToolbar
import com.example.drawingapp.viewmodel.DrawingViewModel

/** Drawing screen: connects the ViewModel to the canvas and toolbar. */
@Composable
fun DrawingScreen(viewModel: DrawingViewModel = viewModel()) {
    val penSettings by viewModel.penSettings.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            // TODO(Shea): replace this Box with DrawingCanvas
            Box(Modifier.weight(1f).fillMaxWidth())

            PenToolbar(
                penSettings = penSettings,
                onColorSelected = viewModel::setColor,
                onSizeChanged = viewModel::setSize,
                onShapeSelected = viewModel::setShape,
                onClear = viewModel::clear
            )
        }
    }
}