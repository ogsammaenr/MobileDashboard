package com.mobiledashboard.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mobiledashboard.app.data.model.ConnectionStatus
import com.mobiledashboard.app.data.model.DashboardTheme
import com.mobiledashboard.app.data.model.ScreenOrientation
import com.mobiledashboard.app.ui.components.navigation.TopControlBar

/**
 * Wrapper for TopControlBar popup.
 */
@Composable
fun TopOverlayBar(
    isOpen: Boolean,
    onClose: () -> Unit,
    connectionStatus: ConnectionStatus,
    serverHost: String,
    currentTheme: DashboardTheme,
    brightness: Float,
    orientation: ScreenOrientation,
    onBrightnessChange: (Float) -> Unit,
    onOrientationChange: (ScreenOrientation) -> Unit,
    onThemeChange: (DashboardTheme) -> Unit,
    onSystemControl: (String) -> Unit,
    onReconnect: () -> Unit,
    onChangeServer: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopControlBar(
        isOpen = isOpen,
        onClose = onClose,
        connectionStatus = connectionStatus,
        serverHost = serverHost,
        currentTheme = currentTheme,
        brightness = brightness,
        orientation = orientation,
        onBrightnessChange = onBrightnessChange,
        onOrientationChange = onOrientationChange,
        onThemeChange = onThemeChange,
        onSystemControl = onSystemControl,
        onReconnect = onReconnect,
        onChangeServer = onChangeServer,
        modifier = modifier
    )
}
