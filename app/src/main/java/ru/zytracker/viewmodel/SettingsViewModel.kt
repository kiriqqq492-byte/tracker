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
            _state.value = _state.value.copy(isLoading = true)
            
            combine(
                profileRepository.getProfile(),
                settingsRepository.themeMode,
                settingsRepository.notificationsEnabled,
                settingsRepository.workSchedule
            ) { profile, theme, notifications, workScheduleStr ->
                val workSchedule = try {
                    WorkSchedule.valueOf(workScheduleStr)
                } catch (e: Exception) {
                    WorkSchedule.FIVE_TWO
                }
                SettingsState(
                    profile = profile?.copy(workSchedule = workSchedule) ?: CourierProfile(workSchedule = workSchedule),
                    themeMode = theme,
                    notificationsEnabled = notifications,
                    isLoading = false
                )
            }.collect { newState ->
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
    
    fun updateSchedule(schedule: WorkSchedule) {
        viewModelScope.launch {
            // Сохраняем в DataStore (надёжный способ)
            settingsRepository.setWorkSchedule(schedule.name)
            
            // Также сохраняем в Room для совместимости
            val currentProfile = profileRepository.getProfileOnce()
            val profileToSave = CourierProfile(
                id = 1,
                name = currentProfile?.name ?: "",
                workSchedule = schedule,
                scheduleStartDate = currentProfile?.scheduleStartDate
            )
            profileRepository.insertProfile(profileToSave)
        }
    }

    fun updateScheduleStartDate(startDate: String) {
        viewModelScope.launch {
            // Сохраняем в DataStore
            settingsRepository.setWorkScheduleStartDate(startDate)
            
            // Также сохраняем в Room
            val currentProfile = profileRepository.getProfileOnce()
            val profileToSave = CourierProfile(
                id = 1,
                name = currentProfile?.name ?: "",
                workSchedule = currentProfile?.workSchedule ?: WorkSchedule.FIVE_TWO,
                scheduleStartDate = startDate
            )
            profileRepository.insertProfile(profileToSave)
        }
    }
}