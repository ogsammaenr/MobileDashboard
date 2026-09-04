// ==============================================================================
// REST API CLIENT SERVICE
// ==============================================================================

export const api = {
    async fetchLayouts() {
        try {
            const res = await fetch('/api/layouts?_t=' + Date.now());
            if (!res.ok) throw new Error('Layouts fetch failed');
            return await res.json();
        } catch (e) {
            console.error('[API Error] fetchLayouts:', e);
            return [];
        }
    },

    async saveLayouts(layouts) {
        const res = await fetch('/api/layouts', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(layouts)
        });
        if (!res.ok) throw new Error('Layouts save failed');
        return await res.json();
    },

    async fetchClients() {
        try {
            const res = await fetch('/api/clients?_t=' + Date.now());
            if (!res.ok) throw new Error('Clients fetch failed');
            return await res.json();
        } catch (e) {
            console.error('[API Error] fetchClients:', e);
            return null;
        }
    },

    async fetchConfig() {
        try {
            const res = await fetch('/api/config?_t=' + Date.now());
            if (!res.ok) throw new Error('Config fetch failed');
            return await res.json();
        } catch (e) {
            console.error('[API Error] fetchConfig:', e);
            return null;
        }
    },

    async toggleAutoAccept(enabled) {
        const res = await fetch('/api/config', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ auto_accept_connections: enabled })
        });
        if (!res.ok) throw new Error('Toggle auto-accept failed');
        return await res.json();
    },

    async approveClient(clientId) {
        const res = await fetch('/api/clients/approve', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ client_id: clientId })
        });
        if (!res.ok) throw new Error('Approve client failed');
        return await res.json();
    },

    async rejectClient(clientId) {
        const res = await fetch('/api/clients/reject', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ client_id: clientId })
        });
        if (!res.ok) throw new Error('Reject client failed');
        return await res.json();
    },

    async disconnectClient(clientId) {
        const res = await fetch('/api/clients/disconnect', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ client_id: clientId })
        });
        if (!res.ok) throw new Error('Disconnect client failed');
        return await res.json();
    },

    async deleteAuthorizedClient(clientId) {
        const res = await fetch('/api/clients/delete', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ client_id: clientId })
        });
        if (!res.ok) throw new Error('Delete client failed');
        return await res.json();
    },

    async updateTheme(themeName) {
        const res = await fetch('/api/theme', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ theme: themeName })
        });
        if (!res.ok) throw new Error('Theme update failed');
        return await res.json();
    },

    async fetchInstalledApps() {
        try {
            const res = await fetch('/api/apps/list?_t=' + Date.now());
            if (!res.ok) throw new Error('Fetch apps list failed');
            return await res.json();
        } catch (e) {
            console.error('[API Error] fetchInstalledApps:', e);
            return [];
        }
    },

    async fetchAppInfo(path) {
        try {
            const res = await fetch('/api/apps/info?path=' + encodeURIComponent(path));
            if (!res.ok) throw new Error('Fetch app info failed');
            return await res.json();
        } catch (e) {
            console.error('[API Error] fetchAppInfo:', e);
            return null;
        }
    }
};
