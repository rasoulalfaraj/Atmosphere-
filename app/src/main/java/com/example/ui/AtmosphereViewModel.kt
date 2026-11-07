package com.example.ui

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AtmosphereDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeMode
import com.example.data.model.AqiStandard
import com.example.data.model.CityLocation
import com.example.data.model.IraqiCitiesCatalog
import com.example.data.model.PrecipUnit
import com.example.data.model.PressureUnit
import com.example.data.model.RadarLayerType
import com.example.data.model.TempUnit
import com.example.data.model.UserPreferences
import com.example.data.model.WeatherBundle
import com.example.data.model.WindUnit
import com.example.data.repository.AiDailyIntelligence
import com.example.data.repository.AiWeatherService
import com.example.data.repository.WeatherRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.hypot
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "AtmosphereViewModel"

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val bundle: WeatherBundle) : WeatherUiState
    data class Error(val message: String, val fallbackBundle: WeatherBundle? = null) : WeatherUiState
}

sealed interface AiBriefingUiState {
    data object Idle : AiBriefingUiState
    data object Loading : AiBriefingUiState
    data class Success(val intelligence: AiDailyIntelligence) : AiBriefingUiState
    data class Error(val message: String) : AiBriefingUiState
}

data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val isStreaming: Boolean = false,
    val isError: Boolean = false
)

class AtmosphereViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AtmosphereDatabase.getInstance(application)
    private val repository = WeatherRepository(application, database.weatherDao())
    private val prefsManager = PreferencesManager(application)
    private val aiService = AiWeatherService()

    val preferences: StateFlow<UserPreferences> = prefsManager.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences()
    )

    val savedCities: StateFlow<List<CityLocation>> = repository.savedCitiesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IraqiCitiesCatalog.iraqiCities.take(8)
    )

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    private val _comparisonBundles = MutableStateFlow<Map<String, WeatherBundle>>(emptyMap())
    val comparisonBundles: StateFlow<Map<String, WeatherBundle>> = _comparisonBundles.asStateFlow()

    private val _selectedComparisonCityIds = MutableStateFlow(listOf("iq_baghdad", "iq_basra", "iq_erbil", "iq_nasiriyah"))
    val selectedComparisonCityIds: StateFlow<List<String>> = _selectedComparisonCityIds.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<CityLocation>>(IraqiCitiesCatalog.allCatalogCities)
    val searchResults: StateFlow<List<CityLocation>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _gpsStatusMessage = MutableStateFlow<String?>(null)
    val gpsStatusMessage: StateFlow<String?> = _gpsStatusMessage.asStateFlow()

    // AI Weather Intelligence States
    private val _aiBriefingState = MutableStateFlow<AiBriefingUiState>(AiBriefingUiState.Idle)
    val aiBriefingState: StateFlow<AiBriefingUiState> = _aiBriefingState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<AiChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiStreaming = MutableStateFlow(false)
    val isAiStreaming: StateFlow<Boolean> = _isAiStreaming.asStateFlow()

    // Radar States
    private val _selectedRadarLayer = MutableStateFlow(RadarLayerType.PRECIPITATION_RADAR)
    val selectedRadarLayer: StateFlow<RadarLayerType> = _selectedRadarLayer.asStateFlow()

    private val _radarFrameIndex = MutableStateFlow(0)
    val radarFrameIndex: StateFlow<Int> = _radarFrameIndex.asStateFlow()

    private val _isRadarPlaying = MutableStateFlow(false)
    val isRadarPlaying: StateFlow<Boolean> = _isRadarPlaying.asStateFlow()

    private var radarPlaybackJob: Job? = null
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureDefaultCitiesSeeded()
            val initialPrefs = prefsManager.preferencesFlow.first()
            val initialCity = IraqiCitiesCatalog.findById(initialPrefs.selectedCityId)
            loadWeatherForCity(initialCity, forceRefresh = false)
            preloadComparisonCities()
        }
    }

    fun loadWeatherForCity(city: CityLocation, forceRefresh: Boolean = true) {
        viewModelScope.launch {
            if (_weatherState.value !is WeatherUiState.Success) {
                _weatherState.value = WeatherUiState.Loading
            }
            try {
                prefsManager.setSelectedCityId(city.id)
                repository.saveOrUpdateCity(city)
                val bundle = repository.fetchWeatherForCity(city, forceRefresh = forceRefresh)
                _weatherState.value = WeatherUiState.Success(bundle)
                _comparisonBundles.value = _comparisonBundles.value + (city.id to bundle)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error loading weather for ${city.nameEn}", e)
                val currentBundle = (_weatherState.value as? WeatherUiState.Success)?.bundle
                _weatherState.value = WeatherUiState.Error(
                    message = e.localizedMessage ?: "Failed to retrieve meteorological data.",
                    fallbackBundle = currentBundle
                )
            }
        }
    }

    fun refreshCurrentCity() {
        val currentCity = (_weatherState.value as? WeatherUiState.Success)?.bundle?.city
            ?: IraqiCitiesCatalog.findById(preferences.value.selectedCityId)
        loadWeatherForCity(currentCity, forceRefresh = true)
        preloadComparisonCities()
    }

    fun preloadComparisonCities() {
        viewModelScope.launch {
            val ids = _selectedComparisonCityIds.value
            for (id in ids) {
                val city = IraqiCitiesCatalog.allCatalogCities.firstOrNull { it.id == id }
                    ?: savedCities.value.firstOrNull { it.id == id }
                    ?: continue
                runCatching {
                    val b = repository.fetchWeatherForCity(city, forceRefresh = false)
                    _comparisonBundles.value = _comparisonBundles.value + (city.id to b)
                }
            }
        }
    }

    fun toggleComparisonCity(city: CityLocation) {
        val currentList = _selectedComparisonCityIds.value.toMutableList()
        if (currentList.contains(city.id)) {
            if (currentList.size > 2) currentList.remove(city.id)
        } else {
            if (currentList.size >= 4) currentList.removeAt(0)
            currentList.add(city.id)
        }
        _selectedComparisonCityIds.value = currentList
        viewModelScope.launch {
            if (!_comparisonBundles.value.containsKey(city.id)) {
                runCatching {
                    val b = repository.fetchWeatherForCity(city, forceRefresh = false)
                    _comparisonBundles.value = _comparisonBundles.value + (city.id to b)
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(250)
            val results = repository.searchCities(query, preferences.value.language.code)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun toggleFavoriteCity(city: CityLocation) {
        viewModelScope.launch {
            repository.saveOrUpdateCity(city, isFavorite = !city.isFavorite)
        }
    }

    fun removeSavedCity(cityId: String) {
        viewModelScope.launch {
            repository.removeSavedCity(cityId)
        }
    }

    @SuppressLint("MissingPermission")
    fun detectGpsLocation() {
        val context = getApplication<Application>()
        val client = LocationServices.getFusedLocationProviderClient(context)
        _gpsStatusMessage.value = if (preferences.value.language == AppLanguage.ARABIC) {
            "جارٍ تحديد الموقع الجغرافي عبر GPS..."
        } else {
            "Acquiring GPS coordinates..."
        }

        val tokenSource = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    applyDetectedCoordinates(location.latitude, location.longitude)
                } else {
                    client.lastLocation.addOnSuccessListener { lastLoc ->
                        if (lastLoc != null) {
                            applyDetectedCoordinates(lastLoc.latitude, lastLoc.longitude)
                        } else {
                            // Default fallback when emulator GPS has no fix set
                            _gpsStatusMessage.value = if (preferences.value.language == AppLanguage.ARABIC) {
                                "تعذر التقاط إشارة GPS في المحاكي؛ تم اختيار بغداد"
                            } else {
                                "No active GPS fix on emulator; centered on Baghdad"
                            }
                            loadWeatherForCity(IraqiCitiesCatalog.iraqiCities.first(), forceRefresh = true)
                        }
                    }
                }
            }
            .addOnFailureListener { err ->
                Log.w(TAG, "GPS detection failed", err)
                _gpsStatusMessage.value = err.localizedMessage ?: "Location unavailable"
            }
    }

    private fun applyDetectedCoordinates(lat: Double, lon: Double) {
        // Find closest catalog city if within ~0.6 degrees, otherwise create a custom GPS location
        val nearest = IraqiCitiesCatalog.allCatalogCities.minByOrNull {
            hypot(it.latitude - lat, it.longitude - lon)
        }
        val dist = nearest?.let { hypot(it.latitude - lat, it.longitude - lon) } ?: 999.0
        val targetCity = if (nearest != null && dist < 0.65) {
            nearest
        } else {
            CityLocation(
                id = "gps_${(lat * 100).toInt()}_${(lon * 100).toInt()}",
                nameEn = "Current GPS Location",
                nameAr = "موقعي الحالي (GPS)",
                governorateEn = String.format(java.util.Locale.US, "%.2f°N, %.2f°E", lat, lon),
                governorateAr = String.format(java.util.Locale.US, "%.2f° شمالاً، %.2f° شرقاً", lat, lon),
                countryEn = "Live Telemetry",
                countryAr = "رصد مباشر",
                latitude = lat,
                longitude = lon,
                isIraqiCity = lat in 29.0..37.5 && lon in 38.5..49.0,
                isFavorite = true
            )
        }
        _gpsStatusMessage.value = if (preferences.value.language == AppLanguage.ARABIC) {
            "تم التحديث حسب الموقع: ${targetCity.nameAr}"
        } else {
            "Locked onto: ${targetCity.nameEn}"
        }
        loadWeatherForCity(targetCity, forceRefresh = true)
    }

    fun clearGpsMessage() {
        _gpsStatusMessage.value = null
    }

    // AI Weather Intelligence
    fun generateAiDailyBriefing() {
        val bundle = (_weatherState.value as? WeatherUiState.Success)?.bundle ?: return
        viewModelScope.launch {
            _aiBriefingState.value = AiBriefingUiState.Loading
            val result = aiService.generateDailyIntelligence(bundle, preferences.value.language)
            result.fold(
                onSuccess = { intel ->
                    _aiBriefingState.value = AiBriefingUiState.Success(intel)
                },
                onFailure = { err ->
                    _aiBriefingState.value = AiBriefingUiState.Error(
                        err.localizedMessage ?: "Failed to generate AI briefing."
                    )
                }
            )
        }
    }

    fun askAiWeatherQuestion(question: String) {
        val trimmed = question.trim()
        if (trimmed.isEmpty() || _isAiStreaming.value) return
        val bundle = (_weatherState.value as? WeatherUiState.Success)?.bundle ?: return

        // If user mentions Nasiriyah, Basra, Erbil, etc., make sure comparison bundles are loaded
        val userMsgId = "user_${System.currentTimeMillis()}"
        val aiMsgId = "ai_${System.currentTimeMillis() + 1}"

        _chatMessages.value = _chatMessages.value +
            AiChatMessage(id = userMsgId, isUser = true, text = trimmed) +
            AiChatMessage(id = aiMsgId, isUser = false, text = "", isStreaming = true)

        viewModelScope.launch {
            _isAiStreaming.value = true
            try {
                // Ensure Nasiriyah & Basra are in comparison context for Iraq questions
                val nasiriyah = IraqiCitiesCatalog.findById("iq_nasiriyah")
                if (!_comparisonBundles.value.containsKey(nasiriyah.id)) {
                    runCatching {
                        val nb = repository.fetchWeatherForCity(nasiriyah, forceRefresh = false)
                        _comparisonBundles.value = _comparisonBundles.value + (nasiriyah.id to nb)
                    }
                }

                var finalAnswer = ""
                aiService.streamWeatherQuestion(
                    question = trimmed,
                    primaryBundle = bundle,
                    comparisonBundles = _comparisonBundles.value.values.toList(),
                    language = preferences.value.language
                ).collect { partialText ->
                    finalAnswer = partialText
                    _chatMessages.value = _chatMessages.value.map { msg ->
                        if (msg.id == aiMsgId) {
                            msg.copy(text = partialText, isStreaming = true)
                        } else {
                            msg
                        }
                    }
                }

                if (finalAnswer.isBlank()) {
                    _chatMessages.value = _chatMessages.value.map { msg ->
                        if (msg.id == aiMsgId) {
                            msg.copy(
                                text = "Model returned an empty response.",
                                isStreaming = false,
                                isError = true
                            )
                        } else {
                            msg
                        }
                    }
                } else {
                    _chatMessages.value = _chatMessages.value.map { msg ->
                        if (msg.id == aiMsgId) {
                            msg.copy(text = finalAnswer, isStreaming = false, isError = false)
                        } else {
                            msg
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI question timed out", e)
                _chatMessages.value = _chatMessages.value.map { msg ->
                    if (msg.id == aiMsgId) {
                        msg.copy(
                            text = "AI generation timed out after 60 seconds. Please try again.",
                            isStreaming = false,
                            isError = true
                        )
                    } else {
                        msg
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI question failed", e)
                _chatMessages.value = _chatMessages.value.map { msg ->
                    if (msg.id == aiMsgId) {
                        msg.copy(
                            text = e.localizedMessage ?: "AI request failed.",
                            isStreaming = false,
                            isError = true
                        )
                    } else {
                        msg
                    }
                }
            } finally {
                _isAiStreaming.value = false
            }
        }
    }

    // Radar playback controls
    fun selectRadarLayer(layer: RadarLayerType) {
        _selectedRadarLayer.value = layer
    }

    fun setRadarFrameIndex(index: Int) {
        _radarFrameIndex.value = index
    }

    fun toggleRadarPlayback(totalFrames: Int) {
        if (_isRadarPlaying.value) {
            _isRadarPlaying.value = false
            radarPlaybackJob?.cancel()
        } else {
            _isRadarPlaying.value = true
            radarPlaybackJob?.cancel()
            radarPlaybackJob = viewModelScope.launch {
                while (_isRadarPlaying.value && totalFrames > 0) {
                    delay(750)
                    _radarFrameIndex.value = (_radarFrameIndex.value + 1) % totalFrames
                }
            }
        }
    }

    // Settings mutations
    fun setLanguage(lang: AppLanguage) = viewModelScope.launch { prefsManager.setLanguage(lang) }
    fun setThemeMode(mode: AppThemeMode) = viewModelScope.launch { prefsManager.setThemeMode(mode) }
    fun setTempUnit(unit: TempUnit) = viewModelScope.launch { prefsManager.setTempUnit(unit) }
    fun setWindUnit(unit: WindUnit) = viewModelScope.launch { prefsManager.setWindUnit(unit) }
    fun setPressureUnit(unit: PressureUnit) = viewModelScope.launch { prefsManager.setPressureUnit(unit) }
    fun setPrecipUnit(unit: PrecipUnit) = viewModelScope.launch { prefsManager.setPrecipUnit(unit) }
    fun setAqiStandard(std: AqiStandard) = viewModelScope.launch { prefsManager.setAqiStandard(std) }
    fun setReducedMotion(enabled: Boolean) = viewModelScope.launch { prefsManager.setReducedMotion(enabled) }
    fun setBatterySaver(enabled: Boolean) = viewModelScope.launch { prefsManager.setBatterySaver(enabled) }
    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch { prefsManager.setNotificationsEnabled(enabled) }
    fun setUpdateInterval(minutes: Int) = viewModelScope.launch { prefsManager.setUpdateIntervalMinutes(minutes) }
    fun clearOfflineWeatherCache() = viewModelScope.launch { repository.clearOfflineCache() }
}
