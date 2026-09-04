package com.mobiledashboard.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiledashboard.app.data.model.ConnectionStatus
import com.mobiledashboard.app.data.model.DashboardTheme
import com.mobiledashboard.app.data.model.DiscoveredServer
import com.mobiledashboard.app.data.model.ScreenOrientation
import com.mobiledashboard.app.data.model.TelemetryPayload
import com.mobiledashboard.app.data.repository.DiscoveryRepository
import com.mobiledashboard.app.data.repository.TelemetryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val telemetryRepo: TelemetryRepository = TelemetryRepository(),
    private val discoveryRepo: DiscoveryRepository = DiscoveryRepository()
) : ViewModel() {

    val telemetry: StateFlow<TelemetryPayload> = telemetryRepo.telemetry
    val layouts: StateFlow<List<com.mobiledashboard.app.data.model.PageLayout>> = telemetryRepo.layouts
    val connectionStatus: StateFlow<ConnectionStatus> = telemetryRepo.connectionStatus

    private val _discoveredServers = MutableStateFlow<List<DiscoveredServer>>(emptyList())
    val discoveredServers: StateFlow<List<DiscoveredServer>> = _discoveredServers.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _selectedTheme = MutableStateFlow(DashboardTheme.NORD)
    val selectedTheme: StateFlow<DashboardTheme> = _selectedTheme.asStateFlow()

    private val _screenBrightness = MutableStateFlow(0.8f)
    val screenBrightness: StateFlow<Float> = _screenBrightness.asStateFlow()

    private val _screenOrientation = MutableStateFlow(ScreenOrientation.AUTO)
    val screenOrientation: StateFlow<ScreenOrientation> = _screenOrientation.asStateFlow()

    private val _currentHost = MutableStateFlow("")
    val currentHost: StateFlow<String> = _currentHost.asStateFlow()

    private val _currentPort = MutableStateFlow(8000)
    val currentPort: StateFlow<Int> = _currentPort.asStateFlow()

    private var scanJob: Job? = null

    init {
        viewModelScope.launch {
            layouts.collect { pageList ->
                val firstTheme = pageList.firstOrNull()?.theme
                if (!firstTheme.isNullOrBlank()) {
                    _selectedTheme.value = DashboardTheme.fromId(firstTheme)
                }
            }
        }
        startDiscovery()
    }

    fun startDiscovery() {
        scanJob?.cancel()
        _discoveredServers.value = emptyList()
        _isScanning.value = true

        scanJob = viewModelScope.launch {
            discoveryRepo.discoverServers().collect { server ->
                val current = _discoveredServers.value.toMutableList()
                if (current.none { it.ip == server.ip && it.port == server.port }) {
                    current.add(server)
                    _discoveredServers.value = current
                    
                    // Auto-connect to first discovered PC if not already connected
                    if (_currentHost.value.isBlank()) {
                        connectToServer(server.ip, server.port)
                    }
                }
            }
            delay(3000)
            _isScanning.value = false
        }
    }

    fun connectToServer(ip: String, port: Int = 8000) {
        _currentHost.value = ip
        _currentPort.value = port
        telemetryRepo.connect(ip, port)
    }

    fun reconnect() {
        if (_currentHost.value.isNotBlank()) {
            telemetryRepo.connect(_currentHost.value, _currentPort.value)
        }
    }

    fun disconnectAndScan() {
        telemetryRepo.disconnect()
        _currentHost.value = ""
        startDiscovery()
    }

    fun controlMedia(action: String) {
        viewModelScope.launch {
            telemetryRepo.controlMedia(action)
        }
    }

    fun controlSystem(action: String, value: Int = 0, target: String = "") {
        viewModelScope.launch {
            telemetryRepo.controlSystem(action, value, target)
        }
    }

    fun setTheme(theme: DashboardTheme) {
        _selectedTheme.value = theme
    }

    fun setBrightness(value: Float) {
        _screenBrightness.value = value.coerceIn(0.05f, 1f)
    }

    fun setScreenOrientation(orientation: ScreenOrientation) {
        _screenOrientation.value = orientation
    }

    override fun onCleared() {
        super.onCleared()
        telemetryRepo.disconnect()
    }
}
