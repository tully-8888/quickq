package com.tully.quickq.data.constants

import com.tully.quickq.domain.model.InterviewDifficulty

object Constants {
    const val QUICKQ_BASE_URL = "https://quickq69-732728700948.us-central1.run.app/"
    
    // SharedPreferences keys
    const val PREFS_NAME = "quickq_prefs"
    const val KEY_INTERVIEW_HISTORY = "interview_history"
    const val KEY_USER_PROFILE = "user_profile"
    const val KEY_ACTIVE_INTERVIEW = "active_interview"
    
    // Interview constants - now dynamic based on company
    const val BASE_QUESTIONS_COUNT = 5
    const val MAX_ADDITIONAL_QUESTIONS = 5
    
    // Company reputation mapping for interview difficulty
    val COMPANY_REPUTATION_MAP = mapOf(
        // FAANG and top tech companies
        "Google" to 5.0, "Apple" to 5.0, "Meta" to 5.0, "Amazon" to 4.8, "Netflix" to 4.9,
        "Microsoft" to 4.9, "Tesla" to 4.7, "SpaceX" to 4.8, "OpenAI" to 4.9, "Anthropic" to 4.8,
        
        // Other major tech companies
        "Uber" to 4.5, "Airbnb" to 4.6, "Stripe" to 4.7, "Spotify" to 4.5, "Slack" to 4.4,
        "Zoom" to 4.3, "Dropbox" to 4.4, "Twitter" to 4.2, "LinkedIn" to 4.6, "Salesforce" to 4.3,
        
        // Established companies
        "IBM" to 4.0, "Oracle" to 3.9, "Intel" to 4.1, "Cisco" to 4.0, "Adobe" to 4.2,
        "VMware" to 4.0, "ServiceNow" to 4.1, "Workday" to 4.0, "Palantir" to 4.3,
        
        // Financial tech
        "Goldman Sachs" to 4.4, "JPMorgan" to 4.2, "Morgan Stanley" to 4.3, "Citadel" to 4.6,
        "Two Sigma" to 4.5, "Jane Street" to 4.7, "DE Shaw" to 4.6,
        
        // Consulting
        "McKinsey" to 4.5, "BCG" to 4.4, "Bain" to 4.4, "Deloitte" to 4.0, "Accenture" to 3.8,
        
        // Startups and smaller companies (default range)
        "default" to 3.5
    )
    
    // Interview difficulty configuration
    fun getInterviewConfig(companyRating: Double, experienceLevel: String): InterviewConfig {
        val difficulty = when {
            companyRating >= 4.8 -> InterviewDifficulty.EXTREME
            companyRating >= 4.5 -> InterviewDifficulty.VERY_HARD
            companyRating >= 4.2 -> InterviewDifficulty.CHALLENGING
            companyRating >= 3.8 -> InterviewDifficulty.MODERATE
            else -> InterviewDifficulty.EASY
        }
        
        val questionCount = when (difficulty) {
            InterviewDifficulty.EXTREME -> BASE_QUESTIONS_COUNT + 5
            InterviewDifficulty.VERY_HARD -> BASE_QUESTIONS_COUNT + 4
            InterviewDifficulty.CHALLENGING -> BASE_QUESTIONS_COUNT + 3
            InterviewDifficulty.MODERATE -> BASE_QUESTIONS_COUNT + 2
            InterviewDifficulty.EASY -> BASE_QUESTIONS_COUNT + 1
        }
        
        val experienceMultiplier = when (experienceLevel.uppercase()) {
            "PRINCIPAL", "LEAD" -> 1.3
            "SENIOR" -> 1.2
            "MID" -> 1.0
            "JUNIOR" -> 0.8
            else -> 1.0
        }
        
        return InterviewConfig(
            difficulty = difficulty,
            questionCount = (questionCount * experienceMultiplier).toInt().coerceIn(5, 12),
            technicalQuestionRatio = when (difficulty) {
                InterviewDifficulty.EXTREME -> 0.8
                InterviewDifficulty.VERY_HARD -> 0.7
                InterviewDifficulty.CHALLENGING -> 0.6
                InterviewDifficulty.MODERATE -> 0.5
                InterviewDifficulty.EASY -> 0.4
            }
        )
    }
    
    data class InterviewConfig(
        val difficulty: InterviewDifficulty,
        val questionCount: Int,
        val technicalQuestionRatio: Double
    )
    
    // Mock interviewer names
    val INTERVIEWER_NAMES = listOf(
        "Sarah Johnson", "Michael Chen", "Emily Rodriguez", "David Kim",
        "Jessica Taylor", "Ryan Patel", "Amanda Foster", "Kevin Liu",
        "Sophia Martinez", "James Wilson", "Alex Thompson", "Maria Garcia",
        "Daniel Lee", "Rachel Brown", "Christopher Davis", "Lisa Wang"
    )
} 