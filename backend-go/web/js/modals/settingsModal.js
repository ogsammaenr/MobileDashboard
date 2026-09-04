// ==============================================================================
// WIDGET SETTINGS MODAL COMPONENT
// ==============================================================================

import { state } from '../state.js';
import { api } from '../services/api.js';
import { WIDGETS_CATALOG } from '../constants/catalog.js';
import { modalManager } from './modalManager.js';

let installedAppsCache = null;
let currentShortcutMode = 'installed';

export function initSettingsModal() {
    modalManager.register('settingsModal', {
        onInit: () => {
            bindControls();
        },
        onOpen: async (data) => {
            if (data && data.widgetIndex !== undefined) {
                await populateSettings(data.widgetIndex);
            }
        },
        onClose: () => {
            state.editingWidgetIndex = null;
        }
    });
}

function bindControls() {
    // Font scale buttons
    document.querySelectorAll('.btn-toggle-group .btn-toggle[data-scale]').forEach(btn => {
        btn.addEventListener('click', () => {
            const scale = btn.dataset.scale || 'medium';
            setFontScale(scale);
        });
    });

    // Color palette dots
    document.querySelectorAll('.color-palette .color-dot').forEach(dot => {
        dot.addEventListener('click', () => {
            const color = dot.dataset.color || 'cyan';
            setAccentColor(color);
        });
    });

    // Close buttons
    const closeBtn = document.querySelector('#settingsModal .modal-close');
    const cancelBtn = document.querySelector('#settingsModal .modal-footer .btn:not(.btn-primary)');
    const applyBtn = document.querySelector('#settingsModal .modal-footer .btn-primary');

    if (closeBtn) closeBtn.addEventListener('click', () => modalManager.close('settingsModal'));
    if (cancelBtn) cancelBtn.addEventListener('click', () => modalManager.close('settingsModal'));
    if (applyBtn) applyBtn.addEventListener('click', saveWidgetSettings);

    // Shortcut Mode Selector
    document.querySelectorAll('#shortcutModeToggle .btn-toggle').forEach(btn => {
        btn.addEventListener('click', () => {
            const mode = btn.dataset.mode || 'installed';
            setShortcutMode(mode);
        });
    });

    // Installed App Select Change
    const appSelect = document.getElementById('modalInstalledAppSelect');
    if (appSelect) {
        appSelect.addEventListener('change', () => {
            const selectedPath = appSelect.value;
            if (!installedAppsCache) return;
            const app = installedAppsCache.find(x => x.path === selectedPath);
            if (app) {
                updateDetectedAppDisplay(app.name, app.exec, app.icon_url);
                const titleInput = document.getElementById('modalCustomTitle');
                if (titleInput && !titleInput.value.trim()) {
                    titleInput.value = app.name;
                }
            }
        });
    }

    // Detect Path Button Click
    const btnDetect = document.getElementById('btnDetectPath');
    const inputPath = document.getElementById('modalAppPath');
    if (btnDetect && inputPath) {
        btnDetect.addEventListener('click', async () => {
            const path = inputPath.value.trim();
            if (!path) return;
            btnDetect.innerText = '⏳';
            const app = await api.fetchAppInfo(path);
            btnDetect.innerText = '🔍 Algıla';
            if (app) {
                updateDetectedAppDisplay(app.name, app.exec, app.icon_url);
                const titleInput = document.getElementById('modalCustomTitle');
                if (titleInput && !titleInput.value.trim()) {
                    titleInput.value = app.name;
                }
            } else {
                alert('Dosya bulunamadı veya geçerli bir uygulama değil.');
            }
        });
    }
}

function setShortcutMode(mode) {
    currentShortcutMode = mode;
    document.querySelectorAll('#shortcutModeToggle .btn-toggle').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.mode === mode);
    });

    const gInstalled = document.getElementById('groupInstalledApps');
    const gPath = document.getElementById('groupCustomPath');
    const gCmd = document.getElementById('groupCustomCommand');

    if (gInstalled) gInstalled.style.display = mode === 'installed' ? 'block' : 'none';
    if (gPath) gPath.style.display = mode === 'custom_path' ? 'block' : 'none';
    if (gCmd) gCmd.style.display = mode === 'command' ? 'block' : 'none';
}

