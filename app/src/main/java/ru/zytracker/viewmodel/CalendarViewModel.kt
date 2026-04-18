package ru.zytracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.zytracker.data.model.Shift
import ru.zytracker.data.model.WorkSchedule
import ru.zytracker.data.repository.CourierProfileRepository
import ru.zytracker.data.repository.ShiftRepository
import ru.zytracker.data.preferences.SettingsRepository
import ru.zytracker.util.ScheduleUtils
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class CalendarState(
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: LocalDate = LocalDate.now(),
    val shifts: List<Shift> = emptyList(),
    val totalOrders: Int = 0,
    val totalKilometers: Double = 0.0,
    val isLoading: Boolean = false,
    val showDialog: Boolean = false,
    val dialogShift: Shift? = null,
    val error: String? = null,
    val workSchedule: WorkSchedule = WorkSchedule.FIVE_TWO,
    val scheduleStartDate: LocalDate? = null,
    val workDaysMap: Map<LocalDate, ScheduleUtils.DayType> = emptyMap(),
    val statsPeriod: String = "month"
)

class CalendarViewModel(
    private val shiftRepository: ShiftRepository,
    private val profileRepository: CourierProfileRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CalendarState())
    val state: StateFlow<CalendarState> = _state.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    private val yearFormatter = DateTimeFormatter.ofPattern("yyyy")

    init {
        // Load profile and subscribe to changes
        viewModelScope.launch {
            profileRepository.getProfile().collect { profile ->
                profile?.let {
                    val startDate = it.scheduleStartDate?.let { dateStr ->
                        LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    _state.value = _state.value.copy(
                        workSchedule = it.workSchedule,
                        scheduleStartDate = startDate
                    )
                    calculateWorkDays()
                }
            }
        }
        
        // Subscribe to stats period changes
        viewModelScope.launch {
            settingsRepository.statsPeriod.collect { period ->
                _state.value = _state.value.copy(statsPeriod = period)
                loadData()
            }
        }
        
        loadData()
    }

    private fun calculateWorkDays() {
        val state = _state.value
        val yearMonth = YearMonth.from(state.currentMonth)
        val daysInMonth = yearMonth.lengthOfMonth()
        
        val workDaysMap = mutableMapOf<LocalDate, ScheduleUtils.DayType>()
        
        for (day in 1..daysInMonth) {
            val date = yearMonth.atDay(day)
            val shift = state.shifts.find { it.date == date.format(dateFormatter) }
            
            val dayType = ScheduleUtils.getDayType(
                date = date,
                schedule = state.workSchedule,
                startDate = state.scheduleStartDate,
                hasShift = shift != null
            )
            
            workDaysMap[date] = dayType
        }
        
        _state.value = _state.value.copy(workDaysMap = workDaysMap)
    }
    
    fun selectDate(date: LocalDate) {
        _state.value = _state.value.copy(selectedDate = date)
        checkShiftForDate(date)
    }
    
    fun changeMonth(monthChange: Int) {
        val newMonth = _state.value.currentMonth.plusMonths(monthChange.toLong())
        _state.value = _state.value.copy(currentMonth = newMonth)
        loadData()
    }
    
    fun setShowDialog(show: Boolean, shift: Shift? = null) {
        _state.value = _state.value.copy(showDialog = show, dialogShift = shift)
    }
    
    fun checkShiftForDate(date: LocalDate) {
        viewModelScope.launch {
            val dateStr = date.format(dateFormatter)
            val shift = shiftRepository.getShiftByDate(dateStr)
            _state.value = _state.value.copy(dialogShift = shift, showDialog = true)
        }
    }
    
    fun saveShift(orders: Int, kilometers: Double) {
        viewModelScope.launch {
            try {
                val date = _state.value.selectedDate
                val dateStr = date.format(dateFormatter)
                val existingShift = shiftRepository.getShiftByDate(dateStr)

                val shift = if (existingShift != null) {
                    existingShift.copy(
                        orders = orders,
                        kilometers = kilometers,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    Shift(
                        date = dateStr,
                        orders = orders,
                        kilometers = kilometers
                    )
                }

                shiftRepository.insertShift(shift)
                _state.value = _state.value.copy(showDialog = false, error = null)
                loadData()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Ошибка сохранения: ${e.message}")
            }
        }
    }
    
    fun deleteShift() {
        viewModelScope.launch {
            try {
                val date = _state.value.selectedDate
                val dateStr = date.format(dateFormatter)
                shiftRepository.deleteShiftByDate(dateStr)
                _state.value = _state.value.copy(showDialog = false, error = null)
                loadData()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Ошибка удаления: ${e.message}")
            }
        }
    }
    
    private fun loadData() {
        val state = _state.value
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            if (state.statsPeriod == "year") {
                // Загрузка данных за год
                val year = state.currentMonth.format(yearFormatter)
                val monthMask = year + "-%"
                
                shiftRepository.getShiftsByYear(year)
                    .combine(shiftRepository.getTotalOrdersByYear(year)) { shifts, orders ->
                        shifts to (orders ?: 0)
                    }
                    .combine(shiftRepository.getTotalKilometersByYear(year)) { pair, km ->
                        Triple(pair.first, pair.second, km ?: 0.0)
                    }
                    .combine(shiftRepository.getShiftsByMonth(monthMask)) { triple, monthShifts ->
                        // Используем данные за год для отображения, но monthShifts для workDaysMap
                        Triple(triple.first, triple.second, triple.third)
                    }
                    .collect { (shifts, orders, km) ->
                        _state.value = _state.value.copy(
                            shifts = shifts,
                            totalOrders = orders,
                            totalKilometers = km,
                            isLoading = false
                        )
                        calculateWorkDays()
                    }
            } else {
                // Загрузка данных за месяц
                val yearMonth = state.currentMonth.format(yearMonthFormatter) + "%"
                
                shiftRepository.getShiftsByMonth(yearMonth)
                    .combine(shiftRepository.getTotalOrdersByMonth(yearMonth)) { shifts, orders ->
                        shifts to (orders ?: 0)
                    }
                    .combine(shiftRepository.getTotalKilometersByMonth(yearMonth)) { pair, km ->
                        Triple(pair.first, pair.second, km ?: 0.0)
                    }
                    .collect { (shifts, orders, km) ->
                        _state.value = _state.value.copy(
                            shifts = shifts,
                            totalOrders = orders,
                            totalKilometers = km,
                            isLoading = false
                        )
                        calculateWorkDays()
                    }
            }
        }
    }
    
    fun hasShiftOnDate(date: LocalDate): Boolean {
        return _state.value.shifts.any { it.date == date.format(dateFormatter) }
    }
}
