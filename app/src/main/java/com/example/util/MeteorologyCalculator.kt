package com.example.util

import com.example.data.model.AgricultureInsight
import com.example.data.model.AirQualityData
import com.example.data.model.AlertCategory
import com.example.data.model.AlertSeverity
import com.example.data.model.AstronomyInfo
import com.example.data.model.CityLocation
import com.example.data.model.ComfortScoreBreakdown
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyForecastItem
import com.example.data.model.GoldenHourInfo
import com.example.data.model.HealthBiometeorologyInsight
import com.example.data.model.HighwayCorridorStatus
import com.example.data.model.HourlyForecastItem
import com.example.data.model.PrayerWeatherSlot
import com.example.data.model.SolarEnergyInsight
import com.example.data.model.WeatherAlert
import com.example.data.model.WeatherConditionType
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

object MeteorologyCalculator {

    /**
     * NOAA Rothfusz Heat Index regression equation (converted to/from Fahrenheit for accuracy),
     * blended with Australian Apparent Temperature for dry windy desert conditions.
     */
    fun calculateHeatIndexC(tempC: Double, relativeHumidity: Int, windSpeedKmh: Double): Double {
        if (tempC < 26.0) {
            // Apparent temperature accounting for wind chill or mild breeze
            val e = (relativeHumidity / 100.0) * 6.105 * kotlin.math.exp((17.27 * tempC) / (237.7 + tempC))
            val wsMs = windSpeedKmh / 3.6
            return tempC + 0.33 * e - 0.70 * wsMs - 4.00
        }
        val tF = tempC * 9.0 / 5.0 + 32.0
        val rh = relativeHumidity.toDouble()
        val hiF = -42.379 +
            2.04901523 * tF +
            10.14333127 * rh -
            0.22475541 * tF * rh -
            0.00683783 * tF * tF -
            0.05481717 * rh * rh +
            0.00122874 * tF * tF * rh +
            0.00085282 * tF * rh * rh -
            0.00000199 * tF * tF * rh * rh

        var adjustedHiF = hiF
        if (rh < 13.0 && tF in 80.0..112.0) {
            adjustedHiF -= ((13.0 - rh) / 4.0) * kotlin.math.sqrt((17.0 - abs(tF - 95.0)) / 17.0)
        } else if (rh > 85.0 && tF in 80.0..87.0) {
            adjustedHiF += ((rh - 85.0) / 10.0) * ((87.0 - tF) / 5.0)
        }
        return (adjustedHiF - 32.0) * 5.0 / 9.0
    }

    /**
     * Magnus-Tetens formula for Dew Point (°C)
     */
    fun calculateDewPointC(tempC: Double, relativeHumidity: Int): Double {
        val rh = relativeHumidity.coerceIn(1, 100) / 100.0
        val a = 17.27
        val b = 237.7
        val alpha = ((a * tempC) / (b + tempC)) + ln(rh)
        return (b * alpha) / (a - alpha)
    }

    fun windCardinal(degrees: Int): Pair<String, String> {
        val norm = ((degrees % 360) + 360) % 360
        return when (norm) {
            in 338..360, in 0..22 -> "N (Shamal)" to "شمالية"
            in 23..67 -> "NE" to "شمالية شرقية"
            in 68..112 -> "E" to "شرقية"
            in 113..157 -> "SE (Sharqi)" to "جنوبية شرقية (شرجي)"
            in 158..202 -> "S" to "جنوبية"
            in 203..247 -> "SW" to "جنوبية غربية"
            in 248..292 -> "W" to "غربية"
            else -> "NW (Shamal)" to "شمالية غربية (شمال)"
        }
    }

    data class ConditionDescriptor(
        val type: WeatherConditionType,
        val descEn: String,
        val descAr: String,
        val iraqiNoteEn: String,
        val iraqiNoteAr: String
    )

