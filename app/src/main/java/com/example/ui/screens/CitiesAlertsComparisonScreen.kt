package com.example.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.CityLocation
import com.example.data.model.IraqiCitiesCatalog
import com.example.data.model.UserPreferences
import com.example.data.model.WeatherAlert
import com.example.data.model.WeatherBundle
import com.example.ui.components.ChartDataPoint
import com.example.ui.components.InteractiveMeteorologicalChart
import com.example.ui.theme.ExtremeHeatCrimson
import com.example.ui.theme.GoldenHourOrange
import com.example.ui.theme.IonCyan
import com.example.ui.theme.OasisEmerald
import com.example.ui.theme.ShamalDustOchre
import com.example.ui.theme.SolarAmber
import com.example.util.AppStrings.tr
import com.example.util.UnitFormatter
import com.example.util.WeatherNotificationHelper
import kotlin.math.roundToInt

@Composable
fun CitiesAlertsComparisonScreen(
    primaryBundle: WeatherBundle,
    savedCities: List<CityLocation>,
    searchQuery: String,
    searchResults: List<CityLocation>,
    comparisonBundles: Map<String, WeatherBundle>,
    selectedComparisonIds: List<String>,
    prefs: UserPreferences,
    onSearchQueryChange: (String) -> Unit,
    onSelectCity: (CityLocation) -> Unit,
    onToggleFavorite: (CityLocation) -> Unit,
    onToggleComparisonCity: (CityLocation) -> Unit,
    onDetectGps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = prefs.language
    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && primaryBundle.alerts.isNotEmpty()) {
            WeatherNotificationHelper.sendAlertNotification(context, primaryBundle.alerts.first(), lang)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("cities_alerts_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = tr(lang, "Cities, Alerts & Travel Comparison", "المدن والتحذيرات ومقارنة السفر"),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        label = { Text(tr(lang, "Saved Cities & Iraq", "المدن والمحافظات")) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IonCyan.copy(alpha = 0.22f),
                            selectedLabelColor = IonCyan
                        ),
                        modifier = Modifier.testTag("subtab_cities")
                    )
                    FilterChip(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        label = { Text("${tr(lang, "Alerts", "التحذيرات")} (${primaryBundle.alerts.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ShamalDustOchre.copy(alpha = 0.22f),
                            selectedLabelColor = ShamalDustOchre
                        ),
                        modifier = Modifier.testTag("subtab_alerts")
                    )
                    FilterChip(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        label = { Text(tr(lang, "Compare & Travel", "المقارنة والسفر")) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OasisEmerald.copy(alpha = 0.22f),
                            selectedLabelColor = OasisEmerald
                        ),
                        modifier = Modifier.testTag("subtab_compare")
                    )
                }
            }
        }

        when (activeTab) {
            0 -> {
                // SMART LOCATION MANAGEMENT & IRAQ GOVERNORATES DIRECTORY
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            placeholder = {
                                Text(tr(lang, "Search Iraqi or global city...", "ابحث عن محافظة عراقية أو مدينة عالمية..."))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("city_search_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp)
                        )

                        Button(
                            onClick = onDetectGps,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IonCyan),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "GPS",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                val listToDisplay = if (searchQuery.isNotBlank()) searchResults else savedCities +
                    IraqiCitiesCatalog.allCatalogCities.filter { cat -> savedCities.none { it.id == cat.id } }

                items(listToDisplay, key = { it.id }) { city ->
                    val cachedBundle = comparisonBundles[city.id]
                        ?: if (city.id == primaryBundle.city.id) primaryBundle else null
                    val isSelected = city.id == primaryBundle.city.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) IonCyan.copy(alpha = 0.16f)
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.80f)
                            )
                            .border(
                                1.dp,
                                if (isSelected) IonCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { onSelectCity(city) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (city.isIraqiCity) SolarAmber else IonCyan
                            )
                            Column {
                                Text(
                                    text = "${city.nameEn} • ${city.nameAr}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${city.displayRegion(lang)} (${city.elevationMeters}m)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (cachedBundle != null) {
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = UnitFormatter.formatTemp(cachedBundle.current.temperatureC, prefs.tempUnit),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = IonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "AQI ${cachedBundle.airQuality.usAqi}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { onToggleFavorite(city) }) {
                            Icon(
                                imageVector = if (city.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (city.isFavorite) SolarAmber else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            1 -> {
                // SEVERE WEATHER ALERT SYSTEM
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr(lang, "Active Environmental Bulletins", "النشرات والتحذيرات البيئية النشطة"),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Button(
                            onClick = {
                                val topAlert = primaryBundle.alerts.firstOrNull() ?: return@Button
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    val sent = WeatherNotificationHelper.sendAlertNotification(context, topAlert, lang)
                                    Toast.makeText(
                                        context,
                                        if (sent) "Alert notification dispatched" else "Notifications disabled",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ShamalDustOchre),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("dispatch_alert_notification_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(tr(lang, "Send Alert Push", "إرسال إشعار تحذيري"))
                        }
                    }
                }

                items(primaryBundle.alerts, key = { it.id }) { alert ->
                    SevereAlertDetailCard(alert = alert, lang = lang)
                }
            }

            2 -> {
                // WEATHER COMPARISON & TRAVEL WEATHER PLANNER
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CompareArrows, contentDescription = null, tint = OasisEmerald)
                            Text(
                                text = tr(lang, "Select Cities to Compare (2–4)", "اختر المدن للمقارنة المتزامنة (2–4)"),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(IraqiCitiesCatalog.allCatalogCities) { city ->
                                val isSel = selectedComparisonIds.contains(city.id)
                                FilterChip(
                                    selected = isSel,
                                    onClick = { onToggleComparisonCity(city) },
                                    label = { Text(city.displayName(lang)) }
                                )
                            }
                        }
                    }
                }

                val activeCompBundles = selectedComparisonIds.mapNotNull { comparisonBundles[it] }
                if (activeCompBundles.isNotEmpty()) {
                    // Synchronized Temperature & Dust Comparison Chart
                    item {
                        val chartPts = activeCompBundles.map { b ->
                            ChartDataPoint(
                                label = b.city.displayName(lang),
                                primaryValue = UnitFormatter.convertTempValue(b.current.temperatureC, prefs.tempUnit).toFloat(),
                                secondaryValue = UnitFormatter.convertTempValue(b.current.maxTempC, prefs.tempUnit).toFloat(),
                                tooltipSubtitle = "Dust ${b.airQuality.dustUgM3.roundToInt()} µg/m³ • Comfort ${b.comfort.overallScore}/100"
                            )
                        }
                        InteractiveMeteorologicalChart(
                            title = tr(lang, "Multi-City Temperature Comparison", "مقارنة درجات الحرارة بين المدن"),
                            subtitle = tr(lang, "Current temperature vs daily high across selected cities", "الحرارة الحالية مقارنة بالعظمى اليومية"),
                            points = chartPts,
                            unitSymbol = prefs.tempUnit.symbol,
                            primaryColor = IonCyan,
                            secondaryColor = SolarAmber,
                            showBars = true
                        )
                    }

                    // Side-by-Side Travel Weather Planner Cards
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = IonCyan)
                            Text(
                                text = tr(lang, "Travel Weather Planner & Destination Matrix", "مخطط طقس السفر ومصفوفة الوجهات"),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    items(activeCompBundles, key = { "comp_${it.city.id}" }) { b ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${b.city.nameEn} • ${b.city.nameAr}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (lang == AppLanguage.ARABIC) b.current.descriptionAr else b.current.descriptionEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = IonCyan
                                    )
                                }
                                Text(
                                    text = UnitFormatter.formatTemp(b.current.temperatureC, prefs.tempUnit),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = SolarAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${tr(lang, "High/Low", "عظمى/صغرى")}: ${UnitFormatter.formatTemp(b.current.maxTempC, prefs.tempUnit)}/${UnitFormatter.formatTemp(b.current.minTempC, prefs.tempUnit)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${tr(lang, "Wind", "الرياح")}: ${UnitFormatter.formatWind(b.current.windSpeedKmh, prefs)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "AQI ${b.airQuality.usAqi} • ${tr(lang, "Comfort", "الراحة")} ${b.comfort.overallScore}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OasisEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SevereAlertDetailCard(alert: WeatherAlert, lang: AppLanguage) {
    val color = when (alert.severity.level) {
        4 -> ExtremeHeatCrimson
        3 -> ShamalDustOchre
        2 -> GoldenHourOrange
        else -> SolarAmber
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .border(1.5.dp, color.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = color)
                Text(
                    text = if (lang == AppLanguage.ARABIC) alert.titleAr else alert.titleEn,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = color.copy(alpha = 0.2f)
            ) {
                Text(
                    text = if (lang == AppLanguage.ARABIC) alert.severity.labelAr else alert.severity.labelEn,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Text(
            text = "${tr(lang, "Area", "المنطقة")}: ${if (lang == AppLanguage.ARABIC) alert.areaAr else alert.areaEn}  •  ${alert.metricTriggerText}",
            style = MaterialTheme.typography.labelMedium,
            color = color
        )

        Text(
            text = if (lang == AppLanguage.ARABIC) alert.summaryAr else alert.summaryEn,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        val precautions = if (lang == AppLanguage.ARABIC) alert.precautionsAr else alert.precautionsEn
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = tr(lang, "Recommended Safety Precautions:", "إرشادات السلامة الموصى بها:"),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            precautions.forEach { item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "${tr(lang, "Source", "المصدر")}: ${if (lang == AppLanguage.ARABIC) alert.authorityAr else alert.authorityEn}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
