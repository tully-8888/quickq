package com.tully.quickq.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tully.quickq.data.local.entity.JobEntity

@Dao
interface JobDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>)

    @Query("SELECT * FROM jobs WHERE title LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    suspend fun searchJobs(query: String): List<JobEntity>

    @Query("SELECT * FROM jobs WHERE id = :jobId")
    suspend fun getJobById(jobId: String): JobEntity?

    @Query("DELETE FROM jobs")
    suspend fun clearJobs()
} 