    fun mapWmoWeatherCode(
        code: Int,
        isDay: Boolean,
        tempC: Double,
        dustUgM3: Double,
        pm10UgM3: Double,
        windSpeedKmh: Double
    ): ConditionDescriptor {
        // Check Iraqi environmental overrides first: severe dust/sandstorm or extreme summer heat
        if (dustUgM3 >= 140.0 || pm10UgM3 >= 180.0) {
            val isHaboob = windSpeedKmh >= 35.0 || pm10UgM3 >= 300.0
            return if (isHaboob) {
                ConditionDescriptor(
                    type = WeatherConditionType.DUST_SANDSTORM,
                    descEn = "Active Dust Storm (Tooz / Haboob)",
                    descAr = "عاصفة ترابية نشطة (طوز)",
                    iraqiNoteEn = "Shamal wind transport lifting dense Mesopotamian alluvial silt. Visibility reduced.",
                    iraqiNoteAr = "نشاط رياح الشمال يثير غباراً كثيفاً مع تدني مدى الرؤية الأفقية."
                )
            } else {
                ConditionDescriptor(
                    type = WeatherConditionType.DUST_SANDSTORM,
                    descEn = "Suspended Dust Haze",
                    descAr = "غبار عالق في الأجواء",
                    iraqiNoteEn = "Elevated fine dust particulates suspended across the basin.",
                    iraqiNoteAr = "ارتفاع تركيز الدقائق الترابية العالقة في طبقات الجو السطحية."
                )
            }
        }

        if (tempC >= 45.0 && code in listOf(0, 1, 2)) {
            return ConditionDescriptor(
                type = WeatherConditionType.EXTREME_HEAT,
                descEn = "Scorching Desert Heat",
                descAr = "حر لاهب شديد",
                iraqiNoteEn = "Intense solar thermal dome over Iraq; avoid direct midday sun exposure.",
                iraqiNoteAr = "كتلة حرارية شديدة تسيطر على الأجواء؛ تجنب التعرض المباشر للشمس ظهراً."
            )
        }

        return when (code) {
            0 -> ConditionDescriptor(
                type = if (isDay) WeatherConditionType.CLEAR_DAY else WeatherConditionType.CLEAR_NIGHT,
                descEn = if (isDay) "Clear Sky" else "Clear Starry Night",
                descAr = if (isDay) "سماء صافية ومشمسة" else "سماء صافية ليلاً",
                iraqiNoteEn = if (isDay) "High atmospheric transparency with abundant solar irradiance." else "Radiative night cooling across open plains.",
                iraqiNoteAr = if (isDay) "صفاء جوي عالٍ مع سطوع شمسي مباشر." else "انخفاض تدريجي للحرارة ليلاً بفعل الإشعاع الأرضي."
            )
            1 -> ConditionDescriptor(
                type = if (isDay) WeatherConditionType.CLEAR_DAY else WeatherConditionType.CLEAR_NIGHT,
                descEn = if (isDay) "Mostly Clear" else "Mostly Clear Night",
                descAr = if (isDay) "صافية غالباً" else "صافية غالباً ليلاً",
                iraqiNoteEn = "Stable high-pressure conditions with minimal high cirrus.",
                iraqiNoteAr = "استقرار جوي مع ظهور سحب خفيفة متفرقة."
            )
            2 -> ConditionDescriptor(
                type = WeatherConditionType.PARTLY_CLOUDY,
                descEn = "Partly Cloudy",
                descAr = "غائم جزئياً",
                iraqiNoteEn = "Scattered cumulus and mid-level clouds moderating surface heating.",
                iraqiNoteAr = "سحب متفرقة تخفف من حدة الإشعاع الشمسي."
            )
            3 -> ConditionDescriptor(
                type = WeatherConditionType.OVERCAST,
                descEn = "Overcast Clouds",
                descAr = "غائم كلياً",
                iraqiNoteEn = "Complete cloud canopy over the region.",
                iraqiNoteAr = "غطاء سحابي كامل يغطي سماء المنطقة."
            )
            45, 48 -> ConditionDescriptor(
                type = WeatherConditionType.FOG_MIST,
                descEn = "Dense Fog & Mist",
                descAr = "ضباب كثيف وشابورة",
                iraqiNoteEn = "River basin moisture inversion causing reduced morning visibility.",
                iraqiNoteAr = "تشكل الضباب في أحواض الأنهار والمنخفضات مع تراجع الرؤية."
            )
            51, 53, 55, 56, 57, 61, 80 -> ConditionDescriptor(
                type = WeatherConditionType.LIGHT_RAIN,
                descEn = "Light Rain Showers",
                descAr = "زخات مطر خفيفة",
                iraqiNoteEn = "Beneficial light precipitation settling suspended atmospheric dust.",
                iraqiNoteAr = "أمطار خفيفة تساعد على ترسيب الغبار وتلطيف الأجواء."
            )
            63, 65, 66, 67, 81, 82 -> ConditionDescriptor(
                type = WeatherConditionType.HEAVY_RAIN,
                descEn = "Moderate to Heavy Rain",
                descAr = "أمطار متوسطة إلى غزيرة",
                iraqiNoteEn = "Active Mediterranean/Red Sea trough; watch for urban ponding in low streets.",
                iraqiNoteAr = "منخفض جوي ممطر؛ يرجى الانتباه من تجمع المياه في الطرق المنخفضة."
            )
            71, 73, 75, 77, 85, 86 -> ConditionDescriptor(
                type = WeatherConditionType.SNOW,
                descEn = "Snowfall",
                descAr = "تساقط ثلوج",
                iraqiNoteEn = "Cold polar air mass bringing snow to northern highlands.",
                iraqiNoteAr = "كتلة قطبية باردة تؤدي لتساقط الثلوج فوق المرتفعات الجبلية."
            )
            95, 96, 99 -> ConditionDescriptor(
                type = WeatherConditionType.THUNDERSTORM,
                descEn = "Thunderstorm & Lightning",
                descAr = "عواصف رعدية وبرق",
                iraqiNoteEn = "Convective instability with gusty outflow boundaries and lightning risk.",
                iraqiNoteAr = "سحب رعدية ركامية مصحوبة بنشاط البرق ورياح هابطة."
            )
            else -> ConditionDescriptor(
                type = WeatherConditionType.PARTLY_CLOUDY,
                descEn = "Variable Sky",
                descAr = "أجواء متقلبة",
                iraqiNoteEn = "Transitional synoptic conditions.",
                iraqiNoteAr = "حالة جوية انتقالية."
            )
        }
    }

    /**
     * Explainable Outdoor Comfort Score (0–100)
     */
    fun calculateComfortScore(
        current: CurrentWeather,
        airQuality: AirQualityData
    ): ComfortScoreBreakdown {
        // Optimal thermal comfort around 22°C
        val tempDiff = abs(current.feelsLikeC - 22.0)
        val thermalScore = (100.0 - (tempDiff * 3.8)).roundToInt().coerceIn(5, 100)

        // Optimal humidity 35-55%
        val humPenalty = when {
            current.humidityPercent in 30..55 -> 0
            current.humidityPercent < 30 -> (30 - current.humidityPercent)
            else -> (current.humidityPercent - 55) * 2
        }
        val humidityScore = (100 - humPenalty).coerceIn(15, 100)

        // Optimal wind < 18 km/h
        val windScore = when {
            current.windSpeedKmh <= 15.0 -> 96
            current.windSpeedKmh <= 28.0 -> (95 - (current.windSpeedKmh - 15) * 2.2).roundToInt()
            else -> (65 - (current.windSpeedKmh - 28) * 1.8).roundToInt().coerceAtLeast(10)
        }

        // Air & Dust score from US AQI and PM10
        val airScore = (105 - (airQuality.usAqi * 0.45) - (airQuality.dustUgM3 * 0.12))
            .roundToInt().coerceIn(5, 100)

        // UV score
        val uvScore = (100 - (current.uvIndex * 7.5)).roundToInt().coerceIn(10, 100)

        val overall = (
            thermalScore * 0.38 +
                airScore * 0.26 +
                windScore * 0.14 +
                humidityScore * 0.12 +
                uvScore * 0.10
            ).roundToInt().coerceIn(5, 100)

        val (ratingEn, ratingAr) = when {
            overall >= 82 -> "Ideal Outdoor Comfort" to "ممتاز للأنشطة الخارجية"
            overall >= 65 -> "Comfortable & Pleasant" to "مريح ومعتدل"
            overall >= 45 -> "Moderate — Plan Ahead" to "متوسط — خطط مسبقاً"
            overall >= 28 -> "Challenging Conditions" to "أجواء مجهدة خارجياً"
            else -> "Harsh — Limit Exposure" to "قاسٍ — قلل البقاء بالخارج"
        }

        val limitingFactorEn = when (minOf(thermalScore, airScore, windScore)) {
            airScore -> "elevated dust/AQI (${airQuality.usAqi})"
            windScore -> "gusty winds (${current.windSpeedKmh.roundToInt()} km/h)"
            else -> "thermal feels-like (${current.feelsLikeC.roundToInt()}°C)"
        }
        val limitingFactorAr = when (minOf(thermalScore, airScore, windScore)) {
            airScore -> "تركيز الغبار وجودة الهواء (${airQuality.usAqi})"
            windScore -> "سرعة الرياح (${current.windSpeedKmh.roundToInt()} كم/س)"
            else -> "الحرارة المحسوسة (${current.feelsLikeC.roundToInt()}°م)"
        }

        return ComfortScoreBreakdown(
            overallScore = overall,
            thermalScore = thermalScore,
            humidityScore = humidityScore,
            windScore = windScore,
            airAndDustScore = airScore,
            uvScore = uvScore,
            ratingEn = ratingEn,
            ratingAr = ratingAr,
            explanationEn = "Score weighted by thermal comfort (${thermalScore}%), air & dust purity (${airScore}%), and wind stability (${windScore}%). Primary factor today: $limitingFactorEn.",
            explanationAr = "يعتمد المؤشر على الراحة الحرارية (${thermalScore}%) ونقاء الهواء من الغبار (${airScore}%) واستقرار الرياح (${windScore}%). العامل الأبرز حالياً: $limitingFactorAr."
        )
    }