function updateDetectedAppDisplay(name, exec, iconUrl) {
    const elName = document.getElementById('detectedAppName');
    const elExec = document.getElementById('detectedAppExec');
    const elImg = document.getElementById('detectedIconImg');
    const elEmoji = document.getElementById('detectedIconEmoji');
    const elHiddenUrl = document.getElementById('modalAppIconUrl');

    if (elName) elName.innerText = name || 'Uygulama';
    if (elExec) elExec.innerText = exec || 'Otomatik Komut';
    if (elHiddenUrl) elHiddenUrl.value = iconUrl || '';

    if (iconUrl && elImg && elEmoji) {
        elImg.src = iconUrl;
        elImg.style.display = 'block';
        elEmoji.style.display = 'none';
        elImg.onerror = () => {
            elImg.style.display = 'none';
            elEmoji.style.display = 'block';
        };
    } else if (elImg && elEmoji) {
        elImg.style.display = 'none';
        elEmoji.style.display = 'block';
    }
}

function setFontScale(scale) {
    state.selectedFontScale = scale;
    document.querySelectorAll('.btn-toggle-group .btn-toggle[data-scale]').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.scale === scale);
    });
}

function setAccentColor(color) {
    state.selectedAccentColor = color;
    document.querySelectorAll('.color-palette .color-dot').forEach(dot => {
        dot.classList.toggle('active', dot.dataset.color === color);
    });
}

async function populateSettings(wIdx) {
    state.editingWidgetIndex = wIdx;
    const page = state.getCurrentPage();
    if (!page || !page.widgets || !page.widgets[wIdx]) return;

    const w = page.widgets[wIdx];
    const wDef = WIDGETS_CATALOG.find(x => x.id === w.widget_id) || { name: w.widget_id, icon: '📦' };
    const cfg = w.config || {};
    const params = cfg.params || cfg;

    const titleEl = document.getElementById('modalWidgetTitle');
    const customTitleInput = document.getElementById('modalCustomTitle');
    const shapeSelect = document.getElementById('modalShapeStyle');

    if (titleEl) titleEl.innerText = `${wDef.icon} ${wDef.name} Ayarları`;
    if (customTitleInput) customTitleInput.value = cfg.custom_title || '';

    // Font scale & Accent Color & Shape
    setFontScale(cfg.font_scale || 'medium');
    setAccentColor(cfg.accent_color || 'nord');
    if (shapeSelect) shapeSelect.value = cfg.shape_style || 'rounded';

    // Conditional visibility
    const isClock = w.widget_id.startsWith('clock_');
    const isHardware = ['cpu_card', 'gpu_card', 'ram_card', 'network_card', 'disk_card'].includes(w.widget_id);
    const isMedia = w.widget_id.startsWith('media_');
    const isShortcut = ['app_shortcut', 'shortcut_card', 'app_launcher', 'quick_action'].includes(w.widget_id);

    const clockOpts = document.getElementById('modalClockOptions');
    const hwOpts = document.getElementById('modalHardwareOptions');
    const mediaOpts = document.getElementById('modalMediaOptions');
    const shortcutOpts = document.getElementById('modalAppShortcutOptions');

    if (clockOpts) clockOpts.style.display = isClock ? 'flex' : 'none';
    if (hwOpts) hwOpts.style.display = isHardware ? 'flex' : 'none';
    if (mediaOpts) mediaOpts.style.display = isMedia ? 'flex' : 'none';
    if (shortcutOpts) shortcutOpts.style.display = isShortcut ? 'flex' : 'none';

    if (isClock) {
        const sec = document.getElementById('modalShowSeconds');
        const date = document.getElementById('modalShowDate');
        const h12 = document.getElementById('modalIs12Hour');
        if (sec) sec.checked = params.show_seconds !== false;
        if (date) date.checked = params.show_date !== false;
        if (h12) h12.checked = !!params.is_12hour;
    }

    if (isHardware) {
        const temp = document.getElementById('modalShowTemp');
        const bar = document.getElementById('modalShowBar');
        const badge = document.getElementById('modalShowBadge');
        const badgeLabel = document.getElementById('labelHardwareBadge');
        const badgeRow = document.getElementById('rowHardwareBadge');

        if (temp) temp.checked = params.show_temp !== false;
        if (bar) bar.checked = params.show_bar !== false;
        if (badge) badge.checked = params.show_badge !== false;

        if (badgeLabel && badgeRow) {
            badgeRow.style.display = 'flex';
            if (w.widget_id === 'cpu_card') {
                badgeLabel.innerText = 'CPU Durum Rozetini Göster (Aşırı Yük / Turbo / Optimal / Boşta)';
            } else if (w.widget_id === 'gpu_card') {
                badgeLabel.innerText = 'VRAM Kullanım Rozetini Göster';
            } else if (w.widget_id === 'ram_card') {
                badgeLabel.innerText = 'RAM Tüketim Durumu Rozetini Göster';
            } else {
                badgeLabel.innerText = 'Durum Rozetini Göster';
            }
        }
    }

    if (isMedia) {
        const blur = document.getElementById('modalBlurBg');
        if (blur) blur.checked = params.blur_background !== false;
    }

    if (isShortcut) {
        // Load installed PC applications if not loaded
        const appSelect = document.getElementById('modalInstalledAppSelect');
        if (!installedAppsCache) {
            if (appSelect) appSelect.innerHTML = '<option value="">⏳ Bilgisayardaki uygulamalar taranıyor...</option>';
            installedAppsCache = await api.fetchInstalledApps();
        }

        if (appSelect && installedAppsCache && installedAppsCache.length > 0) {
            appSelect.innerHTML = installedAppsCache.map(app => `
                <option value="${app.path}">${app.name} (${app.id})</option>
            `).join('');
        }

        const inputPath = document.getElementById('modalAppPath');
        const inputCmd = document.getElementById('modalAppCommand');

        const appPathVal = params.app_path || '';
        const appCmdVal = params.app_command || '';

        if (appPathVal && appPathVal.endsWith('.desktop')) {
            setShortcutMode('installed');
            if (appSelect) appSelect.value = appPathVal;
            if (inputPath) inputPath.value = appPathVal;
        } else if (appPathVal) {
            setShortcutMode('custom_path');
            if (inputPath) inputPath.value = appPathVal;
        } else if (appCmdVal) {
            setShortcutMode('command');
            if (inputCmd) inputCmd.value = appCmdVal;
        } else {
            setShortcutMode('installed');
        }

        updateDetectedAppDisplay(cfg.custom_title || 'Uygulama', appCmdVal || appPathVal || 'Otomatik', params.app_icon_url);
    }
}

