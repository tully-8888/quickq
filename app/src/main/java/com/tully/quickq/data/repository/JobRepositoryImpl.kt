package com.tully.quickq.data.repository

import com.google.gson.Gson
import com.tully.quickq.data.api.QuickQApiService
import com.tully.quickq.data.constants.Constants
import com.tully.quickq.data.dto.JobDto
import com.tully.quickq.data.dto.JobSearchRequest
import com.tully.quickq.data.local.dao.JobDao
import com.tully.quickq.data.local.entity.toDomainModel
import com.tully.quickq.data.local.entity.toEntity
import com.tully.quickq.domain.model.*
import com.tully.quickq.domain.repository.JobRepository
import com.tully.quickq.presentation.viewmodel.JobFilters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

class JobRepositoryImpl(
    private val quickQApi: QuickQApiService,
    private val gson: Gson,
    private val jobDao: JobDao
) : JobRepository {
    
    override suspend fun searchJobs(query: String): Result<List<Job>> {
        return searchJobs(query, JobFilters())
    }
    
    override suspend fun searchJobs(query: String, filters: JobFilters): Result<List<Job>> = supervisorScope {
        try {
            // First, try to load from Room cache
            val cachedEntities = withContext(Dispatchers.IO) {
                jobDao.searchJobs(query)
            }
            
            val cachedJobs = withContext(Dispatchers.Default) {
                cachedEntities.map { it.toDomainModel() }
            }

            // Apply filters to cached results
            val filteredCachedJobs = cachedJobs.filter { job ->
                val matchesExperience = if (filters.experienceLevels.isNotEmpty()) {
                    filters.experienceLevels.contains(job.experienceLevel)
                } else true
                val matchesJobType = if (filters.jobTypes.isNotEmpty()) {
                    filters.jobTypes.contains(job.jobType)
                } else true
                val matchesWorkEnvironment = if (filters.workEnvironments.isNotEmpty()) {
                    filters.workEnvironments.contains(job.workEnvironment)
                } else true
                val matchesCompanySize = if (filters.companySizes.isNotEmpty()) {
                    filters.companySizes.contains(job.companySize)
                } else true
                val matchesLocations = if (filters.locations.isNotEmpty()) {
                    filters.locations.any { location ->
                        job.location.contains(location, ignoreCase = true)
                    }
                } else true
                val matchesSalaryRanges = if (filters.salaryRanges.isNotEmpty()) {
                    filters.salaryRanges.any { salaryRange ->
                        job.salaryRange?.let { salary ->
                            val (minSalary, maxSalary) = salary.split("-").map { it.trim().filter { it.isDigit() }.toIntOrNull() ?: 0 }
                            minSalary >= salaryRange.minSalary && (maxSalary <= salaryRange.maxSalary || salaryRange.maxSalary == Int.MAX_VALUE)
                        } ?: false
                    }
                } else true
                val matchesIndustries = if (filters.industries.isNotEmpty()) {
                    filters.industries.any { industry ->
                        job.industry.contains(industry, ignoreCase = true)
                    }
                } else true
                val matchesSkills = if (filters.skills.isNotEmpty()) {
                    filters.skills.any { skill ->
                        job.skills.any { it.contains(skill, ignoreCase = true) }
                    }
                } else true

                matchesExperience && matchesJobType && matchesWorkEnvironment && matchesCompanySize &&
                matchesLocations && matchesSalaryRanges && matchesIndustries && matchesSkills
            }
            
            if (filteredCachedJobs.isNotEmpty()) {
                // Shuffle cached results to give a fresh feel
                return@supervisorScope Result.success(filteredCachedJobs.shuffled())
            }

            val result = withContext(Dispatchers.IO) {
                val request = JobSearchRequest(
                    query = query,
                    techSkills = if (filters.skills.isNotEmpty()) filters.skills.toList() else null,
                    jobLevel = filters.experienceLevels.firstOrNull()?.name?.lowercase()
                )
                
                val response = withTimeoutOrNull(180000) {
                    quickQApi.searchJobs(request)
                }

                if (response == null) {
                    Result.failure(Exception("Search request timed out. Please check your connection and try again."))
                } else if (response.isSuccessful && response.body() != null) {
                    val jobDtos = response.body()!!.jobs
                    if (jobDtos.isNotEmpty()) {
                        val jobs = withContext(Dispatchers.Default) {
                            jobDtos.mapNotNull { dto ->
                                try {
                                    dto.toDomainModel()
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        }
                        
                        if (jobs.isNotEmpty()) {
                            // Insert into Room cache
                            val jobEntities = withContext(Dispatchers.Default) {
                                jobs.map { it.toEntity() }
                            }
                            jobDao.insertJobs(jobEntities)
                            Result.success(jobs)
                        } else {
                            Result.failure(Exception("No valid jobs could be parsed from API response"))
                        }
                    } else {
                        Result.failure(Exception("No jobs received from API"))
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown API error"
                    Result.failure(Exception("API call failed: $errorMessage"))
                }
            }
            
            result
        } catch (e: Exception) {
            Result.failure(Exception("Job search failed: ${e.message}", e))
        }
    }
    
    override suspend fun getFeaturedJobs(): Result<List<Job>> {
        // Return a mix of popular tech jobs from top companies
        return searchJobs("Software Engineer Google Apple Microsoft Amazon Meta Netflix")
    }
    
    override suspend fun getJobDetail(jobId: String): Result<Job?> = withContext(Dispatchers.IO) {
        try {
            val jobEntity = jobDao.getJobById(jobId)
            Result.success(jobEntity?.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Remove unused prompt and parsing functions
    private fun createEnhancedJobSearchPrompt(query: String, filters: JobFilters): String {
        throw NotImplementedError("This method is no longer used with the new backend API.")
    }
    
    private fun buildFilterConstraints(filters: JobFilters): String {
        throw NotImplementedError("This method is no longer used with the new backend API.")
    }
    
    private fun parseJobsFromResponse(content: String): List<Job> {
        throw NotImplementedError("This method is no longer used with the new backend API for jobs.")
    }
    
    private fun extractJsonFromContent(content: String): String {
        throw NotImplementedError("This method is no longer used with the new backend API.")
    }
    
    // Extension function to convert data.dto.JobDto to domain.model.Job
    private fun JobDto.toDomainModel(): Job {
        // Get company rating from our reputation map or use a default if not found
        val finalCompanyRating = Constants.COMPANY_REPUTATION_MAP[company] ?: 3.5 // Default to 3.5 if not in map
        
        return Job(
            id = UUID.randomUUID().toString(), // Generate new ID as backend JobDto doesn't have it
            title = title,
            company = company,
            location = location,
            description = description, // JobDto description field now matches domain model
            requirements = emptyList(), // New JobDto does not provide this
            responsibilities = emptyList(), // New JobDto does not provide this
            salaryRange = null, // New JobDto does not provide this
            experienceLevel = when (jobLevel.lowercase()) {
                "entry" -> ExperienceLevel.JUNIOR
                "junior" -> ExperienceLevel.JUNIOR
                "mid" -> ExperienceLevel.MID
                "senior" -> ExperienceLevel.SENIOR
                "lead" -> ExperienceLevel.LEAD
                "principal" -> ExperienceLevel.PRINCIPAL
                else -> ExperienceLevel.MID // Default
            },
            jobType = when (jobType.lowercase()) {
                "remote" -> com.tully.quickq.domain.model.JobType.REMOTE
                "full-time" -> com.tully.quickq.domain.model.JobType.FULL_TIME
                "part-time" -> com.tully.quickq.domain.model.JobType.PART_TIME
                "contract" -> com.tully.quickq.domain.model.JobType.CONTRACT
                "internship" -> com.tully.quickq.domain.model.JobType.INTERNSHIP
                "onsite" -> com.tully.quickq.domain.model.JobType.ON_SITE
                else -> com.tully.quickq.domain.model.JobType.FULL_TIME // Default
            },
            skills = skills,
            postedDate = firstSeen,
            companySize = com.tully.quickq.domain.model.CompanySize.MEDIUM, // New JobDto does not provide this, setting a default
            industry = "Technology", // New JobDto does not provide this, setting a default
            companyDescription = "", // New JobDto does not provide this
            benefits = emptyList(), // New JobDto does not provide this
            workEnvironment = when (jobType.lowercase()) { // Infer from job_type for now
                "remote" -> com.tully.quickq.domain.model.WorkEnvironment.REMOTE
                "onsite" -> com.tully.quickq.domain.model.WorkEnvironment.ON_SITE
                else -> com.tully.quickq.domain.model.WorkEnvironment.HYBRID // Default
            },
            applicationDeadline = null, // New JobDto does not provide this
            companyRating = finalCompanyRating,
            interviewDifficulty = InterviewDifficulty.MODERATE, // New JobDto does not provide this, setting a default
            applicantCount = 0, // New JobDto does not provide this
            hiringUrgency = HiringUrgency.MODERATE // New JobDto does not provide this
        )
    }
} 