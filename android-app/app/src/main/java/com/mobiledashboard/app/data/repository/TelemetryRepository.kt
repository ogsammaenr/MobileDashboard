package com.mobiledashboard.app.data.repository

import android.os.Build
import com.mobiledashboard.app.data.model.ConnectionStatus
import com.mobiledashboard.app.data.model.PageLayout
import com.mobiledashboard.app.data.model.TelemetryPayload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class TelemetryRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // WebSocket için readTimeout mutlaka 0 (limitsiz) olmalıdır!
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .pingInterval(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // Kalıcı ve sabit Cihaz Kimliği (UUID/Fingerprint)
    val deviceId: String by lazy {
        val model = Build.MODEL.replace("[^a-zA-Z0-9_]".toRegex(), "_")
        val brand = Build.BRAND.replace("[^a-zA-Z0-9_]".toRegex(), "_")
        val fingerprintHash = Math.abs(Build.FINGERPRINT.hashCode()).toString()
        "android_${brand}_${model}_$fingerprintHash"
    }

    val deviceName: String by lazy {
        val brand = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        "$brand ${Build.MODEL}"
    }

    // Sticky Telemetry Cache: Bağlantı kopsa dahi son telemetri asla sıfırlanmaz!
    private val _telemetry = MutableStateFlow(TelemetryPayload())
    val telemetry: StateFlow<TelemetryPayload> = _telemetry.asStateFlow()

    private val _layouts = MutableStateFlow<List<PageLayout>>(emptyList())
    val layouts: StateFlow<List<PageLayout>> = _layouts.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private var currentWebSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var isManualDisconnect = false
    private val scope = CoroutineScope(Dispatchers.IO)
    private var currentHost: String = ""
    private var currentPort: Int = 8000

    fun connect(host: String, port: Int = 8000) {
        currentHost = host
        currentPort = port
        reconnectJob?.cancel()

        isManualDisconnect = true
        currentWebSocket?.close(1000, "Reconnecting")
        currentWebSocket = null
        isManualDisconnect = false

        _connectionStatus.value = ConnectionStatus.CONNECTING

        fetchLayouts()

        val encodedId = URLEncoder.encode(deviceId, "UTF-8")
        val encodedName = URLEncoder.encode(deviceName, "UTF-8")
        val wsUrl = "ws://$host:$port/ws?device_id=$encodedId&device_name=$encodedName&role=client"
        val request = Request.Builder().url(wsUrl).build()

        android.util.Log.d("MobileDashboard_Telemetry", "🔌 WebSocket Sunucusuna Bağlanılıyor: $wsUrl")

        currentWebSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                android.util.Log.d("MobileDashboard_Telemetry", "🟢 WebSocket Bağlantısı Kuruldu! HTTP: ${response.code}")
                _connectionStatus.value = ConnectionStatus.CONNECTED
                fetchLayouts()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    if (text.contains("PAIRING_PENDING")) {
                        android.util.Log.d("MobileDashboard_Telemetry", "🟡 Eşleşme Onayı Bekleniyor...")
                        _connectionStatus.value = ConnectionStatus.CONNECTING
                        return
                    }
                    if (text.contains("PAIRING_APPROVED")) {
                        android.util.Log.d("MobileDashboard_Telemetry", "🟢 Eşleşme Onaylandı! Telemetri akışı başlıyor.")
                        _connectionStatus.value = ConnectionStatus.CONNECTED
                        fetchLayouts()
                        return
                    }
                    if (text.contains("PAIRING_REJECTED") || text.contains("PAIRING_TIMEOUT")) {
                        android.util.Log.w("MobileDashboard_Telemetry", "🔴 Eşleşme Reddedildi veya Zaman Aşımı!")
                        _connectionStatus.value = ConnectionStatus.DISCONNECTED
                        return
                    }
                    if (text.contains("LAYOUT_UPDATED")) {
                        android.util.Log.d("MobileDashboard_Telemetry", "🔄 Sunucudan LAYOUT_UPDATED sinyali alındı.")
                        fetchLayouts()
                        return
                    }
                    val payload = json.decodeFromString<TelemetryPayload>(text)
                    _telemetry.value = payload
                } catch (e: Exception) {
                    android.util.Log.e("MobileDashboard_Telemetry", "⚠️ Telemetri JSON parse hatası: ${e.message}")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                android.util.Log.d("MobileDashboard_Telemetry", "🟡 WebSocket Kapanıyor (Code: $code, Reason: $reason)")
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                android.util.Log.d("MobileDashboard_Telemetry", "🔴 WebSocket Kapandı (Code: $code, Reason: $reason)")
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                if (!isManualDisconnect) {
                    scheduleReconnect()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                android.util.Log.e("MobileDashboard_Telemetry", "❌ WebSocket Bağlantı Hatası: ${t.message} (HTTP: ${response?.code})", t)
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                if (!isManualDisconnect) {
                    scheduleReconnect()
                }
            }
        })
    }

    private fun scheduleReconnect() {
        if (currentHost.isBlank()) return
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(1500)
            if (_connectionStatus.value == ConnectionStatus.DISCONNECTED && !isManualDisconnect) {
                connect(currentHost, currentPort)
            }
        }
    }

    fun disconnect() {
        isManualDisconnect = true
        reconnectJob?.cancel()
        currentWebSocket?.close(1000, "App disconnect")
        currentWebSocket = null
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
    }

    suspend fun controlMedia(action: String) = withContext(Dispatchers.IO) {
        if (currentHost.isBlank()) return@withContext
        try {
            val url = "http://$currentHost:$currentPort/api/media/control"
            val body = """{"action":"$action"}""".toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()
            client.newCall(request).execute().close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun controlSystem(action: String, value: Int = 0, target: String = "") = withContext(Dispatchers.IO) {
        if (currentHost.isBlank()) return@withContext
        try {
            val url = "http://$currentHost:$currentPort/api/system/control"
            val json = when {
                target.isNotBlank() -> """{"action":"$action","target":"$target"}"""
                value > 0 || action == "set-volume" -> """{"action":"$action","value":$value}"""
                else -> """{"action":"$action"}"""
            }
            val body = json.toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()
            client.newCall(request).execute().close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchLayouts() {
        if (currentHost.isBlank()) return
        scope.launch {
            try {
                val url = "http://$currentHost:$currentPort/api/layouts?_t=${System.currentTimeMillis()}"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val bodyStr = response.body?.string() ?: ""
                response.close()
                if (bodyStr.isNotBlank()) {
                    val newList = json.decodeFromString<List<PageLayout>>(bodyStr)
                    // Akıllı Diff: Yalnızca layout listesi gerçekten değişmişse StateFlow'u güncelle!
                    // Böylece gereksiz recomposition ve ekranın yanıp sönmesi önlenir.
                    if (_layouts.value != newList) {
                        _layouts.value = newList
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