    /**
     * Golden Hour & Weather Photography Tracker
     */
    fun calculateGoldenHour(
        sunriseIso: String,
        sunsetIso: String,
        cloudCover: Int,
        visibilityMeters: Double,
        windSpeedKmh: Double,
        dustUgM3: Double
    ): GoldenHourInfo {
        val srTime = extractHourMinute(sunriseIso, "06:15")
        val ssTime = extractHourMinute(sunsetIso, "17:10")

        val srMinutes = parseHourMinToMinutes(srTime)
        val ssMinutes = parseHourMinToMinutes(ssTime)
        val solarNoonMin = (srMinutes + ssMinutes) / 2

        val morningBlue = formatMinutesToHHMM(srMinutes - 28)
        val morningGoldenEnd = formatMinutesToHHMM(srMinutes + 45)
        val eveningGoldenStart = formatMinutesToHHMM(ssMinutes - 48)
        val eveningBlueEnd = formatMinutesToHHMM(ssMinutes + 28)
        val solarNoon = formatMinutesToHHMM(solarNoonMin)

        // Best sunset photography happens with 25-65% clouds and good visibility or warm desert dust glow
        val cloudScore = when (cloudCover) {
            in 25..65 -> 95
            in 10..24 -> 84
            in 66..85 -> 76
            else -> 68
        }
        val visScore = ((visibilityMeters / 10000.0) * 100).roundToInt().coerceIn(40, 100)
        val photoScore = ((cloudScore * 0.55) + (visScore * 0.45)).roundToInt().coerceIn(45, 98)

        val (cloudDramaEn, cloudDramaAr) = when {
            dustUgM3 > 90 -> "Amber Desert Tyndall Glow (Rich Warm Tones)" to "توهج كهرماني صحراوي دافئ عند الغروب"
            cloudCover in 25..65 -> "High Cloud Texture — Dramatic Golden Rays" to "تشكيلات سحب مثالية لإضاءة درامية"
            cloudCover < 25 -> "Clean Minimalist Horizon Gradient" to "أفق صافٍ بتدرج لوني نقي"
            else -> "Diffused Softbox Overcast Light" to "إضاءة ناعمة موزعة بسبب السحب"
        }

        val visKm = visibilityMeters / 1000.0
        val (clarityEn, clarityAr) = if (visKm >= 9.0) {
            "Crisp long-range contrast (${String.format(Locale.US, "%.1f", visKm)} km)" to "تباين بصري عالٍ للمسافات البعيدة (${String.format(Locale.US, "%.1f", visKm)} كم)"
        } else {
            "Atmospheric haze / soft telephoto compression (${String.format(Locale.US, "%.1f", visKm)} km)" to "ضبابية جوية خفيفة تضفي عمقاً للصور (${String.format(Locale.US, "%.1f", visKm)} كم)"
        }

        val (tripodEn, tripodAr) = if (windSpeedKmh < 20.0) {
            "Stable for long-exposure & drone flights (<20 km/h)" to "الرياح مستقرة للتعريض الطويل والطائرات المسيرة"
        } else {
            "Use weighted tripod; wind gusts may vibrate telephoto lenses" to "ينصح بتثبيت الحامل الثلاثي جيداً بسبب هبات الرياح"
        }

        return GoldenHourInfo(
            morningBlueHour = "$morningBlue – $srTime",
            morningGoldenHour = "$srTime – $morningGoldenEnd",
            sunriseTime = srTime,
            solarNoonTime = solarNoon,
            eveningGoldenHourStart = "$eveningGoldenStart – $ssTime",
            sunsetTime = ssTime,
            eveningBlueHourEnd = "$ssTime – $eveningBlueEnd",
            photographyScore = photoScore,
            cloudDramaEn = cloudDramaEn,
            cloudDramaAr = cloudDramaAr,
            atmosphericClarityEn = clarityEn,
            atmosphericClarityAr = clarityAr,
            tripodWindAdviceEn = tripodEn,
            tripodWindAdviceAr = tripodAr
        )
    }

    /**
     * Synodic Moon Phase & Night-Sky Stargazing Calculator
     */
    fun calculateAstronomy(
        timestampMillis: Long,
        cloudCover: Int,
        humidity: Int,
        dustUgM3: Double
    ): AstronomyInfo {
        // Reference Known New Moon: 2000-01-06 18:14 UTC (947182440000L)
        val synodicMonthDays = 29.530588853
        val daysSinceRef = (timestampMillis - 947182440000L).toDouble() / (1000.0 * 86400.0)
        val phaseFraction = ((daysSinceRef / synodicMonthDays) % 1.0 + 1.0) % 1.0
        val moonAge = phaseFraction * synodicMonthDays
        val illumination = ((1.0 - cos(phaseFraction * 2.0 * Math.PI)) / 2.0 * 100.0).roundToInt()

        val (phaseEn, phaseAr) = when {
            phaseFraction < 0.03 || phaseFraction > 0.97 -> "New Moon" to "المحق (قمر جديد)"
            phaseFraction < 0.22 -> "Waxing Crescent" to "هلال متزايد"
            phaseFraction < 0.28 -> "First Quarter" to "التربيع الأول"
            phaseFraction < 0.47 -> "Waxing Gibbous" to "أحدب متزايد"
            phaseFraction < 0.53 -> "Full Moon" to "بدر كامل"
            phaseFraction < 0.72 -> "Waning Gibbous" to "أحدب متناقص"
            phaseFraction < 0.78 -> "Last Quarter" to "التربيع الأخير"
            else -> "Waning Crescent" to "هلال آخر الشهر"
        }

        // Estimate moonrise/moonset shift across the synodic cycle
        val offsetMin = (phaseFraction * 24.0 * 60.0).roundToInt()
        val moonriseMin = (360 + offsetMin) % 1440
        val moonsetMin = (1080 + offsetMin) % 1440

        val stargazingScore = (
            100 - (cloudCover * 0.65) - (dustUgM3 * 0.15) - (illumination * 0.20) - ((humidity - 40).coerceAtLeast(0) * 0.2)
            ).roundToInt().coerceIn(10, 99)

        val (starEn, starAr) = when {
            stargazingScore >= 78 -> "Excellent night-sky transparency for deep-sky observation" to "صفاء ممتاز لرصد النجوم والكواكب ليلاً"
            stargazingScore >= 52 -> "Good planetary & lunar viewing; moderate sky glow" to "رؤية فلكية جيدة للقمر والكواكب اللامعة"
            else -> "Limited stargazing due to cloud cover or atmospheric dust" to "رؤية فلكية محدودة بسبب السحب أو الغبار العالق"
        }

        return AstronomyInfo(
            moonPhaseNameEn = phaseEn,
            moonPhaseNameAr = phaseAr,
            illuminationPercent = illumination,
            moonAgeDays = moonAge,
            moonPhaseFraction = phaseFraction,
            moonriseEst = formatMinutesToHHMM(moonriseMin),
            moonsetEst = formatMinutesToHHMM(moonsetMin),
            stargazingScore = stargazingScore,
            stargazingConditionEn = starEn,
            stargazingConditionAr = starAr
        )
    }

