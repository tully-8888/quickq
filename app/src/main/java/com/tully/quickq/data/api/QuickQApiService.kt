package com.tully.quickq.data.api

import com.tully.quickq.data.dto.FeedbackRequest
import com.tully.quickq.data.dto.FeedbackResponse
import com.tully.quickq.data.dto.JobSearchRequest
import com.tully.quickq.data.dto.JobSearchResponse
import com.tully.quickq.data.dto.QuestionGenerationRequest
import com.tully.quickq.data.dto.QuestionGenerationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface QuickQApiService {
    @POST("jobs")
    suspend fun searchJobs(@Body request: JobSearchRequest): Response<JobSearchResponse>

    @POST("questions")
    suspend fun generateQuestions(@Body request: QuestionGenerationRequest): Response<QuestionGenerationResponse>

    @POST("feedback")
    suspend fun getFeedback(@Body request: FeedbackRequest): Response<FeedbackResponse>
} 