function saveWidgetSettings() {
    if (state.editingWidgetIndex === null) return;
    const page = state.getCurrentPage();
    if (!page || !page.widgets || !page.widgets[state.editingWidgetIndex]) return;

    const w = page.widgets[state.editingWidgetIndex];
    if (!w.config) w.config = {};
    if (!w.config.params) w.config.params = {};
    const params = w.config.params;

    const customTitleInput = document.getElementById('modalCustomTitle');
    const shapeSelect = document.getElementById('modalShapeStyle');

    if (customTitleInput) w.config.custom_title = customTitleInput.value.trim();
    w.config.font_scale = state.selectedFontScale;
    w.config.accent_color = state.selectedAccentColor;
    if (shapeSelect) w.config.shape_style = shapeSelect.value;

    const isClock = w.widget_id.startsWith('clock_');
    const isHardware = ['cpu_card', 'gpu_card', 'ram_card', 'network_card', 'disk_card'].includes(w.widget_id);
    const isMedia = w.widget_id.startsWith('media_');
    const isShortcut = ['app_shortcut', 'shortcut_card', 'app_launcher', 'quick_action'].includes(w.widget_id);

    if (isClock) {
        const sec = document.getElementById('modalShowSeconds');
        const date = document.getElementById('modalShowDate');
        const h12 = document.getElementById('modalIs12Hour');
        if (sec) params.show_seconds = sec.checked;
        if (date) params.show_date = date.checked;
        if (h12) params.is_12hour = h12.checked;
    }

    if (isHardware) {
        const temp = document.getElementById('modalShowTemp');
        const bar = document.getElementById('modalShowBar');
        const badge = document.getElementById('modalShowBadge');
        if (temp) params.show_temp = temp.checked;
        if (bar) params.show_bar = bar.checked;
        if (badge) params.show_badge = badge.checked;
    }

    if (isMedia) {
        const blur = document.getElementById('modalBlurBg');
        if (blur) params.blur_background = blur.checked;
    }

    if (isShortcut) {
        const appSelect = document.getElementById('modalInstalledAppSelect');
        const inputPath = document.getElementById('modalAppPath');
        const inputCmd = document.getElementById('modalAppCommand');
        const hiddenIconUrl = document.getElementById('modalAppIconUrl');

        if (currentShortcutMode === 'installed' && appSelect) {
            const selectedPath = appSelect.value;
            const app = (installedAppsCache || []).find(x => x.path === selectedPath);
            if (app) {
                params.app_id = app.id;
                params.app_path = app.path;
                params.app_command = app.exec;
                params.app_icon_url = app.icon_url;
                if (!w.config.custom_title) w.config.custom_title = app.name;
            }
        } else if (currentShortcutMode === 'custom_path' && inputPath) {
            const p = inputPath.value.trim();
            params.app_path = p;
            params.app_command = p;
            params.app_icon_url = hiddenIconUrl ? hiddenIconUrl.value : '';
        } else if (currentShortcutMode === 'command' && inputCmd) {
            params.app_command = inputCmd.value.trim();
            params.app_path = '';
            params.app_icon_url = '';
        }
    }

    modalManager.close('settingsModal');
    state.emit('layouts:updated', state.layouts);
}
