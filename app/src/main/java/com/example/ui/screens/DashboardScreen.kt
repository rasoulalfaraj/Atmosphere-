package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.CityLocation
import com.example.data.model.DataFreshnessSource
import com.example.data.model.HourlyForecastItem
import com.example.data.model.UserPreferences
import com.example.data.model.WeatherBundle
import com.example.data.model.WeatherConditionType
import com.example.ui.theme.AqiGoodGreen
import com.example.ui.theme.AqiModerateYellow
import com.example.ui.theme.AqiSensitiveOrange
import com.example.ui.theme.AqiUnhealthyRed
import com.example.ui.theme.ExtremeHeatCrimson
import com.example.ui.theme.GoldenHourOrange
import com.example.ui.theme.IonCyan
import com.example.ui.theme.OasisEmerald
import com.example.ui.theme.ShamalDustOchre
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.StormViolet
import com.example.util.AppStrings.tr
import com.example.util.MeteorologyCalculator
import com.example.util.UnitFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    bundle: WeatherBundle,
    savedCities: List<CityLocation>,
    prefs: UserPreferences,
    gpsStatusMessage: String?,
    onSelectCity: (CityLocation) -> Unit,
    onRequestGps: () -> Unit,
    onRefresh: () -> Unit,
    onOpenForecasts: () -> Unit,
    onOpenAlertsAndCities: () -> Unit,
    onOpenAiAssistant: () -> Unit,
    onOpenAirQuality: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = prefs.language
    val c = bundle.current
    val aq = bundle.airQuality

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Location Bar & Quick Iraqi Governorate Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = bundle.city.displayName(lang),
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            FreshnessBadge(source = bundle.source, lang = lang)
                        }
                        Text(
                            text = "${bundle.city.displayRegion(lang)} • ${String.format(Locale.US, "%.2f°N, %.2f°E", bundle.city.latitude, bundle.city.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onRequestGps,
                            modifier = Modifier.testTag("gps_detect_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = tr(lang, "Detect GPS Location", "تحديد موقعي عبر GPS"),
                                tint = IonCyan
                            )
                        }
                        IconButton(
                            onClick = onOpenAlertsAndCities,
                            modifier = Modifier.testTag("manage_cities_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = tr(lang, "Saved Cities", "المدن المحفوظة"),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.testTag("refresh_weather_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = tr(lang, "Refresh Weather", "تحديث الطقس"),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (!gpsStatusMessage.isNullOrBlank()) {
                    Text(
                        text = gpsStatusMessage,
                        style = MaterialTheme.typography.labelMedium,
                        color = IonCyan
                    )
                }

                // Quick City Switcher Strip (Baghdad, Basra, Nasiriyah, Najaf, Karbala, Mosul, Erbil, Sulaymaniyah)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("city_quick_row")
                ) {
                    items(savedCities, key = { it.id }) { cityItem ->
                        val isSelected = cityItem.id == bundle.city.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectCity(cityItem) },
                            label = {
                                Text(
                                    text = cityItem.displayName(lang),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IonCyan.copy(alpha = 0.24f),
                                selectedLabelColor = IonCyan
                            )
                        )
                    }
                }
            }
        }

        // 2. Hero Weather Card with Atmospheric Artwork & Primary Telemetry
        item {
            HeroWeatherOverviewCard(
                bundle = bundle,
                prefs = prefs,
                onOpenAiAssistant = onOpenAiAssistant
            )
        }

        // 3. Active Severe Weather / Iraqi Dust & Heat Alert Banner
        if (bundle.alerts.isNotEmpty()) {
            item {
                val topAlert = bundle.alerts.first()
                val alertColor = when (topAlert.severity.level) {
                    4 -> ExtremeHeatCrimson
                    3 -> ShamalDustOchre
                    else -> SolarAmber
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(alertColor.copy(alpha = 0.16f))
                        .border(1.dp, alertColor.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                        .clickable { onOpenAlertsAndCities() }
                        .padding(14.dp)
                        .testTag("active_alert_banner"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(alertColor.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = tr(lang, "Weather Alert", "تحذير جوي"),
                            tint = alertColor
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (lang == AppLanguage.ARABIC) topAlert.titleAr else topAlert.titleEn,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (lang == AppLanguage.ARABIC) topAlert.summaryAr else topAlert.summaryEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 4. Smart 24-Hour Weather Timeline
        item {
            SmartHourlyTimelineCard(
                hourly = bundle.hourly.take(24),
                prefs = prefs,
                onOpenForecasts = onOpenForecasts
            )
        }

        // 5. Iraq Environmental Monitor (Heat Index, Dust PM10, Shamal Wind, Visibility)
        item {
            IraqClimateMonitorCard(
                bundle = bundle,
                prefs = prefs,
                onOpenAirQuality = onOpenAirQuality
            )
        }

        // 6. Comprehensive 12-Tile Real-Time Meteorological Telemetry Grid
        item {
            val updatedTimeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(bundle.fetchedAtEpochMillis))
            val localClock = MeteorologyCalculator.extractHourMinute(c.localTimeIso, "14:00")
            val srClock = MeteorologyCalculator.extractHourMinute(c.sunriseIso, "06:20")
            val ssClock = MeteorologyCalculator.extractHourMinute(c.sunsetIso, "17:05")

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr(lang, "Real-Time Atmospheric Telemetry", "الرصد الجوي التفصيلي المباشر"),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = tr(lang, "Updated $updatedTimeStr", "تحديث $updatedTimeStr"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val metrics = listOf(
                    TelemetryTileData(
                        icon = Icons.Default.DeviceThermostat,
                        label = tr(lang, "Feels Like / Heat Index", "المحسوسة / مؤشر الحر"),
                        value = UnitFormatter.formatTemp(c.feelsLikeC, prefs.tempUnit),
                        sub = "${tr(lang, "Heat Index", "الحر")}: ${UnitFormatter.formatTemp(c.heatIndexC, prefs.tempUnit)}",
                        accent = SolarAmber
                    ),
                    TelemetryTileData(
                        icon = Icons.Default.Air,
                        label = tr(lang, "Wind & Direction", "الرياح والاتجاه"),
                        value = UnitFormatter.formatWind(c.windSpeedKmh, prefs),
                        sub = "${if (lang == AppLanguage.ARABIC) c.windDirectionCardinalAr else c.windDirectionCardinalEn} (${c.windDirectionDeg}°)",
                        accent = IonCyan
                    ),
                    TelemetryTileData(
                        icon = Icons.Default.Speed,
                        label = tr(lang, "Wind Gusts", "هبات الرياح"),
                        value = UnitFormatter.formatWind(c.windGustsKmh, prefs),
                        sub = tr(lang, "Peak 10m surface gust", "أقصى هبة سطحية (10م)"),
                        accent = IonCyan
                    ),
                    TelemetryTileData(
                        icon = Icons.Default.WaterDrop,
                        label = tr(lang, "Humidity & Dew Point", "الرطوبة ونقطة الندى"),
                        value = "${c.humidityPercent}%",
                        sub = "${tr(lang, "Dew Point", "نقطة الندى")}: ${UnitFormatter.formatTemp(c.dewPointC, prefs.tempUnit)}",
                        accent = Color(0xFF38BDF8)
                    ),
                    TelemetryTileData(
                        icon = Icons.Default.Compress,
                        label = tr(lang, "Atmospheric Pressure", "الضغط الجوي"),
                        value = UnitFormatter.formatPressure(c.pressureHpa, prefs.pressureUnit),
                        sub = "${tr(lang, "Surface", "السطحي")}: ${c.surfacePressureHpa.roundToInt()} hPa",
                        accent = StormViolet
                    ),
                    TelemetryTileData(
                        icon = Icons.Default.Visibility,
                        label = tr(lang, "Horizontal Visibility", "مدى الرؤية الأفقية"),
                        value = UnitFormatter.formatVisibility(c.visibilityMeters, lang),
                        sub = if (c.visibilityMeters >= 9000) tr(lang, "Clear horizon", "رؤية واضحة") else tr(lang, "Dust/haze reduction", "تأثر بالغبار/الضباب"),
                        accent = OasisEmerald
                    ),
                    TelemetryTileData(
                        icon = Icons.Default.WbSunny,
                        label = tr(lang, "UV Index", "الأشعة فوق البنفسجية"),
                        value = String.format(Locale.US, "%.1f", c.uvIndex),
                        sub = when {
                            c.uvIndex >= 8 -> tr(lang, "Very High — Use SPF 50+", "مرتفع جداً — استخدم واقي")
                            c.uvIndex >= 5 -> tr(lang, "Moderate — Sunglasses", "متوسط — نظارات شمسية")
                            else -> tr(lang, "Low UV exposure", "منخفض")
                        },
                        accent = GoldenHourOrange
                    ),
                    TelemetryTileData(
                        icon = Icons.Default.Cloud,
                        label = tr(lang, "Cloud Coverage", "الغطاء السحابي"),
                        value = "${c.cloudCoverPercent}%",
                        sub = tr(lang, "Total sky obscuration", "نسبة تغطية السماء"),
                        accent = IonCyan
                    ),
                    TelemetryTileData(
                        icon = Icons.Default.Umbrella,
                        label = tr(lang, "Rain Probability", "احتمالية الأمطار"),
                        value = "${c.precipitationProbPercent}%",
                        sub = "${tr(lang, "Accumulation", "التراكم")}: ${UnitFormatter.formatPrecip(c.precipitationMm, prefs.precipUnit)}",
                        accent = Color(0xFF60A5FA)
                    ),
                    TelemetryTileData(
                        icon = Icons.Default.WbTwilight,
                        label = tr(lang, "Sunrise & Sunset", "الشروق والغروب"),
                        value = "$srClock / $ssClock",
                        sub = "${tr(lang, "Daylight", "طول النهار")}: ${String.format(Locale.US, "%.1f h", c.daylightDurationHours)}",
                        accent = SolarAmber
                    ),
                    TelemetryTileData(
                        icon = Icons.Default.Grain,
                        label = tr(lang, "Air Quality (US AQI)", "جودة الهواء (AQI)"),
                        value = "${aq.usAqi}",
                        sub = "PM10: ${aq.pm10UgM3.roundToInt()} • PM2.5: ${aq.pm25UgM3.roundToInt()} µg/m³",
                        accent = if (aq.usAqi <= 50) AqiGoodGreen else if (aq.usAqi <= 100) AqiModerateYellow else AqiSensitiveOrange
                    ),
                    TelemetryTileData(
                        icon = Icons.Default.Schedule,
                        label = tr(lang, "Local Solar Clock", "التوقيت المحلي"),
                        value = localClock,
                        sub = "${bundle.city.timezone} (${bundle.city.elevationMeters}m ASL)",
                        accent = OasisEmerald
                    )
                )

                for (rowIdx in metrics.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (tile in rowIdx) {
                            TelemetryMetricTile(
                                data = tile,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 7. Outdoor Comfort Score Card (Explainable AI & Biometeorology)
        item {
            OutdoorComfortScoreCard(bundle = bundle, lang = lang)
        }

        // 8. Golden Hour Tracker & Weather Photography Mode
        item {
            GoldenHourPhotographyCard(bundle = bundle, lang = lang)
        }

        // 9. Astronomy Dashboard (Moon Phase, Moonrise/Moonset, Stargazing)
        item {
            AstronomyDashboardCard(bundle = bundle, lang = lang)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FreshnessBadge(source: DataFreshnessSource, lang: AppLanguage) {
    val (label, color, icon) = when (source) {
        DataFreshnessSource.LIVE_API -> Triple(
            tr(lang, "LIVE", "مباشر"),
            OasisEmerald,
            Icons.Default.Explore
        )
        DataFreshnessSource.OFFLINE_ROOM_CACHE -> Triple(
            tr(lang, "OFFLINE CACHE", "ذاكرة غير متصلة"),
            SolarAmber,
            Icons.Default.CloudOff
        )
        DataFreshnessSource.CLIMATOLOGICAL_FALLBACK -> Triple(
            tr(lang, "BASELINE", "نموذج مناخي"),
            IonCyan,
            Icons.Default.Cloud
        )
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.18f),
        modifier = Modifier.border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(50))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HeroWeatherOverviewCard(
    bundle: WeatherBundle,
    prefs: UserPreferences,
    onOpenAiAssistant: () -> Unit
) {
    val lang = prefs.language
    val c = bundle.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(28.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_iraq_atmosphere_hero_1794026117887),
            contentDescription = tr(lang, "Atmospheric sky view", "مشهد الغلاف الجوي"),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(265.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(265.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x99060D1E),
                            Color(0xCC091328),
                            Color(0xF20B152B)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = UnitFormatter.formatTemp(c.temperatureC, prefs.tempUnit),
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White
                    )
                    Text(
                        text = if (lang == AppLanguage.ARABIC) c.descriptionAr else c.descriptionEn,
                        style = MaterialTheme.typography.headlineSmall,
                        color = IonCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${tr(lang, "H", "عظمى")}: ${UnitFormatter.formatTemp(c.maxTempC, prefs.tempUnit)}  •  " +
                            "${tr(lang, "L", "صغرى")}: ${UnitFormatter.formatTemp(c.minTempC, prefs.tempUnit)}  •  " +
                            "${tr(lang, "Feels", "المحسوسة")}: ${UnitFormatter.formatTemp(c.feelsLikeC, prefs.tempUnit)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFE2E8F0)
                    )
                }

                // Weather Condition Emblem
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = conditionIcon(c.conditionType),
                        contentDescription = c.descriptionEn,
                        tint = SolarAmber,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (lang == AppLanguage.ARABIC) c.iraqiNoteAr else c.iraqiNoteEn,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCBD5E1),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick AI Weather Intelligence Prompt Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(IonCyan.copy(alpha = 0.18f))
                    .border(1.dp, IonCyan.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                    .clickable { onOpenAiAssistant() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("ask_atmosphere_ai_banner"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = IonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = tr(
                            lang,
                            "Ask Atmosphere AI: \"How will weather change in ${bundle.city.nameEn} over 24h?\"",
                            "اسأل المساعد الذكي: كيف سيتغير طقس ${bundle.city.nameAr} خلال 24 ساعة؟"
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun SmartHourlyTimelineCard(
    hourly: List<HourlyForecastItem>,
    prefs: UserPreferences,
    onOpenForecasts: () -> Unit
) {
    val lang = prefs.language
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = tr(lang, "Smart 24-Hour Weather Timeline", "الخط الزمني الذكي لـ 24 ساعة"),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = tr(lang, "Hourly temperature, rain probability & wind", "توقعات الحرارة والأمطار والرياح ساعة بساعة"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = tr(lang, "72h & 15d →", "72 ساعة و15 يوماً ←"),
                style = MaterialTheme.typography.labelLarge,
                color = IonCyan,
                modifier = Modifier
                    .clickable { onOpenForecasts() }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(hourly) { item ->
                Column(
                    modifier = Modifier
                        .width(74.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .padding(vertical = 10.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.hourLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = conditionIcon(item.conditionType),
                        contentDescription = item.hourLabel,
                        tint = if (item.isDay) SolarAmber else IonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = UnitFormatter.formatTemp(item.temperatureC, prefs.tempUnit),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "💧${item.precipProbPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.precipProbPercent >= 30) IonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${item.windSpeedKmh.roundToInt()} km/h",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun IraqClimateMonitorCard(
    bundle: WeatherBundle,
    prefs: UserPreferences,
    onOpenAirQuality: () -> Unit
) {
    val lang = prefs.language
    val aq = bundle.airQuality
    val c = bundle.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.84f))
            .border(1.dp, ShamalDustOchre.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
            .clickable { onOpenAirQuality() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Grain,
                    contentDescription = "Dust & Heat Monitor",
                    tint = ShamalDustOchre
                )
                Text(
                    text = tr(
                        lang,
                        "Iraq & Regional Climate Intelligence",
                        "المرصد المناخي العراقي (الحر والغبار والشمال)"
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "AQI ${aq.usAqi}",
                style = MaterialTheme.typography.labelLarge,
                color = ShamalDustOchre,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (lang == AppLanguage.ARABIC) aq.iraqiDustStatusAr else aq.iraqiDustStatusEn,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )

        if (bundle.city.climateNotesEn.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (lang == AppLanguage.ARABIC) bundle.city.climateNotesAr else bundle.city.climateNotesEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MiniStatPill(
                label = tr(lang, "Mineral Dust", "الغبار المعدني"),
                value = "${aq.dustUgM3.roundToInt()} µg/m³",
                color = ShamalDustOchre
            )
            MiniStatPill(
                label = tr(lang, "PM10 Silt", "دقائق PM10"),
                value = "${aq.pm10UgM3.roundToInt()} µg/m³",
                color = SolarAmber
            )
            MiniStatPill(
                label = tr(lang, "Heat Index", "مؤشر الحرارة"),
                value = UnitFormatter.formatTemp(c.heatIndexC, prefs.tempUnit),
                color = ExtremeHeatCrimson
            )
        }
    }
}

@Composable
private fun MiniStatPill(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold)
    }
}

data class TelemetryTileData(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val sub: String,
    val accent: Color
)

@Composable
private fun TelemetryMetricTile(
    data: TelemetryTileData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = data.label,
                tint = data.accent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = data.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = data.value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = data.sub,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OutdoorComfortScoreCard(bundle: WeatherBundle, lang: AppLanguage) {
    val comfort = bundle.comfort
    val scoreColor = when {
        comfort.overallScore >= 75 -> OasisEmerald
        comfort.overallScore >= 50 -> SolarAmber
        else -> AqiUnhealthyRed
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tr(lang, "Explainable Outdoor Comfort Score", "مؤشر الراحة الخارجية التفسيري"),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (lang == AppLanguage.ARABIC) comfort.ratingAr else comfort.ratingEn,
                    style = MaterialTheme.typography.labelLarge,
                    color = scoreColor
                )
            }
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(scoreColor.copy(alpha = 0.16f))
                    .border(2.dp, scoreColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${comfort.overallScore}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = scoreColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = if (lang == AppLanguage.ARABIC) comfort.explanationAr else comfort.explanationEn,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ComfortFactorRow(tr(lang, "Thermal Comfort", "الراحة الحرارية"), comfort.thermalScore, IonCyan)
        ComfortFactorRow(tr(lang, "Air & Dust Purity", "نقاء الهواء من الغبار"), comfort.airAndDustScore, OasisEmerald)
        ComfortFactorRow(tr(lang, "Wind Stability", "استقرار الرياح"), comfort.windScore, SolarAmber)
    }
}

@Composable
private fun ComfortFactorRow(label: String, score: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "$score/100", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { (score / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = color,
            trackColor = color.copy(alpha = 0.16f)
        )
    }
}

@Composable
private fun GoldenHourPhotographyCard(bundle: WeatherBundle, lang: AppLanguage) {
    val gh = bundle.goldenHour
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
            .border(1.dp, GoldenHourOrange.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Photography Mode",
                    tint = GoldenHourOrange
                )
                Column {
                    Text(
                        text = tr(lang, "Golden Hour & Photography Mode", "الساعة الذهبية ووضع التصوير الفوتوغرافي"),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (lang == AppLanguage.ARABIC) gh.cloudDramaAr else gh.cloudDramaEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldenHourOrange
                    )
                }
            }
            Text(
                text = "${gh.photographyScore}%",
                style = MaterialTheme.typography.titleLarge,
                color = GoldenHourOrange,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MiniStatPill(
                label = tr(lang, "Morning Golden", "ذهبية الصباح"),
                value = gh.morningGoldenHour,
                color = SolarAmber
            )
            MiniStatPill(
                label = tr(lang, "Evening Golden", "ذهبية المساء"),
                value = gh.eveningGoldenHourStart,
                color = GoldenHourOrange
            )
            MiniStatPill(
                label = tr(lang, "Blue Hour", "الساعة الزرقاء"),
                value = gh.eveningBlueHourEnd,
                color = IonCyan
            )
        }

        Text(
            text = "• ${if (lang == AppLanguage.ARABIC) gh.atmosphericClarityAr else gh.atmosphericClarityEn}\n• ${if (lang == AppLanguage.ARABIC) gh.tripodWindAdviceAr else gh.tripodWindAdviceEn}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AstronomyDashboardCard(bundle: WeatherBundle, lang: AppLanguage) {
    val astro = bundle.astronomy
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
            .border(1.dp, StormViolet.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Custom Moon Phase Canvas
                Canvas(modifier = Modifier.size(44.dp)) {
                    val r = size.minDimension / 2f
                    drawCircle(color = Color(0xFF1E293B), radius = r)
                    val sweep = (astro.illuminationPercent / 100f) * 360f
                    drawArc(
                        color = Color(0xFFFDE047),
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height)
                    )
                    drawCircle(
                        color = StormViolet,
                        radius = r,
                        style = Stroke(width = 2f)
                    )
                }

                Column {
                    Text(
                        text = tr(lang, "Astronomy & Night-Sky Dashboard", "المرصد الفلكي ومنازل القمر"),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${if (lang == AppLanguage.ARABIC) astro.moonPhaseNameAr else astro.moonPhaseNameEn} • ${astro.illuminationPercent}% ${tr(lang, "Illuminated", "إضاءة")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = StormViolet
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.NightsStay,
                contentDescription = "Moon",
                tint = StormViolet
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MiniStatPill(
                label = tr(lang, "Moonrise", "طلوع القمر"),
                value = astro.moonriseEst,
                color = StormViolet
            )
            MiniStatPill(
                label = tr(lang, "Moonset", "أفول القمر"),
                value = astro.moonsetEst,
                color = IonCyan
            )
            MiniStatPill(
                label = tr(lang, "Stargazing", "رصد النجوم"),
                value = "${astro.stargazingScore}/100",
                color = OasisEmerald
            )
        }

        Text(
            text = if (lang == AppLanguage.ARABIC) astro.stargazingConditionAr else astro.stargazingConditionEn,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun conditionIcon(type: WeatherConditionType): ImageVector = when (type) {
    WeatherConditionType.CLEAR_DAY, WeatherConditionType.EXTREME_HEAT -> Icons.Default.WbSunny
    WeatherConditionType.CLEAR_NIGHT -> Icons.Default.NightsStay
    WeatherConditionType.PARTLY_CLOUDY, WeatherConditionType.OVERCAST, WeatherConditionType.FOG_MIST -> Icons.Default.Cloud
    WeatherConditionType.DUST_SANDSTORM -> Icons.Default.Grain
    WeatherConditionType.LIGHT_RAIN, WeatherConditionType.HEAVY_RAIN -> Icons.Default.Umbrella
    WeatherConditionType.THUNDERSTORM -> Icons.Default.Thunderstorm
    WeatherConditionType.SNOW -> Icons.Default.Cloud
}
