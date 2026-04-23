package ru.zytracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {
    
    private val dataStore = context.settingsDataStore
    
    object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val STATS_PERIOD = stringPreferencesKey("stats_period")
        val WORK_SCHEDULE = stringPreferencesKey("work_schedule") // FIVE_TWO, TWO_TWO, THREE_THREE, TWO_TWO_THREE, CUSTOM
        val WORK_SCHEDULE_START_DATE = stringPreferencesKey("work_schedule_start_date")
    }
    
    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: "system"
    }
    
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
    }
    
    val statsPeriod: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.STATS_PERIOD] ?: "month"
    }
    
    val workSchedule: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WORK_SCHEDULE] ?: "FIVE_TWO"
    }
    
    val workScheduleStartDate: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WORK_SCHEDULE_START_DATE]
    }
    
    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }
    
    suspend fun setStatsPeriod(period: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.STATS_PERIOD] = period
        }
    }
    
    suspend fun setWorkSchedule(schedule: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WORK_SCHEDULE] = schedule
        }
    }
    
    suspend fun setWorkScheduleStartDate(date: String?) {
        dataStore.edit { preferences ->
            if (date != null) {
                preferences[PreferencesKeys.WORK_SCHEDULE_START_DATE] = date
            } else {
                preferences.remove(PreferencesKeys.WORK_SCHEDULE_START_DATE)
            }
        }
    }
}