package com.mobiledashboard.app.ui.components.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
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
import com.mobiledashboard.app.data.model.ConnectionStatus
import com.mobiledashboard.app.data.model.DashboardTheme
import com.mobiledashboard.app.data.model.ScreenOrientation
import com.mobiledashboard.app.ui.theme.*

/**
 * Material 3 Settings & Quick Controls Popup.
 * Triggered via 3-finger tap on the screen.
 */
@Composable
fun TopControlBar(
    isOpen: Boolean,
    onClose: () -> Unit,
    connectionStatus: ConnectionStatus,
    serverHost: String,
    currentTheme: DashboardTheme,
    brightness: Float,
    orientation: ScreenOrientation,
    onBrightnessChange: (Float) -> Unit,
    onOrientationChange: (ScreenOrientation) -> Unit,
    onThemeChange: (DashboardTheme) -> Unit,
    onSystemControl: (String) -> Unit,
    onReconnect: () -> Unit,
    onChangeServer: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(animationSpec = tween(180)),
        exit = fadeOut(animationSpec = tween(180)),
        modifier = modifier.fillMaxSize()
    ) {
        // Scrim backdrop (dim background, tap to dismiss)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.60f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClose() },
            contentAlignment = Alignment.Center
        ) {
            val scrollState = rememberScrollState()

            // Popup Dialog / Window
            Surface(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* prevent click through to scrim */ },
                shape = M3ShapeTokens.Card,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Header: Status, Host & Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (connectionStatus) {
                                            ConnectionStatus.CONNECTED -> AccentGreen
                                            ConnectionStatus.CONNECTING -> AccentYellow
                                            ConnectionStatus.DISCONNECTED -> M3DarkError
                                        }
                                    )
                            )
                            Text(
                                text = if (connectionStatus == ConnectionStatus.CONNECTED) serverHost else "Bağlantı Yok",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(onClick = onReconnect, modifier = Modifier.size(34.dp)) {
                                Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "Yenile", tint = MaterialTheme.colorScheme.onSurface)
                            }
                            IconButton(onClick = onChangeServer, modifier = Modifier.size(34.dp)) {
                                Icon(imageVector = Icons.Rounded.Dns, contentDescription = "Sunucu Seç", tint = MaterialTheme.colorScheme.onSurface)
                            }
                            IconButton(onClick = onClose, modifier = Modifier.size(34.dp)) {
                                Icon(imageVector = Icons.Rounded.Close, contentDescription = "Kapat", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    // 2. Brightness Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.BrightnessMedium,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    "Ekran Parlaklığı",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${(brightness * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Slider(
                            value = brightness,
                            onValueChange = onBrightnessChange,
                            valueRange = 0.05f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    // 3. Screen Orientation Controls
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ScreenRotation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    "Ekran Yönü",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = orientation.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OrientationOptionChip(
                                icon = Icons.Rounded.StayCurrentPortrait,
                                label = "Dikey",
                                isSelected = orientation == ScreenOrientation.PORTRAIT,
                                onClick = { onOrientationChange(ScreenOrientation.PORTRAIT) },
                                modifier = Modifier.weight(1f)
                            )
                            OrientationOptionChip(
                                icon = Icons.Rounded.StayCurrentLandscape,
                                label = "Yatay",
                                isSelected = orientation == ScreenOrientation.LANDSCAPE,
                                onClick = { onOrientationChange(ScreenOrientation.LANDSCAPE) },
                                modifier = Modifier.weight(1f)
                            )
                            OrientationOptionChip(
                                icon = Icons.Rounded.ScreenRotation,
                                label = "Otomatik",
                                isSelected = orientation == ScreenOrientation.AUTO,
                                onClick = { onOrientationChange(ScreenOrientation.AUTO) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 4. PC Remote Actions
                    Text(
                        "💻 Masaüstü Eylemleri",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HudActionIconChip(Icons.AutoMirrored.Rounded.VolumeDown, "Ses -", { onSystemControl("vol-down") }, Modifier.weight(1f))
                        HudActionIconChip(Icons.AutoMirrored.Rounded.VolumeUp, "Ses +", { onSystemControl("vol-up") }, Modifier.weight(1f))
                        HudActionIconChip(Icons.Rounded.VolumeOff, "Mute", { onSystemControl("vol-mute") }, Modifier.weight(1f))
                        HudActionIconChip(Icons.Rounded.Lock, "Kilit", { onSystemControl("lock") }, Modifier.weight(1.2f))
                        HudActionIconChip(Icons.Rounded.Bedtime, "Uyku", { onSystemControl("sleep") }, Modifier.weight(1.2f))
                    }

                    // 5. Theme Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "🎨 Renk Teması",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DashboardTheme.entries.forEach { t ->
                                val isSelected = currentTheme == t
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(t.primaryHex))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.25f),
                                            shape = CircleShape
                                        )
                                        .clickable { onThemeChange(t) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(t.bgHex))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrientationOptionChip(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(38.dp)
            .clip(M3ShapeTokens.Pill)
            .clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        shape = M3ShapeTokens.Pill
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HudActionIconChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(38.dp)
            .clip(M3ShapeTokens.Pill)
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = M3ShapeTokens.Pill
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
