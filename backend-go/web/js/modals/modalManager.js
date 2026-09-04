// ==============================================================================
// MODAL & WINDOW MANAGER (EXTENSIBLE DIALOG REGISTRY)
// ==============================================================================

class ModalManager {
    constructor() {
        this.modals = new Map();
        this.activeModalId = null;
        this.initGlobalListeners();
    }

    initGlobalListeners() {
        // Close on ESC key
        window.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.activeModalId) {
                this.close(this.activeModalId);
            }
        });
    }

    /**
     * Registers a new modal handler.
     * @param {string} modalId - DOM ID of the modal overlay (e.g. 'settingsModal', 'clientModal')
     * @param {Object} options - { onOpen: (data) => {}, onClose: () => {}, onInit: () => {} }
     */
    register(modalId, options = {}) {
        this.modals.set(modalId, options);
        if (options.onInit) {
            try {
                options.onInit();
            } catch (e) {
                console.error(`[ModalManager] Error initializing modal ${modalId}:`, e);
            }
        }
    }

    /**
     * Opens a registered modal.
     * @param {string} modalId
     * @param {any} data
     */
    open(modalId, data = null) {
        const el = document.getElementById(modalId);
        if (!el) {
            console.warn(`[ModalManager] Modal element not found: #${modalId}`);
            return;
        }

        // Close currently active if any
        if (this.activeModalId && this.activeModalId !== modalId) {
            this.close(this.activeModalId);
        }

        el.classList.add('open');
        el.classList.add('active');
        this.activeModalId = modalId;

        const handler = this.modals.get(modalId);
        if (handler && handler.onOpen) {
            try {
                handler.onOpen(data);
            } catch (e) {
                console.error(`[ModalManager] Error in onOpen for ${modalId}:`, e);
            }
        }
    }

    /**
     * Closes an active modal.
     * @param {string} modalId
     */
    close(modalId) {
        const targetId = modalId || this.activeModalId;
        if (!targetId) return;

        const el = document.getElementById(targetId);
        if (el) {
            el.classList.remove('open');
            el.classList.remove('active');
        }

        const handler = this.modals.get(targetId);
        if (handler && handler.onClose) {
            try {
                handler.onClose();
            } catch (e) {
                console.error(`[ModalManager] Error in onClose for ${targetId}:`, e);
            }
        }

        if (this.activeModalId === targetId) {
            this.activeModalId = null;
        }
    }
}

export const modalManager = new ModalManager();
