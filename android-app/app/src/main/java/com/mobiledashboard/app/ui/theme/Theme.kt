package com.mobiledashboard.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.mobiledashboard.app.data.model.DashboardTheme

data class CustomThemeColors(
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val outlineVariant: Color,
    val glowColor: Color
)

val LocalCustomTheme = staticCompositionLocalOf {
    CustomThemeColors(
        primaryAccent = NordAccent,
        secondaryAccent = NordAccent,
        primaryContainer = NordContainer,
        onPrimaryContainer = NordFg,
        surfaceContainer = NordCardBg,
        surfaceContainerHigh = NordContainer,
        surfaceContainerHighest = NordContainerHigh,
        outlineVariant = NordOutline,
        glowColor = NordAccent.copy(alpha = 0.35f)
    )
}

/**
 * Builds authentic Material 3 Dark ColorScheme for each of the 7 official theme palettes.
 */
fun getM3ColorSchemeForTheme(theme: DashboardTheme): ColorScheme {
    return when (theme) {
        DashboardTheme.NORD -> darkColorScheme(
            primary = NordAccent,
            onPrimary = NordBg,
            primaryContainer = NordContainer,
            onPrimaryContainer = NordFg,
            secondary = Color(0xFF81A1C1),
            onSecondary = NordBg,
            secondaryContainer = NordContainerHigh,
            onSecondaryContainer = NordFg,
            tertiary = Color(0xFFB48EAD),
            onTertiary = NordBg,
            tertiaryContainer = NordContainer,
            onTertiaryContainer = NordFg,
            error = M3DarkError,
            onError = M3DarkOnError,
            errorContainer = M3DarkErrorContainer,
            onErrorContainer = M3DarkOnErrorContainer,
            background = NordBg,
            onBackground = NordFg,
            surface = NordCardBg,
            onSurface = NordFg,
            surfaceVariant = NordContainer,
            onSurfaceVariant = NordFg.copy(alpha = 0.8f),
            outline = NordOutline,
            outlineVariant = NordContainer
        )

        DashboardTheme.CATPPUCCIN -> darkColorScheme(
            primary = CatppuccinAccent,
            onPrimary = CatppuccinBg,
            primaryContainer = CatppuccinContainer,
            onPrimaryContainer = CatppuccinFg,
            secondary = Color(0xFF8AADF4),
            onSecondary = CatppuccinBg,
            secondaryContainer = CatppuccinContainerHigh,
            onSecondaryContainer = CatppuccinFg,
            tertiary = Color(0xFF8BD5CA),
            onTertiary = CatppuccinBg,
            tertiaryContainer = CatppuccinContainer,
            onTertiaryContainer = CatppuccinFg,
            error = M3DarkError,
            onError = M3DarkOnError,
            errorContainer = M3DarkErrorContainer,
            onErrorContainer = M3DarkOnErrorContainer,
            background = CatppuccinBg,
            onBackground = CatppuccinFg,
            surface = CatppuccinCardBg,
            onSurface = CatppuccinFg,
            surfaceVariant = CatppuccinContainer,
            onSurfaceVariant = CatppuccinFg.copy(alpha = 0.8f),
            outline = CatppuccinOutline,
            outlineVariant = CatppuccinContainer
        )

        DashboardTheme.EVERFOREST -> darkColorScheme(
            primary = EverforestAccent,
            onPrimary = EverforestBg,
            primaryContainer = EverforestContainer,
            onPrimaryContainer = EverforestFg,
            secondary = Color(0xFF7FBBB3),
            onSecondary = EverforestBg,
            secondaryContainer = EverforestContainerHigh,
            onSecondaryContainer = EverforestFg,
            tertiary = Color(0xFFDBBC7F),
            onTertiary = EverforestBg,
            tertiaryContainer = EverforestContainer,
            onTertiaryContainer = EverforestFg,
            error = M3DarkError,
            onError = M3DarkOnError,
            errorContainer = M3DarkErrorContainer,
            onErrorContainer = M3DarkOnErrorContainer,
            background = EverforestBg,
            onBackground = EverforestFg,
            surface = EverforestCardBg,
            onSurface = EverforestFg,
            surfaceVariant = EverforestContainer,
            onSurfaceVariant = EverforestFg.copy(alpha = 0.8f),
            outline = EverforestOutline,
            outlineVariant = EverforestContainer
        )

        DashboardTheme.TOKYONIGHT -> darkColorScheme(
            primary = TokyoNightAccent,
            onPrimary = TokyoNightBg,
            primaryContainer = TokyoNightContainer,
            onPrimaryContainer = TokyoNightFg,
            secondary = Color(0xFFBB9AF7),
            onSecondary = TokyoNightBg,
            secondaryContainer = TokyoNightContainerHigh,
            onSecondaryContainer = TokyoNightFg,
            tertiary = Color(0xFF7DCFFF),
            onTertiary = TokyoNightBg,
            tertiaryContainer = TokyoNightContainer,
            onTertiaryContainer = TokyoNightFg,
            error = M3DarkError,
            onError = M3DarkOnError,
            errorContainer = M3DarkErrorContainer,
            onErrorContainer = M3DarkOnErrorContainer,
            background = TokyoNightBg,
            onBackground = TokyoNightFg,
            surface = TokyoNightCardBg,
            onSurface = TokyoNightFg,
            surfaceVariant = TokyoNightContainer,
            onSurfaceVariant = TokyoNightFg.copy(alpha = 0.8f),
            outline = TokyoNightOutline,
            outlineVariant = TokyoNightContainer
        )

        DashboardTheme.GRUVBOX -> darkColorScheme(
            primary = GruvboxAccent,
            onPrimary = GruvboxBg,
            primaryContainer = GruvboxContainer,
            onPrimaryContainer = GruvboxFg,
            secondary = Color(0xFFFABD2F),
            onSecondary = GruvboxBg,
            secondaryContainer = GruvboxContainerHigh,
            onSecondaryContainer = GruvboxFg,
            tertiary = Color(0xFF83A598),
            onTertiary = GruvboxBg,
            tertiaryContainer = GruvboxContainer,
            onTertiaryContainer = GruvboxFg,
            error = M3DarkError,
            onError = M3DarkOnError,
            errorContainer = M3DarkErrorContainer,
            onErrorContainer = M3DarkOnErrorContainer,
            background = GruvboxBg,
            onBackground = GruvboxFg,
            surface = GruvboxCardBg,
            onSurface = GruvboxFg,
            surfaceVariant = GruvboxContainer,
            onSurfaceVariant = GruvboxFg.copy(alpha = 0.8f),
            outline = GruvboxOutline,
            outlineVariant = GruvboxContainer
        )

        DashboardTheme.MONOCHROME -> darkColorScheme(
            primary = MonochromeAccent,
            onPrimary = MonochromeBg,
            primaryContainer = MonochromeContainer,
            onPrimaryContainer = MonochromeFg,
            secondary = Color(0xFFA0A0A0),
            onSecondary = MonochromeBg,
            secondaryContainer = MonochromeContainerHigh,
            onSecondaryContainer = MonochromeFg,
            tertiary = Color(0xFF757575),
            onTertiary = MonochromeBg,
            tertiaryContainer = MonochromeContainer,
            onTertiaryContainer = MonochromeFg,
            error = M3DarkError,
            onError = M3DarkOnError,
            errorContainer = M3DarkErrorContainer,
            onErrorContainer = M3DarkOnErrorContainer,
            background = MonochromeBg,
            onBackground = MonochromeFg,
            surface = MonochromeCardBg,
            onSurface = MonochromeFg,
            surfaceVariant = MonochromeContainer,
            onSurfaceVariant = MonochromeFg.copy(alpha = 0.8f),
            outline = MonochromeOutline,
            outlineVariant = MonochromeContainer
        )

        DashboardTheme.ROSEPINE -> darkColorScheme(
            primary = RosePineAccent,
            onPrimary = RosePineBg,
            primaryContainer = RosePineContainer,
            onPrimaryContainer = RosePineFg,
            secondary = Color(0xFFC4A7E7),
            onSecondary = RosePineBg,
            secondaryContainer = RosePineContainerHigh,
            onSecondaryContainer = RosePineFg,
            tertiary = Color(0xFF9CCFD8),
            onTertiary = RosePineBg,
            tertiaryContainer = RosePineContainer,
            onTertiaryContainer = RosePineFg,
            error = M3DarkError,
            onError = M3DarkOnError,
            errorContainer = M3DarkErrorContainer,
            onErrorContainer = M3DarkOnErrorContainer,
            background = RosePineBg,
            onBackground = RosePineFg,
            surface = RosePineCardBg,
            onSurface = RosePineFg,
            surfaceVariant = RosePineContainer,
            onSurfaceVariant = RosePineFg.copy(alpha = 0.8f),
            outline = RosePineOutline,
            outlineVariant = RosePineContainer
        )
    }
}

@Composable
fun MobileDashboardTheme(
    theme: DashboardTheme = DashboardTheme.NORD,
    content: @Composable () -> Unit
) {
    val colorScheme = getM3ColorSchemeForTheme(theme)
    val glow = colorScheme.primary.copy(alpha = 0.35f)

    val customColors = CustomThemeColors(
        primaryAccent = colorScheme.primary,
        secondaryAccent = colorScheme.secondary,
        primaryContainer = colorScheme.primaryContainer,
        onPrimaryContainer = colorScheme.onPrimaryContainer,
        surfaceContainer = colorScheme.surfaceVariant,
        surfaceContainerHigh = colorScheme.secondaryContainer,
        surfaceContainerHighest = colorScheme.secondaryContainer,
        outlineVariant = colorScheme.outlineVariant,
        glowColor = glow
    )

    CompositionLocalProvider(LocalCustomTheme provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
