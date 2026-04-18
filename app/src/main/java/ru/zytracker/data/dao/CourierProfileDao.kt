package ru.zytracker.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.zytracker.data.model.CourierProfile

@Dao
interface CourierProfileDao {
    
    @Query("SELECT * FROM courier_profile WHERE id = 1")
    fun getProfile(): Flow<CourierProfile?>
    
    @Query("SELECT * FROM courier_profile WHERE id = 1")
    suspend fun getProfileOnce(): CourierProfile?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: CourierProfile)
    
    @Update
    suspend fun updateProfile(profile: CourierProfile)
    
    @Query("SELECT EXISTS(SELECT 1 FROM courier_profile WHERE id = 1)")
    suspend fun hasProfile(): Boolean
    
    @Query("DELETE FROM courier_profile")
    suspend fun deleteProfile()
}
