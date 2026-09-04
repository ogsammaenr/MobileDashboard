// ==============================================================================
// 2D CANVAS, PAGES BAR & RESIZE ENGINE COMPONENT
// ==============================================================================

import { state } from '../state.js';
import { WIDGETS_CATALOG } from '../constants/catalog.js';
import { getWidgetMiniPreviewHtml } from './widgetPreviews.js';
import { modalManager } from '../modals/modalManager.js';

export function initCanvas() {
    initPageInputs();
    initDropHandlers();

    // Re-render listeners
    state.on('layouts:updated', () => {
        renderPagesBar();
        renderCanvas();
    });

    state.on('page:changed', () => {
        renderPagesBar();
        renderCanvas();
    });

    state.on('canvas:ghost_hide', hideGhost);
}

function initPageInputs() {
    const titleInput = document.getElementById('pageTitleInput');
    const themeSelect = document.getElementById('pageThemeSelect');
    const addPageBtn = document.getElementById('addPageBtn');

    if (titleInput) {
        titleInput.addEventListener('input', (e) => {
            const page = state.getCurrentPage();
            if (page) {
                page.title = e.target.value;
                renderPagesBar();
            }
        });
    }

    if (themeSelect) {
        themeSelect.addEventListener('change', (e) => {
            const page = state.getCurrentPage();
            if (page) {
                page.theme = e.target.value;
                state.emit('theme:changed', page.theme);
            }
        });
    }

    if (addPageBtn) {
        addPageBtn.addEventListener('click', addNewPage);
    }
}

export function addNewPage() {
    state.layouts.push({
        id: 'page_' + Date.now(),
        title: 'Yeni Sayfa ' + (state.layouts.length + 1),
        icon: '📱',
        theme: 'cyan',
        widgets: [
            { widget_id: 'clock_cyber_hud', x: 0, y: 0, w: 4, h: 1, span: 4, row_span: 1, config: { font_scale: 'medium', accent_color: 'cyan', show_seconds: true, show_date: true } },
            { widget_id: 'cpu_card', x: 0, y: 1, w: 2, h: 1, span: 2, row_span: 1, config: { font_scale: 'medium', accent_color: 'cyan', show_temp: true, show_bar: true } },
            { widget_id: 'gpu_card', x: 2, y: 1, w: 2, h: 1, span: 2, row_span: 1, config: { font_scale: 'medium', accent_color: 'cyan', show_temp: true, show_bar: true } }
        ]
    });
    state.activePageIndex = state.layouts.length - 1;
    state.emit('layouts:updated', state.layouts);
}

export function deletePage(idx) {
    if (state.layouts.length <= 1) return;
    state.layouts.splice(idx, 1);
    if (state.activePageIndex >= state.layouts.length) {
        state.activePageIndex = state.layouts.length - 1;
    }
    state.emit('layouts:updated', state.layouts);
}

export function renderPagesBar() {
    const bar = document.getElementById('pagesBar');
    if (!bar) return;
    bar.querySelectorAll('.page-tab').forEach(el => el.remove());

    const addBtn = document.getElementById('addPageBtn');

    state.layouts.forEach((page, idx) => {
        const tab = document.createElement('div');
        tab.className = `page-tab ${idx === state.activePageIndex ? 'active' : ''}`;
        tab.innerHTML = `
            <span>${page.icon || '📄'} ${page.title || 'Sayfa ' + (idx + 1)}</span>
            ${state.layouts.length > 1 ? `<span class="page-tab-del" title="Sayfayı Sil">✕</span>` : ''}
        `;
        tab.addEventListener('click', (e) => {
            if (e.target.classList.contains('page-tab-del')) {
                deletePage(idx);
            } else {
                state.setActivePageIndex(idx);
            }
        });
        bar.insertBefore(tab, addBtn);
    });
}

