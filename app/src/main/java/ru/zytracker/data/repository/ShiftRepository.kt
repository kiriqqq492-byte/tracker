package ru.zytracker.data.repository

import kotlinx.coroutines.flow.Flow
import ru.zytracker.data.dao.ShiftDao
import ru.zytracker.data.model.Shift

class ShiftRepository(private val shiftDao: ShiftDao) {
    
    fun getShiftsByMonth(yearMonth: String): Flow<List<Shift>> {
        return shiftDao.getShiftsByMonth(yearMonth)
    }
    
    suspend fun getShiftByDate(date: String): Shift? {
        return shiftDao.getShiftByDate(date)
    }
    
    fun getAllShifts(): Flow<List<Shift>> {
        return shiftDao.getAllShifts()
    }
    
    suspend fun insertShift(shift: Shift) {
        shiftDao.insertShift(shift)
    }
    
    suspend fun updateShift(shift: Shift) {
        shiftDao.updateShift(shift)
    }
    
    suspend fun deleteShift(shift: Shift) {
        shiftDao.deleteShift(shift)
    }
    
    suspend fun deleteShiftByDate(date: String) {
        shiftDao.deleteShiftByDate(date)
    }
    
    fun getTotalOrdersByMonth(yearMonth: String): Flow<Int?> {
        return shiftDao.getTotalOrdersByMonth(yearMonth)
    }
    
    fun getTotalKilometersByMonth(yearMonth: String): Flow<Double?> {
        return shiftDao.getTotalKilometersByMonth(yearMonth)
    }
    
    suspend fun hasShiftOnDate(date: String): Boolean {
        return shiftDao.hasShiftOnDate(date) > 0
    }

    // Методы для года
    fun getShiftsByYear(year: String): Flow<List<Shift>> {
        return shiftDao.getShiftsByYear(year)
    }

    fun getTotalOrdersByYear(year: String): Flow<Int?> {
        return shiftDao.getTotalOrdersByYear(year)
    }

    fun getTotalKilometersByYear(year: String): Flow<Double?> {
        return shiftDao.getTotalKilometersByYear(year)
    }
}
