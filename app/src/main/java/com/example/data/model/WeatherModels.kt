package com.example.data.model

enum class AppLanguage(val code: String, val labelEn: String, val labelAr: String) {
    ENGLISH("en", "English", "الإنجليزية"),
    ARABIC("ar", "Arabic (العربية - العراق)", "العربية (العراق)")
}

enum class AppThemeMode(val labelEn: String, val labelAr: String) {
    DARK("Atmospheric Dark", "الوضع الداكن"),
    LIGHT("Daylight Crisp", "الوضع النهاري"),
    AUTO("System Automatic", "تلقائي حسب النظام")
}

enum class TempUnit(val symbol: String, val labelEn: String, val labelAr: String) {
    CELSIUS("°C", "Celsius (°C)", "مئوية (°م)"),
    FAHRENHEIT("°F", "Fahrenheit (°F)", "فهرنهايت (°ف)")
}

enum class WindUnit(val symbolEn: String, val symbolAr: String, val labelEn: String, val labelAr: String) {
    KMH("km/h", "كم/س", "Kilometers per hour (km/h)", "كيلومتر في الساعة (كم/س)"),
    MS("m/s", "م/ث", "Meters per second (m/s)", "متر في الثانية (م/ث)"),
    MPH("mph", "ميل/س", "Miles per hour (mph)", "ميل في الساعة (ميل/س)")
}

enum class PressureUnit(val symbol: String, val labelEn: String, val labelAr: String) {
    HPA("hPa", "Hectopascals (hPa)", "هكتوباسكال (hPa)"),
    INHG("inHg", "Inches of Mercury (inHg)", "بوصة زئبقية (inHg)")
}

enum class PrecipUnit(val symbol: String, val labelEn: String, val labelAr: String) {
    MM("mm", "Millimeters (mm)", "ملليمتر (مم)"),
    INCH("in", "Inches (in)", "بوصة (in)")
}

enum class AqiStandard(val labelEn: String, val labelAr: String) {
    US_EPA("US EPA AQI (0–500)", "مؤشر وكالة حماية البيئة الأمريكية"),
    EUROPEAN_EAQI("European EAQI (0–100+)", "المؤشر الأوروبي لجودة الهواء")
}

data class UserPreferences(
    val language: AppLanguage = AppLanguage.ARABIC,
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val tempUnit: TempUnit = TempUnit.CELSIUS,
    val windUnit: WindUnit = WindUnit.KMH,
    val pressureUnit: PressureUnit = PressureUnit.HPA,
    val precipUnit: PrecipUnit = PrecipUnit.MM,
    val aqiStandard: AqiStandard = AqiStandard.US_EPA,
    val reducedMotion: Boolean = false,
    val batterySaverMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val updateIntervalMinutes: Int = 30,
    val selectedCityId: String = "iq_baghdad"
)

data class CityLocation(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val governorateEn: String,
    val governorateAr: String,
    val countryEn: String = "Iraq",
    val countryAr: String = "العراق",
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Int = 34,
    val timezone: String = "Asia/Baghdad",
    val isIraqiCity: Boolean = true,
    val isFavorite: Boolean = false,
    val climateNotesEn: String = "",
    val climateNotesAr: String = ""
) {
    fun displayName(lang: AppLanguage): String =
        if (lang == AppLanguage.ARABIC) nameAr else nameEn

    fun displayRegion(lang: AppLanguage): String =
        if (lang == AppLanguage.ARABIC) "$governorateAr، $countryAr" else "$governorateEn, $countryEn"
}

enum class WeatherConditionType {
    CLEAR_DAY,
    CLEAR_NIGHT,
    PARTLY_CLOUDY,
    OVERCAST,
    FOG_MIST,
    DUST_SANDSTORM,
    LIGHT_RAIN,
    HEAVY_RAIN,
    THUNDERSTORM,
    SNOW,
    EXTREME_HEAT
}

data class CurrentWeather(
    val temperatureC: Double,
    val feelsLikeC: Double,
    val heatIndexC: Double,
    val minTempC: Double,
    val maxTempC: Double,
    val weatherCode: Int,
    val conditionType: WeatherConditionType,
    val descriptionEn: String,
    val descriptionAr: String,
    val iraqiNoteEn: String,
    val iraqiNoteAr: String,
    val pressureHpa: Double,
    val surfacePressureHpa: Double,
    val humidityPercent: Int,
    val dewPointC: Double,
    val windSpeedKmh: Double,
    val windDirectionDeg: Int,
    val windDirectionCardinalEn: String,
    val windDirectionCardinalAr: String,
    val windGustsKmh: Double,
    val visibilityMeters: Double,
    val cloudCoverPercent: Int,
    val uvIndex: Double,
    val precipitationProbPercent: Int,
    val precipitationMm: Double,
    val isDay: Boolean,
    val sunriseIso: String,
    val sunsetIso: String,
    val daylightDurationHours: Double,
    val localTimeIso: String,
    val capeJkg: Double = 0.0
)

