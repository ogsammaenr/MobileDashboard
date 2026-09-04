package com.mobiledashboard.app.data.model

import androidx.compose.ui.graphics.Color
import com.mobiledashboard.app.ui.theme.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable
data class PageLayout(
    val id: String = "",
    val title: String = "Sayfa",
    val icon: String = "📊",
    val theme: String = "cyan",
    val widgets: List<WidgetInstance> = emptyList()
)

/**
 * 🧱 Generic Metadata + Dynamic Parameter Map Architecture.
 * Universal layout/styling options stay top-level, while widget-specific options live inside params.
 */
@Serializable(with = WidgetConfigSerializer::class)
data class WidgetConfig(
    val customTitle: String = "",
    val fontScale: String = "medium", // small, medium, large, xlarge
    val accentColor: String = "cyan",
    val shapeStyle: String = "rounded", // rounded, segmented_pills, pill, scalloped, clover, asymmetric
    val params: JsonObject = JsonObject(emptyMap()) // Dynamic widget-specific parameters map
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

    // Dynamic parameter access helpers
    fun getBool(key: String, default: Boolean = true): Boolean {
        val elem = params[key] ?: return default
        return (elem as? JsonPrimitive)?.booleanOrNull ?: default
    }

    fun getString(key: String, default: String = ""): String {
        val elem = params[key] ?: return default
        return (elem as? JsonPrimitive)?.contentOrNull ?: default
    }

    fun getInt(key: String, default: Int = 0): Int {
        val elem = params[key] ?: return default
        return (elem as? JsonPrimitive)?.intOrNull ?: default
    }

    fun getDouble(key: String, default: Double = 0.0): Double {
        val elem = params[key] ?: return default
        return (elem as? JsonPrimitive)?.doubleOrNull ?: default
    }

    // Convenience property getters for standard widgets (cleanly backed by dynamic params)
    val showSeconds: Boolean get() = getBool("show_seconds", true)
    val showDate: Boolean get() = getBool("show_date", true)
    val showTemp: Boolean get() = getBool("show_temp", true)
    val showBar: Boolean get() = getBool("show_bar", true)
    val showBadge: Boolean get() = getBool("show_badge", true)
    val is12Hour: Boolean get() = getBool("is_12hour", false)
    val blurBackground: Boolean get() = getBool("blur_background", true)
    val appId: String get() = getString("app_id", "spotify")
    val appPath: String get() = getString("app_path", "")
    val appCommand: String get() = getString("app_command", "")
    val appIcon: String get() = getString("app_icon", "")
    val appIconUrl: String get() = getString("app_icon_url", "")
}

object WidgetConfigSerializer : KSerializer<WidgetConfig> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("WidgetConfig")

    override fun deserialize(decoder: Decoder): WidgetConfig {
        val input = decoder as? JsonDecoder ?: return WidgetConfig()
        val jsonElement = input.decodeJsonElement()
        if (jsonElement !is JsonObject) return WidgetConfig()

        val customTitle = jsonElement["custom_title"]?.jsonPrimitive?.contentOrNull ?: ""
        val fontScale = jsonElement["font_scale"]?.jsonPrimitive?.contentOrNull ?: "medium"
        val accentColor = jsonElement["accent_color"]?.jsonPrimitive?.contentOrNull ?: "cyan"
        val shapeStyle = jsonElement["shape_style"]?.jsonPrimitive?.contentOrNull ?: "rounded"

        val paramsMap = mutableMapOf<String, JsonElement>()
        val nestedParams = jsonElement["params"]?.let { if (it is JsonObject) it else null }
        if (nestedParams != null) {
            paramsMap.putAll(nestedParams)
        }

        // Backward compatibility: merge any legacy top-level non-metadata keys into params
        val knownMeta = setOf("custom_title", "font_scale", "accent_color", "shape_style", "params")
        for ((k, v) in jsonElement) {
            if (k !in knownMeta && k !in paramsMap) {
                paramsMap[k] = v
            }
        }

        return WidgetConfig(
            customTitle = customTitle,
            fontScale = fontScale,
            accentColor = accentColor,
            shapeStyle = shapeStyle,
            params = JsonObject(paramsMap)
        )
    }

    override fun serialize(encoder: Encoder, value: WidgetConfig) {
        val output = encoder as? JsonEncoder ?: return
        val json = buildJsonObject {
            if (value.customTitle.isNotEmpty()) put("custom_title", value.customTitle)
            put("font_scale", value.fontScale)
            put("accent_color", value.accentColor)
            put("shape_style", value.shapeStyle)
            if (value.params.isNotEmpty()) {
                put("params", value.params)
            }
        }
        output.encodeJsonElement(json)
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
