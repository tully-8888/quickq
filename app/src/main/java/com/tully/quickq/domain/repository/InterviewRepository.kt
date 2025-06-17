package com.tully.quickq.domain.repository

import com.tully.quickq.domain.model.*

interface InterviewRepository {
    suspend fun startInterview(jobId: String, feedbackMode: FeedbackMode): Result<Interview>
    suspend fun getNextQuestion(interview: Interview): Result<String>
    suspend fun submitAnswer(interviewId: String, questionId: String, answer: String): Result<Unit>
    suspend fun getFeedback(interviewId: String, questionId: String): Result<InterviewFeedback>
    suspend fun completeInterview(interviewId: String): Result<InterviewSummary>
    suspend fun getInterviewHistory(): Result<List<InterviewSummary>>
    suspend fun updateInterviewFeedbackMode(interviewId: String, feedbackMode: FeedbackMode): Result<Unit>
} 