export function renderCanvas() {
    const page = state.getCurrentPage();
    if (!page) return;

    const titleInput = document.getElementById('pageTitleInput');
    const themeSelect = document.getElementById('pageThemeSelect');
    if (titleInput) titleInput.value = page.title || '';
    if (themeSelect) themeSelect.value = page.theme || 'cyan';

    const grid = document.getElementById('dropGrid');
    const ghost = document.getElementById('gridGhost');
    if (!grid) return;

    grid.innerHTML = '';
    if (ghost) grid.appendChild(ghost);

    (page.widgets || []).forEach((w, wIdx) => {
        clampWidget(w);
        const wDef = WIDGETS_CATALOG.find(x => x.id === w.widget_id) || { name: w.widget_id, icon: '📦' };
        const cfg = w.config || {};
        const customTitle = cfg.custom_title || wDef.name;

        const item = document.createElement('div');
        item.className = 'grid-widget-item';
        item.draggable = true;
        item.dataset.index = wIdx;

        item.style.gridColumn = `${w.x + 1} / span ${w.w}`;
        item.style.gridRow = `${w.y + 1} / span ${w.h}`;
        item.style.height = `${w.h * 115 + (w.h - 1) * 12}px`;

        item.innerHTML = `
            <div class="item-top-bar">
                <div style="display:flex; align-items:center; gap:6px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">
                    <span>${wDef.icon}</span>
                    <span style="color:#fff;">${customTitle}</span>
                </div>
                <div class="item-controls">
                    <button class="btn-mini btn-mini-settings" title="Widget Ayarları">⚙️</button>
                    <button class="btn-mini btn-mini-del" title="Sil">✕</button>
                </div>
            </div>
            <div class="item-preview-content">
                ${getWidgetMiniPreviewHtml(w)}
            </div>
            <div class="resize-handle-right" title="Sağa Çekerek Genişlet"></div>
            <div class="resize-handle-bottom" title="Aşağı Çekerek Uzat"></div>
            <div class="resize-handle-corner" title="Serbest Boyutlandır">⤡</div>
        `;

        // Controls click handlers
        item.querySelector('.btn-mini-settings').addEventListener('click', () => {
            modalManager.open('settingsModal', { widgetIndex: wIdx });
        });
        item.querySelector('.btn-mini-del').addEventListener('click', () => deleteWidget(wIdx));

        // Item Drag Start
        item.addEventListener('dragstart', (e) => {
            state.draggedGridItemIndex = wIdx;
            state.draggedFromPaletteWidgetId = null;
            state.activeDragWidth = w.w;
            state.activeDragHeight = w.h;
            item.classList.add('dragging');
            e.dataTransfer.setData('text/plain', w.widget_id);
            e.dataTransfer.effectAllowed = 'move';
        });

        item.addEventListener('dragend', () => {
            item.classList.remove('dragging');
            hideGhost();
            state.draggedGridItemIndex = null;
        });

        // Resize Handles setup
        attachResizeListeners(item, w, grid);

        grid.appendChild(item);
    });
}

function attachResizeListeners(item, w, grid) {
    const handleRight = item.querySelector('.resize-handle-right');
    const handleBottom = item.querySelector('.resize-handle-bottom');
    const handleCorner = item.querySelector('.resize-handle-corner');

    function initResize(e, type) {
        e.preventDefault();
        e.stopPropagation();

        item.draggable = false;
        document.body.style.userSelect = 'none';

        const startX = e.clientX;
        const startY = e.clientY;
        const startW = w.w;
        const startH = w.h;

        const gridRect = grid.getBoundingClientRect();
        const colWidth = (gridRect.width - 36) / 4;
        const rowHeight = 115;

        function onPointerMove(moveEvent) {
            moveEvent.preventDefault();
            const deltaX = moveEvent.clientX - startX;
            const deltaY = moveEvent.clientY - startY;

            let updated = false;

            if (type === 'right' || type === 'corner') {
                const maxPossibleW = 4 - w.x;
                const wDelta = Math.round(deltaX / (colWidth * 0.7));
                let newW = Math.min(maxPossibleW, Math.max(1, startW + wDelta));
                if (newW !== w.w) {
                    w.w = newW;
                    updated = true;
                }
            }

            if (type === 'bottom' || type === 'corner') {
                const hDelta = Math.round(deltaY / (rowHeight * 0.75));
                let newH = Math.min(6, Math.max(1, startH + hDelta));
                if (newH !== w.h) {
                    w.h = newH;
                    updated = true;
                }
            }

            if (updated) {
                clampWidget(w);
                item.style.gridColumn = `${w.x + 1} / span ${w.w}`;
                item.style.gridRow = `${w.y + 1} / span ${w.h}`;
                item.style.height = `${w.h * 115 + (w.h - 1) * 12}px`;
                state.emit('canvas:resized', w);
            }
        }

        function onPointerUp() {
            window.removeEventListener('pointermove', onPointerMove);
            window.removeEventListener('pointerup', onPointerUp);
            window.removeEventListener('mousemove', onPointerMove);
            window.removeEventListener('mouseup', onPointerUp);

            item.draggable = true;
            document.body.style.userSelect = '';
            state.emit('layouts:updated', state.layouts);
        }

        window.addEventListener('pointermove', onPointerMove);
        window.addEventListener('pointerup', onPointerUp);
        window.addEventListener('mousemove', onPointerMove);
        window.addEventListener('mouseup', onPointerUp);
    }

    if (handleRight) {
        handleRight.addEventListener('pointerdown', (e) => initResize(e, 'right'));
        handleRight.addEventListener('mousedown', (e) => initResize(e, 'right'));
    }
    if (handleBottom) {
        handleBottom.addEventListener('pointerdown', (e) => initResize(e, 'bottom'));
        handleBottom.addEventListener('mousedown', (e) => initResize(e, 'bottom'));
    }
    if (handleCorner) {
        handleCorner.addEventListener('pointerdown', (e) => initResize(e, 'corner'));
        handleCorner.addEventListener('mousedown', (e) => initResize(e, 'corner'));
    }
}

export function shiftWidget(wIdx, dx, dy) {
    const page = state.getCurrentPage();
    if (!page) return;
    const w = page.widgets[wIdx];
    w.x += dx;
    w.y += dy;
    clampWidget(w);
    state.emit('layouts:updated', state.layouts);
}

