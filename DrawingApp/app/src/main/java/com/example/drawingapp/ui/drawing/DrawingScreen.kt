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

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drawingapp.viewmodel.DrawingViewModel

/** Drawing screen: connects the ViewModel to the canvas and toolbar. */
@Composable
fun DrawingScreen(viewModel: DrawingViewModel = viewModel()) {
    // TODO(Shea)
}