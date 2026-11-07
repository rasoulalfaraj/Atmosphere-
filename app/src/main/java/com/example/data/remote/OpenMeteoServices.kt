package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

// 1. Open-Meteo Forecast API DTOs
@JsonClass(generateAdapter = true)
data class OpenMeteoForecastResponse(
    @Json(name = "latitude") val latitude: Double = 0.0,
    @Json(name = "longitude") val longitude: Double = 0.0,
    @Json(name = "elevation") val elevation: Double? = null,
    @Json(name = "timezone") val timezone: String? = null,
    @Json(name = "current") val current: CurrentDto? = null,
    @Json(name = "hourly") val hourly: HourlyDto? = null,
    @Json(name = "daily") val daily: DailyDto? = null
)

@JsonClass(generateAdapter = true)
data class CurrentDto(
    @Json(name = "time") val time: String? = null,
    @Json(name = "temperature_2m") val temperature2m: Double? = null,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int? = null,
    @Json(name = "apparent_temperature") val apparentTemperature: Double? = null,
    @Json(name = "is_day") val isDay: Int? = null,
    @Json(name = "precipitation") val precipitation: Double? = null,
    @Json(name = "weather_code") val weatherCode: Int? = null,
    @Json(name = "cloud_cover") val cloudCover: Int? = null,
    @Json(name = "pressure_msl") val pressureMsl: Double? = null,
    @Json(name = "surface_pressure") val surfacePressure: Double? = null,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double? = null,
    @Json(name = "wind_direction_10m") val windDirection10m: Int? = null,
    @Json(name = "wind_gusts_10m") val windGusts10m: Double? = null
)