export function cycleWidth(wIdx) {
    const page = state.getCurrentPage();
    if (!page) return;
    const w = page.widgets[wIdx];
    let nextW = w.w + 1;
    if (w.x + nextW > 4) {
        if (w.x > 0 && nextW <= 4) {
            w.x = 4 - nextW;
        } else {
            nextW = 1;
        }
    }
    w.w = nextW;
    clampWidget(w);
    state.emit('layouts:updated', state.layouts);
}

export function cycleHeight(wIdx) {
    const page = state.getCurrentPage();
    if (!page) return;
    const w = page.widgets[wIdx];
    let nextH = w.h + 1;
    if (nextH > 4) nextH = 1;
    w.h = nextH;
    clampWidget(w);
    state.emit('layouts:updated', state.layouts);
}

export function deleteWidget(wIdx) {
    const page = state.getCurrentPage();
    if (!page) return;
    page.widgets.splice(wIdx, 1);
    state.emit('layouts:updated', state.layouts);
}

export function clampWidget(w) {
    if (w.w === undefined || w.w === null) w.w = w.span || 2;
    if (w.h === undefined || w.h === null) w.h = w.row_span || 1;
    if (w.x === undefined || w.x === null) w.x = 0;
    if (w.y === undefined || w.y === null) w.y = 0;
    if (w.w > 4) w.w = 4;
    if (w.w < 1) w.w = 1;
    if (w.h < 1) w.h = 1;
    if (w.h > 6) w.h = 6;
    if (w.x < 0) w.x = 0;
    if (w.x + w.w > 4) {
        w.x = Math.max(0, 4 - w.w);
    }
    if (w.y < 0) w.y = 0;
    w.span = w.w;
    w.row_span = w.h;
}

function initDropHandlers() {
    const dropGrid = document.getElementById('dropGrid');
    if (!dropGrid) return;

    dropGrid.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropGrid.classList.add('drag-over');
        const { cellX, cellY, colWidth, rowHeight } = getCellFromCoords(e);
        showGhost(cellX, cellY, colWidth, rowHeight, state.activeDragWidth, state.activeDragHeight);
    });

    dropGrid.addEventListener('dragleave', (e) => {
        if (e.target === dropGrid) {
            dropGrid.classList.remove('drag-over');
            hideGhost();
        }
    });

    dropGrid.addEventListener('drop', (e) => {
        e.preventDefault();
        dropGrid.classList.remove('drag-over');
        hideGhost();

        const { cellX, cellY } = getCellFromCoords(e);
        const page = state.getCurrentPage();
        if (!page) return;

        if (state.draggedGridItemIndex !== null) {
            const item = page.widgets[state.draggedGridItemIndex];
            if (item) {
                item.x = cellX;
                item.y = cellY;
                clampWidget(item);
            }
        } else if (state.draggedFromPaletteWidgetId) {
            const wDef = WIDGETS_CATALOG.find(x => x.id === state.draggedFromPaletteWidgetId);
            const newWidget = {
                widget_id: state.draggedFromPaletteWidgetId,
                x: cellX,
                y: cellY,
                w: wDef ? wDef.span : 2,
                h: wDef ? wDef.row : 1,
                span: wDef ? wDef.span : 2,
                row_span: wDef ? wDef.row : 1,
                config: {
                    font_scale: "medium",
                    accent_color: "cyan",
                    shape_style: "rounded",
                    params: {
                        show_seconds: true,
                        show_date: true,
                        show_temp: true,
                        show_bar: true,
                        show_badge: true,
                        blur_background: true
                    }
                }
            };
            clampWidget(newWidget);
            page.widgets.push(newWidget);
        }

        state.draggedFromPaletteWidgetId = null;
        state.draggedGridItemIndex = null;
        state.emit('layouts:updated', state.layouts);
    });
}

function getCellFromCoords(e) {
    const dropGrid = document.getElementById('dropGrid');
    const rect = dropGrid.getBoundingClientRect();
    const xOffset = e.clientX - rect.left - 16;
    const yOffset = e.clientY - rect.top - 16;

    const colWidth = (rect.width - 32 - 36) / 4;
    const rowHeight = 127;

    let cellX = Math.floor(xOffset / (colWidth + 12));
    let cellY = Math.floor(yOffset / rowHeight);

    if (cellX < 0) cellX = 0;
    if (cellX > 3) cellX = 3;
    if (cellY < 0) cellY = 0;

    const w = state.activeDragWidth || 2;
    if (cellX + w > 4) {
        cellX = Math.max(0, 4 - w);
    }

    return { cellX, cellY, colWidth, rowHeight };
}

function showGhost(cellX, cellY, colWidth, rowHeight, w, h) {
    const ghost = document.getElementById('gridGhost');
    if (!ghost) return;
    ghost.style.display = 'block';
    ghost.style.left = `${16 + cellX * (colWidth + 12)}px`;
    ghost.style.top = `${16 + cellY * rowHeight}px`;
    ghost.style.width = `${w * colWidth + (w - 1) * 12}px`;
    ghost.style.height = `${h * 115 + (h - 1) * 12}px`;
}

function hideGhost() {
    const ghost = document.getElementById('gridGhost');
    if (ghost) ghost.style.display = 'none';
}
