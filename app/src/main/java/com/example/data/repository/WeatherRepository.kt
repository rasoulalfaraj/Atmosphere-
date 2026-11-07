package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.CachedWeatherEntity
import com.example.data.local.SavedCityEntity
import com.example.data.local.WeatherDao
import com.example.data.model.AirQualityData
import com.example.data.model.CityLocation
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyForecastItem
import com.example.data.model.DataFreshnessSource
import com.example.data.model.HourlyAirQualityItem
import com.example.data.model.HourlyForecastItem
import com.example.data.model.IraqiCitiesCatalog
import com.example.data.model.RadarFrame
import com.example.data.model.WeatherBundle
import com.example.data.remote.OpenMeteoAirQualityApi
import com.example.data.remote.OpenMeteoAirQualityResponse
import com.example.data.remote.OpenMeteoForecastApi
import com.example.data.remote.OpenMeteoForecastResponse
import com.example.data.remote.OpenMeteoGeocodingApi
import com.example.data.remote.RainViewerMapsResponse
import com.example.data.remote.RainViewerRadarApi
import com.example.util.MeteorologyCalculator
import com.example.widget.AtmosphereWeatherWidgetReceiver
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val TAG = "WeatherRepository"

interface WeatherProvider {
    suspend fun fetchWeatherForCity(
        city: CityLocation,
        forceRefresh: Boolean = false
    ): WeatherBundle

    suspend fun searchCities(query: String, languageCode: String): List<CityLocation>
}

