package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.AqiStandard
import com.example.data.model.UserPreferences
import com.example.data.model.WeatherBundle
import com.example.ui.components.ChartDataPoint
import com.example.ui.components.InteractiveMeteorologicalChart
import com.example.ui.theme.AqiGoodGreen
import com.example.ui.theme.AqiHazardousMaroon
import com.example.ui.theme.AqiModerateYellow
import com.example.ui.theme.AqiSensitiveOrange
import com.example.ui.theme.AqiUnhealthyRed
import com.example.ui.theme.AqiVeryUnhealthyPurple
import com.example.ui.theme.GoldenHourOrange
import com.example.ui.theme.IonCyan
import com.example.ui.theme.OasisEmerald
import com.example.ui.theme.ShamalDustOchre
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.StormViolet
import com.example.util.AppStrings.tr
import com.example.util.UnitFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AirQualityAndAnalyticsScreen(
    bundle: WeatherBundle,
    prefs: UserPreferences,
    onSelectAqiStandard: (AqiStandard) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = prefs.language
    val aq = bundle.airQuality
    var selectedSection by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("air_analytics_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = tr(lang, "Air Quality & Weather Analytics", "جودة الهواء والتحليلات البيئية"),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = selectedSection == 0,
                        onClick = { selectedSection = 0 },
                        label = { Text(tr(lang, "Air Quality & Dust (AQI)", "جودة الهواء والغبار")) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OasisEmerald.copy(alpha = 0.22f),
                            selectedLabelColor = OasisEmerald
                        ),
                        modifier = Modifier.testTag("tab_air_quality")
                    )
                    FilterChip(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        label = { Text(tr(lang, "Meteorological Charts", "الرسوم البيانية للطقس")) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IonCyan.copy(alpha = 0.22f),
                            selectedLabelColor = IonCyan
                        ),
                        modifier = Modifier.testTag("tab_weather_analytics")
                    )
                }
            }
        }

        if (selectedSection == 0) {
            // 1. AQI Gauge Card + Standard Toggle (US EPA vs European EAQI)
            item {
                val activeAqiValue = if (prefs.aqiStandard == AqiStandard.US_EPA) aq.usAqi else aq.europeanAqi
                val aqiColor = getAqiColor(aq.usAqi)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.84f))
                        .border(1.dp, aqiColor.copy(alpha = 0.5f), RoundedCornerShape(26.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (lang == AppLanguage.ARABIC) aq.categoryAr else aq.categoryEn,
                                style = MaterialTheme.typography.headlineSmall,
                                color = aqiColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (lang == AppLanguage.ARABIC) prefs.aqiStandard.labelAr else prefs.aqiStandard.labelEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Circular AQI Dial
                        Box(
                            modifier = Modifier.size(84.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = aqiColor.copy(alpha = 0.2f),
                                    startAngle = 135f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    style = Stroke(width = 18f, cap = StrokeCap.Round)
                                )
                                val progress = (activeAqiValue / 300f).coerceIn(0.05f, 1f)
                                drawArc(
                                    color = aqiColor,
                                    startAngle = 135f,
                                    sweepAngle = 270f * progress,
                                    useCenter = false,
                                    style = Stroke(width = 18f, cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$activeAqiValue",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (prefs.aqiStandard == AqiStandard.US_EPA) "US AQI" else "EAQI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // AQI Standard Switcher
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AqiStandard.entries.forEach { std ->
                            FilterChip(
                                selected = prefs.aqiStandard == std,
                                onClick = { onSelectAqiStandard(std) },
                                label = {
                                    Text(if (std == AqiStandard.US_EPA) "US EPA AQI" else "European EAQI")
                                }
                            )
                        }
                    }

                    Text(
                        text = if (lang == AppLanguage.ARABIC) aq.healthGuidanceAr else aq.healthGuidanceEn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = if (lang == AppLanguage.ARABIC) aq.dataSourceNoteAr else aq.dataSourceNoteEn,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. 8-Pollutant Concentration Breakdown (PM2.5, PM10, Mineral Dust, O3, NO2, SO2, CO, AOD)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.80f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = tr(lang, "Atmospheric Pollutant Concentrations", "تراكيز الملوثات والدقائق العالقة"),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    PollutantBarRow(
                        name = tr(lang, "PM2.5 (Fine Respirable Particulates)", "دقائق PM2.5 الناعمة"),
                        valueText = "${String.format(Locale.US, "%.1f", aq.pm25UgM3)} µg/m³",
                        fraction = (aq.pm25UgM3 / 75.0).toFloat(),
                        color = if (aq.pm25UgM3 < 25) OasisEmerald else SolarAmber
                    )
                    PollutantBarRow(
                        name = tr(lang, "PM10 (Coarse Thoracic Dust)", "دقائق PM10 الترابية"),
                        valueText = "${String.format(Locale.US, "%.1f", aq.pm10UgM3)} µg/m³",
                        fraction = (aq.pm10UgM3 / 250.0).toFloat(),
                        color = if (aq.pm10UgM3 < 80) IonCyan else ShamalDustOchre
                    )
                    PollutantBarRow(
                        name = tr(lang, "Mineral Desert Dust (Iraq Tooz)", "الغبار المعدني الصحراوي (طوز)"),
                        valueText = "${String.format(Locale.US, "%.1f", aq.dustUgM3)} µg/m³",
                        fraction = (aq.dustUgM3 / 220.0).toFloat(),
                        color = ShamalDustOchre
                    )
                    PollutantBarRow(
                        name = tr(lang, "Ozone (O₃)", "الأوزون السطحي (O₃)"),
                        valueText = "${String.format(Locale.US, "%.1f", aq.ozoneUgM3)} µg/m³",
                        fraction = (aq.ozoneUgM3 / 160.0).toFloat(),
                        color = IonCyan
                    )
                    PollutantBarRow(
                        name = tr(lang, "Nitrogen Dioxide (NO₂)", "ثاني أكسيد النيتروجين (NO₂)"),
                        valueText = "${String.format(Locale.US, "%.1f", aq.no2UgM3)} µg/m³",
                        fraction = (aq.no2UgM3 / 100.0).toFloat(),
                        color = StormViolet
                    )
                    PollutantBarRow(
                        name = tr(lang, "Sulfur Dioxide (SO₂)", "ثاني أكسيد الكبريت (SO₂)"),
                        valueText = "${String.format(Locale.US, "%.1f", aq.so2UgM3)} µg/m³",
                        fraction = (aq.so2UgM3 / 80.0).toFloat(),
                        color = GoldenHourOrange
                    )
                    PollutantBarRow(
                        name = tr(lang, "Carbon Monoxide (CO)", "أول أكسيد الكربون (CO)"),
                        valueText = "${aq.coUgM3.roundToInt()} µg/m³",
                        fraction = (aq.coUgM3 / 1000.0).toFloat(),
                        color = OasisEmerald
                    )
                    PollutantBarRow(
                        name = tr(lang, "Aerosol Optical Depth (550nm)", "العمق البصري للهباء الجوي (AOD)"),
                        valueText = String.format(Locale.US, "%.2f", aq.aerosolOpticalDepth),
                        fraction = (aq.aerosolOpticalDepth / 1.5).toFloat(),
                        color = SolarAmber
                    )
                }
            }

            // 3. 48-Hour PM10 & Dust Concentration Trend Chart
            item {
                val aqPoints = aq.hourlyTrend.take(36).map { h ->
                    ChartDataPoint(
                        label = h.hourLabel,
                        primaryValue = h.pm10.toFloat(),
                        secondaryValue = h.dustUgM3.toFloat(),
                        tooltipSubtitle = "US AQI ${h.usAqi} • PM2.5: ${h.pm25.roundToInt()} µg/m³"
                    )
                }
                InteractiveMeteorologicalChart(
                    title = tr(lang, "36h PM10 & Mineral Dust Trend", "اتجاه الغبار ودقائق PM10 لـ 36 ساعة"),
                    subtitle = tr(lang, "Tracks Shamal dust transport and particulate settling", "يتتبع حركة الغبار وترسب العوالق الترابية"),
                    points = aqPoints,
                    unitSymbol = " µg/m³",
                    primaryColor = ShamalDustOchre,
                    secondaryColor = SolarAmber,
                    primaryLegend = "PM10",
                    secondaryLegend = tr(lang, "Mineral Dust", "الغبار")
                )
            }
        } else {
            // ADVANCED WEATHER ANALYTICS SECTION
            // 1. Temperature vs Dew Point Spread Chart
            item {
                val tempDewPoints = bundle.hourly.take(36).map { h ->
                    ChartDataPoint(
                        label = h.hourLabel,
                        primaryValue = UnitFormatter.convertTempValue(h.temperatureC, prefs.tempUnit).toFloat(),
                        secondaryValue = UnitFormatter.convertTempValue(h.dewPointC, prefs.tempUnit).toFloat(),
                        tooltipSubtitle = "RH ${h.humidityPercent}%"
                    )
                }
                InteractiveMeteorologicalChart(
                    title = tr(lang, "Temperature & Dew Point Spread", "الحرارة ونقطة الندى"),
                    subtitle = tr(lang, "Closer curves indicate higher condensation/fog potential", "تقارب المنحنيين يشير إلى رطوبة عالية أو تشكل ضباب"),
                    points = tempDewPoints,
                    unitSymbol = prefs.tempUnit.symbol,
                    primaryColor = SolarAmber,
                    secondaryColor = IonCyan,
                    primaryLegend = tr(lang, "Temperature", "الحرارة"),
                    secondaryLegend = tr(lang, "Dew Point", "نقطة الندى")
                )
            }

            // 2. Wind Speed & Peak Gusts Chart
            item {
                val windPoints = bundle.hourly.take(36).map { h ->
                    ChartDataPoint(
                        label = h.hourLabel,
                        primaryValue = h.windSpeedKmh.toFloat(),
                        secondaryValue = h.windGustsKmh.toFloat(),
                        tooltipSubtitle = "${h.windDirectionDeg}°"
                    )
                }
                InteractiveMeteorologicalChart(
                    title = tr(lang, "Sustained Wind & Peak Gusts", "سرعة الرياح والهبات القصوى"),
                    subtitle = tr(lang, "10m surface anemometer forecast & gust factor", "الرياح السطحية على ارتفاع 10 أمتار والهبات"),
                    points = windPoints,
                    unitSymbol = " km/h",
                    primaryColor = IonCyan,
                    secondaryColor = GoldenHourOrange,
                    primaryLegend = tr(lang, "Sustained Wind", "الرياح المستمرة"),
                    secondaryLegend = tr(lang, "Peak Gusts", "الهبات")
                )
            }

            // 3. Atmospheric Pressure Trend Chart
            item {
                val pressurePoints = bundle.hourly.take(36).map { h ->
                    ChartDataPoint(
                        label = h.hourLabel,
                        primaryValue = h.pressureHpa.toFloat(),
                        tooltipSubtitle = "Clouds ${h.cloudCoverPercent}%"
                    )
                }
                InteractiveMeteorologicalChart(
                    title = tr(lang, "Barometric Pressure Tendency (MSL)", "اتجاه الضغط الجوي عند مستوى سطح البحر"),
                    subtitle = tr(lang, "Falling pressure signals approaching troughs or Shamal fronts", "انخفاض الضغط يشير لاقتراب منخفض جوي أو جبهة رياح"),
                    points = pressurePoints,
                    unitSymbol = " hPa",
                    primaryColor = StormViolet,
                    secondaryColor = IonCyan
                )
            }

            // 4. Relative Humidity & UV Index Charts
            item {
                val humPoints = bundle.hourly.take(36).map { h ->
                    ChartDataPoint(
                        label = h.hourLabel,
                        primaryValue = h.humidityPercent.toFloat(),
                        tooltipSubtitle = "UV ${String.format(Locale.US, "%.1f", h.uvIndex)}"
                    )
                }
                InteractiveMeteorologicalChart(
                    title = tr(lang, "Relative Humidity Curve (%)", "منحنى الرطوبة النسبية (%)"),
                    subtitle = tr(lang, "Diurnal moisture cycle across 36 hours", "الدورة اليومية للرطوبة خلال 36 ساعة"),
                    points = humPoints,
                    unitSymbol = "%",
                    primaryColor = OasisEmerald,
                    secondaryColor = IonCyan
                )
            }
        }
    }
}

@Composable
private fun PollutantBarRow(
    name: String,
    valueText: String,
    fraction: Float,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(text = valueText, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { fraction.coerceIn(0.04f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = color,
            trackColor = color.copy(alpha = 0.16f)
        )
    }
}

private fun getAqiColor(usAqi: Int): Color = when {
    usAqi <= 50 -> AqiGoodGreen
    usAqi <= 100 -> AqiModerateYellow
    usAqi <= 150 -> AqiSensitiveOrange
    usAqi <= 200 -> AqiUnhealthyRed
    usAqi <= 300 -> AqiVeryUnhealthyPurple
    else -> AqiHazardousMaroon
}
