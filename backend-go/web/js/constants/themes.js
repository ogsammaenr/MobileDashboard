// ==============================================================================
// 7 OFFICIAL THEME PALETTES COLOR MAP
// ==============================================================================
export function getAccentColorHex(colorName) {
    switch (colorName) {
        case 'nord': return '#88c0d0';
        case 'catppuccin': return '#c6a0f6';
        case 'everforest': return '#a7c080';
        case 'tokyonight': return '#7aa2f7';
        case 'gruvbox': return '#fe8019';
        case 'monochrome': return '#e0e0e0';
        case 'rosepine': return '#ebbcba';
        case 'cyan': return '#88c0d0';
        case 'purple': return '#c6a0f6';
        case 'green': return '#a7c080';
        case 'blue': return '#7aa2f7';
        case 'amber': return '#fe8019';
        case 'white': return '#e0e0e0';
        case 'red': return '#ebbcba';
        default: return '#88c0d0';
    }
}

// ==============================================================================
// FONT SCALE MULTIPLIERS
// ==============================================================================
export function getFontScaleMultiplier(scale) {
    switch (scale) {
        case 'small': return 0.85;
        case 'large': return 1.25;
        case 'xlarge': return 1.55;
        default: return 1.0;
    }
}
