// ==============================================================================
// LIVE PHONE SIMULATOR COMPONENT
// ==============================================================================

import { state } from '../state.js';
import { WIDGETS_CATALOG } from '../constants/catalog.js';
import { getAccentColorHex } from '../constants/themes.js';

export function initPreview() {
    renderSimPreview();

    state.on('layouts:updated', renderSimPreview);
    state.on('page:changed', renderSimPreview);
    state.on('canvas:resized', renderSimPreview);
    state.on('theme:changed', renderSimPreview);
}

export function renderSimPreview() {
    const sim = document.getElementById('simGrid');
    if (!sim) return;
    sim.innerHTML = '';

    const page = state.getCurrentPage();
    if (!page || !page.widgets) return;

    page.widgets.forEach(w => {
        const wDef = WIDGETS_CATALOG.find(x => x.id === w.widget_id) || { name: w.widget_id, icon: '📦' };
        const cfg = w.config || {};
        const accent = getAccentColorHex(cfg.accent_color);
        const title = cfg.custom_title || wDef.name;

        const card = document.createElement('div');
        card.className = 'sim-card';
        card.style.gridColumn = `${(w.x || 0) + 1} / span ${w.w || w.span || 2}`;
        card.style.gridRow = `${(w.y || 0) + 1} / span ${w.h || w.row_span || 1}`;
        card.style.height = `${(w.h || w.row_span || 1) * 55 + ((w.h || w.row_span || 1) - 1) * 6}px`;

        card.innerHTML = `
            <div style="font-weight:bold; color:#fff; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">${wDef.icon} ${title}</div>
            <div style="font-size:0.55rem; color:${accent}; font-weight:bold;">${w.w || w.span}x${w.h || w.row_span} • ${cfg.font_scale || 'norm'}</div>
        `;
        sim.appendChild(card);
    });
}
