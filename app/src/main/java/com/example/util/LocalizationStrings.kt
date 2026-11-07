package com.example.util

import com.example.data.model.AppLanguage
import com.example.data.model.PrecipUnit
import com.example.data.model.PressureUnit
import com.example.data.model.TempUnit
import com.example.data.model.UserPreferences
import com.example.data.model.WindUnit
import java.util.Locale
import kotlin.math.roundToInt

object UnitFormatter {

    fun formatTemp(tempC: Double, unit: TempUnit, includeUnit: Boolean = true): String {
        val value = when (unit) {
            TempUnit.CELSIUS -> tempC.roundToInt()
            TempUnit.FAHRENHEIT -> ((tempC * 9.0 / 5.0) + 32.0).roundToInt()
        }
        return if (includeUnit) "$value${unit.symbol}" else "$value°"
    }

    fun convertTempValue(tempC: Double, unit: TempUnit): Double {
        return when (unit) {
            TempUnit.CELSIUS -> tempC
            TempUnit.FAHRENHEIT -> (tempC * 9.0 / 5.0) + 32.0
        }
    }

    fun formatWind(speedKmh: Double, prefs: UserPreferences): String {
        val value = when (prefs.windUnit) {
            WindUnit.KMH -> speedKmh.roundToInt()
            WindUnit.MS -> (speedKmh / 3.6).roundToInt()
            WindUnit.MPH -> (speedKmh * 0.621371).roundToInt()
        }
        val unitLabel = if (prefs.language == AppLanguage.ARABIC) {
            prefs.windUnit.symbolAr
        } else {
            prefs.windUnit.symbolEn
        }
        return "$value $unitLabel"
    }

    fun formatPressure(pressureHpa: Double, unit: PressureUnit): String {
        return when (unit) {
            PressureUnit.HPA -> "${pressureHpa.roundToInt()} hPa"
            PressureUnit.INHG -> String.format(Locale.US, "%.2f inHg", pressureHpa * 0.02953)
        }
    }

    fun formatPrecip(precipMm: Double, unit: PrecipUnit): String {
        return when (unit) {
            PrecipUnit.MM -> String.format(Locale.US, "%.1f mm", precipMm)
            PrecipUnit.INCH -> String.format(Locale.US, "%.2f in", precipMm * 0.0393701)
        }
    }

    fun formatVisibility(visibilityMeters: Double, lang: AppLanguage): String {
        val km = (visibilityMeters / 1000.0).coerceIn(0.1, 25.0)
        return if (lang == AppLanguage.ARABIC) {
            String.format(Locale.US, "%.1f كم", km)
        } else {
            String.format(Locale.US, "%.1f km", km)
        }
    }
}

object AppStrings {
    fun tr(lang: AppLanguage, en: String, ar: String): String =
        if (lang == AppLanguage.ARABIC) ar else en
}
