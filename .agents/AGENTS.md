# MobileDashboard - AI Agent & Geliştirici Sistem Rehberi (`AGENTS.md`)

Bu doküman, **MobileDashboard v2.0** projesinin sistem mimarisini, Go native backend yapısını, Android Kotlin Jetpack Compose (Material 3) istemcisini ve geliştirme standartlarını açıklar.

---

## 📱 Proje Özeti ve Amacı

**MobileDashboard**, akıllı telefon veya tabletleri yerel ağ (Wi-Fi) üzerinden bilgisayara bağlayarak masaüstünde ikinci bir **donanım izleme, büyük saat ve medya kontrol ekranı (Hardware Dashboard)** olarak kullanmayı sağlayan ultra-hafif, sıfır gecikmeli ve AMOLED dostu bir sistemdir.

### 🌟 Temel Özellikler
1. **Ultra-Hafif Go Native Backend (`backend-go`):**
   - Yalnızca **~6 MB RAM** ve **%0 CPU** tüketimi.
   - Saniyede 1 kez CPU, GPU, RAM, Disk, Ağ ve Medya verilerini toplayan ve WebSocket (`/ws`) üzerinden yayınlayan telemetri motoru.
2. **Sıfır Yapılandırmalı UDP Keşif Protokolü (`:8001`):**
   - Mobil uygulamanın IP adresi yazmaya gerek kalmadan PC'yi yerel ağda (LAN) saniyeler içinde otomatik algılaması.
3. **Android Jetpack Compose & Material 3 İstemcisi (`android-app`):**
   - Google Pixel Android 14/15 tarzı **Material 3 (M3 Expressive)** widget'ları.
   - AMOLED saf siyah (`#000000`) taban ve donanımsal `FLAG_KEEP_SCREEN_ON` ile ekranın asla kapanmaması.
   - Sayfalar arasında 120Hz akıcı yatay kaydırma (`HorizontalPager`).
   - Üstten açılan gizli kontrol çubuğu (Ekran parlaklığı, PC ses kontrolü, PC kilitleme, tema seçimi).
4. **İnteraktif Web Düzen Yöneticisi (`:8000/admin`):**
   - 4 sütunlu 2D koordinat ızgarasında sürükle-bırak (Drag & Drop) ve serbest boyutlandırma.
   - Kategorilere ayrılmış zengin bileşen kataloğu ve canlı arama çubuğu.
   - Yapılan her değişikliğin telefona canlı yansıması (`LAYOUT_UPDATED` sinyali).

---

## 📂 Dizin Yapısı

```text
MobileDashboard/
├── .agents/
│   ├── AGENTS.md                 # Ana mimari ve sistem dokümantasyonu
│   ├── PROMPTS.md                # Sıfırdan adım adım geliştirme promptları
│   └── rules/
│       ├── material3_spec.md     # Material Design 3 (M3 Expressive) Token ve Tasarım Spesifikasyonu
│       ├── design_system.md      # Material 3 & AMOLED Tasarım Sistemi ve Bileşen Kuralları
│       ├── architecture.md       # Go & Android protokol ve sistem mimarisi
│       └── guidelines.md         # Kodlama ve performans standartları
│
├── backend-go/                   # Go Native Telemetry & UDP Discovery Backend
│   ├── main.go                   # Ana sunucu giriş noktası
│   ├── layouts.json              # Kullanıcı sayfa ve widget düzen veritabanı
│   ├── internal/
│   │   ├── config/config.go      # Yetkilendirme & auto-accept yapılandırması
│   │   ├── discovery/udp.go      # UDP Keşif Sunucusu (Port 8001)
│   │   ├── hardware/collector.go # CPU, GPU, RAM, Disk, Ağ ölçüm motoru
│   │   ├── media/controller.go   # MPRIS playerctl & sistem kontrolü
│   │   ├── models/               # Telemetri, düzen ve istemci veri modelleri
│   │   └── server/server.go      # REST API ve WebSocket sunucusu
│   └── web/                      # Modüler Web Admin & Düzen Yöneticisi
│       ├── admin.html            # 2D Sürükle-Bırak HTML Kabuğu
│       ├── css/                  # Modüler stiller (main, tokens, grid, simulator, modals)
│       └── js/                   # ES6 Modülleri (app, state, catalog, canvas, modals, api, ws)
│
├── android-app/                  # Android Native Jetpack Compose İstemcisi
│   ├── app/src/main/java/com/mobiledashboard/app/
│   │   ├── MainActivity.kt       # Tam ekran & Keep-Screen-On ana aktivite
│   │   ├── data/
│   │   │   ├── model/            # Telemetry & Layout modelleri
│   │   │   └── repository/       # DiscoveryRepository & TelemetryRepository
│   │   ├── ui/
│   │   │   ├── components/       # Material 3 Donanım, Saat ve Medya kartları
│   │   │   ├── screens/          # DashboardScreen & DiscoveryScreen
│   │   │   └── theme/            # Color, Theme, Type (M3 Tonal Tokens)
│   │   └── viewmodel/            # DashboardViewModel
│   └── build.gradle.kts          # Gradle bağımlılıkları
│
├── start.sh                      # Tek tıkla Go backend çalıştırma scripti
└── README.md                     # Kullanıcı başlangıç rehberi
```

---

## 📋 Önemli Kurallar ve Referans Belgeler

- [🎨 Material 3 Kapsamlı Token & Tasarım Spesifikasyonu](file:///.agents/rules/material3_spec.md)
- [📱 Material 3 & AMOLED Tasarım Sistemi](file:///.agents/rules/design_system.md)
- [🏗️ Go Backend & Android Sistem Mimarisi & Şema Kuralları](file:///.agents/rules/architecture.md)
- [🌐 Web Admin Modüler Mimarisi & Geliştirici Rehberi](file:///.agents/WEB_ADMIN.md)
- [⚡ Geliştirme & Performans Standartları](file:///.agents/rules/guidelines.md)

---

## 🛠️ Teknoloji Yığını

### Backend (PC)
- **Go 1.22+**
- **Gorilla WebSocket (`github.com/gorilla/websocket`):** Canlı telemetri yayını (`/ws`).
- **Gopsutil & Sysfs & Subprocess:**
  - CPU, RAM, Disk, Ağ ölçümleri.
  - `nvidia-smi` (NVIDIA GPU yükü ve sıcaklığı).
  - `playerctl` (Linux MPRIS medya okuma ve uzaktan yürütme/duraklatma).
- **UDP Socket (Port 8001):** Sıfır yapılandırmalı broadcast keşif yayını.

### Frontend / Mobile (Android)
- **Kotlin 1.9+ & Jetpack Compose (BOM 2024.04.01)**
- **Material 3 (M3 Expressive):** Tonal surface containers, squircle shapes, pill badges.
- **OkHttp 4.12:** WebSocket ve REST bağlantı yönetimi.
- **Kotlinx Serialization:** JSON telemetri parse motoru.
- **Coil Compose:** Asenkron albüm kapağı yükleme ve önbellekleme.

---

## 🚀 Hızlı Başlatma

```bash
# 1. PC'de Go sunucusunu başlatın
./start.sh

# 2. Android cihazınızda uygulamayı açın
# Ev ağındaki bilgisayarınız otomatik olarak algılanır ve ekran açılır!
```
