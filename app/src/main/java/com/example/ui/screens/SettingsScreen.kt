package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeMode
import com.example.data.model.PrecipUnit
import com.example.data.model.PressureUnit
import com.example.data.model.TempUnit
import com.example.data.model.UserPreferences
import com.example.data.model.WeatherBundle
import com.example.data.model.WindUnit
import com.example.ui.theme.IonCyan
import com.example.ui.theme.OasisEmerald
import com.example.ui.theme.SolarAmber
import com.example.util.AppStrings.tr
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    bundle: WeatherBundle,
    prefs: UserPreferences,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeChange: (AppThemeMode) -> Unit,
    onTempUnitChange: (TempUnit) -> Unit,
    onWindUnitChange: (WindUnit) -> Unit,
    onPressureUnitChange: (PressureUnit) -> Unit,
    onPrecipUnitChange: (PrecipUnit) -> Unit,
    onReducedMotionChange: (Boolean) -> Unit,
    onBatterySaverChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onClearOfflineCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = prefs.language
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = tr(lang, "Settings, Widgets & Personalization", "الإعدادات والتخصيص والودجت"),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // 1. Language & RTL/LTR Toggle (Arabic Iraq & English)
        item {
            SettingsCard(title = tr(lang, "Language & Regional Layout (RTL / LTR)", "اللغة واتجاه الواجهة (العربية / الإنجليزية)")) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppLanguage.entries.forEach { item ->
                        FilterChip(
                            selected = prefs.language == item,
                            onClick = { onLanguageChange(item) },
                            label = { Text(if (lang == AppLanguage.ARABIC) item.labelAr else item.labelEn) },
                            modifier = Modifier.testTag("lang_chip_${item.code}")
                        )
                    }
                }
            }
        }

        // 2. Theme Appearance (Atmospheric Dark, Daylight Crisp, System Auto)
        item {
            SettingsCard(title = tr(lang, "Atmospheric Theme Mode", "مظهر التطبيق")) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = prefs.themeMode == mode,
                            onClick = { onThemeChange(mode) },
                            label = { Text(if (lang == AppLanguage.ARABIC) mode.labelAr else mode.labelEn) }
                        )
                    }
                }
            }
        }

        // 3. Meteorological Units (Celsius default, km/h default, hPa, mm)
        item {
            SettingsCard(title = tr(lang, "Meteorological Measurement Units", "وحدات القياس الجوية")) {
                Text(
                    text = tr(lang, "Temperature Unit", "وحدة الحرارة"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TempUnit.entries.forEach { u ->
                        FilterChip(
                            selected = prefs.tempUnit == u,
                            onClick = { onTempUnitChange(u) },
                            label = { Text(if (lang == AppLanguage.ARABIC) u.labelAr else u.labelEn) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tr(lang, "Wind Speed Unit", "وحدة سرعة الرياح"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WindUnit.entries.forEach { u ->
                        FilterChip(
                            selected = prefs.windUnit == u,
                            onClick = { onWindUnitChange(u) },
                            label = { Text(u.symbolEn) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = tr(lang, "Pressure", "الضغط الجوي"),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PressureUnit.entries.forEach { u ->
                                FilterChip(
                                    selected = prefs.pressureUnit == u,
                                    onClick = { onPressureUnitChange(u) },
                                    label = { Text(u.symbol) }
                                )
                            }
                        }
                    }

                    Column {
                        Text(
                            text = tr(lang, "Precipitation", "الهطول المطري"),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PrecipUnit.entries.forEach { u ->
                                FilterChip(
                                    selected = prefs.precipUnit == u,
                                    onClick = { onPrecipUnitChange(u) },
                                    label = { Text(u.symbol) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Accessibility, Battery Saver & Notification Toggles
        item {
            SettingsCard(title = tr(lang, "Performance, Accessibility & Alerts", "الأداء وإمكانية الوصول والإشعارات")) {
                ToggleSettingRow(
                    title = tr(lang, "Severe Weather & Dust Notifications", "إشعارات الطقس الشديد والعواصف الترابية"),
                    subtitle = tr(lang, "Alert when heat index ≥44°C or PM10 dust spikes", "تنبيه عند ارتفاع مؤشر الحر أو موجات الغبار"),
                    checked = prefs.notificationsEnabled,
                    onCheckedChange = onNotificationsChange
                )
                ToggleSettingRow(
                    title = tr(lang, "Reduced Motion (Accessibility)", "تقليل الحركة (إمكانية الوصول)"),
                    subtitle = tr(lang, "Pauses continuous background particle animations", "إيقاف حركات الخلفية المستمرة لراحة العين"),
                    checked = prefs.reducedMotion,
                    onCheckedChange = onReducedMotionChange
                )
                ToggleSettingRow(
                    title = tr(lang, "Battery-Saving Mode", "وضع توفير الطاقة والبطارية"),
                    subtitle = tr(lang, "Optimizes background refresh and canvas frame rate", "تقليل استهلاك الطاقة أثناء العمل في الخلفية"),
                    checked = prefs.batterySaverMode,
                    onCheckedChange = onBatterySaverChange
                )
            }
        }

        // 5. Home Screen Weather Widget Live Preview & Setup Guide
        item {
            SettingsCard(title = tr(lang, "Android Home Screen Weather Widget", "ودجت الطقس للشاشة الرئيسية")) {
                Text(
                    text = tr(
                        lang,
                        "Atmosphere AI includes a native Android AppWidget (AtmosphereWeatherWidgetReceiver). Long-press your Android home screen → Widgets → Atmosphere AI to pin it.",
                        "يتضمن التطبيق ودجت أندرويد أصلي للشاشة الرئيسية. اضغط مطولاً على شاشة الهاتف ← الودجت ← Atmosphere AI لإضافته."
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Live Preview matching the RemoteViews widget
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0B132B), Color(0xFF1B2A4A))
                            )
                        )
                        .border(1.dp, IonCyan.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${bundle.city.nameEn} • ${bundle.city.nameAr}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${bundle.current.descriptionEn} • Feels ${bundle.current.feelsLikeC.roundToInt()}°C",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB0C4DE)
                            )
                        }
                        Text(
                            text = "${bundle.current.temperatureC.roundToInt()}°C",
                            style = MaterialTheme.typography.displaySmall,
                            color = IonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "H: ${bundle.current.maxTempC.roundToInt()}° L: ${bundle.current.minTempC.roundToInt()}°  •  Wind ${bundle.current.windSpeedKmh.roundToInt()} km/h  •  AQI ${bundle.airQuality.usAqi}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }
        }

        // 6. Offline Room Database Cache & Data Provider Transparency
        item {
            SettingsCard(title = tr(lang, "Offline Room Cache & Data Provenance", "التخزين دون اتصال ومصادر البيانات")) {
                Text(
                    text = tr(
                        lang,
                        "• Weather & 14-Day Forecast: Open-Meteo High-Resolution Synoptic API (Real-Time)\n" +
                            "• Air Quality & Mineral Dust: CAMS / Open-Meteo Environmental API (US EPA & EAQI)\n" +
                            "• Observed Weather Radar: RainViewer Global Weather Radar API + GIS Synoptic Canvas\n" +
                            "• AI Weather Intelligence: Firebase AI Logic (Gemini 3.8 Flash) strictly grounded in retrieved telemetry\n" +
                            "• Offline Persistence: Android Room SQLite Database + Jetpack DataStore",
                        "• الطقس والتوقعات: واجهة Open-Meteo عالية الدقة\n" +
                            "• جودة الهواء والغبار: نموذج CAMS / Open-Meteo البيئي\n" +
                            "• رادار الأمطار: RainViewer + طبقات نظم المعلومات الجغرافية\n" +
                            "• الذكاء الاصطناعي: Firebase AI Logic (Gemini 3.8 Flash)\n" +
                            "• العمل دون إنترنت: قاعدة بيانات Room SQLite المحلية"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = {
                        onClearOfflineCache()
                        Toast.makeText(
                            context,
                            if (lang == AppLanguage.ARABIC) "تم مسح ذاكرة التخزين المؤقت" else "Offline weather cache cleared",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolarAmber),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = tr(lang, "Purge & Refresh Offline Room Cache", "مسح وتحديث الذاكرة المؤقتة"),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = IonCyan,
            fontWeight = FontWeight.Bold
        )
        content()
    }
}

@Composable
private fun ToggleSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
