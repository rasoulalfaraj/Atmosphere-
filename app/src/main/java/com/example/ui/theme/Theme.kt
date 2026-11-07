package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeMode

private val AtmosphereDarkColorScheme = darkColorScheme(
    primary = IonCyan,
    onPrimary = Color(0xFF031525),
    primaryContainer = Color(0xFF0C3454),
    onPrimaryContainer = Color(0xFFD0F0FF),
    secondary = SolarAmber,
    onSecondary = Color(0xFF281600),
    secondaryContainer = Color(0xFF472C04),
    onSecondaryContainer = Color(0xFFFFE5B4),
    tertiary = OasisEmerald,
    onTertiary = Color(0xFF00291B),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = DeepAtmosphereNavy,
    onBackground = Color(0xFFF1F5F9),
    surface = MidnightStratosphere,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = TwilightSlate,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    error = ExtremeHeatCrimson,
    onError = Color.White
)

private val AtmosphereLightColorScheme = lightColorScheme(
    primary = DeepAzure,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8F0FF),
    onPrimaryContainer = Color(0xFF002B49),
    secondary = ShamalDustOchre,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEDD5),
    onSecondaryContainer = Color(0xFF431407),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF064E3B),
    background = SkyDaylightBg,
    onBackground = Color(0xFF0F172A),
    surface = CloudSurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = MistContainerLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun AtmosphereAITheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    language: AppLanguage = AppLanguage.ARABIC,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.AUTO -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) AtmosphereDarkColorScheme else AtmosphereLightColorScheme
    val isArabic = language == AppLanguage.ARABIC
    val layoutDirection = if (isArabic) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = buildAppTypography(isArabic = isArabic),
            content = content
        )
    }
}
