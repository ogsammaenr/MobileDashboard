package com.mobiledashboard.app.ui.components.core

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobiledashboard.app.ui.theme.*
import kotlin.math.PI
import kotlin.math.sin

/**
 * Material 3 / PixelPlayer Sinusoidal Wavy Seekbar Component.
 */
@Composable
fun M3WavyBar(
    progress: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = M3SurfaceContainerHighest,
    thumbColor: Color = activeColor,
    height: Dp = 22.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "m3_wavy_seek")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) (2f * PI).toFloat() else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val targetAmplitude = if (isPlaying) 3.5f else 0f
    val animatedAmplitude by animateFloatAsState(
        targetValue = targetAmplitude,
        animationSpec = tween(400),
        label = "amplitude"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val width = size.width
        val barHeight = size.height
        val centerY = barHeight / 2f
        val strokeWidth = 3.5.dp.toPx()
        val thumbRadius = 5.5.dp.toPx()

        val progressX = (width * progress.coerceIn(0f, 1f)).coerceIn(0f, width)

        // Inactive Track (Straight clean line)
        if (progressX < width) {
            drawLine(
                color = inactiveColor,
                start = Offset(progressX, centerY),
                end = Offset(width, centerY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        // Active Track (Android 13+ Wavy sinusoidal line)
        if (progressX > 0f) {
            val wavePath = Path()
            val waveLength = 22.dp.toPx()
            val amp = animatedAmplitude.dp.toPx()

            wavePath.moveTo(0f, centerY)
            var x = 0f
            val step = 1.5.dp.toPx()
            while (x <= progressX) {
                val y = centerY + amp * sin(((x / waveLength) * (2f * PI).toFloat()) - phase)
                wavePath.lineTo(x, y)
                x += step
            }

            drawPath(
                path = wavePath,
                color = activeColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Dynamic Thumb at exact wave tip
            val thumbY = centerY + amp * sin(((progressX / waveLength) * (2f * PI).toFloat()) - phase)
            drawCircle(
                color = thumbColor,
                radius = thumbRadius,
                center = Offset(progressX, thumbY)
            )
        }
    }
}
