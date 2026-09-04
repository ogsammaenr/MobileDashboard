package com.mobiledashboard.app.ui.components.widgets.clock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Schedule
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
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.components.core.M3Card
import com.mobiledashboard.app.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Android 14/15 Google Pixel Lockscreen 2-line Giant Clock Widget.
 */
@Composable
fun PixelClockWidget(
    modifier: Modifier = Modifier,
    config: WidgetConfig = WidgetConfig()
) {
    var currentTime by remember { mutableStateOf(Date()) }
    val theme = LocalCustomTheme.current
    val accent = config.customAccentColor ?: theme.primaryAccent
    val scale = config.scaleMultiplier

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000)
        }
    }

    val hourFormat = if (config.is12Hour) "hh" else "HH"
    val hours = SimpleDateFormat(hourFormat, Locale.getDefault()).format(currentTime)
    val minutes = SimpleDateFormat("mm", Locale.getDefault()).format(currentTime)
    val dateStr = SimpleDateFormat("EEEE, d MMMM", Locale("tr")).format(currentTime)

    val shape = M3ShapeTokens.getShape(config.shapeStyle)

    M3Card(
        modifier = modifier,
        shape = shape,
        padding = PaddingValues(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Two-line stacked Pixel Clock
            Column(verticalArrangement = Arrangement.spacedBy((-16).dp)) {
                Text(
                    text = hours,
                    fontSize = (68 * scale).sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = (68 * scale).sp
                )
                Text(
                    text = minutes,
                    fontSize = (68 * scale).sp,
                    fontWeight = FontWeight.Black,
                    color = accent,
                    lineHeight = (68 * scale).sp
                )
            }

            // Date & Live Status Pill
            if (config.showDate) {
                Surface(
                    shape = M3ShapeTokens.Pill,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = dateStr.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
