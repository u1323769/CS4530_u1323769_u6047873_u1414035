/**
 * File:         PenToolbar.kt
 * Owner:        Serena
 * Contributors:
 * Phase:        1
 *
 * Purpose:
 *   Color picker, size slider, shape selector, and clear button.
 *
 * Inputs / Outputs:
 *   - In:  current PenSettings
 *   - Out: onColorSelected / onSizeChanged / onShapeSelected / onClear
 *          (does NOT talk to the ViewModel directly)
 *
 * TODO(Serena):
 *   - Build the controls, highlight current selection, tablet layout
 */
package com.example.drawingapp.ui.toolbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.drawingapp.model.PenSettings
import com.example.drawingapp.model.PenShape

/** Pen customization controls. */
@Composable
fun PenToolbar(
    penSettings: PenSettings,
    onColorSelected: (Color) -> Unit,
    onSizeChanged: (Float) -> Unit,
    onShapeSelected: (PenShape) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO(Serena)
}