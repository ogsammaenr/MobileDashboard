# 📱 MobileDashboard v2.0

<div align="center">

![Go Version](https://img.shields.io/badge/Go-1.22+-00ADD8?style=for-the-badge&logo=go&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3%20Expressive-4285F4?style=for-the-badge&logo=android&logoColor=white)
![AMOLED](https://img.shields.io/badge/AMOLED-Pure%20Black%20%23000000-000000?style=for-the-badge)
![Latency](https://img.shields.io/badge/Latency-Zero%20Lag%20WebSocket-success?style=for-the-badge)

**Eski veya yeni akıllı telefon/tabletlerinizi yerel ağ (Wi-Fi) üzerinden bilgisayarınıza bağlayarak masaüstünüzde ikinci bir donanım izleme, büyük saat, medya kontrol ve uygulama başlatıcı ekranı (Hardware Dashboard) olarak kullanın!**

[Özellikler](#-temel-özellikler) • [Sistem Mimarisi](#-sistem-mimarisi) • [Hızlı Başlangıç](#-hızlı-başlangıç) • [Web Admin Paneli](#-web-admin-paneli-8000admin) • [Bileşen Kataloğu](#-zengin-bileşen-kataloğu) • [Android İstemcisi](#-android-istemcisi) • [Yapılandırma & API](#-yapılandırma--api)

</div>

---

## 🌟 Temel Özellikler

- ⚡ **Ultra-Hafif Go Native Backend (`backend-go`):** Yalnızca **~6 MB RAM** ve **%0 CPU** tüketimiyle sistem kaynaklarınızı tüketmez.
- 📡 **Sıfır Yapılandırmalı UDP Keşif Protokolü (`:8001`):** IP adresi yazmaya gerek kalmadan telefonunuz PC'yi yerel ağda (LAN) saniyeler içinde otomatik algılar ve bağlanır.
- 🎨 **Google Pixel Android 14/15 Material 3 Expressive Tasarım:** Squircle kartlar, 8 yapraklı yonca (scalloped), asimetrik şekiller ve stadyum kapsül rozetleri.
- 🖤 **AMOLED Saf Siyah (`#000000`) & Donanımsal Ekran Açık Tutma:** `FLAG_KEEP_SCREEN_ON` ile ekran asla kapanmaz, OLED ekranlarda minimum pil ve sıfır yanma riski sunar.
- 📐 **100% Çerçevesiz Ekran (Full Display Cutout / Notch Desteği):** Kamera deliği veya çentik alanları dahil tüm ekranı (%100 Edge-to-Edge) kullanır.
- 🖥️ **2D Koordinat Izgarası ve Web Düzen Yöneticisi (`:8000/admin`):** Sürükle-bırak (Drag & Drop), serbest boyutlandırma ve canlı AMOLED telefon simülatörü.
- 🚀 **Masaüstü Uygulama Başlatıcı (App Launcher):** PC'nizdeki uygulamaları otomatik tarar, sistem ikonlarını çeker ve telefondan tek dokunuşla PC'de başlatır.
- 🎛️ **Hızlı Kontrol Çubuğu (3 Parmakla Dokunma):** Ekran parlaklığı, ekran yönü (Dikey/Yatay/Otomatik), 7 farklı M3 renk teması ve PC ses/kilit kontrolleri.

---

## 🏗️ Sistem Mimarisi

```text
MobileDashboard/
├── backend-go/                          # 🚀 Go Native Telemetry & UDP Discovery Backend
│   ├── main.go                          # Ana sunucu giriş noktası
│   ├── config.json                      # Güvenlik & yetkilendirme ayarları
│   ├── layouts.json                     # Kullanıcı sayfa ve widget koordinat veritabanı
│   ├── internal/
│   │   ├── apps/                        # .desktop tarayıcı ve sistem ikonu ayrıştırıcı
│   │   ├── config/                      # Yetkilendirme & auto-accept yapılandırması
│   │   ├── discovery/                   # UDP Broadcast Keşif Sunucusu (Port 8001)
│   │   ├── hardware/                    # CPU, GPU (NVIDIA/AMD), RAM, Çoklu Disk, Ağ motoru
│   │   ├── media/                       # Linux MPRIS playerctl & ses/sistem yöneticisi
│   │   ├── models/                      # Telemetri, düzen ve istemci veri modelleri
│   │   └── server/                      # REST API ve WebSocket sunucusu (Port 8000)
│   └── web/                             # 🌐 Modüler Web Admin Paneli
│       ├── admin.html                   # 2D Sürükle-Bırak HTML Kabuğu
│       ├── css/                         # Modüler CSS (main, tokens, grid, simulator, modals)
│       └── js/                          # ES6 Modülleri (app, state, catalog, canvas, modals, api, ws)
│
├── android-app/                         # 📱 Android Native Jetpack Compose İstemcisi
│   ├── app/src/main/java/com/mobiledashboard/app/
│   │   ├── MainActivity.kt              # Tam ekran & Keep-Screen-On ana aktivite
│   │   ├── data/model/                  # Telemetry, Layout & Theme modelleri
│   │   ├── data/repository/             # UDP Discovery & WebSocket Telemetry repository
│   │   ├── ui/components/               # Material 3 Donanım, Saat, Medya ve Kısayol kartları
│   │   ├── ui/screens/                  # DashboardScreen (HorizontalPager) & DiscoveryScreen
│   │   └── ui/theme/                    # Color, Theme, Shape & Type (M3 Tonal Tokens)
│   └── build.gradle.kts                 # Gradle bağımlılıkları (Compose BOM 2024.04.01)
│
├── start.sh                             # Tek tıkla Go backend derleme ve çalıştırma scripti
└── README.md                            # Proje ana dokümantasyonu
```

---

## ⚡ Hızlı Başlangıç

### Gereksinimler
- **Bilgisayar:** Linux (Ubuntu, Arch, Fedora vb.) veya Windows / macOS • **Go 1.22+**
- **Telefon/Tablet:** Android 7.0+ (API 24+) yüklü herhangi bir cihaz
- **Ağ:** Bilgisayar ve telefonun aynı yerel ağda (Wi-Fi) olması yeterlidir.

### 1. PC Tarafını Başlatın (Go Backend)
Ana dizinde yer alan başlatma scriptini çalıştırın:

```bash
./start.sh
```
*Sunucu otomatik derlenir, UDP keşif yayını (`:8001`) ve WebSocket telemetri sunucusu (`:8000`) aktif hale gelir.*

### 2. Android Uygulamasını Yükleyin
Uygulamayı derleyip telefonunuza yüklemek için aşağıdaki adımlardan birini seçin:

#### Seçenek A: ADB ile Tek Komutla Yükleme
Telefonunuz USB ile bağlı ve USB Hata Ayıklama açıksa:
```bash
cd android-app
./gradlew installDebug
```

#### Seçenek B: Android Studio ile Açma
1. `android-app` dizinini **Android Studio** ile açın.
2. Cihazınızı seçip **Run (Çalıştır ▶)** butonuna basın.

> **🎉 Bitti!** Telefonunuzdaki uygulamayı açtığınız anda bilgisayarınız otomatik algılanır ve canlı dashboard ekranı açılır.

---

## 🌐 Web Admin Paneli (`:8000/admin`)

Masaüstü tarayıcınızdan `http://localhost:8000/admin` veya `http://<PC_IP>:8000/admin` adresine giderek düzeninizi canlı yönetebilirsiniz.

### ✨ Web Admin Yetenekleri:
- **4 Sütunlu 2D Koordinat Izgarası:** Kartları sürükleyip bırakın (Drag & Drop), sağdan ve alttan tutarak serbestçe yeniden boyutlandırın.
- **Canlı Telefon Simülatörü:** Yaptığınız tüm değişiklikler sağdaki telefon mockup'ında anında görünür ve telefonunuza sıfır gecikmeyle yansır.
- **Detaylı Widget Ayar Modalı (`⚙️`):**
  - Özel Başlık / İsim belirleme
  - Tipografi boyutu (Küçük, Normal, Büyük, Devasa)
  - 7 farklı vurgu rengi (Nord, Catppuccin, Everforest, Tokyo Night, Gruvbox, Monochrome, Rosé Pine)
  - M3 Şekil Stili (Yonca, Çiçek, Dairesel, M3 Squircle, Kapsül, Asimetrik)
  - Donanım Sıcaklığı (°C), İlerleme Çubuğu ve **Durum/Tüketim Rozetleri (CPU/VRAM/RAM)** açma/kapama
  - Saat Saniye, Tarih ve 12/24 Saat formatı seçimi
  - Uygulama Kısayolu için PC'de kurulu uygulamaları otomatik tarama ve seçme.
- **Cihaz Yönetimi & Güvenlik:** Yetkilendirme tokenleri, onay bekleyen ve engellenen cihaz listesi, Otomatik Bağlantı (Auto-Accept) anahtarı.

---

## 📦 Zengin Bileşen Kataloğu

| Kategori | Bileşen Adı | Widget ID | Açıklama |
| :--- | :--- | :--- | :--- |
| ⚡ **Donanım** | **İşlemci (CPU)** | `cpu_card` | Canlı kullanım %, Sıcaklık °C, Sparkline dalga grafiği ve CPU durum rozeti |
| ⚡ **Donanım** | **Ekran Kartı (GPU)** | `gpu_card` | NVIDIA/AMD yük %, GPU Sıcaklığı °C, VRAM kullanım rozeti ve bellek barı |
| ⚡ **Donanım** | **Bellek (RAM)** | `ram_card` | RAM doluluk %, Çok segmentli bellek kapsülü ve tüketim durumu rozeti |
| ⚡ **Donanım** | **Depolama (SSD/Disk)**| `disk_card` | Tüm fiziksel disklerin/bölümlerin toplam boyutu, doluluk % ve boş GB |
| ⚡ **Donanım** | **Ağ Trafiği** | `network_card` | Canlı indirme/yükleme hızları (MB/s), aktivite LED'i ve çift sparkline |
| ⚡ **Donanım** | **Sistem Merkezi (Hero)**| `system_fullscreen_m3`| Kesintisiz sinüs dalgalı ağ akışı ve 4'lü diagonal bento donanım matrisi |
| ⚡ **Donanım** | **Dual Gösterge** | `m3_gauge_card` | CPU ve GPU için yan yana çift kadranlı dairesel M3 gösterge |
| ⚡ **Donanım** | **Hızlı Sistem Özeti** | `quick_stats` | CPU, GPU, RAM ve Ağ metriklerini tek satırda toplayan kompakt hap |
| 🕰️ **Saat** | **Dev Dijital Saat (Hero)**| `clock_fullscreen_m3`| 360° saniye kadranı, AM/PM sync rozeti, takvim podu ve dünya saati |
| 🕰️ **Saat** | **Pixel Dijital Saat** | `clock_m3_pixel` | Android 14/15 kilit ekranı tarzı devasa üst üste saat & dakika |
| 🕰️ **Saat** | **M3 Kapsül Saat** | `clock_m3_pill` | Stadyum hap formunda saat, gün ve saniye rozeti |
| 🕰️ **Saat** | **Monolit Saat** | `clock_giant_monolith`| Devasa monolitik tipografi ve özel başlık etiketi |
| 🕰️ **Saat** | **Siberpunk HUD Saat** | `clock_cyber_hud` | Neon siberpunk tipografi ve parlayan saniye göstergesi |
| 🕰️ **Saat** | **3D Split Flip Saat** | `clock_split_flip` | Retro mekanik flip gösterge blokları |
| 🕰️ **Saat** | **Minimal Analog Saat** | `clock_analog` | Akıcı saniye ibreli modern Material 3 analog saat |
| 🎵 **Medya** | **Dev Medya Merkezi (Hero)**| `media_fullscreen_m3`| Bulanık albüm kapağı arkaplanı, dalga ekolayzır ve dokunmatik kontroller |
| 🎵 **Medya** | **Vinil Plak Medya** | `media_vinyl` | Çalan şarkıyla birlikte dönen retro vinil plak ve şarkı bilgisi |
| 🎵 **Medya** | **Kompakt Medya Kartı**| `media_card` | Şarkı/Sanatçı bilgisi ve hızlı oynat/duraklat butonları |
| 🚀 **Kısayol** | **Uygulama / Komut Kısayolu**| `app_shortcut` | PC'deki uygulamaları veya özel komutları telefondan tek tıkla çalıştırır |

---

## 📱 Android İstemcisi Özellikleri

- **120Hz Akıcı Yatay Kaydırma (`HorizontalPager`):** Sayfalar arasında takılmadan pürüzsüz geçiş.
- **Gizli Hızlı Kontrol Çubuğu (3 Parmak Dokunma):**
  - **Ekran Parlaklığı:** Donanımsal pencere parlaklığını %5 - %100 arasında canlı ayarlama.
  - **Ekran Yönü:** Dikey (Portrait), Yatay (Landscape) veya Sensöre Göre Otomatik (Auto).
  - **Masaüstü Eylemleri:** PC Ses Azalt / Artır / Sessiz, PC Kilitleme (`lock`), PC Uyutma (`sleep`).
  - **Canlı Tema Değiştirici:** Nord, Catppuccin, Everforest, Tokyo Night, Gruvbox, Monochrome, Rosé Pine.
  - **Kaydırılabilir Arayüz:** Yatay konumda tüm ayarlara tam erişim sağlayan dikey kaydırma desteği.
- **Otomatik Yeniden Bağlanma (Auto Reconnect):** Wi-Fi kopsa bile bağlantı geldiği anda sıfır kullanıcı müdahalesiyle telemetriyi devam ettirir.

---

## ⚙️ Yapılandırma & API

### Go Backend Yapılandırması (`config.json`)

```json
{
  "port": 8000,
  "discovery_port": 8001,
  "auto_accept_connections": true,
  "require_auth": false,
  "theme": "rosepine"
}
```

### Önemli REST API Uç Noktaları

| Metot | Uç Nokta | Açıklama |
| :--- | :--- | :--- |
| `GET` | `/api/status` | Sunucu durumu, hostname ve port bilgisi |
| `GET` | `/api/telemetry` | Anlık CPU, GPU, RAM, Disk, Ağ ve Medya verisi |
| `GET` | `/api/layouts` | Aktif sayfa ve widget koordinat düzenleri |
| `POST`| `/api/layouts` | Yeni sayfa ve widget düzenlerini kaydeder |
| `GET` | `/api/apps` | PC'de kurulu `.desktop` uygulamalarını tarar |
| `GET` | `/api/apps/icon` | Uygulama sistem ikonunu (SVG/PNG) sunar |
| `POST`| `/api/apps/launch` | İstenen uygulamayı veya komutu PC'de başlatır |
| `POST`| `/api/system/action` | Ses aç/kapa, kilit veya uyku komutunu yürütür |
| `POST`| `/api/media/control` | MPRIS medya yürütme/duraklatma/atlama eylemleri |
| `GET` | `/ws` | Canlı WebSocket telemetri yayını (1Hz) |
| `GET` | `/ws?role=admin` | Web Admin çift yönlü canlı senkronizasyon soketi |

---

## 📄 Lisans

Bu proje MIT lisansı altında korunmaktadır. İstediğiniz gibi geliştirebilir, özelleştirebilir ve kullanabilirsiniz.
