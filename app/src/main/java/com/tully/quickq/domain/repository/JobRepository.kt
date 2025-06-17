package com.tully.quickq.domain.repository

import com.tully.quickq.domain.model.Job
import com.tully.quickq.presentation.viewmodel.JobFilters

interface JobRepository {
    suspend fun searchJobs(query: String): Result<List<Job>>
    suspend fun searchJobs(query: String, filters: JobFilters): Result<List<Job>>
    suspend fun getJobDetail(jobId: String): Result<Job?>
    suspend fun getFeaturedJobs(): Result<List<Job>>
} 