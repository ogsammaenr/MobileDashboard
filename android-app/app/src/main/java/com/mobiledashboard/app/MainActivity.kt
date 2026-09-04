package com.mobiledashboard.app

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mobiledashboard.app.data.model.ConnectionStatus
import com.mobiledashboard.app.ui.components.TopOverlayBar
import com.mobiledashboard.app.ui.screens.DashboardScreen
import com.mobiledashboard.app.ui.screens.DiscoveryScreen
import com.mobiledashboard.app.ui.theme.MobileDashboardTheme
import com.mobiledashboard.app.viewmodel.DashboardViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. HARDWARE LEVEL: KEEP SCREEN ON (Ekran Asla Kapanmaz)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 2. EXTEND INTO DISPLAY CUTOUT / CAMERA NOTCH AREA (Kamera Alanına Tam Yayılma)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        // 3. EDGE-TO-EDGE IMMERSIVE FULLSCREEN (Çerçevesiz Tam Ekran)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()

        setContent {
            val theme by viewModel.selectedTheme.collectAsState()
            val telemetry by viewModel.telemetry.collectAsState()
            val layouts by viewModel.layouts.collectAsState()
            val connectionStatus by viewModel.connectionStatus.collectAsState()
            val currentHost by viewModel.currentHost.collectAsState()
            val currentPort by viewModel.currentPort.collectAsState()
            val discoveredServers by viewModel.discoveredServers.collectAsState()
            val isScanning by viewModel.isScanning.collectAsState()
            val brightness by viewModel.screenBrightness.collectAsState()
            val orientation by viewModel.screenOrientation.collectAsState()

            var isSettingsOpen by remember { mutableStateOf(false) }

            // Apply screen orientation setting to activity
            LaunchedEffect(orientation) {
                requestedOrientation = orientation.activityInfoOrientation
            }

            // Update physical window screen brightness
            val lp = window.attributes
            if (lp.screenBrightness != brightness) {
                lp.screenBrightness = brightness
                window.attributes = lp
            }

            MobileDashboardTheme(theme = theme) {
                // 3-Finger Tap Detector on Root Container
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    val pressedPointersCount = event.changes.count { it.pressed }
                                    if (pressedPointersCount >= 3) {
                                        isSettingsOpen = true
                                    }
                                }
                            }
                        }
                ) {
                    if (currentHost.isBlank() && connectionStatus == ConnectionStatus.DISCONNECTED) {
                        DiscoveryScreen(
                            discoveredServers = discoveredServers,
                            isScanning = isScanning,
                            onServerSelected = { ip, port -> viewModel.connectToServer(ip, port) },
                            onRescan = { viewModel.startDiscovery() }
                        )
                    } else {
                        val serverBaseUrl = "http://$currentHost:$currentPort"
                        DashboardScreen(
                            telemetry = telemetry,
                            layouts = layouts,
                            serverBaseUrl = serverBaseUrl,
                            onMediaControl = { viewModel.controlMedia(it) },
                            onSystemControl = { action, value, target -> viewModel.controlSystem(action, value, target) }
                        )

                        TopOverlayBar(
                            isOpen = isSettingsOpen,
                            onClose = { isSettingsOpen = false },
                            connectionStatus = connectionStatus,
                            serverHost = "$currentHost:$currentPort",
                            currentTheme = theme,
                            brightness = brightness,
                            orientation = orientation,
                            onBrightnessChange = { viewModel.setBrightness(it) },
                            onOrientationChange = { viewModel.setScreenOrientation(it) },
                            onThemeChange = { viewModel.setTheme(it) },
                            onSystemControl = { viewModel.controlSystem(it) },
                            onReconnect = { viewModel.reconnect() },
                            onChangeServer = { viewModel.disconnectAndScan() }
                        )
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun hideSystemBars() {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}
