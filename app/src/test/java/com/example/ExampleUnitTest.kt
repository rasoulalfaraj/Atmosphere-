package com.example

import com.example.data.model.AppLanguage
import com.example.data.model.IraqiCitiesCatalog
import com.example.data.model.TempUnit
import com.example.data.model.UserPreferences
import com.example.data.model.WeatherConditionType
import com.example.data.model.WindUnit
import com.example.util.MeteorologyCalculator
import com.example.util.UnitFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun iraqiCitiesCatalog_containsRequiredIraqiGovernorates() {
        val namesEn = IraqiCitiesCatalog.iraqiCities.map { it.nameEn }
        assertTrue(namesEn.contains("Baghdad"))
        assertTrue(namesEn.contains("Basra"))
        assertTrue(namesEn.contains("Nasiriyah"))
        assertTrue(namesEn.contains("Najaf"))
        assertTrue(namesEn.contains("Karbala"))
        assertTrue(namesEn.contains("Mosul"))
        assertTrue(namesEn.contains("Erbil"))
        assertTrue(namesEn.contains("Sulaymaniyah"))
    }

    @Test
    fun meteorologyCalculator_detectsIraqiDustStormAndExtremeHeat() {
        val dustCond = MeteorologyCalculator.mapWmoWeatherCode(
            code = 0,
            isDay = true,
            tempC = 34.0,
            dustUgM3 = 210.0,
            pm10UgM3 = 320.0,
            windSpeedKmh = 42.0
        )
        assertEquals(WeatherConditionType.DUST_SANDSTORM, dustCond.type)

        val heatCond = MeteorologyCalculator.mapWmoWeatherCode(
            code = 0,
            isDay = true,
            tempC = 48.0,
            dustUgM3 = 30.0,
            pm10UgM3 = 50.0,
            windSpeedKmh = 12.0
        )
        assertEquals(WeatherConditionType.EXTREME_HEAT, heatCond.type)
    }

    @Test
    fun meteorologyCalculator_computesHeatIndexAndDewPoint() {
        val hi = MeteorologyCalculator.calculateHeatIndexC(42.0, 35, 14.0)
        assertTrue("Heat index at 42C/35% RH should exceed 42C", hi >= 42.0)

        val dp = MeteorologyCalculator.calculateDewPointC(30.0, 50)
        assertTrue("Dew point at 30C/50% RH should be around 18.4C", dp in 17.5..19.5)
    }

    @Test
    fun unitFormatter_convertsCelsiusToFahrenheitAndWindUnits() {
        assertEquals("32°C", UnitFormatter.formatTemp(32.0, TempUnit.CELSIUS))
        assertEquals("90°F", UnitFormatter.formatTemp(32.0, TempUnit.FAHRENHEIT))

        val prefsAr = UserPreferences(language = AppLanguage.ARABIC, windUnit = WindUnit.KMH)
        assertTrue(UnitFormatter.formatWind(20.0, prefsAr).contains("كم/س"))
    }
}
