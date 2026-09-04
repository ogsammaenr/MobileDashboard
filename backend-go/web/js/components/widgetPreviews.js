// ==============================================================================
// WIDGET MINI VISUAL PREVIEWS HTML GENERATOR
// ==============================================================================

import { getAccentColorHex, getFontScaleMultiplier } from '../constants/themes.js';

export function getWidgetMiniPreviewHtml(w) {
    const widgetId = w.widget_id;
    const cfg = w.config || {};
    const accent = getAccentColorHex(cfg.accent_color);
    const scale = getFontScaleMultiplier(cfg.font_scale);
    const showSec = cfg.show_seconds !== false;
    const showTemp = cfg.show_temp !== false;
    const showBadge = cfg.show_badge !== false;
    const title = cfg.custom_title;

    switch (widgetId) {
        case 'clock_animated_digital':
        case 'clock_animated_fullscreen':
            return `
                <div style="width:100%; height:100%; display:flex; flex-direction:column; justify-content:space-between; align-items:center; padding:12px; background:radial-gradient(circle, ${accent}1a 0%, #11131a 80%); border-radius:24px; border:1px solid ${accent}33; font-family:'Space Grotesk',sans-serif; transform:scale(${scale}); position:relative; overflow:hidden;">
                    <!-- Top Chip Bar -->
                    <div style="width:100%; display:flex; justify-content:space-between; align-items:center;">
                        <span style="font-size:0.6rem; color:${accent}; font-weight:900; background:${accent}18; padding:2px 8px; border-radius:10px;">✨ ${title || 'DİJİTAL SAAT'}</span>
                        <span style="font-size:0.55rem; color:#86efac; background:#86efac18; padding:2px 6px; border-radius:8px; font-weight:bold;">● CANLI</span>
                    </div>

                    <!-- Center Hero Big Digits with glowing colon -->
                    <div style="display:flex; align-items:center; justify-content:center; gap:6px; margin:10px 0;">
                        <span style="font-size:2.6rem; font-weight:900; color:#fff; letter-spacing:-1px; text-shadow:0 0 12px ${accent}44;">21</span>
                        <span style="font-size:2.4rem; font-weight:900; color:${accent}; animation:pulse 1s infinite;">:</span>
                        <span style="font-size:2.6rem; font-weight:900; color:${accent}; letter-spacing:-1px; text-shadow:0 0 16px ${accent}66;">58</span>
                        ${showSec ? `<span style="font-size:1.0rem; font-weight:900; background:#1e202d; color:${accent}; border:1.2px solid ${accent}66; padding:3px 7px; border-radius:12px; margin-left:4px;">:42</span>` : ''}
                    </div>

                    <!-- Bottom Date & 60s Progress Bar -->
                    <div style="width:100%; display:flex; flex-direction:column; align-items:center; gap:6px;">
                        <span style="font-size:0.62rem; color:#aaa; font-weight:bold; background:#1b1d28; padding:2px 10px; border-radius:10px; border:1px solid #2e3144;">📅 Cuma, 4 Eylül 2026</span>
                        <div style="width:100%; height:4px; background:#222533; border-radius:2px; overflow:hidden;">
                            <div style="width:70%; height:100%; background:linear-gradient(90deg, ${accent}88, ${accent}); border-radius:2px;"></div>
                        </div>
                    </div>
                </div>
            `;
        case 'clock_fullscreen_m3':
            return `
                <div style="width:100%; height:100%; display:flex; flex-direction:column; gap:8px; padding:6px; background:transparent; font-family:'Space Grotesk',sans-serif; transform:scale(${scale});">
                    <!-- Top Hero: Giant Digital Clock Pod & Second Dial -->
                    <div style="background:#1b1d26; border-radius:24px; padding:8px 14px; border:1px solid #2d3142; display:flex; justify-content:space-between; align-items:center;">
                        <div>
                            <div style="display:flex; align-items:center; gap:6px; margin-bottom:2px;">
                                <span style="font-size:0.6rem; color:${accent}; font-weight:900;">🕒 DİJİTAL SAAT</span>
                                <span style="font-size:0.55rem; background:${accent}22; color:${accent}; padding:1px 5px; border-radius:8px; font-weight:bold;">24S</span>
                                <span style="font-size:0.55rem; background:#86efac22; color:#86efac; padding:1px 5px; border-radius:8px; font-weight:bold;">● SYNC</span>
                            </div>
                            <div style="font-size:1.55rem; font-weight:900; color:#fff; line-height:1; letter-spacing:-0.5px;">
                                21<span style="color:${accent};">:</span>58
                            </div>
                        </div>
                        <div style="width:38px; height:38px; border-radius:50%; border:2.5px solid ${accent}; display:flex; flex-direction:column; justify-content:center; align-items:center; background:#14151e;">
                            <span style="font-size:0.75rem; font-weight:900; color:#fff; font-family:monospace;">:30</span>
                            <span style="font-size:0.45rem; color:#888;">SANİYE</span>
                        </div>
                    </div>

                    <!-- Bottom 3-Piece Bento Matrix -->
                    <div style="display:grid; grid-template-columns: 1fr 1fr 1fr; gap:6px; flex:1;">
                        <!-- Pod 1: Calendar -->
                        <div style="background:#1b1d26; border-radius:18px; padding:6px 8px; border:1px solid #2d3142; display:flex; flex-direction:column; justify-content:space-between;">
                            <div style="display:flex; justify-content:space-between; align-items:center;">
                                <span style="font-size:0.55rem; font-weight:900; color:${accent};">📅 TAKVİM</span>
                                <span style="font-size:0.5rem; color:#aaa;">36. Hft</span>
                            </div>
                            <div style="font-size:0.75rem; font-weight:900; color:#fff;">Çarşamba</div>
                            <div style="display:flex; gap:2px;">
                                <span style="font-size:0.48rem; padding:1px 3px; background:#222; border-radius:4px; color:#666;">P</span>
                                <span style="font-size:0.48rem; padding:1px 3px; background:#222; border-radius:4px; color:#666;">S</span>
                                <span style="font-size:0.48rem; padding:1px 3px; background:${accent}; border-radius:4px; color:#000; font-weight:bold;">Ç</span>
                                <span style="font-size:0.48rem; padding:1px 3px; background:#222; border-radius:4px; color:#666;">P</span>
                                <span style="font-size:0.48rem; padding:1px 3px; background:#222; border-radius:4px; color:#666;">C</span>
                            </div>
                        </div>

                        <!-- Pod 2: Solar Progress -->
                        <div style="background:#1b1d26; border-radius:18px; padding:6px 8px; border:1px solid #2d3142; display:flex; flex-direction:column; justify-content:space-between;">
                            <div style="display:flex; justify-content:space-between; align-items:center;">
                                <span style="font-size:0.55rem; font-weight:900; color:#fde68a;">🌙 DÖNGÜ</span>
                                <span style="font-size:0.55rem; font-weight:bold; color:#fde68a;">%91</span>
                            </div>
                            <div style="font-size:0.72rem; font-weight:bold; color:#fff;">İyi Geceler</div>
                            <div style="width:100%; height:4px; background:#2a2c3a; border-radius:2px; overflow:hidden;">
                                <div style="width:91%; height:100%; background:#fde68a;"></div>
                            </div>
                        </div>

                        <!-- Pod 3: World UTC & Stats -->
                        <div style="background:#1b1d26; border-radius:18px; padding:6px 8px; border:1px solid #2d3142; display:flex; flex-direction:column; justify-content:space-between;">
                            <div style="display:flex; justify-content:space-between; align-items:center;">
                                <span style="font-size:0.55rem; font-weight:900; color:#d8b4fe;">🌐 DÜNYA</span>
                                <span style="font-size:0.5rem; color:#d8b4fe;">GMT+3</span>
                            </div>
                            <div style="font-size:0.72rem; font-weight:900; color:#fff;">18:58 UTC</div>
                            <div style="display:flex; gap:3px;">
                                <span style="font-size:0.5rem; background:${accent}22; color:${accent}; padding:1px 3px; border-radius:4px; font-weight:bold;">⚡ %15</span>
                                <span style="font-size:0.5rem; background:#86efac22; color:#86efac; padding:1px 3px; border-radius:4px; font-weight:bold;">💾 %48</span>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        case 'clock_m3_pixel':
            return `
                <div style="font-family:'Space Grotesk',sans-serif; text-align:center; line-height:0.95; transform:scale(${scale});">
                    <div style="font-size:1.4rem; font-weight:900; color:#fff;">12</div>
                    <div style="font-size:1.4rem; font-weight:900; color:${accent};">45</div>
                </div>
            `;
        case 'clock_m3_pill':
            return `
                <div style="width:100%; display:flex; justify-content:space-between; align-items:center; transform:scale(${scale});">
                    <div style="font-size:1.2rem; font-weight:900; color:#fff;">12:45 ${showSec ? `<span style="font-size:0.75rem; background:${accent}; color:#000; padding:1px 5px; border-radius:10px; font-weight:bold;">30</span>` : ''}</div>
                    <div style="font-size:0.7rem; color:${accent}; font-weight:bold;">Pazar</div>
                </div>
            `;
        case 'clock_giant_monolith':
            return `
                <div style="font-family:'Outfit',sans-serif; text-align:center; line-height:1; transform:scale(${scale});">
                    <div style="font-size:1.6rem; font-weight:900; color:#fff;">12<span style="color:${accent};">:</span>45 ${showSec ? `<span style="font-size:0.8rem; color:${accent};">:30</span>` : ''}</div>
                    <div style="font-size:0.65rem; color:var(--text-sub); margin-top:2px;">${title || 'MONOLITH CLOCK'}</div>
                </div>
            `;
        case 'clock_cyber_hud':
            return `
                <div style="font-family:'Orbitron',monospace; text-align:center; transform:scale(${scale});">
                    <div style="font-size:0.6rem; color:${accent};">// ${title || 'CYBER_TIME'}</div>
                    <div style="font-size:1.2rem; font-weight:900; color:#fff; text-shadow:0 0 6px ${accent};">12:45 ${showSec ? `<span style="font-size:0.75rem; color:${accent};">:30</span>` : ''}</div>
                </div>
            `;
        case 'clock_split_flip':
            return `
                <div style="display:flex; gap:4px; align-items:center; font-family:'JetBrains Mono',monospace; transform:scale(${scale});">
                    <span style="background:#181822; padding:2px 6px; border-radius:4px; font-weight:bold; font-size:1rem; color:#fff;">12</span>
                    <span style="color:${accent}; font-weight:bold;">:</span>
                    <span style="background:#181822; padding:2px 6px; border-radius:4px; font-weight:bold; font-size:1rem; color:#fff;">45</span>
                    ${showSec ? `<span style="background:#181822; padding:2px 4px; border-radius:4px; font-size:0.8rem; color:${accent};">:30</span>` : ''}
                </div>
            `;
        case 'clock_vertical_poster':
            return `
                <div style="font-family:'Bebas Neue',sans-serif; text-align:center; line-height:0.9; transform:scale(${scale});">
                    <div style="font-size:1.3rem; font-weight:900; color:#fff;">12</div>
                    <div style="font-size:1.3rem; font-weight:900; color:${accent};">45</div>
                </div>
            `;
        case 'clock_analog':
            return `
                <div style="text-align:center; font-size:1.1rem; color:${accent}; transform:scale(${scale});">🕰️ 12:45:30</div>
            `;
        case 'cpu_card':
            return `
                <div style="width:100%; display:flex; justify-content:space-between; align-items:center; transform:scale(${scale});">
                    <span style="font-size:1.1rem; font-weight:900; color:#fff;">⚡ %15</span>
                    <div style="display:flex; gap:6px; align-items:center;">
                        ${showTemp ? `<span style="font-size:0.75rem; color:${accent}; font-weight:bold;">44°C</span>` : ''}
                        ${showBadge ? `<span style="font-size:0.62rem; background:#1b1d28; border:1px solid ${accent}44; color:${accent}; padding:2px 6px; border-radius:10px; font-weight:bold;">OPTİMAL</span>` : ''}
                    </div>
                </div>
            `;
        case 'gpu_card':
            return `
                <div style="width:100%; display:flex; justify-content:space-between; align-items:center; transform:scale(${scale});">
                    <span style="font-size:1.1rem; font-weight:900; color:#fff;">🎮 %24</span>
                    <div style="display:flex; gap:6px; align-items:center;">
                        ${showTemp ? `<span style="font-size:0.75rem; color:${accent}; font-weight:bold;">52°C</span>` : ''}
                        ${showBadge ? `<span style="font-size:0.62rem; background:#1b1d28; border:1px solid #3b3b4f; color:#aaa; padding:2px 6px; border-radius:10px; font-weight:bold;">4/8 GB</span>` : ''}
                    </div>
                </div>
            `;
        case 'ram_card':
            return `
                <div style="width:100%; display:flex; justify-content:space-between; align-items:center; transform:scale(${scale});">
                    <span style="font-size:1.1rem; font-weight:900; color:#fff;">💾 %48</span>
                    <div style="display:flex; gap:6px; align-items:center;">
                        <span style="font-size:0.72rem; color:${accent};">7.8/16 GB</span>
                        ${showBadge ? `<span style="font-size:0.62rem; background:#1b1d28; border:1px solid ${accent}44; color:${accent}; padding:2px 6px; border-radius:10px; font-weight:bold;">DENGELİ</span>` : ''}
                    </div>
                </div>
            `;
        case 'network_card':
            return `
                <div style="width:100%; font-size:0.75rem; display:flex; justify-content:space-between; transform:scale(${scale});">
                    <span style="color:${accent};">⬇ 1.2 MB/s</span>
                    <span style="color:var(--accent-purple);">⬆ 320 KB/s</span>
                </div>
            `;
        case 'disk_card':
            return `
                <div style="width:100%; display:flex; justify-content:space-between; align-items:center; transform:scale(${scale});">
                    <span style="font-size:1.1rem; font-weight:900; color:#fff;">💽 %65</span>
                    <span style="font-size:0.72rem; color:${accent};">180 GB Boş</span>
                </div>
            `;
        case 'volume_card':
            return `
                <div style="width:100%; display:flex; flex-direction:column; gap:4px; transform:scale(${scale});">
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <span style="font-size:0.95rem; font-weight:bold; color:#fff;">🔊 Ses: %75</span>
                        <span style="font-size:0.7rem; background:${accent}; color:#000; padding:2px 6px; border-radius:8px; font-weight:bold;">M3 Bar</span>
                    </div>
                    <div style="width:100%; height:6px; background:#222; border-radius:4px; overflow:hidden;">
                        <div style="width:75%; height:100%; background:${accent};"></div>
                    </div>
                </div>
            `;
        case 'm3_system_hub':
            return `
                <div style="width:100%; display:flex; justify-content:space-around; align-items:center; font-size:0.7rem; font-weight:bold;">
                    <span style="background:#1c1d26; padding:3px 8px; border-radius:10px; color:#67e8f9;">CPU 15%</span>
                    <span style="background:#1c1d26; padding:3px 8px; border-radius:10px; color:#d8b4fe;">GPU 24%</span>
                    <span style="background:#1c1d26; padding:3px 8px; border-radius:10px; color:#86efac;">RAM 48%</span>
                    <span style="background:#1c1d26; padding:3px 8px; border-radius:10px; color:#fde68a;">SSD 65%</span>
                </div>
            `;
        case 'system_fullscreen_m3':
            return `
                <div style="width:100%; height:100%; display:flex; flex-direction:column; gap:8px; padding:6px; background:transparent; font-family:'Space Grotesk',sans-serif; transform:scale(${scale});">
                    <!-- Top Hero: Continuous Live Network Streamline Capsule -->
                    <div style="background:#1b1d26; border-radius:24px; padding:5px 12px; border:1px solid #2d3142; display:flex; justify-content:space-between; align-items:center;">
                        <div style="display:flex; align-items:center; gap:6px;">
                            <span style="font-size:0.75rem;">🌸</span>
                            <div>
                                <div style="font-size:0.55rem; color:#888;">İNDİRME</div>
                                <div style="font-size:0.85rem; font-weight:900; color:${accent};">⬇ 4.2 MB/s</div>
                            </div>
                        </div>
                        <div style="flex:1; height:12px; margin:0 12px;">
                            <svg width="100%" height="12" viewBox="0 0 100 12" fill="none">
                                <path d="M0 6 Q 25 0, 50 6 T 100 6" stroke="${accent}" stroke-width="1.8" fill="none"/>
                            </svg>
                        </div>
                        <div style="display:flex; align-items:center; gap:6px;">
                            <div style="text-align:right;">
                                <div style="font-size:0.55rem; color:#888;">YÜKLEME</div>
                                <div style="font-size:0.85rem; font-weight:900; color:#d8b4fe;">⬆ 680 KB/s</div>
                            </div>
                            <span style="font-size:0.55rem; background:#d8b4fe22; color:#d8b4fe; padding:2px 6px; border-radius:10px; font-weight:bold;">1 Gbps</span>
                        </div>
                    </div>

                    <!-- Bottom 4-Piece Diagonal Bento Matrix (2x2) -->
                    <div style="display:grid; grid-template-columns: 1fr 1fr; gap:8px; flex:1;">
                        <!-- CPU Diagonal Pill Pod -->
                        <div style="background:#1b1d26; border-radius:24px 8px 24px 8px; padding:8px 10px; border:1px solid #2d3142; display:flex; justify-content:space-between; align-items:center;">
                            <div>
                                <div style="font-size:0.62rem; font-weight:900; color:#fff;">⚡ CPU</div>
                                <div style="font-size:1.15rem; font-weight:900; color:#fff;">%18</div>
                            </div>
                            <span style="font-size:0.60rem; color:${accent}; background:${accent}18; padding:2px 6px; border-radius:8px; font-weight:bold;">42°C</span>
                        </div>

                        <!-- GPU Reverse Diagonal Pill Pod -->
                        <div style="background:#1b1d26; border-radius:8px 24px 8px 24px; padding:8px 10px; border:1px solid #2d3142; display:flex; justify-content:space-between; align-items:center;">
                            <div>
                                <div style="font-size:0.62rem; font-weight:900; color:#fff;">🎮 GPU</div>
                                <div style="font-size:1.15rem; font-weight:900; color:#fff;">%26</div>
                            </div>
                            <span style="font-size:0.60rem; color:#d8b4fe; background:#d8b4fe18; padding:2px 6px; border-radius:8px; font-weight:bold;">48°C</span>
                        </div>

                        <!-- RAM Reverse Diagonal Pill Pod -->
                        <div style="background:#1b1d26; border-radius:8px 24px 8px 24px; padding:8px 10px; border:1px solid #2d3142; display:flex; flex-direction:column; justify-content:space-between;">
                            <div style="display:flex; justify-content:space-between; font-size:0.62rem; font-weight:bold;">
                                <span style="color:#fff;">💾 RAM</span>
                                <span style="color:#86efac; font-weight:900;">%48</span>
                            </div>
                            <div style="width:100%; height:4px; background:#2a2c3a; border-radius:2px; overflow:hidden;">
                                <div style="width:48%; height:100%; background:#86efac;"></div>
                            </div>
                            <div style="font-size:0.55rem; color:#aaa;">7.8 / 16 GB</div>
                        </div>

                        <!-- SSD Diagonal Pill Pod -->
                        <div style="background:#1b1d26; border-radius:24px 8px 24px 8px; padding:8px 10px; border:1px solid #2d3142; display:flex; flex-direction:column; justify-content:space-between;">
                            <div style="display:flex; justify-content:space-between; font-size:0.62rem; font-weight:bold;">
                                <span style="color:#fff;">💽 SSD</span>
                                <span style="color:#fde68a; font-weight:900;">%65</span>
                            </div>
                            <div style="width:100%; height:4px; background:#2a2c3a; border-radius:2px; overflow:hidden;">
                                <div style="width:65%; height:100%; background:#fde68a;"></div>
                            </div>
                            <div style="display:flex; justify-content:space-between; font-size:0.52rem; color:#aaa;">
                                <span>340 GB Dolu</span>
                                <span>172 GB Boş</span>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        case 'm3_gauge_card':
            return `
                <div style="width:100%; display:flex; justify-content:space-around; align-items:center;">
                    <div style="text-align:center;"><span style="font-size:0.95rem; font-weight:900; color:#67e8f9;">⚡ 15%</span><div style="font-size:0.6rem; color:#888;">CPU</div></div>
                    <div style="text-align:center;"><span style="font-size:0.95rem; font-weight:900; color:#d8b4fe;">🎮 24%</span><div style="font-size:0.6rem; color:#888;">GPU</div></div>
                </div>
            `;
        case 'quick_stats':
            return `
                <div style="width:100%; display:flex; justify-content:space-around; font-size:0.72rem; font-weight:bold;">
                    <span style="color:#fff;">CPU %15</span>
                    <span style="color:#fff;">GPU %24</span>
                    <span style="color:#fff;">RAM %48</span>
                    <span style="color:${accent};">AĞ 1.2M</span>
                </div>
            `;
        case 'media_card':
            return `
                <div style="width:100%; display:flex; justify-content:space-between; align-items:center;">
                    <span style="font-size:0.8rem; color:#fff; font-weight:bold;">🎵 ${title || 'Şarkı Adı • Sanatçı'}</span>
                    <span style="font-size:0.85rem; color:${accent};">⏮ ⏯ ⏭</span>
                </div>
            `;
        case 'media_fullscreen_m3':
            return `
                <div style="width:100%; text-align:center; padding:8px 0;">
                    <div style="font-size:1.6rem; margin-bottom:4px;">📻 🎵</div>
                    <div style="font-size:0.85rem; font-weight:900; color:#fff;">${title || 'Dev Medya Merkezi (Hero)'}</div>
                    <div style="font-size:0.7rem; color:${accent}; margin-top:2px;">Albüm Kapağı • Dalga Ekolayzır • Dokunmatik Kontroller</div>
                    <div style="font-size:0.85rem; color:#fff; margin-top:6px;">🔀  ⏮  <span style="background:${accent}; color:#000; padding:2px 8px; border-radius:12px; font-weight:bold;">▶</span>  ⏭  🔁</div>
                </div>
            `;
        case 'media_vinyl':
            return `
                <div style="width:100%; display:flex; justify-content:space-between; align-items:center;">
                    <span style="font-size:0.8rem; color:#fff; font-weight:bold;">💿 ${title || 'Vinil Plak Medya'}</span>
                    <span style="font-size:0.85rem; color:${accent};">⏮ ⏯ ⏭</span>
                </div>
            `;
        case 'app_shortcut': {
            const appId = (cfg.app_id || 'spotify').toLowerCase();
            const iconMap = {
                'spotify': '🎵', 'steam': '🎮', 'discord': '💬', 'vesktop': '💬', 'code': '💻', 'vscode': '💻',
                'browser': '🌐', 'chrome': '🌐', 'firefox': '🦊', 'terminal': '📟', 'files': '📁',
                'calculator': '🔢', 'screenshot': '📸', 'flameshot': '📸', 'youtube': '▶️',
                'obsidian': '📝', 'obs': '🎥', 'lock': '🔒', 'sleep': '🌙'
            };
            const appIcon = cfg.app_icon || iconMap[appId] || '🚀';
            const defaultNameMap = {
                'spotify': 'Spotify', 'steam': 'Steam', 'discord': 'Discord', 'vesktop': 'Vesktop', 'code': 'VS Code', 'vscode': 'VS Code',
                'browser': 'Tarayıcı', 'chrome': 'Chrome', 'firefox': 'Firefox', 'terminal': 'Terminal', 'files': 'Dosyalar',
                'calculator': 'Hesap M.', 'screenshot': 'Ekran Al.', 'flameshot': 'Flameshot', 'youtube': 'YouTube',
                'obsidian': 'Obsidian', 'obs': 'OBS Studio', 'lock': 'PC Kilitle', 'sleep': 'PC Uyut'
            };
            const appLabel = title || defaultNameMap[appId] || 'Kısayol';
            const iconContent = cfg.app_icon_url ?
                `<img src="${cfg.app_icon_url}" style="width:36px; height:36px; object-fit:contain; border-radius:6px;" onerror="this.outerHTML='<span style=\\'font-size:1.6rem;\\'>${appIcon}</span>'" />` :
                `<span style="font-size:1.6rem;">${appIcon}</span>`;

            return `
                <div style="width:100%; height:100%; display:flex; flex-direction:column; align-items:center; justify-content:space-between; padding:6px 6px; background:linear-gradient(180deg, #141620 0%, #0B0C10 100%); border-radius:inherit; position:relative; overflow:hidden; transform:scale(${scale});">
                    <!-- Ambient Glow -->
                    <div style="position:absolute; top:-6px; width:50px; height:50px; border-radius:50%; background:radial-gradient(circle, ${accent}33 0%, transparent 70%); pointer-events:none;"></div>
                    
                    <!-- Center Icon -->
                    <div style="flex:1; display:flex; align-items:center; justify-content:center; z-index:1;">
                        ${iconContent}
                    </div>

                    <!-- Bottom M3 Tonal Pill -->
                    <div style="width:100%; background:#1b1d28dd; border:0.8px solid #2c2e3d; border-radius:8px; padding:2px 4px; text-align:center; z-index:1;">
                        <span style="font-size:0.68rem; font-weight:700; color:#fff; display:block; max-width:100%; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">
                            ${appLabel}
                        </span>
                    </div>
                </div>
            `;
        }
        default:
            return `<div style="color:var(--text-sub); font-size:0.8rem;">${widgetId}</div>`;
    }
}
