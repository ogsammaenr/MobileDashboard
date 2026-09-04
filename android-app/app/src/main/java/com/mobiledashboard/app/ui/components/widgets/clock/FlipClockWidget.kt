package com.mobiledashboard.app.ui.components.widgets.clock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
import com.mobiledashboard.app.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FlipClockWidget(
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

    val hours = SimpleDateFormat("HH", Locale.getDefault()).format(currentTime)
    val minutes = SimpleDateFormat("mm", Locale.getDefault()).format(currentTime)
    val seconds = SimpleDateFormat("ss", Locale.getDefault()).format(currentTime)

    M3Card(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlipCardUnit(hours, "SAAT", accent, scale)
            Text(
                text = ":",
                fontSize = (28 * scale).sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            FlipCardUnit(minutes, "DAKİKA", accent, scale)
            if (config.showSeconds) {
                Text(
                    text = ":",
                    fontSize = (28 * scale).sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                FlipCardUnit(seconds, "SANİYE", MaterialTheme.colorScheme.tertiary, scale)
            }
        }
    }
}

@Composable
fun FlipCardUnit(
    value: String,
    label: String,
    accentColor: androidx.compose.ui.graphics.Color,
    scale: Float = 1.0f
) {
    Surface(
        shape = M3ShapeTokens.SubCard,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = (34 * scale).sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = accentColor
            )
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
        }
    }
}
