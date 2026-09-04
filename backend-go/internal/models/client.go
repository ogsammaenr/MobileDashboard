package models

import "time"

type ConnectedClientInfo struct {
	ID          string    `json:"id"`
	IP          string    `json:"ip"`
	DeviceName  string    `json:"device_name"`
	UserAgent   string    `json:"user_agent"`
	ConnectedAt time.Time `json:"connected_at"`
	LastActive  time.Time `json:"last_active"`
	Status      string    `json:"status"` // "authorized", "pending_approval", "rejected"
	IsActive    bool      `json:"is_active"`
}

type PendingClientRequest struct {
	ID          string    `json:"id"`
	IP          string    `json:"ip"`
	DeviceName  string    `json:"device_name"`
	UserAgent   string    `json:"user_agent"`
	RequestedAt time.Time `json:"requested_at"`
}

type ClientsStatusResponse struct {
	AutoAcceptEnabled bool                    `json:"auto_accept_enabled"`
	ConnectedClients  []*ConnectedClientInfo  `json:"connected_clients"`
	PendingRequests   []*PendingClientRequest `json:"pending_requests"`
	AuthorizedCount   int                     `json:"authorized_count"`
}

type ClientActionRequest struct {
	ClientID string `json:"client_id"`
	Action   string `json:"action"` // "approve", "reject", "disconnect", "delete"
}

type UpdateConfigRequest struct {
	AutoAcceptConnections *bool   `json:"auto_accept_connections,omitempty"`
	ServerName            *string `json:"server_name,omitempty"`
}
