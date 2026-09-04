# MobileDashboard - Material 3 & AMOLED Tasarım Sistemi (`design_system.md`)

> **Önemli Not:** Google'ın derinlemesine Material 3 token mimarisi, HCT renk uzayı ve M3 Expressive detayları için lütfen [Material 3 Kapsamlı Spesifikasyonu (`material3_spec.md`)](file:///.agents/rules/material3_spec.md) belgesine bakınız.

Bu doküman, **MobileDashboard** projesinin Android native istemcisi ve Web Admin panelindeki görsel standartlarını, renk paletini, tipografi hiyerarşisini, bileşen şekillerini ve Material 3 (M3 Expressive) tasarım ilkelerini tanımlar.

---

## 🎨 1. Temel Tasarım Felsefesi

1. **AMOLED Dostu Gerçek Siyah (`#000000`):**
   - Arka plan saf siyahtır. Bu, OLED/AMOLED mobil ekranlarda piksellerin tamamen sönmesini sağlayarak minimum enerji tüketimi ve sonsuz kontrast sunar.
2. **Material 3 Tonal Konteyner Hiyerarşisi:**
   - Düz gri kutular veya keskin kenarlar yerine, derinlik ve hiyerarşi **M3 Surface Container** tonlarıyla sağlanır.
3. **M3 Expressive Geometri:**
   - Kartlar ve paneller için **22–24dp squircle** yuvarlatılmış köşeler.
   - Durum rozetleri, sayaçlar ve dokunmatik kontrol butonları için tam yuvarlak **Pill (Hap / 50% radius)** şekli.
4. **Yüksek Okunabilirlik & Glanceability:**
   - Cihaz masa üzerinde dururken veya uzaktan bakıldığında donanım verileri ve saat tek bakışta anlaşılabilecek netlikte olmalıdır.

---

## 🌈 2. Renk Paleti ve M3 Renk Belirteçleri (Tokens)

### Yüzey ve Katman Renkleri (Dark Mode)
| Belirteç Adı | Hex Kodu | Kullanım Alanı |
|---|---|---|
| `AmoledBlack` | `#000000` | Ana ekran arka planı, pencere kökü |
| `DarkCardBg` / `M3Surface` | `#0D0E12` | Widget kartlarının taban arka planı |
| `M3SurfaceContainerHigh` | `#1E202A` | İç çipler, pill rozetleri, ikincil bilgi blokları |
| `M3SurfaceContainerHighest` | `#262835` | İlerleme çubuğu arka planı, ikon yuvaları, butonlar |
| `M3OutlineVariant` | `#2D2F3C` | 1dp inceliğinde yumuşak M3 kart ve çip sınır çizgileri |

### Metin Renkleri
| Belirteç Adı | Hex Kodu | Kullanım Alanı |
|---|---|---|
| `TextMain` | `#F4F4F6` | Büyük saat rakamları, birincil metrik değerleri |
| `TextSub` | `#A5A7B8` | Etiketler, sanatçı bilgisi, ikincil metinler |
| `TextMuted` | `#6E7182` | Kategori başlıkları, pasif durumlar |

### Tema & Vurgu Renkleri
| Tema Adı | Birincil Hex | İkincil Hex | Konteyner Hex |
|---|---|---|---|
| **Midnight Cyan** | `#06B6D4` | `#3B82F6` | `#083344` |
| **Emerald Matrix** | `#22C55E` | `#059669` | `#052E16` |
| **Cyberpunk Neon** | `#A855F7` | `#EC4899` | `#2E1065` |
| **Crimson Gamer** | `#EF4444` | `#F97316` | `#450A0A` |
| **Sunset Gold** | `#F59E0B` | `#D97706` | `#451A03` |
| **Deep Cobalt** | `#3B82F6` | `#6366F1` | `#172554` |
| **Titanium Stealth**| `#E4E4E7` | `#71717A` | `#262835` |

---

## 📐 3. Tipografi Hiyerarşisi (M3 Typography)

