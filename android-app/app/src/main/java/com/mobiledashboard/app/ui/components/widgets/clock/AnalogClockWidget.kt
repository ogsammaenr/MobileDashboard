package com.mobiledashboard.app.ui.components.widgets.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.components.core.M3Card
import com.mobiledashboard.app.ui.theme.*
import kotlinx.coroutines.delay
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogClockWidget(
    modifier: Modifier = Modifier,
    config: WidgetConfig = WidgetConfig()
) {
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    val theme = LocalCustomTheme.current
    val accent = config.customAccentColor ?: theme.primaryAccent

    LaunchedEffect(Unit) {
        while (true) {
            calendar = Calendar.getInstance()
            delay(1000)
        }
    }

    val seconds = calendar.get(Calendar.SECOND)
    val minutes = calendar.get(Calendar.MINUTE)
    val hours = calendar.get(Calendar.HOUR)

    val dialRimColor = MaterialTheme.colorScheme.surfaceVariant
    val majorTickColor = MaterialTheme.colorScheme.onSurface
    val minorTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val hourHandColor = MaterialTheme.colorScheme.onSurface
    val secondHandColor = MaterialTheme.colorScheme.tertiary

    M3Card(
        modifier = modifier,
        shape = M3ScallopedShape(8),
        padding = PaddingValues(14.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Dial Rim
                drawCircle(
                    color = dialRimColor,
                    radius = radius - 4.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Hour Markers
                for (i in 0 until 12) {
                    val angle = (i * 30.0) * (PI / 180.0)
                    val isMajor = i % 3 == 0
                    val markerLength = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                    val startR = radius - 16.dp.toPx()
                    val endR = startR - markerLength

                    val startX = center.x + (startR * sin(angle)).toFloat()
                    val startY = center.y - (startR * cos(angle)).toFloat()
                    val endX = center.x + (endR * sin(angle)).toFloat()
                    val endY = center.y - (endR * cos(angle)).toFloat()

                    drawLine(
                        color = if (isMajor) majorTickColor else minorTickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Hour Hand
                val hourAngle = ((hours + minutes / 60f) * 30f) * (PI.toFloat() / 180f)
                val hourLen = radius * 0.48f
                drawLine(
                    color = hourHandColor,
                    start = center,
                    end = Offset(center.x + hourLen * sin(hourAngle), center.y - hourLen * cos(hourAngle)),
                    strokeWidth = 4.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Minute Hand
                val minAngle = (minutes * 6f) * (PI.toFloat() / 180f)
                val minLen = radius * 0.70f
                drawLine(
                    color = accent,
                    start = center,
                    end = Offset(center.x + minLen * sin(minAngle), center.y - minLen * cos(minAngle)),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Second Hand
                val secAngle = (seconds * 6f) * (PI.toFloat() / 180f)
                val secLen = radius * 0.82f
                drawLine(
                    color = secondHandColor,
                    start = center,
                    end = Offset(center.x + secLen * sin(secAngle), center.y - secLen * cos(secAngle)),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Center Pin
                drawCircle(color = secondHandColor, radius = 4.dp.toPx(), center = center)
                drawCircle(color = dialRimColor, radius = 2.dp.toPx(), center = center)
            }
        }
    }
}
