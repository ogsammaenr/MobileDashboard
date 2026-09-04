package discovery

import (
	"fmt"
	"net"
	"os"
	"strings"
)

const (
	DefaultDiscoveryPort = 8001
	PingMessage          = "MOBILEDASHBOARD_DISCOVERY_PING"
)

type UDPDiscoveryServer struct {
	httpPort      int
	discoveryPort int
	running       bool
}

func NewUDPDiscoveryServer(httpPort int, discoveryPort int) *UDPDiscoveryServer {
	if discoveryPort <= 0 {
		discoveryPort = DefaultDiscoveryPort
	}
	return &UDPDiscoveryServer{
		httpPort:      httpPort,
		discoveryPort: discoveryPort,
		running:       false,
	}
}

func (s *UDPDiscoveryServer) Start() error {
	addr := net.UDPAddr{
		Port: s.discoveryPort,
		IP:   net.ParseIP("0.0.0.0"),
	}

	conn, err := net.ListenUDP("udp4", &addr)
	if err != nil {
		return fmt.Errorf("failed to bind UDP discovery port %d: %w", s.discoveryPort, err)
	}
	defer conn.Close()

	s.running = true
	hostname, _ := os.Hostname()
	buf := make([]byte, 1024)

	fmt.Printf("\033[35m\033[1m[📡 UDP DISCOVERY]\033[0m UDP Port :%d dinleniyor (Sıfır Yapılandırma Keşif Aktif)\n", s.discoveryPort)

	for s.running {
		n, clientAddr, err := conn.ReadFromUDP(buf)
		if err != nil {
			if !s.running {
				break
			}
			continue
		}

		msg := strings.TrimSpace(string(buf[:n]))
		if strings.Contains(msg, PingMessage) {
			response := fmt.Sprintf("MOBILEDASHBOARD_DISCOVERY_PONG|PORT=%d|HOSTNAME=%s|OS=linux", s.httpPort, hostname)
			_, writeErr := conn.WriteToUDP([]byte(response), clientAddr)
			if writeErr != nil {
				fmt.Printf("\033[31m[📡 UDP HATA]\033[0m %s adresine PONG gönderilemedi: %v\n", clientAddr.String(), writeErr)
			} else {
				fmt.Printf("\033[35m[📡 UDP KEŞİF]\033[0m Cihaz: \033[36m%s\033[0m -> PONG Gönderildi (Host: %s, Port: %d)\n", clientAddr.String(), hostname, s.httpPort)
			}
		} else {
			fmt.Printf("\033[33m[📡 UDP PAKET]\033[0m %s -> Bilinmeyen paket: %q\n", clientAddr.String(), msg)
		}
	}

	return nil
}

func (s *UDPDiscoveryServer) Stop() {
	s.running = false
}
