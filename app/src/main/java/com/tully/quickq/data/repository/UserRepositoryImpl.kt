package com.tully.quickq.data.repository

import android.content.SharedPreferences
import com.tully.quickq.domain.model.*
import com.tully.quickq.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class UserRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : UserRepository {
    
    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_FIRST_TIME = "is_first_time"
        private const val KEY_PROFILE_ROLE = "profile_role"
        private const val KEY_PROFILE_SENIORITY = "profile_seniority"
        private const val KEY_PROFILE_SKILLS = "profile_skills"
        private const val KEY_PROFILE_BIO = "profile_bio"
        private const val KEY_PROFILE_LOCATION = "profile_location"
        private const val KEY_USER_CREATED_AT = "user_created_at"
        private const val KEY_USER_UPDATED_AT = "user_updated_at"
    }
    
    override suspend fun getCurrentUser(): Result<User?> = withContext(Dispatchers.IO) {
        try {
            val userId = sharedPreferences.getString(KEY_USER_ID, null)
            if (userId == null) {
                Result.success(null)
            } else {
                val user = User(
                    id = userId,
                    isFirstTime = sharedPreferences.getBoolean(KEY_IS_FIRST_TIME, true),
                    profile = getUserProfile(),
                    createdAt = sharedPreferences.getLong(KEY_USER_CREATED_AT, System.currentTimeMillis()),
                    updatedAt = sharedPreferences.getLong(KEY_USER_UPDATED_AT, System.currentTimeMillis())
                )
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createUser(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val userId = UUID.randomUUID().toString()
            val currentTime = System.currentTimeMillis()
            
            sharedPreferences.edit()
                .putString(KEY_USER_ID, userId)
                .putBoolean(KEY_IS_FIRST_TIME, true)
                .putLong(KEY_USER_CREATED_AT, currentTime)
                .putLong(KEY_USER_UPDATED_AT, currentTime)
                .apply()
            
            val user = User(
                id = userId,
                isFirstTime = true,
                profile = null,
                createdAt = currentTime,
                updatedAt = currentTime
            )
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateUserProfile(profile: UserProfile): Result<User> = withContext(Dispatchers.IO) {
        try {
            val userId = sharedPreferences.getString(KEY_USER_ID, null)
                ?: return@withContext Result.failure(Exception("User not found"))
            
            val currentTime = System.currentTimeMillis()
            
            sharedPreferences.edit()
                .putString(KEY_PROFILE_ROLE, profile.role)
                .putString(KEY_PROFILE_SENIORITY, profile.seniority?.name)
                .putString(KEY_PROFILE_SKILLS, profile.skills.joinToString(","))
                .putString(KEY_PROFILE_BIO, profile.bio)
                .putString(KEY_PROFILE_LOCATION, profile.location)
                .putLong(KEY_USER_UPDATED_AT, currentTime)
                .apply()
            
            val user = User(
                id = userId,
                isFirstTime = sharedPreferences.getBoolean(KEY_IS_FIRST_TIME, true),
                profile = profile,
                createdAt = sharedPreferences.getLong(KEY_USER_CREATED_AT, System.currentTimeMillis()),
                updatedAt = currentTime
            )
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun completeOnboarding(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val userId = sharedPreferences.getString(KEY_USER_ID, null)
                ?: return@withContext Result.failure(Exception("User not found"))
            
            val currentTime = System.currentTimeMillis()
            
            sharedPreferences.edit()
                .putBoolean(KEY_IS_FIRST_TIME, false)
                .putLong(KEY_USER_UPDATED_AT, currentTime)
                .apply()
            
            val user = User(
                id = userId,
                isFirstTime = false,
                profile = getUserProfile(),
                createdAt = sharedPreferences.getLong(KEY_USER_CREATED_AT, System.currentTimeMillis()),
                updatedAt = currentTime
            )
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun isFirstTimeUser(): Boolean = withContext(Dispatchers.IO) {
        val userId = sharedPreferences.getString(KEY_USER_ID, null)
        userId == null || sharedPreferences.getBoolean(KEY_IS_FIRST_TIME, true)
    }
    
    private fun getUserProfile(): UserProfile? {
        val role = sharedPreferences.getString(KEY_PROFILE_ROLE, null) ?: return null
        
        val seniorityName = sharedPreferences.getString(KEY_PROFILE_SENIORITY, null)
        val seniority = seniorityName?.let { 
            try { 
                ExperienceLevel.valueOf(it) 
            } catch (e: Exception) { 
                null 
            } 
        }
        
        val skillsString = sharedPreferences.getString(KEY_PROFILE_SKILLS, "")
        val skills = if (skillsString.isNullOrEmpty()) {
            emptyList()
        } else {
            skillsString.split(",").filter { it.isNotBlank() }
        }
        
        return UserProfile(
            role = role,
            seniority = seniority,
            skills = skills,
            bio = sharedPreferences.getString(KEY_PROFILE_BIO, "") ?: "",
            location = sharedPreferences.getString(KEY_PROFILE_LOCATION, "") ?: ""
        )
    }
} 