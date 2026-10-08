/**
 * File:         PenToolbar.kt
 * Owner:        Serena
 * Phase:        1
 *
 * Purpose:
 *   Color picker, size slider, shape selector, and clear button.
 *
 */
package com.example.drawingapp.ui.toolbar

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.drawingapp.model.PenSettings
import com.example.drawingapp.model.PenShape
import androidx.compose.foundation.layout.Spacer

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
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        BoxWithConstraints(Modifier.padding(12.dp)) {
            when {
                // Tablets: everything fits on one line
                maxWidth >= 1000.dp -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ColorPicker(penSettings.color, onColorSelected)
                    SizeSlider(penSettings.size, onSizeChanged, Modifier.weight(1f))
                    ShapeSelector(penSettings.shape, onShapeSelected)
                    ClearButton(onClear)
                }

                // Phones in landscape: two rows
                maxWidth >= 600.dp -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ColorPicker(penSettings.color, onColorSelected)
                        SizeSlider(penSettings.size, onSizeChanged, Modifier.weight(1f))
                    }
                    ShapesAndClearRow(penSettings.shape, onShapeSelected, onClear)
                }

                // Phones in portrait: stacked
                else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorPicker(penSettings.color, onColorSelected)
                    SizeSlider(penSettings.size, onSizeChanged, Modifier.fillMaxWidth())
                    ShapesAndClearRow(penSettings.shape, onShapeSelected, onClear)
                }
            }
        }
    }
}

@Composable
private fun ShapesAndClearRow(
    selected: PenShape,
    onShapeSelected: (PenShape) -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // let Clear be measured first, so it never gets squeezed
        ShapeSelector(selected, onShapeSelected, Modifier.weight(1f, fill = false))
        Spacer(Modifier.weight(0.001f))
        ClearButton(onClear)
    }
}

@Composable
private fun ClearButton(onClear: () -> Unit) {
    OutlinedButton(onClick = onClear) {
        Text("Clear", maxLines = 1, softWrap = false)
    }
}

@Composable
private fun ColorPicker(
    selected: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PenOptions.palette.forEachIndexed { index, color ->
            val isSelected = color == selected
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Gray,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) }
                    .semantics {
                        contentDescription = "Color ${index + 1}"
                        this.selected = isSelected
                    },
                color = color,
                shape = CircleShape
            ) {}
        }
    }
}

@Composable
private fun ShapeSelector(
    selected: PenShape,
    onShapeSelected: (PenShape) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PenShape.entries.forEach { shape ->
            FilterChip(
                selected = shape == selected,
                onClick = { onShapeSelected(shape) },
                label = {
                    Text(shape.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            )
        }
    }
}
@Composable
private fun SizeSlider(
    size: Float,
    onSizeChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Size ${size.toInt()}", modifier = Modifier.width(64.dp))
        Slider(
            value = PenOptions.clampSize(size),
            onValueChange = { onSizeChanged(PenOptions.clampSize(it)) },
            valueRange = PenOptions.MIN_SIZE..PenOptions.MAX_SIZE
        )
    }
}