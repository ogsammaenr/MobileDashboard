package com.mobiledashboard.app.ui.components.widgets.media

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mobiledashboard.app.data.model.MediaData
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.components.core.M3Card
import com.mobiledashboard.app.ui.components.core.M3WavyBar
import com.mobiledashboard.app.ui.theme.*

/**
 * PixelPlayer Compact Glance Widget (4x1 / 4x2 style).
 */
@Composable
fun CompactMediaWidget(
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

    val fullArtUrl = if (data.artUrl.startsWith("http")) {
        data.artUrl
    } else if (data.artUrl.isNotBlank()) {
        "$serverBaseUrl${data.artUrl}"
    } else ""

    val progress = if (data.lengthSec > 0) {
        (data.positionSec.toFloat() / data.lengthSec.toFloat()).coerceIn(0f, 1f)
    } else 0f

    M3Card(
        modifier = modifier,
        shape = M3ShapeTokens.Card,
        backdropImageUrl = if (config.blurBackground) fullArtUrl else null,
        padding = PaddingValues(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Artwork + Title/Artist + Compact Play FAB
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Album Cover
                Surface(
                    modifier = Modifier.size((52 * scale).dp),
                    shape = M3ShapeTokens.SubCard,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
                ) {
                    if (fullArtUrl.isNotBlank()) {
                        AsyncImage(
                            model = fullArtUrl,
                            contentDescription = "Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Metadata
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
                }

                // Play / Pause FAB
                FilledIconButton(
                    onClick = { onMediaControl("play-pause") },
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = accent,
                        contentColor = AmoledBlack
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Wavy Seekbar
            M3WavyBar(
                progress = progress,
                isPlaying = isPlaying,
                activeColor = accent,
                inactiveColor = MaterialTheme.colorScheme.surfaceVariant,
                thumbColor = accent
            )

            // Bottom Row: Time and Skip Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${formatTime(data.positionSec)} / ${formatTime(data.lengthSec)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = { onMediaControl("previous") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Prev",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { onMediaControl("next") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    if (seconds <= 0) return "0:00"
    val mins = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(mins, secs)
}
