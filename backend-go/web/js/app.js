// ==============================================================================
// MOBILEDASHBOARD ADMIN PANEL - MAIN ENTRY POINT
// ==============================================================================

import { state } from './state.js';
import { api } from './services/api.js';
import { initWebSocket } from './services/websocket.js';
import { initHeader } from './components/header.js';
import { initCatalog } from './components/catalog.js';
import { initCanvas } from './components/canvas.js';
import { initPreview } from './components/preview.js';
import { initSettingsModal } from './modals/settingsModal.js';
import { initClientModal } from './modals/clientModal.js';

async function bootstrap() {
    console.log('[MobileDashboard Admin] Modular Architecture Initializing...');

    try {
        // 1. Initialize Components & Modals
        initHeader();
        initCatalog();
        initCanvas();
        initPreview();
        initSettingsModal();
        initClientModal();

        // 2. Load Initial Layouts
        const layouts = await api.fetchLayouts();
        console.log('[MobileDashboard Admin] Layouts fetched:', layouts);
        if (layouts && layouts.length > 0) {
            state.setLayouts(layouts);
        } else {
            // Fallback default page if server had empty layouts
            state.setLayouts([
                {
                    id: 'page_' + Date.now(),
                    title: 'Genel Görünüm',
                    icon: '📱',
                    theme: 'nord',
                    widgets: [
                        { widget_id: 'clock_fullscreen_m3', x: 0, y: 0, w: 4, h: 3, span: 4, row_span: 3, config: { font_scale: 'medium', accent_color: 'nord', show_seconds: true, show_date: true } }
                    ]
                }
            ]);
        }

        // 3. Load Initial Clients Data
        const clients = await api.fetchClients();
        console.log('[MobileDashboard Admin] Clients fetched:', clients);
        if (clients) {
            state.setClientsData(clients);
        }

        // 4. Start Real-time WebSocket connection
        initWebSocket();

        console.log('[MobileDashboard Admin] Ready.');
    } catch (err) {
        console.error('[MobileDashboard Admin Init Error]:', err);
    }
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', bootstrap);
} else {
    bootstrap();
}
