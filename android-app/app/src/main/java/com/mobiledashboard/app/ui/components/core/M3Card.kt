package com.mobiledashboard.app.ui.components.core

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mobiledashboard.app.ui.theme.*

/**
 * Material 3 Expressive Card Container.
 * Provides hierarchical tonal background surfaces, subtle outline borders,
 * dynamic elevation, ambient glow aura, and artwork blur backdrops.
 */
@Composable
fun M3Card(
    modifier: Modifier = Modifier,
    shape: Shape = M3ShapeTokens.Card,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    borderWidth: Dp = 1.dp,
    tonalElevation: Dp = 2.dp,
    shadowElevation: Dp = 0.dp,
    padding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    glowColor: Color? = null,
    backdropImageUrl: String? = null,
    backdropBlurRadius: Dp = 42.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "m3_card_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val effectiveBorderColor = if (glowColor != null) {
        glowColor.copy(alpha = 0.45f)
    } else {
        borderColor
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .clip(shape),
        shape = shape,
        color = backgroundColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = BorderStroke(borderWidth, effectiveBorderColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Optional Ambient Pulse Glow Backdrop
            if (glowColor != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    glowColor.copy(alpha = pulseAlpha),
                                    glowColor.copy(alpha = pulseAlpha * 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Optional Ambient Blurred Artwork Backdrop
            if (!backdropImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = backdropImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(backdropBlurRadius)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xDD140C0E),
                                    Color(0xF0191113),
                                    Color(0xFA191113)
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.SpaceBetween,
                content = content
            )
        }
    }
}