    /**
     * Generates real, threshold-grounded Meteorological Alerts & Advisories
     */
    fun evaluateWeatherAlerts(
        city: CityLocation,
        current: CurrentWeather,
        hourly: List<HourlyForecastItem>,
        daily: List<DailyForecastItem>,
        airQuality: AirQualityData
    ): List<WeatherAlert> {
        val alerts = mutableListOf<WeatherAlert>()
        val maxUpcomingTemp = max(current.temperatureC, daily.take(2).maxOfOrNull { it.maxTempC } ?: current.maxTempC)
        val maxUpcomingWind = max(current.windGustsKmh, daily.take(2).maxOfOrNull { it.windGustsMaxKmh } ?: current.windGustsKmh)
        val maxUpcomingRain = daily.take(2).maxOfOrNull { it.precipSumMm } ?: current.precipitationMm
        val maxUpcomingUv = max(current.uvIndex, daily.take(2).maxOfOrNull { it.uvIndexMax } ?: current.uvIndex)

        // 1. Dust & Sandstorm Alert (Iraq Priority)
        if (airQuality.dustUgM3 >= 85.0 || airQuality.pm10UgM3 >= 120.0) {
            val isSevere = airQuality.dustUgM3 >= 180.0 || airQuality.pm10UgM3 >= 250.0
            alerts.add(
                WeatherAlert(
                    id = "alert_dust_${city.id}",
                    category = AlertCategory.DUST_SANDSTORM,
                    severity = if (isSevere) AlertSeverity.WARNING else AlertSeverity.ADVISORY,
                    titleEn = if (isSevere) "Dense Dust & Sandstorm Warning (Tooz)" else "Suspended Dust & PM10 Advisory",
                    titleAr = if (isSevere) "تحذير من عاصفة ترابية كثيفة (طوز)" else "تنبيه من غبار عالق وارتفاع الدقائق الترابية",
                    areaEn = "${city.nameEn} & ${city.governorateEn}",
                    areaAr = "${city.nameAr} و${city.governorateAr}",
                    summaryEn = "Atmospheric dust concentration has reached ${airQuality.dustUgM3.roundToInt()} µg/m³ with PM10 at ${airQuality.pm10UgM3.roundToInt()} µg/m³. Horizontal visibility is ${String.format(Locale.US, "%.1f km", current.visibilityMeters / 1000.0)}.",
                    summaryAr = "بلغ تركيز الغبار الجوي ${airQuality.dustUgM3.roundToInt()} مايكروغرام/م³ مع وصول PM10 إلى ${airQuality.pm10UgM3.roundToInt()} مايكروغرام/م³. مدى الرؤية الأفقية حالياً ${String.format(Locale.US, "%.1f كم", current.visibilityMeters / 1000.0)}.",
                    startTimeIso = current.localTimeIso,
                    endTimeIso = "+18h Synoptic Window",
                    authorityEn = "Atmosphere AI Environmental Telemetry (IMOS / CAMS Thresholds)",
                    authorityAr = "منظومة الرصد البيئي Atmosphere AI (وفق معايير الأنواء الجوية العراقية)",
                    precautionsEn = listOf(
                        "Keep windows sealed and run indoor HEPA filtration on recirculation mode.",
                        "Wear an N95/KN95 respirator if outdoor travel is unavoidable, especially for asthma patients.",
                        "Reduce highway driving speed and use low-beam headlights in dust corridors."
                    ),
                    precautionsAr = listOf(
                        "أحكم إغلاق النوافذ وشغّل أجهزة تنقية الهواء الداخلية.",
                        "ارتدِ كمامة واقية عند الخروج، خاصة لمرضى الربو والحساسية.",
                        "خفف السرعة على الطرق الخارجية واستخدم الأنوار المنخفضة."
                    ),
                    metricTriggerText = "PM10: ${airQuality.pm10UgM3.roundToInt()} µg/m³ • Dust: ${airQuality.dustUgM3.roundToInt()} µg/m³",
                    isOfficialFeed = false
                )
            )
        }

        // 2. Extreme Heat Alert
        if (maxUpcomingTemp >= 40.0 || current.heatIndexC >= 41.0) {
            val isExtreme = maxUpcomingTemp >= 47.0
            alerts.add(
                WeatherAlert(
                    id = "alert_heat_${city.id}",
                    category = AlertCategory.EXTREME_HEAT,
                    severity = if (isExtreme) AlertSeverity.EXTREME else AlertSeverity.WARNING,
                    titleEn = if (isExtreme) "Extreme Mesopotamian Heat Dome (47°C+)" else "High Thermal Stress & Heat Advisory",
                    titleAr = if (isExtreme) "تحذير أقصى: موجة حر شديدة تتجاوز 47°م" else "تنبيه من إجهاد حراري وارتفاع درجات الحرارة",
                    areaEn = "${city.nameEn} Metropolitan Basin",
                    areaAr = "عموم ${city.nameAr} والمناطق المجاورة",
                    summaryEn = "Peak daytime temperature reaching ${maxUpcomingTemp.roundToInt()}°C with a Heat Index of ${current.heatIndexC.roundToInt()}°C. High risk of heat exhaustion during solar noon (11:00–16:00).",
                    summaryAr = "تصل درجة الحرارة العظمى إلى ${maxUpcomingTemp.roundToInt()}°م مع مؤشر حرارة محسوسة ${current.heatIndexC.roundToInt()}°م. خطر الإجهاد الحراري يرتفع بين الساعة 11 صباحاً و4 عصراً.",
                    startTimeIso = "11:00 Local",
                    endTimeIso = "17:00 Local",
                    authorityEn = "Atmosphere AI Thermal Intelligence (NOAA / IMOS Heat Thresholds)",
                    authorityAr = "تحليل الإجهاد الحراري Atmosphere AI",
                    precautionsEn = listOf(
                        "Avoid direct sun exposure between 11:00 AM and 4:00 PM.",
                        "Drink water and electrolytes proactively every 30 minutes outdoors.",
                        "Never leave children, pets, or electronics inside parked vehicles."
                    ),
                    precautionsAr = listOf(
                        "تجنب التعرض المباشر لأشعة الشمس بين الساعة 11 ظهراً و4 عصراً.",
                        "أكثر من شرب الماء والسوائل لتعويض الأملاح باستمرار.",
                        "لا تترك الأطفال أو المواد القابلة للاشتعال داخل المركبات المتوقفة."
                    ),
                    metricTriggerText = "Max: ${maxUpcomingTemp.roundToInt()}°C • Heat Index: ${current.heatIndexC.roundToInt()}°C"
                )
            )
        }

        // 3. Shamal / Strong Wind Alert
        if (maxUpcomingWind >= 38.0) {
            alerts.add(
                WeatherAlert(
                    id = "alert_wind_${city.id}",
                    category = AlertCategory.SHAMAL_WIND,
                    severity = if (maxUpcomingWind >= 55.0) AlertSeverity.WARNING else AlertSeverity.ADVISORY,
                    titleEn = "Active Shamal Wind Gusts Advisory",
                    titleAr = "تنبيه من نشاط رياح الشمال والهبات القوية",
                    areaEn = "${city.nameEn} & Open Highway Corridors",
                    areaAr = "${city.nameAr} والطرق الخارجية المفتوحة",
                    summaryEn = "Surface winds sustained at ${current.windSpeedKmh.roundToInt()} km/h with gusts reaching ${maxUpcomingWind.roundToInt()} km/h (${current.windDirectionCardinalEn}). Potential for localized blowing dust.",
                    summaryAr = "تهب رياح بسرعة ${current.windSpeedKmh.roundToInt()} كم/س مع هبات تصل إلى ${maxUpcomingWind.roundToInt()} كم/س (${current.windDirectionCardinalAr}) مما قد يثير الغبار المحلي.",
                    startTimeIso = current.localTimeIso,
                    endTimeIso = "+24h Forecast Window",
                    authorityEn = "Atmosphere AI Synoptic Wind Monitor",
                    authorityAr = "مرصد الرياح السطحية Atmosphere AI",
                    precautionsEn = listOf(
                        "Secure loose rooftop items, solar panels, and outdoor awnings.",
                        "Exercise caution when driving high-profile vehicles on open inter-city roads."
                    ),
                    precautionsAr = listOf(
                        "ثبّت الأجسام غير المستقرة والمظلات على الأسطح والحدائق.",
                        "انتبه أثناء القيادة على الطرق السريعة المكشوفة بين المحافظات."
                    ),
                    metricTriggerText = "Gusts: ${maxUpcomingWind.roundToInt()} km/h • Dir: ${current.windDirectionDeg}°"
                )
            )
        }

        // 4. Heavy Rain / Thunderstorm Alert
        if (maxUpcomingRain >= 8.0 || current.conditionType == WeatherConditionType.THUNDERSTORM) {
            alerts.add(
                WeatherAlert(
                    id = "alert_rain_${city.id}",
                    category = if (current.conditionType == WeatherConditionType.THUNDERSTORM) AlertCategory.THUNDERSTORM_LIGHTNING else AlertCategory.HEAVY_RAIN_FLOOD,
                    severity = if (maxUpcomingRain >= 20.0) AlertSeverity.WARNING else AlertSeverity.WATCH,
                    titleEn = "Convective Rain & Urban Runoff Watch",
                    titleAr = "مراقبة جوية: أمطار رعدية وتجمعات مياه",
                    areaEn = "${city.nameEn} Basin",
                    areaAr = "${city.nameAr} والمناطق المنخفضة",
                    summaryEn = "Forecast accumulation of ${String.format(Locale.US, "%.1f mm", maxUpcomingRain)} over the next 48 hours with convective cloud bands.",
                    summaryAr = "توقعات بهطول أمطار تصل إلى ${String.format(Locale.US, "%.1f ملم", maxUpcomingRain)} خلال الـ 48 ساعة القادمة مع سحب ركامية.",
                    startTimeIso = current.localTimeIso,
                    endTimeIso = "+48h Window",
                    authorityEn = "Atmosphere AI Hydro-Meteorological Model",
                    authorityAr = "النموذج الهيدرولوجي Atmosphere AI",
                    precautionsEn = listOf(
                        "Avoid underpasses and low-lying drainage channels during peak downpours.",
                        "Unplug sensitive outdoor electrical equipment during lightning activity."
                    ),
                    precautionsAr = listOf(
                        "تجنب الأنفاق والطرق المنخفضة أثناء غزارة الهطول.",
                        "ابتعد عن الأعمدة المعدنية والأماكن المكشوفة عند حدوث البرق."
                    ),
                    metricTriggerText = "Precip: ${String.format(Locale.US, "%.1f mm", maxUpcomingRain)}"
                )
            )
        }

        // 5. UV / Solar Radiation Advisory (ensures every city has at least 1 actionable environmental bulletin)
        if (maxUpcomingUv >= 6.5 || alerts.isEmpty()) {
            alerts.add(
                WeatherAlert(
                    id = "alert_uv_${city.id}",
                    category = AlertCategory.HIGH_UV_RADIATION,
                    severity = if (maxUpcomingUv >= 8.5) AlertSeverity.WATCH else AlertSeverity.ADVISORY,
                    titleEn = if (maxUpcomingUv >= 7.0) "High Solar UV & Atmospheric Bulletin" else "Regional Synoptic Weather Bulletin",
                    titleAr = if (maxUpcomingUv >= 7.0) "تنبيه من ارتفاع الأشعة فوق البنفسجية" else "النشرة الجوية الإقليمية للمحافظة",
                    areaEn = "${city.nameEn} (${city.governorateEn})",
                    areaAr = "${city.nameAr} (${city.governorateAr})",
                    summaryEn = "Peak UV Index reaches ${String.format(Locale.US, "%.1f", maxUpcomingUv)} with surface pressure at ${current.pressureHpa.roundToInt()} hPa and ${current.windDirectionCardinalEn} airflow.",
                    summaryAr = "يسجل مؤشر الأشعة فوق البنفسجية ${String.format(Locale.US, "%.1f", maxUpcomingUv)} مع ضغط جوي سطحي ${current.pressureHpa.roundToInt()} هكتوباسكال ورياح ${current.windDirectionCardinalAr}.",
                    startTimeIso = current.sunriseIso,
                    endTimeIso = current.sunsetIso,
                    authorityEn = "Atmosphere AI Environmental Intelligence (Grounded Telemetry)",
                    authorityAr = "مركز معلومات Atmosphere AI (بيانات رصد حقيقية)",
                    precautionsEn = listOf(
                        "Use UV-400 sunglasses and SPF 30+ protection during midday hours.",
                        "Monitor hourly wind and PM10 dust charts before planning extended outdoor sports."
                    ),
                    precautionsAr = listOf(
                        "استخدم النظارات الشمسية الواقية من الأشعة فوق البنفسجية ظهراً.",
                        "تابع مخططات الغبار والرياح الساعية قبل التخطيط للرحلات الخارجية."
                    ),
                    metricTriggerText = "UV Max: ${String.format(Locale.US, "%.1f", maxUpcomingUv)} • Pressure: ${current.pressureHpa.roundToInt()} hPa"
                )
            )
        }

        return alerts
    }

