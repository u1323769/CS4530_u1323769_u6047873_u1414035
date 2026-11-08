/**
 * File:         AnalysisViewModel.kt
 * Owner:        Shea
 * Phase:        3
 *
 * Purpose:
 *   Loads the drawing's imported image, asks VisionRepository to analyze it,
 *   and exposes the result as UI state.
 *
 * Used by:
 *   - AnalysisScreen
 *
 * Notes:
 *   - Only talks to repositories (DrawingRepository + VisionRepository).
 *   - Drawing id comes from navigation (SavedStateHandle).
 *
 * Tested by:
 *   - AnalysisViewModelTest.kt
 *
 * TODO(Shea):
 *   - UiState: Loading, Success(objects), Empty, Error(message)
 *   - analyze(), retry(), Factory
 */
package com.example.drawingapp.viewmodel

