package ru.zytracker.util

import ru.zytracker.data.model.WorkSchedule
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Утилита для определения рабочих дней на основе графика работы
 */
object ScheduleUtils {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * Определяет, является ли дата рабочим днем по графику
     * 
     * @param date дата для проверки
     * @param schedule график работы
     * @param startDate дата начала отсчета графика (если null, используется логика по умолчанию)
     * @return true если это рабочий день по графику
     */
    fun isWorkDay(
        date: LocalDate,
        schedule: WorkSchedule,
        startDate: LocalDate? = null
    ): Boolean {
        val start = startDate ?: getDefaultStartDate(schedule)
        
        when (schedule) {
            WorkSchedule.FIVE_TWO -> return isWorkDayFiveTwo(date)
            WorkSchedule.TWO_TWO -> return isWorkDayCycle(date, start, listOf(true, true, false, false))
            WorkSchedule.THREE_THREE -> return isWorkDayCycle(date, start, listOf(true, true, true, false, false, false))
            WorkSchedule.TWO_TWO_THREE -> return isWorkDayCycle(date, start, listOf(true, true, false, false, true, true, true, false, false, true, true, false, false, false))
            WorkSchedule.CUSTOM -> return true // Для своего графика все дни считаются потенциально рабочими
        }
    }

    /**
     * График 5/2 - рабочие дни с понедельника по пятницу
     */
    private fun isWorkDayFiveTwo(date: LocalDate): Boolean {
        val dayOfWeek = date.dayOfWeek.value // 1 = Monday, 7 = Sunday
        return dayOfWeek in 1..5
    }

    /**
     * Циклический график (2/2, 3/3, 2/2/3)
     */
    private fun isWorkDayCycle(
        date: LocalDate,
        startDate: LocalDate,
        pattern: List<Boolean>
    ): Boolean {
        val daysDiff = ChronoUnit.DAYS.between(startDate, date)
        if (daysDiff < 0) return false // Дата до начала графика
        
        val cycleIndex = daysDiff.toInt() % pattern.size
        return pattern[cycleIndex.toInt()]
    }

    /**
     * Дата начала по умолчанию для разных графиков
     */
    @Suppress("UNUSED_PARAMETER")
    private fun getDefaultStartDate(schedule: WorkSchedule): LocalDate {
        return LocalDate.now()
    }

    /**
     * Получить тип дня (для подсветки в календаре)
     */
    fun getDayType(
        date: LocalDate,
        schedule: WorkSchedule,
        startDate: LocalDate?,
        hasShift: Boolean
    ): DayType {
        val isWorkBySchedule = isWorkDay(date, schedule, startDate)
        
        return when {
            hasShift -> DayType.WORKED // День с введенными данными
            isWorkBySchedule -> DayType.SCHEDULED_WORK // Рабочий день по графику
            else -> DayType.OFF // Выходной
        }
    }

    enum class DayType {
        SCHEDULED_WORK, // Рабочий день по графику (без данных)
        WORKED,         // День с введенными данными
        OFF             // Выходной
    }
}
