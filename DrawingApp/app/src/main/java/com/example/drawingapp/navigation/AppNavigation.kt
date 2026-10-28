/**
 * File:         AppNavigation.kt
 * Owner:        Serena
 * Phase:        2
 *
 * Purpose:
 *   Routes and navigation.
 */
package com.example.drawingapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.drawingapp.ui.drawing.DrawingScreen
import com.example.drawingapp.ui.splash.SplashScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

// All route names in one place.
object Routes {
    const val SPLASH = "splash"
    const val GALLERY = "gallery"

    const val DRAWING_ID_ARG = "drawingId"
    private const val DRAWING_BASE = "drawing"

    // Route pattern registered in the NavHost.
    const val DRAWING = "$DRAWING_BASE?$DRAWING_ID_ARG={$DRAWING_ID_ARG}"

    // Value used when no id is passed
    const val NO_DRAWING_ID = -1L

    // Route to open a drawing: null = new drawing, otherwise the saved drawing's id.
    fun drawing(id: Long? = null): String =
        if (id == null) DRAWING_BASE else "$DRAWING_BASE?$DRAWING_ID_ARG=$id"

    // Converts the raw nav argument back to an id, or null for a new drawing.
    fun toDrawingIdOrNull(raw: Long): Long? = raw.takeIf { it > 0 }
}

// App navigation graph.
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = {
                    // TODO: change to Routes.GALLERY once GalleryScreen exists
                    navController.navigate(Routes.DRAWING) {
                        // Remove splash from the back stack so Back exits the app
                        popUpTo(Routes.SPLASH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Routes.DRAWING,
            arguments = listOf(
                navArgument(Routes.DRAWING_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = Routes.NO_DRAWING_ID
                }
            )
        ) {
            // TODO: pass drawingId and onBack once DrawingScreen accepts them
            DrawingScreen()
        }
    }
}