| Seviye | Font Boyutu | Ağırlık | Örnek Kullanım |
|---|---|---|---|
| `DisplayLarge` | `48sp - 56sp` | `Black (900)` | Büyük saat rakamları (`GiantMonolith`, `M3PixelClock`) |
| `HeadlineMedium`| `28sp - 32sp` | `ExtraBold (800)`| Donanım kartı yüzde değerleri (`%15`, `%48`) |
| `TitleMedium` | `14sp - 15sp` | `Bold (700)` | Şarkı başlığı, kart başlıkları |
| `BodyMedium` | `11sp - 13sp` | `Normal / SemiBold`| Sanatçı adı, indirme hızı, tarih |
| `LabelSmall` | `9sp - 10sp` | `Bold / Monospace` | Çip etiketleri (`CPU`, `GPU`, `CANLI`), sıcaklık rozetleri |

---

## 🧩 4. Standart Bileşen Kuralları

### A. BaseCard (Temel Kart Konteyneri)
- **Köşe Yuvarlama:** `RoundedCornerShape(22.dp)` veya `24.dp`.
- **Arka Plan:** `DarkCardBg` (`#0D0E12`).
- **Kenarlık:** `1.dp border`, `M3OutlineVariant.copy(alpha = 0.8f)`.
- **İç Boşluk (Padding):** `horizontal = 14.dp`, `vertical = 12.dp`.

### B. CustomProgressBar (Kapsül İlerleme Çubuğu)
- **Yükseklik:** `6.dp` veya `8.dp`.
- **Uçlar:** Tam yuvarlak (`RoundedCornerShape(height)`).
- **Arka Plan Kanalı:** `M3SurfaceContainerHighest`.
- **Dinamik Renk Kuralı:**
  - `> 88%` $\rightarrow$ `AccentRed` (`#EF4444`)
  - `72% - 88%` $\rightarrow$ `AccentYellow` (`#F59E0B`)
  - `< 72%` $\rightarrow$ Aktif tema vurgu rengi (`accentColor`).

### C. M3CircularGauge (Dairesel İbre)
- **Açı Aralığı:** `270°` tarama açısı (`startAngle = 135°`, `sweepAngle = 270°`).
- **Çizgi Genişliği:** `3.5dp - 6dp`, `StrokeCap.Round`.
- **Arka Plan Halkası:** `M3SurfaceContainerHighest`.

### D. Medya Dokunmatik Butonları (`M3MediaPillButton`)
- **Birincil Oynat/Duraklat Butonu:** `38dp` çapında tam daire, aktif tema rengi arka planı, `AmoledBlack` ikon.
- **İkincil Butonlar (Önceki/Sonraki):** `32dp` çapında tam daire, `M3SurfaceContainerHighest` arka planı, `1dp M3OutlineVariant` kenarlık.
- **Canlı Ekolayzır:** Şarkı çalarken (`isPlaying == true`) 3 adet asenkron dalgalanan `3.5dp` genişliğinde animasyonlu dikey çubuk.

---

## ⚡ 5. 2D Izgara (Grid) ve Boyutlandırma Kuralları

- Mobil ve tablet ekranları **4 sütunlu (4-Column Matrix)** bir ızgara düzenine oturur.
- Standart satır yüksekliği birimi **115dp**'dir.
- **Standart Widget Boyutları:**
  - `2x1` (Yarım genişlik, tek satır): CPU, GPU, RAM, Disk, Ağ, Dual Gauge.
  - `4x1` (Tam genişlik, tek satır): Medya Çalar, Hızlı 4'lü Özet, M3 Pill Saat, M3 Sistem Hub.
  - `2x2` (Kare / Dikey poster): M3 Pixel Saat, Dikey Poster Saat, Analog Saat.
  - `4x2` (Tam genişlik, çift satır): Devasa Monolit Saat.

---

## 🔗 İlgili Belgeler ve Referanslar
- [Material 3 Kapsamlı Token & Tasarım Spesifikasyonu (`material3_spec.md`)](file:///.agents/rules/material3_spec.md)
- [Go & Android Sistem Mimarisi (`architecture.md`)](file:///.agents/rules/architecture.md)
- [Geliştirme & Performans Kuralları (`guidelines.md`)](file:///.agents/rules/guidelines.md)
