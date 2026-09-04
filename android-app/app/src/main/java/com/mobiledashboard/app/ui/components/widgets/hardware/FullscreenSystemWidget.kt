package com.mobiledashboard.app.ui.components.widgets.hardware

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledashboard.app.data.model.TelemetryPayload
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.components.core.M3Gauge
import com.mobiledashboard.app.ui.components.core.M3ProgressBar
import com.mobiledashboard.app.ui.theme.*
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Material 3 Expressive Unified Monolith System Telemetry Widget.
 * 
 * Features:
 * - 100% Transparent Root Canvas (floating on AMOLED pure black).
 * - Top Hero Band: Continuous Live Network Sine-Wave Stream with 8-Lobe Scalloped Clover Badge.
 * - Bottom 4-Piece Diagonal Bento Matrix: CPU, GPU, RAM, SSD with balanced Diagonal & Reverse Diagonal Pill Shapes.
 * - Zero text overflow & spacious breathing room.
 */
@Composable
fun FullscreenSystemWidget(
    telemetry: TelemetryPayload,
    modifier: Modifier = Modifier,
    config: WidgetConfig = WidgetConfig()
) {
    val scale = config.scaleMultiplier
    val theme = LocalCustomTheme.current
    val accent = config.customAccentColor ?: theme.primaryAccent

    // Infinite sine-wave phase transition
    val infiniteTransition = rememberInfiniteTransition(label = "FullscreenSystemAnim")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    // Smooth animated metric transitions
    val animatedCpuPercent by animateFloatAsState(
        targetValue = (telemetry.cpu.percent / 100f).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "AnimCpu"
    )
    val animatedGpuPercent by animateFloatAsState(
        targetValue = (telemetry.gpu.percent / 100f).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "AnimGpu"
    )
    val animatedRamPercent by animateFloatAsState(
        targetValue = (telemetry.ram.percent / 100f).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "AnimRam"
    )
    val animatedDiskPercent by animateFloatAsState(
        targetValue = (telemetry.disk.percent / 100f).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "AnimDisk"
    )

    // 100% Transparent Root Canvas with generous organic breathing room
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(8.dp)
    ) {
        // ==============================================================
        // UNIFIED MONOLITH BENTO: TOP NETWORK STREAM + 4-PIECE HARDWARE MATRIX
        // ==============================================================
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. TOP HERO BAND: Live Network Sine-Wave Streamline Capsule
            M3NetworkTopStreamCapsule(
                downKbps = telemetry.network.downKbps,
                upKbps = telemetry.network.upKbps,
                accent = accent,
                wavePhase = wavePhase,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.72f),
                scale = scale
            )

            // 2. BOTTOM 4-PIECE DIAGONAL BENTO MATRIX (2x2 Grid)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2.28f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Row 1: CPU (Diagonal Pill) & GPU (Reverse Diagonal Pill)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // CPU Diagonal Pill Pod (48dp / 14dp / 48dp / 14dp)
                    M3CpuDiagonalPillPod(
                        percent = telemetry.cpu.percent,
                        animatedProgress = animatedCpuPercent,
                        temp = telemetry.cpu.temp,
                        accent = accent,
                        modifier = Modifier.weight(1f),
                        scale = scale
                    )

                    // GPU Reverse Diagonal Pill Pod (14dp / 48dp / 14dp / 48dp)
                    M3GpuDiagonalPillPod(
                        name = telemetry.gpu.name.ifBlank { "NVIDIA / AMD GPU" },
                        percent = telemetry.gpu.percent,
                        animatedProgress = animatedGpuPercent,
                        temp = telemetry.gpu.temp,
                        memoryUsedMb = telemetry.gpu.memoryUsedMb,
                        memoryTotalMb = telemetry.gpu.memoryTotalMb,
                        accent = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        scale = scale
                    )
                }

                // Row 2: RAM (Reverse Diagonal Pill) & SSD (Diagonal Pill)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // RAM Reverse Diagonal Pill Pod (14dp / 48dp / 14dp / 48dp)
                    M3RamDiagonalPillPod(
                        percent = telemetry.ram.percent,
                        animatedProgress = animatedRamPercent,
                        usedGb = telemetry.ram.usedGb,
                        totalGb = telemetry.ram.totalGb,
                        freeGb = telemetry.ram.freeGb,
                        accent = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                        scale = scale
                    )

                    // SSD Diagonal Pill Pod (48dp / 14dp / 48dp / 14dp)
                    M3SsdDiagonalPillPod(
                        percent = telemetry.disk.percent,
                        animatedProgress = animatedDiskPercent,
                        usedGb = telemetry.disk.usedGb,
                        totalGb = telemetry.disk.totalGb,
                        freeGb = telemetry.disk.freeGb,
                        accent = AccentYellow,
                        modifier = Modifier.weight(1f),
                        scale = scale
                    )
                }
            }
        }
    }
}

