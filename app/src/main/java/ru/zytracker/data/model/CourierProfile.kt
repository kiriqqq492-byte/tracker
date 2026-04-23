package ru.zytracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courier_profile")
data class CourierProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "",
    val workSchedule: WorkSchedule = WorkSchedule.FIVE_TWO,
    val scheduleStartDate: String? = null, // YYYY-MM-DD, дата начала отсчета графика
    val createdAt: Long = System.currentTimeMillis()
)

enum class WorkSchedule(val displayName: String, val cycleDays: Int) {
    FIVE_TWO("5/2 (Пн-Пт)", 7),
    TWO_TWO("2/2", 4),
    THREE_THREE("3/3", 6),
    TWO_TWO_THREE("2/2/3", 7),
    CUSTOM("Свой график", 0)
}
