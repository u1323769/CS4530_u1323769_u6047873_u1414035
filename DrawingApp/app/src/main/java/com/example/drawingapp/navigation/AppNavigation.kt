/**
 * File:         AppNavigation.kt
 * Owner:        Serena
 * Contributors:
 * Phase:        1
 *
 * Purpose:
 *   Routes and navigation. Phase 1 flow: Splash -> Drawing.
 *
 * Used by:
 *   - MainActivity
 *
 * Notes:
 *   - Pop splash off the back stack so Back exits the app.
 *   - Later phases add: gallery, backup/sharing.
 *
 * TODO(Serena):
 *   - NavHost with SPLASH and DRAWING
 */
package com.example.drawingapp.navigation

import androidx.compose.runtime.Composable

/** All route names in one place. */
object Routes {
    const val SPLASH = "splash"
    const val DRAWING = "drawing"
}

/** App navigation graph. */
@Composable
fun AppNavigation() {
    // TODO(Serena)
}