// ==============================================================================
// CENTRAL REACTIVE STATE & PUB-SUB EVENT BUS
// ==============================================================================

class StateManager {
    constructor() {
        this.layouts = [];
        this.activePageIndex = 0;
        this.selectedCategoryFilter = "all";
        this.searchKeyword = "";
        
        // Drag state
        this.draggedFromPaletteWidgetId = null;
        this.draggedGridItemIndex = null;
        this.activeDragWidth = 2;
        this.activeDragHeight = 1;

        // Editing state
        this.editingWidgetIndex = null;
        this.selectedFontScale = "medium";
        this.selectedAccentColor = "cyan";

        // Clients state
        this.clientsData = {
            auto_accept_enabled: true,
            connected_clients: [],
            pending_requests: [],
            authorized_count: 0
        };

        this.listeners = new Map();
    }

    on(event, callback) {
        if (!this.listeners.has(event)) {
            this.listeners.set(event, []);
        }
        this.listeners.get(event).push(callback);
    }

    emit(event, payload) {
        if (this.listeners.has(event)) {
            this.listeners.get(event).forEach(cb => {
                try {
                    cb(payload);
                } catch (e) {
                    console.error(`[EventBus Error] ${event}:`, e);
                }
            });
        }
    }

    getCurrentPage() {
        return this.layouts[this.activePageIndex] || null;
    }

    setLayouts(newLayouts) {
        this.layouts = newLayouts || [];
        if (this.activePageIndex >= this.layouts.length) {
            this.activePageIndex = Math.max(0, this.layouts.length - 1);
        }
        this.emit('layouts:updated', this.layouts);
    }

    setActivePageIndex(idx) {
        if (idx >= 0 && idx < this.layouts.length) {
            this.activePageIndex = idx;
            this.emit('page:changed', this.activePageIndex);
        }
    }

    setCategoryFilter(category) {
        this.selectedCategoryFilter = category;
        this.emit('catalog:filter_changed', { category, query: this.searchKeyword });
    }

    setSearchKeyword(keyword) {
        this.searchKeyword = keyword || "";
        this.emit('catalog:filter_changed', { category: this.selectedCategoryFilter, query: this.searchKeyword });
    }

    setClientsData(data) {
        this.clientsData = data || {
            auto_accept_enabled: true,
            connected_clients: [],
            pending_requests: [],
            authorized_count: 0
        };
        this.emit('clients:updated', this.clientsData);
    }
}

export const state = new StateManager();
