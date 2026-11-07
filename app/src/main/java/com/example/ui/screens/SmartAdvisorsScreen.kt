package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.data.model.HighwayCorridorStatus
import com.example.data.model.PrayerWeatherSlot
import com.example.data.model.UserPreferences
import com.example.data.model.WeatherBundle
import com.example.ui.theme.ExtremeHeatCrimson
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
fun SmartAdvisorsScreen(
    bundle: WeatherBundle,
    prefs: UserPreferences,
    modifier: Modifier = Modifier
) {
    val lang = prefs.language
    var selectedKwSize by rememberSaveable { mutableDoubleStateOf(5.0) }
    var consumedCups by rememberSaveable { mutableIntStateOf(3) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("smart_advisors_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = tr(
                        lang,
                        "Smart Specialized Advisors — ${bundle.city.displayName(lang)}",
                        "المستشار البيئي والخدمي الذكي — ${bundle.city.displayName(lang)}"
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = tr(
                        lang,
                        "Solar Energy • Health & Hydration • Prayer Weather • Smart Agriculture • Iraqi Highways",
                        "الطاقة الشمسية • الرادار الصحي وشرب الماء • طقس أوقات الصلاة • الزراعة والنخيل • الطرق السريعة"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. SOLAR ROOFTOP & HOME INVERTER OPTIMIZER
        item {
            val solar = bundle.solarInsight
            val scaledKwh = (solar.estimatedKwhPer5KwSystem / 5.0) * selectedKwSize
            val peakAmps220V = ((selectedKwSize * 1000.0 * (solar.panelEfficiencyPercent / 100.0)) / 220.0).roundToInt()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .border(1.dp, SolarAmber.copy(alpha = 0.5f), RoundedCornerShape(26.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SolarAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SolarPower,
                                contentDescription = null,
                                tint = SolarAmber
                            )
                        }
                        Column {
                            Text(
                                text = tr(
                                    lang,
                                    "Solar Rooftop & Inverter Optimizer",
                                    "مستشار منظومات الطاقة الشمسية والإنفرتر"
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tr(
                                    lang,
                                    "Real-time PV efficiency, dust soiling & Ampere calculator",
                                    "كفاءة الألواح المباشرة، فاقد الغبار، وحاسبة الأمبير التقديرية"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Circular Efficiency Gauge
                    Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = SolarAmber.copy(alpha = 0.2f),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 12f, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = SolarAmber,
                                startAngle = -90f,
                                sweepAngle = 360f * (solar.panelEfficiencyPercent / 100f),
                                useCenter = false,
                                style = Stroke(width = 12f, cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "${solar.panelEfficiencyPercent}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = SolarAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // System Capacity Selector (3kW, 5kW, 8kW, 12kW)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = tr(lang, "Select Your Home Solar Array Size:", "اختر حجم منظومتك الشمسية المنزلية:"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(3.0, 5.0, 8.0, 12.0).forEach { kw ->
                            FilterChip(
                                selected = selectedKwSize == kw,
                                onClick = { selectedKwSize = kw },
                                label = { Text("${kw.toInt()} kW") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SolarAmber.copy(alpha = 0.25f),
                                    selectedLabelColor = SolarAmber
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SmartMetricBox(
                        label = tr(lang, "Daily Output", "الإنتاج اليومي"),
                        value = "${String.format(Locale.US, "%.1f", scaledKwh)} kWh",
                        color = OasisEmerald
                    )
                    SmartMetricBox(
                        label = tr(lang, "Noon Current", "أمبير الظهيرة"),
                        value = "~$peakAmps220V A",
                        color = IonCyan
                    )
                    SmartMetricBox(
                        label = tr(lang, "Dust Loss", "فاقد الغبار"),
                        value = "-${solar.dustSoilingLossPercent}%",
                        color = ShamalDustOchre
                    )
                    SmartMetricBox(
                        label = tr(lang, "Heat Loss", "فاقد الحرارة"),
                        value = "-${solar.heatDeratingPercent}%",
                        color = ExtremeHeatCrimson
                    )
                }

                Text(
                    text = "• ${if (lang == AppLanguage.ARABIC) solar.cleaningAdviceAr else solar.cleaningAdviceEn}\n" +
                        "• ${if (lang == AppLanguage.ARABIC) solar.inverterNoteAr else solar.inverterNoteEn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 2. BIOMETEOROLOGICAL HEALTH RADAR & INTERACTIVE HYDRATION TRACKER
        item {
            val health = bundle.healthInsight
            val targetCups = health.dailyWaterCups
            val hydrationProgress = (consumedCups.toFloat() / targetCups.coerceAtLeast(1)).coerceIn(0f, 1f)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .border(1.dp, IonCyan.copy(alpha = 0.45f), RoundedCornerShape(26.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(IonCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = IonCyan)
                    }
                    Column {
                        Text(
                            text = tr(
                                lang,
                                "Biometeorological Health & Hydration Radar",
                                "الرادار الصحي البيومتري وحاسبة شرب الماء"
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tr(
                                lang,
                                "Asthma/dust index, barometric migraine & thermal hydration",
                                "مؤشر الحساسية والربو، الشقيقة، والاحتياج المائي اليومي"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Interactive Water Intake Tracker Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(IonCyan.copy(alpha = 0.12f))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.LocalDrink, contentDescription = null, tint = IonCyan)
                            Column {
                                Text(
                                    text = tr(
                                        lang,
                                        "Recommended Hydration: ${String.format(Locale.US, "%.1f", health.dailyWaterLiters)} Liters ($targetCups Cups)",
                                        "الاحتياج اليومي للماء: ${String.format(Locale.US, "%.1f", health.dailyWaterLiters)} لتر ($targetCups كوب)"
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tr(
                                        lang,
                                        "Logged today: $consumedCups / $targetCups cups",
                                        "شربت اليوم: $consumedCups من أصل $targetCups كوب"
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IonCyan
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { if (consumedCups > 0) consumedCups-- },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Less", modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { if (consumedCups < 24) consumedCups++ },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(IonCyan)
                                    .testTag("add_water_cup_button")
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Cup",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { hydrationProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = IonCyan,
                        trackColor = IonCyan.copy(alpha = 0.2f)
                    )
                }

                HealthIndicatorRow(
                    title = tr(lang, "Asthma & Dust Respiratory Index", "مؤشر الربو والحساسية للغبار"),
                    description = if (lang == AppLanguage.ARABIC) health.respiratoryDustRiskAr else health.respiratoryDustRiskEn,
                    riskLevel = health.respiratoryRiskLevel
                )
                HealthIndicatorRow(
                    title = tr(lang, "Barometric Pressure & Migraine Trigger", "تقلبات الضغط الجوي والصداع النصفي"),
                    description = if (lang == AppLanguage.ARABIC) health.migrainePressureRiskAr else health.migrainePressureRiskEn,
                    riskLevel = health.migraineRiskLevel
                )
                HealthIndicatorRow(
                    title = tr(lang, "Sinus & Joint Comfort", "راحة الجيوب الأنفية والمفاصل"),
                    description = if (lang == AppLanguage.ARABIC) health.sinusAndJointNoteAr else health.sinusAndJointNoteEn,
                    riskLevel = if (bundle.current.humidityPercent < 25) 2 else 1
                )
            }
        }

        // 3. ASTRONOMICAL PRAYER TIMES & WEATHER-PER-PRAYER SCHEDULE
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .border(1.dp, StormViolet.copy(alpha = 0.45f), RoundedCornerShape(26.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(StormViolet.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = StormViolet)
                    }
                    Column {
                        Text(
                            text = tr(
                                lang,
                                "Prayer Times & Weather-Per-Prayer",
                                "مواقيت الصلاة الفلكية وحالة الطقس لكل وقت"
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tr(
                                lang,
                                "Astronomical solar calculation for ${bundle.city.displayName(lang)}",
                                "حساب فلكي شمسي لإحداثيات ${bundle.city.displayName(lang)} مع الحرارة المتوقعة"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(bundle.prayerSchedule) { slot ->
                        PrayerWeatherCard(slot = slot, prefs = prefs)
                    }
                }
            }
        }

        // 4. SMART AGRICULTURE, DATE PALM & IRRIGATION ADVISOR
        item {
            val agri = bundle.agricultureInsight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .border(1.dp, OasisEmerald.copy(alpha = 0.45f), RoundedCornerShape(26.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(OasisEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Agriculture, contentDescription = null, tint = OasisEmerald)
                        }
                        Column {
                            Text(
                                text = tr(
                                    lang,
                                    "Smart Agriculture & Date Palm Advisor",
                                    "المرصد الزراعي الذكي وبساتين النخيل"
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tr(
                                    lang,
                                    "Evapotranspiration (ET₀), irrigation & foliar spraying window",
                                    "معدل التبخر والنتح (ET₀)، جدولة الري، وأوقات الرش الآمنة"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    SmartMetricBox(
                        label = "ET₀",
                        value = "${String.format(Locale.US, "%.1f", agri.evapotranspirationMmDay)} mm",
                        color = OasisEmerald
                    )
                }

                Text(
                    text = "• ${if (lang == AppLanguage.ARABIC) agri.irrigationNeedAr else agri.irrigationNeedEn}\n" +
                        "• ${if (lang == AppLanguage.ARABIC) agri.datePalmStatusAr else agri.datePalmStatusEn}\n" +
                        "• ${if (lang == AppLanguage.ARABIC) agri.sprayingWindowAr else agri.sprayingWindowEn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 5. IRAQ INTER-GOVERNORATE HIGHWAY & CORRIDOR SAFETY MONITOR
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .border(1.dp, GoldenHourOrange.copy(alpha = 0.45f), RoundedCornerShape(26.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GoldenHourOrange.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = GoldenHourOrange)
                    }
                    Column {
                        Text(
                            text = tr(
                                lang,
                                "Iraq Inter-Governorate Highway Monitor",
                                "مرصد أمان الطرق السريعة بين المحافظات العراقية"
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tr(
                                lang,
                                "Visibility, crosswinds & dust monitoring along main corridors",
                                "مدى الرؤية الأفقية والرياح الجانبية والغبار على الطرق الدولية"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                bundle.highwayCorridors.forEach { hwy ->
                    HighwayCorridorRow(corridor = hwy, prefs = prefs)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SmartMetricBox(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HealthIndicatorRow(
    title: String,
    description: String,
    riskLevel: Int
) {
    val (badgeLabelEn, badgeLabelAr, color) = when (riskLevel) {
        4 -> Triple("High Risk", "مرتفع", ExtremeHeatCrimson)
        3 -> Triple("Moderate", "متوسط", ShamalDustOchre)
        2 -> Triple("Mild", "خفيف", SolarAmber)
        else -> Triple("Low / Safe", "آمن / منخفض", OasisEmerald)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = RoundedCornerShape(50),
                color = color.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "$badgeLabelAr • $badgeLabelEn",
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PrayerWeatherCard(
    slot: PrayerWeatherSlot,
    prefs: UserPreferences
) {
    val lang = prefs.language
    Column(
        modifier = Modifier
            .width(115.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .border(1.dp, StormViolet.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = if (lang == AppLanguage.ARABIC) slot.nameAr else slot.nameEn,
            style = MaterialTheme.typography.titleSmall,
            color = StormViolet,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = slot.timeHHMM,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = UnitFormatter.formatTemp(slot.tempC, prefs.tempUnit),
            style = MaterialTheme.typography.titleMedium,
            color = IonCyan,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (lang == AppLanguage.ARABIC) slot.statusAr else slot.statusEn,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun HighwayCorridorRow(
    corridor: HighwayCorridorStatus,
    prefs: UserPreferences
) {
    val lang = prefs.language
    val scoreColor = when {
        corridor.safetyScore >= 80 -> OasisEmerald
        corridor.safetyScore >= 58 -> SolarAmber
        else -> ExtremeHeatCrimson
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (lang == AppLanguage.ARABIC) corridor.nameAr else corridor.nameEn,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${corridor.safetyScore}%",
                style = MaterialTheme.typography.titleMedium,
                color = scoreColor,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = if (lang == AppLanguage.ARABIC) corridor.statusAr else corridor.statusEn,
            style = MaterialTheme.typography.bodySmall,
            color = scoreColor
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "👁️ ${String.format(Locale.US, "%.1f km", corridor.visibilityKm)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "💨 ${UnitFormatter.formatWind(corridor.windKmh, prefs)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "🌫️ ${corridor.dustUgM3.roundToInt()} µg/m³",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
