package com.tully.quickq.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
data class Interview(
    val id: String,
    val jobId: String,
    val interviewerName: String,
    val questions: List<InterviewQuestion>,
    val currentQuestionIndex: Int = 0,
    val isCompleted: Boolean = false,
    val feedbackMode: FeedbackMode = FeedbackMode.END_OF_INTERVIEW,
    val job: Job
)

@Immutable
data class InterviewQuestion(
    val id: String,
    val question: String,
    val userAnswer: String? = null,
    val feedback: InterviewFeedback? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Immutable
data class InterviewFeedback(
    val score: Int, // 1-10
    val strengths: List<String>,
    val areasForImprovement: List<String>,
    val suggestions: List<String>,
    val overallComment: String
)

@Stable
enum class FeedbackMode {
    AFTER_EACH_QUESTION,
    END_OF_INTERVIEW
}

@Immutable
data class InterviewSummary(
    val totalQuestions: Int,
    val averageScore: Double,
    val totalDuration: Long,
    val overallFeedback: InterviewFeedback,
    val completionDate: Long = System.currentTimeMillis()
) 