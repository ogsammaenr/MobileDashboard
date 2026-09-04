package com.mobiledashboard.app.ui.components.widgets.media

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mobiledashboard.app.data.model.MediaData
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.components.core.M3Card
import com.mobiledashboard.app.ui.theme.*

@Composable
fun VinylMediaWidget(
    data: MediaData,
    serverBaseUrl: String,
    onMediaControl: (String) -> Unit,
    modifier: Modifier = Modifier,
    config: WidgetConfig = WidgetConfig()
) {
    val isPlaying = data.status.equals("Playing", ignoreCase = true)
    val scale = config.scaleMultiplier
    val theme = LocalCustomTheme.current
    val accent = config.customAccentColor ?: theme.primaryAccent

    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinylRotation"
    )

    val fullArtUrl = if (data.artUrl.startsWith("http")) {
        data.artUrl
    } else if (data.artUrl.isNotBlank()) {
        "$serverBaseUrl${data.artUrl}"
    } else ""

    M3Card(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Animated Vinyl Disc
            Box(
                modifier = Modifier
                    .size((64 * scale).dp)
                    .rotate(if (isPlaying) rotation else 0f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF2B2C36), Color(0xFF0F1015))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (fullArtUrl.isNotBlank()) {
                    AsyncImage(
                        model = fullArtUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size((26 * scale).dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size((16 * scale).dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.title.ifBlank { "Müzik Yok" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = data.artist.ifBlank { "--" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onMediaControl("previous") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.SkipPrevious, contentDescription = "Prev", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    FilledIconButton(
                        onClick = { onMediaControl("play-pause") },
                        modifier = Modifier.size(38.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = accent,
                            contentColor = AmoledBlack
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Pause"
                        )
                    }
                    IconButton(
                        onClick = { onMediaControl("next") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.SkipNext, contentDescription = "Next", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
