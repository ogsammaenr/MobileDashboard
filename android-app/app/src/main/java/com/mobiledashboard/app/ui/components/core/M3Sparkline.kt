package com.mobiledashboard.app.ui.components.core

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Material 3 Live Sparkline / Area Chart Component.
 * Renders a smooth cubic Bézier curve with vertical gradient area fill and glowing tip.
 */
@Composable
fun M3Sparkline(
    points: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillGradient: List<Color> = listOf(
        lineColor.copy(alpha = 0.28f),
        lineColor.copy(alpha = 0.05f),
        Color.Transparent
    ),
    strokeWidth: Float = 2.2f,
    showGlowDot: Boolean = true,
    minValue: Float = 0f,
    maxValue: Float = 100f
) {
    if (points.isEmpty()) return

    // Animate the last point value for super-smooth micro-interpolation
    val lastPoint = points.last()
    val animatedLastPoint by animateFloatAsState(
        targetValue = lastPoint,
        animationSpec = tween(durationMillis = 350),
        label = "sparkline_last_point"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0 || h <= 0) return@Canvas

        val effectivePoints = if (points.size == 1) {
            listOf(points[0], animatedLastPoint)
        } else {
            points.dropLast(1) + animatedLastPoint
        }

        val range = (maxValue - minValue).coerceAtLeast(1f)
        val stepX = w / (effectivePoints.size - 1).coerceAtLeast(1)

        // Convert points to (X, Y) Canvas coordinates
        val coords = effectivePoints.mapIndexed { index, value ->
            val normalizedY = ((value - minValue) / range).coerceIn(0f, 1f)
            val pxX = index * stepX
            val pxY = h - (normalizedY * (h - 8.dp.toPx())) - 4.dp.toPx()
            Offset(pxX, pxY)
        }

        if (coords.isEmpty()) return@Canvas

        // 1. Build Smooth Cubic Bézier Path
        val strokePath = Path().apply {
            moveTo(coords.first().x, coords.first().y)
            for (i in 0 until coords.size - 1) {
                val current = coords[i]
                val next = coords[i + 1]
                val controlX1 = current.x + (next.x - current.x) / 2f
                val controlY1 = current.y
                val controlX2 = current.x + (next.x - current.x) / 2f
                val controlY2 = next.y
                cubicTo(controlX1, controlY1, controlX2, controlY2, next.x, next.y)
            }
        }

        // 2. Build Gradient Area Fill Path
        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(coords.last().x, h)
            lineTo(coords.first().x, h)
            close()
        }

        // Draw Vertical Area Gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = fillGradient,
                startY = 0f,
                endY = h
            )
        )

        // Draw Stroke Line
        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(
                width = strokeWidth.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 3. Draw Glow Dot at current / last point
        if (showGlowDot && coords.isNotEmpty()) {
            val tip = coords.last()
            // Outer soft glow halo
            drawCircle(
                color = lineColor.copy(alpha = 0.35f),
                radius = 6.dp.toPx(),
                center = tip
            )
            // Inner solid dot
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = tip
            )
            // Center white highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = 1.2.dp.toPx(),
                center = tip
            )
        }
    }
}
