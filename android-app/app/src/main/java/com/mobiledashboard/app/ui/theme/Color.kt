package com.mobiledashboard.app.ui.theme

import androidx.compose.ui.graphics.Color

// ==============================================================================
// 7 EXCLUSIVE MATERIAL 3 COLOR THEMES SPECIFICATION
// ==============================================================================

// 1. Nord Theme
val NordAccent = Color(0xFF88C0D0)
val NordBg = Color(0xFF2E3440)
val NordFg = Color(0xFFECEFF4)
val NordCardBg = Color(0xFF3B4252)
val NordContainer = Color(0xFF434C5E)
val NordContainerHigh = Color(0xFF4C566A)
val NordOutline = Color(0xFF4C566A)

// 2. Catppuccin Macchiato Theme
val CatppuccinAccent = Color(0xFFC6A0F6)
val CatppuccinBg = Color(0xFF24273A)
val CatppuccinFg = Color(0xFFCAD3F5)
val CatppuccinCardBg = Color(0xFF363A4F)
val CatppuccinContainer = Color(0xFF494D64)
val CatppuccinContainerHigh = Color(0xFF5B6078)
val CatppuccinOutline = Color(0xFF5B6078)

// 3. Everforest Dark Theme
val EverforestAccent = Color(0xFFA7C080)
val EverforestBg = Color(0xFF2D353B)
val EverforestFg = Color(0xFFD3C6AA)
val EverforestCardBg = Color(0xFF343F44)
val EverforestContainer = Color(0xFF3D484D)
val EverforestContainerHigh = Color(0xFF475258)
val EverforestOutline = Color(0xFF475258)

// 4. Tokyo Night Theme
val TokyoNightAccent = Color(0xFF7AA2F7)
val TokyoNightBg = Color(0xFF1A1B26)
val TokyoNightFg = Color(0xFFC0CAF5)
val TokyoNightCardBg = Color(0xFF24283B)
val TokyoNightContainer = Color(0xFF2F3549)
val TokyoNightContainerHigh = Color(0xFF3B4261)
val TokyoNightOutline = Color(0xFF3B4261)

// 5. Gruvbox Dark Theme
val GruvboxAccent = Color(0xFFFE8019)
val GruvboxBg = Color(0xFF282828)
val GruvboxFg = Color(0xFFEBDBB2)
val GruvboxCardBg = Color(0xFF3C3836)
val GruvboxContainer = Color(0xFF504945)
val GruvboxContainerHigh = Color(0xFF665C54)
val GruvboxOutline = Color(0xFF665C54)

// 6. Monochrome Minimal Theme
val MonochromeAccent = Color(0xFFE0E0E0)
val MonochromeBg = Color(0xFF121212)
val MonochromeFg = Color(0xFFF0F0F0)
val MonochromeCardBg = Color(0xFF1E1E1E)
val MonochromeContainer = Color(0xFF282828)
val MonochromeContainerHigh = Color(0xFF333333)
val MonochromeOutline = Color(0xFF424242)

// 7. Rosé Pine Theme
val RosePineAccent = Color(0xFFEBBCBA)
val RosePineBg = Color(0xFF191724)
val RosePineFg = Color(0xFFE0DEF4)
val RosePineCardBg = Color(0xFF1F1D2E)
val RosePineContainer = Color(0xFF26233A)
val RosePineContainerHigh = Color(0xFF312F44)
val RosePineOutline = Color(0xFF403D52)

// ==============================================================================
// BASE AMOLED & MATERIAL 3 APPLIED TOKENS
// ==============================================================================
val AmoledBlack = Color(0xFF000000)
val DarkCardBg = NordCardBg
val SubCardBg = NordContainer
val CardBorder = NordOutline
val SubBorder = NordContainerHigh

// Material 3 Surface Containers (Default fallback)
val M3Surface = NordCardBg
val M3SurfaceDim = NordBg
val M3SurfaceBright = NordContainerHigh
val M3SurfaceContainerLowest = NordBg
val M3SurfaceContainerLow = NordBg
val M3SurfaceContainer = NordCardBg
val M3SurfaceContainerHigh = NordContainer
val M3SurfaceContainerHighest = NordContainerHigh

// Material 3 Outline Tokens
val M3Outline = NordOutline
val M3OutlineVariant = NordContainer

// Typography & Text Tokens
val TextMain = NordFg
val TextSub = NordFg.copy(alpha = 0.75f)
val TextMuted = NordFg.copy(alpha = 0.50f)

// Material 3 Universal Status Colors
val M3DarkError = Color(0xFFFFB4AB)
val M3DarkOnError = Color(0xFF690005)
val M3DarkErrorContainer = Color(0xFF93000A)
val M3DarkOnErrorContainer = Color(0xFFFFDAD6)

val AccentCyan = Color(0xFF88C0D0)
val AccentGreen = Color(0xFFA7C080)
val AccentRed = Color(0xFFE06C75)
val AccentYellow = Color(0xFFE5C07B)
val AccentBlue = Color(0xFF7AA2F7)
val AccentPurple = Color(0xFFC6A0F6)
val AccentPink = Color(0xFFEBBCBA)
val AccentAmber = Color(0xFFFE8019)
val AccentRose = Color(0xFFEBBCBA)
