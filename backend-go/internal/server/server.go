package server

import (
	"encoding/json"
	"fmt"
	"io"
	"net"
	"net/http"
	"net/url"
	"os"
	"path/filepath"
	"strings"
	"sync"
	"time"

	"github.com/gorilla/websocket"
	"github.com/mobiledashboard/backend-go/internal/apps"
	"github.com/mobiledashboard/backend-go/internal/config"
	"github.com/mobiledashboard/backend-go/internal/hardware"
	"github.com/mobiledashboard/backend-go/internal/media"
	"github.com/mobiledashboard/backend-go/internal/models"
)

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool {
		return true // Allow all origins (LAN and local web admin)
	},
}

// SafeWebSocketConn wraps a Gorilla WebSocket with a mutex to guarantee thread-safe writes
// and prevent "concurrent write to websocket connection" panics and sudden disconnections.
type SafeWebSocketConn struct {
	conn *websocket.Conn
	mu   sync.Mutex
}

func NewSafeWebSocketConn(conn *websocket.Conn) *SafeWebSocketConn {
	return &SafeWebSocketConn{conn: conn}
}

func (s *SafeWebSocketConn) WriteMessage(messageType int, data []byte) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	_ = s.conn.SetWriteDeadline(time.Now().Add(5 * time.Second))
	return s.conn.WriteMessage(messageType, data)
}

func (s *SafeWebSocketConn) WriteJSON(v interface{}) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	_ = s.conn.SetWriteDeadline(time.Now().Add(5 * time.Second))
	return s.conn.WriteJSON(v)
}

func (s *SafeWebSocketConn) Close() error {
	s.mu.Lock()
	defer s.mu.Unlock()
	return s.conn.Close()
}

func (s *SafeWebSocketConn) Underlying() *websocket.Conn {
	return s.conn
}

type PendingConn struct {
	Conn        *SafeWebSocketConn
	Info        *models.PendingClientRequest
	ApproveChan chan bool
}

type Server struct {
	port          int
	collector     *hardware.Collector
	mediaCtrl     *media.Controller
	cfg           *config.AppConfig
	activeClients map[*SafeWebSocketConn]*models.ConnectedClientInfo
	pendingConns  map[string]*PendingConn // Key: client_id
	mu            sync.RWMutex
}

func NewServer(port int, collector *hardware.Collector, mediaCtrl *media.Controller, cfg *config.AppConfig) *Server {
	if cfg == nil {
		cfg = config.DefaultConfig()
	}
	return &Server{
		port:          port,
		collector:     collector,
		mediaCtrl:     mediaCtrl,
		cfg:           cfg,
		activeClients: make(map[*SafeWebSocketConn]*models.ConnectedClientInfo),
		pendingConns:  make(map[string]*PendingConn),
	}
}

