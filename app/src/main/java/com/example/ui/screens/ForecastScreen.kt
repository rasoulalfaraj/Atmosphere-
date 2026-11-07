package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.DailyForecastItem
import com.example.data.model.UserPreferences
import com.example.data.model.WeatherBundle
import com.example.ui.components.ChartDataPoint
import com.example.ui.components.InteractiveMeteorologicalChart
import com.example.ui.theme.ExtremeHeatCrimson
import com.example.ui.theme.GoldenHourOrange
import com.example.ui.theme.IonCyan
import com.example.ui.theme.OasisEmerald
import com.example.ui.theme.RainCobalt
import com.example.ui.theme.SolarAmber
import com.example.util.AppStrings.tr
import com.example.util.MeteorologyCalculator
import com.example.util.UnitFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ForecastScreen(
    bundle: WeatherBundle,
    prefs: UserPreferences,
    modifier: Modifier = Modifier
) {
    val lang = prefs.language
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        tr(lang, "72-Hour Hourly", "توقعات 72 ساعة"),
        tr(lang, "14-Day Outlook", "توقعات 14 يوماً"),
        tr(lang, "Historical & Anomalies", "الأرشيف والشذوذ الحراري")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("forecast_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${bundle.city.displayName(lang)} — ${tr(lang, "Advanced Forecasting", "التنبؤات الجوية المتقدمة")}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { idx, label ->
                        FilterChip(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IonCyan.copy(alpha = 0.22f),
                                selectedLabelColor = IonCyan
                            ),
                            modifier = Modifier.testTag("forecast_tab_$idx")
                        )
                    }
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // 72-Hour Interactive Temperature + Feels Like Curve
                item {
                    val tempPoints = bundle.hourly.take(48).map { h ->
                        ChartDataPoint(
                            label = h.hourLabel,
                            primaryValue = UnitFormatter.convertTempValue(h.temperatureC, prefs.tempUnit).toFloat(),
                            secondaryValue = UnitFormatter.convertTempValue(h.feelsLikeC, prefs.tempUnit).toFloat(),
                            tooltipSubtitle = "${tr(lang, "Rain", "مطر")} ${h.precipProbPercent}% • ${UnitFormatter.formatWind(h.windSpeedKmh, prefs)}"
                        )
                    }
                    InteractiveMeteorologicalChart(
                        title = tr(lang, "48–72h Temperature & Feels-Like Curve", "منحنى الحرارة والمحسوسة (48–72 ساعة)"),
                        subtitle = tr(lang, "Tap or scrub across the curve to inspect hourly telemetry", "المس المنحنى لاستعراض القراءات الساعية"),
                        points = tempPoints,
                        unitSymbol = prefs.tempUnit.symbol,
                        primaryColor = IonCyan,
                        secondaryColor = SolarAmber,
                        primaryLegend = tr(lang, "Air Temp", "درجة الحرارة"),
                        secondaryLegend = tr(lang, "Feels Like", "المحسوسة")
                    )
                }

                // Hourly Precipitation Probability Bar Chart
                item {
                    val rainPoints = bundle.hourly.take(36).map { h ->
                        ChartDataPoint(
                            label = h.hourLabel,
                            primaryValue = h.precipProbPercent.toFloat(),
                            tooltipSubtitle = UnitFormatter.formatPrecip(h.precipMm, prefs.precipUnit)
                        )
                    }
                    InteractiveMeteorologicalChart(
                        title = tr(lang, "Hourly Precipitation Probability", "احتمالية الهطول المطري الساعية"),
                        subtitle = tr(lang, "Chance of rain (%) and expected accumulation", "نسبة الهطول المئوية والكمية المتوقعة"),
                        points = rainPoints,
                        unitSymbol = "%",
                        primaryColor = RainCobalt,
                        secondaryColor = IonCyan,
                        showBars = true
                    )
                }

                // Detailed 72-Hour Breakdown Rows
                items(bundle.hourly.take(72), key = { it.timeIso }) { hour ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.width(78.dp)) {
                            Text(
                                text = hour.hourLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = hour.timeIso.take(10),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = conditionIcon(hour.conditionType),
                            contentDescription = hour.hourLabel,
                            tint = if (hour.isDay) SolarAmber else IonCyan,
                            modifier = Modifier.size(24.dp)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "💧 ${hour.precipProbPercent}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (hour.precipProbPercent >= 30) IonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "☁️ ${hour.cloudCoverPercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = UnitFormatter.formatWind(hour.windSpeedKmh, prefs),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "RH ${hour.humidityPercent}% • UV ${hour.uvIndex.roundToInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = UnitFormatter.formatTemp(hour.temperatureC, prefs.tempUnit),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            1 -> {
                // 14-Day Daily Forecast with Visual Range Bars & Confidence Indicators
                val globalMin = bundle.daily.minOfOrNull { it.minTempC } ?: 10.0
                val globalMax = bundle.daily.maxOfOrNull { it.maxTempC } ?: 42.0
                val totalSpan = (globalMax - globalMin).coerceAtLeast(5.0)

                items(bundle.daily, key = { it.dateIso }) { day ->
                    DailyForecastCard(
                        day = day,
                        globalMinC = globalMin,
                        totalSpanC = totalSpan,
                        prefs = prefs
                    )
                }
            }

            2 -> {
                // Historical Weather Explorer & Temperature Anomalies
                item {
                    val allDays = bundle.pastDays + bundle.daily.take(10)
                    val anomalyPoints = allDays.map { d ->
                        ChartDataPoint(
                            label = d.dateIso.takeLast(5),
                            primaryValue = d.tempAnomalyC.toFloat(),
                            tooltipSubtitle = "${if (d.isPastDay) tr(lang, "Observed", "مرصود") else tr(lang, "Forecast", "متوقع")}: ${UnitFormatter.formatTemp(d.maxTempC, prefs.tempUnit)}"
                        )
                    }

                    InteractiveMeteorologicalChart(
                        title = tr(lang, "Temperature Anomaly Explorer (°C vs Synoptic Mean)", "مستكشف الشذوذ الحراري مقارنة بالمعدل"),
                        subtitle = tr(
                            lang,
                            "Compares past 3 observed days with upcoming 10-day synoptic trend",
                            "يقارن الأيام الثلاثة الماضية المرصودة باتجاه الأيام العشرة القادمة"
                        ),
                        points = anomalyPoints,
                        unitSymbol = "°C",
                        primaryColor = GoldenHourOrange,
                        secondaryColor = IonCyan,
                        showBars = true
                    )
                }

                item {
                    Text(
                        text = tr(lang, "Observed Past Days vs Forecast Confidence", "الأيام المرصودة السابقة ومؤشر موثوقية التوقعات"),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(bundle.pastDays + bundle.daily.take(7), key = { "hist_${it.dateIso}" }) { day ->
                    val anomSign = if (day.tempAnomalyC >= 0) "+" else ""
                    val anomColor = if (day.tempAnomalyC > 1.5) ExtremeHeatCrimson else if (day.tempAnomalyC < -1.5) IonCyan else OasisEmerald

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${if (lang == AppLanguage.ARABIC) day.dayNameAr else day.dayNameEn} (${day.dateIso})",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (day.isPastDay) {
                                    tr(lang, "Verified Archive Observation", "قراءة أرشيفية مرصودة")
                                } else {
                                    "${tr(lang, "Model Confidence", "موثوقية النموذج")}: ${day.confidencePercent}%"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${UnitFormatter.formatTemp(day.maxTempC, prefs.tempUnit)} / ${UnitFormatter.formatTemp(day.minTempC, prefs.tempUnit)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${tr(lang, "Anomaly", "الانحراف")}: $anomSign${String.format(Locale.US, "%.1f°C", day.tempAnomalyC)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = anomColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyForecastCard(
    day: DailyForecastItem,
    globalMinC: Double,
    totalSpanC: Double,
    prefs: UserPreferences
) {
    val lang = prefs.language
    val sr = MeteorologyCalculator.extractHourMinute(day.sunriseIso, "06:20")
    val ss = MeteorologyCalculator.extractHourMinute(day.sunsetIso, "17:05")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.80f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.width(110.dp)) {
                Text(
                    text = if (lang == AppLanguage.ARABIC) day.dayNameAr else day.dayNameEn,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (lang == AppLanguage.ARABIC) day.descriptionAr else day.descriptionEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = conditionIcon(day.conditionType),
                contentDescription = day.descriptionEn,
                tint = SolarAmber,
                modifier = Modifier.size(24.dp)
            )

            // Temperature Range Visual Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                Text(
                    text = UnitFormatter.formatTemp(day.minTempC, prefs.tempUnit, includeUnit = false),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val startFrac = ((day.minTempC - globalMinC) / totalSpanC).toFloat().coerceIn(0f, 0.85f)
                val endFrac = ((day.maxTempC - globalMinC) / totalSpanC).toFloat().coerceIn(startFrac + 0.1f, 1f)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(endFrac)
                            .padding(start = (startFrac * 60).dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(IonCyan, SolarAmber, ExtremeHeatCrimson)
                                )
                            )
                    )
                }

                Text(
                    text = UnitFormatter.formatTemp(day.maxTempC, prefs.tempUnit, includeUnit = false),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "💧 ${day.precipProbMaxPercent}% (${UnitFormatter.formatPrecip(day.precipSumMm, prefs.precipUnit)})",
                style = MaterialTheme.typography.labelSmall,
                color = if (day.precipProbMaxPercent >= 30) IonCyan else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "💨 ${UnitFormatter.formatWind(day.windSpeedMaxKmh, prefs)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "☀️ UV ${String.format(Locale.US, "%.1f", day.uvIndexMax)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "🌅 $sr – $ss",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
