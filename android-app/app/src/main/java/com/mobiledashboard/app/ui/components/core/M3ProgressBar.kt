package com.mobiledashboard.app.ui.components.core

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mobiledashboard.app.ui.theme.*

/**
 * Material 3 Expressive Capsule Progress Bar with smooth animation and glowing leading edge.
 */
@Composable
fun M3ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    height: Int = 8,
    showGlowHead: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "m3_progress"
    )

    val barColor = when {
        progress > 0.88f -> M3DarkError
        progress > 0.72f -> AccentYellow
        else -> accentColor
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(height.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Active Fill Bar
        if (animatedProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(height.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                barColor.copy(alpha = 0.55f),
                                barColor.copy(alpha = 0.85f),
                                barColor
                            )
                        )
                    )
            ) {
                if (showGlowHead && animatedProgress > 0.05f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(height.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.65f))
                    )
                }
            }
        }
    }
}