func (s *Server) Start() error {
	mux := http.NewServeMux()

	// CORS Middleware Wrapper
	corsHandler := func(next http.HandlerFunc) http.HandlerFunc {
		return func(w http.ResponseWriter, r *http.Request) {
			w.Header().Set("Access-Control-Allow-Origin", "*")
			w.Header().Set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
			w.Header().Set("Access-Control-Allow-Headers", "Content-Type")
			if r.Method == http.MethodOptions {
				w.WriteHeader(http.StatusOK)
				return
			}
			next(w, r)
		}
	}

	// 1. WebSocket Telemetry & Pairing Stream
	mux.HandleFunc("/ws", s.handleWebSocket)

	// 2. Status & Config APIs
	mux.HandleFunc("/api/status", corsHandler(s.handleStatus))
	mux.HandleFunc("/api/config", corsHandler(s.handleConfig))

	// 3. Client & Pairing Management APIs
	mux.HandleFunc("/api/clients", corsHandler(s.handleClients))
	mux.HandleFunc("/api/clients/approve", corsHandler(s.handleApproveClient))
	mux.HandleFunc("/api/clients/reject", corsHandler(s.handleRejectClient))
	mux.HandleFunc("/api/clients/disconnect", corsHandler(s.handleDisconnectClient))
	mux.HandleFunc("/api/clients/delete", corsHandler(s.handleDeleteClient))

	// 4. Media & System Control APIs
	mux.HandleFunc("/api/media/control", corsHandler(s.handleMediaControl))
	mux.HandleFunc("/api/system/control", corsHandler(s.handleSystemControl))
	mux.HandleFunc("/api/media/cover", corsHandler(s.handleMediaCover))

	// 5. App Launcher & Icon APIs
	mux.HandleFunc("/api/apps/list", corsHandler(s.handleAppsList))
	mux.HandleFunc("/api/apps/info", corsHandler(s.handleAppInfo))
	mux.HandleFunc("/api/apps/icon", corsHandler(s.handleAppIcon))

	// 6. Layouts & Themes API
	mux.HandleFunc("/api/layouts", corsHandler(s.handleLayouts))
	mux.HandleFunc("/api/theme", corsHandler(s.handleTheme))

	// 6. Admin UI & Static Assets
	webDir := s.getWebDir()
	fileServer := http.FileServer(http.Dir(webDir))
	mux.Handle("/css/", fileServer)
	mux.Handle("/js/", fileServer)

	mux.HandleFunc("/admin", s.handleAdminPage)
	mux.HandleFunc("/", s.handleAdminPage)

	// Start Background Telemetry Broadcast Loop (1 Hz) & Heartbeat (20s)
	go s.broadcastLoop()
	go s.heartbeatLoop()

	loggingHandler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		start := time.Now()
		clientIP := getClientIP(r)
		mux.ServeHTTP(w, r)
		if strings.HasPrefix(r.URL.Path, "/api/") || r.URL.Path == "/admin" || r.URL.Path == "/" {
			fmt.Printf("\033[34m[🌐 HTTP %s]\033[0m IP: \033[36m%s\033[0m | Yol: \033[32m%s\033[0m (%v)\n", r.Method, clientIP, r.URL.RequestURI(), time.Since(start).Round(time.Millisecond))
		}
	})

	addr := fmt.Sprintf("0.0.0.0:%d", s.port)
	fmt.Printf("\033[36m\033[1m[🚀 MobileDashboard Native Go Server v2.0]\033[0m\n")
	fmt.Printf("  • Web Admin Paneli   : \033[32mhttp://localhost:%d/admin\033[0m\n", s.port)
	fmt.Printf("  • Canlı Telemetri WS : \033[32mws://localhost:%d/ws\033[0m\n", s.port)
	fmt.Printf("  • REST API           : \033[32mhttp://localhost:%d/api/status\033[0m\n", s.port)
	fmt.Printf("  • Otomatik Bağlantı : \033[33m%v\033[0m\n\n", s.cfg.IsAutoAccept())

	return http.ListenAndServe(addr, loggingHandler)
}

func getClientIP(r *http.Request) string {
	forwarded := r.Header.Get("X-Forwarded-For")
	if forwarded != "" {
		parts := strings.Split(forwarded, ",")
		return strings.TrimSpace(parts[0])
	}
	host, _, err := net.SplitHostPort(r.RemoteAddr)
	if err == nil {
		return host
	}
	return r.RemoteAddr
}

