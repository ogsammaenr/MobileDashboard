package com.mobiledashboard.app.ui.components.widgets.clock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.components.core.M3Card
import com.mobiledashboard.app.ui.components.core.M3Header
import com.mobiledashboard.app.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PillClockWidget(
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

    val timeStr = SimpleDateFormat(if (config.is12Hour) "hh:mm" else "HH:mm", Locale.getDefault()).format(currentTime)
    val secStr = SimpleDateFormat("ss", Locale.getDefault()).format(currentTime)
    val dateStr = SimpleDateFormat("d MMMM yyyy, EEEE", Locale("tr")).format(currentTime)

    M3Card(
        modifier = modifier,
        shape = M3ShapeTokens.Pill,
        padding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        M3Header(
            iconVector = Icons.Rounded.Schedule,
            title = config.customTitle.ifBlank { "Saat" },
            badgeText = dateStr.take(10),
            iconTint = accent
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Center Time Capsule
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = M3ShapeTokens.Pill,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = timeStr,
                    fontSize = (38 * scale).sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (config.showSeconds) {
                    Text(
                        text = ":$secStr",
                        fontSize = (20 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = accent,
                        modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                    )
                }
            }
        }

        if (config.showDate) {
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