    /**
     * 1. Solar Rooftop & Home Inverter Optimizer (Crucial for Iraqi homes & solar arrays)
     */
    fun calculateSolarEnergyInsight(
        current: CurrentWeather,
        airQuality: AirQualityData,
        daily: List<DailyForecastItem>
    ): SolarEnergyInsight {
        val cloudLoss = (current.cloudCoverPercent * 0.55).roundToInt()
        val dustSoilingLoss = ((airQuality.dustUgM3 / 18.0) + (airQuality.pm10UgM3 / 32.0))
            .roundToInt().coerceIn(2, 38)
        // PV cell temperature exceeds ambient by ~18°C under full sun; -0.4% per °C above 25°C
        val cellTempC = current.temperatureC + if (current.isDay) 16.0 else 0.0
        val heatDerating = if (cellTempC > 25.0) {
            ((cellTempC - 25.0) * 0.42).roundToInt().coerceIn(0, 22)
        } else {
            0
        }

        val netEfficiency = (100 - (cloudLoss * 0.4).roundToInt() - dustSoilingLoss - heatDerating)
            .coerceIn(28, 97)
        val peakSunHours = (current.daylightDurationHours * (1.0 - current.cloudCoverPercent / 150.0) * 0.62)
            .coerceIn(2.2, 8.4)
        val est5KwOutput = (5.0 * peakSunHours * (netEfficiency / 100.0)).coerceIn(4.5, 34.0)

        val upcomingDustOrRain = daily.take(2).any { it.precipProbMaxPercent >= 35 } || airQuality.dustUgM3 >= 110.0
        val (cleanEn, cleanAr) = if (upcomingDustOrRain) {
            "Wait 48 hours before washing panels — active dust or rain showers expected soon." to
                "تريّث 48 ساعة قبل غسل الألواح الشمسية — يُتوقع نشاط للغبار أو زخات مطر قريباً."
        } else if (dustSoilingLoss >= 12) {
            "Recommended: Wash solar panels early tomorrow morning (before 06:30) to recover +$dustSoilingLoss% output." to
                "موصى به: اغسل الألواح الشمسية في الصباح الباكر (قبل 6:30 ص) لاستعادة +$dustSoilingLoss% من الكفاءة."
        } else {
            "Panels are operating cleanly with minimal dust attenuation ($dustSoilingLoss% loss)." to
                "الألواح تعمل بكفاءة عالية مع فاقد غبار طفيف ($dustSoilingLoss% فقط)."
        }

        val (invEn, invAr) = if (current.temperatureC >= 40.0) {
            "Ensure inverter room ventilation is active; high ambient heat reduces charging efficiency." to
                "تأكد من تهوية غرفة الإنفرتر والبطاريات جيداً لتفادي الإجهاد الحراري وقت الظهيرة."
        } else {
            "Optimal charging window between 09:00 and 15:00 local solar time." to
                "أفضل ذروة لشحن منظومة الطاقة الشمسية والبطاريات بين 9:00 صباحاً و3:00 عصراً."
        }

        return SolarEnergyInsight(
            panelEfficiencyPercent = netEfficiency,
            peakSunHours = peakSunHours,
            dustSoilingLossPercent = dustSoilingLoss,
            heatDeratingPercent = heatDerating,
            estimatedKwhPer5KwSystem = est5KwOutput,
            cleaningAdviceEn = cleanEn,
            cleaningAdviceAr = cleanAr,
            inverterNoteEn = invEn,
            inverterNoteAr = invAr
        )
    }

