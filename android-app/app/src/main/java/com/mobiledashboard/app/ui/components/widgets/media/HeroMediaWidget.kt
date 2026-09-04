package com.mobiledashboard.app.ui.components.widgets.media

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mobiledashboard.app.data.model.MediaData
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.components.core.M3WavyBar
import com.mobiledashboard.app.ui.theme.*
import kotlin.math.min

/**
 * PixelPlayer Material 3 Fullscreen Media Widget.
 * Rich and diverse M3 Expressive layout combining 8-lobe scalloped rotating artwork,
 * 4-leaf clover equalizer hub, asymmetric track info card, and split hero control pods over a transparent canvas.
 */
@Composable
fun HeroMediaWidget(
    data: MediaData,
    serverBaseUrl: String,
    onMediaControl: (String) -> Unit,
    modifier: Modifier = Modifier,
    config: WidgetConfig = WidgetConfig()
) {
    val isPlaying = data.status.equals("Playing", ignoreCase = true)
    val isPaused = data.status.equals("Paused", ignoreCase = true)
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

    // 8-Lobe Clover Continuous Rotation Animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "MediaAnim")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DiscRotation"
    )

    // Pulsing aura animation
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraPulse"
    )

    // 100% Transparent Root Canvas
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(4.dp)
    ) {
        val containerW = maxWidth
        val containerH = maxHeight
        val isLandscape = containerW > containerH * 1.15f

        if (isLandscape) {
            // ==============================================================
            // LANDSCAPE: 2-COLUMN DIVERSE MATERIAL 3 FLOATING ISLANDS
            // ==============================================================
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT: 8-Lobe Scalloped Rotating Artwork Island
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Animated8LobeArtwork(
                        artUrl = fullArtUrl,
                        isPlaying = isPlaying,
                        rotationAngle = rotationAngle,
                        auraScale = auraScale,
                        accent = accent,
                        sizeDp = (min(containerH.value, containerW.value * 0.45f) * 0.90f * scale).dp
                    )
                }

                // RIGHT: Multi-Shape M3 Expressive Control Column
                Column(
                    modifier = Modifier
                        .weight(1.35f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Top Row: 4-Leaf Clover EQ Visualizer + Audio Stream Chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CloverLiveVisualizerHub(
                            isPlaying = isPlaying,
                            isPaused = isPaused,
                            accent = accent
                        )
                        AudioFormatBadge(accent = accent)
                    }

                    // 2. Asymmetric Track Info Card
                    AsymmetricTrackInfoCard(
                        title = data.title.ifBlank { "Müzik Çalınmıyor" },
                        artist = data.artist.ifBlank { "Desktop Audio Stream" },
                        album = data.album,
                        accent = accent,
                        scale = scale
                    )

                    // 3. Wavy Seekbar Container
                    WavySeekbarIsland(
                        progress = progress,
                        isPlaying = isPlaying,
                        positionSec = data.positionSec,
                        lengthSec = data.lengthSec,
                        accent = accent
                    )

                    // 4. Split Hero Playback Controls (Pills + Center Hero Circle)
                    SplitPlaybackControls(
                        isPlaying = isPlaying,
                        onMediaControl = onMediaControl,
                        accent = accent
                    )
                }
            }
        } else {
            // ==============================================================
            // PORTRAIT: VERTICAL DIVERSE MATERIAL 3 FLOATING ISLANDS
            // ==============================================================
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Top Row: 4-Leaf Clover Visualizer & Stream Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CloverLiveVisualizerHub(
                        isPlaying = isPlaying,
                        isPaused = isPaused,
                        accent = accent
                    )
                    AudioFormatBadge(accent = accent)
                }

                // 2. Center: 8-Lobe Scalloped Rotating Artwork Island
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Animated8LobeArtwork(
                        artUrl = fullArtUrl,
                        isPlaying = isPlaying,
                        rotationAngle = rotationAngle,
                        auraScale = auraScale,
                        accent = accent,
                        sizeDp = (min(containerH.value * 0.40f, containerW.value * 0.65f) * scale).dp
                    )
                }

                // 3. Asymmetric Track Info Card
                AsymmetricTrackInfoCard(
                    title = data.title.ifBlank { "Müzik Çalınmıyor" },
                    artist = data.artist.ifBlank { "Desktop Audio Stream" },
                    album = data.album,
                    accent = accent,
                    scale = scale
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 4. Wavy Seekbar Container
                WavySeekbarIsland(
                    progress = progress,
                    isPlaying = isPlaying,
                    positionSec = data.positionSec,
                    lengthSec = data.lengthSec,
                    accent = accent
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 5. Split Hero Playback Controls
                SplitPlaybackControls(
                    isPlaying = isPlaying,
                    onMediaControl = onMediaControl,
                    accent = accent
                )
            }
        }
    }
}

/**
 * 8-Lobe Scalloped Clover Animated Artwork Island with living radial aura ring.
 */
