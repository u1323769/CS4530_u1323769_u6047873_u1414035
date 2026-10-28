/**
 * File:         AppNavigation.kt
 * Owner:        Serena
 * Phase:        1
 *
 * Purpose:
 *   Routes and navigation. Phase 1 flow: Splash -> Drawing.
 */
package com.example.drawingapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.drawingapp.model.NEW_DRAWING_ID
import com.example.drawingapp.ui.drawing.DrawingScreen
import com.example.drawingapp.ui.gallery.GalleryScreen
import com.example.drawingapp.ui.splash.SplashScreen
import com.example.drawingapp.viewmodel.DrawingViewModel

/** All route names in one place. */
object Routes {
    const val SPLASH = "splash"
    const val GALLERY = "gallery"
    const val DRAWING = "drawing/{${DrawingViewModel.DRAWING_ID_ARG}}"
    fun drawing(id: Long) = "drawing/$id"
}

/** App navigation graph. */
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
                    navController.navigate(Routes.GALLERY) {
                        // Remove splash from the back stack so Back exits the app
                        popUpTo(Routes.SPLASH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.GALLERY) {
            GalleryScreen(
                onNewDrawing = { navController.navigate(Routes.drawing(NEW_DRAWING_ID)) },
                onOpenDrawing = { id -> navController.navigate(Routes.drawing(id)) }
            )
        }

        composable(
            route = Routes.DRAWING,
            arguments = listOf(
                navArgument(DrawingViewModel.DRAWING_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = NEW_DRAWING_ID
                }
            )
        ) {
            // DrawingViewModel reads the id from its SavedStateHandle,
            // so the screen only needs to know how to go back.
            DrawingScreen(onBack = { navController.popBackStack() })
        }
    }
}