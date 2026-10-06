/**
 * File:         SplashScreen.kt
 * Owner:        Serena
 * Contributors:
 * Phase:        1
 *
 * Purpose:
 *   Animated splash screen shown on launch: a sun above the cursive
 *   letters "css" (our initials / Creative Sketch Studio) bobbing like
 *   waves, with the app title underneath. Calls onFinished() when done.
 *
 * Used by:
 *   - AppNavigation (start screen)
 */
package com.example.drawingapp.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val SunYellow = Color(0xFFFDD835)
private val OceanBlue = Color(0xFF1E88E5)

private const val RAY_COUNT = 12
private val LETTERS = listOf("c", "s", "s")

// Swap for FontFamily(Font(R.font.pacifico))
private val SplashFont = FontFamily.Cursive

/** Splash screen shown when the app opens. */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val sunScale = remember { Animatable(0f) }
    val rayProgress = remember { Animatable(0f) }
    val letterProgress = remember { Animatable(0f) } // 0..3, one unit per letter
    val wavePhase = remember { Animatable(0f) }      // each 1.0 = one full bob
    val titleAlpha = remember { Animatable(0f) }

    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        launch { sunScale.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }
        launch {
            delay(300)
            rayProgress.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        }
        launch {
            delay(500)
            wavePhase.animateTo(2f, tween(1400, easing = LinearEasing))
        }
        launch {
            delay(1000)
            titleAlpha.animateTo(1f, tween(500))
        }
        delay(500)
        letterProgress.animateTo(LETTERS.size.toFloat(), tween(600, easing = LinearEasing))
        delay(800) // let the letters bob a little longer
        currentOnFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(modifier = Modifier.size(width = 200.dp, height = 150.dp)) {
                drawSun(sunScale.value, rayProgress.value)
            }

            // c s s riding the waves
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                LETTERS.forEachIndexed { index, letter ->
                    Text(
                        text = letter,
                        fontFamily = SplashFont,
                        fontSize = 80.sp,
                        color = OceanBlue,
                        modifier = Modifier.graphicsLayer {
                            // Each letter fades/rises in after the one before it
                            val appear = (letterProgress.value - index).coerceIn(0f, 1f)
                            alpha = appear
                            val rise = (1f - appear) * 24.dp.toPx()

                            // Rolling wave: each letter lags behind the previous one
                            val angle = (wavePhase.value * 2 * PI - index * 0.9).toFloat()
                            val bob = sin(angle) * 8.dp.toPx()

                            translationY = rise + bob
                        }
                    )
                }
            }

            Text(
                text = "Creative Sketch Studio",
                fontFamily = SplashFont,
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.graphicsLayer { alpha = titleAlpha.value }
            )
        }
    }
}

/** Yellow circle with dash-shaped rays around it. */
private fun DrawScope.drawSun(scale: Float, rayProgress: Float) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = size.height * 0.22f

    drawCircle(color = SunYellow, radius = radius * scale, center = center)

    if (rayProgress <= 0f) return
    val rayStart = radius * 1.35f
    val rayLength = radius * 0.6f * rayProgress
    val rayWidth = 4.dp.toPx()

    for (i in 0 until RAY_COUNT) {
        val angle = (2 * PI * i / RAY_COUNT).toFloat()
        val dir = Offset(cos(angle), sin(angle))
        drawLine(
            color = SunYellow,
            start = center + dir * rayStart,
            end = center + dir * (rayStart + rayLength),
            strokeWidth = rayWidth,
            cap = StrokeCap.Round
        )
    }
}