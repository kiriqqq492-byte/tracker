package ru.zytracker.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.zytracker.data.model.Shift

@Dao
interface ShiftDao {
    
    @Query("SELECT * FROM shifts WHERE date LIKE :yearMonth ORDER BY date ASC")
    fun getShiftsByMonth(yearMonth: String): Flow<List<Shift>>
    
    @Query("SELECT * FROM shifts WHERE date = :date")
    suspend fun getShiftByDate(date: String): Shift?
    
    @Query("SELECT * FROM shifts ORDER BY date DESC")
    fun getAllShifts(): Flow<List<Shift>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: Shift)
    
    @Update
    suspend fun updateShift(shift: Shift)
    
    @Delete
    suspend fun deleteShift(shift: Shift)
    
    @Query("DELETE FROM shifts WHERE date = :date")
    suspend fun deleteShiftByDate(date: String)
    
    @Query("SELECT SUM(orders) FROM shifts WHERE date LIKE :yearMonth")
    fun getTotalOrdersByMonth(yearMonth: String): Flow<Int?>
    
    @Query("SELECT SUM(kilometers) FROM shifts WHERE date LIKE :yearMonth")
    fun getTotalKilometersByMonth(yearMonth: String): Flow<Double?>
    
    @Query("SELECT COUNT(*) FROM shifts WHERE date = :date")
    suspend fun hasShiftOnDate(date: String): Int

    // Методы для года
    @Query("SELECT * FROM shifts WHERE date LIKE :year ORDER BY date ASC")
    fun getShiftsByYear(year: String): Flow<List<Shift>>

    @Query("SELECT SUM(orders) FROM shifts WHERE date LIKE :year")
    fun getTotalOrdersByYear(year: String): Flow<Int?>

    @Query("SELECT SUM(kilometers) FROM shifts WHERE date LIKE :year")
    fun getTotalKilometersByYear(year: String): Flow<Double?>
}
