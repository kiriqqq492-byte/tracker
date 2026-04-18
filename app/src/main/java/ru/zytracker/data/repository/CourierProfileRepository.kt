package ru.zytracker.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.zytracker.data.dao.CourierProfileDao
import ru.zytracker.data.model.CourierProfile

class CourierProfileRepository(private val profileDao: CourierProfileDao) {
    
    fun getProfile(): Flow<CourierProfile?> {
        return profileDao.getProfile()
    }
    
    fun hasProfileFlow(): Flow<Boolean> {
        return profileDao.getProfile().map { it != null }
    }
    
    suspend fun hasProfile(): Boolean {
        return profileDao.hasProfile()
    }
    
    suspend fun insertProfile(profile: CourierProfile) {
        profileDao.insertProfile(profile)
    }
    
    suspend fun updateProfile(profile: CourierProfile) {
        profileDao.updateProfile(profile)
    }
    
    suspend fun deleteProfile() {
        profileDao.deleteProfile()
    }

    suspend fun getProfileOnce(): CourierProfile? {
        return profileDao.getProfile().first()
    }
}
