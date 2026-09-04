package com.mobiledashboard.app.ui.components.widgets.shortcut

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Launch
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledashboard.app.data.model.WidgetConfig
import com.mobiledashboard.app.ui.components.core.M3Card
import com.mobiledashboard.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest

data class AppPreset(
    val id: String,
    val defaultName: String,
    val emoji: String,
    val icon: ImageVector?,
    val brandColor: Color
)

object AppPresets {
    private val presets = mapOf(
        "spotify" to AppPreset("spotify", "Spotify", "🎵", Icons.Rounded.Headphones, Color(0xFF1DB954)),
        "steam" to AppPreset("steam", "Steam", "🎮", Icons.Rounded.SportsEsports, Color(0xFF66C0F4)),
        "discord" to AppPreset("discord", "Discord", "💬", Icons.Rounded.Forum, Color(0xFF5865F2)),
        "vesktop" to AppPreset("vesktop", "Vesktop", "💬", Icons.Rounded.Forum, Color(0xFF5865F2)),
        "code" to AppPreset("code", "VS Code", "💻", Icons.Rounded.Code, Color(0xFF007ACC)),
        "vscode" to AppPreset("vscode", "VS Code", "💻", Icons.Rounded.Code, Color(0xFF007ACC)),
        "browser" to AppPreset("browser", "Tarayıcı", "🌐", Icons.Rounded.Language, Color(0xFF4285F4)),
        "chrome" to AppPreset("chrome", "Chrome", "🌐", Icons.Rounded.Language, Color(0xFFEA4335)),
        "firefox" to AppPreset("firefox", "Firefox", "🦊", Icons.Rounded.Language, Color(0xFFFF7139)),
        "terminal" to AppPreset("terminal", "Terminal", "📟", Icons.Rounded.Terminal, Color(0xFF22C55E)),
        "files" to AppPreset("files", "Dosyalar", "📁", Icons.Rounded.Folder, Color(0xFFF59E0B)),
        "calculator" to AppPreset("calculator", "Hesap M.", "🔢", Icons.Rounded.Calculate, Color(0xFFEC4899)),
        "screenshot" to AppPreset("screenshot", "Ekran Al.", "📸", Icons.Rounded.CropFree, Color(0xFFA855F7)),
        "flameshot" to AppPreset("flameshot", "Flameshot", "📸", Icons.Rounded.CropFree, Color(0xFFA855F7)),
        "youtube" to AppPreset("youtube", "YouTube", "▶️", Icons.Rounded.PlayCircle, Color(0xFFFF0000)),
        "obsidian" to AppPreset("obsidian", "Obsidian", "📝", Icons.Rounded.Description, Color(0xFF7C3AED)),
        "obs" to AppPreset("obs", "OBS Studio", "🎥", Icons.Rounded.Videocam, Color(0xFF38BDF8)),
        "lock" to AppPreset("lock", "PC Kilitle", "🔒", Icons.Rounded.Lock, Color(0xFFEF4444)),
        "sleep" to AppPreset("sleep", "PC Uyut", "🌙", Icons.Rounded.Bedtime, Color(0xFF8B5CF6))
    )

    fun get(appId: String): AppPreset {
        val key = appId.lowercase().trim()
        return presets[key] ?: AppPreset(key, "Kısayol", "🚀", Icons.AutoMirrored.Rounded.Launch, AccentCyan)
    }
}

/**
 * Material 3 Expressive Full-Bleed App Shortcut & Action Tile.
 * Integrates the app icon seamlessly into the widget geometry (Scalloped 8-lobe, Clover, Circle, Squircle)
 * with ambient backdrop and cohesive theme harmony.
 */
