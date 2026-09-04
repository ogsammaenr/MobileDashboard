package main

import (
	"flag"
	"fmt"
	"log"
	"net"
	"os"
	"os/signal"
	"syscall"

	"github.com/mobiledashboard/backend-go/internal/config"
	"github.com/mobiledashboard/backend-go/internal/discovery"
	"github.com/mobiledashboard/backend-go/internal/hardware"
	"github.com/mobiledashboard/backend-go/internal/media"
	"github.com/mobiledashboard/backend-go/internal/server"
)

func getLocalIPs() []string {
	var ips []string
	// Connect to public dummy IP to find preferred outbound IP
	conn, err := net.Dial("udp", "8.8.8.8:80")
	if err == nil {
		localAddr := conn.LocalAddr().(*net.UDPAddr)
		ips = append(ips, localAddr.IP.String())
		conn.Close()
	}

	// Also enumerate all interfaces
	ifaces, err := net.Interfaces()
	if err == nil {
		for _, i := range ifaces {
			addrs, err := i.Addrs()
			if err != nil {
				continue
			}
			for _, addr := range addrs {
				var ip net.IP
				switch v := addr.(type) {
				case *net.IPNet:
					ip = v.IP
				case *net.IPAddr:
					ip = v.IP
				}
				if ip != nil && !ip.IsLoopback() && ip.To4() != nil {
					ipStr := ip.String()
					// Deduplicate
					found := false
					for _, existing := range ips {
						if existing == ipStr {
							found = true
							break
						}
					}
					if !found {
						ips = append(ips, ipStr)
					}
				}
			}
		}
	}

	if len(ips) == 0 {
		ips = append(ips, "127.0.0.1")
	}
	return ips
}

func printBanner(ips []string, cfg *config.AppConfig) {
	fmt.Println("\033[36m\033[1m")
	fmt.Println("  __  __       _     _ _      _____            _     _                         _ ")
	fmt.Println(" |  \\/  |     | |   (_) |    |  __ \\          | |   | |                       | |")
	fmt.Println(" | \\  / | ___ | |__  _| | ___| |  | | __ _ ___| |__ | |__   ___   __ _ _ __ __| |")
	fmt.Println(" | |\\/| |/ _ \\| '_ \\| | |/ _ \\ |  | |/ _` / __| '_ \\| '_ \\ / _ \\ / _` | '__/ _` |")
	fmt.Println(" | |  | | (_) | |_) | | |  __/ |__| | (_| \\__ \\ | | | |_) | (_) | (_| | | | (_| |")
	fmt.Println(" |_|  |_|\\___/|_.__/|_|_|\\___|_____/ \\__,_|___/_| |_|_.__/ \\___/ \\__,_|_|  \\__,_|")
	fmt.Println("                      [ GO NATIVE BACKEND v2.0 ]")
	fmt.Println("\033[0m")

	fmt.Println("\033[32m\033[1m================================================================\033[0m")
	fmt.Println("\033[32m\033[1m🚀 GO TELEMETRİ & MEDYA SUNUCUSU BAŞLATILDI!\033[0m")
	fmt.Println("\033[32m\033[1m================================================================\033[0m")
	fmt.Printf("\033[1m💻 Web Admin & Yönetim:\033[0m \033[36mhttp://localhost:%d/admin\033[0m\n", cfg.Port)
	fmt.Printf("\033[1m📡 WebSocket Endpoint:\033[0m   \033[36mws://localhost:%d/ws\033[0m\n", cfg.Port)
	fmt.Printf("\033[1m🔍 UDP Auto-Discovery:\033[0m   \033[33mPort %d (Aktif)\033[0m\n", cfg.DiscoveryPort)

	if cfg.AutoAcceptConnections {
		fmt.Printf("\033[1m🛡️ Otomatik Bağlantı:\033[0m   \033[32m\033[1m[AÇIK] - Cihazlar doğrudan bağlanabilir\033[0m\n")
	} else {
		fmt.Printf("\033[1m🛡️ Otomatik Bağlantı:\033[0m   \033[33m\033[1m[KAPALI - Eşleşme Onayı Gerekir]\033[0m\n")
	}

	for _, ip := range ips {
		fmt.Printf("\033[1m📱 Android İstemci URL:\033[0m  \033[32m\033[1mhttp://%s:%d\033[0m\n", ip, cfg.Port)
	}
	fmt.Println("\033[32m\033[1m================================================================\033[0m")
	fmt.Println("\033[33mAndroid uygulamanız veya Web Admin paneliniz anında bağlanabilir.\033[0m")
	fmt.Println("\033[33mDurdurmak için: CTRL + C\033[0m")
}

func main() {
	portFlag := flag.Int("port", 0, "HTTP server port (default 8000)")
	discoveryFlag := flag.Int("discovery-port", 0, "UDP discovery port (default 8001)")
	autoAcceptFlag := flag.Bool("auto-accept", false, "Auto accept incoming client pairings")
	flag.Parse()

	// Load or create config.json
	cfg, err := config.LoadConfig("config.json")
	if err != nil {
		log.Printf("[Config Warning] %v (Varsayılan ayarlar kullanılıyor)", err)
		cfg = config.DefaultConfig()
	}

	if *portFlag > 0 {
		cfg.Port = *portFlag
	}
	if *discoveryFlag > 0 {
		cfg.DiscoveryPort = *discoveryFlag
	}
	if *autoAcceptFlag {
		cfg.AutoAcceptConnections = true
	}

	collector := hardware.NewCollector()
	mediaCtrl := media.NewController()
	httpServer := server.NewServer(cfg.Port, collector, mediaCtrl, cfg)
	discoveryServer := discovery.NewUDPDiscoveryServer(cfg.Port, cfg.DiscoveryPort)

	// Start UDP Discovery Server in background goroutine
	go func() {
		if err := discoveryServer.Start(); err != nil {
			log.Printf("[UDP Discovery Error] %v", err)
		}
	}()

	ips := getLocalIPs()
	printBanner(ips, cfg)

	// Listen for OS signals for graceful shutdown
	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, os.Interrupt, syscall.SIGTERM)

	go func() {
		<-sigChan
		fmt.Println("\n\033[33m[!] Sunucu kapatılıyor...\033[0m")
		discoveryServer.Stop()
		os.Exit(0)
	}()

	// Start HTTP/WebSocket Server (Blocking)
	if err := httpServer.Start(); err != nil {
		log.Fatalf("[Server Error] %v", err)
	}
}
