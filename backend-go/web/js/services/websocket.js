// ==============================================================================
// LIVE WEBSOCKET SERVICE & AUTO-RECONNECT
// ==============================================================================

import { state } from '../state.js';
import { api } from './api.js';

let socket = null;
let reconnectTimer = null;

export function initWebSocket() {
    if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
        return;
    }

    const proto = location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${proto}//${location.host}/ws?role=admin&device_name=Web%20Admin`;

    try {
        socket = new WebSocket(wsUrl);

        socket.onopen = () => {
            console.log('[WebSocket] Admin panel live connection established.');
            if (reconnectTimer) {
                clearTimeout(reconnectTimer);
                reconnectTimer = null;
            }
        };

        socket.onmessage = async (event) => {
            try {
                const msg = JSON.parse(event.data);
                
                // 1. Client status update (pairing request, connect, disconnect)
                if (msg.type === 'CLIENTS_UPDATED') {
                    const clients = await api.fetchClients();
                    if (clients) {
                        state.setClientsData(clients);
                    }
                }

                // 2. Remote Layout update
                if (msg.type === 'LAYOUT_UPDATED') {
                    const layouts = await api.fetchLayouts();
                    state.setLayouts(layouts);
                }

                // 3. Remote Theme update
                if (msg.type === 'THEME_UPDATED') {
                    state.emit('theme:received', msg.theme);
                }

                // 4. Live Telemetry
                if (msg.cpu && msg.ram) {
                    state.emit('telemetry:stream', msg);
                }
            } catch (err) {
                console.error('[WebSocket Message Parse Error]:', err);
            }
        };

        socket.onclose = () => {
            socket = null;
            scheduleReconnect();
        };

        socket.onerror = () => {
            if (socket) socket.close();
        };
    } catch (e) {
        console.error('[WebSocket Init Error]:', e);
        scheduleReconnect();
    }
}

function scheduleReconnect() {
    if (reconnectTimer) return;
    reconnectTimer = setTimeout(() => {
        reconnectTimer = null;
        initWebSocket();
    }, 1500);
}
