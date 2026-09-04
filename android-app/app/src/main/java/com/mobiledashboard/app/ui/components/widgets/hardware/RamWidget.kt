package com.mobiledashboard.app.ui.components.widgets.hardware

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storage
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
import com.mobiledashboard.app.data.model.RAMData
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.components.core.BarSegment
import com.mobiledashboard.app.ui.components.core.M3Card
import com.mobiledashboard.app.ui.components.core.M3SegmentedBar
import com.mobiledashboard.app.ui.components.core.M3Sparkline
import com.mobiledashboard.app.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun RamWidget(
    data: RAMData,
    modifier: Modifier = Modifier,
    config: WidgetConfig = WidgetConfig()
) {
    val title = config.customTitle.ifBlank { "Bellek (RAM)" }
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

    val isHighPressure = data.percent > 85.0
    val glowColor = if (isHighPressure) AccentYellow else null

    val pressureStatus = when {
        data.percent > 88 -> "KRİTİK"
        data.percent > 75 -> "YÜKSEK"
        data.percent > 40 -> "DENGELİ"
        else -> "DÜŞÜK"
    }

    val pressureColor = when {
        data.percent > 88 -> M3DarkError
        data.percent > 75 -> AccentYellow
        data.percent > 40 -> accent
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val usedFraction = (data.percent / 100.0).toFloat().coerceIn(0f, 1f)
    val freeFraction = (1f - usedFraction).coerceIn(0f, 1f)

    M3Card(
        modifier = modifier,
        shape = shape,
        glowColor = glowColor
    ) {
        // 1. Top Header Row: Icon, Title & Used/Total GB Pill
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
                            imageVector = Icons.Rounded.Storage,
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
                Text(
                    text = "%.1f / %.0f GB".format(data.usedGb, data.totalGb),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // 2. Middle Metric Row with Live Sparkline & Pressure Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 2.dp)
        ) {
            // Live Sparkline Waveform in background
            M3Sparkline(
                points = history,
                lineColor = if (isHighPressure) AccentYellow else accent,
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
                        color = accent,
                        modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                    )
                }

                if (config.showBadge) {
                    Surface(
                        shape = M3ShapeTokens.Pill,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, pressureColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "$pressureStatus • %.1f GB Boş".format(data.freeGb),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = pressureColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // 3. Bottom Multi-Segment Memory Capsule Bar
        if (config.showBar) {
            M3SegmentedBar(
                segments = listOf(
                    BarSegment(fraction = usedFraction, color = if (isHighPressure) AccentYellow else accent, label = "used"),
                    BarSegment(fraction = freeFraction, color = MaterialTheme.colorScheme.surfaceVariant, label = "free")
                ),
                height = 7.dp
            )
        }
    }
}
