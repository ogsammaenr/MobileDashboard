// ==============================================================================
// CLIENTS & DEVICE PAIRING MANAGER MODAL COMPONENT
// ==============================================================================

import { state } from '../state.js';
import { api } from '../services/api.js';
import { modalManager } from './modalManager.js';

export function initClientModal() {
    modalManager.register('clientModal', {
        onInit: () => {
            bindControls();
        },
        onOpen: async () => {
            await refreshClients();
        }
    });

    state.on('clients:updated', renderClientsModal);
}

function bindControls() {
    const closeBtn = document.querySelector('#clientModal .modal-close');
    const doneBtn = document.querySelector('#clientModal .modal-footer .btn-primary');
    const autoSwitch = document.getElementById('autoAcceptSwitch');
    const refreshBtn = document.getElementById('refreshClientsBtn');

    if (closeBtn) closeBtn.addEventListener('click', () => modalManager.close('clientModal'));
    if (doneBtn) doneBtn.addEventListener('click', () => modalManager.close('clientModal'));
    if (refreshBtn) refreshBtn.addEventListener('click', refreshClients);

    if (autoSwitch) {
        autoSwitch.addEventListener('change', async (e) => {
            try {
                await api.toggleAutoAccept(e.target.checked);
                await refreshClients();
            } catch (err) {
                alert('Ayar güncellenemedi: ' + err.message);
            }
        });
    }
}

export async function refreshClients() {
    const clients = await api.fetchClients();
    if (clients) {
        state.setClientsData(clients);
    }
}

export function renderClientsModal() {
    const clientsData = state.clientsData;

    // 1. Auto Accept Switch
    const autoSwitch = document.getElementById('autoAcceptSwitch');
    const statusTag = document.getElementById('autoAcceptStatusTag');
    if (autoSwitch) autoSwitch.checked = clientsData.auto_accept_enabled;

    if (statusTag) {
        if (clientsData.auto_accept_enabled) {
            statusTag.innerText = 'AÇIK';
            statusTag.style.background = '#22c55e22';
            statusTag.style.color = '#22c55e';
            statusTag.style.borderColor = '#22c55e44';
        } else {
            statusTag.innerText = 'KAPALI (ONAY GEREKİR)';
            statusTag.style.background = '#f59e0b22';
            statusTag.style.color = '#f59e0b';
            statusTag.style.borderColor = '#f59e0b44';
        }
    }

    // 2. Pending Requests Section
    const pendingSection = document.getElementById('pendingSection');
    const pendingList = document.getElementById('pendingList');
    const pendingBadge = document.getElementById('pendingCountBadge');
    const pending = clientsData.pending_requests || [];

    if (pendingSection && pendingList) {
        if (pending.length > 0) {
            pendingSection.style.display = 'flex';
            if (pendingBadge) pendingBadge.innerText = pending.length;
            pendingList.innerHTML = pending.map(p => `
                <div style="background: #201a10; border: 1px solid #f59e0b55; border-radius: 10px; padding: 10px 14px; display: flex; align-items: center; justify-content: space-between; gap: 10px;">
                    <div>
                        <div style="font-weight: 700; font-size: 0.88rem; color: #fff;">📱 ${escapeHtml(p.device_name)}</div>
                        <div style="font-size: 0.72rem; color: #f59e0b; font-family: monospace;">IP: ${escapeHtml(p.ip)} • ID: ${escapeHtml(p.id)}</div>
                    </div>
                    <div style="display: flex; gap: 6px;">
                        <button class="btn btn-mini btn-approve" style="background: #10b981; color: #fff; border-color: #10b981; font-weight: 700;" data-id="${escapeHtml(p.id)}">✓ Kabul Et</button>
                        <button class="btn btn-mini btn-mini-del btn-reject" style="font-weight: 700;" data-id="${escapeHtml(p.id)}">✕ Reddet</button>
                    </div>
                </div>
            `).join('');

            // Attach click handlers
            pendingList.querySelectorAll('.btn-approve').forEach(b => {
                b.addEventListener('click', async () => {
                    await api.approveClient(b.dataset.id);
                    await refreshClients();
                });
            });
            pendingList.querySelectorAll('.btn-reject').forEach(b => {
                b.addEventListener('click', async () => {
                    await api.rejectClient(b.dataset.id);
                    await refreshClients();
                });
            });
        } else {
            pendingSection.style.display = 'none';
        }
    }

    // 3. Live Connected Clients Section
    const connectedList = document.getElementById('connectedList');
    const liveBadge = document.getElementById('liveClientCountBadge');
    const connected = clientsData.connected_clients || [];
    if (liveBadge) liveBadge.innerText = connected.length;

    if (connectedList) {
        if (connected.length === 0) {
            connectedList.innerHTML = `
                <div style="background: #0e0e12; border: 1px dashed #282834; border-radius: 8px; padding: 12px; font-size: 0.78rem; color: var(--text-muted); text-align: center;">
                    Şu anda bağlı aktif mobil cihaz bulunmuyor.
                </div>
            `;
        } else {
            connectedList.innerHTML = connected.map(c => `
                <div style="background: #121218; border: 1px solid #282834; border-radius: 10px; padding: 10px 14px; display: flex; align-items: center; justify-content: space-between; gap: 10px;">
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <div style="width: 10px; height: 10px; border-radius: 50%; background: #10b981; box-shadow: 0 0 8px #10b981;"></div>
                        <div>
                            <div style="font-weight: 700; font-size: 0.88rem; color: #fff;">📱 ${escapeHtml(c.device_name)}</div>
                            <div style="font-size: 0.72rem; color: var(--text-muted); font-family: monospace;">IP: ${escapeHtml(c.ip)} • ID: ${escapeHtml(c.id)}</div>
                        </div>
                    </div>
                    <button class="btn-mini btn-mini-del btn-disconnect" data-id="${escapeHtml(c.id)}">Bağlantıyı Kes</button>
                </div>
            `).join('');

            connectedList.querySelectorAll('.btn-disconnect').forEach(b => {
                b.addEventListener('click', async () => {
                    await api.disconnectClient(b.dataset.id);
                    await refreshClients();
                });
            });
        }
    }

    // 4. Authorized Clients List
    fetchAndRenderAuthorizedClients();
}

