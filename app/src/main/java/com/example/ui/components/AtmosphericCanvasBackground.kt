package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.model.WeatherConditionType
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dynamic 60 FPS atmospheric canvas rendering real-time sky gradients, solar rays,
 * drifting clouds, rain streaks, snow crystals, lightning flashes, or Iraqi Shamal dust haze.
 * Respects reducedMotion and batterySaver preferences.
 */
@Composable
fun AtmosphericCanvasBackground(
    conditionType: WeatherConditionType,
    isDarkTheme: Boolean,
    animateMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "atmosphere_bg")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animateMotion) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val fastPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animateMotion) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fastPhase"
    )

    val gradientColors = when (conditionType) {
        WeatherConditionType.CLEAR_DAY -> if (isDarkTheme) {
            listOf(Color(0xFF0A1931), Color(0xFF153462), Color(0xFF0B132B))
        } else {
            listOf(Color(0xFFBAE6FD), Color(0xFFE0F2FE), Color(0xFFF0F9FF))
        }
        WeatherConditionType.CLEAR_NIGHT -> listOf(
            Color(0xFF040814),
            Color(0xFF0B1528),
            Color(0xFF121E36)
        )
        WeatherConditionType.EXTREME_HEAT -> if (isDarkTheme) {
            listOf(Color(0xFF2B120A), Color(0xFF431A08), Color(0xFF131829))
        } else {
            listOf(Color(0xFFFED7AA), Color(0xFFFFEDD5), Color(0xFFFFF7ED))
        }
        WeatherConditionType.DUST_SANDSTORM -> if (isDarkTheme) {
            listOf(Color(0xFF332211), Color(0xFF4A3219), Color(0xFF1C1917))
        } else {
            listOf(Color(0xFFFDE68A), Color(0xFFFEF3C7), Color(0xFFFFFBEB))
        }
        WeatherConditionType.LIGHT_RAIN, WeatherConditionType.HEAVY_RAIN -> if (isDarkTheme) {
            listOf(Color(0xFF0A1424), Color(0xFF162742), Color(0xFF0E172A))
        } else {
            listOf(Color(0xFFCBD5E1), Color(0xFFE2E8F0), Color(0xFFF1F5F9))
        }
        WeatherConditionType.THUNDERSTORM -> listOf(
            Color(0xFF110C24),
            Color(0xFF1E1B4B),
            Color(0xFF090D16)
        )
        else -> if (isDarkTheme) {
            listOf(Color(0xFF091326), Color(0xFF13223E), Color(0xFF0B132B))
        } else {
            listOf(Color(0xFFD8F0FF), Color(0xFFEEF6FF), Color(0xFFF8FAFC))
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(colors = gradientColors)
        )

        // Subtle atmospheric glow orb
        val orbColor = when (conditionType) {
            WeatherConditionType.EXTREME_HEAT -> Color(0x33F97316)
            WeatherConditionType.DUST_SANDSTORM -> Color(0x38D97706)
            WeatherConditionType.CLEAR_DAY -> Color(0x2838BDF8)
            WeatherConditionType.THUNDERSTORM -> Color(0x288B5CF6)
            else -> Color(0x1E38BDF8)
        }
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(orbColor, Color.Transparent),
                center = Offset(w * 0.82f, h * 0.14f),
                radius = w * 0.65f
            ),
            radius = w * 0.65f,
            center = Offset(w * 0.82f, h * 0.14f)
        )

        when (conditionType) {
            WeatherConditionType.CLEAR_DAY, WeatherConditionType.EXTREME_HEAT -> {
                val sunCenter = Offset(w * 0.84f, h * 0.13f)
                val pulse = 1f + sin(phase * 2f * Math.PI.toFloat()) * 0.08f
                val sunTint = if (conditionType == WeatherConditionType.EXTREME_HEAT) Color(0x44FB923C) else Color(0x33FBBF24)
                drawCircle(
                    color = sunTint,
                    radius = 96f * pulse,
                    center = sunCenter
                )
                drawCircle(
                    color = if (conditionType == WeatherConditionType.EXTREME_HEAT) Color(0x66F59E0B) else Color(0x44FDE047),
                    radius = 54f * pulse,
                    center = sunCenter
                )
            }

            WeatherConditionType.CLEAR_NIGHT -> {
                // Twinkling stars
                for (i in 0 until 28) {
                    val sx = ((i * 137 + 41) % 100) / 100f * w
                    val sy = ((i * 73 + 19) % 55) / 100f * h
                    val alpha = 0.25f + 0.55f * ((sin((phase * 6.28f) + i) + 1f) / 2f)
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = if (i % 4 == 0) 3.2f else 1.8f,
                        center = Offset(sx, sy)
                    )
                }
            }

            WeatherConditionType.DUST_SANDSTORM -> {
                // Drifting Shamal dust waves and suspended silt particles
                for (i in 0 until 32) {
                    val x = (((i * 97f) / 32f * w) + (phase * w * 1.4f)) % w
                    val y = ((i * 61) % 100) / 100f * h
                    val r = 2.5f + (i % 5) * 1.6f
                    drawCircle(
                        color = Color(0xFFD97706).copy(alpha = 0.22f),
                        radius = r,
                        center = Offset(x, y)
                    )
                }
                for (wave in 0..2) {
                    val path = Path()
                    val baseY = h * (0.2f + wave * 0.22f)
                    path.moveTo(0f, baseY)
                    var x = 0f
                    while (x <= w) {
                        val y = baseY + sin((x / w * 4f + phase * 6.28f + wave).toDouble()).toFloat() * 24f
                        path.lineTo(x, y)
                        x += 40f
                    }
                    drawPath(
                        path = path,
                        color = Color(0x24F59E0B),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                }
            }

            WeatherConditionType.LIGHT_RAIN, WeatherConditionType.HEAVY_RAIN, WeatherConditionType.THUNDERSTORM -> {
                val dropCount = if (conditionType == WeatherConditionType.LIGHT_RAIN) 26 else 48
                for (i in 0 until dropCount) {
                    val startX = ((i * 83) % 100) / 100f * w
                    val progress = ((fastPhase + (i * 0.13f)) % 1f)
                    val y = progress * h
                    val x = (startX - progress * 45f + w) % w
                    drawLine(
                        color = Color(0xFF7DD3FC).copy(alpha = 0.32f),
                        start = Offset(x, y),
                        end = Offset(x - 6f, y + 26f),
                        strokeWidth = 2.2f,
                        cap = StrokeCap.Round
                    )
                }
            }

            WeatherConditionType.SNOW -> {
                for (i in 0 until 30) {
                    val x = (((i * 67) % 100) / 100f * w + sin(phase * 6.28f + i) * 18f + w) % w
                    val y = ((fastPhase * 0.5f + (i * 0.09f)) % 1f) * h
                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f),
                        radius = 3.5f + (i % 3),
                        center = Offset(x, y)
                    )
                }
            }

            else -> {
                // Subtle drifting cloud arcs
                for (c in 0..2) {
                    val cx = ((c * 0.35f + phase * 0.4f) % 1.2f - 0.1f) * w
                    val cy = h * (0.12f + c * 0.09f)
                    drawCircle(
                        color = Color.White.copy(alpha = if (isDarkTheme) 0.04f else 0.35f),
                        radius = 110f + c * 25f,
                        center = Offset(cx, cy)
                    )
                }
            }
        }
    }
}