@JsonClass(generateAdapter = true)
data class HourlyDto(
    @Json(name = "time") val time: List<String> = emptyList(),
    @Json(name = "temperature_2m") val temperature2m: List<Double?> = emptyList(),
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: List<Int?> = emptyList(),
    @Json(name = "dew_point_2m") val dewPoint2m: List<Double?> = emptyList(),
    @Json(name = "apparent_temperature") val apparentTemperature: List<Double?> = emptyList(),
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int?> = emptyList(),
    @Json(name = "precipitation") val precipitation: List<Double?> = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int?> = emptyList(),
    @Json(name = "pressure_msl") val pressureMsl: List<Double?> = emptyList(),
    @Json(name = "cloud_cover") val cloudCover: List<Int?> = emptyList(),
    @Json(name = "visibility") val visibility: List<Double?> = emptyList(),
    @Json(name = "wind_speed_10m") val windSpeed10m: List<Double?> = emptyList(),
    @Json(name = "wind_direction_10m") val windDirection10m: List<Int?> = emptyList(),
    @Json(name = "wind_gusts_10m") val windGusts10m: List<Double?> = emptyList(),
    @Json(name = "uv_index") val uvIndex: List<Double?> = emptyList(),
    @Json(name = "is_day") val isDay: List<Int?> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DailyDto(
    @Json(name = "time") val time: List<String> = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int?> = emptyList(),
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double?> = emptyList(),
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double?> = emptyList(),
    @Json(name = "apparent_temperature_max") val apparentTemperatureMax: List<Double?> = emptyList(),
    @Json(name = "apparent_temperature_min") val apparentTemperatureMin: List<Double?> = emptyList(),
    @Json(name = "sunrise") val sunrise: List<String?> = emptyList(),
    @Json(name = "sunset") val sunset: List<String?> = emptyList(),
    @Json(name = "daylight_duration") val daylightDuration: List<Double?> = emptyList(),
    @Json(name = "uv_index_max") val uvIndexMax: List<Double?> = emptyList(),
    @Json(name = "precipitation_sum") val precipitationSum: List<Double?> = emptyList(),
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int?> = emptyList(),
    @Json(name = "wind_speed_10m_max") val windSpeed10mMax: List<Double?> = emptyList(),
    @Json(name = "wind_gusts_10m_max") val windGusts10mMax: List<Double?> = emptyList(),
    @Json(name = "wind_direction_10m_dominant") val windDirection10mDominant: List<Int?> = emptyList()
)

// 2. Open-Meteo Air Quality & Dust API DTOs
@JsonClass(generateAdapter = true)
data class OpenMeteoAirQualityResponse(
    @Json(name = "current") val current: CurrentAirQualityDto? = null,
    @Json(name = "hourly") val hourly: HourlyAirQualityDto? = null
)

@JsonClass(generateAdapter = true)
data class CurrentAirQualityDto(
    @Json(name = "time") val time: String? = null,
    @Json(name = "us_aqi") val usAqi: Int? = null,
    @Json(name = "european_aqi") val europeanAqi: Int? = null,
    @Json(name = "pm10") val pm10: Double? = null,
    @Json(name = "pm2_5") val pm25: Double? = null,
    @Json(name = "carbon_monoxide") val carbonMonoxide: Double? = null,
    @Json(name = "nitrogen_dioxide") val nitrogenDioxide: Double? = null,
    @Json(name = "sulphur_dioxide") val sulphurDioxide: Double? = null,
    @Json(name = "ozone") val ozone: Double? = null,
    @Json(name = "aerosol_optical_depth") val aerosolOpticalDepth: Double? = null,
    @Json(name = "dust") val dust: Double? = null,
    @Json(name = "uv_index") val uvIndex: Double? = null
)

@JsonClass(generateAdapter = true)
data class HourlyAirQualityDto(
    @Json(name = "time") val time: List<String> = emptyList(),
    @Json(name = "us_aqi") val usAqi: List<Int?> = emptyList(),
    @Json(name = "european_aqi") val europeanAqi: List<Int?> = emptyList(),
    @Json(name = "pm10") val pm10: List<Double?> = emptyList(),
    @Json(name = "pm2_5") val pm25: List<Double?> = emptyList(),
    @Json(name = "dust") val dust: List<Double?> = emptyList(),
    @Json(name = "ozone") val ozone: List<Double?> = emptyList(),
    @Json(name = "nitrogen_dioxide") val nitrogenDioxide: List<Double?> = emptyList()
)

// 3. Open-Meteo Geocoding API DTOs
@JsonClass(generateAdapter = true)
data class GeocodingSearchResponse(
    @Json(name = "results") val results: List<GeocodingResultDto>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResultDto(
    @Json(name = "id") val id: Long = 0L,
    @Json(name = "name") val name: String = "",
    @Json(name = "latitude") val latitude: Double = 0.0,
    @Json(name = "longitude") val longitude: Double = 0.0,
    @Json(name = "elevation") val elevation: Double? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "country_code") val countryCode: String? = null,
    @Json(name = "admin1") val admin1: String? = null,
    @Json(name = "timezone") val timezone: String? = null
)

// 4. RainViewer Radar Maps API DTOs
@JsonClass(generateAdapter = true)
data class RainViewerMapsResponse(
    @Json(name = "host") val host: String? = null,
    @Json(name = "radar") val radar: RainViewerRadarData? = null
)

@JsonClass(generateAdapter = true)
data class RainViewerRadarData(
    @Json(name = "past") val past: List<RainViewerFrameDto>? = null,
    @Json(name = "nowcast") val nowcast: List<RainViewerFrameDto>? = null
)

@JsonClass(generateAdapter = true)
data class RainViewerFrameDto(
    @Json(name = "time") val time: Long = 0L,
    @Json(name = "path") val path: String = ""
)

interface OpenMeteoForecastApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,cloud_cover,pressure_msl,surface_pressure,wind_speed_10m,wind_direction_10m,wind_gusts_10m",
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,dew_point_2m,apparent_temperature,precipitation_probability,precipitation,weather_code,pressure_msl,cloud_cover,visibility,wind_speed_10m,wind_direction_10m,wind_gusts_10m,uv_index,is_day",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,sunrise,sunset,daylight_duration,uv_index_max,precipitation_sum,precipitation_probability_max,wind_speed_10m_max,wind_gusts_10m_max,wind_direction_10m_dominant",
        @Query("timezone") timezone: String = "auto",
        @Query("past_days") pastDays: Int = 3,
        @Query("forecast_days") forecastDays: Int = 14
    ): OpenMeteoForecastResponse
}

interface OpenMeteoAirQualityApi {
    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "us_aqi,european_aqi,pm10,pm2_5,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,ozone,aerosol_optical_depth,dust,uv_index",
        @Query("hourly") hourly: String = "us_aqi,european_aqi,pm10,pm2_5,dust,ozone,nitrogen_dioxide",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 3
    ): OpenMeteoAirQualityResponse
}

interface OpenMeteoGeocodingApi {
    @GET("v1/search")
    suspend fun searchCities(
        @Query("name") query: String,
        @Query("count") count: Int = 12,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingSearchResponse
}

interface RainViewerRadarApi {
    @GET("public/weather-maps.json")
    suspend fun getRadarFrames(): RainViewerMapsResponse
}
