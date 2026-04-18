package ru.zytracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.zytracker.data.model.CourierProfile
import ru.zytracker.data.model.WorkSchedule
import ru.zytracker.data.repository.CourierProfileRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class WelcomeState(
    val selectedSchedule: WorkSchedule? = null,
    val scheduleStartDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null
)

class WelcomeViewModel(private val repository: CourierProfileRepository) : ViewModel() {
    
    private val _state = MutableStateFlow(WelcomeState())
    val state: StateFlow<WelcomeState> = _state.asStateFlow()
    
    fun updateSchedule(schedule: WorkSchedule) {
        _state.value = _state.value.copy(selectedSchedule = schedule)
    }

    fun updateScheduleStartDate(date: LocalDate) {
        _state.value = _state.value.copy(scheduleStartDate = date)
    }
    
    fun saveProfile() {
        viewModelScope.launch {
            val currentState = _state.value
            
            if (currentState.selectedSchedule == null) {
                _state.value = currentState.copy(error = "Выберите график работы")
                return@launch
            }
            
            try {
                _state.value = currentState.copy(isLoading = true, error = null)
                
                val profile = CourierProfile(
                    name = "",  // Имя больше не требуется
                    workSchedule = currentState.selectedSchedule,
                    scheduleStartDate = currentState.scheduleStartDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                )
                
                repository.insertProfile(profile)
                
                _state.value = currentState.copy(isLoading = false, isCompleted = true)
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isLoading = false,
                    error = "Ошибка сохранения: ${e.message}"
                )
            }
        }
    }
}