@Composable
fun AppShortcutWidget(
    config: WidgetConfig,
    serverBaseUrl: String,
    onSystemControl: (String, Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val preset = AppPresets.get(config.appId)
    val appName = if (config.customTitle.isNotBlank()) config.customTitle else preset.defaultName
    val theme = LocalCustomTheme.current
    val accentColor = config.customAccentColor ?: theme.primaryAccent
    
    // Default to M3 8-lobe Scalloped shape if not specified
    val shape = when (config.shapeStyle) {
        "rounded" -> M3ShapeTokens.Card
        "clover" -> M3ShapeTokens.getShape("clover")
        "circle" -> M3ShapeTokens.Circle
        "pill" -> M3ShapeTokens.Pill
        "asymmetric" -> M3ShapeTokens.AsymmetricCard
        else -> M3ShapeTokens.Scalloped8 // 8-leaf scalloped flower by default
    }
    val context = LocalContext.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()
    var isTriggered by remember { mutableStateOf(false) }

    // Smooth Spring Bounce Scale
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.60f, stiffness = 380f),
        label = "m3_tile_scale"
    )

    // Animated Tonal Border & Glow
    val animatedBorderColor by animateColorAsState(
        targetValue = when {
            isTriggered -> accentColor
            isPressed -> accentColor.copy(alpha = 0.70f)
            else -> CardBorder.copy(alpha = 0.85f)
        },
        animationSpec = tween(220),
        label = "m3_tile_border"
    )

    val target = when {
        config.appCommand.isNotBlank() -> config.appCommand
        config.appPath.isNotBlank() -> config.appPath
        else -> config.appId
    }

    val iconUrl = if (config.appIconUrl.isNotBlank()) {
        if (config.appIconUrl.startsWith("http")) config.appIconUrl else "$serverBaseUrl${config.appIconUrl}"
    } else null

    val imageRequest = remember(iconUrl) {
        if (iconUrl.isNullOrBlank()) null
        else {
            ImageRequest.Builder(context)
                .data(iconUrl)
                .decoderFactory(SvgDecoder.Factory())
                .crossfade(true)
                .build()
        }
    }

    M3Card(
        modifier = modifier
            .fillMaxSize()
            .scale(scale)
            .border(1.4.dp, animatedBorderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                val action = if (config.appId in listOf("lock", "sleep")) config.appId else "launch-app"
                onSystemControl(action, 0, target)

                scope.launch {
                    isTriggered = true
                    delay(700)
                    isTriggered = false
                }
            },
        shape = shape,
        backgroundColor = theme.surfaceContainer
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (isTriggered) accentColor.copy(alpha = 0.35f)
                            else if (isPressed) accentColor.copy(alpha = 0.20f)
                            else accentColor.copy(alpha = 0.12f),
                            theme.surfaceContainer
                        )
                    )
                )
                .padding(horizontal = 6.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            val availableHeight = maxHeight
            val isCompact = availableHeight < 95.dp
            val primaryIconSize = if (isCompact) 38.dp else 46.dp

            // 1. Layer 1: Ambient Backdrop Icon Tint (faint diffuse fill)
            if (imageRequest != null) {
                SubcomposeAsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(primaryIconSize * 1.5f)
                        .align(Alignment.Center)
                        .scale(1.2f),
                    alpha = if (isTriggered) 0.30f else 0.10f
                )
            }

            // 2. Layer 2: Main Foreground Content
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Centered Crisp Icon
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isTriggered) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Başlatıldı",
                            tint = accentColor,
                            modifier = Modifier.size(primaryIconSize * 0.85f)
                        )
                    } else if (imageRequest != null) {
                        SubcomposeAsyncImage(
                            model = imageRequest,
                            contentDescription = appName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(primaryIconSize),
                            loading = {
                                Text(preset.emoji, fontSize = if (isCompact) 22.sp else 28.sp)
                            },
                            error = {
                                if (config.appIcon.isNotBlank()) {
                                    Text(config.appIcon, fontSize = if (isCompact) 22.sp else 28.sp)
                                } else if (preset.icon != null) {
                                    Icon(
                                        imageVector = preset.icon,
                                        contentDescription = appName,
                                        tint = accentColor,
                                        modifier = Modifier.size(primaryIconSize)
                                    )
                                } else {
                                    Text(preset.emoji, fontSize = if (isCompact) 22.sp else 28.sp)
                                }
                            }
                        )
                    } else if (config.appIcon.isNotBlank()) {
                        Text(
                            text = config.appIcon,
                            fontSize = if (isCompact) 26.sp else 32.sp
                        )
                    } else if (preset.icon != null) {
                        Icon(
                            imageVector = preset.icon,
                            contentDescription = appName,
                            tint = accentColor,
                            modifier = Modifier.size(primaryIconSize)
                        )
                    } else {
                        Text(
                            text = preset.emoji,
                            fontSize = if (isCompact) 26.sp else 32.sp
                        )
                    }
                }

                // Seamless Embedded App Name
                Text(
                    text = if (isTriggered) "Açılıyor..." else appName,
                    fontSize = (if (isCompact) 10.0f else 11.2f * config.scaleMultiplier).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isTriggered) accentColor else TextMain,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp)
                )
            }

            // Top-right mini live dot indicator
            if (isTriggered) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}
