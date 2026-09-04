# MobileDashboard - Geliştirme & Kodlama Standartları (`guidelines.md`)

Yapay zeka modelleri ve geliştiriciler **MobileDashboard** projesine kod eklerken, bileşen tasarlarken veya hata ayıklarken aşağıdaki kuralları esas almalıdır:

---

## 1. Mimari & Performans İlkeleri

1. **Ultra-Hafif Backend Kuralı (Go Native):**
   - Sunucu tarafında saniyede 1 kez çalışan telemetri döngüsü CPU'yu yormamalı, bellek tüketimi **~10 MB RAM** altında kalmalıdır.
   - Uzun sürebilecek komut çalıştırma (`exec.Command`) ve I/O işlemleri sunucunun WebSocket döngüsünü tıkamamalıdır.
2. **Sıfır Derlemeli Web Admin Paneli (`admin.html`):**
   - Web arayüzü tek bir HTML dosyası içinde Vanilla JS ve saf CSS3 ile geliştirilmeli; Webpack, Vite veya React bağımlılığı getirilmemelidir.
3. **Akıcı Android Compose Performansı (60/120 FPS):**
   - Gereksiz yeniden oluşturmalardan (recomposition) kaçınmak için `remember`, `derivedStateOf` ve kararlı veri modelleri (`@Serializable data class`) kullanılmalıdır.
   - Ağır hesaplamalar ve ağ işlemleri `Dispatchers.IO` üzerinde koşturulmalıdır.

---

## 2. Donanım & Sensör Hata Yönetimi

1. **Eksik Donanım Dayanıklılığı:**
   - Bilgisayarda NVIDIA GPU (`nvidia-smi`), batarya veya ortam sensörü bulunmayabilir.
   - Hiçbir sensör fonksiyonu sunucuyu veya uygulamayı çökertecek (panic/crash) şekilde yazılmamalı; hata durumunda varsayılan güvenli değerler (`0.0`, `"--"`) dönmelidir.
2. **Medya Kontrolü Fallback Mekanizması:**
   - Medya bilgisi alınırken `playerctl` yoksa veya hiçbir oynatıcı açık değilse durum `"Stopped"`, başlık `"Çalan Medya Yok"` olarak doldurulmalıdır.
   - YouTube oynatılıyorsa kapak görseli fallback olarak YouTube thumbnail URL'sine dönüştürülmelidir.

---

## 3. Mobil Kullanıcı Deneyimi & AMOLED Kuralları

1. **AMOLED Siyahı (`#000000`):**
   - Ana ekran arka planı her zaman `#000000` olmalıdır. Bu, uzun süreli masaüstü monitör kullanımında ekran yanmasını (burn-in) önler ve pil tüketimini minimize eder.
2. **Kesintisiz Ekran Açıklığı (`Keep Screen On`):**
   - Mobil istemcide `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON` bayrağı aktif tutulmalıdır.
3. **Otomatik Yeniden Bağlanma (Resilient Reconnect):**
   - Wi-Fi kesildiğinde, IP değiştiğinde veya PC yeniden başladığında istemci çökmeyecek; arka planda otomatik yeniden bağlanma ve UDP broadcast yeniden keşif döngüsünü işletecektir.
4. **Çift Yönlü Canlı Senkronizasyon:**
   - Web admin panelinde yapılan her düzenleme anında `layouts.json`'a kaydedilmeli ve bağlı telefonlara WebSocket üzerinden anında yansıtılmalıdır.