    /**
     * 2. Smart Agriculture, Date Palm & Irrigation Evapotranspiration Advisor
     */
    fun calculateAgricultureInsight(
        current: CurrentWeather,
        airQuality: AirQualityData
    ): AgricultureInsight {
        // Hargreaves/Penman simplified daily reference Evapotranspiration (ET0 in mm/day)
        val tempRange = (current.maxTempC - current.minTempC).coerceAtLeast(6.0)
        val windFactor = 1.0 + (current.windSpeedKmh / 65.0)
        val et0 = (0.0023 * (current.temperatureC + 17.8) * kotlin.math.sqrt(tempRange) * 3.6 * windFactor)
            .coerceIn(1.5, 14.5)

        val soilStress = ((et0 / 12.0 * 65.0) + ((100 - current.humidityPercent) * 0.35))
            .roundToInt().coerceIn(15, 98)

        val (irrigEn, irrigAr) = when {
            et0 >= 7.5 -> "High Evapotranspiration (${String.format(Locale.US, "%.1f", et0)} mm/day) — Irrigate at dawn or after sunset to prevent 40% evaporation loss." to
                "تبخر ونتح مرتفع (${String.format(Locale.US, "%.1f", et0)} ملم/يوم) — يُنصح بالسقي فجراً أو بعد الغروب لتقليل فاقد التبخر بنسبة 40%."
            et0 >= 4.5 -> "Moderate water demand (${String.format(Locale.US, "%.1f", et0)} mm/day) — Standard drip/basin irrigation schedule." to
                "احتياج مائي معتدل (${String.format(Locale.US, "%.1f", et0)} ملم/يوم) — جدول الري بالتنقيط أو الأحواض المعتاد."
            else -> "Low evaporation rate (${String.format(Locale.US, "%.1f", et0)} mm/day) — Conserve irrigation water; check soil moisture." to
                "معدل تبخر منخفض (${String.format(Locale.US, "%.1f", et0)} ملم/يوم) — قلل كميات السقي لتجنب تشبع التربة."
        }

        val (palmEn, palmAr) = if (airQuality.dustUgM3 >= 95.0) {
            "Date Palms & Orchards: Dust accumulation on fronds increases mite risk; foliar water wash recommended." to
                "بساتين النخيل والحمضيات: تراكم الغبار على السعف يزيد خطر عنكبوت الغبار (الحمير)؛ يُنصح بالغسل المائي."
        } else {
            "Date Palms & Crops: Favorable thermal & humidity balance for vegetative growth." to
                "بساتين النخيل والمحاصيل: توازن حراري ورطوبي ملائم لنمو الأشجار والمحاصيل الحقلية."
        }

        val (sprayEn, sprayAr) = if (current.windSpeedKmh <= 15.0 && current.precipitationProbPercent < 25) {
            "Suitable for foliar fertilization & pest control (Wind ${current.windSpeedKmh.roundToInt()} km/h < 15 km/h)." to
                "الوقت مناسب للرش الورقي والمكافحة الزراعية (سرعة الرياح ${current.windSpeedKmh.roundToInt()} كم/س ضمن الحد الآمن)."
        } else {
            "Avoid agricultural spraying right now — wind drift (${current.windSpeedKmh.roundToInt()} km/h) or rain risk." to
                "تجنب الرش الزراعي حالياً — سرعة الرياح (${current.windSpeedKmh.roundToInt()} كم/س) تؤدي لتطاير المبيدات والأسمدة."
        }

        return AgricultureInsight(
            evapotranspirationMmDay = et0,
            irrigationNeedEn = irrigEn,
            irrigationNeedAr = irrigAr,
            datePalmStatusEn = palmEn,
            datePalmStatusAr = palmAr,
            sprayingWindowEn = sprayEn,
            sprayingWindowAr = sprayAr,
            soilStressScore = soilStress
        )
    }

