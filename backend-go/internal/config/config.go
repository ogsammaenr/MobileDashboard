package config

import (
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"sync"
	"time"
)

type AuthorizedClient struct {
	ID           string    `json:"id"`
	Name         string    `json:"name"`
	IP           string    `json:"ip"`
	AuthorizedAt time.Time `json:"authorized_at"`
	LastSeen     time.Time `json:"last_seen"`
}

type AppConfig struct {
	ServerName            string                       `json:"server_name"`
	Port                  int                          `json:"port"`
	DiscoveryPort         int                          `json:"discovery_port"`
	AutoAcceptConnections bool                         `json:"auto_accept_connections"`
	AuthorizedClients     map[string]*AuthorizedClient `json:"authorized_clients"`
	BlockedIPs            []string                     `json:"blocked_ips"`

	mu         sync.RWMutex `json:"-"`
	configPath string       `json:"-"`
}

func DefaultConfig() *AppConfig {
	hostname, _ := os.Hostname()
	if hostname == "" {
		hostname = "MobileDashboard PC"
	}

	return &AppConfig{
		ServerName:            hostname,
		Port:                  8000,
		DiscoveryPort:         8001,
		AutoAcceptConnections: true, // Varsayılan olarak açık, config.json üzerinden kapatılabilir
		AuthorizedClients:     make(map[string]*AuthorizedClient),
		BlockedIPs:            make([]string, 0),
		configPath:            "config.json",
	}
}

// LoadConfig loads config from path or creates default if not exists.
func LoadConfig(configPath string) (*AppConfig, error) {
	if configPath == "" {
		configPath = "config.json"
	}

	possiblePaths := []string{
		configPath,
		filepath.Join(".", configPath),
		filepath.Join("..", configPath),
	}

	var data []byte
	var actualPath string
	var err error

	for _, p := range possiblePaths {
		data, err = os.ReadFile(p)
		if err == nil {
			actualPath = p
			break
		}
	}

	if err != nil || len(data) == 0 {
		// File does not exist, create default config
		cfg := DefaultConfig()
		cfg.configPath = configPath
		_ = cfg.Save()
		return cfg, nil
	}

	cfg := DefaultConfig()
	if err := json.Unmarshal(data, cfg); err != nil {
		return nil, fmt.Errorf("config.json okunamadı: %w", err)
	}

	if cfg.AuthorizedClients == nil {
		cfg.AuthorizedClients = make(map[string]*AuthorizedClient)
	}
	if cfg.BlockedIPs == nil {
		cfg.BlockedIPs = make([]string, 0)
	}
	cfg.configPath = actualPath

	return cfg, nil
}

// Save saves current configuration to disk safely.
func (c *AppConfig) Save() error {
	c.mu.RLock()
	defer c.mu.RUnlock()

	targetPath := c.configPath
	if targetPath == "" {
		targetPath = "config.json"
	}

	data, err := json.MarshalIndent(c, "", "  ")
	if err != nil {
		return fmt.Errorf("config JSON serialize hatası: %w", err)
	}

	tmpPath := targetPath + ".tmp"
	if err := os.WriteFile(tmpPath, data, 0644); err != nil {
		return fmt.Errorf("config.json yazılamadı: %w", err)
	}

	return os.Rename(tmpPath, targetPath)
}

func (c *AppConfig) SetAutoAccept(enabled bool) error {
	c.mu.Lock()
	c.AutoAcceptConnections = enabled
	c.mu.Unlock()
	return c.Save()
}

func (c *AppConfig) IsAutoAccept() bool {
	c.mu.RLock()
	defer c.mu.RUnlock()
	return c.AutoAcceptConnections
}

func (c *AppConfig) IsClientAuthorized(clientID string, ip string) bool {
	c.mu.RLock()
	defer c.mu.RUnlock()

	// Localhost and internal loopback always authorized (Web Admin / Local tools)
	if ip == "127.0.0.1" || ip == "::1" || ip == "localhost" || strings.HasPrefix(ip, "127.") {
		return true
	}

	// Check if IP is blocked
	for _, blocked := range c.BlockedIPs {
		if blocked == ip {
			return false
		}
	}

	// If AutoAccept is enabled, client is automatically authorized
	if c.AutoAcceptConnections {
		return true
	}

	// Check by ID or IP
	if clientID != "" {
		if _, ok := c.AuthorizedClients[clientID]; ok {
			return true
		}
	}

	if ip != "" {
		if _, ok := c.AuthorizedClients[ip]; ok {
			return true
		}
		// Check if any authorized client matches this IP
		for _, auth := range c.AuthorizedClients {
			if auth != nil && auth.IP == ip {
				return true
			}
		}
	}

	return false
}

func (c *AppConfig) AuthorizeClient(id string, name string, ip string) error {
	c.mu.Lock()
	if c.AuthorizedClients == nil {
		c.AuthorizedClients = make(map[string]*AuthorizedClient)
	}

	key := id
	if key == "" {
		key = ip
	}

	c.AuthorizedClients[key] = &AuthorizedClient{
		ID:           key,
		Name:         name,
		IP:           ip,
		AuthorizedAt: time.Now(),
		LastSeen:     time.Now(),
	}
	c.mu.Unlock()

	return c.Save()
}

func (c *AppConfig) RevokeClient(id string) error {
	c.mu.Lock()
	delete(c.AuthorizedClients, id)
	c.mu.Unlock()
	return c.Save()
}

func (c *AppConfig) UpdateClientLastSeen(id string, ip string) {
	c.mu.Lock()
	defer c.mu.Unlock()

	key := id
	if key == "" {
		key = ip
	}

	if client, ok := c.AuthorizedClients[key]; ok {
		client.LastSeen = time.Now()
		if ip != "" {
			client.IP = ip
		}
	}
}

func (c *AppConfig) GetAuthorizedClientsList() []*AuthorizedClient {
	c.mu.RLock()
	defer c.mu.RUnlock()

	list := make([]*AuthorizedClient, 0, len(c.AuthorizedClients))
	for _, client := range c.AuthorizedClients {
		list = append(list, client)
	}
	return list
}
