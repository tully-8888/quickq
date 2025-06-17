package com.tully.quickq.domain.repository

import com.tully.quickq.domain.model.User
import com.tully.quickq.domain.model.UserProfile

interface UserRepository {
    suspend fun getCurrentUser(): Result<User?>
    suspend fun createUser(): Result<User>
    suspend fun updateUserProfile(profile: UserProfile): Result<User>
    suspend fun completeOnboarding(): Result<User>
    suspend fun isFirstTimeUser(): Boolean
} 