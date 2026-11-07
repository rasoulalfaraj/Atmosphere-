package com.example

import com.example.data.model.AirQualityData
import com.example.data.model.CurrentWeather
import com.example.data.model.IraqiCitiesCatalog
import com.example.data.model.WeatherConditionType
import com.example.util.MeteorologyCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun comfortScore_and_goldenHour_and_smartAdvisors_workCorrectly() {
        val baghdad = IraqiCitiesCatalog.findById("iq_baghdad")
        assertEquals("Baghdad", baghdad.nameEn)
        assertEquals("بغداد", baghdad.nameAr)

        val sampleCurrent = CurrentWeather(
            temperatureC = 45.5,
            feelsLikeC = 46.8,
            heatIndexC = 47.2,
            minTempC = 29.0,
            maxTempC = 46.0,
            weatherCode = 0,
            conditionType = WeatherConditionType.EXTREME_HEAT,
            descriptionEn = "Scorching Desert Heat",
            descriptionAr = "حر لاهب شديد",
            iraqiNoteEn = "Intense solar thermal dome",
            iraqiNoteAr = "كتلة حرارية شديدة",
            pressureHpa = 1006.0,
            surfacePressureHpa = 1002.0,
            humidityPercent = 18,
            dewPointC = 12.0,
            windSpeedKmh = 28.0,
            windDirectionDeg = 315,
            windDirectionCardinalEn = "NW (Shamal)",
            windDirectionCardinalAr = "شمالية غربية (شمال)",
            windGustsKmh = 44.0,
            visibilityMeters = 4500.0,
            cloudCoverPercent = 10,
            uvIndex = 9.5,
            precipitationProbPercent = 0,
            precipitationMm = 0.0,
            isDay = true,
            sunriseIso = "2026-07-15T05:05",
            sunsetIso = "2026-07-15T19:10",
            daylightDurationHours = 14.1,
            localTimeIso = "2026-07-15T14:00"
        )

        val sampleAq = AirQualityData(
            usAqi = 155,
            europeanAqi = 88,
            pm25UgM3 = 48.0,
            pm10UgM3 = 195.0,
            dustUgM3 = 160.0,
            ozoneUgM3 = 82.0,
            no2UgM3 = 24.0,
            so2UgM3 = 11.0,
            coUgM3 = 310.0,
            aerosolOpticalDepth = 0.65,
            categoryEn = "Unhealthy",
            categoryAr = "غير صحي",
            healthGuidanceEn = "Limit outdoor exertion",
            healthGuidanceAr = "قلل المجهود الخارجي",
            iraqiDustStatusEn = "Moderate Dust",
            iraqiDustStatusAr = "غبار متوسط",
            dataSourceNoteEn = "Open-Meteo CAMS",
            dataSourceNoteAr = "Open-Meteo CAMS",
            hourlyTrend = emptyList()
        )

        val comfort = MeteorologyCalculator.calculateComfortScore(sampleCurrent, sampleAq)
        assertTrue(comfort.overallScore in 5..100)

        val golden = MeteorologyCalculator.calculateGoldenHour(
            sunriseIso = sampleCurrent.sunriseIso,
            sunsetIso = sampleCurrent.sunsetIso,
            cloudCover = sampleCurrent.cloudCoverPercent,
            visibilityMeters = sampleCurrent.visibilityMeters,
            windSpeedKmh = sampleCurrent.windSpeedKmh,
            dustUgM3 = sampleAq.dustUgM3
        )
        assertEquals("05:05", golden.sunriseTime)
        assertEquals("19:10", golden.sunsetTime)

        val solar = MeteorologyCalculator.calculateSolarEnergyInsight(sampleCurrent, sampleAq, emptyList())
        assertTrue(solar.panelEfficiencyPercent in 20..100)

        val agri = MeteorologyCalculator.calculateAgricultureInsight(sampleCurrent, sampleAq)
        assertTrue(agri.evapotranspirationMmDay > 0.0)

        val health = MeteorologyCalculator.calculateHealthBiometeorology(sampleCurrent, emptyList(), sampleAq)
        assertTrue(health.dailyWaterCups >= 8)

        val prayers = MeteorologyCalculator.calculatePrayerWeatherSchedule(sampleCurrent, emptyList())
        assertEquals(6, prayers.size)

        val highways = MeteorologyCalculator.calculateIraqHighwayCorridors(sampleCurrent, sampleAq)
        assertEquals(4, highways.size)
    }
}
