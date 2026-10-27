/**
 * File:         PenToolbar.kt
 * Owner:        Serena
 * Phase:        1, updated in 2
 *
 * Purpose:
 *   Compact pen controls: color picker (color wheel dialog with presets),
 *   size slider, shape dropdown, and clear button.
 *
 * Inputs / Outputs:
 *   - In:  current PenSettings
 *   - Out: onColorSelected / onSizeChanged / onShapeSelected / onClear
 *
 * Layouts:
 *   - Phone portrait:  two compact rows
 *   - Phone landscape: narrow vertical side panel
 *   - Tablet:          single row
 */
package com.example.drawingapp.ui.toolbar

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.drawingapp.model.PenSettings
import com.example.drawingapp.model.PenShape
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

private val SWATCH_SIZE = 32.dp
private val SWATCH_SPACING = 8.dp
private const val GRID_COLUMNS = 4

// Pen customization controls.
@Composable
fun PenToolbar(
    penSettings: PenSettings,
    onColorSelected: (Color) -> Unit,
    onSizeChanged: (Float) -> Unit,
    onShapeSelected: (PenShape) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val config = LocalConfiguration.current
    val isPhoneLandscape =
        config.orientation == Configuration.ORIENTATION_LANDSCAPE &&
                config.screenWidthDp < 1000

    if (isPhoneLandscape) {
        VerticalToolbar(penSettings, onColorSelected, onSizeChanged, onShapeSelected, onClear, modifier)
    } else {
        HorizontalToolbar(penSettings, onColorSelected, onSizeChanged, onShapeSelected, onClear, modifier)
    }
}

// Layouts

@Composable
private fun HorizontalToolbar(
    penSettings: PenSettings,
    onColorSelected: (Color) -> Unit,
    onSizeChanged: (Float) -> Unit,
    onShapeSelected: (PenShape) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxWidth(), tonalElevation = 3.dp) {
        BoxWithConstraints(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            if (maxWidth >= 600.dp) {
                // Tablets: one row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ColorPickerButton(penSettings.color, onColorSelected)
                    ShapeDropdown(penSettings.shape, onShapeSelected)
                    SizeSlider(penSettings.size, onSizeChanged, Modifier.weight(1f))
                    ClearButton(onClear)
                }
            } else {
                // Phones in portrait: two compact rows
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ColorPickerButton(penSettings.color, onColorSelected)
                        ShapeDropdown(penSettings.shape, onShapeSelected)
                        Spacer(Modifier.weight(1f))
                        ClearButton(onClear)
                    }
                    SizeSlider(penSettings.size, onSizeChanged, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun VerticalToolbar(
    penSettings: PenSettings,
    onColorSelected: (Color) -> Unit,
    onSizeChanged: (Float) -> Unit,
    onShapeSelected: (PenShape) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxHeight().width(160.dp),
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState()), // safety net on very short screens
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorPickerButton(penSettings.color, onColorSelected)
                Text("Color", style = MaterialTheme.typography.labelLarge)
            }

            ShapeDropdown(penSettings.shape, onShapeSelected, fillWidth = true)

            Column {
                Text("Size ${penSettings.size.toInt()}", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = PenOptions.clampSize(penSettings.size),
                    onValueChange = { onSizeChanged(PenOptions.clampSize(it)) },
                    valueRange = PenOptions.MIN_SIZE..PenOptions.MAX_SIZE
                )
            }

            ClearButton(onClear, Modifier.fillMaxWidth())
        }
    }
}

// Color picker

// Shows the current color, tapping opens a color wheel dialog.
@Composable
private fun ColorPickerButton(
    selected: Color,
    onColorSelected: (Color) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    ColorSwatch(
        color = selected,
        isSelected = true,
        description = "Pen color, tap to change",
        onClick = { showDialog = true },
        size = 36.dp
    )

    if (showDialog) {
        ColorPickerDialog(
            initialColor = selected,
            onConfirm = { color ->
                onColorSelected(color)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

// Color wheel + brightness slider, with the preset palette as quick picks.
@Composable
private fun ColorPickerDialog(
    initialColor: Color,
    onConfirm: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val controller = rememberColorPickerController()
    var pickedColor by remember { mutableStateOf(initialColor) }

    // Smaller wheel when the screen is short, phone in landscape
    val wheelSize = if (LocalConfiguration.current.screenHeightDp < 500) 150.dp else 220.dp

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pen color") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HsvColorPicker(
                    modifier = Modifier.size(wheelSize),
                    controller = controller,
                    initialColor = initialColor,
                    onColorChanged = { envelope: ColorEnvelope ->
                        pickedColor = envelope.color
                    }
                )

                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    controller = controller,
                    initialColor = initialColor
                )

                // Preview of the color that will be applied
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Selected:", style = MaterialTheme.typography.labelLarge)
                    ColorSwatch(
                        color = pickedColor,
                        isSelected = false,
                        description = "Selected color preview",
                        onClick = {}
                    )
                }

                // Quick picks, tap to apply immediately
                Text("Presets", style = MaterialTheme.typography.labelLarge)
                Column(verticalArrangement = Arrangement.spacedBy(SWATCH_SPACING)) {
                    PenOptions.palette.withIndex().chunked(GRID_COLUMNS).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(SWATCH_SPACING)) {
                            row.forEach { (index, color) ->
                                ColorSwatch(
                                    color = color,
                                    isSelected = color == initialColor,
                                    description = "Preset color ${index + 1}",
                                    onClick = { onConfirm(color) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(pickedColor) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Other controls

// Dropdown, combo box, for choosing the pen shape.
@Composable
private fun ShapeDropdown(
    selected: PenShape,
    onShapeSelected: (PenShape) -> Unit,
    fillWidth: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    Box(if (fillWidth) Modifier.fillMaxWidth() else Modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = (if (fillWidth) Modifier.fillMaxWidth() else Modifier)
                .semantics { contentDescription = "Pen shape: ${selected.displayName()}" }
        ) {
            Text(selected.displayName(), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("  ▾")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PenShape.entries.forEach { shape ->
                DropdownMenuItem(
                    text = {
                        Text((if (shape == selected) "✓  " else "     ") + shape.displayName())
                    },
                    onClick = {
                        onShapeSelected(shape)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SizeSlider(
    size: Float,
    onSizeChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("Size ${size.toInt()}", modifier = Modifier.width(64.dp))
        Slider(
            value = PenOptions.clampSize(size),
            onValueChange = { onSizeChanged(PenOptions.clampSize(it)) },
            valueRange = PenOptions.MIN_SIZE..PenOptions.MAX_SIZE
        )
    }
}

@Composable
private fun ClearButton(onClear: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClear, modifier = modifier) {
        Text("Clear", maxLines = 1, softWrap = false)
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    description: String,
    onClick: () -> Unit,
    size: Dp = SWATCH_SIZE
) {
    Surface(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = description
                this.selected = isSelected
            },
        color = color,
        shape = CircleShape
    ) {}
}

// "TRIANGLE" changes to "Triangle"
private fun PenShape.displayName(): String =
    name.lowercase().replaceFirstChar { it.uppercase() }