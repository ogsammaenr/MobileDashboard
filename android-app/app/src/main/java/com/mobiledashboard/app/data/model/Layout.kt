package com.mobiledashboard.app.data.model

import androidx.compose.ui.graphics.Color
import com.mobiledashboard.app.ui.theme.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PageLayout(
    val id: String = "",
    val title: String = "Sayfa",
    val icon: String = "📊",
    val theme: String = "cyan",
    val widgets: List<WidgetInstance> = emptyList()
)

@Serializable
data class WidgetConfig(
    @SerialName("custom_title") val customTitle: String = "",
    @SerialName("font_scale") val fontScale: String = "medium", // small, medium, large, xlarge
    @SerialName("accent_color") val accentColor: String = "cyan",
    @SerialName("shape_style") val shapeStyle: String = "rounded", // rounded, segmented_pills, pill, scalloped, clover, asymmetric
    @SerialName("app_id") val appId: String = "spotify",
    @SerialName("app_path") val appPath: String = "",
    @SerialName("app_command") val appCommand: String = "",
    @SerialName("app_icon") val appIcon: String = "",
    @SerialName("app_icon_url") val appIconUrl: String = "",
    @SerialName("show_seconds") val showSeconds: Boolean = true,
    @SerialName("show_date") val showDate: Boolean = true,
    @SerialName("show_temp") val showTemp: Boolean = true,
    @SerialName("show_bar") val showBar: Boolean = true,
    @SerialName("show_badge") val showBadge: Boolean = true,
    @SerialName("is_12hour") val is12Hour: Boolean = false,
    @SerialName("blur_background") val blurBackground: Boolean = true
) {
    val scaleMultiplier: Float get() = when (fontScale) {
        "small" -> 0.82f
        "large" -> 1.25f
        "xlarge" -> 1.55f
        else -> 1.0f
    }

    val customAccentColor: Color? get() = when (accentColor) {
        "nord" -> NordAccent
        "catppuccin" -> CatppuccinAccent
        "everforest" -> EverforestAccent
        "tokyonight" -> TokyoNightAccent
        "gruvbox" -> GruvboxAccent
        "monochrome" -> MonochromeAccent
        "rosepine" -> RosePineAccent
        "cyan" -> NordAccent
        "purple" -> CatppuccinAccent
        "green" -> EverforestAccent
        "blue" -> TokyoNightAccent
        "amber" -> GruvboxAccent
        "white" -> MonochromeAccent
        "red" -> RosePineAccent
        else -> null
    }
}

@Serializable
data class WidgetInstance(
    @SerialName("widget_id") val widgetId: String,
    val x: Int = 0,
    val y: Int = 0,
    val w: Int = 2,
    val h: Int = 1,
    val span: Int = 2,
    @SerialName("row_span") val rowSpan: Int = 1,
    val config: WidgetConfig = WidgetConfig()
) {
    val effectiveWidth: Int get() = if (w > 0) w else span
    val effectiveHeight: Int get() = if (h > 0) h else rowSpan
}
