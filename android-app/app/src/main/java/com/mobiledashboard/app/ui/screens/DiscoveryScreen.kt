package com.mobiledashboard.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledashboard.app.data.model.DiscoveredServer
import com.mobiledashboard.app.ui.theme.*

@Composable
fun DiscoveryScreen(
    discoveredServers: List<DiscoveredServer>,
    isScanning: Boolean,
    onServerSelected: (String, Int) -> Unit,
    onRescan: () -> Unit,
    modifier: Modifier = Modifier
) {
    var manualIp by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 20.dp)
            ) {
                // Radar Pulse Circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(if (isScanning) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(AccentCyan.copy(alpha = 0.15f))
                        .border(1.dp, AccentCyan.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📡", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Masaüstü PC Aranıyor...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Text(
                    text = "Aynı Wi-Fi ağına bağlı Go sunucusu otomatik olarak algılanacaktır.",
                    fontSize = 12.sp,
                    color = TextSub,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // Discovered Servers List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (discoveredServers.isNotEmpty()) {
                    Text(
                        text = "BULUNAN BİLGİSAYARLAR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan,
                        letterSpacing = 1.sp
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(discoveredServers) { server ->
                            DiscoveredServerCard(
                                server = server,
                                onClick = { onServerSelected(server.ip, server.port) }
                            )
                        }
                    }
                } else if (!isScanning) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Ağda açık bir PC bulunamadı. Lütfen PC'de ./start.sh'nin çalıştığından emin olun.", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
                    }
                }
            }

            // Manual IP Entry & Scan Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = manualIp,
                    onValueChange = { manualIp = it },
                    label = { Text("Veya Manuel IP Girin (Örn: 192.168.1.35)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextMain,
                        unfocusedTextColor = TextMain,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSub
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRescan,
                        colors = ButtonDefaults.buttonColors(containerColor = SubCardBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("🔄 Yeniden Tara", fontSize = 13.sp, color = TextMain)
                    }

                    Button(
                        onClick = {
                            if (manualIp.isNotBlank()) {
                                onServerSelected(manualIp.trim(), 8000)
                            }
                        },
                        enabled = manualIp.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Bağlan ➜", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmoledBlack)
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoveredServerCard(
    server: DiscoveredServer,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkCardBg)
            .border(1.dp, AccentCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("💻", fontSize = 24.sp)
            Column {
                Text(server.hostname, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextMain)
                Text("${server.ip}:${server.port}", fontSize = 12.sp, color = TextSub)
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AccentCyan)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text("Bağlan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmoledBlack)
        }
    }
}