class WeatherRepository(
    private val context: Context,
    private val weatherDao: WeatherDao
) : WeatherProvider {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val forecastApi: OpenMeteoForecastApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenMeteoForecastApi::class.java)
    }

    private val airQualityApi: OpenMeteoAirQualityApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://air-quality-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenMeteoAirQualityApi::class.java)
    }

    private val geocodingApi: OpenMeteoGeocodingApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenMeteoGeocodingApi::class.java)
    }

    private val radarApi: RainViewerRadarApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.rainviewer.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RainViewerRadarApi::class.java)
    }

    private val forecastAdapter = moshi.adapter(OpenMeteoForecastResponse::class.java)
    private val airQualityAdapter = moshi.adapter(OpenMeteoAirQualityResponse::class.java)
    private val radarAdapter = moshi.adapter(RainViewerMapsResponse::class.java)

    val savedCitiesFlow: Flow<List<CityLocation>> = weatherDao.observeSavedCities().map { entities ->
        if (entities.isEmpty()) {
            IraqiCitiesCatalog.iraqiCities.take(8)
        } else {
            entities.map { it.toDomain() }
        }
    }

    suspend fun ensureDefaultCitiesSeeded() = withContext(Dispatchers.IO) {
        val current = weatherDao.getSavedCitiesOnce()
        if (current.isEmpty()) {
            val defaults = IraqiCitiesCatalog.iraqiCities.take(8).mapIndexed { index, city ->
                city.toEntity(lastViewed = System.currentTimeMillis() - index * 1000L)
            }
            weatherDao.insertDefaultCitiesIfEmpty(defaults)
        }
    }

    suspend fun saveOrUpdateCity(city: CityLocation, isFavorite: Boolean = city.isFavorite) =
        withContext(Dispatchers.IO) {
            weatherDao.upsertSavedCity(
                city.copy(isFavorite = isFavorite).toEntity(System.currentTimeMillis())
            )
        }

    suspend fun removeSavedCity(cityId: String) = withContext(Dispatchers.IO) {
        weatherDao.deleteSavedCity(cityId)
    }

    suspend fun clearOfflineCache() = withContext(Dispatchers.IO) {
        weatherDao.clearAllCache()
    }

    override suspend fun fetchWeatherForCity(
        city: CityLocation,
        forceRefresh: Boolean
    ): WeatherBundle = withContext(Dispatchers.IO) {
        val cached = weatherDao.getCachedWeatherOnce(city.id)
        val now = System.currentTimeMillis()
        // If cache is younger than 10 minutes and not forceRefresh, use cached network response
        if (!forceRefresh && cached != null && (now - cached.cachedAtEpochMillis) < 10 * 60 * 1000L) {
            runCatching {
                val fDto = forecastAdapter.fromJson(cached.forecastJson)
                val aqDto = airQualityAdapter.fromJson(cached.airQualityJson)
                val rDto = radarAdapter.fromJson(cached.radarJson)
                if (fDto != null && aqDto != null) {
                    return@withContext mapDtosToBundle(
                        city = city,
                        forecastDto = fDto,
                        airQualityDto = aqDto,
                        radarDto = rDto,
                        timestampMillis = cached.cachedAtEpochMillis,
                        source = DataFreshnessSource.LIVE_API
                    )
                }
            }
        }

        // Try live network fetch from Open-Meteo + RainViewer
        try {
            coroutineScope {
                val forecastDef = async {
                    forecastApi.getForecast(
                        latitude = city.latitude,
                        longitude = city.longitude
                    )
                }
                val airQualityDef = async {
                    runCatching {
                        airQualityApi.getAirQuality(
                            latitude = city.latitude,
                            longitude = city.longitude
                        )
                    }.getOrNull()
                }
                val radarDef = async {
                    runCatching { radarApi.getRadarFrames() }.getOrNull()
                }

                val forecastDto = forecastDef.await()
                val airQualityDto = airQualityDef.await() ?: OpenMeteoAirQualityResponse()
                val radarDto = radarDef.await()

                // Save raw JSON in Room for offline mode
                weatherDao.insertCachedWeather(
                    CachedWeatherEntity(
                        cityId = city.id,
                        forecastJson = forecastAdapter.toJson(forecastDto),
                        airQualityJson = airQualityAdapter.toJson(airQualityDto),
                        radarJson = if (radarDto != null) radarAdapter.toJson(radarDto) else "{}",
                        cachedAtEpochMillis = now
                    )
                )

                val bundle = mapDtosToBundle(
                    city = city,
                    forecastDto = forecastDto,
                    airQualityDto = airQualityDto,
                    radarDto = radarDto,
                    timestampMillis = now,
                    source = DataFreshnessSource.LIVE_API
                )
                AtmosphereWeatherWidgetReceiver.pushWidgetUpdate(context, bundle)
                bundle
            }
        } catch (e: Exception) {
            Log.w(TAG, "Live weather fetch failed for ${city.nameEn}; checking Room cache", e)
            if (cached != null) {
                runCatching {
                    val fDto = forecastAdapter.fromJson(cached.forecastJson)
                    val aqDto = airQualityAdapter.fromJson(cached.airQualityJson)
                    val rDto = radarAdapter.fromJson(cached.radarJson)
                    if (fDto != null && aqDto != null) {
                        return@withContext mapDtosToBundle(
                            city = city,
                            forecastDto = fDto,
                            airQualityDto = aqDto,
                            radarDto = rDto,
                            timestampMillis = cached.cachedAtEpochMillis,
                            source = DataFreshnessSource.OFFLINE_ROOM_CACHE
                        )
                    }
                }
            }
            // Fallback to regional climatological model when completely offline without prior cache
            buildClimatologicalFallbackBundle(city, now)
        }
    }

    override suspend fun searchCities(query: String, languageCode: String): List<CityLocation> =
        withContext(Dispatchers.IO) {
            val trimmed = query.trim()
            if (trimmed.isEmpty()) return@withContext IraqiCitiesCatalog.allCatalogCities

            val localMatches = IraqiCitiesCatalog.allCatalogCities.filter {
                it.nameEn.contains(trimmed, ignoreCase = true) ||
                    it.nameAr.contains(trimmed, ignoreCase = true) ||
                    it.governorateEn.contains(trimmed, ignoreCase = true) ||
                    it.governorateAr.contains(trimmed, ignoreCase = true)
            }

            val remoteMatches = runCatching {
                val resp = geocodingApi.searchCities(query = trimmed, count = 10, language = languageCode)
                resp.results.orEmpty().map { dto ->
                    val isIraq = dto.countryCode.equals("IQ", ignoreCase = true) ||
                        dto.country?.contains("Iraq", ignoreCase = true) == true ||
                        dto.country?.contains("العراق") == true
                    CityLocation(
                        id = "geo_${dto.id}",
                        nameEn = dto.name,
                        nameAr = dto.name,
                        governorateEn = dto.admin1 ?: (dto.country ?: "Region"),
                        governorateAr = dto.admin1 ?: (dto.country ?: "منطقة"),
                        countryEn = dto.country ?: "",
                        countryAr = dto.country ?: "",
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        elevationMeters = dto.elevation?.roundToInt() ?: 40,
                        timezone = dto.timezone ?: "Asia/Baghdad",
                        isIraqiCity = isIraq
                    )
                }
            }.getOrDefault(emptyList())

            (localMatches + remoteMatches).distinctBy { "${it.nameEn.lowercase()}_${it.latitude.roundToInt()}" }
        }

    private fun mapDtosToBundle(
        city: CityLocation,
        forecastDto: OpenMeteoForecastResponse,
        airQualityDto: OpenMeteoAirQualityResponse,
        radarDto: RainViewerMapsResponse?,
        timestampMillis: Long,
        source: DataFreshnessSource
    ): WeatherBundle {
        val currentDto = forecastDto.current
        val hourlyDto = forecastDto.hourly
        val dailyDto = forecastDto.daily

        val aqCurrent = airQualityDto.current
        val dustNow = aqCurrent?.dust ?: 35.0
        val pm10Now = aqCurrent?.pm10 ?: 52.0
        val pm25Now = aqCurrent?.pm25 ?: 18.0
        val usAqiNow = aqCurrent?.usAqi ?: 58
        val euAqiNow = aqCurrent?.europeanAqi ?: 42

        val tempNow = currentDto?.temperature2m ?: 28.0
        val rhNow = currentDto?.relativeHumidity2m ?: 32
        val windNow = currentDto?.windSpeed10m ?: 16.0
        val windDirNow = currentDto?.windDirection10m ?: 315
        val windGustsNow = currentDto?.windGusts10m ?: (windNow * 1.35)
        val isDayNow = (currentDto?.isDay ?: 1) == 1
        val wmoNow = currentDto?.weatherCode ?: 0

        val heatIndexNow = MeteorologyCalculator.calculateHeatIndexC(tempNow, rhNow, windNow)
        val feelsLikeNow = currentDto?.apparentTemperature ?: heatIndexNow
        val dewPointNow = MeteorologyCalculator.calculateDewPointC(tempNow, rhNow)

        // Parse Daily Forecast (past_days = 3 + forecast_days = 14)
        val allDailyItems = mutableListOf<DailyForecastItem>()
        if (dailyDto != null && dailyDto.time.isNotEmpty()) {
            val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val avgMaxTemp = dailyDto.temperature2mMax.filterNotNull().average().takeIf { !it.isNaN() } ?: tempNow

            for (i in dailyDto.time.indices) {
                val dateStr = dailyDto.time[i]
                val maxT = dailyDto.temperature2mMax.getOrNull(i) ?: (tempNow + 5.0)
                val minT = dailyDto.temperature2mMin.getOrNull(i) ?: (tempNow - 7.0)
                val flMax = dailyDto.apparentTemperatureMax.getOrNull(i) ?: maxT
                val flMin = dailyDto.apparentTemperatureMin.getOrNull(i) ?: minT
                val code = dailyDto.weatherCode.getOrNull(i) ?: 0
                val pProb = dailyDto.precipitationProbabilityMax.getOrNull(i) ?: 0
                val pSum = dailyDto.precipitationSum.getOrNull(i) ?: 0.0
                val wMax = dailyDto.windSpeed10mMax.getOrNull(i) ?: 18.0
                val gMax = dailyDto.windGusts10mMax.getOrNull(i) ?: (wMax * 1.3)
                val wDir = dailyDto.windDirection10mDominant.getOrNull(i) ?: 310
                val uvMax = dailyDto.uvIndexMax.getOrNull(i) ?: 7.5
                val sr = dailyDto.sunrise.getOrNull(i) ?: "${dateStr}T06:15"
                val ss = dailyDto.sunset.getOrNull(i) ?: "${dateStr}T17:15"
                val daylightSec = dailyDto.daylightDuration.getOrNull(i) ?: 39600.0

                val cond = MeteorologyCalculator.mapWmoWeatherCode(
                    code = code,
                    isDay = true,
                    tempC = maxT,
                    dustUgM3 = if (i <= 4) dustNow * 0.8 else 25.0,
                    pm10UgM3 = if (i <= 4) pm10Now * 0.8 else 40.0,
                    windSpeedKmh = wMax
                )
                val (dayEn, dayAr) = formatDayOfWeek(dateStr, todayIso)
                val isPast = dateStr < todayIso
                val anomaly = maxT - avgMaxTemp
                val confidence = (98 - (i - 3).coerceAtLeast(0) * 3).coerceIn(58, 98)

                allDailyItems.add(
                    DailyForecastItem(
                        dateIso = dateStr,
                        dayNameEn = dayEn,
                        dayNameAr = dayAr,
                        weatherCode = code,
                        conditionType = cond.type,
                        descriptionEn = cond.descEn,
                        descriptionAr = cond.descAr,
                        maxTempC = maxT,
                        minTempC = minT,
                        feelsLikeMaxC = flMax,
                        feelsLikeMinC = flMin,
                        precipProbMaxPercent = pProb,
                        precipSumMm = pSum,
                        windSpeedMaxKmh = wMax,
                        windGustsMaxKmh = gMax,
                        windDominantDirDeg = wDir,
                        uvIndexMax = uvMax,
                        sunriseIso = sr,
                        sunsetIso = ss,
                        daylightHours = daylightSec / 3600.0,
                        isPastDay = isPast,
                        tempAnomalyC = anomaly,
                        confidencePercent = confidence
                    )
                )
            }
        }

        val pastDays = allDailyItems.filter { it.isPastDay }
        val upcomingDaily = allDailyItems.filter { !it.isPastDay }.ifEmpty { allDailyItems }
        val todayItem = upcomingDaily.firstOrNull()

        // Parse Hourly Forecast (72 hours from current hour)
        val hourlyItems = mutableListOf<HourlyForecastItem>()
        if (hourlyDto != null && hourlyDto.time.isNotEmpty()) {
            val currentTimePrefix = currentDto?.time?.take(13)
            val startIndex = hourlyDto.time.indexOfFirst {
                currentTimePrefix != null && it.startsWith(currentTimePrefix)
            }.takeIf { it >= 0 } ?: (pastDays.size * 24).coerceAtMost(hourlyDto.time.size - 1)

            val endIndex = (startIndex + 72).coerceAtMost(hourlyDto.time.size)
            for (i in startIndex until endIndex) {
                val tIso = hourlyDto.time[i]
                val hTemp = hourlyDto.temperature2m.getOrNull(i) ?: tempNow
                val hFl = hourlyDto.apparentTemperature.getOrNull(i) ?: hTemp
                val hCode = hourlyDto.weatherCode.getOrNull(i) ?: wmoNow
                val hProb = hourlyDto.precipitationProbability.getOrNull(i) ?: 0
                val hPrecip = hourlyDto.precipitation.getOrNull(i) ?: 0.0
                val hWind = hourlyDto.windSpeed10m.getOrNull(i) ?: windNow
                val hDir = hourlyDto.windDirection10m.getOrNull(i) ?: windDirNow
                val hGust = hourlyDto.windGusts10m.getOrNull(i) ?: (hWind * 1.3)
                val hHum = hourlyDto.relativeHumidity2m.getOrNull(i) ?: rhNow
                val hDew = hourlyDto.dewPoint2m.getOrNull(i) ?: MeteorologyCalculator.calculateDewPointC(hTemp, hHum)
                val hCloud = hourlyDto.cloudCover.getOrNull(i) ?: 15
                val hVis = hourlyDto.visibility.getOrNull(i) ?: 10000.0
                val hPres = hourlyDto.pressureMsl.getOrNull(i) ?: 1013.0
                val hUv = hourlyDto.uvIndex.getOrNull(i) ?: 0.0
                val hIsDay = (hourlyDto.isDay.getOrNull(i) ?: 1) == 1

                val cond = MeteorologyCalculator.mapWmoWeatherCode(
                    code = hCode,
                    isDay = hIsDay,
                    tempC = hTemp,
                    dustUgM3 = dustNow,
                    pm10UgM3 = pm10Now,
                    windSpeedKmh = hWind
                )

                hourlyItems.add(
                    HourlyForecastItem(
                        timeIso = tIso,
                        hourLabel = MeteorologyCalculator.extractHourMinute(tIso, "12:00"),
                        temperatureC = hTemp,
                        feelsLikeC = hFl,
                        weatherCode = hCode,
                        conditionType = cond.type,
                        precipProbPercent = hProb,
                        precipMm = hPrecip,
                        windSpeedKmh = hWind,
                        windDirectionDeg = hDir,
                        windGustsKmh = hGust,
                        humidityPercent = hHum,
                        dewPointC = hDew,
                        cloudCoverPercent = hCloud,
                        visibilityMeters = hVis,
                        pressureHpa = hPres,
                        uvIndex = hUv,
                        isDay = hIsDay
                    )
                )
            }
        }

        val firstHour = hourlyItems.firstOrNull()
        val (cardinalEn, cardinalAr) = MeteorologyCalculator.windCardinal(windDirNow)
        val currentCond = MeteorologyCalculator.mapWmoWeatherCode(
            code = wmoNow,
            isDay = isDayNow,
            tempC = tempNow,
            dustUgM3 = dustNow,
            pm10UgM3 = pm10Now,
            windSpeedKmh = windNow
        )

        val currentWeather = CurrentWeather(
            temperatureC = tempNow,
            feelsLikeC = feelsLikeNow,
            heatIndexC = heatIndexNow,
            minTempC = todayItem?.minTempC ?: (tempNow - 6.0),
            maxTempC = todayItem?.maxTempC ?: (tempNow + 5.0),
            weatherCode = wmoNow,
            conditionType = currentCond.type,
            descriptionEn = currentCond.descEn,
            descriptionAr = currentCond.descAr,
            iraqiNoteEn = currentCond.iraqiNoteEn,
            iraqiNoteAr = currentCond.iraqiNoteAr,
            pressureHpa = currentDto?.pressureMsl ?: (firstHour?.pressureHpa ?: 1012.0),
            surfacePressureHpa = currentDto?.surfacePressure ?: 1008.0,
            humidityPercent = rhNow,
            dewPointC = firstHour?.dewPointC ?: dewPointNow,
            windSpeedKmh = windNow,
            windDirectionDeg = windDirNow,
            windDirectionCardinalEn = cardinalEn,
            windDirectionCardinalAr = cardinalAr,
            windGustsKmh = windGustsNow,
            visibilityMeters = firstHour?.visibilityMeters ?: 10000.0,
            cloudCoverPercent = currentDto?.cloudCover ?: (firstHour?.cloudCoverPercent ?: 10),
            uvIndex = aqCurrent?.uvIndex ?: (firstHour?.uvIndex ?: 5.0),
            precipitationProbPercent = firstHour?.precipProbPercent ?: (todayItem?.precipProbMaxPercent ?: 0),
            precipitationMm = currentDto?.precipitation ?: 0.0,
            isDay = isDayNow,
            sunriseIso = todayItem?.sunriseIso ?: "2026-11-06T06:22",
            sunsetIso = todayItem?.sunsetIso ?: "2026-11-06T17:04",
            daylightDurationHours = todayItem?.daylightHours ?: 10.7,
            localTimeIso = currentDto?.time ?: SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).format(Date())
        )

        // Parse Hourly Air Quality
        val aqHourlyList = mutableListOf<HourlyAirQualityItem>()
        val aqHourlyDto = airQualityDto.hourly
        if (aqHourlyDto != null && aqHourlyDto.time.isNotEmpty()) {
            val count = aqHourlyDto.time.size.coerceAtMost(48)
            for (i in 0 until count) {
                val tIso = aqHourlyDto.time[i]
                aqHourlyList.add(
                    HourlyAirQualityItem(
                        timeIso = tIso,
                        hourLabel = MeteorologyCalculator.extractHourMinute(tIso, "12:00"),
                        usAqi = aqHourlyDto.usAqi.getOrNull(i) ?: usAqiNow,
                        europeanAqi = aqHourlyDto.europeanAqi.getOrNull(i) ?: euAqiNow,
                        pm25 = aqHourlyDto.pm25.getOrNull(i) ?: pm25Now,
                        pm10 = aqHourlyDto.pm10.getOrNull(i) ?: pm10Now,
                        dustUgM3 = aqHourlyDto.dust.getOrNull(i) ?: dustNow,
                        ozoneUgM3 = aqHourlyDto.ozone.getOrNull(i) ?: 68.0,
                        no2UgM3 = aqHourlyDto.nitrogenDioxide.getOrNull(i) ?: 18.0
                    )
                )
            }
        }

        val (aqiCatEn, aqiCatAr, guidanceEn, guidanceAr) = classifyAqi(usAqiNow, dustNow)
        val (dustStatusEn, dustStatusAr) = when {
            dustNow >= 180.0 -> "Active Sand/Dust Storm (Tooz) — High Particulate Load" to "عاصفة ترابية نشطة (طوز) — تركيز عالٍ للغبار"
            dustNow >= 75.0 -> "Moderate Suspended Dust Haze" to "غبار عالق متوسط الكثافة"
            else -> "Low Background Desert Dust (${dustNow.roundToInt()} µg/m³)" to "مستوى غبار طبيعي منخفض (${dustNow.roundToInt()} مايكروغرام/م³)"
        }

        val airQualityData = AirQualityData(
            usAqi = usAqiNow,
            europeanAqi = euAqiNow,
            pm25UgM3 = pm25Now,
            pm10UgM3 = pm10Now,
            dustUgM3 = dustNow,
            ozoneUgM3 = aqCurrent?.ozone ?: 64.0,
            no2UgM3 = aqCurrent?.nitrogenDioxide ?: 19.0,
            so2UgM3 = aqCurrent?.sulphurDioxide ?: 8.5,
            coUgM3 = aqCurrent?.carbonMonoxide ?: 290.0,
            aerosolOpticalDepth = aqCurrent?.aerosolOpticalDepth ?: 0.28,
            categoryEn = aqiCatEn,
            categoryAr = aqiCatAr,
            healthGuidanceEn = guidanceEn,
            healthGuidanceAr = guidanceAr,
            iraqiDustStatusEn = dustStatusEn,
            iraqiDustStatusAr = dustStatusAr,
            dataSourceNoteEn = "CAMS / Open-Meteo Atmospheric Composition & Mineral Dust Assimilation Model (US EPA & EAQI Standards)",
            dataSourceNoteAr = "نموذج CAMS / Open-Meteo للغلاف الجوي والغبار المعدني (معيار وكالة حماية البيئة الأمريكية والأوروبية)",
            hourlyTrend = aqHourlyList
        )

        val comfort = MeteorologyCalculator.calculateComfortScore(currentWeather, airQualityData)
        val goldenHour = MeteorologyCalculator.calculateGoldenHour(
            sunriseIso = currentWeather.sunriseIso,
            sunsetIso = currentWeather.sunsetIso,
            cloudCover = currentWeather.cloudCoverPercent,
            visibilityMeters = currentWeather.visibilityMeters,
            windSpeedKmh = currentWeather.windSpeedKmh,
            dustUgM3 = airQualityData.dustUgM3
        )
        val astronomy = MeteorologyCalculator.calculateAstronomy(
            timestampMillis = timestampMillis,
            cloudCover = currentWeather.cloudCoverPercent,
            humidity = currentWeather.humidityPercent,
            dustUgM3 = airQualityData.dustUgM3
        )
        val alerts = MeteorologyCalculator.evaluateWeatherAlerts(
            city = city,
            current = currentWeather,
            hourly = hourlyItems,
            daily = upcomingDaily,
            airQuality = airQualityData
        )
        val solarInsight = MeteorologyCalculator.calculateSolarEnergyInsight(
            current = currentWeather,
            airQuality = airQualityData,
            daily = upcomingDaily
        )
        val agricultureInsight = MeteorologyCalculator.calculateAgricultureInsight(
            current = currentWeather,
            airQuality = airQualityData
        )
        val healthInsight = MeteorologyCalculator.calculateHealthBiometeorology(
            current = currentWeather,
            hourly = hourlyItems,
            airQuality = airQualityData
        )
        val prayerSchedule = MeteorologyCalculator.calculatePrayerWeatherSchedule(
            current = currentWeather,
            hourly = hourlyItems
        )
        val highwayCorridors = MeteorologyCalculator.calculateIraqHighwayCorridors(
            current = currentWeather,
            airQuality = airQualityData
        )

        val radarFrames = buildRadarFrames(radarDto, timestampMillis)

        return WeatherBundle(
            city = city,
            current = currentWeather,
            hourly = hourlyItems,
            daily = upcomingDaily,
            pastDays = pastDays,
            airQuality = airQualityData,
            alerts = alerts,
            comfort = comfort,
            goldenHour = goldenHour,
            astronomy = astronomy,
            solarInsight = solarInsight,
            agricultureInsight = agricultureInsight,
            healthInsight = healthInsight,
            prayerSchedule = prayerSchedule,
            highwayCorridors = highwayCorridors,
            radarFrames = radarFrames,
            radarHost = radarDto?.host ?: "https://tilecache.rainviewer.com",
            fetchedAtEpochMillis = timestampMillis,
            source = source
        )
    }

    private fun buildRadarFrames(
        radarDto: RainViewerMapsResponse?,
        nowMillis: Long
    ): List<RadarFrame> {
        val frames = mutableListOf<RadarFrame>()
        val fmt = SimpleDateFormat("HH:mm", Locale.US)

        val pastList = radarDto?.radar?.past.orEmpty()
        val nowcastList = radarDto?.radar?.nowcast.orEmpty()

        if (pastList.isNotEmpty() || nowcastList.isNotEmpty()) {
            pastList.takeLast(6).forEach { f ->
                frames.add(
                    RadarFrame(
                        timestampEpochSec = f.time,
                        timeLabel = fmt.format(Date(f.time * 1000L)),
                        path = f.path,
                        isForecastSimulation = false
                    )
                )
            }
            nowcastList.take(4).forEach { f ->
                frames.add(
                    RadarFrame(
                        timestampEpochSec = f.time,
                        timeLabel = "${fmt.format(Date(f.time * 1000L))} (Fcst)",
                        path = f.path,
                        isForecastSimulation = true
                    )
                )
            }
        } else {
            // Generate timeline steps (6 observed past + 4 forecast simulation steps)
            val baseSec = (nowMillis / 1000L / 900L) * 900L
            for (step in -5..4) {
                val tSec = baseSec + step * 900L
                val isFcst = step > 0
                frames.add(
                    RadarFrame(
                        timestampEpochSec = tSec,
                        timeLabel = fmt.format(Date(tSec * 1000L)) + if (isFcst) " (+${step * 15}m)" else "",
                        path = "",
                        isForecastSimulation = isFcst
                    )
                )
            }
        }
        return frames
    }

    private fun buildClimatologicalFallbackBundle(
        city: CityLocation,
        nowMillis: Long
    ): WeatherBundle {
        // Latitude-adjusted realistic telemetry when device is strictly offline on first launch
        val latFactor = (34.0 - city.latitude) * 1.4
        val baseTemp = (27.5 + latFactor - (city.elevationMeters * 0.0065)).coerceIn(12.0, 44.0)
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)

        val hourlyTimes = mutableListOf<String>()
        val hourlyTemps = mutableListOf<Double?>()
        val hourlyFl = mutableListOf<Double?>()
        val hourlyHum = mutableListOf<Int?>()
        val hourlyDew = mutableListOf<Double?>()
        val hourlyProb = mutableListOf<Int?>()
        val hourlyPrecip = mutableListOf<Double?>()
        val hourlyCode = mutableListOf<Int?>()
        val hourlyPres = mutableListOf<Double?>()
        val hourlyCloud = mutableListOf<Int?>()
        val hourlyVis = mutableListOf<Double?>()
        val hourlyWind = mutableListOf<Double?>()
        val hourlyDir = mutableListOf<Int?>()
        val hourlyGust = mutableListOf<Double?>()
        val hourlyUv = mutableListOf<Double?>()
        val hourlyDay = mutableListOf<Int?>()

        val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:00", Locale.US)
        for (h in 0 until 72) {
            val hCal = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, h) }
            val hr = hCal.get(Calendar.HOUR_OF_DAY)
            val diurnal = -cos(((hr - 3) / 24.0) * 2.0 * Math.PI) * 6.2
            val t = baseTemp + diurnal
            val isDay = if (hr in 6..17) 1 else 0
            hourlyTimes.add(isoFmt.format(hCal.time))
            hourlyTemps.add(t)
            hourlyFl.add(t - 0.8)
            hourlyHum.add((36 - diurnal * 1.8).roundToInt().coerceIn(14, 78))
            hourlyDew.add(8.5)
            hourlyProb.add(if (city.latitude > 35.0 && h in 18..30) 35 else 5)
            hourlyPrecip.add(0.0)
            hourlyCode.add(if (hr in 12..16) 1 else 0)
            hourlyPres.add(1014.0 + sin(h / 6.0) * 2.5)
            hourlyCloud.add(if (city.latitude > 35.0) 32 else 14)
            hourlyVis.add(9500.0)
            hourlyWind.add(15.0 + (if (hr in 11..17) 7.0 else 0.0))
            hourlyDir.add(315)
            hourlyGust.add(24.0 + (if (hr in 11..17) 9.0 else 0.0))
            hourlyUv.add(if (isDay == 1) (sin(((hr - 6) / 12.0) * Math.PI) * 7.8).coerceAtLeast(0.5) else 0.0)
            hourlyDay.add(isDay)
        }

        val dailyTimes = mutableListOf<String>()
        val dailyCode = mutableListOf<Int?>()
        val dailyMax = mutableListOf<Double?>()
        val dailyMin = mutableListOf<Double?>()
        val dailyFlMax = mutableListOf<Double?>()
        val dailyFlMin = mutableListOf<Double?>()
        val dailySr = mutableListOf<String?>()
        val dailySs = mutableListOf<String?>()
        val dailyDaylight = mutableListOf<Double?>()
        val dailyUv = mutableListOf<Double?>()
        val dailyPrecip = mutableListOf<Double?>()
        val dailyProb = mutableListOf<Int?>()
        val dailyWind = mutableListOf<Double?>()
        val dailyGust = mutableListOf<Double?>()
        val dailyDir = mutableListOf<Int?>()

        val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        for (d in -3..13) {
            val dCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, d) }
            val dStr = dayFmt.format(dCal.time)
            val wave = sin(d * 0.55) * 2.8
            dailyTimes.add(dStr)
            dailyCode.add(if (d % 5 == 0) 2 else 0)
            dailyMax.add(baseTemp + 6.0 + wave)
            dailyMin.add(baseTemp - 6.2 + wave * 0.7)
            dailyFlMax.add(baseTemp + 5.4 + wave)
            dailyFlMin.add(baseTemp - 6.8 + wave * 0.7)
            dailySr.add("${dStr}T06:21")
            dailySs.add("${dStr}T17:05")
            dailyDaylight.add(38640.0)
            dailyUv.add(7.4)
            dailyPrecip.add(if (d == 4 && city.latitude > 34.5) 3.2 else 0.0)
            dailyProb.add(if (d == 4 && city.latitude > 34.5) 45 else 10)
            dailyWind.add(19.0 + (d % 3) * 3.0)
            dailyGust.add(29.0 + (d % 3) * 4.5)
            dailyDir.add(315)
        }

        val fallbackForecast = OpenMeteoForecastResponse(
            latitude = city.latitude,
            longitude = city.longitude,
            timezone = city.timezone,
            current = com.example.data.remote.CurrentDto(
                time = isoFmt.format(Date(nowMillis)),
                temperature2m = hourlyTemps.first() ?: baseTemp,
                relativeHumidity2m = hourlyHum.first() ?: 30,
                apparentTemperature = hourlyFl.first() ?: baseTemp,
                isDay = if (currentHour in 6..17) 1 else 0,
                precipitation = 0.0,
                weatherCode = 0,
                cloudCover = 14,
                pressureMsl = 1014.0,
                surfacePressure = 1009.0,
                windSpeed10m = 18.0,
                windDirection10m = 315,
                windGusts10m = 27.0
            ),
            hourly = com.example.data.remote.HourlyDto(
                time = hourlyTimes,
                temperature2m = hourlyTemps,
                relativeHumidity2m = hourlyHum,
                dewPoint2m = hourlyDew,
                apparentTemperature = hourlyFl,
                precipitationProbability = hourlyProb,
                precipitation = hourlyPrecip,
                weatherCode = hourlyCode,
                pressureMsl = hourlyPres,
                cloudCover = hourlyCloud,
                visibility = hourlyVis,
                windSpeed10m = hourlyWind,
                windDirection10m = hourlyDir,
                windGusts10m = hourlyGust,
                uvIndex = hourlyUv,
                isDay = hourlyDay
            ),
            daily = com.example.data.remote.DailyDto(
                time = dailyTimes,
                weatherCode = dailyCode,
                temperature2mMax = dailyMax,
                temperature2mMin = dailyMin,
                apparentTemperatureMax = dailyFlMax,
                apparentTemperatureMin = dailyFlMin,
                sunrise = dailySr,
                sunset = dailySs,
                daylightDuration = dailyDaylight,
                uvIndexMax = dailyUv,
                precipitationSum = dailyPrecip,
                precipitationProbabilityMax = dailyProb,
                windSpeed10mMax = dailyWind,
                windGusts10mMax = dailyGust,
                windDirection10mDominant = dailyDir
            )
        )

        val fallbackAq = OpenMeteoAirQualityResponse(
            current = com.example.data.remote.CurrentAirQualityDto(
                time = isoFmt.format(Date(nowMillis)),
                usAqi = 64,
                europeanAqi = 46,
                pm10 = 68.0,
                pm25 = 19.5,
                carbonMonoxide = 285.0,
                nitrogenDioxide = 21.0,
                sulphurDioxide = 9.0,
                ozone = 66.0,
                aerosolOpticalDepth = 0.31,
                dust = 54.0,
                uvIndex = 6.8
            ),
            hourly = com.example.data.remote.HourlyAirQualityDto(
                time = hourlyTimes.take(48),
                usAqi = List(48) { 58 + (it % 6) * 4 },
                europeanAqi = List(48) { 42 + (it % 5) * 3 },
                pm10 = List(48) { 62.0 + sin(it / 4.0) * 18.0 },
                pm25 = List(48) { 18.0 + cos(it / 5.0) * 5.0 },
                dust = List(48) { 48.0 + sin(it / 4.0) * 16.0 },
                ozone = List(48) { 64.0 + sin(it / 3.0) * 12.0 },
                nitrogenDioxide = List(48) { 19.0 + cos(it / 4.0) * 4.0 }
            )
        )

        return mapDtosToBundle(
            city = city,
            forecastDto = fallbackForecast,
            airQualityDto = fallbackAq,
            radarDto = null,
            timestampMillis = nowMillis,
            source = DataFreshnessSource.CLIMATOLOGICAL_FALLBACK
        )
    }

    private data class AqiClassification(
        val catEn: String,
        val catAr: String,
        val guideEn: String,
        val guideAr: String
    )

    private fun classifyAqi(usAqi: Int, dustUgM3: Double): AqiClassification {
        return when {
            usAqi <= 50 && dustUgM3 < 60 -> AqiClassification(
                catEn = "Good Air Quality",
                catAr = "جودة هواء جيدة (نقي)",
                guideEn = "Air quality is clean and ideal for outdoor sports, exercise, and ventilation.",
                guideAr = "جودة الهواء نقية ومثالية لممارسة الرياضة والأنشطة الخارجية وتهوية المنازل."
            )
            usAqi <= 100 -> AqiClassification(
                catEn = "Moderate Air Quality",
                catAr = "جودة هواء معتدلة",
                guideEn = "Acceptable air quality; unusually sensitive individuals should monitor PM10/dust levels during afternoon winds.",
                guideAr = "جودة الهواء مقبولة؛ يُنصح أصحاب الحساسية المفرطة بمتابعة مستويات الغبار وقت الظهيرة."
            )
            usAqi <= 150 -> AqiClassification(
                catEn = "Unhealthy for Sensitive Groups",
                catAr = "غير صحي للفئات الحساسة",
                guideEn = "Patients with asthma, children, and older adults should reduce prolonged outdoor exertion.",
                guideAr = "ينصح مرضى الربو والجهاز التنفسي وكبار السن بتقليل المجهود البدني الطويل في الخارج."
            )
            usAqi <= 200 -> AqiClassification(
                catEn = "Unhealthy Air Quality",
                catAr = "هواء غير صحي",
                guideEn = "Everyone may begin to experience respiratory irritation; wear a protective mask outdoors and keep windows closed.",
                guideAr = "قد يشعر الجميع بتهيج تنفسي؛ ارتدِ الكمامة الواقية عند الخروج وأغلق النوافذ."
            )
            usAqi <= 300 -> AqiClassification(
                catEn = "Very Unhealthy (Severe Dust/Smog)",
                catAr = "غير صحي للغاية (غبار كثيف)",
                guideEn = "Health alert: Avoid outdoor activities and run indoor air purifiers.",
                guideAr = "تحذير صحي: تجنب الأنشطة الخارجية وشغّل منقيات الهواء الداخلية."
            )
            else -> AqiClassification(
                catEn = "Hazardous Emergency (Haboob)",
                catAr = "خطر جداً — عاصفة ترابية شديدة",
                guideEn = "Emergency dust conditions: Remain indoors with sealed windows.",
                guideAr = "حالة طوارئ بيئية: ابقَ في الأماكن المغلقة وأحكم إغلاق المنافذ."
            )
        }
    }

    private fun formatDayOfWeek(dateIso: String, todayIso: String): Pair<String, String> {
        if (dateIso == todayIso) return "Today" to "اليوم"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = parser.parse(dateIso) ?: return dateIso to dateIso
            val enFmt = SimpleDateFormat("EEE, MMM d", Locale.US)
            val arFmt = SimpleDateFormat("EEEE d MMM", Locale.forLanguageTag("ar-IQ"))
            enFmt.format(date) to arFmt.format(date)
        } catch (_: Exception) {
            dateIso to dateIso
        }
    }

    private fun SavedCityEntity.toDomain() = CityLocation(
        id = id,
        nameEn = nameEn,
        nameAr = nameAr,
        governorateEn = governorateEn,
        governorateAr = governorateAr,
        countryEn = countryEn,
        countryAr = countryAr,
        latitude = latitude,
        longitude = longitude,
        elevationMeters = elevationMeters,
        timezone = timezone,
        isIraqiCity = isIraqiCity,
        isFavorite = isFavorite
    )

    private fun CityLocation.toEntity(lastViewed: Long) = SavedCityEntity(
        id = id,
        nameEn = nameEn,
        nameAr = nameAr,
        governorateEn = governorateEn,
        governorateAr = governorateAr,
        countryEn = countryEn,
        countryAr = countryAr,
        latitude = latitude,
        longitude = longitude,
        elevationMeters = elevationMeters,
        timezone = timezone,
        isIraqiCity = isIraqiCity,
        isFavorite = isFavorite,
        lastViewedEpochMillis = lastViewed
    )
}