func (s *Server) handleWebSocket(w http.ResponseWriter, r *http.Request) {
	rawConn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		return
	}
	safeConn := NewSafeWebSocketConn(rawConn)
	defer safeConn.Close()

	clientIP := getClientIP(r)
	query := r.URL.Query()
	deviceID := query.Get("device_id")
	deviceName := query.Get("device_name")
	role := query.Get("role")
	userAgent := r.UserAgent()

	isLocalAdmin := role == "admin" || clientIP == "127.0.0.1" || clientIP == "::1" || strings.HasPrefix(clientIP, "127.")

	if deviceID == "" {
		if isLocalAdmin {
			deviceID = "local_web_admin"
		} else {
			deviceID = fmt.Sprintf("dev_%s", strings.ReplaceAll(clientIP, ".", "_"))
		}
	}
	if deviceName == "" {
		if isLocalAdmin {
			deviceName = "Web Admin Panel"
		} else if strings.Contains(userAgent, "Android") {
			deviceName = "Android Cihaz"
		} else {
			deviceName = fmt.Sprintf("Mobil İstemci (%s)", clientIP)
		}
	}

	fmt.Printf("\033[36m\033[1m[📡 WS BAĞLANTI İSTEĞİ]\033[0m IP: \033[32m%s\033[0m | Cihaz: \033[1m%s\033[0m (ID: %s, Rol: %s)\n", clientIP, deviceName, deviceID, role)

	// WebSocket bağlantı limitleri ve Heartbeat (Ping/Pong)
	rawConn.SetReadLimit(512 * 1024)
	_ = rawConn.SetReadDeadline(time.Now().Add(60 * time.Second))
	rawConn.SetPongHandler(func(string) error {
		_ = rawConn.SetReadDeadline(time.Now().Add(60 * time.Second))
		return nil
	})
	rawConn.SetPingHandler(func(appData string) error {
		_ = rawConn.SetReadDeadline(time.Now().Add(60 * time.Second))
		return safeConn.WriteMessage(websocket.PongMessage, []byte(appData))
	})

	// Yetkilendirme kontrolü
	isAuthorized := isLocalAdmin || s.cfg.IsClientAuthorized(deviceID, clientIP)

	if !isAuthorized {
		approveChan := make(chan bool, 1)
		pendingReq := &models.PendingClientRequest{
			ID:          deviceID,
			IP:          clientIP,
			DeviceName:  deviceName,
			UserAgent:   userAgent,
			RequestedAt: time.Now(),
		}

		s.mu.Lock()
		s.pendingConns[deviceID] = &PendingConn{
			Conn:        safeConn,
			Info:        pendingReq,
			ApproveChan: approveChan,
		}
		s.mu.Unlock()

		fmt.Printf("\033[33m\033[1m[🟡 BAĞLANTI İSTEĞİ]\033[0m IP: \033[36m%s\033[0m | Cihaz: \033[32m%s\033[0m (Onay Bekleniyor)\n", clientIP, deviceName)

		_ = safeConn.WriteJSON(map[string]interface{}{
			"type":        "PAIRING_PENDING",
			"client_id":   deviceID,
			"device_name": deviceName,
			"message":     "Bağlantı isteği sunucuya iletildi. Lütfen masaüstü panelinden onaylayın.",
		})

		s.broadcastClientsUpdate()

		clientClosed := make(chan struct{})
		go func() {
			for {
				if _, _, readErr := rawConn.ReadMessage(); readErr != nil {
					close(clientClosed)
					return
				}
				_ = rawConn.SetReadDeadline(time.Now().Add(60 * time.Second))
			}
		}()

		select {
		case approved := <-approveChan:
			if !approved {
				_ = safeConn.WriteJSON(map[string]string{
					"type":    "PAIRING_REJECTED",
					"message": "Bağlantı isteği sunucu tarafından reddedildi.",
				})
				return
			}
		case <-clientClosed:
			s.mu.Lock()
			delete(s.pendingConns, deviceID)
			s.mu.Unlock()
			s.broadcastClientsUpdate()
			return
		case <-time.After(5 * time.Minute):
			s.mu.Lock()
			delete(s.pendingConns, deviceID)
			s.mu.Unlock()
			_ = safeConn.WriteJSON(map[string]string{
				"type":    "PAIRING_TIMEOUT",
				"message": "Bağlantı isteği zaman aşımına uğradı.",
			})
			s.broadcastClientsUpdate()
			return
		}
	}

	// İstemci onaylandı veya otomatik kabul edildi
	clientInfo := &models.ConnectedClientInfo{
		ID:          deviceID,
		IP:          clientIP,
		DeviceName:  deviceName,
		UserAgent:   userAgent,
		ConnectedAt: time.Now(),
		LastActive:  time.Now(),
		Status:      "authorized",
		IsActive:    true,
	}

	s.mu.Lock()
	delete(s.pendingConns, deviceID)
	s.activeClients[safeConn] = clientInfo
	s.mu.Unlock()

	_ = s.cfg.AuthorizeClient(deviceID, deviceName, clientIP)

	fmt.Printf("\033[32m\033[1m[🟢 CİHAZ BAĞLANDI]\033[0m IP: \033[36m%s\033[0m | Cihaz: \033[32m%s\033[0m (Aktif)\n", clientIP, deviceName)

	_ = safeConn.WriteJSON(map[string]interface{}{
		"type":        "PAIRING_APPROVED",
		"client_id":   deviceID,
		"device_name": deviceName,
		"status":      "authorized",
	})

	// İlk telemetri paketini anında gönder
	payload := s.getMergedTelemetry()
	_ = safeConn.WriteJSON(payload)

	s.broadcastClientsUpdate()

	// Okuma döngüsü
	for {
		_, msg, err := rawConn.ReadMessage()
		if err != nil {
			break
		}
		_ = rawConn.SetReadDeadline(time.Now().Add(60 * time.Second))

		s.mu.Lock()
		if info, ok := s.activeClients[safeConn]; ok {
			info.LastActive = time.Now()
		}
		s.mu.Unlock()

		if len(msg) > 0 {
			var cmd map[string]interface{}
			if err := json.Unmarshal(msg, &cmd); err == nil {
				if cmdType, ok := cmd["type"].(string); ok && cmdType == "PING" {
					_ = safeConn.WriteJSON(map[string]string{"type": "PONG"})
				}
			}
		}
	}

	s.mu.Lock()
	delete(s.activeClients, safeConn)
	s.mu.Unlock()

	fmt.Printf("\033[31m[🔴 CİHAZ AYRILDI]\033[0m IP: %s | Cihaz: %s\n", clientIP, deviceName)
	s.broadcastClientsUpdate()
}

