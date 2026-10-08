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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.drawingapp.ui.drawing.DrawingScreen
import com.example.drawingapp.ui.splash.SplashScreen

/** All route names in one place. */
object Routes {
    const val SPLASH = "splash"
    const val DRAWING = "drawing"
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
                    navController.navigate(Routes.DRAWING) {
                        // Remove splash from the back stack so Back exits the app
                        popUpTo(Routes.SPLASH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.DRAWING) {
            DrawingScreen()
        }
    }
}