data class HourlyForecastItem(
    val timeIso: String,
    val hourLabel: String,
    val temperatureC: Double,
    val feelsLikeC: Double,
    val weatherCode: Int,
    val conditionType: WeatherConditionType,
    val precipProbPercent: Int,
    val precipMm: Double,
    val windSpeedKmh: Double,
    val windDirectionDeg: Int,
    val windGustsKmh: Double,
    val humidityPercent: Int,
    val dewPointC: Double,
    val cloudCoverPercent: Int,
    val visibilityMeters: Double,
    val pressureHpa: Double,
    val uvIndex: Double,
    val isDay: Boolean
)

data class DailyForecastItem(
    val dateIso: String,
    val dayNameEn: String,
    val dayNameAr: String,
    val weatherCode: Int,
    val conditionType: WeatherConditionType,
    val descriptionEn: String,
    val descriptionAr: String,
    val maxTempC: Double,
    val minTempC: Double,
    val feelsLikeMaxC: Double,
    val feelsLikeMinC: Double,
    val precipProbMaxPercent: Int,
    val precipSumMm: Double,
    val windSpeedMaxKmh: Double,
    val windGustsMaxKmh: Double,
    val windDominantDirDeg: Int,
    val uvIndexMax: Double,
    val sunriseIso: String,
    val sunsetIso: String,
    val daylightHours: Double,
    val isPastDay: Boolean = false,
    val tempAnomalyC: Double = 0.0,
    val confidencePercent: Int = 92
)

data class HourlyAirQualityItem(
    val timeIso: String,
    val hourLabel: String,
    val usAqi: Int,
    val europeanAqi: Int,
    val pm25: Double,
    val pm10: Double,
    val dustUgM3: Double,
    val ozoneUgM3: Double,
    val no2UgM3: Double
)

data class AirQualityData(
    val usAqi: Int,
    val europeanAqi: Int,
    val pm25UgM3: Double,
    val pm10UgM3: Double,
    val dustUgM3: Double,
    val ozoneUgM3: Double,
    val no2UgM3: Double,
    val so2UgM3: Double,
    val coUgM3: Double,
    val aerosolOpticalDepth: Double,
    val categoryEn: String,
    val categoryAr: String,
    val healthGuidanceEn: String,
    val healthGuidanceAr: String,
    val iraqiDustStatusEn: String,
    val iraqiDustStatusAr: String,
    val dataSourceNoteEn: String,
    val dataSourceNoteAr: String,
    val hourlyTrend: List<HourlyAirQualityItem>
)

enum class AlertSeverity(val level: Int, val labelEn: String, val labelAr: String) {
    ADVISORY(1, "Weather Advisory", "تنبيه جوي"),
    WATCH(2, "Weather Watch", "مراقبة جوية"),
    WARNING(3, "Severe Warning", "تحذير جوي شديد"),
    EXTREME(4, "Extreme Emergency", "حالة طوارئ قصوى")
}

enum class AlertCategory {
    EXTREME_HEAT,
    EXTREME_COLD,
    DUST_SANDSTORM,
    SHAMAL_WIND,
    HEAVY_RAIN_FLOOD,
    THUNDERSTORM_LIGHTNING,
    DENSE_FOG_VISIBILITY,
    HIGH_UV_RADIATION
}

data class WeatherAlert(
    val id: String,
    val category: AlertCategory,
    val severity: AlertSeverity,
    val titleEn: String,
    val titleAr: String,
    val areaEn: String,
    val areaAr: String,
    val summaryEn: String,
    val summaryAr: String,
    val startTimeIso: String,
    val endTimeIso: String,
    val authorityEn: String,
    val authorityAr: String,
    val precautionsEn: List<String>,
    val precautionsAr: List<String>,
    val metricTriggerText: String,
    val isOfficialFeed: Boolean = false
)

data class ComfortScoreBreakdown(
    val overallScore: Int, // 0..100
    val thermalScore: Int, // 0..100
    val humidityScore: Int,
    val windScore: Int,
    val airAndDustScore: Int,
    val uvScore: Int,
    val ratingEn: String,
    val ratingAr: String,
    val explanationEn: String,
    val explanationAr: String
)

data class GoldenHourInfo(
    val morningBlueHour: String,
    val morningGoldenHour: String,
    val sunriseTime: String,
    val solarNoonTime: String,
    val eveningGoldenHourStart: String,
    val sunsetTime: String,
    val eveningBlueHourEnd: String,
    val photographyScore: Int,
    val cloudDramaEn: String,
    val cloudDramaAr: String,
    val atmosphericClarityEn: String,
    val atmosphericClarityAr: String,
    val tripodWindAdviceEn: String,
    val tripodWindAdviceAr: String
)

