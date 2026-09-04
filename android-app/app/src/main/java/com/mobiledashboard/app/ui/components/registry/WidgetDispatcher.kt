package com.mobiledashboard.app.ui.components.registry

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mobiledashboard.app.data.model.TelemetryPayload
import com.mobiledashboard.app.data.model.WidgetInstance
import com.mobiledashboard.app.ui.components.widgets.clock.*
import com.mobiledashboard.app.ui.components.widgets.hardware.*
import com.mobiledashboard.app.ui.components.widgets.media.*
import com.mobiledashboard.app.ui.components.widgets.shortcut.AppShortcutWidget

/**
 * Material 3 Central Widget Dispatcher.
 * Maps widget_id strings to their corresponding Jetpack Compose composables.
 */
@Composable
fun RenderWidget(
    widget: WidgetInstance,
    telemetry: TelemetryPayload,
    serverBaseUrl: String,
    onMediaControl: (String) -> Unit,
    onSystemControl: (String, Int, String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val cfg = widget.config
    when (widget.widgetId) {
        // --- 1. CLOCK WIDGETS ---
        "clock_fullscreen_m3", "m3_clock_fullscreen", "fullscreen_clock", "clock_fullscreen" -> FullscreenClockWidget(
            telemetry = telemetry,
            modifier = modifier,
            config = cfg
        )
        "clock_m3_pixel", "m3_clock_lockscreen" -> PixelClockWidget(modifier = modifier, config = cfg)
        "clock_m3_pill" -> PillClockWidget(modifier = modifier, config = cfg)
        "clock_split_flip" -> FlipClockWidget(modifier = modifier, config = cfg)
        "clock_giant_monolith", "clock_hero", "clock_vertical_poster", "clock_cyber_hud" -> MonolithClockWidget(modifier = modifier, config = cfg)
        "clock_analog" -> AnalogClockWidget(modifier = modifier, config = cfg)

        // --- 2. HARDWARE & SENSOR WIDGETS ---
        "cpu_card" -> CpuWidget(data = telemetry.cpu, modifier = modifier, config = cfg)
        "gpu_card" -> GpuWidget(data = telemetry.gpu, modifier = modifier, config = cfg)
        "ram_card" -> RamWidget(data = telemetry.ram, modifier = modifier, config = cfg)
        "disk_card" -> DiskWidget(data = telemetry.disk, modifier = modifier, config = cfg)
        "network_card" -> NetworkWidget(data = telemetry.network, modifier = modifier, config = cfg)
        "system_fullscreen_m3", "m3_system_fullscreen", "system_monolith_m3", "system_dashboard_m3", "system_center", "hardware_fullscreen", "m3_fullscreen_system" -> FullscreenSystemWidget(
            telemetry = telemetry,
            modifier = modifier,
            config = cfg
        )
        "m3_system_hub", "quick_stats", "system_hub" -> {
            if (widget.effectiveHeight >= 2) {
                FullscreenSystemWidget(
                    telemetry = telemetry,
                    modifier = modifier,
                    config = cfg
                )
            } else {
                SystemHubWidget(telemetry = telemetry, modifier = modifier, config = cfg)
            }
        }
        "m3_gauge_card" -> DualGaugeWidget(dataCpu = telemetry.cpu, dataGpu = telemetry.gpu, modifier = modifier, config = cfg)
        "volume_card", "system_volume", "volume_control" -> VolumeWidget(
            data = telemetry.audio,
            onSystemControl = { action, value -> onSystemControl(action, value, "") },
            modifier = modifier,
            config = cfg
        )

        // --- 3. APP SHORTCUT & QUICK LAUNCHER WIDGETS ---
        "app_shortcut", "shortcut_card", "app_launcher", "quick_action" -> AppShortcutWidget(
            config = cfg,
            serverBaseUrl = serverBaseUrl,
            onSystemControl = onSystemControl,
            modifier = modifier
        )

        // --- 4. MEDIA & PIXELPLAYER WIDGETS ---
        "media_fullscreen_m3", "media_hero_player", "media_fullscreen" -> HeroMediaWidget(
            data = telemetry.media,
            serverBaseUrl = serverBaseUrl,
            onMediaControl = onMediaControl,
            modifier = modifier,
            config = cfg
        )
        "media_vinyl" -> VinylMediaWidget(
            data = telemetry.media,
            serverBaseUrl = serverBaseUrl,
            onMediaControl = onMediaControl,
            modifier = modifier,
            config = cfg
        )
        "media_card", "media_m3_expressive" -> {
            if (widget.effectiveHeight >= 2) {
                HeroMediaWidget(
                    data = telemetry.media,
                    serverBaseUrl = serverBaseUrl,
                    onMediaControl = onMediaControl,
                    modifier = modifier,
                    config = cfg
                )
            } else {
                CompactMediaWidget(
                    data = telemetry.media,
                    serverBaseUrl = serverBaseUrl,
                    onMediaControl = onMediaControl,
                    modifier = modifier,
                    config = cfg
                )
            }
        }

        // --- FALLBACK ---
        else -> CpuWidget(data = telemetry.cpu, modifier = modifier, config = cfg)
    }
}
