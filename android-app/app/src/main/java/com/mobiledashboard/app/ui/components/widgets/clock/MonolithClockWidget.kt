package com.mobiledashboard.app.ui.components.widgets.clock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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

@Composable
fun MonolithClockWidget(
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
    val dateStr = SimpleDateFormat("d MMMM yyyy, EEEE", Locale("tr")).format(currentTime)

    val shape = M3ShapeTokens.getShape(config.shapeStyle)

    M3Card(
        modifier = modifier,
        shape = shape,
        padding = PaddingValues(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((-20).dp)
            ) {
                Text(
                    text = hours,
                    fontSize = (84 * scale).sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = (84 * scale).sp
                )
                Text(
                    text = minutes,
                    fontSize = (84 * scale).sp,
                    fontWeight = FontWeight.Black,
                    color = accent,
                    lineHeight = (84 * scale).sp
                )
            }

            Surface(
                shape = M3ShapeTokens.Pill,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ":$seconds",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = dateStr.replaceFirstChar { it.uppercase() },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
