package com.mobiledashboard.app.ui.components.widgets.hardware

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeMute
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledashboard.app.data.model.AudioData
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.components.core.M3Card
import com.mobiledashboard.app.ui.components.core.M3Header
import com.mobiledashboard.app.ui.theme.*
import kotlin.math.roundToInt

/**
 * Material 3 PC Volume Control & Sound Manager Widget.
 * Allows adjusting PC system volume via interactive M3 slider, quick presets, and step buttons.
 */
@Composable
fun VolumeWidget(
    data: AudioData,
    onSystemControl: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
    config: WidgetConfig = WidgetConfig()
) {
    val theme = LocalCustomTheme.current
    val accent = config.customAccentColor ?: theme.primaryAccent
    val scale = config.scaleMultiplier
    val shape = M3ShapeTokens.getShape(config.shapeStyle)
    val title = config.customTitle.ifBlank { "PC Ses Seviyesi" }

    // Dynamic Speaker Icon
    val volumeIcon: ImageVector = when {
        data.isMuted -> Icons.AutoMirrored.Rounded.VolumeOff
        data.volumePercent <= 0 -> Icons.AutoMirrored.Rounded.VolumeMute
        data.volumePercent < 50 -> Icons.AutoMirrored.Rounded.VolumeDown
        else -> Icons.AutoMirrored.Rounded.VolumeUp
    }

    val badgeColor = if (data.isMuted) M3DarkError else accent
    val badgeText = if (data.isMuted) "SESSİZ" else "${data.volumePercent}%"

    // Slider local drag state for smooth dragging
    var sliderValue by remember(data.volumePercent) { mutableFloatStateOf(data.volumePercent / 100f) }

    M3Card(modifier = modifier, shape = shape) {
        // 1. Header
        M3Header(
            iconVector = volumeIcon,
            title = title,
            badgeText = badgeText,
            badgeColor = badgeColor,
            iconTint = if (data.isMuted) M3DarkError else accent
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Interactive M3 Volume Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.VolumeMute,
                contentDescription = "Min Volume",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )

            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    sliderValue = newValue
                    onSystemControl("set-volume", (newValue * 100).roundToInt())
                },
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = if (data.isMuted) M3DarkError else accent,
                    activeTrackColor = if (data.isMuted) M3DarkError else accent,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                contentDescription = "Max Volume",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 3. Quick Volume Preset & Step Action Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute / Unmute Toggle Button
            VolumePresetChip(
                label = if (data.isMuted) "Aç" else "Mute",
                isSelected = data.isMuted,
                accentColor = if (data.isMuted) M3DarkError else accent,
                onClick = { onSystemControl("vol-mute", 0) },
                modifier = Modifier.weight(1.1f),
                scale = scale
            )

            // Step Down (-5%)
            VolumeActionIconChip(
                icon = Icons.Rounded.Remove,
                onClick = { onSystemControl("vol-down", 0) },
                modifier = Modifier.weight(0.9f)
            )

            // Step Up (+5%)
            VolumeActionIconChip(
                icon = Icons.Rounded.Add,
                onClick = { onSystemControl("vol-up", 0) },
                modifier = Modifier.weight(0.9f)
            )

            // Preset 25%
            VolumePresetChip(
                label = "25%",
                isSelected = !data.isMuted && data.volumePercent in 20..30,
                accentColor = accent,
                onClick = { onSystemControl("set-volume", 25) },
                modifier = Modifier.weight(1f),
                scale = scale
            )

            // Preset 50%
            VolumePresetChip(
                label = "50%",
                isSelected = !data.isMuted && data.volumePercent in 45..55,
                accentColor = accent,
                onClick = { onSystemControl("set-volume", 50) },
                modifier = Modifier.weight(1f),
                scale = scale
            )

            // Preset 75%
            VolumePresetChip(
                label = "75%",
                isSelected = !data.isMuted && data.volumePercent in 70..80,
                accentColor = accent,
                onClick = { onSystemControl("set-volume", 75) },
                modifier = Modifier.weight(1f),
                scale = scale
            )

            // Preset 100%
            VolumePresetChip(
                label = "100%",
                isSelected = !data.isMuted && data.volumePercent >= 95,
                accentColor = accent,
                onClick = { onSystemControl("set-volume", 100) },
                modifier = Modifier.weight(1f),
                scale = scale
            )
        }
    }
}

@Composable
private fun VolumePresetChip(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    scale: Float = 1.0f
) {
    Surface(
        modifier = modifier
            .height(30.dp)
            .clip(M3ShapeTokens.Pill)
            .clickable { onClick() },
        shape = M3ShapeTokens.Pill,
        color = if (isSelected) accentColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = (11 * scale).sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VolumeActionIconChip(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(30.dp)
            .clip(M3ShapeTokens.Pill)
            .clickable { onClick() },
        shape = M3ShapeTokens.Pill,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
