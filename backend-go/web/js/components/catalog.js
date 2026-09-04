// ==============================================================================
// CATALOG COMPONENT (SIDEBAR & SEARCH & CATEGORY FILTER)
// ==============================================================================

import { state } from '../state.js';
import { CATEGORIES, WIDGETS_CATALOG } from '../constants/catalog.js';

export function initCatalog() {
    initCategoryTabs();
    initSearchInput();
    renderPalette();

    // Re-render when filter or search changes
    state.on('catalog:filter_changed', () => {
        renderPalette();
    });
}

function initCategoryTabs() {
    const tabsContainer = document.getElementById('categoryTabs');
    if (!tabsContainer) return;

    tabsContainer.querySelectorAll('.category-tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            tabsContainer.querySelectorAll('.category-tab-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            const category = btn.dataset.category || 'all';
            state.setCategoryFilter(category);
        });
    });
}

function initSearchInput() {
    const searchInput = document.getElementById('widgetSearchInput');
    const clearBtn = document.getElementById('searchClearBtn');

    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value;
            if (clearBtn) {
                clearBtn.style.display = query.length > 0 ? 'block' : 'none';
            }
            state.setSearchKeyword(query);
        });
    }

    if (clearBtn) {
        clearBtn.addEventListener('click', () => {
            if (searchInput) searchInput.value = '';
            clearBtn.style.display = 'none';
            state.setSearchKeyword('');
        });
    }
}

export function renderPalette() {
    const list = document.getElementById('paletteList');
    if (!list) return;
    list.innerHTML = '';

    const query = state.searchKeyword.toLowerCase().trim();
    const filterCat = state.selectedCategoryFilter;

    const filtered = WIDGETS_CATALOG.filter(w => {
        const matchCat = (filterCat === 'all' || w.category === filterCat);
        const matchQuery = !query || 
            w.name.toLowerCase().includes(query) || 
            w.desc.toLowerCase().includes(query) || 
            w.id.toLowerCase().includes(query);
        return matchCat && matchQuery;
    });

    // Update Header Count Badge
    const totalBadge = document.getElementById('catalogTotalBadge');
    if (totalBadge) {
        totalBadge.innerText = `${filtered.length} Bileşen`;
    }

    if (filtered.length === 0) {
        list.innerHTML = `
            <div style="text-align:center; padding:36px 14px; color:var(--text-muted); font-size:0.82rem;">
                <div style="font-size:2rem; margin-bottom:8px;">🔍</div>
                <div style="font-weight:700; color:var(--text-sub); margin-bottom:4px;">Bileşen Bulunamadı</div>
                <div>"${state.searchKeyword}" aramasına uygun widget yok.</div>
            </div>
        `;
        return;
    }

    // Determine Active Categories to render
    const activeCategories = (filterCat === 'all' && !query)
        ? CATEGORIES
        : (filterCat !== 'all'
            ? CATEGORIES.filter(c => c.id === filterCat)
            : CATEGORIES.filter(c => filtered.some(w => w.category === c.id)));

    activeCategories.forEach(cat => {
        const catWidgets = filtered.filter(w => w.category === cat.id);
        if (catWidgets.length === 0) return;

        const section = document.createElement('div');
        section.className = 'category-section';

        const header = document.createElement('div');
        header.className = 'category-section-header';
        header.innerHTML = `
            <div class="category-section-title">
                <span>${cat.icon}</span>
                <span>${cat.name}</span>
            </div>
            <span class="category-section-count">${catWidgets.length}</span>
        `;
        section.appendChild(header);

        catWidgets.forEach(w => {
            const card = createWidgetSourceCard(w);
            section.appendChild(card);
        });

        list.appendChild(section);
    });
}

function createWidgetSourceCard(w) {
    const card = document.createElement('div');
    card.className = 'widget-source-card';
    card.draggable = true;
    card.innerHTML = `
        <div class="source-icon-box">${w.icon}</div>
        <div class="source-info">
            <div class="source-card-header">
                <span class="source-title" title="${w.name}">${w.name}</span>
                <span class="source-badge">${w.span}x${w.row}</span>
            </div>
            <div class="source-desc" title="${w.desc}">${w.desc}</div>
        </div>
        <button class="source-quick-add-btn" title="Sayfaya Hızlı Ekle">+</button>
    `;

    card.addEventListener('dragstart', (e) => {
        state.draggedFromPaletteWidgetId = w.id;
        state.draggedGridItemIndex = null;
        state.activeDragWidth = w.span;
        state.activeDragHeight = w.row;
        e.dataTransfer.setData('text/plain', w.id);
        e.dataTransfer.effectAllowed = 'copy';
    });

    card.addEventListener('dragend', () => {
        state.emit('canvas:ghost_hide');
        state.draggedFromPaletteWidgetId = null;
    });

    // Double Click Quick-Add
    card.addEventListener('dblclick', () => {
        quickAddWidget(w);
    });

    // Plus Button Click
    const addBtn = card.querySelector('.source-quick-add-btn');
    if (addBtn) {
        addBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            quickAddWidget(w);
        });
    }

    return card;
}

export function quickAddWidget(w) {
    const page = state.getCurrentPage();
    if (page) {
        if (!page.widgets) page.widgets = [];
        const slot = findFirstAvailableSlot(page.widgets, w.span, w.row);
        page.widgets.push({
            widget_id: w.id,
            x: slot.x,
            y: slot.y,
            w: w.span,
            h: w.row,
            span: w.span,
            row_span: w.row,
            config: {
                font_scale: "medium",
                accent_color: "cyan",
                show_seconds: true,
                show_date: true,
                show_temp: true,
                show_bar: true,
                blur_background: true
            }
        });
        state.emit('layouts:updated', state.layouts);
    }
}

export function findFirstAvailableSlot(widgets, w, h) {
    for (let testY = 0; testY < 50; testY++) {
        for (let testX = 0; testX <= (4 - w); testX++) {
            let occupied = false;
            for (const item of (widgets || [])) {
                const itemX = item.x !== undefined ? item.x : 0;
                const itemY = item.y !== undefined ? item.y : 0;
                const itemW = item.w || item.span || 2;
                const itemH = item.h || item.row_span || 1;

                if (
                    testX < itemX + itemW &&
                    testX + w > itemX &&
                    testY < itemY + itemH &&
                    testY + h > itemY
                ) {
                    occupied = true;
                    break;
                }
            }
            if (!occupied) {
                return { x: testX, y: testY };
            }
        }
    }
    return { x: 0, y: 0 };
}
