package com.mobiledashboard.app.data.model

import android.content.pm.ActivityInfo

/**
 * Screen orientation options for MobileDashboard client.
 */
enum class ScreenOrientation(
    val id: String,
    val title: String,
    val activityInfoOrientation: Int
) {
    AUTO("auto", "Otomatik", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED),
    PORTRAIT("portrait", "Dikey", ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT),
    LANDSCAPE("landscape", "Yatay", ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

    companion object {
        fun fromId(id: String?): ScreenOrientation {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: AUTO
        }
    }
}
