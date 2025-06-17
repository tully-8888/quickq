package com.tully.quickq.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.tully.quickq.data.api.QuickQApiService
import com.tully.quickq.data.constants.Constants
import com.tully.quickq.data.dto.JobDto
import com.tully.quickq.data.dto.QuestionGenerationRequest
import com.tully.quickq.domain.model.*
import com.tully.quickq.domain.repository.InterviewRepository
import com.tully.quickq.domain.repository.JobRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import androidx.core.content.edit
import com.tully.quickq.data.dto.FeedbackQuestion
import com.tully.quickq.data.dto.FeedbackRequest
import com.tully.quickq.data.dto.JobDetailsForFeedback
import com.tully.quickq.data.dto.JobDetailsForQuestionGeneration
import kotlinx.coroutines.delay

class InterviewRepositoryImpl(
    private val quickQApi: QuickQApiService,
    private val jobRepository: JobRepository,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : InterviewRepository {
    
    private val activeInterviews = mutableMapOf<String, Interview>()
    
    companion object {
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 1000L // 1 second
    }
    
    override suspend fun startInterview(jobId: String, feedbackMode: FeedbackMode): Result<Interview> = withContext(Dispatchers.IO) {
        try {
            val jobResult = jobRepository.getJobDetail(jobId)
            if (jobResult.isFailure) {
                return@withContext Result.failure(jobResult.exceptionOrNull() ?: Exception("Job not found"))
            }
            
            val job = jobResult.getOrNull() ?: return@withContext Result.failure(Exception("Job not found"))
            
            val interviewId = UUID.randomUUID().toString()
            val interviewerName = Constants.INTERVIEWER_NAMES.random()
            
            // Generate questions based on job requirements and company difficulty
            val questions = generateQuestionsForJob(job)
            
            val interview = Interview(
                id = interviewId,
                jobId = jobId,
                interviewerName = interviewerName,
                questions = questions,
                feedbackMode = feedbackMode,
                job = job
            )
            
            activeInterviews[interviewId] = interview
            saveActiveInterview(interview)
            Result.success(interview)
        } catch (e: Exception) {
            Log.e("InterviewRepositoryImpl", "Failed to start interview: ${e.message}", e)
            Result.failure(Exception("Failed to start interview: ${e.message}", e))
        }
    }
    
    override suspend fun getNextQuestion(interview: Interview): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (interview.currentQuestionIndex >= interview.questions.size) {
                Result.failure(Exception("No more questions available"))
            } else {
                val question = interview.questions[interview.currentQuestionIndex].question
                Result.success(question)
            }
        } catch (e: Exception) {
            Log.e("InterviewRepositoryImpl", "Failed to get next question: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    fun getActiveInterview(interviewId: String): Interview? {
        return activeInterviews[interviewId] ?: loadActiveInterview(interviewId)
    }
    
    override suspend fun submitAnswer(interviewId: String, questionId: String, answer: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val interview = activeInterviews[interviewId] ?: return@withContext Result.failure(Exception("Interview not found"))
            
            val updatedQuestions = interview.questions.map { question ->
                if (question.id == questionId) {
                    question.copy(userAnswer = answer)
                } else {
                    question
                }
            }
            
            val updatedInterview = interview.copy(
                questions = updatedQuestions,
                currentQuestionIndex = interview.currentQuestionIndex + 1
            )
            
            activeInterviews[interviewId] = updatedInterview
            saveActiveInterview(updatedInterview)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("InterviewRepositoryImpl", "Failed to submit answer: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getFeedback(interviewId: String, questionId: String): Result<InterviewFeedback> = withContext(Dispatchers.IO) {
        try {
            val interview = activeInterviews[interviewId] ?: return@withContext Result.failure(Exception("Interview not found"))
            val question = interview.questions.find { it.id == questionId } ?: return@withContext Result.failure(Exception("Question not found"))
            val userAnswer = question.userAnswer ?: return@withContext Result.failure(Exception("No answer provided"))
            
            val job = interview.job
            
            val feedbackRequest = FeedbackRequest(
                job = JobDetailsForFeedback(
                    title = job.title,
                    description = job.description,
                    skills = job.skills
                ),
                questions = listOf(
                    FeedbackQuestion(
                        question = question.question,
                        answer = userAnswer
                    )
                )
            )
            
            var retryCount = 0
            while (retryCount < MAX_RETRIES) {
                Log.d("InterviewFeedbackDebug", "Sending feedback request (Attempt ${retryCount + 1}): ${gson.toJson(feedbackRequest)}")
                val response = quickQApi.getFeedback(feedbackRequest)
                
                if (response.isSuccessful && response.body() != null) {
                    val feedbackBody = response.body()!!
                    val feedbackString = withContext(Dispatchers.Default) { feedbackBody.feedback }
                    Log.d("InterviewFeedbackDebug", "Received feedback response body: ${gson.toJson(response.body())}")
                    if (feedbackString.isNotBlank()) {
                        val interviewFeedback = parseFeedbackString(feedbackString)
                        Log.d("InterviewFeedbackDebug", "Mapped individual feedback: ${interviewFeedback.overallComment}")
                        return@withContext Result.success(interviewFeedback)
                    } else {
                        Log.w("InterviewFeedbackDebug", "No individual feedback received in response. Retrying...")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown API error"
                    Log.e("InterviewFeedbackDebug", "API call failed to get individual feedback (Attempt ${retryCount + 1}): $errorMessage", response.errorBody()?.string()?.let { Exception(it) })
                }
                
                retryCount++
                if (retryCount < MAX_RETRIES) {
                    delay(RETRY_DELAY_MS)
                }
            }
            
            Result.failure(Exception("Failed to generate individual feedback after $MAX_RETRIES attempts."))
        } catch (e: Exception) {
            Log.e("InterviewFeedbackDebug", "Failed to generate individual feedback: ${e.message}", e)
            Result.failure(Exception("Failed to generate feedback: ${e.message}", e))
        }
    }
    
    override suspend fun completeInterview(interviewId: String): Result<InterviewSummary> = withContext(Dispatchers.IO) {
        try {
            val interview = activeInterviews[interviewId] ?: return@withContext Result.failure(Exception("Interview not found"))
            
            val answeredQuestions = interview.questions.filter { it.userAnswer != null }
            val totalQuestions = answeredQuestions.size
            
            val job = interview.job
            
            // Generate overall feedback using the new /feedback endpoint
            val feedbackQuestions = answeredQuestions.map { question ->
                FeedbackQuestion(
                    question = question.question,
                    answer = question.userAnswer ?: ""
                )
            }
            
            var overallFeedback: InterviewFeedback = parseFeedbackString("Failed to retrieve overall feedback.")
            var retryCount = 0
            
            while (retryCount < MAX_RETRIES) {
                val feedbackRequest = FeedbackRequest(
                    job = JobDetailsForFeedback(
                        title = job.title,
                        description = job.description,
                        skills = job.skills
                    ),
                    questions = feedbackQuestions
                )
                
                Log.d("InterviewFeedbackDebug", "Sending overall feedback request (Attempt ${retryCount + 1}): ${gson.toJson(feedbackRequest)}")
                val feedbackResponse = quickQApi.getFeedback(feedbackRequest)
                
                if (feedbackResponse.isSuccessful && feedbackResponse.body() != null) {
                    val feedbackBody = feedbackResponse.body()!!
                    Log.d("InterviewFeedbackDebug", "Received overall feedback response body: ${gson.toJson(feedbackResponse.body())}")
                    val combinedFeedback = withContext(Dispatchers.Default) { feedbackBody.feedback }
                    if (combinedFeedback.isNotBlank()) {
                        overallFeedback = parseFeedbackString(combinedFeedback)
                        break // Exit retry loop on success
                    } else {
                        Log.w("InterviewFeedbackDebug", "No overall feedback received in response. Retrying...")
                    }
                } else {
                    val errorMessage = feedbackResponse.errorBody()?.string() ?: "Unknown error"
                    Log.e("InterviewFeedbackDebug", "API call failed to get overall feedback (Attempt ${retryCount + 1}): $errorMessage", feedbackResponse.errorBody()?.string()?.let { Exception(it) })
                }
                
                retryCount++
                if (retryCount < MAX_RETRIES) {
                    delay(RETRY_DELAY_MS)
                }
            }
            
            val summary = InterviewSummary(
                totalQuestions = totalQuestions,
                averageScore = 0.0, // Placeholder, as score is not provided by new API
                totalDuration = System.currentTimeMillis() - (interview.questions.firstOrNull()?.timestamp ?: 0L),
                overallFeedback = overallFeedback
            )

            // Save to history
            saveInterviewToHistory(summary)
            
            // Remove from active interviews and SharedPreferences
            activeInterviews.remove(interviewId)
            clearActiveInterview()
            
            Result.success(summary)
        } catch (e: Exception) {
            Log.e("InterviewFeedbackDebug", "Failed to complete interview: ${e.message}", e)
            Result.failure(Exception("Failed to complete interview: ${e.message}", e))
        }
    }
    
    override suspend fun getInterviewHistory(): Result<List<InterviewSummary>> = withContext(Dispatchers.IO) {
        try {
            val historyJson = sharedPreferences.getString(Constants.KEY_INTERVIEW_HISTORY, "[]")
            val listType = object : TypeToken<List<InterviewSummary>>() {}.type
            val history: List<InterviewSummary> = withContext(Dispatchers.Default) { gson.fromJson(historyJson, listType) ?: emptyList() }
            Result.success(history)
        } catch (e: Exception) {
            Log.e("InterviewRepositoryImpl", "Failed to get interview history: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun updateInterviewFeedbackMode(interviewId: String, feedbackMode: FeedbackMode): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val interview = activeInterviews[interviewId] ?: return@withContext Result.failure(Exception("Interview not found"))
            val updatedInterview = interview.copy(feedbackMode = feedbackMode)
            activeInterviews[interviewId] = updatedInterview
            saveActiveInterview(updatedInterview)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("InterviewRepositoryImpl", "Failed to update feedback mode: ${e.message}", e)
            Result.failure(Exception("Failed to update feedback mode: ${e.message}", e))
        }
    }
    
    private suspend fun generateQuestionsForJob(job: Job): List<InterviewQuestion> = withContext(Dispatchers.IO) {
        val interviewConfig = Constants.getInterviewConfig(job.companyRating, job.experienceLevel.name)
        
        val request = QuestionGenerationRequest(
            job = JobDetailsForQuestionGeneration(
                title = job.title,
                description = job.description,
                skills = job.skills
            )
        )

        return@withContext try {
            val response = quickQApi.generateQuestions(request)
            if (response.isSuccessful && response.body() != null) {
                val questions = withContext(Dispatchers.Default) { response.body()!!.questions }
                if (questions.isNotEmpty()) {
                    questions.mapIndexed { index, questionString ->
                        InterviewQuestion(
                            id = UUID.randomUUID().toString(),
                            question = questionString.trim()
                        )
                    }.take(interviewConfig.questionCount) // Apply question count limit from config
                } else {
                    Log.w("InterviewRepositoryImpl", "No questions generated from API response for job: ${job.title}")
                    throw Exception("No questions generated from API response")
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown API error"
                Log.e("InterviewRepositoryImpl", "API call failed to generate questions: $errorMessage", response.errorBody()?.string()?.let { Exception(it) })
                throw Exception("API call failed: ${errorMessage}")
            }
        } catch (e: Exception) {
            Log.e("InterviewRepositoryImpl", "Failed to generate questions for job ${job.title}: ${e.message}", e)
            throw Exception("Failed to generate questions: ${e.message}", e)
        }
    }
    
    private fun saveInterviewToHistory(summary: InterviewSummary) {
        try {
            val historyJson = sharedPreferences.getString(Constants.KEY_INTERVIEW_HISTORY, "[]")
            val listType = object : TypeToken<List<InterviewSummary>>() {}.type
            val currentHistory: List<InterviewSummary> = gson.fromJson(historyJson, listType) ?: emptyList()
            val updatedHistory = currentHistory + summary
            val updatedHistoryJson = gson.toJson(updatedHistory)
            sharedPreferences.edit().putString(Constants.KEY_INTERVIEW_HISTORY, updatedHistoryJson).apply()
        } catch (e: Exception) {
            Log.e("InterviewRepositoryImpl", "Failed to save interview history: ${e.message}", e)
            // Log error but don't fail the interview completion
        }
    }
    
    private fun saveActiveInterview(interview: Interview) {
        try {
            val interviewJson = gson.toJson(interview)
            sharedPreferences.edit().putString(Constants.KEY_ACTIVE_INTERVIEW, interviewJson).commit()
        } catch (e: Exception) {
            Log.e("InterviewRepositoryImpl", "Failed to save active interview: ${e.message}", e)
            // Log error
        }
    }
    
    private fun loadActiveInterview(interviewId: String): Interview? {
        return try {
            val interviewJson = sharedPreferences.getString(Constants.KEY_ACTIVE_INTERVIEW, null)
            if (interviewJson != null) {
                val interview = gson.fromJson(interviewJson, Interview::class.java)
                // Ensure it's the correct interview being loaded
                if (interview.id == interviewId) {
                    activeInterviews[interviewId] = interview // Add to in-memory map
                    interview
                } else {
                    // Mismatch, clear it to avoid stale data
                    Log.w("InterviewRepositoryImpl", "Loaded active interview ID mismatch. Clearing stale data.")
                    clearActiveInterview()
                    null
                }
            } else {
                null
            }
        } catch (e: JsonSyntaxException) {
            Log.e("InterviewRepositoryImpl", "JSON parsing error for active interview. Clearing corrupted data.", e)
            // JSON parsing error, clear corrupted data
            clearActiveInterview()
            null
        } catch (e: Exception) {
            Log.e("InterviewRepositoryImpl", "Failed to load active interview: ${e.message}", e)
            // Other errors
            null
        }
    }
    
    private fun clearActiveInterview() {
        sharedPreferences.edit(commit = true) { remove(Constants.KEY_ACTIVE_INTERVIEW) }
    }

    private fun parseFeedbackString(feedbackText: String): InterviewFeedback {
        val strengths = mutableListOf<String>()
        val areasForImprovement = mutableListOf<String>()
        val suggestions = mutableListOf<String>()
        val overallComment = feedbackText.trim()

        return InterviewFeedback(
            score = 0, // Score is not provided by new API
            strengths = strengths,
            areasForImprovement = areasForImprovement,
            suggestions = suggestions,
            overallComment = overallComment
        )
    }
} 