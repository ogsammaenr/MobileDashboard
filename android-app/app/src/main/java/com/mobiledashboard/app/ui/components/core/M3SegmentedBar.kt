package com.mobiledashboard.app.ui.components.core

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class BarSegment(
    val fraction: Float,
    val color: Color,
    val label: String = ""
)

/**
 * Material 3 Multi-Segment Capsule Bar.
 * Animates segment proportions smoothly and renders a clean segmented capsule.
 */
@Composable
fun M3SegmentedBar(
    segments: List<BarSegment>,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    spacing: Dp = 2.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            segments.forEach { segment ->
                val animatedFraction by animateFloatAsState(
                    targetValue = segment.fraction.coerceIn(0f, 1f),
                    animationSpec = tween(500),
                    label = "bar_segment_${segment.label}"
                )

                if (animatedFraction > 0.005f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(animatedFraction)
                            .clip(RoundedCornerShape(height / 2))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(segment.color.copy(alpha = 0.85f), segment.color)
                                )
                            )
                    )
                }
            }
        }
    }
}
