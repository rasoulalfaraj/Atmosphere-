package com.example.data.repository

import android.util.Log
import com.example.data.model.AppLanguage
import com.example.data.model.WeatherBundle
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiWeatherService"

data class AiDailyIntelligence(
    val morningBriefing: String,
    val outdoorActivityAdvice: String,
    val clothingSuggestion: String,
    val commuteAndTravelAdvice: String,
    val heatAndDustSafetyNote: String
)

class AiWeatherService {

    private val chatModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val briefingSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "morningBriefing" to Schema.string(description = "Concise 2-3 sentence daily meteorological briefing grounded strictly in the provided telemetry"),
                "outdoorActivityAdvice" to Schema.string(description = "Specific best hours and recommendations for outdoor activities based on comfort score, UV, and dust"),
                "clothingSuggestion" to Schema.string(description = "Practical clothing & gear advice based on temperature, wind, UV, and rain probability"),
                "commuteAndTravelAdvice" to Schema.string(description = "Commute, visibility, wind, and road condition guidance"),
                "heatAndDustSafetyNote" to Schema.string(description = "Heat exposure, hydration, and Iraqi dust/PM10 respiratory guidance")
            )
        )
    }

    private val structuredBriefingModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = briefingSchema
            }
        )
    }

    /**
     * Generates a structured AI meteorological briefing grounded strictly in retrieved telemetry.
     */
    suspend fun generateDailyIntelligence(
        bundle: WeatherBundle,
        language: AppLanguage
    ): Result<AiDailyIntelligence> {
        val telemetryContext = buildTelemetryPromptContext(bundle, language)
        val langInstruction = if (language == AppLanguage.ARABIC) {
            "Respond entirely in clear, natural Arabic suitable for Iraq (using Iraqi meteorological context where appropriate)."
        } else {
            "Respond entirely in clear, professional English."
        }

        val prompt = """
            You are Atmosphere AI, a senior meteorologist and environmental intelligence system.
            Analyze ONLY the following real retrieved meteorological telemetry. NEVER invent or fabricate any measurements not present below.
            $langInstruction

            RETRIEVED TELEMETRY:
            $telemetryContext
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                structuredBriefingModel.generateContent(prompt)
            }
            val jsonText = response.text ?: throw IllegalStateException("Empty AI response.")
            val json = JSONObject(jsonText)
            Result.success(
                AiDailyIntelligence(
                    morningBriefing = json.optString("morningBriefing", ""),
                    outdoorActivityAdvice = json.optString("outdoorActivityAdvice", ""),
                    clothingSuggestion = json.optString("clothingSuggestion", ""),
                    commuteAndTravelAdvice = json.optString("commuteAndTravelAdvice", ""),
                    heatAndDustSafetyNote = json.optString("heatAndDustSafetyNote", "")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI Daily Intelligence timed out", e)
            Result.failure(Exception("AI analysis timed out after 60 seconds. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI Daily Intelligence failed", e)
            Result.failure(e)
        }
    }

    /**
     * Streams natural-language answers to user weather questions grounded in live telemetry
     * and saved cities comparison context.
     */
    fun streamWeatherQuestion(
        question: String,
        primaryBundle: WeatherBundle,
        comparisonBundles: List<WeatherBundle>,
        language: AppLanguage
    ): Flow<String> = flow {
        val primaryContext = buildTelemetryPromptContext(primaryBundle, language)
        val otherCitiesContext = comparisonBundles
            .filter { it.city.id != primaryBundle.city.id }
            .joinToString("\n\n") { b ->
                "CITY: ${b.city.nameEn} (${b.city.nameAr})\n" +
                    "Current: ${b.current.temperatureC.roundToInt()}°C (Feels ${b.current.feelsLikeC.roundToInt()}°C), ${b.current.descriptionEn}, " +
                    "Wind: ${b.current.windSpeedKmh.roundToInt()} km/h ${b.current.windDirectionCardinalEn}, Humidity: ${b.current.humidityPercent}%, " +
                    "Precip Prob: ${b.current.precipitationProbPercent}%, US AQI: ${b.airQuality.usAqi}, Dust: ${b.airQuality.dustUgM3.roundToInt()} µg/m³, " +
                    "Today High/Low: ${b.current.maxTempC.roundToInt()}°C / ${b.current.minTempC.roundToInt()}°C."
            }

        val langRule = if (language == AppLanguage.ARABIC) {
            "Answer in fluent Arabic (using Iraqi weather terminology such as الشمال، الشرجي، طوز where relevant)."
        } else {
            "Answer in clear, concise, authoritative English."
        }

        val fullPrompt = """
            You are Atmosphere AI, an expert meteorologist for Iraq and global weather.
            CRITICAL RULE: Ground every single number, temperature, wind speed, rain probability, and AQI reading strictly in the RETRIEVED TELEMETRY below. Never fabricate measurements.
            $langRule

            PRIMARY ACTIVE CITY TELEMETRY:
            $primaryContext

            OTHER MONITORED CITIES TELEMETRY:
            $otherCitiesContext

            USER QUESTION:
            $question
        """.trimIndent()

        val accumulated = StringBuilder()
        withTimeout(60_000L) {
            chatModel.generateContentStream(fullPrompt).collect { chunk ->
                val text = chunk.text.orEmpty()
                if (text.isNotEmpty()) {
                    accumulated.append(text)
                    emit(accumulated.toString())
                }
            }
        }
    }

    private fun buildTelemetryPromptContext(bundle: WeatherBundle, lang: AppLanguage): String {
        val c = bundle.current
        val aq = bundle.airQuality
        val next24Hours = bundle.hourly.take(24)
        val hottestHour = next24Hours.maxByOrNull { it.temperatureC }
        val coolestHour = next24Hours.minByOrNull { it.temperatureC }
        val wettestHour = next24Hours.maxByOrNull { it.precipProbPercent }

        val hourlyDigest = next24Hours.filterIndexed { index, _ -> index % 3 == 0 }
            .joinToString(" | ") { h ->
                "${h.hourLabel}: ${h.temperatureC.roundToInt()}°C, Rain ${h.precipProbPercent}%, Wind ${h.windSpeedKmh.roundToInt()}km/h"
            }

        val dailyDigest = bundle.daily.take(7).joinToString("\n") { d ->
            "- ${d.dateIso} (${d.dayNameEn}): High ${d.maxTempC.roundToInt()}°C / Low ${d.minTempC.roundToInt()}°C, ${d.descriptionEn}, Rain Prob ${d.precipProbMaxPercent}% (${String.format(Locale.US, "%.1f", d.precipSumMm)} mm), Max Wind ${d.windSpeedMaxKmh.roundToInt()} km/h, UV ${String.format(Locale.US, "%.1f", d.uvIndexMax)}"
        }

        return """
            Location: ${bundle.city.nameEn} (${bundle.city.nameAr}), ${bundle.city.governorateEn}, Lat ${bundle.city.latitude}, Lon ${bundle.city.longitude}
            Local Time: ${c.localTimeIso}
            Current Temp: ${String.format(Locale.US, "%.1f", c.temperatureC)}°C (Feels Like: ${String.format(Locale.US, "%.1f", c.feelsLikeC)}°C, Heat Index: ${String.format(Locale.US, "%.1f", c.heatIndexC)}°C)
            Today High / Low: ${String.format(Locale.US, "%.1f", c.maxTempC)}°C / ${String.format(Locale.US, "%.1f", c.minTempC)}°C
            Condition: ${c.descriptionEn} (${c.descriptionAr}) — ${c.iraqiNoteEn}
            Wind: ${String.format(Locale.US, "%.1f", c.windSpeedKmh)} km/h from ${c.windDirectionDeg}° (${c.windDirectionCardinalEn}), Gusts ${String.format(Locale.US, "%.1f", c.windGustsKmh)} km/h
            Humidity: ${c.humidityPercent}%, Dew Point: ${String.format(Locale.US, "%.1f", c.dewPointC)}°C
            Pressure: ${c.pressureHpa.roundToInt()} hPa, Visibility: ${String.format(Locale.US, "%.1f", c.visibilityMeters / 1000.0)} km, Cloud Cover: ${c.cloudCoverPercent}%, UV Index: ${String.format(Locale.US, "%.1f", c.uvIndex)}
            Precipitation Probability Now: ${c.precipitationProbPercent}%, Current Precip: ${c.precipitationMm} mm
            Sunrise: ${c.sunriseIso}, Sunset: ${c.sunsetIso}
            Air Quality: US AQI ${aq.usAqi} (${aq.categoryEn}), European EAQI ${aq.europeanAqi}, PM2.5: ${aq.pm25UgM3} µg/m³, PM10: ${aq.pm10UgM3} µg/m³, Mineral Dust: ${aq.dustUgM3} µg/m³, O3: ${aq.ozoneUgM3} µg/m³, NO2: ${aq.no2UgM3} µg/m³
            Outdoor Comfort Score: ${bundle.comfort.overallScore}/100 (${bundle.comfort.ratingEn})
            Next 24h Peak Temp: ${hottestHour?.temperatureC?.roundToInt()}°C at ${hottestHour?.hourLabel} | Lowest Temp: ${coolestHour?.temperatureC?.roundToInt()}°C at ${coolestHour?.hourLabel} | Max Rain Prob Next 24h: ${wettestHour?.precipProbPercent}% at ${wettestHour?.hourLabel}
            24h 3-Hourly Timeline: $hourlyDigest
            7-Day Outlook:
            $dailyDigest
        """.trimIndent()
    }
}
