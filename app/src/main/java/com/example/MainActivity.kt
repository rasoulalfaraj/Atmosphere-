package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeMode
import com.example.data.model.WeatherConditionType
import com.example.ui.AtmosphereViewModel
import com.example.ui.WeatherUiState
import com.example.ui.components.AtmosphericCanvasBackground
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AirQualityAndAnalyticsScreen
import com.example.ui.screens.CitiesAlertsComparisonScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ForecastScreen
import com.example.ui.screens.RadarScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmartAdvisorsScreen
import com.example.ui.theme.AtmosphereAITheme
import com.example.ui.theme.IonCyan
import com.example.ui.theme.SolarAmber
import com.example.util.AppStrings.tr
import com.example.util.configureAppCheck

enum class AtmosphereDestination(
    val route: String,
    val icon: ImageVector,
    val labelEn: String,
    val labelAr: String
) {
    DASHBOARD("dashboard", Icons.Default.Dashboard, "Home", "الرئيسية"),
    FORECASTS("forecasts", Icons.Default.CalendarMonth, "Forecasts", "التوقعات"),
    RADAR("radar", Icons.Default.Radar, "Radar", "الرادار"),
    SMART_ADVISORS("smart_advisors", Icons.Default.SolarPower, "Smart Hub", "المستشار"),
    AI_ASSISTANT("ai_assistant", Icons.Default.AutoAwesome, "AI Weather", "الذكاء"),
    AIR_ANALYTICS("air_analytics", Icons.Default.Analytics, "AQI & Charts", "البيئة"),
    CITIES_ALERTS("cities_alerts", Icons.Default.LocationCity, "Cities/Alerts", "المدن"),
    SETTINGS("settings", Icons.Default.Settings, "Settings", "الإعدادات")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before any UI or ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            val viewModel: AtmosphereViewModel = viewModel()
            val prefs by viewModel.preferences.collectAsStateWithLifecycle()

            AtmosphereAITheme(
                themeMode = prefs.themeMode,
                language = prefs.language
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AtmosphereAppRoot(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AtmosphereAppRoot(viewModel: AtmosphereViewModel) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
    val savedCities by viewModel.savedCities.collectAsStateWithLifecycle()
    val comparisonBundles by viewModel.comparisonBundles.collectAsStateWithLifecycle()
    val selectedComparisonIds by viewModel.selectedComparisonCityIds.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val gpsMessage by viewModel.gpsStatusMessage.collectAsStateWithLifecycle()

    val aiBriefingState by viewModel.aiBriefingState.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiStreaming by viewModel.isAiStreaming.collectAsStateWithLifecycle()

    val selectedRadarLayer by viewModel.selectedRadarLayer.collectAsStateWithLifecycle()
    val radarFrameIndex by viewModel.radarFrameIndex.collectAsStateWithLifecycle()
    val isRadarPlaying by viewModel.isRadarPlaying.collectAsStateWithLifecycle()

    var currentDestination by rememberSaveable { mutableStateOf(AtmosphereDestination.DASHBOARD) }

    // Ensure Back press from any secondary screen returns to Home Dashboard
    BackHandler(enabled = currentDestination != AtmosphereDestination.DASHBOARD) {
        currentDestination = AtmosphereDestination.DASHBOARD
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.detectGpsLocation()
    }

    val activeBundle = when (val state = weatherState) {
        is WeatherUiState.Success -> state.bundle
        is WeatherUiState.Error -> state.fallbackBundle
        is WeatherUiState.Loading -> null
    }

    val conditionType = activeBundle?.current?.conditionType ?: WeatherConditionType.CLEAR_DAY
    val isDark = when (prefs.themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.AUTO -> isSystemInDarkTheme()
    }
    val animateBg = !prefs.reducedMotion && !prefs.batterySaverMode

    // Primary 5 bottom bar tabs including the new Smart Advisors Hub
    val bottomDestinations = listOf(
        AtmosphereDestination.DASHBOARD,
        AtmosphereDestination.FORECASTS,
        AtmosphereDestination.RADAR,
        AtmosphereDestination.SMART_ADVISORS,
        AtmosphereDestination.AI_ASSISTANT
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AtmosphericCanvasBackground(
            conditionType = conditionType,
            isDarkTheme = isDark,
            animateMotion = animateBg
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= 600.dp

            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
                topBar = {
                    AtmosphereTopBar(
                        lang = prefs.language,
                        currentDestination = currentDestination,
                        onToggleLanguage = {
                            val next = if (prefs.language == AppLanguage.ARABIC) AppLanguage.ENGLISH else AppLanguage.ARABIC
                            viewModel.setLanguage(next)
                        },
                        onOpenAirAnalytics = { currentDestination = AtmosphereDestination.AIR_ANALYTICS },
                        onOpenCitiesAlerts = { currentDestination = AtmosphereDestination.CITIES_ALERTS },
                        onOpenSettings = { currentDestination = AtmosphereDestination.SETTINGS }
                    )
                },
                bottomBar = {
                    if (!isExpandedScreen) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            tonalElevation = 8.dp
                        ) {
                            bottomDestinations.forEach { dest ->
                                val selected = currentDestination == dest
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentDestination = dest },
                                    icon = {
                                        Icon(
                                            imageVector = dest.icon,
                                            contentDescription = dest.labelEn
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = if (prefs.language == AppLanguage.ARABIC) dest.labelAr else dest.labelEn,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = IonCyan,
                                        selectedTextColor = IonCyan,
                                        indicatorColor = IonCyan.copy(alpha = 0.16f)
                                    ),
                                    modifier = Modifier.testTag("nav_${dest.route}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isExpandedScreen) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            AtmosphereDestination.entries.forEach { dest ->
                                val selected = currentDestination == dest
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { currentDestination = dest },
                                    icon = { Icon(dest.icon, contentDescription = dest.labelEn) },
                                    label = {
                                        Text(if (prefs.language == AppLanguage.ARABIC) dest.labelAr else dest.labelEn)
                                    },
                                    modifier = Modifier.testTag("rail_${dest.route}")
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        when {
                            activeBundle == null && weatherState is WeatherUiState.Loading -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = IonCyan)
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = tr(
                                            prefs.language,
                                            "Synchronizing Atmospheric Telemetry...",
                                            "جارٍ مزامنة بيانات الرصد الجوي..."
                                        ),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }

                            activeBundle == null && weatherState is WeatherUiState.Error -> {
                                val err = (weatherState as WeatherUiState.Error).message
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = err,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(onClick = { viewModel.refreshCurrentCity() }) {
                                        Text(tr(prefs.language, "Retry", "إعادة المحاولة"))
                                    }
                                }
                            }

                            activeBundle != null -> {
                                when (currentDestination) {
                                    AtmosphereDestination.DASHBOARD -> DashboardScreen(
                                        bundle = activeBundle,
                                        savedCities = savedCities,
                                        prefs = prefs,
                                        gpsStatusMessage = gpsMessage,
                                        onSelectCity = { viewModel.loadWeatherForCity(it) },
                                        onRequestGps = {
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        },
                                        onRefresh = { viewModel.refreshCurrentCity() },
                                        onOpenForecasts = { currentDestination = AtmosphereDestination.FORECASTS },
                                        onOpenAlertsAndCities = { currentDestination = AtmosphereDestination.CITIES_ALERTS },
                                        onOpenAiAssistant = { currentDestination = AtmosphereDestination.AI_ASSISTANT },
                                        onOpenAirQuality = { currentDestination = AtmosphereDestination.AIR_ANALYTICS },
                                        onOpenSmartAdvisors = { currentDestination = AtmosphereDestination.SMART_ADVISORS }
                                    )

                                    AtmosphereDestination.FORECASTS -> ForecastScreen(
                                        bundle = activeBundle,
                                        prefs = prefs
                                    )

                                    AtmosphereDestination.RADAR -> RadarScreen(
                                        bundle = activeBundle,
                                        comparisonBundles = comparisonBundles,
                                        selectedLayer = selectedRadarLayer,
                                        frameIndex = radarFrameIndex,
                                        isPlaying = isRadarPlaying,
                                        prefs = prefs,
                                        onSelectLayer = { viewModel.selectRadarLayer(it) },
                                        onFrameChange = { viewModel.setRadarFrameIndex(it) },
                                        onTogglePlay = { viewModel.toggleRadarPlayback(it) },
                                        onSelectCityFromMap = { city ->
                                            viewModel.loadWeatherForCity(city)
                                        }
                                    )

                                    AtmosphereDestination.SMART_ADVISORS -> SmartAdvisorsScreen(
                                        bundle = activeBundle,
                                        prefs = prefs
                                    )

                                    AtmosphereDestination.AIR_ANALYTICS -> AirQualityAndAnalyticsScreen(
                                        bundle = activeBundle,
                                        prefs = prefs,
                                        onSelectAqiStandard = { viewModel.setAqiStandard(it) }
                                    )

                                    AtmosphereDestination.AI_ASSISTANT -> AiAssistantScreen(
                                        bundle = activeBundle,
                                        prefs = prefs,
                                        briefingState = aiBriefingState,
                                        chatMessages = chatMessages,
                                        isStreaming = isAiStreaming,
                                        onGenerateBriefing = { viewModel.generateAiDailyBriefing() },
                                        onAskQuestion = { viewModel.askAiWeatherQuestion(it) }
                                    )

                                    AtmosphereDestination.CITIES_ALERTS -> CitiesAlertsComparisonScreen(
                                        primaryBundle = activeBundle,
                                        savedCities = savedCities,
                                        searchQuery = searchQuery,
                                        searchResults = searchResults,
                                        comparisonBundles = comparisonBundles,
                                        selectedComparisonIds = selectedComparisonIds,
                                        prefs = prefs,
                                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                        onSelectCity = { city ->
                                            viewModel.loadWeatherForCity(city)
                                            currentDestination = AtmosphereDestination.DASHBOARD
                                        },
                                        onToggleFavorite = { viewModel.toggleFavoriteCity(it) },
                                        onToggleComparisonCity = { viewModel.toggleComparisonCity(it) },
                                        onDetectGps = {
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                    )

                                    AtmosphereDestination.SETTINGS -> SettingsScreen(
                                        bundle = activeBundle,
                                        prefs = prefs,
                                        onLanguageChange = { viewModel.setLanguage(it) },
                                        onThemeChange = { viewModel.setThemeMode(it) },
                                        onTempUnitChange = { viewModel.setTempUnit(it) },
                                        onWindUnitChange = { viewModel.setWindUnit(it) },
                                        onPressureUnitChange = { viewModel.setPressureUnit(it) },
                                        onPrecipUnitChange = { viewModel.setPrecipUnit(it) },
                                        onReducedMotionChange = { viewModel.setReducedMotion(it) },
                                        onBatterySaverChange = { viewModel.setBatterySaver(it) },
                                        onNotificationsChange = { viewModel.setNotificationsEnabled(it) },
                                        onClearOfflineCache = { viewModel.clearOfflineWeatherCache() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AtmosphereTopBar(
    lang: AppLanguage,
    currentDestination: AtmosphereDestination,
    onToggleLanguage: () -> Unit,
    onOpenAirAnalytics: () -> Unit,
    onOpenCitiesAlerts: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "ATMOSPHERE AI",
                style = MaterialTheme.typography.titleMedium,
                color = IonCyan,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (lang == AppLanguage.ARABIC) "• الطقس والبيئة" else "• Weather IQ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Prominent Bilingual Pill Button (عربي / EN)
            Surface(
                shape = RoundedCornerShape(50),
                color = SolarAmber.copy(alpha = 0.2f),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, SolarAmber, RoundedCornerShape(50))
                    .clickable { onToggleLanguage() }
                    .testTag("top_language_toggle")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = SolarAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (lang == AppLanguage.ARABIC) "EN / عربي" else "عربي / EN",
                        style = MaterialTheme.typography.labelSmall,
                        color = SolarAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Air Quality & Analytics Button
            IconButton(
                onClick = onOpenAirAnalytics,
                modifier = Modifier.testTag("top_air_analytics_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = tr(lang, "AQI & Analytics", "جودة الهواء والرسوم"),
                    tint = if (currentDestination == AtmosphereDestination.AIR_ANALYTICS) IonCyan else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Cities, Travel & Severe Alerts Hub Button
            IconButton(
                onClick = onOpenCitiesAlerts,
                modifier = Modifier.testTag("top_cities_alerts_button")
            ) {
                Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = tr(lang, "Cities, Alerts & Comparison", "المدن والتحذيرات والمقارنة"),
                    tint = if (currentDestination == AtmosphereDestination.CITIES_ALERTS) IonCyan else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Settings & Widgets Button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("top_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = tr(lang, "Settings", "الإعدادات"),
                    tint = if (currentDestination == AtmosphereDestination.SETTINGS) IonCyan else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
