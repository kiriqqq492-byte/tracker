package ru.zytracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shifts")
data class Shift(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Формат: YYYY-MM-DD
    val orders: Int = 0,
    val kilometers: Double = 0.0,
    val isWorkDayOverride: Boolean = false, // true если выходной стал рабочим днем
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
