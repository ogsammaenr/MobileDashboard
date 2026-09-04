package com.mobiledashboard.app.ui.components.widgets.hardware

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledashboard.app.data.model.DiskData
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.components.core.BarSegment
import com.mobiledashboard.app.ui.components.core.M3Card
import com.mobiledashboard.app.ui.components.core.M3SegmentedBar
import com.mobiledashboard.app.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun DiskWidget(
    data: DiskData,
    modifier: Modifier = Modifier,
    config: WidgetConfig = WidgetConfig()
) {
    val title = config.customTitle.ifBlank { "Depolama (SSD)" }
    val theme = LocalCustomTheme.current
    val accent = config.customAccentColor ?: theme.primaryAccent
    val scale = config.scaleMultiplier
    val shape = M3ShapeTokens.getShape(config.shapeStyle)

    val isFull = data.percent > 90.0
    val glowColor = if (isFull) M3DarkError else null

    val usedFraction = (data.percent / 100.0).toFloat().coerceIn(0f, 1f)
    val freeFraction = (1f - usedFraction).coerceIn(0f, 1f)

    M3Card(
        modifier = modifier,
        shape = shape,
        glowColor = glowColor
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
                            imageVector = Icons.Rounded.Save,
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
                    text = "%.0f / %.0f GB".format(data.usedGb, data.totalGb),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 2. Metrics Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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

            Surface(
                shape = M3ShapeTokens.Pill,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "Boş: %.0f GB".format(data.freeGb),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // 3. Segmented Progress Bar
        if (config.showBar) {
            M3SegmentedBar(
                segments = listOf(
                    BarSegment(fraction = usedFraction, color = if (isFull) M3DarkError else accent, label = "used"),
                    BarSegment(fraction = freeFraction, color = MaterialTheme.colorScheme.surfaceVariant, label = "free")
                ),
                height = 7.dp
            )
        }
    }
}