func (s *Server) broadcastLoop() {
	ticker := time.NewTicker(1000 * time.Millisecond)
	defer ticker.Stop()

	for range ticker.C {
		s.mu.RLock()
		clientCount := len(s.activeClients)
		if clientCount == 0 {
			s.mu.RUnlock()
			continue
		}

		conns := make([]*SafeWebSocketConn, 0, clientCount)
		for conn := range s.activeClients {
			conns = append(conns, conn)
		}
		s.mu.RUnlock()

		payload := s.getMergedTelemetry()
		data, err := json.Marshal(payload)
		if err != nil {
			continue
		}

		var deadConns []*SafeWebSocketConn
		for _, conn := range conns {
			if err := conn.WriteMessage(websocket.TextMessage, data); err != nil {
				deadConns = append(deadConns, conn)
			}
		}

		if len(deadConns) > 0 {
			s.mu.Lock()
			for _, conn := range deadConns {
				conn.Close()
				delete(s.activeClients, conn)
			}
			s.mu.Unlock()
			s.broadcastClientsUpdate()
		}
	}
}

// heartbeatLoop sends periodic ping frames every 20 seconds to keep connections alive
// and prevent silent timeout disconnections from browsers and mobile clients.
func (s *Server) heartbeatLoop() {
	ticker := time.NewTicker(20 * time.Second)
	defer ticker.Stop()

	for range ticker.C {
		s.mu.RLock()
		conns := make([]*SafeWebSocketConn, 0, len(s.activeClients))
		for conn := range s.activeClients {
			conns = append(conns, conn)
		}
		s.mu.RUnlock()

		for _, conn := range conns {
			_ = conn.WriteMessage(websocket.PingMessage, []byte("heartbeat"))
		}
	}
}

