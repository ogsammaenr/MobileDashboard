package com.mobiledashboard.app.ui.components.widgets.clock

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.theme.LocalCustomTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * 🕰️ Material 3 Pure AMOLED Fullscreen Animated Digital Clock.
 * 
 * Features:
 * - 100% Transparent Root Box (floats directly on pure AMOLED black).
 * - No card borders, no titles, no badges, no progress bars.
 * - Pure centered animated digital clock numbers with sliding digit transitions.
 * - Breathing ambient glow aura in background.
 * - Pulsing glowing colon (:).
 * - Optional seconds and subtle date.
 */
@Composable
fun AnimatedDigitalClockWidget(
    modifier: Modifier = Modifier,
    config: WidgetConfig = WidgetConfig()
) {
    val scale = config.scaleMultiplier
    val theme = LocalCustomTheme.current
    val accent = config.customAccentColor ?: theme.primaryAccent

    // 1-second interval time ticker
    var currentTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(500)
        }
    }

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
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraAlpha"
    )

    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraScale"
    )

    // Pulsing colon
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ColonAlpha"
    )

    // Pure 100% Transparent Root Box (Directly on AMOLED Pure Black)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight

        // Dynamically compute giant responsive font sizes so it maximally fills the screen
        val hasExtras = config.showSeconds || config.is12Hour
        val calculatedFromWidth = containerWidth.value / (if (hasExtras) 4.0f else 3.2f)
        val calculatedFromHeight = containerHeight.value * 0.58f
        val calculatedBase = minOf(calculatedFromWidth, calculatedFromHeight).coerceIn(48f, 240f)

        val mainFontSize = (calculatedBase * scale).sp
        val colonFontSize = (calculatedBase * 0.92f * scale).sp
        val secondsFontSize = (calculatedBase * 0.36f * scale).sp
        val amPmFontSize = (calculatedBase * 0.20f * scale).sp
        val dateFontSize = (calculatedBase * 0.16f * scale).coerceIn(12f, 26f).sp

        // 1. BACKGROUND: Breathing Ambient Glow Aura
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension * 0.65f) * auraScale

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = auraAlpha * 0.40f),
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

        // 2. FOREGROUND: Pure Centered Giant Animated Digital Clock
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Clock Row (HH : MM + optional SS / AM-PM)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Animated Hours
                AnimatedTimeSegment(
                    text = hoursStr,
                    fontSize = mainFontSize,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                // Pulsing Glowing Colon
                Text(
                    text = ":",
                    fontSize = colonFontSize,
                    fontWeight = FontWeight.Black,
                    color = accent.copy(alpha = colonAlpha),
                    modifier = Modifier.padding(horizontal = (8 * scale).dp)
                )

                // Animated Minutes
                AnimatedTimeSegment(
                    text = minutesStr,
                    fontSize = mainFontSize,
                    fontWeight = FontWeight.Black,
                    color = accent
                )

                // Optional Seconds / 12H Tag
                if (config.showSeconds || config.is12Hour) {
                    Column(
                        modifier = Modifier.padding(start = (10 * scale).dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (config.is12Hour) {
                            Text(
                                text = amPmStr,
                                fontSize = amPmFontSize,
                                fontWeight = FontWeight.Black,
                                color = accent.copy(alpha = 0.85f),
                                letterSpacing = 1.sp
                            )
                        }
                        if (config.showSeconds) {
                            AnimatedTimeSegment(
                                text = secondsStr,
                                fontSize = secondsFontSize,
                                fontWeight = FontWeight.Bold,
                                color = accent.copy(alpha = 0.90f)
                            )
                        }
                    }
                }
            }

            // Optional Subtle Date
            if (config.showDate) {
                Spacer(modifier = Modifier.height((10 * scale).dp))
                Text(
                    text = "$dayNameStr, $dateFullStr",
                    fontSize = dateFontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.65f),
                    letterSpacing = 0.8.sp
                )
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
                    letterSpacing = (-2).sp
                )
            }
        }
    }
}
