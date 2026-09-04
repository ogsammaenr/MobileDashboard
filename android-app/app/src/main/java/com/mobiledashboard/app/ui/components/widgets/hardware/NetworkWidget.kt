package com.mobiledashboard.app.ui.components.widgets.hardware

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledashboard.app.data.model.NetworkData
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.components.core.M3Card
import com.mobiledashboard.app.ui.components.core.M3Header
import com.mobiledashboard.app.ui.components.core.M3Sparkline
import com.mobiledashboard.app.ui.theme.*

@Composable
fun NetworkWidget(
    data: NetworkData,
    modifier: Modifier = Modifier,
    config: WidgetConfig = WidgetConfig()
) {
    val title = config.customTitle.ifBlank { "Ağ Trafiği" }
    val theme = LocalCustomTheme.current
    val accent = config.customAccentColor ?: theme.primaryAccent
    val shape = M3ShapeTokens.getShape(config.shapeStyle)

    // Maintain rolling histories for both download and upload (12 points)
    var downHistory by remember { mutableStateOf(listOf(data.downKbps.toFloat())) }
    var upHistory by remember { mutableStateOf(listOf(data.upKbps.toFloat())) }

    LaunchedEffect(data.downKbps) {
        downHistory = (downHistory + data.downKbps.toFloat()).takeLast(12)
    }
    LaunchedEffect(data.upKbps) {
        upHistory = (upHistory + data.upKbps.toFloat()).takeLast(12)
    }

    val isDownActive = data.downKbps > 5.0
    val isUpActive = data.upKbps > 5.0

    // Pulsing LED animation when data is flowing
    val infiniteTransition = rememberInfiniteTransition(label = "net_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "led_pulse"
    )

    fun formatSpeed(kbps: Double): Pair<String, String> {
        return when {
            kbps >= 1024 * 1024 -> "%.1f".format(kbps / (1024 * 1024)) to "GB/s"
            kbps >= 1024 -> "%.1f".format(kbps / 1024.0) to "MB/s"
            else -> "%.0f".format(kbps) to "KB/s"
        }
    }

    val (downVal, downUnit) = formatSpeed(data.downKbps)
    val (upVal, upUnit) = formatSpeed(data.upKbps)

    val maxDownVal = (downHistory.maxOrNull() ?: 100f).coerceAtLeast(10f)
    val maxUpVal = (upHistory.maxOrNull() ?: 100f).coerceAtLeast(10f)

    M3Card(
        modifier = modifier,
        shape = shape
    ) {
        // 1. Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = M3ShapeTokens.Chip,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.SwapVert,
                            contentDescription = title,
                            tint = accent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )
            }

            Surface(
                shape = M3ShapeTokens.Pill,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDownActive || isUpActive) AccentGreen.copy(alpha = pulseAlpha) else AccentGreen.copy(alpha = 0.4f)
                            )
                    )
                    Text(
                        text = "CANLI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        fontSize = 9.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 2. Dual Download & Upload Pill Rows
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Download Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                shape = M3ShapeTokens.Pill,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(
                    1.dp,
                    if (isDownActive) accent.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Micro Sparkline
                    M3Sparkline(
                        points = downHistory,
                        lineColor = accent.copy(alpha = 0.6f),
                        fillGradient = listOf(accent.copy(alpha = 0.20f), Color.Transparent),
                        strokeWidth = 1.4f,
                        showGlowDot = isDownActive,
                        minValue = 0f,
                        maxValue = maxDownVal,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp, vertical = 3.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = M3ShapeTokens.Chip,
                                color = accent.copy(alpha = if (isDownActive) 0.22f else 0.12f),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowDownward,
                                        contentDescription = "Download",
                                        tint = if (isDownActive) accent.copy(alpha = pulseAlpha) else accent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Text(
                                text = "İndirme",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = downVal,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = accent
                            )
                            Text(
                                text = " $downUnit",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accent.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Upload Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                shape = M3ShapeTokens.Pill,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(
                    1.dp,
                    if (isUpActive) AccentPurple.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Micro Sparkline
                    M3Sparkline(
                        points = upHistory,
                        lineColor = AccentPurple.copy(alpha = 0.6f),
                        fillGradient = listOf(AccentPurple.copy(alpha = 0.20f), Color.Transparent),
                        strokeWidth = 1.4f,
                        showGlowDot = isUpActive,
                        minValue = 0f,
                        maxValue = maxUpVal,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp, vertical = 3.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = M3ShapeTokens.Chip,
                                color = AccentPurple.copy(alpha = if (isUpActive) 0.22f else 0.12f),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowUpward,
                                        contentDescription = "Upload",
                                        tint = if (isUpActive) AccentPurple.copy(alpha = pulseAlpha) else AccentPurple,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Yükleme",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = upVal,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = AccentPurple
                            )
                            Text(
                                text = " $upUnit",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentPurple.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
