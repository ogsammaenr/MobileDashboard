package com.mobiledashboard.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TelemetryPayload(
    val timestamp: Long = 0L,
    val cpu: CPUData = CPUData(),
    val gpu: GPUData = GPUData(),
    val ram: RAMData = RAMData(),
    val disk: DiskData = DiskData(),
    val network: NetworkData = NetworkData(),
    val media: MediaData = MediaData(),
    val audio: AudioData = AudioData()
)

@Serializable
data class AudioData(
    @SerialName("volume_percent") val volumePercent: Int = 50,
    @SerialName("is_muted") val isMuted: Boolean = false
)

@Serializable
data class CPUData(
    val percent: Double = 0.0,
    val temp: Double = 0.0
)

@Serializable
data class GPUData(
    val percent: Double = 0.0,
    val temp: Double = 0.0,
    val name: String = "GPU",
    @SerialName("memory_used_mb") val memoryUsedMb: Double = 0.0,
    @SerialName("memory_total_mb") val memoryTotalMb: Double = 0.0
)

@Serializable
data class RAMData(
    val percent: Double = 0.0,
    @SerialName("used_gb") val usedGb: Double = 0.0,
    @SerialName("total_gb") val totalGb: Double = 0.0,
    @SerialName("free_gb") val freeGb: Double = 0.0
)

@Serializable
data class DiskData(
    val percent: Double = 0.0,
    @SerialName("used_gb") val usedGb: Double = 0.0,
    @SerialName("total_gb") val totalGb: Double = 0.0,
    @SerialName("free_gb") val freeGb: Double = 0.0
)

@Serializable
data class NetworkData(
    @SerialName("down_kbps") val downKbps: Double = 0.0,
    @SerialName("up_kbps") val upKbps: Double = 0.0
)

@Serializable
data class MediaData(
    val title: String = "Çalan Medya Yok",
    val artist: String = "--",
    val album: String = "--",
    val status: String = "Stopped",
    @SerialName("art_url") val artUrl: String = "",
    @SerialName("position_sec") val positionSec: Long = 0L,
    @SerialName("length_sec") val lengthSec: Long = 0L
)

data class DiscoveredServer(
    val ip: String,
    val port: Int = 8000,
    val hostname: String = "Desktop PC",
    val os: String = "linux"
)

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

enum class DashboardTheme(
    val id: String,
    val title: String,
    val primaryHex: Long,
    val bgHex: Long,
    val fgHex: Long,
    val cardBgHex: Long
) {
    NORD("nord", "Nord", 0xFF88C0D0, 0xFF2E3440, 0xFFECEFF4, 0xFF3B4252),
    CATPPUCCIN("catppuccin", "Catppuccin Macchiato", 0xFFC6A0F6, 0xFF24273A, 0xFFCAD3F5, 0xFF363A4F),
    EVERFOREST("everforest", "Everforest Dark", 0xFFA7C080, 0xFF2D353B, 0xFFD3C6AA, 0xFF343F44),
    TOKYONIGHT("tokyonight", "Tokyo Night", 0xFF7AA2F7, 0xFF1A1B26, 0xFFC0CAF5, 0xFF24283B),
    GRUVBOX("gruvbox", "Gruvbox Dark", 0xFFFE8019, 0xFF282828, 0xFFEBDBB2, 0xFF3C3836),
    MONOCHROME("monochrome", "Monochrome Minimal", 0xFFE0E0E0, 0xFF121212, 0xFFF0F0F0, 0xFF1E1E1E),
    ROSEPINE("rosepine", "Rosé Pine", 0xFFEBBCBA, 0xFF191724, 0xFFE0DEF4, 0xFF1F1D2E);

    companion object {
        fun fromId(id: String?): DashboardTheme {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: NORD
        }
    }
}
