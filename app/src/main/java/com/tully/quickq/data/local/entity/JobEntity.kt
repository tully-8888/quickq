package com.tully.quickq.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tully.quickq.domain.model.*

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    val title: String,
    val company: String,
    val location: String,
    val description: String,
    val experienceLevel: String,
    val jobType: String,
    val skills: String,
    val postedDate: String?,
    val companySize: String,
    val industry: String,
    val companyDescription: String,
    val benefits: String,
    val workEnvironment: String,
    val applicationDeadline: String?,
    val companyRating: Double,
    val interviewDifficulty: String,
    val applicantCount: Int,
    val hiringUrgency: String,
    val salaryRange: String? = null,
    val requirements: String,
    val responsibilities: String
)

fun JobEntity.toDomainModel(): Job {
    val gson = Gson()
    val listType = object : TypeToken<List<String>>() {}.type
    return Job(
        id = this.id,
        title = this.title,
        company = this.company,
        location = this.location,
        description = this.description,
        requirements = gson.fromJson(this.requirements, listType),
        responsibilities = gson.fromJson(this.responsibilities, listType),
        salaryRange = this.salaryRange,
        experienceLevel = ExperienceLevel.valueOf(this.experienceLevel),
        jobType = JobType.valueOf(this.jobType),
        skills = gson.fromJson(this.skills, listType),
        postedDate = this.postedDate,
        companySize = CompanySize.valueOf(this.companySize),
        industry = this.industry,
        companyDescription = this.companyDescription,
        benefits = gson.fromJson(this.benefits, listType),
        workEnvironment = WorkEnvironment.valueOf(this.workEnvironment),
        applicationDeadline = this.applicationDeadline,
        companyRating = this.companyRating,
        interviewDifficulty = InterviewDifficulty.valueOf(this.interviewDifficulty),
        applicantCount = this.applicantCount,
        hiringUrgency = HiringUrgency.valueOf(this.hiringUrgency)
    )
}

fun Job.toEntity(): JobEntity {
    val gson = Gson()
    return JobEntity(
        id = this.id,
        title = this.title,
        company = this.company,
        location = this.location,
        description = this.description,
        requirements = gson.toJson(this.requirements),
        responsibilities = gson.toJson(this.responsibilities),
        salaryRange = this.salaryRange,
        experienceLevel = this.experienceLevel.name,
        jobType = this.jobType.name,
        skills = gson.toJson(this.skills),
        postedDate = this.postedDate,
        companySize = this.companySize.name,
        industry = this.industry,
        companyDescription = this.companyDescription,
        benefits = gson.toJson(this.benefits),
        workEnvironment = this.workEnvironment.name,
        applicationDeadline = this.applicationDeadline,
        companyRating = this.companyRating,
        interviewDifficulty = this.interviewDifficulty.name,
        applicantCount = this.applicantCount,
        hiringUrgency = this.hiringUrgency.name
    )
} 