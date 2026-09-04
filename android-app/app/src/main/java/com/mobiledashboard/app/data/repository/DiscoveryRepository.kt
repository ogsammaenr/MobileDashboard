package com.mobiledashboard.app.data.repository

import android.util.Log
import com.mobiledashboard.app.data.model.DiscoveredServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

class DiscoveryRepository {

    companion object {
        private const val TAG = "MobileDashboard_Discovery"
        private const val DISCOVERY_PORT = 8001
        private const val PING_MSG = "MOBILEDASHBOARD_DISCOVERY_PING"
        private const val TIMEOUT_MS = 2500
    }

    fun discoverServers(): Flow<DiscoveredServer> = flow {
        val foundServers = mutableSetOf<String>()
        var socket: DatagramSocket? = null

        Log.d(TAG, "📡 UDP Sunucu Keşfi Başlatılıyor (Port: $DISCOVERY_PORT, Timeout: ${TIMEOUT_MS}ms)...")

        try {
            socket = DatagramSocket().apply {
                broadcast = true
                soTimeout = TIMEOUT_MS
            }

            val sendData = PING_MSG.toByteArray()
            val broadcastAddr = InetAddress.getByName("255.255.255.255")
            val sendPacket = DatagramPacket(sendData, sendData.size, broadcastAddr, DISCOVERY_PORT)

            // Send 3 ping packets in rapid succession for reliability on Wi-Fi
            repeat(3) { i ->
                socket.send(sendPacket)
                Log.d(TAG, "📤 PING paketi #${i + 1} 255.255.255.255:$DISCOVERY_PORT adresine gönderildi.")
            }

            val receiveBuf = ByteArray(1024)
            val receivePacket = DatagramPacket(receiveBuf, receiveBuf.size)
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < TIMEOUT_MS) {
                try {
                    socket.receive(receivePacket)
                    val senderIp = receivePacket.address.hostAddress ?: continue
                    val responseStr = String(receivePacket.data, 0, receivePacket.length).trim()

                    Log.d(TAG, "📥 UDP Yanıtı Alındı: $senderIp -> $responseStr")

                    if (responseStr.startsWith("MOBILEDASHBOARD_DISCOVERY_PONG")) {
                        var port = 8000
                        var hostname = "Masaüstü PC"
                        var os = "linux"

                        val tokens = responseStr.split("|")
                        for (token in tokens) {
                            if (token.startsWith("PORT=")) {
                                port = token.removePrefix("PORT=").toIntOrNull() ?: 8000
                            } else if (token.startsWith("HOSTNAME=")) {
                                hostname = token.removePrefix("HOSTNAME=")
                            } else if (token.startsWith("OS=")) {
                                os = token.removePrefix("OS=")
                            }
                        }

                        val key = "$senderIp:$port"
                        if (!foundServers.contains(key)) {
                            foundServers.add(key)
                            Log.d(TAG, "✅ Yeni Sunucu Keşfedildi: $hostname ($key, OS: $os)")
                            emit(DiscoveredServer(ip = senderIp, port = port, hostname = hostname, os = os))
                        }
                    }
                } catch (e: SocketTimeoutException) {
                    Log.d(TAG, "⏱️ UDP Keşif soketi zaman aşımı (${TIMEOUT_MS}ms).")
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ UDP Keşif Hatası: ${e.message}", e)
        } finally {
            socket?.close()
            Log.d(TAG, "🏁 UDP Keşif tamamlandı. Toplam bulunan sunucu: ${foundServers.size}")
        }
    }.flowOn(Dispatchers.IO)
}