data class AstronomyInfo(
    val moonPhaseNameEn: String,
    val moonPhaseNameAr: String,
    val illuminationPercent: Int,
    val moonAgeDays: Double,
    val moonPhaseFraction: Double, // 0.0 (New) -> 0.5 (Full) -> 1.0
    val moonriseEst: String,
    val moonsetEst: String,
    val stargazingScore: Int,
    val stargazingConditionEn: String,
    val stargazingConditionAr: String
)

data class RadarFrame(
    val timestampEpochSec: Long,
    val timeLabel: String,
    val path: String,
    val isForecastSimulation: Boolean
)

enum class RadarLayerType(
    val labelEn: String,
    val labelAr: String,
    val unitSymbol: String,
    val isObservedRadar: Boolean
) {
    PRECIPITATION_RADAR("Precipitation Radar", "رادار الأمطار", "dBZ / mm/h", true),
    DUST_SANDSTORM("Dust & Haboob Plume", "موجات الغبار والطوز", "µg/m³ PM10", false),
    TEMPERATURE_MAP("Temperature Field", "خريطة الحرارة", "°C", false),
    WIND_STREAMLINES("Shamal & Wind Vectors", "تيارات الرياح والشمال", "km/h", false),
    PRESSURE_ISOBARS("Atmospheric Pressure", "الضغط الجوي (Isobars)", "hPa", false),
    CLOUD_SATELLITE("Cloud & IR Satellite", "الأقمار الصناعية والسحب", "%", true),
    ALERT_ZONES("Severe Alert Zones", "مناطق التحذير الجوي", "Risk", false)
}

enum class DataFreshnessSource {
    LIVE_API,
    OFFLINE_ROOM_CACHE,
    CLIMATOLOGICAL_FALLBACK
}

data class SolarEnergyInsight(
    val panelEfficiencyPercent: Int,
    val peakSunHours: Double,
    val dustSoilingLossPercent: Int,
    val heatDeratingPercent: Int,
    val estimatedKwhPer5KwSystem: Double,
    val cleaningAdviceEn: String,
    val cleaningAdviceAr: String,
    val inverterNoteEn: String,
    val inverterNoteAr: String
)

data class AgricultureInsight(
    val evapotranspirationMmDay: Double,
    val irrigationNeedEn: String,
    val irrigationNeedAr: String,
    val datePalmStatusEn: String,
    val datePalmStatusAr: String,
    val sprayingWindowEn: String,
    val sprayingWindowAr: String,
    val soilStressScore: Int // 0..100
)

data class HealthBiometeorologyInsight(
    val dailyWaterLiters: Double,
    val dailyWaterCups: Int,
    val respiratoryDustRiskEn: String,
    val respiratoryDustRiskAr: String,
    val respiratoryRiskLevel: Int, // 1..4
    val migrainePressureRiskEn: String,
    val migrainePressureRiskAr: String,
    val migraineRiskLevel: Int, // 1..4
    val heatStrokeRiskEn: String,
    val heatStrokeRiskAr: String,
    val sinusAndJointNoteEn: String,
    val sinusAndJointNoteAr: String
)

data class PrayerWeatherSlot(
    val nameEn: String,
    val nameAr: String,
    val timeHHMM: String,
    val tempC: Double,
    val windKmh: Double,
    val statusEn: String,
    val statusAr: String
)

data class HighwayCorridorStatus(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val visibilityKm: Double,
    val windKmh: Double,
    val dustUgM3: Double,
    val safetyScore: Int, // 0..100
    val statusEn: String,
    val statusAr: String
)

data class WeatherBundle(
    val city: CityLocation,
    val current: CurrentWeather,
    val hourly: List<HourlyForecastItem>,
    val daily: List<DailyForecastItem>,
    val pastDays: List<DailyForecastItem>,
    val airQuality: AirQualityData,
    val alerts: List<WeatherAlert>,
    val comfort: ComfortScoreBreakdown,
    val goldenHour: GoldenHourInfo,
    val astronomy: AstronomyInfo,
    val solarInsight: SolarEnergyInsight,
    val agricultureInsight: AgricultureInsight,
    val healthInsight: HealthBiometeorologyInsight,
    val prayerSchedule: List<PrayerWeatherSlot>,
    val highwayCorridors: List<HighwayCorridorStatus>,
    val radarFrames: List<RadarFrame>,
    val radarHost: String = "https://tilecache.rainviewer.com",
    val fetchedAtEpochMillis: Long,
    val source: DataFreshnessSource
)
