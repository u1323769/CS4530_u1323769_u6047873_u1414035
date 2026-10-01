package com.example.drawingapp.ui.drawing

/**
 * File:         DrawingScreen.kt
 * Owner:        Shea
 * Contributors: Serena (toolbar wiring)
 * Phase:        1
 *
 * Purpose:
 *   The full drawing screen. The ONLY composable that talks to
 *   DrawingViewModel: collects its state, passes it down to
 *   DrawingCanvas and PenToolbar, and sends their events back up.
 *
 * Used by:
 *   - AppNavigation ("drawing" route)
 *
 * Notes:
 *   - viewModel() returns the same instance after rotation, which is
 *     what keeps the drawing from disappearing.
 *   - Use collectAsStateWithLifecycle() to read StateFlows.
 *
 * TODO(Shea):
 *   - Layout: canvas on top, toolbar at bottom
 *   - Tablet/landscape layout
 */