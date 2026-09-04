# 🌐 Web Admin Paneli Modüler Mimarisi (`WEB_ADMIN.md`)

Bu doküman, **MobileDashboard v2.0** Web Admin Panelinin (`backend-go/web/`) modüler mimarisini, dizin yapısını, reaktif state yönetimini, 2D ızgara & boyutlandırma motorunu ve sisteme yeni bileşen/pencere (modal) ekleme standartlarını açıklar.

---

## 🎯 Mimari İlkeler ve Genel Bakış

Web Admin Paneli, masaüstü tarayıcılardan (`:8000/admin`) mobil dashboard düzenini 2D koordinat ızgarasında yönetmeyi, cihaz bağlantı ve güvenlik izinlerini denetlemeyi sağlayan yönetim merkezidir.

### Temel Prensipler:
1. **Saf Web Standartları (Zero Build Step):** Webpack, Vite, Node veya npm gibi harici derleyicilere gerek duymadan doğrudan modern tarayıcıların saf **ES6 Modules (`type="module"`)** ve **CSS `@import`** yetenekleriyle çalışır.
2. **Sorumlulukların Ayrımı (Separation of Concerns):** UI şablonu (`HTML`), stiller (`CSS`), reaktif durum (`State`), servisler (`API/WebSocket`), görsel bileşenler (`Components`) ve açılır pencereler (`Modals`) izole dosyalara bölünmüştür.
3. **Reaktif Durum & Event Bus (`state.js`):** Tek yönlü veri akışı ve Pub/Sub mimarisi ile sayfa, widget ve istemci değişiklikleri tüm arayüze anında dağıtılır.
4. **Tak-Çıkar Pencere Yöneticisi (`modalManager.js`):** Gelecekte eklenecek yeni ayar pencereleri veya kontrol ekranları tek satırla sisteme kaydedilip açılabilir.

---

## 📂 Dizin ve Dosya Yapısı

```text
backend-go/web/
├── admin.html                          # 📄 Temiz HTML Shell (Yalnızca iskelet & importlar)
│
├── css/                                # 🎨 Modüler Stil Dosyaları
│   ├── main.css                        # Tüm alt CSS'leri toplayan ana giriş dosyası
│   ├── tokens.css                      # Renk paletleri, tipografi değişkenleri, reset
│   ├── header.css                      # Üst gezinme çubuğu, rozetler ve kaydet butonu
│   ├── catalog.css                     # Sol bileşen kataloğu, arama çubuğu, kategori sekmeleri
│   ├── grid.css                        # 4 sütunlu 2D ızgara, widget kartları, resize tutamaçları
│   ├── simulator.css                   # Canlı AMOLED telefon simülatörü & mockup
│   └── modals.css                      # Genel modallar, form kontrolleri, switch slider, toast
│
└── js/                                 # ⚡ Modüler JavaScript (ES6 Modules)
    ├── app.js                          # 🚀 Ana orkestratör & başlatıcı (Entry point)
    ├── state.js                        # 🧠 Merkezi reaktif durum yöneticisi (Pub/Sub Event Bus)
    │
    ├── constants/
    │   ├── catalog.js                  # 📦 17 adet widget ve kategori meta veri tanımları
    │   └── themes.js                   # 🌈 M3 renk hex haritası ve font ölçekleme katsayıları
    │
    ├── services/
    │   ├── api.js                      # 🌐 REST API istemcisi (layouts, config, clients, theme)
    │   └── websocket.js                # 📡 WebSocket istemcisi & otomatik reconnect motoru
    │
    ├── components/
    │   ├── header.js                   # 🔝 Üst bar yönetimi (Kaydet butonu, cihaz rozeti)
    │   ├── catalog.js                  # 📦 Sol sidebar (Arama, kategori filtreleme, dragstart)
    │   ├── canvas.js                   # 📐 2D Grid matrisi, ghost indicator & Resize Motoru
    │   ├── preview.js                  # 📱 Sağ telefon simülasyon render motoru
    │   └── widgetPreviews.js           # 🖼️ Widget mini görsel önizleme HTML şablonları
    │
    └── modals/
        ├── modalManager.js             # 🎛️ Genişletilebilir Modal & Pencere Yöneticisi
        ├── settingsModal.js            # ⚙️ Widget Özelleştirme Modalı
        └── clientModal.js              # 🛡️ Cihaz İzinleri & Bağlantı Modalı
```

---

## 🧠 Temel Katmanlar ve İşleyiş

