package com.mobiledashboard.app.ui.components.widgets.hardware

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledashboard.app.data.model.TelemetryPayload
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.components.core.M3Card
import com.mobiledashboard.app.ui.components.core.M3Gauge
import com.mobiledashboard.app.ui.components.core.M3Header
import com.mobiledashboard.app.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun SystemHubWidget(
    telemetry: TelemetryPayload,
    modifier: Modifier = Modifier,
    config: WidgetConfig = WidgetConfig()
) {
    val scale = config.scaleMultiplier
    val title = config.customTitle.ifBlank { "Sistem Durumu (M3 Hub)" }

    if (config.shapeStyle == "segmented_pills" || config.shapeStyle == "pills") {
        // Segmented M3 Individual Stadium Pills Row
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            M3StandalonePillGauge(
                label = "CPU",
                valueStr = "${telemetry.cpu.percent.roundToInt()}%",
                progress = (telemetry.cpu.percent / 100f).toFloat(),
                accent = MaterialTheme.colorScheme.primary,
                icon = Icons.Rounded.Memory,
                modifier = Modifier.weight(1f),
                scale = scale
            )
            M3StandalonePillGauge(
                label = "GPU",
                valueStr = "${telemetry.gpu.percent.roundToInt()}%",
                progress = (telemetry.gpu.percent / 100f).toFloat(),
                accent = MaterialTheme.colorScheme.secondary,
                icon = Icons.Rounded.SportsEsports,
                modifier = Modifier.weight(1f),
                scale = scale
            )
            M3StandalonePillGauge(
                label = "RAM",
                valueStr = "${telemetry.ram.percent.roundToInt()}%",
                progress = (telemetry.ram.percent / 100f).toFloat(),
                accent = MaterialTheme.colorScheme.tertiary,
                icon = Icons.Rounded.Storage,
                modifier = Modifier.weight(1f),
                scale = scale
            )
            M3StandalonePillGauge(
                label = "SSD",
                valueStr = "${telemetry.disk.percent.roundToInt()}%",
                progress = (telemetry.disk.percent / 100f).toFloat(),
                accent = AccentYellow,
                icon = Icons.Rounded.Save,
                modifier = Modifier.weight(1f),
                scale = scale
            )
        }
    } else {
        // Standard M3 Card Container with Customizable Shape
        val cardShape = M3ShapeTokens.getShape(config.shapeStyle, M3ShapeTokens.Card)
        M3Card(modifier = modifier, shape = cardShape) {
            M3Header(
                iconVector = Icons.Rounded.Dashboard,
                title = title,
                badgeText = "NORMAL",
                badgeColor = AccentGreen,
                iconTint = AccentGreen
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                M3MiniGaugeTile("CPU", (telemetry.cpu.percent / 100f).toFloat(), "${telemetry.cpu.percent.roundToInt()}%", MaterialTheme.colorScheme.primary, Modifier.weight(1f), scale)
                M3MiniGaugeTile("GPU", (telemetry.gpu.percent / 100f).toFloat(), "${telemetry.gpu.percent.roundToInt()}%", MaterialTheme.colorScheme.secondary, Modifier.weight(1f), scale)
                M3MiniGaugeTile("RAM", (telemetry.ram.percent / 100f).toFloat(), "${telemetry.ram.percent.roundToInt()}%", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f), scale)
                M3MiniGaugeTile("SSD", (telemetry.disk.percent / 100f).toFloat(), "${telemetry.disk.percent.roundToInt()}%", AccentYellow, Modifier.weight(1f), scale)
            }
        }
    }
}

/**
 * Standalone M3 Pill Capsule for Segmented Hub mode.
 */
@Composable
fun M3StandalonePillGauge(
    label: String,
    valueStr: String,
    progress: Float,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    scale: Float = 1.0f
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = M3ShapeTokens.Pill,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Mini Icon Chip
            Surface(
                modifier = Modifier.size(24.dp),
                shape = M3ShapeTokens.Circle,
                color = accent.copy(alpha = 0.16f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = accent,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Center Gauge
            Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                M3Gauge(
                    progress = progress,
                    accentColor = accent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 3.5f
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Bottom Value
            Text(
                text = valueStr,
                fontSize = (12 * scale).sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun M3MiniGaugeTile(
    label: String,
    progress: Float,
    valueStr: String,
    accent: Color,
    modifier: Modifier = Modifier,
    scale: Float = 1.0f
) {
    Surface(
        modifier = modifier,
        shape = M3ShapeTokens.SubCard,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                M3Gauge(
                    progress = progress,
                    accentColor = accent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    strokeWidth = 3.5f
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = valueStr,
                fontSize = (12 * scale).sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