async function fetchAndRenderAuthorizedClients() {
    const cfg = await api.fetchConfig();
    const authList = document.getElementById('authorizedList');
    if (!authList || !cfg) return;

    const clients = cfg.authorized_clients || {};
    const keys = Object.keys(clients);

    if (keys.length === 0) {
        authList.innerHTML = `
            <div style="background: #0e0e12; border: 1px dashed #282834; border-radius: 8px; padding: 10px; font-size: 0.75rem; color: var(--text-muted); text-align: center;">
                Henüz kayıtlı yetkili cihaz yok.
            </div>
        `;
    } else {
        authList.innerHTML = keys.map(k => {
            const item = clients[k];
            return `
                <div style="background: #111116; border: 1px solid #22222a; border-radius: 8px; padding: 8px 12px; display: flex; align-items: center; justify-content: space-between; gap: 10px;">
                    <div>
                        <div style="font-weight: 600; font-size: 0.8rem; color: #e4e4e7;">${escapeHtml(item.name || k)}</div>
                        <div style="font-size: 0.68rem; color: var(--text-muted); font-family: monospace;">IP: ${escapeHtml(item.ip || '-')}</div>
                    </div>
                    <button class="btn-mini btn-mini-del btn-delete-auth" style="padding: 2px 7px; font-size: 0.68rem;" data-id="${escapeHtml(k)}">Yetkiyi Sil</button>
                </div>
            `;
        }).join('');

        authList.querySelectorAll('.btn-delete-auth').forEach(b => {
            b.addEventListener('click', async () => {
                if (!confirm('Bu cihazın yetkisini silmek istediğinize emin misiniz?')) return;
                await api.deleteAuthorizedClient(b.dataset.id);
                await refreshClients();
            });
        });
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
