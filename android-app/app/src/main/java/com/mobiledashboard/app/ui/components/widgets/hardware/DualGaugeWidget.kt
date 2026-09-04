package com.mobiledashboard.app.ui.components.widgets.hardware

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledashboard.app.data.model.CPUData
import com.mobiledashboard.app.data.model.GPUData
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.components.core.M3Card
import com.mobiledashboard.app.ui.components.core.M3Gauge
import com.mobiledashboard.app.ui.components.core.M3Header
import com.mobiledashboard.app.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun DualGaugeWidget(
    dataCpu: CPUData,
    dataGpu: GPUData,
    modifier: Modifier = Modifier,
    config: WidgetConfig = WidgetConfig()
) {
    val scale = config.scaleMultiplier
    val title = config.customTitle.ifBlank { "CPU & GPU GÖSTERGE" }
    val shape = M3ShapeTokens.getShape(config.shapeStyle)
    val theme = LocalCustomTheme.current
    val accentCpu = config.customAccentColor ?: theme.primaryAccent
    val accentGpu = theme.secondaryAccent

    val isOverheated = dataCpu.temp > 80 || dataGpu.temp > 80
    val glowColor = if (isOverheated) M3DarkError else null

    M3Card(
        modifier = modifier,
        shape = shape,
        glowColor = glowColor
    ) {
        M3Header(
            iconVector = Icons.Rounded.Speed,
            title = title,
            badgeText = "DUAL GAUGE",
            iconTint = accentCpu
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // CPU Dial SubCard
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = M3ShapeTokens.SubCard,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 4.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                        M3Gauge(
                            progress = (dataCpu.percent / 100f).toFloat(),
                            accentColor = accentCpu,
                            strokeWidth = 4.5f
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${dataCpu.percent.roundToInt()}%",
                                fontSize = (12 * scale).sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (dataCpu.temp > 0) {
                                Text(
                                    text = "${dataCpu.temp.roundToInt()}°",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dataCpu.temp > 80) M3DarkError else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Text(
                        text = "CPU",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentCpu,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // GPU Dial SubCard
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = M3ShapeTokens.SubCard,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 4.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                        M3Gauge(
                            progress = (dataGpu.percent / 100f).toFloat(),
                            accentColor = accentGpu,
                            strokeWidth = 4.5f
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${dataGpu.percent.roundToInt()}%",
                                fontSize = (12 * scale).sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (dataGpu.temp > 0) {
                                Text(
                                    text = "${dataGpu.temp.roundToInt()}°",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dataGpu.temp > 80) M3DarkError else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Text(
                        text = "GPU",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentGpu,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