    /**
     * 3. Biometeorological Health, Hydration & Barometric Migraine Radar
     */
    fun calculateHealthBiometeorology(
        current: CurrentWeather,
        hourly: List<HourlyForecastItem>,
        airQuality: AirQualityData
    ): HealthBiometeorologyInsight {
        // Daily hydration requirement based on temperature, heat index, and dry air
        val baseLiters = 2.2
        val heatExtra = ((current.maxTempC - 24.0).coerceAtLeast(0.0)) * 0.085
        val dryExtra = if (current.humidityPercent < 25) 0.35 else 0.0
        val waterLiters = (baseLiters + heatExtra + dryExtra).coerceIn(2.0, 5.2)
        val cups = (waterLiters / 0.25).roundToInt()

        val respLevel = when {
            airQuality.usAqi >= 150 || airQuality.pm10UgM3 >= 180 -> 4
            airQuality.usAqi >= 100 || airQuality.pm10UgM3 >= 100 -> 3
            airQuality.usAqi >= 55 -> 2
            else -> 1
        }
        val (respEn, respAr) = when (respLevel) {
            4 -> "High Respiratory Alert — Keep asthma inhalers nearby & wear N95 mask outdoors." to
                "خطر تنفسي مرتفع — يُنصح مرضى الربو والحساسية بالبقاء بالداخل أو ارتداء كمامة N95."
            3 -> "Moderate Dust Sensitivity — Limit vigorous outdoor running during afternoon winds." to
                "حساسية متوسطة للغبار — قلل المجهود الرياضي الخارجي عند نشاط الرياح."
            2 -> "Mild Air Sensitivity — Safe for general public; monitor midday PM10." to
                "تنبيه خفيف — الهواء مناسب للعموم مع متابعة الدقائق الترابية ظهراً."
            else -> "Low Respiratory Risk — Clean air favorable for deep breathing & outdoor exercise." to
                "مخاطر تنفسية منخفضة — هواء نقي ومناسب تماماً للرياضة والتنزه."
        }

        // Barometric pressure swing over next 12 hours
        val pNow = current.pressureHpa
        val p12 = hourly.take(12).map { it.pressureHpa }
        val maxSwing = p12.maxOfOrNull { abs(it - pNow) } ?: 1.5
        val migLevel = when {
            maxSwing >= 6.0 -> 4
            maxSwing >= 3.5 -> 3
            maxSwing >= 2.0 -> 2
            else -> 1
        }
        val (migEn, migAr) = when (migLevel) {
            4, 3 -> "Barometric Swing (${String.format(Locale.US, "%.1f", maxSwing)} hPa) — Elevated migraine & sinus headache trigger." to
                "تذبذب الضغط الجوي (${String.format(Locale.US, "%.1f", maxSwing)} hPa) — قد يحفز الصداع النصفي والشقيقة."
            else -> "Stable Barometric Pressure (${current.pressureHpa.roundToInt()} hPa) — Low weather-related headache risk." to
                "ضغط جوي مستقر (${current.pressureHpa.roundToInt()} hPa) — احتمالية منخفضة لصداع تقلبات الطقس."
        }

        val (heatEn, heatAr) = if (current.heatIndexC >= 39.0) {
            "High Thermal Load (Heat Index ${current.heatIndexC.roundToInt()}°C) — Drink 1 cup of water every 30–40 min." to
                "إجهاد حراري مرتفع (المحسوسة ${current.heatIndexC.roundToInt()}°م) — اشرب كوب ماء كل 30–40 دقيقة."
        } else {
            "Comfortable Thermal Balance — Maintain steady hydration ($cups cups / ${String.format(Locale.US, "%.1f", waterLiters)} L today)." to
                "توازن حراري مريح — حافظ على شرب $cups أكواب (${String.format(Locale.US, "%.1f", waterLiters)} لتر) من الماء اليوم."
        }

        val (sinusEn, sinusAr) = if (current.humidityPercent < 25) {
            "Dry air (${current.humidityPercent}% RH) may cause dry eyes & nasal passages; saline spray or indoor humidifier helps." to
                "جفاف الهواء (${current.humidityPercent}%) قد يسبب جفاف الجيوب الأنفية والعيون؛ يُنصح بترطيب الجو الداخلي."
        } else {
            "Balanced humidity (${current.humidityPercent}%) for joint & sinus comfort." to
                "رطوبة متوازنة (${current.humidityPercent}%) مريحة للمفاصل والجيوب الأنفية."
        }

        return HealthBiometeorologyInsight(
            dailyWaterLiters = waterLiters,
            dailyWaterCups = cups,
            respiratoryDustRiskEn = respEn,
            respiratoryDustRiskAr = respAr,
            respiratoryRiskLevel = respLevel,
            migrainePressureRiskEn = migEn,
            migrainePressureRiskAr = migAr,
            migraineRiskLevel = migLevel,
            heatStrokeRiskEn = heatEn,
            heatStrokeRiskAr = heatAr,
            sinusAndJointNoteEn = sinusEn,
            sinusAndJointNoteAr = sinusAr
        )
    }