func (s *Server) broadcastClientsUpdate() {
	msg := []byte(`{"type":"CLIENTS_UPDATED"}`)
	s.mu.RLock()
	conns := make([]*SafeWebSocketConn, 0, len(s.activeClients))
	for conn := range s.activeClients {
		conns = append(conns, conn)
	}
	s.mu.RUnlock()

	for _, conn := range conns {
		_ = conn.WriteMessage(websocket.TextMessage, msg)
	}
}

func (s *Server) getMergedTelemetry() models.TelemetryPayload {
	payload := s.collector.CollectAll()
	payload.Media = s.mediaCtrl.GetMedia()
	payload.Audio = s.mediaCtrl.GetAudio()
	return payload
}

func (s *Server) handleStatus(w http.ResponseWriter, r *http.Request) {
	hostname, _ := os.Hostname()
	resp := models.StatusResponse{
		Status:   "online",
		Version:  "2.0.0-go",
		Hostname: hostname,
		Port:     s.port,
	}
	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(resp)
}

func (s *Server) handleConfig(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")

	if r.Method == http.MethodGet {
		_ = json.NewEncoder(w).Encode(s.cfg)
		return
	}

	if r.Method == http.MethodPost {
		var req models.UpdateConfigRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			http.Error(w, "Geçersiz JSON", http.StatusBadRequest)
			return
		}

		if req.AutoAcceptConnections != nil {
			_ = s.cfg.SetAutoAccept(*req.AutoAcceptConnections)
			fmt.Printf("\033[35m\033[1m[⚙️ AYAR DEĞİŞTİ]\033[0m Otomatik Bağlantı Kabulü: \033[1m%v\033[0m\n", *req.AutoAcceptConnections)
		}

		s.broadcastClientsUpdate()
		_ = json.NewEncoder(w).Encode(map[string]interface{}{
			"status": "ok",
			"config": s.cfg,
		})
		return
	}

	http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
}

func (s *Server) handleClients(w http.ResponseWriter, r *http.Request) {
	s.mu.RLock()
	connectedList := make([]*models.ConnectedClientInfo, 0, len(s.activeClients))
	for _, c := range s.activeClients {
		connectedList = append(connectedList, c)
	}

	pendingList := make([]*models.PendingClientRequest, 0, len(s.pendingConns))
	for _, p := range s.pendingConns {
		pendingList = append(pendingList, p.Info)
	}
	s.mu.RUnlock()

	resp := models.ClientsStatusResponse{
		AutoAcceptEnabled: s.cfg.IsAutoAccept(),
		ConnectedClients:  connectedList,
		PendingRequests:   pendingList,
		AuthorizedCount:   len(s.cfg.GetAuthorizedClientsList()),
	}

	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(resp)
}

func (s *Server) handleApproveClient(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req models.ClientActionRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Geçersiz istek gövdesi", http.StatusBadRequest)
		return
	}

	s.mu.Lock()
	pending, exists := s.pendingConns[req.ClientID]
	if !exists {
		s.mu.Unlock()
		http.Error(w, "Bekleyen istek bulunamadı", http.StatusNotFound)
		return
	}

	delete(s.pendingConns, req.ClientID)
	s.mu.Unlock()

	select {
	case pending.ApproveChan <- true:
	default:
	}

	s.broadcastClientsUpdate()

	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(map[string]string{"status": "ok", "message": "İstemci onaylandı"})
}

func (s *Server) handleRejectClient(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req models.ClientActionRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Geçersiz istek", http.StatusBadRequest)
		return
	}

	s.mu.Lock()
	pending, exists := s.pendingConns[req.ClientID]
	if !exists {
		s.mu.Unlock()
		http.Error(w, "Bekleyen istek bulunamadı", http.StatusNotFound)
		return
	}

	delete(s.pendingConns, req.ClientID)
	s.mu.Unlock()

	select {
	case pending.ApproveChan <- false:
	default:
	}

	s.broadcastClientsUpdate()

	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(map[string]string{"status": "ok", "message": "İstemci reddedildi"})
}

