package com.mobiledashboard.app.ui.components.widgets.hardware

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledashboard.app.data.model.CPUData
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.components.core.M3Card
import com.mobiledashboard.app.ui.components.core.M3ProgressBar
import com.mobiledashboard.app.ui.components.core.M3Sparkline
import com.mobiledashboard.app.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun CpuWidget(
    data: CPUData,
    modifier: Modifier = Modifier,
    config: WidgetConfig = WidgetConfig()
) {
    val title = config.customTitle.ifBlank { "İşlemci (CPU)" }
    val theme = LocalCustomTheme.current
    val accent = config.customAccentColor ?: theme.primaryAccent
    val scale = config.scaleMultiplier
    val shape = M3ShapeTokens.getShape(config.shapeStyle)

    // Maintain a rolling history for the live Sparkline area chart (14 points)
    var history by remember { mutableStateOf(listOf(data.percent.toFloat())) }
    LaunchedEffect(data.percent) {
        val next = (history + data.percent.toFloat()).takeLast(14)
        history = next
    }

    val isOverheated = data.temp > 80.0
    val isHeavyLoad = data.percent > 85.0
    val glowColor = when {
        isOverheated -> M3DarkError
        isHeavyLoad -> AccentYellow
        else -> null
    }

    val statusText = when {
        data.percent > 85 -> "AŞIRI YÜK"
        data.percent > 65 -> "TURBO"
        data.percent > 25 -> "OPTİMAL"
        else -> "BOŞTA"
    }

    val statusColor = when {
        data.percent > 85 -> M3DarkError
        data.percent > 65 -> AccentYellow
        data.percent > 25 -> accent
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    M3Card(
        modifier = modifier,
        shape = shape,
        glowColor = glowColor
    ) {
        // 1. Top Header Row: Icon, Title & Temperature Pill
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
                            imageVector = Icons.Rounded.Memory,
                            contentDescription = title,
                            tint = if (isOverheated) M3DarkError else accent,
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

            if (config.showTemp && data.temp > 0) {
                Surface(
                    shape = M3ShapeTokens.Pill,
                    color = if (isOverheated) M3DarkError.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (isOverheated) M3DarkError.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isOverheated) Icons.Rounded.Bolt else Icons.Rounded.Thermostat,
                            contentDescription = "Temp",
                            tint = if (isOverheated) M3DarkError else if (data.temp > 65) AccentYellow else AccentGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${data.temp.roundToInt()}°C",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverheated) M3DarkError else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 2. Middle Metric Row with Background Live Sparkline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 2.dp)
        ) {
            // Live Sparkline Waveform in background
            M3Sparkline(
                points = history,
                lineColor = if (isOverheated) M3DarkError else accent,
                strokeWidth = 2.0f,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 4.dp)
            )

            // Foreground Metric Values
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${data.percent.roundToInt()}",
                        fontSize = (34 * scale).sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "%",
                        fontSize = (18 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverheated) M3DarkError else accent,
                        modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                    )
                }

                if (config.showBadge) {
                    Surface(
                        shape = M3ShapeTokens.Pill,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // 3. Bottom Progress Bar
        if (config.showBar) {
            M3ProgressBar(
                progress = (data.percent / 100.0).toFloat(),
                accentColor = accent,
                height = 7
            )
        }
    }
}
