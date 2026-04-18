package ru.zytracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.zytracker.data.model.CourierProfile
import ru.zytracker.data.model.WorkSchedule
import ru.zytracker.data.repository.CourierProfileRepository
import ru.zytracker.data.preferences.SettingsRepository

data class SettingsState(
    val profile: CourierProfile? = null,
    val themeMode: String = "system",
    val notificationsEnabled: Boolean = true,
    val statsPeriod: String = "month", // "month" или "year"
    val isLoading: Boolean = false
)

class SettingsViewModel(
    private val profileRepository: CourierProfileRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            profileRepository.getProfile()
                .combine(settingsRepository.themeMode) { profile, theme ->
                    Pair(profile, theme)
                }
                .combine(settingsRepository.notificationsEnabled) { pair, notifications ->
                    Triple(pair.first, pair.second, notifications)
                }
                .combine(settingsRepository.statsPeriod) { triple, period ->
                    SettingsState(
                        profile = triple.first,
                        themeMode = triple.second,
                        notificationsEnabled = triple.third,
                        statsPeriod = period
                    )
                }
                .collect { newState ->
                    _state.value = newState
                }
        }
    }
    
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }
    
    fun setStatsPeriod(period: String) {
        viewModelScope.launch {
            settingsRepository.setStatsPeriod(period)
        }
    }
    
    fun updateSchedule(schedule: WorkSchedule) {
        viewModelScope.launch {
            val currentProfile = _state.value.profile
            if (currentProfile != null) {
                val updatedProfile = currentProfile.copy(workSchedule = schedule)
                profileRepository.updateProfile(updatedProfile)
            } else {
                // Создаём профиль если его нет
                val newProfile = CourierProfile(
                    name = "",
                    workSchedule = schedule,
                    scheduleStartDate = null
                )
                profileRepository.insertProfile(newProfile)
            }
        }
    }

    fun updateScheduleStartDate(startDate: String) {
        viewModelScope.launch {
            val currentProfile = _state.value.profile
            if (currentProfile != null) {
                val updatedProfile = currentProfile.copy(scheduleStartDate = startDate)
                profileRepository.updateProfile(updatedProfile)
            } else {
                // Создаём профиль если его нет
                val newProfile = CourierProfile(
                    name = "",
                    workSchedule = WorkSchedule.FIVE_TWO,
                    scheduleStartDate = startDate
                )
                profileRepository.insertProfile(newProfile)
            }
        }
    }
}