func (s *Server) handleDisconnectClient(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req models.ClientActionRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Geçersiz istek", http.StatusBadRequest)
		return
	}

	s.mu.Lock()
	var targetConn *SafeWebSocketConn
	for conn, info := range s.activeClients {
		if info.ID == req.ClientID || info.IP == req.ClientID {
			targetConn = conn
			break
		}
	}
	s.mu.Unlock()

	if targetConn != nil {
		_ = targetConn.WriteJSON(map[string]string{"type": "DISCONNECTED_BY_SERVER", "message": "Sunucu bağlantınızı kesti."})
		targetConn.Close()
	}

	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(map[string]string{"status": "ok", "message": "İstemci bağlantısı kesildi"})
}

func (s *Server) handleDeleteClient(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req models.ClientActionRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Geçersiz istek", http.StatusBadRequest)
		return
	}

	_ = s.cfg.RevokeClient(req.ClientID)

	// Aktifse bağlantısını da kes
	s.handleDisconnectClient(w, r)
}

func (s *Server) handleTheme(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req struct {
		Theme string `json:"theme"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Geçersiz tema JSON", http.StatusBadRequest)
		return
	}

	if req.Theme == "" {
		http.Error(w, "Tema adı boş olamaz", http.StatusBadRequest)
		return
	}

	// Bağlı tüm istemcilere tema güncellemesi yayınla
	msg, _ := json.Marshal(map[string]string{
		"type":  "THEME_UPDATED",
		"theme": req.Theme,
	})

	s.mu.RLock()
	conns := make([]*SafeWebSocketConn, 0, len(s.activeClients))
	for conn := range s.activeClients {
		conns = append(conns, conn)
	}
	s.mu.RUnlock()

	for _, conn := range conns {
		_ = conn.WriteMessage(websocket.TextMessage, msg)
	}

	fmt.Printf("\033[36m\033[1m[🎨 TEMA DEĞİŞTİ]\033[0m Yeni Tema: \033[32m%s\033[0m (Tüm cihazlara iletildi)\n", req.Theme)

	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(map[string]string{"status": "ok", "theme": req.Theme})
}

func (s *Server) handleMediaControl(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req models.MediaControlRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid JSON", http.StatusBadRequest)
		return
	}

	if err := s.mediaCtrl.ControlMedia(req.Action); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(map[string]string{"status": "ok", "action": req.Action})
}

func (s *Server) handleSystemControl(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req models.SystemControlRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid JSON", http.StatusBadRequest)
		return
	}

	if err := s.mediaCtrl.ExecuteSystemControl(req); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(map[string]string{"status": "ok", "action": req.Action})
}

func (s *Server) handleMediaCover(w http.ResponseWriter, r *http.Request) {
	rawPath := r.URL.Query().Get("path")
	if rawPath == "" {
		http.Error(w, "Missing path parameter", http.StatusBadRequest)
		return
	}

	decodedPath, err := url.QueryUnescape(rawPath)
	if err != nil {
		decodedPath = rawPath
	}

	cleanPath := filepath.Clean(decodedPath)
	file, err := os.Open(cleanPath)
	if err != nil {
		http.Error(w, "File not found", http.StatusNotFound)
		return
	}
	defer file.Close()

	ext := strings.ToLower(filepath.Ext(cleanPath))
	switch ext {
	case ".jpg", ".jpeg":
		w.Header().Set("Content-Type", "image/jpeg")
	case ".png":
		w.Header().Set("Content-Type", "image/png")
	case ".webp":
		w.Header().Set("Content-Type", "image/webp")
	default:
		w.Header().Set("Content-Type", "application/octet-stream")
	}

	_, _ = io.Copy(w, file)
}

func (s *Server) getWebDir() string {
	possibleDirs := []string{"web", "./web", "../web"}
	for _, d := range possibleDirs {
		if stat, err := os.Stat(d); err == nil && stat.IsDir() {
			return d
		}
	}
	return "web"
}

func (s *Server) handleAdminPage(w http.ResponseWriter, r *http.Request) {
	possiblePaths := []string{
		filepath.Join(s.getWebDir(), "admin.html"),
		"web/admin.html",
		"../web/admin.html",
		"./web/admin.html",
	}

	for _, p := range possiblePaths {
		if data, err := os.ReadFile(p); err == nil {
			w.Header().Set("Content-Type", "text/html; charset=utf-8")
			w.Header().Set("Cache-Control", "no-cache")
			_, _ = w.Write(data)
			return
		}
	}

	http.Error(w, "Admin UI (web/admin.html) not found", http.StatusNotFound)
}

func (s *Server) handleLayouts(w http.ResponseWriter, r *http.Request) {
	layoutPaths := []string{
		"layouts.json",
		"./layouts.json",
		"../layouts.json",
	}

	targetPath := "layouts.json"

	if r.Method == http.MethodGet {
		w.Header().Set("Content-Type", "application/json")
		w.Header().Set("Cache-Control", "no-cache")

		for _, p := range layoutPaths {
			if data, err := os.ReadFile(p); err == nil {
				_, _ = w.Write(data)
				return
			}
		}

		_, _ = w.Write([]byte("[]"))
		return
	}

	if r.Method == http.MethodPost {
		var layouts []models.PageLayout
		if err := json.NewDecoder(r.Body).Decode(&layouts); err != nil {
			http.Error(w, "Invalid layout JSON", http.StatusBadRequest)
			return
		}

		data, err := json.MarshalIndent(layouts, "", "  ")
		if err != nil {
			http.Error(w, "JSON encode error", http.StatusInternalServerError)
			return
		}

		if err := os.WriteFile(targetPath, data, 0644); err != nil {
			http.Error(w, fmt.Sprintf("Failed to write layouts.json: %v", err), http.StatusInternalServerError)
			return
		}

		s.broadcastLayoutUpdate()

		w.Header().Set("Content-Type", "application/json")
		_ = json.NewEncoder(w).Encode(map[string]string{"status": "ok", "message": "Layouts saved"})
		return
	}

	http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
}

func (s *Server) broadcastLayoutUpdate() {
	msg := []byte(`{"type":"LAYOUT_UPDATED"}`)
	s.mu.RLock()
	conns := make([]*SafeWebSocketConn, 0, len(s.activeClients))
	for conn := range s.activeClients {
		conns = append(conns, conn)
	}
	s.mu.RUnlock()

	for _, conn := range conns {
		_ = conn.WriteMessage(websocket.TextMessage, msg)
	}
}

func (s *Server) handleAppsList(w http.ResponseWriter, r *http.Request) {
	appList := apps.ScanInstalledApps()
	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(appList)
}

func (s *Server) handleAppInfo(w http.ResponseWriter, r *http.Request) {
	targetPath := r.URL.Query().Get("path")
	if targetPath == "" {
		http.Error(w, "Missing path parameter", http.StatusBadRequest)
		return
	}

	app, err := apps.ParseDesktopFile(targetPath)
	if err != nil {
		// If binary or .exe file
		if info, statErr := os.Stat(targetPath); statErr == nil && !info.IsDir() {
			base := filepath.Base(targetPath)
			name := strings.TrimSuffix(base, filepath.Ext(base))
			app = &apps.DesktopApp{
				ID:      name,
				Name:    name,
				Exec:    targetPath,
				Path:    targetPath,
				IconURL: "/api/apps/icon?name=application-x-executable",
			}
		} else {
			http.Error(w, err.Error(), http.StatusNotFound)
			return
		}
	}

	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(app)
}

func (s *Server) handleAppIcon(w http.ResponseWriter, r *http.Request) {
	iconName := r.URL.Query().Get("name")
	desktopPath := r.URL.Query().Get("path")

	filePath, mimeType := apps.ResolveIconPath(iconName, desktopPath)
	if filePath == "" {
		http.NotFound(w, r)
		return
	}

	w.Header().Set("Content-Type", mimeType)
	w.Header().Set("Cache-Control", "public, max-age=86400")
	http.ServeFile(w, r, filePath)
}
