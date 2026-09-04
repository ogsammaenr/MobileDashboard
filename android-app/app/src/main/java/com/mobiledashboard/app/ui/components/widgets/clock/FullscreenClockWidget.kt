package com.mobiledashboard.app.ui.components.widgets.clock

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledashboard.app.data.model.TelemetryPayload
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.components.core.M3ProgressBar
import com.mobiledashboard.app.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Material 3 Expressive Fullscreen Unified Digital Clock & Bento Matrix.
 * 
 * Features:
 * - 100% Transparent Root Canvas (floating cleanly on AMOLED pure black).
 * - Hero Digital Clock Pod: Giant Display Numbers, 360-degree Second Gauge, AM/PM & Live Sync Badges.
 * - Bottom 3-Piece Bento Matrix:
 *   1. Calendar & 7-Day Weekly Chip Strip Pod (8-Lobe Scalloped Clover Badge).
 *   2. Solar Day Phase & 24h Progress Pod (Animated Stadium Bar).
 *   3. World UTC Time & System Hardware Telemetry Mini Pod.
 */
@Composable
fun FullscreenClockWidget(
    telemetry: TelemetryPayload = TelemetryPayload(),
    modifier: Modifier = Modifier,
    config: WidgetConfig = WidgetConfig()
) {
    val scale = config.scaleMultiplier
    val theme = LocalCustomTheme.current
    val accent = config.customAccentColor ?: theme.primaryAccent

    // Real-time 1-second clock ticker
    var currentTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(500)
        }
    }

    val cal = Calendar.getInstance().apply { time = currentTime }
    val hour24 = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)
    val second = cal.get(Calendar.SECOND)
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday, ...
    val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
    val weekOfYear = cal.get(Calendar.WEEK_OF_YEAR)

    // Formatted strings
    val hourFormat = if (config.is12Hour) "hh" else "HH"
    val hoursStr = SimpleDateFormat(hourFormat, Locale.getDefault()).format(currentTime)
    val minutesStr = SimpleDateFormat("mm", Locale.getDefault()).format(currentTime)
    val secondsStr = SimpleDateFormat("ss", Locale.getDefault()).format(currentTime)
    val amPmStr = SimpleDateFormat("a", Locale.getDefault()).format(currentTime).uppercase()

    val dateFullStr = SimpleDateFormat("d MMMM yyyy", Locale("tr")).format(currentTime)
    val dayNameStr = SimpleDateFormat("EEEE", Locale("tr")).format(currentTime).replaceFirstChar { it.uppercase() }

    // UTC Time format
    val utcTimeStr = SimpleDateFormat("HH:mm", Locale.ROOT).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(currentTime)

    // Day progress (0.0 to 1.0)
    val totalSecondsPassed = (hour24 * 3600) + (minute * 60) + second
    val dayProgress = (totalSecondsPassed / 86400f).coerceIn(0f, 1f)

    // Solar cycle greeting & icon
    val (greeting, solarIcon, solarColor) = when (hour24) {
        in 5..11 -> Triple("Günaydın", Icons.Rounded.WbSunny, AccentYellow)
        in 12..17 -> Triple("Tünaydın", Icons.Rounded.LightMode, accent)
        in 18..22 -> Triple("İyi Akşamlar", Icons.Rounded.NightsStay, AccentPurple)
        else -> Triple("İyi Geceler", Icons.Rounded.Bedtime, AccentCyan)
    }

    // Smooth animated second progress
    val animatedSecondProgress by animateFloatAsState(
        targetValue = second / 60f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "SecondProgress"
    )

    // 100% Transparent Root Canvas
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ==============================================================
            // 1. TOP HERO POD: GIANT DIGITAL CLOCK & 360° SECOND DIAL
            // ==============================================================
            HeroClockPod(
                hours = hoursStr,
                minutes = minutesStr,
                seconds = secondsStr,
                amPm = amPmStr,
                is12Hour = config.is12Hour,
                secondProgress = animatedSecondProgress,
                accent = accent,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.42f),
                scale = scale
            )

            // ==============================================================
            // 2. BOTTOM 3-PIECE BENTO MATRIX (Calendar, Day Cycle, World/Stats)
            // ==============================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pod 1: Calendar & 7-Day Strip
                CalendarBentoPod(
                    dateFullStr = dateFullStr,
                    dayNameStr = dayNameStr,
                    dayOfWeek = dayOfWeek,
                    dayOfYear = dayOfYear,
                    weekOfYear = weekOfYear,
                    accent = accent,
                    modifier = Modifier.weight(1f),
                    scale = scale
                )

                // Pod 2: Day Cycle & Solar Progress
                DayCycleBentoPod(
                    greeting = greeting,
                    solarIcon = solarIcon,
                    solarColor = solarColor,
                    dayProgress = dayProgress,
                    modifier = Modifier.weight(1f),
                    scale = scale
                )

                // Pod 3: World UTC Time & Telemetry Status
                WorldAndTelemetryPod(
                    utcTime = utcTimeStr,
                    telemetry = telemetry,
                    accent = accent,
                    modifier = Modifier.weight(1f),
                    scale = scale
                )
            }
        }
    }
}

/**
 * 👑 Top Hero Pod: Giant M3 Display Numbers & 360-Degree Animated Second Ring.
 */