    /**
     * 4. Astronomical Prayer Times & Weather-Per-Prayer Schedule
     */
    fun calculatePrayerWeatherSchedule(
        current: CurrentWeather,
        hourly: List<HourlyForecastItem>
    ): List<PrayerWeatherSlot> {
        val srTime = extractHourMinute(current.sunriseIso, "06:18")
        val ssTime = extractHourMinute(current.sunsetIso, "17:08")
        val srMin = parseHourMinToMinutes(srTime)
        val ssMin = parseHourMinToMinutes(ssTime)
        val dhuhrMin = (srMin + ssMin) / 2 + 2
        val daySpan = (ssMin - srMin).coerceAtLeast(480)
        val fajrMin = srMin - 78
        val asrMin = dhuhrMin + (daySpan * 0.32).roundToInt()
        val maghribMin = ssMin + 4
        val ishaMin = ssMin + 82

        val slots = listOf(
            Triple("Fajr", "الفجر", fajrMin),
            Triple("Sunrise", "الشروق", srMin),
            Triple("Dhuhr", "الظهر", dhuhrMin),
            Triple("Asr", "العصر", asrMin),
            Triple("Maghrib", "المغرب", maghribMin),
            Triple("Isha", "العشاء", ishaMin)
        )

        return slots.map { (nameEn, nameAr, minutes) ->
            val targetHour = ((minutes / 60) % 24).coerceIn(0, 23)
            val hhStr = String.format(Locale.US, "%02d:", targetHour)
            val matchedHour = hourly.take(24).firstOrNull { it.hourLabel.startsWith(hhStr) }
            val temp = matchedHour?.temperatureC ?: current.temperatureC
            val wind = matchedHour?.windSpeedKmh ?: current.windSpeedKmh
            val rainProb = matchedHour?.precipProbPercent ?: current.precipitationProbPercent

            val (statusEn, statusAr) = when {
                rainProb >= 35 -> "Rain chance $rainProb%" to "احتمال مطر $rainProb%"
                temp >= 40.0 -> "Hot (${temp.roundToInt()}°C)" to "حار (${temp.roundToInt()}°م)"
                temp <= 14.0 -> "Cool breeze" to "نسيم بارد"
                else -> "Pleasant & Clear" to "أجواء صافية ومعتدلة"
            }

            PrayerWeatherSlot(
                nameEn = nameEn,
                nameAr = nameAr,
                timeHHMM = formatMinutesToHHMM(minutes),
                tempC = temp,
                windKmh = wind,
                statusEn = statusEn,
                statusAr = statusAr
            )
        }
    }

    /**
     * 5. Iraq Inter-Governorate Highway & Travel Corridor Safety Monitor
     */
    fun calculateIraqHighwayCorridors(
        current: CurrentWeather,
        airQuality: AirQualityData
    ): List<HighwayCorridorStatus> {
        val baseVisKm = (current.visibilityMeters / 1000.0).coerceIn(0.5, 15.0)
        val baseWind = current.windSpeedKmh
        val baseDust = airQuality.dustUgM3

        data class CorridorSeed(
            val id: String,
            val nameEn: String,
            val nameAr: String,
            val visFactor: Double,
            val windFactor: Double,
            val dustFactor: Double
        )

        val seeds = listOf(
            CorridorSeed(
                id = "hwy_baghdad_basra",
                nameEn = "Highway 1: Baghdad – Nasiriyah – Basra",
                nameAr = "الطريق الدولي السريع: بغداد – الناصرية – البصرة",
                visFactor = 0.92,
                windFactor = 1.12,
                dustFactor = 1.18
            ),
            CorridorSeed(
                id = "hwy_baghdad_karbala_najaf",
                nameEn = "Pilgrimage Route: Baghdad – Karbala – Najaf",
                nameAr = "طريق الزائرين: بغداد – كربلاء – النجف الأشرف",
                visFactor = 0.95,
                windFactor = 1.05,
                dustFactor = 1.10
            ),
            CorridorSeed(
                id = "hwy_baghdad_mosul_erbil",
                nameEn = "Northern Corridor: Baghdad – Tikrit – Mosul / Erbil",
                nameAr = "الطريق الشمالي: بغداد – تكريت – الموصل وأربيل",
                visFactor = 1.04,
                windFactor = 0.95,
                dustFactor = 0.85
            ),
            CorridorSeed(
                id = "hwy_ramadi_western",
                nameEn = "Western Desert Highway: Baghdad – Ramadi – Rutba",
                nameAr = "طريق الأنبار الدولي: بغداد – الرمادي – الرطبة",
                visFactor = 0.86,
                windFactor = 1.22,
                dustFactor = 1.32
            )
        )

        return seeds.map { s ->
            val vis = (baseVisKm * s.visFactor).coerceIn(0.8, 12.0)
            val wnd = (baseWind * s.windFactor).coerceIn(5.0, 85.0)
            val dst = (baseDust * s.dustFactor).coerceIn(10.0, 500.0)
            val score = (100 - ((10.0 - vis).coerceAtLeast(0.0) * 5.5) - (dst * 0.09) - ((wnd - 20.0).coerceAtLeast(0.0) * 0.6))
                .roundToInt().coerceIn(22, 98)

            val (stEn, stAr) = when {
                score >= 80 -> "Clear Highway — Optimal Driving Visibility" to "الطريق سالك بوضوح — رؤية ممتازة للقيادة"
                score >= 58 -> "Moderate Crosswinds / Light Dust — Drive with Care" to "رياح جانبية خفيفة أو غبار عالق — قدْ بحذر"
                else -> "Reduced Visibility & Blowing Dust — Reduce Speed" to "تدني مدى الرؤية بسبب الغبار — خفف السرعة وشغّل الأنوار"
            }

            HighwayCorridorStatus(
                id = s.id,
                nameEn = s.nameEn,
                nameAr = s.nameAr,
                visibilityKm = vis,
                windKmh = wnd,
                dustUgM3 = dst,
                safetyScore = score,
                statusEn = stEn,
                statusAr = stAr
            )
        }
    }

    fun extractHourMinute(iso: String, fallback: String): String {
        val tIndex = iso.indexOf('T')
        if (tIndex != -1 && iso.length >= tIndex + 6) {
            return iso.substring(tIndex + 1, tIndex + 6)
        }
        return fallback
    }

    private fun parseHourMinToMinutes(hhmm: String): Int {
        val parts = hhmm.split(":")
        if (parts.size == 2) {
            val h = parts[0].toIntOrNull() ?: 6
            val m = parts[1].toIntOrNull() ?: 0
            return h * 60 + m
        }
        return 360
    }

    private fun formatMinutesToHHMM(totalMin: Int): String {
        val norm = ((totalMin % 1440) + 1440) % 1440
        val h = norm / 60
        val m = norm % 60
        return String.format(Locale.US, "%02d:%02d", h, m)
    }
}
