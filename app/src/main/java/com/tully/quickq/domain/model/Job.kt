package com.tully.quickq.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
data class Job(
    val id: String,
    val title: String,
    val company: String,
    val location: String,
    val description: String,
    val requirements: List<String>,
    val responsibilities: List<String>,
    val salaryRange: String?,
    val experienceLevel: ExperienceLevel,
    val jobType: JobType,
    val skills: List<String>,
    val postedDate: String?,
    val companySize: CompanySize,
    val industry: String,
    val companyDescription: String,
    val benefits: List<String>,
    val workEnvironment: WorkEnvironment,
    val applicationDeadline: String?,
    val companyRating: Double, // 1.0-5.0 for company reputation
    val interviewDifficulty: InterviewDifficulty,
    val applicantCount: Int,
    val hiringUrgency: HiringUrgency
)

@Stable
enum class ExperienceLevel {
    JUNIOR, MID, SENIOR, LEAD, PRINCIPAL;

    val displayName: String
        get() = when (this) {
            JUNIOR -> "Junior"
            MID -> "Mid-level"
            SENIOR -> "Senior"
            LEAD -> "Lead"
            PRINCIPAL -> "Principal"
        }
}

@Stable
enum class JobType {
    FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP, REMOTE ,ON_SITE
}

@Stable
enum class CompanySize {
    STARTUP, SMALL, MEDIUM, LARGE, ENTERPRISE
}

@Stable
enum class WorkEnvironment {
    REMOTE, HYBRID, ON_SITE
}

@Stable
enum class InterviewDifficulty {
    EASY, MODERATE, CHALLENGING, VERY_HARD, EXTREME
}

@Stable
enum class HiringUrgency {
    LOW, MODERATE, HIGH, URGENT
} 