/**
 * 🌐 1. Top Hero Band: Continuous Live Network Streamline Capsule with 8-Lobe Scalloped Badge.
 */
@Composable
private fun M3NetworkTopStreamCapsule(
    downKbps: Double,
    upKbps: Double,
    accent: Color,
    wavePhase: Float,
    modifier: Modifier = Modifier,
    scale: Float = 1.0f
) {
    val downStr = if (downKbps >= 1024) "%.1f MB/s".format(downKbps / 1024.0) else "%.0f KB/s".format(downKbps)
    val upStr = if (upKbps >= 1024) "%.1f MB/s".format(upKbps / 1024.0) else "%.0f KB/s".format(upKbps)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = M3ShapeTokens.Pill,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: 8-Lobe Scalloped Clover Network Badge & Download Stat
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 8-Lobe Scalloped Clover Mini Hub
                Surface(
                    shape = M3ShapeTokens.Scalloped8,
                    color = accent.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.SwapVert,
                            contentDescription = "Network",
                            tint = accent,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "İNDİRME",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = M3ShapeTokens.Pill,
                            color = AccentGreen.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, AccentGreen.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "1 Gbps",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGreen,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = downStr,
                        fontSize = (14 * scale).sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = accent
                    )
                }
            }

            // Center: Live Sine Wave Activity Visualizer
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val midY = h / 2f
                    val wavePath = Path()
                    val activity = ((downKbps + upKbps) / 2048.0).toFloat().coerceIn(0.20f, 1.0f)

                    for (x in 0..w.toInt() step 4) {
                        val normX = x / w
                        val y = midY + (sin((normX * 4 * PI.toFloat()) + wavePhase) * (h * 0.38f * activity))
                        if (x == 0) wavePath.moveTo(x.toFloat(), y) else wavePath.lineTo(x.toFloat(), y)
                    }

                    drawPath(
                        path = wavePath,
                        brush = Brush.horizontalGradient(
                            listOf(accent.copy(alpha = 0.35f), accent, AccentPurple)
                        ),
                        style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Right: Upload Stat & Arrow Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "YÜKLEME",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = upStr,
                        fontSize = (14 * scale).sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = AccentPurple
                    )
                }

                Surface(
                    shape = M3ShapeTokens.Pill,
                    color = AccentPurple.copy(alpha = 0.16f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = "Upload",
                            tint = AccentPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * ⚡ 2. CPU Diagonal Pill Pod (48dp / 14dp / 48dp / 14dp).
 */
@Composable
private fun M3CpuDiagonalPillPod(
    percent: Double,
    animatedProgress: Float,
    temp: Double,
    accent: Color,
    modifier: Modifier = Modifier,
    scale: Float = 1.0f
) {
    val dynamicGaugeColor by animateColorAsState(
        targetValue = when {
            temp >= 82.0 || percent >= 88.0 -> AccentRed
            temp >= 70.0 || percent >= 72.0 -> AccentYellow
            else -> accent
        },
        animationSpec = tween(400),
        label = "CpuGaugeColor"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = M3ShapeTokens.DiagonalPill,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Icon, Label & Temp Pill
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
                        color = accent.copy(alpha = 0.16f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Memory,
                                contentDescription = "CPU",
                                tint = accent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "İŞLEMCİ (CPU)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (temp > 0) {
                    Surface(
                        shape = M3ShapeTokens.Pill,
                        color = dynamicGaugeColor.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, dynamicGaugeColor.copy(alpha = 0.40f))
                    ) {
                        Text(
                            text = "${temp.roundToInt()}°C",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = dynamicGaugeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Center Content: Large Percentage & Circular M3 Arc Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "%${percent.roundToInt()}",
                        fontSize = (28 * scale).sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = if (percent < 30) "Yük: Hafif" else if (percent < 75) "Yük: Normal" else "Yük: Yoğun",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    M3Gauge(
                        progress = animatedProgress,
                        accentColor = dynamicGaugeColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 5f
                    )
                }
            }
        }
    }
}

/**
 * 🎮 3. GPU Reverse Diagonal Pill Pod (14dp / 48dp / 14dp / 48dp).
 */
@Composable
private fun M3GpuDiagonalPillPod(
    name: String,
    percent: Double,
    animatedProgress: Float,
    temp: Double,
    memoryUsedMb: Double,
    memoryTotalMb: Double,
    accent: Color,
    modifier: Modifier = Modifier,
    scale: Float = 1.0f
) {
    val dynamicGaugeColor by animateColorAsState(
        targetValue = when {
            temp >= 82.0 || percent >= 88.0 -> AccentRed
            temp >= 70.0 || percent >= 72.0 -> AccentYellow
            else -> accent
        },
        animationSpec = tween(400),
        label = "GpuGaugeColor"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = M3ShapeTokens.ReverseDiagonalPill,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Icon, GPU Name & Temp Pill
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
                        color = accent.copy(alpha = 0.16f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.SportsEsports,
                                contentDescription = "GPU",
                                tint = accent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "EKRAN KARTI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (temp > 0) {
                    Surface(
                        shape = M3ShapeTokens.Pill,
                        color = dynamicGaugeColor.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, dynamicGaugeColor.copy(alpha = 0.40f))
                    ) {
                        Text(
                            text = "${temp.roundToInt()}°C",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = dynamicGaugeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Center Content: Percentage & Dial Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "%${percent.roundToInt()}",
                        fontSize = (28 * scale).sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                    if (memoryTotalMb > 0) {
                        Text(
                            text = "VRAM: %.1f / %.0f GB".format(memoryUsedMb / 1024.0, memoryTotalMb / 1024.0),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                    } else {
                        Text(
                            text = "GPU Aktif",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    M3Gauge(
                        progress = animatedProgress,
                        accentColor = dynamicGaugeColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 5f
                    )
                }
            }
        }
    }
}

/**
 * 💾 4. RAM Reverse Diagonal Pill Pod (14dp / 48dp / 14dp / 48dp).
 */
@Composable
private fun M3RamDiagonalPillPod(
    percent: Double,
    animatedProgress: Float,
    usedGb: Double,
    totalGb: Double,
    freeGb: Double,
    accent: Color,
    modifier: Modifier = Modifier,
    scale: Float = 1.0f
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = M3ShapeTokens.ReverseDiagonalPill,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row
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
                        color = accent.copy(alpha = 0.16f),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Storage,
                                contentDescription = "RAM",
                                tint = accent,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Text(
                        text = "BELLEK (RAM)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "%${percent.roundToInt()}",
                    fontSize = (17 * scale).sp,
                    fontWeight = FontWeight.Black,
                    color = accent
                )
            }

            // Stadium Animated Progress Bar
            M3ProgressBar(
                progress = animatedProgress,
                accentColor = accent,
                height = 7
            )

            // Details Bottom Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "%.1f / %.1f GB".format(usedGb, totalGb),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "%.1f GB Boş".format(freeGb),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 💽 5. SSD Diagonal Pill Pod (48dp / 14dp / 48dp / 14dp - Zero Overflow).
 */
@Composable
private fun M3SsdDiagonalPillPod(
    percent: Double,
    animatedProgress: Float,
    usedGb: Double,
    totalGb: Double,
    freeGb: Double,
    accent: Color,
    modifier: Modifier = Modifier,
    scale: Float = 1.0f
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = M3ShapeTokens.DiagonalPill,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row
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
                        color = accent.copy(alpha = 0.16f),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Save,
                                contentDescription = "SSD",
                                tint = accent,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Text(
                        text = "DEPOLAMA (SSD)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "%${percent.roundToInt()}",
                    fontSize = (17 * scale).sp,
                    fontWeight = FontWeight.Black,
                    color = accent
                )
            }

            // Stadium Animated Progress Bar
            M3ProgressBar(
                progress = animatedProgress,
                accentColor = accent,
                height = 7
            )

            // Details Bottom Row (Formatted cleanly to prevent overflow)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "%.0f / %.0f GB".format(usedGb, totalGb),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "%.0f GB Boş".format(freeGb),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
