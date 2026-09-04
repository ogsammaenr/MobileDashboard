// ==============================================================================
// WIDGET CATEGORIES SPECIFICATION
// ==============================================================================
export const CATEGORIES = [
    { id: "clock", name: "Saat & Zaman", icon: "🕒" },
    { id: "hardware", name: "Donanım & Sensörler", icon: "⚡" },
    { id: "hub", name: "Özet Panelleri & Hub", icon: "✨" },
    { id: "media", name: "Medya & Müzik", icon: "🎵" }
];

// ==============================================================================
// 18 EXCLUSIVE MATERIAL 3 & AMOLED WIDGETS CATALOG
// ==============================================================================
export const WIDGETS_CATALOG = [
    { id: "clock_animated_digital", category: "clock", name: "Animasyonlu Dev Dijital Saat", span: 4, row: 3, icon: "✨", desc: "Tüm ekranı kaplayan, kayan rakam animasyonlu, nefes alan auralı ve canlı saniye barlı pürüzsüz dijital saat" },
    { id: "clock_fullscreen_m3", category: "clock", name: "Material 3 Dev Dijital Saat Merkezi", span: 4, row: 3, icon: "🕰️", desc: "Ekranın tamamını kaplayan dev dijital saat, saniye kadranı, M3 takvim ve gün döngüsü bento matrisi" },
    { id: "clock_m3_pixel", category: "clock", name: "Material 3 Pixel Saat", span: 2, row: 2, icon: "📱", desc: "Android 14/15 büyük kilit ekranı saati, üst üste devasa rakamlar" },
    { id: "clock_m3_pill", category: "clock", name: "Material 3 Pill Saat", span: 4, row: 1, icon: "💊", desc: "M3 yatay kapsül bar saat, canlı saniye ve tarih çipi" },
    { id: "clock_giant_monolith", category: "clock", name: "Devasa Monolit Saat", span: 4, row: 2, icon: "🔤", desc: "Outfit fontlu dev saat, milisaniye ve ambient glow" },
    { id: "clock_cyber_hud", category: "clock", name: "Siberpunk HUD Saat", span: 4, row: 1, icon: "📟", desc: "Haftanın günleri, neon saniye barı ve Orbitron fontu" },
    { id: "clock_split_flip", category: "clock", name: "Bölünmüş Flip Kart", span: 4, row: 1, icon: "🎴", desc: "Retro-modern monospaced flip plakalar" },
    { id: "clock_vertical_poster", category: "clock", name: "Dikey Poster Saat", span: 2, row: 2, icon: "⏳", desc: "2x2 alanda üst üste devasa tipografi" },
    { id: "clock_analog", category: "clock", name: "Minimal Analog Saat", span: 2, row: 2, icon: "🕰️", desc: "Akrep, yelkovan ve saniyeli modern AMOLED kadran" },
    
    { id: "cpu_card", category: "hardware", name: "İşlemci (CPU)", span: 2, row: 1, icon: "⚡", desc: "Kullanım %, Sıcaklık °C ve M3 canlı bar" },
    { id: "gpu_card", category: "hardware", name: "Ekran Kartı (GPU)", span: 2, row: 1, icon: "🎮", desc: "NVIDIA/AMD kullanım %, Sıcaklık °C ve VRAM" },
    { id: "ram_card", category: "hardware", name: "Bellek (RAM)", span: 2, row: 1, icon: "💾", desc: "Doluluk % ve Kullanılan/Toplam GB" },
    { id: "network_card", category: "hardware", name: "Ağ Trafiği", span: 2, row: 1, icon: "🌐", desc: "Canlı İndirme ve Yükleme hızları (KB/MB)" },
    { id: "disk_card", category: "hardware", name: "Depolama (SSD)", span: 2, row: 1, icon: "💽", desc: "Disk doluluk % ve Boş GB" },
    { id: "volume_card", category: "hardware", name: "PC Ses Seviyesi & Kontrol", span: 2, row: 1, icon: "🔊", desc: "Material 3 ses seviyesi kaydırıcısı, sessize alma (Mute) ve hızlı % butonları" },
    
    { id: "m3_system_hub", category: "hub", name: "Material 3 Sistem Hub", span: 4, row: 1, icon: "✨", desc: "CPU, GPU, RAM ve SSD için 4'lü dairesel M3 gösterge paneli" },
    { id: "system_fullscreen_m3", category: "hub", name: "Material 3 Dev Sistem Merkezi", span: 4, row: 3, icon: "🖥️", desc: "Ekranın tamamını kaplayan, transparan zeminli, M3 asimetrik & yonca podlu, animasyonlu bento sistem monitörü" },
    { id: "app_shortcut", category: "hub", name: "Uygulama Kısayolu (1x1)", span: 1, row: 1, icon: "🚀", desc: "PC'de tek dokunuşla Spotify, Steam, Discord, VS Code veya özel uygulama başlatan 1x1 M3 butonu" },
    { id: "m3_gauge_card", category: "hub", name: "Material 3 Dual Gauge", span: 2, row: 1, icon: "🎯", desc: "CPU ve GPU için yan yana çift dairesel hız kadranı" },
    { id: "quick_stats", category: "hub", name: "Hızlı 4'lü Özet", span: 4, row: 1, icon: "📊", desc: "CPU, GPU, RAM ve Ağ için kompakt mini çipler" },
    
    { id: "media_card", category: "media", name: "Material 3 Medya (Pixel)", span: 4, row: 1, icon: "🎵", desc: "Android 14/15 M3 müzik çalar, dalga animasyonu ve tonal butonlar" },
    { id: "media_fullscreen_m3", category: "media", name: "Material 3 Dev Medya Merkezi", span: 4, row: 3, icon: "📻", desc: "Tam ekranı kaplayan dev albüm kapağı, dalga ekolayzırı, arama/tekrar/ses ve dokunmatik kontroller" },
    { id: "media_vinyl", category: "media", name: "Vinil Plak Medya", span: 4, row: 1, icon: "💿", desc: "Dönen vinil plak animasyonlu şık oynatıcı" }
];
