/**
 * File:         MainActivity.kt
 * Owner:        Team
 * Phase:        1
 *
 * Purpose:
 *   App entry point. Applies the theme and starts AppNavigation.
 */
package com.example.drawingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.drawingapp.navigation.AppNavigation
import com.example.drawingapp.ui.theme.DrawingAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawingAppTheme {
                AppNavigation()
            }
        }
    }
}