package com.tully.quickq.data.dto

import com.google.gson.annotations.SerializedName

// Job Search DTOs
data class JobSearchRequest(
    @SerializedName("query") val query: String,
    @SerializedName("tech_skills") val techSkills: List<String>? = null,
    @SerializedName("job_level") val jobLevel: String? = null,
    @SerializedName("limit") val limit: Int = 6,
)

data class JobSearchResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("query") val query: String,
    @SerializedName("jobs") val jobs: List<JobDto>,
    @SerializedName("total") val total: Int,
    @SerializedName("ai_generated") val aiGenerated: Boolean
)

data class JobDto(
    @SerializedName("title") val title: String,
    @SerializedName("company") val company: String,
    @SerializedName("description") val description: String,
    @SerializedName("location") val location: String,
    @SerializedName("job_type") val jobType: String,
    @SerializedName("job_level") val jobLevel: String,
    @SerializedName("job_link") val jobLink: String,
    @SerializedName("skills") val skills: List<String>,
    @SerializedName("first_seen") val firstSeen: String
)

fun JobDto.toDomainJob(): com.tully.quickq.domain.model.Job {
    return com.tully.quickq.domain.model.Job(
        id = jobLink,
        title = title,
        company = company,
        location = location,
        description = description,
        requirements = emptyList(),
        responsibilities = emptyList(),
        salaryRange = null,
        experienceLevel = com.tully.quickq.domain.model.ExperienceLevel.valueOf(jobLevel.uppercase()),
        jobType = com.tully.quickq.domain.model.JobType.valueOf(jobType.replace("-", "_").uppercase()),
        skills = skills,
        postedDate = firstSeen,
        companySize = com.tully.quickq.domain.model.CompanySize.MEDIUM,
        industry = "Technology",
        companyDescription = "",
        benefits = emptyList(),
        workEnvironment = com.tully.quickq.domain.model.WorkEnvironment.ON_SITE,
        applicationDeadline = null,
        companyRating = 0.0,
        interviewDifficulty = com.tully.quickq.domain.model.InterviewDifficulty.MODERATE,
        applicantCount = (0..100).random(),
        hiringUrgency = com.tully.quickq.domain.model.HiringUrgency.MODERATE
    )
}

data class QuestionGenerationRequest(
    @SerializedName("job") val job: JobDetailsForQuestionGeneration
)

data class JobDetailsForQuestionGeneration(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("skills") val skills: List<String>
)

data class QuestionGenerationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("job_title") val jobTitle: String,
    @SerializedName("questions") val questions: List<String>,
    @SerializedName("total") val total: Int
)

// Feedback DTOs
data class FeedbackRequest(
    @SerializedName("job") val job: JobDetailsForFeedback,
    @SerializedName("questions") val questions: List<FeedbackQuestion>
)

data class JobDetailsForFeedback(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("skills") val skills: List<String>
)

data class FeedbackQuestion(
    @SerializedName("question") val question: String,
    @SerializedName("answer") val answer: String
)

data class FeedbackResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("job_title") val jobTitle: String,
    @SerializedName("feedback") val feedback: String
) 