@Composable
private fun HeroClockPod(
    hours: String,
    minutes: String,
    seconds: String,
    amPm: String,
    is12Hour: Boolean,
    secondProgress: Float,
    accent: Color,
    modifier: Modifier = Modifier,
    scale: Float = 1.0f
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = M3ShapeTokens.HeroCard,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Status Pills & Time Mode
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
                        shape = M3ShapeTokens.Pill,
                        color = accent.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "DİJİTAL SAAT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = accent
                            )
                        }
                    }

                    Surface(
                        shape = M3ShapeTokens.Pill,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = if (is12Hour) "$amPm (12S)" else "24S MODU",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Live Pulse Pill
                Surface(
                    shape = M3ShapeTokens.Pill,
                    color = AccentGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.40f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(AccentGreen, M3ShapeTokens.Circle)
                        )
                        Text(
                            text = "CANLI SYNC",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentGreen
                        )
                    }
                }
            }

            // Center Display: Giant Digital Numbers + 360-Degree Circular Second Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Giant Hours and Minutes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = hours,
                        fontSize = (68 * scale).sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-1.5).sp,
                        lineHeight = (68 * scale).sp
                    )
                    Text(
                        text = ":",
                        fontSize = (62 * scale).sp,
                        fontWeight = FontWeight.Black,
                        color = accent,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = minutes,
                        fontSize = (68 * scale).sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        color = accent,
                        letterSpacing = (-1.5).sp,
                        lineHeight = (68 * scale).sp
                    )
                }

                // 360° Circular M3 Second Dial
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokePx = 5.dp.toPx()
                        val diameter = size.minDimension - strokePx
                        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

                        // Background track circle
                        drawArc(
                            color = Color(0xFF262835),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )

                        // Active animated second arc
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(accent.copy(alpha = 0.4f), accent, AccentPurple)
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * secondProgress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = ":$seconds",
                            fontSize = (15 * scale).sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "SANİYE",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 📅 Pod 1: Calendar & 7-Day Weekly Strip Pod.
 */
@Composable
private fun CalendarBentoPod(
    dateFullStr: String,
    dayNameStr: String,
    dayOfWeek: Int, // 1=Sunday, 2=Monday, ..., 7=Saturday
    dayOfYear: Int,
    weekOfYear: Int,
    accent: Color,
    modifier: Modifier = Modifier,
    scale: Float = 1.0f
) {
    // Weekdays ordered Monday to Sunday (ISO format)
    val daysList = listOf("P", "S", "Ç", "P", "C", "C", "P")
    val isoDayIndex = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2 // 0 for Monday, 6 for Sunday

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = M3ShapeTokens.ReverseDiagonalCard,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: 8-Lobe Scalloped Icon & Day Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = M3ShapeTokens.Scalloped8,
                        color = accent.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.40f)),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = "Calendar",
                                tint = accent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "TAKVİM",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = M3ShapeTokens.Pill,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "$weekOfYear. Hafta",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Date & Day Texts
            Column {
                Text(
                    text = dayNameStr,
                    fontSize = (15 * scale).sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$dateFullStr • $dayOfYear. Gün",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // Mini 7-Day Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                daysList.forEachIndexed { index, letter ->
                    val isToday = index == isoDayIndex
                    Surface(
                        shape = M3ShapeTokens.Pill,
                        color = if (isToday) accent else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isToday) null else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.size(19.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = letter,
                                fontSize = 8.5.sp,
                                fontWeight = if (isToday) FontWeight.Black else FontWeight.Bold,
                                color = if (isToday) AmoledBlack else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * ☀️ Pod 2: Solar Day Cycle & 24h Progress Pod.
 */
@Composable
private fun DayCycleBentoPod(
    greeting: String,
    solarIcon: androidx.compose.ui.graphics.vector.ImageVector,
    solarColor: Color,
    dayProgress: Float,
    modifier: Modifier = Modifier,
    scale: Float = 1.0f
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = M3ShapeTokens.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Solar Icon & Greeting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = M3ShapeTokens.Chip,
                        color = solarColor.copy(alpha = 0.18f),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = solarIcon,
                                contentDescription = "Solar",
                                tint = solarColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "GÜN DÖNGÜSÜ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "%${(dayProgress * 100).roundToInt()}",
                    fontSize = (13 * scale).sp,
                    fontWeight = FontWeight.Black,
                    color = solarColor
                )
            }

            // Greeting text
            Column {
                Text(
                    text = greeting,
                    fontSize = (15 * scale).sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "Günün %${(dayProgress * 100).roundToInt()} tamamlandı",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Stadium Animated Progress Bar
            M3ProgressBar(
                progress = dayProgress,
                accentColor = solarColor,
                height = 6
            )
        }
    }
}

/**
 * 🌐 Pod 3: World UTC Time & Telemetry Status Pod.
 */
@Composable
private fun WorldAndTelemetryPod(
    utcTime: String,
    telemetry: TelemetryPayload,
    accent: Color,
    modifier: Modifier = Modifier,
    scale: Float = 1.0f
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = M3ShapeTokens.DiagonalCard,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Globe Icon & Time Zone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = M3ShapeTokens.Chip,
                        color = AccentPurple.copy(alpha = 0.18f),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Public,
                                contentDescription = "World Clock",
                                tint = AccentPurple,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "DÜNYA SAATİ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = M3ShapeTokens.Pill,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "GMT+3",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentPurple,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // UTC Time info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$utcTime UTC",
                        fontSize = (15 * scale).sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "İstanbul / Türkiye",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Telemetry Mini Stat Badges (CPU & RAM)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = M3ShapeTokens.Pill,
                    color = accent.copy(alpha = 0.14f),
                    border = BorderStroke(0.5.dp, accent.copy(alpha = 0.35f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⚡ CPU %${telemetry.cpu.percent.roundToInt()}",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = accent,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = M3ShapeTokens.Pill,
                    color = AccentGreen.copy(alpha = 0.14f),
                    border = BorderStroke(0.5.dp, AccentGreen.copy(alpha = 0.35f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "💾 RAM %${telemetry.ram.percent.roundToInt()}",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentGreen,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