### 1. Reaktif Durum Yönetimi & Event Bus (`state.js`)
Durum değişikliklerini dinleyen bileşenleri tetikler:
```javascript
import { state } from './state.js';

// Durumu dinle:
state.on('layouts:updated', (layouts) => { /* UI güncelle */ });
state.on('clients:updated', (clientsData) => { /* Cihaz rozetini yenile */ });

// Durumu değiştir ve yayınla:
state.setLayouts(newLayouts);
state.setClientsData(clientsData);
```

### 2. Servis Katmanı (`services/`)
- **`api.js`:** Backend REST API'lerine (`/api/layouts`, `/api/clients`, `/api/config`, `/api/theme`) JSON istekleri atan `async` istemci.
- **`websocket.js`:** `/ws?role=admin` soketine bağlanır, kopma anında otomatik reconnect yapar ve backend'den gelen `CLIENTS_UPDATED`, `LAYOUT_UPDATED` ve `THEME_UPDATED` sinyallerini yakalayıp `state` üzerine iletir.

### 3. 2D Koordinat Izgarası & Boyutlandırma Motoru (`components/canvas.js`)
- **4 Sütunlu Matris:** `grid-template-columns: repeat(4, 1fr)` ve satır yüksekliği `115px`.
- **Sürükle & Bırak (Drag & Drop):** Sol katalogdan veya ızgara içinden taşınan bileşenin hedef koordinatlarını hesaplar ve yarı saydam mavi `grid-ghost` kutusu ile hedefi canlı gösterir.
- **Serbest Boyutlandırma (Resize Engine):**
  - `resize-handle-right`: Genişliği 1-4 sütun arası artırıp azaltır.
  - `resize-handle-bottom`: Yüksekliği 1-6 satır arası artırıp azaltır.
  - `resize-handle-corner`: Köşeden tutarak hem genişlik hem yüksekliği serbestçe ayarlar.
- **Sınır Denetimi (`clampWidget`):** Kartların 4 sütun dışına taşmasını engeller.

---

## 🛠️ Geliştirici Kılavuzu

### 🎛️ Senaryo 1: Yeni Bir Pencere / Modal Eklemek
Sisteme yeni bir pencere (örneğin *Sistem Logları Penceresi*) eklemek 3 adımdır:

1. **HTML İskeletini Ekleyin (`web/admin.html`):**
   ```html
   <div class="modal-overlay" id="logsModal">
       <div class="modal-window">
           <div class="modal-header">
               <div class="modal-title">📜 Sistem Logları</div>
               <button class="modal-close">✕</button>
           </div>
           <div class="modal-body" id="logsBody">...</div>
       </div>
   </div>
   ```

2. **Modal JS Modülünü Oluşturun (`web/js/modals/logsModal.js`):**
   ```javascript
   import { modalManager } from './modalManager.js';

   export function initLogsModal() {
       modalManager.register('logsModal', {
           onInit: () => {
               document.querySelector('#logsModal .modal-close')
                   .addEventListener('click', () => modalManager.close('logsModal'));
           },
           onOpen: (data) => {
               console.log('Log modalı açıldı:', data);
           },
           onClose: () => {
               console.log('Log modalı kapandı');
           }
       });
   }
   ```

3. **Uygulamaya Tanıtın (`web/js/app.js`):**
   ```javascript
   import { initLogsModal } from './modals/logsModal.js';
   // bootstrap() içinde:
   initLogsModal();
   ```

4. **İstediğiniz Butondan Açın:**
   ```javascript
   modalManager.open('logsModal', { logType: 'system' });
   ```

---

### 📦 Senaryo 2: Yeni Bir Widget Eklemek
Kataloğa yeni bir widget (örneğin *Hava Durumu Kartı*) eklemek için:

1. **Kataloğa Tanımlayın (`web/js/constants/catalog.js`):**
   ```javascript
   { id: "weather_card", category: "hub", name: "Hava Durumu", span: 2, row: 1, icon: "🌤️", desc: "Canlı sıcaklık ve hava tahmini" }
   ```

2. **Mini Önizleme Ekleyin (`web/js/components/widgetPreviews.js`):**
   ```javascript
   case 'weather_card':
       return `<div style="font-size:1.1rem; color:${accent}; font-weight:bold;">🌤️ 24°C Güneşli</div>`;
   ```

---

## 🚀 Go Backend Sunucu Yönlendirmesi

[`backend-go/internal/server/server.go`](file:///home/excalibur/WorkSpace/projects/MobileDashboard/backend-go/internal/server/server.go) statik web varlıklarını doğru MIME type ile sunar:

```go
webDir := s.getWebDir()
fileServer := http.FileServer(http.Dir(webDir))
mux.Handle("/css/", fileServer)
mux.Handle("/js/", fileServer)
```