@Composable
private fun Animated8LobeArtwork(
    artUrl: String,
    isPlaying: Boolean,
    rotationAngle: Float,
    auraScale: Float,
    accent: Color,
    sizeDp: androidx.compose.ui.unit.Dp
) {
    val scallopedShape = remember { M3ScallopedShape(lobes = 8) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(sizeDp)
    ) {
        // Glowing Ambient Aura Ring behind Clover
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .size(sizeDp)
                    .graphicsLayer {
                        scaleX = auraScale
                        scaleY = auraScale
                    }
                    .clip(scallopedShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.50f),
                                accent.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Main 8-Lobe Scalloped Clover Disc
        Surface(
            modifier = Modifier
                .size(sizeDp * 0.94f)
                .graphicsLayer {
                    if (isPlaying) {
                        rotationZ = rotationAngle
                    }
                }
                .shadow(elevation = 16.dp, shape = scallopedShape),
            shape = scallopedShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 2.dp,
                brush = Brush.sweepGradient(
                    listOf(
                        accent.copy(alpha = 0.90f),
                        MaterialTheme.colorScheme.surfaceVariant,
                        accent.copy(alpha = 0.50f),
                        MaterialTheme.colorScheme.surfaceVariant,
                        accent.copy(alpha = 0.90f)
                    )
                )
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (artUrl.isNotBlank()) {
                    AsyncImage(
                        model = artUrl,
                        contentDescription = "Album Artwork",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Album,
                            contentDescription = null,
                            tint = accent.copy(alpha = 0.85f),
                            modifier = Modifier.size(sizeDp * 0.35f)
                        )
                    }
                }

                // Center Vinyl Spindle Node
                Surface(
                    modifier = Modifier.size(sizeDp * 0.22f),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    border = BorderStroke(1.5.dp, accent)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(sizeDp * 0.11f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 4-Leaf Clover Shaped Live Equalizer Visualizer Hub (M3CloverShape).
 */
@Composable
private fun CloverLiveVisualizerHub(
    isPlaying: Boolean,
    isPaused: Boolean,
    accent: Color
) {
    val cloverShape = remember { M3CloverShape() }

    Surface(
        shape = cloverShape,
        color = if (isPlaying) accent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isPlaying) accent.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outlineVariant
        ),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedEqualizerBars(
                isPlaying = isPlaying,
                color = if (isPlaying) accent else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isPlaying) "OYNATILIYOR" else if (isPaused) "DURAKLATILDI" else "HAZIR",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isPlaying) accent else MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.8.sp
            )
        }
    }
}

/**
 * Animated 4-Bar Equalizer.
 */
@Composable
private fun AnimatedEqualizerBars(
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "eq_bars")
    val h1 by transition.animateFloat(
        initialValue = 0.35f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by transition.animateFloat(
        initialValue = 0.85f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(540, easing = LinearEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by transition.animateFloat(
        initialValue = 0.20f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(360, easing = LinearEasing), RepeatMode.Reverse),
        label = "h3"
    )
    val h4 by transition.animateFloat(
        initialValue = 0.65f, targetValue = 0.30f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
        label = "h4"
    )

    Row(
        modifier = modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(h1, h2, h3, h4).forEach { factor ->
            val barHeight = if (isPlaying) (14 * factor).dp else 3.5.dp
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(barHeight)
                    .clip(M3ShapeTokens.Pill)
                    .background(color)
            )
        }
    }
}

/**
 * Audio Format Stream Badge Pill.
 */
@Composable
private fun AudioFormatBadge(accent: Color) {
    Surface(
        shape = M3ShapeTokens.Pill,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Headphones,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "Hi-Res Audio",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Asymmetric Material 3 Track Info Card (topStart=30.dp, topEnd=14.dp, bottomEnd=30.dp, bottomStart=14.dp).
 */
@Composable
private fun AsymmetricTrackInfoCard(
    title: String,
    artist: String,
    album: String,
    accent: Color,
    scale: Float = 1.0f
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = M3ShapeTokens.AsymmetricCard,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                fontSize = (16 * scale).sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = artist,
                fontSize = (12.5 * scale).sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            if (album.isNotBlank() && album != "--") {
                Surface(
                    shape = M3ShapeTokens.Pill,
                    color = accent.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = album,
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Wavy Sinusoidal Seekbar Island (M3 SubCard Squircle).
 */
@Composable
private fun WavySeekbarIsland(
    progress: Float,
    isPlaying: Boolean,
    positionSec: Long,
    lengthSec: Long,
    accent: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = M3ShapeTokens.SubCard,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = formatDuration(positionSec),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            M3WavyBar(
                progress = progress,
                isPlaying = isPlaying,
                activeColor = accent,
                inactiveColor = MaterialTheme.colorScheme.surfaceVariant,
                thumbColor = accent,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = formatDuration(lengthSec),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Split Playback Controls: Left Pill Pod + Central Pulsing Hero Circle + Right Pill Pod.
 */
@Composable
private fun SplitPlaybackControls(
    isPlaying: Boolean,
    onMediaControl: (String) -> Unit,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Action Pod: Shuffle & Previous
        Surface(
            shape = M3ShapeTokens.Pill,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onMediaControl("shuffle") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { onMediaControl("previous") },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Center Hero Play/Pause Circle with Radial Glow
        Surface(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .clickable { onMediaControl("play-pause") },
            shape = CircleShape,
            color = accent,
            shadowElevation = 10.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // Right Action Pod: Next & Repeat
        Surface(
            shape = M3ShapeTokens.Pill,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onMediaControl("next") },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = { onMediaControl("loop") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Repeat,
                        contentDescription = "Repeat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    if (seconds <= 0) return "0:00"
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%d:%02d", m, s)
}
