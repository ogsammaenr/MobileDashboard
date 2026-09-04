package com.mobiledashboard.app.ui.components.core

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.mobiledashboard.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 Expressive Circular Arc Gauge Component with Tick Marks and Glowing Cap.
 */
@Composable
fun M3Gauge(
    progress: Float,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Float = 5.5f,
    showTicks: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "gauge_progress"
    )

    val gaugeColor = when {
        progress > 0.88f -> M3DarkError
        progress > 0.72f -> AccentYellow
        else -> accentColor
    }

    val tickColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.dp.toPx()
            val padding = strokePx / 2 + 4.dp.toPx()
            val diameter = size.minDimension - (padding * 2)
            val center = Offset(size.width / 2, size.height / 2)
            val topLeft = Offset(center.x - diameter / 2, center.y - diameter / 2)
            val radius = diameter / 2

            val startAngle = 135f
            val sweepAngle = 270f

            // 1. Subtle Tick Dots around gauge perimeter
            if (showTicks) {
                val tickCount = 5 // 0%, 25%, 50%, 75%, 100%
                for (i in 0 until tickCount) {
                    val angleDeg = startAngle + (sweepAngle * (i.toFloat() / (tickCount - 1)))
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val tickRadius = radius + (strokePx / 2) + 3.5.dp.toPx()
                    val tickX = center.x + (tickRadius * cos(angleRad)).toFloat()
                    val tickY = center.y + (tickRadius * sin(angleRad)).toFloat()

                    drawCircle(
                        color = tickColor,
                        radius = 1.2.dp.toPx(),
                        center = Offset(tickX, tickY)
                    )
                }
            }

            // 2. Background Inactive Track
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // 3. Active Progress Arc
            if (animatedProgress > 0.005f) {
                drawArc(
                    color = gaugeColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // Glowing Head Dot at current tip
                val currentAngleDeg = startAngle + (sweepAngle * animatedProgress)
                val currentAngleRad = Math.toRadians(currentAngleDeg.toDouble())
                val tipX = center.x + (radius * cos(currentAngleRad)).toFloat()
                val tipY = center.y + (radius * sin(currentAngleRad)).toFloat()

                // Glow halo
                drawCircle(
                    color = gaugeColor.copy(alpha = 0.4f),
                    radius = (strokePx * 0.85f),
                    center = Offset(tipX, tipY)
                )
                // White highlight center
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = (strokePx * 0.35f),
                    center = Offset(tipX, tipY)
                )
            }
        }
    }
}
