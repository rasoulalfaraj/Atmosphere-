package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val OutfitFontFamily = FontFamily(
    Font(R.font.outfit, FontWeight.Normal),
    Font(R.font.outfit, FontWeight.Medium),
    Font(R.font.outfit, FontWeight.SemiBold),
    Font(R.font.outfit, FontWeight.Bold)
)

val PlusJakartaFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val CairoFontFamily = FontFamily(
    Font(R.font.cairo, FontWeight.Normal),
    Font(R.font.cairo, FontWeight.Medium),
    Font(R.font.cairo, FontWeight.SemiBold),
    Font(R.font.cairo, FontWeight.Bold)
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

fun buildAppTypography(isArabic: Boolean): Typography {
    val headingFont = if (isArabic) CairoFontFamily else OutfitFontFamily
    val bodyFont = if (isArabic) CairoFontFamily else PlusJakartaFontFamily

    return Typography(
        displayLarge = TextStyle(
            fontFamily = headingFont,
            fontWeight = FontWeight.Bold,
            fontSize = 62.sp,
            lineHeight = 68.sp,
            letterSpacing = if (isArabic) 0.sp else (-1.5).sp
        ),
        displayMedium = TextStyle(
            fontFamily = headingFont,
            fontWeight = FontWeight.Bold,
            fontSize = 42.sp,
            lineHeight = 48.sp
        ),
        displaySmall = TextStyle(
            fontFamily = headingFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            lineHeight = 38.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = headingFont,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 34.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = headingFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 21.sp,
            lineHeight = 28.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = headingFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 25.sp
        ),
        titleLarge = TextStyle(
            fontFamily = headingFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 25.sp
        ),
        titleMedium = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 22.sp
        ),
        titleSmall = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 19.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 23.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 20.sp
        ),
        bodySmall = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 17.sp
        ),
        labelLarge = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 18.sp
        ),
        labelMedium = TextStyle(
            fontFamily = if (isArabic) CairoFontFamily else JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = if (isArabic) CairoFontFamily else JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            lineHeight = 15.sp
        )
    )
}

val Typography = buildAppTypography(isArabic = false)
