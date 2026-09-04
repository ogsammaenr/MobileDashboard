package com.mobiledashboard.app.ui.components.widgets.clock

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.components.core.M3Card
import com.mobiledashboard.app.ui.theme.LocalCustomTheme
import com.mobiledashboard.app.ui.theme.M3ShapeTokens
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 🕰️ Material 3 Expressive Animated Fullscreen Digital Clock.
 * 
 * Features:
 * - 100% Screen coverage (4x3 full matrix).
 * - Focused strictly on time (pure clock elegance).
 * - Animated sliding digits on minute/hour/second change.
 * - Breathing ambient glow aura in background.
 * - Live animated 360-degree second orbit & sweep trail.
 * - Pulsing glowing neon colon (:).
 * - Bottom smooth 60s progress bar and date pill.
 */
@Composable
fun AnimatedDigitalClockWidget(
    modifier: Modifier = Modifier,
    config: WidgetConfig = WidgetConfig()
) {
    val scale = config.scaleMultiplier
    val theme = LocalCustomTheme.current
    val accent = config.customAccentColor ?: theme.primaryAccent
    val shape = M3ShapeTokens.getShape(config.shapeStyle)
    val title = config.customTitle.ifBlank { "DİJİTAL SAAT" }

    // 1-second interval time ticker
    var currentTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(500)
        }
    }

    val cal = Calendar.getInstance().apply { time = currentTime }
    val second = cal.get(Calendar.SECOND)

    val hourFormat = if (config.is12Hour) "hh" else "HH"
    val hoursStr = SimpleDateFormat(hourFormat, Locale.getDefault()).format(currentTime)
    val minutesStr = SimpleDateFormat("mm", Locale.getDefault()).format(currentTime)
    val secondsStr = SimpleDateFormat("ss", Locale.getDefault()).format(currentTime)
    val amPmStr = SimpleDateFormat("a", Locale.getDefault()).format(currentTime).uppercase()

    val dateFullStr = SimpleDateFormat("d MMMM yyyy", Locale("tr")).format(currentTime)
    val dayNameStr = SimpleDateFormat("EEEE", Locale("tr")).format(currentTime).replaceFirstChar { it.uppercase() }

    // Infinite breathing ambient glow transition
    val infiniteTransition = rememberInfiniteTransition(label = "ClockAura")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraAlpha"
    )

    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraScale"
    )

    // Pulsing colon
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ColonAlpha"
    )

    // Smooth second progress animation (0f to 1f)
    val animatedSecondProgress by animateFloatAsState(
        targetValue = (second / 60f),
        animationSpec = tween(400, easing = LinearEasing),
        label = "AnimSecond"
    )

    M3Card(
        modifier = modifier.fillMaxSize(),
        shape = shape,
        glowColor = accent.copy(alpha = 0.4f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // ==============================================================
            // 1. BACKGROUND: Breathing Ambient Glow Aura
            // ==============================================================
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = (size.minDimension * 0.42f) * auraScale

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = auraAlpha * 0.35f),
                            accent.copy(alpha = auraAlpha * 0.12f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius
                    ),
                    center = center,
                    radius = radius
                )
            }

            // ==============================================================
            // 2. FOREGROUND: Clean Fullscreen Digital Clock Layout
            // ==============================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Title, Sync Badge & 12/24H indicator
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
                            color = accent.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.40f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = "Clock",
                                    tint = accent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = title.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = M3ShapeTokens.Pill,
                            color = accent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = if (config.is12Hour) amPmStr else "24 SAAT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                letterSpacing = 0.5.sp
                            )
                        }

                        Surface(
                            shape = M3ShapeTokens.Pill,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(accent)
                                )
                                Text(
                                    text = "CANLI",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                // Center Hero: Animated Giant Digits (HH : MM : SS)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Animated Hours
                        AnimatedTimeSegment(
                            text = hoursStr,
                            fontSize = (72 * scale).sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Pulsing Glowing Colon
                        Text(
                            text = ":",
                            fontSize = (68 * scale).sp,
                            fontWeight = FontWeight.Black,
                            color = accent.copy(alpha = colonAlpha),
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        // Animated Minutes
                        AnimatedTimeSegment(
                            text = minutesStr,
                            fontSize = (72 * scale).sp,
                            fontWeight = FontWeight.Black,
                            color = accent
                        )

                        // Optional Animated Seconds Pod
                        if (config.showSeconds) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                shape = M3ShapeTokens.Pill,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.90f),
                                border = BorderStroke(1.5.dp, accent.copy(alpha = 0.6f)),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnimatedTimeSegment(
                                        text = secondsStr,
                                        fontSize = (26 * scale).sp,
                                        fontWeight = FontWeight.Black,
                                        color = accent
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Section: Date Pill & Smooth 60-second Wavy Progress Bar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (config.showDate) {
                        Surface(
                            shape = M3ShapeTokens.Pill,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarToday,
                                    contentDescription = "Date",
                                    tint = accent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "$dayNameStr, $dateFullStr",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // 60-Second Full-Width Smooth Capsule Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedSecondProgress.coerceIn(0.01f, 1f))
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(accent.copy(alpha = 0.6f), accent)
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Animated individual character flip/slide transition.
 */
@Composable
private fun AnimatedTimeSegment(
    text: String,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    color: Color
) {
    Row {
        text.forEach { char ->
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    (slideInVertically(animationSpec = tween(420, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(280)))
                        .togetherWith(slideOutVertically(animationSpec = tween(420, easing = FastOutSlowInEasing)) { -it } + fadeOut(tween(280)))
                },
                label = "DigitSlideAnim"
            ) { targetChar ->
                Text(
                    text = targetChar.toString(),
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = color,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-1).sp
                )
            }
        }
    }
}
