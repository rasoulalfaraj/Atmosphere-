package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeMode
import com.example.data.model.AqiStandard
import com.example.data.model.PrecipUnit
import com.example.data.model.PressureUnit
import com.example.data.model.TempUnit
import com.example.data.model.UserPreferences
import com.example.data.model.WindUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "atmosphere_user_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_LANGUAGE = stringPreferencesKey("language_v2")
        private val KEY_THEME = stringPreferencesKey("theme_mode")
        private val KEY_TEMP_UNIT = stringPreferencesKey("temp_unit")
        private val KEY_WIND_UNIT = stringPreferencesKey("wind_unit")
        private val KEY_PRESSURE_UNIT = stringPreferencesKey("pressure_unit")
        private val KEY_PRECIP_UNIT = stringPreferencesKey("precip_unit")
        private val KEY_AQI_STANDARD = stringPreferencesKey("aqi_standard")
        private val KEY_REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        private val KEY_BATTERY_SAVER = booleanPreferencesKey("battery_saver")
        private val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        private val KEY_UPDATE_INTERVAL = intPreferencesKey("update_interval_minutes")
        private val KEY_SELECTED_CITY_ID = stringPreferencesKey("selected_city_id")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            language = prefs[KEY_LANGUAGE]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
                ?: AppLanguage.ARABIC,
            themeMode = prefs[KEY_THEME]?.let { runCatching { AppThemeMode.valueOf(it) }.getOrNull() }
                ?: AppThemeMode.DARK,
            tempUnit = prefs[KEY_TEMP_UNIT]?.let { runCatching { TempUnit.valueOf(it) }.getOrNull() }
                ?: TempUnit.CELSIUS,
            windUnit = prefs[KEY_WIND_UNIT]?.let { runCatching { WindUnit.valueOf(it) }.getOrNull() }
                ?: WindUnit.KMH,
            pressureUnit = prefs[KEY_PRESSURE_UNIT]?.let { runCatching { PressureUnit.valueOf(it) }.getOrNull() }
                ?: PressureUnit.HPA,
            precipUnit = prefs[KEY_PRECIP_UNIT]?.let { runCatching { PrecipUnit.valueOf(it) }.getOrNull() }
                ?: PrecipUnit.MM,
            aqiStandard = prefs[KEY_AQI_STANDARD]?.let { runCatching { AqiStandard.valueOf(it) }.getOrNull() }
                ?: AqiStandard.US_EPA,
            reducedMotion = prefs[KEY_REDUCED_MOTION] ?: false,
            batterySaverMode = prefs[KEY_BATTERY_SAVER] ?: false,
            notificationsEnabled = prefs[KEY_NOTIFICATIONS] ?: true,
            updateIntervalMinutes = prefs[KEY_UPDATE_INTERVAL] ?: 30,
            selectedCityId = prefs[KEY_SELECTED_CITY_ID] ?: "iq_baghdad"
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[KEY_LANGUAGE] = language.name }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[KEY_THEME] = mode.name }
    }

    suspend fun setTempUnit(unit: TempUnit) {
        context.dataStore.edit { it[KEY_TEMP_UNIT] = unit.name }
    }

    suspend fun setWindUnit(unit: WindUnit) {
        context.dataStore.edit { it[KEY_WIND_UNIT] = unit.name }
    }

    suspend fun setPressureUnit(unit: PressureUnit) {
        context.dataStore.edit { it[KEY_PRESSURE_UNIT] = unit.name }
    }

    suspend fun setPrecipUnit(unit: PrecipUnit) {
        context.dataStore.edit { it[KEY_PRECIP_UNIT] = unit.name }
    }

    suspend fun setAqiStandard(standard: AqiStandard) {
        context.dataStore.edit { it[KEY_AQI_STANDARD] = standard.name }
    }

    suspend fun setReducedMotion(enabled: Boolean) {
        context.dataStore.edit { it[KEY_REDUCED_MOTION] = enabled }
    }

    suspend fun setBatterySaver(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BATTERY_SAVER] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS] = enabled }
    }

    suspend fun setUpdateIntervalMinutes(minutes: Int) {
        context.dataStore.edit { it[KEY_UPDATE_INTERVAL] = minutes }
    }

    suspend fun setSelectedCityId(cityId: String) {
        context.dataStore.edit { it[KEY_SELECTED_CITY_ID] = cityId }
    }
}
