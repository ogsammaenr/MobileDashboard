// ==============================================================================
// HEADER COMPONENT
// ==============================================================================

import { state } from '../state.js';
import { api } from '../services/api.js';
import { modalManager } from '../modals/modalManager.js';

export function initHeader() {
    const saveBtn = document.getElementById('saveBtn');
    const clientBtn = document.getElementById('clientManagerBtn');

    if (saveBtn) {
        saveBtn.addEventListener('click', async () => {
            try {
                saveBtn.disabled = true;
                saveBtn.innerHTML = `<span>⏳</span> <span>Kaydediliyor...</span>`;
                
                await api.saveLayouts(state.layouts);
                showToast('✓ Düzenler kaydedildi ve Android telefona aktarıldı!');
            } catch (e) {
                alert('Kaydetme hatası oluştu: ' + e.message);
            } finally {
                saveBtn.disabled = false;
                saveBtn.innerHTML = `<span>💾</span> <span>Düzenleri Kaydet & Telefona Gönder</span>`;
            }
        });
    }

    if (clientBtn) {
        clientBtn.addEventListener('click', () => {
            modalManager.open('clientModal');
        });
    }

    // Listen to state changes to update badge
    state.on('clients:updated', updateHeaderBadge);
}

export function updateHeaderBadge(clientsData) {
    const connected = clientsData.connected_clients ? clientsData.connected_clients.length : 0;
    const pending = clientsData.pending_requests ? clientsData.pending_requests.length : 0;
    const badge = document.getElementById('clientCountBadge');
    const dot = document.getElementById('clientStatusDot');

    if (!badge || !dot) return;

    if (pending > 0) {
        badge.innerText = `${pending} Bekliyor!`;
        badge.style.background = '#f59e0b22';
        badge.style.color = '#f59e0b';
        badge.style.borderColor = '#f59e0b44';
        dot.style.background = '#f59e0b';
    } else {
        badge.innerText = `${connected} Cihaz`;
        badge.style.background = '#22c55e22';
        badge.style.color = '#22c55e';
        badge.style.borderColor = '#22c55e44';
        dot.style.background = connected > 0 ? '#10b981' : '#71717a';
    }
}

export function showToast(message) {
    let toast = document.getElementById('toastMsg');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'toastMsg';
        toast.className = 'toast';
        document.body.appendChild(toast);
    }
    toast.innerText = message;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 3000);
}
