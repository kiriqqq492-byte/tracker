package ru.zytracker.data

import androidx.room.TypeConverter
import ru.zytracker.data.model.WorkSchedule

class Converters {
    @TypeConverter
    fun fromWorkSchedule(value: WorkSchedule): String {
        return value.name
    }

    @TypeConverter
    fun toWorkSchedule(value: String): WorkSchedule {
        return WorkSchedule.valueOf(value)
    }
}
