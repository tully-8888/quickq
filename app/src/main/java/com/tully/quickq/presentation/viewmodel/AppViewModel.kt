package com.tully.quickq.presentation.viewmodel

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tully.quickq.data.dto.JobSearchRequest
import com.tully.quickq.data.dto.JobSearchResponse
import com.tully.quickq.domain.model.User
import com.tully.quickq.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.tully.quickq.domain.model.*
import com.tully.quickq.domain.repository.JobRepository
import com.tully.quickq.domain.repository.InterviewRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job as CoroutineJob
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import com.tully.quickq.data.repository.InterviewRepositoryImpl

class AppViewModel(
    private val userRepository: UserRepository,
    private val jobRepository: JobRepository,
    private val interviewRepository: InterviewRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()
    
    // Job Search specific states and functions
    private val _jobSearchUiState = MutableStateFlow(JobSearchUiState())
    val jobSearchUiState: StateFlow<JobSearchUiState> = _jobSearchUiState.asStateFlow()

    private val _jobFilters = MutableStateFlow(JobFilters())
    val jobFilters: StateFlow<JobFilters> = _jobFilters.asStateFlow()

    private var searchJob: CoroutineJob? = null

    // Profile specific states and functions
    private val _profileUiState = MutableStateFlow(ProfileUiState())
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState.asStateFlow()

    // Interview specific states and functions
    private val _interviewUiState = MutableStateFlow(InterviewUiState())
    val interviewUiState: StateFlow<InterviewUiState> = _interviewUiState.asStateFlow()

    // Onboarding specific states and functions
    private val _onboardingUiState = MutableStateFlow(OnboardingUiState())
    val onboardingUiState: StateFlow<OnboardingUiState> = _onboardingUiState.asStateFlow()

    // Job Detail specific states and functions
    private val _jobDetailUiState = MutableStateFlow(JobDetailUiState())
    val jobDetailUiState: StateFlow<JobDetailUiState> = _jobDetailUiState.asStateFlow()

    init {
        checkUserStatus()

        // Initialize JobSearchViewModel specific init logic
        _jobSearchUiState.value = JobSearchUiState(
            searchQuery = "",
            jobs = emptyList(),
            isLoading = false,
            error = null,
            hasSearched = false,
            showWelcomeState = true,
            showFilters = false
        )

        // Initialize ProfileViewModel specific init logic
        loadUserProfile()

        // Initialize OnboardingViewModel specific init logic
        checkUserStatusOnboarding()

        // No direct init for InterviewViewModel as it needs interviewId
    }
    
    private fun checkUserStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val isFirstTime = userRepository.isFirstTimeUser()
                val currentUser = userRepository.getCurrentUser().getOrNull()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isFirstTimeUser = isFirstTime,
                    currentUser = currentUser,
                    userStatusChecked = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to check user status",
                    userStatusChecked = true
                )
            }
        }
    }
    
    fun onOnboardingCompleted() {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedUser = userRepository.getCurrentUser().getOrNull()
            _uiState.value = _uiState.value.copy(
                isFirstTimeUser = false,
                currentUser = updatedUser
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // Profile related functions
    fun loadUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            _profileUiState.value = _profileUiState.value.copy(isLoading = true, error = null)

            userRepository.getCurrentUser()
                .onSuccess { user ->
                    if (user != null) {
                        _profileUiState.value = _profileUiState.value.copy(
                            isLoading = false,
                            currentUser = user,
                            editedProfile = user.profile ?: UserProfile(role = ""),
                            error = null
                        )
                    } else {
                        _profileUiState.value = _profileUiState.value.copy(
                            isLoading = false,
                            error = "User not found"
                        )
                    }
                }
                .onFailure { exception ->
                    _profileUiState.value = _profileUiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load profile"
                    )
                }
        }
    }

    fun toggleEditMode() {
        val currentEditMode = _profileUiState.value.isEditing
        _profileUiState.value = _profileUiState.value.copy(
            isEditing = !currentEditMode,
            editedProfile = _profileUiState.value.currentUser?.profile ?: UserProfile(role = "")
        )
    }

    fun updateProfileRole(role: String) {
        val currentProfile = _profileUiState.value.editedProfile
        _profileUiState.value = _profileUiState.value.copy(
            editedProfile = currentProfile.copy(role = role)
        )
    }

    fun updateProfileSeniority(seniority: ExperienceLevel?) {
        val currentProfile = _profileUiState.value.editedProfile
        _profileUiState.value = _profileUiState.value.copy(
            editedProfile = currentProfile.copy(seniority = seniority)
        )
    }

    fun updateProfileSkills(skills: List<String>) {
        val currentProfile = _profileUiState.value.editedProfile
        _profileUiState.value = _profileUiState.value.copy(
            editedProfile = currentProfile.copy(skills = skills)
        )
    }

    fun updateProfileBio(bio: String) {
        val currentProfile = _profileUiState.value.editedProfile
        _profileUiState.value = _profileUiState.value.copy(
            editedProfile = currentProfile.copy(bio = bio)
        )
    }

    fun updateProfileLocation(location: String) {
        val currentProfile = _profileUiState.value.editedProfile
        _profileUiState.value = _profileUiState.value.copy(
            editedProfile = currentProfile.copy(location = location)
        )
    }

    fun saveProfile() {
        val editedProfile = _profileUiState.value.editedProfile

        if (editedProfile.role.isBlank()) {
            _profileUiState.value = _profileUiState.value.copy(error = "Role is required")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _profileUiState.value = _profileUiState.value.copy(isSaving = true, error = null)

            userRepository.updateUserProfile(editedProfile)
                .onSuccess { updatedUser ->
                    _profileUiState.value = _profileUiState.value.copy(
                        isSaving = false,
                        currentUser = updatedUser,
                        isEditing = false,
                        showSuccessMessage = true,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _profileUiState.value = _profileUiState.value.copy(
                        isSaving = false,
                        error = exception.message ?: "Failed to save profile"
                    )
                }
        }
    }

    fun cancelProfileEdit() {
        _profileUiState.value = _profileUiState.value.copy(
            isEditing = false,
            editedProfile = _profileUiState.value.currentUser?.profile ?: UserProfile(role = ""),
            error = null
        )
    }

    fun clearProfileError() {
        _profileUiState.value = _profileUiState.value.copy(error = null)
    }

    fun clearProfileSuccessMessage() {
        _profileUiState.value = _profileUiState.value.copy(showSuccessMessage = false)
    }

    fun retryLoadProfile() {
        loadUserProfile()
    }

    // Job Search related functions
    fun updateSearchQuery(query: String) {
        _jobSearchUiState.value = _jobSearchUiState.value.copy(
            searchQuery = query,
            showWelcomeState = query.isEmpty() && !_jobSearchUiState.value.hasSearched
        )
    }

    fun toggleJobFilters() {
        _jobSearchUiState.value = _jobSearchUiState.value.copy(
            showFilters = !_jobSearchUiState.value.showFilters
        )
    }

    fun updateJobFilters(newFilters: JobFilters) {
        _jobFilters.value = newFilters

        val currentQuery = _jobSearchUiState.value.searchQuery.trim()
        if (currentQuery.isNotEmpty()) {
            performSearch()
        }
    }

    fun clearJobFilters() {
        _jobFilters.value = JobFilters()

        val currentQuery = _jobSearchUiState.value.searchQuery.trim()
        if (currentQuery.isNotEmpty()) {
            performSearch()
        }
    }

    fun performSearch() {
        val query = _jobSearchUiState.value.searchQuery.trim()
        val currentFilters = _jobFilters.value

        if (query.isBlank() && currentFilters.isEmpty()) {
            _jobSearchUiState.value = _jobSearchUiState.value.copy(
                jobs = emptyList(),
                showWelcomeState = true,
                hasSearched = false,
                error = null
            )
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch(Dispatchers.IO) {
            _jobSearchUiState.value = _jobSearchUiState.value.copy(
                isLoading = true,
                error = null,
                hasSearched = true,
                showWelcomeState = false
            )

            try {
                val response = jobRepository.searchJobs(query, currentFilters)
                val jobs = response.getOrThrow()

                _jobSearchUiState.value = _jobSearchUiState.value.copy(
                    isLoading = false,
                    jobs = jobs,
                    error = null,
                    showWelcomeState = false
                )
            } catch (e: Exception) {
                _jobSearchUiState.value = _jobSearchUiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unknown error occurred",
                    jobs = emptyList(),
                    showWelcomeState = false
                )
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _jobSearchUiState.value = _jobSearchUiState.value.copy(
            searchQuery = "",
            jobs = emptyList(),
            hasSearched = false,
            error = null,
            showWelcomeState = true,
            showFilters = false
        )
        _jobFilters.value = JobFilters()
    }

    fun retrySearch() {
        val currentQuery = _jobSearchUiState.value.searchQuery.trim()
        val currentFilters = _jobFilters.value
        if (currentQuery.isNotBlank() || !currentFilters.isEmpty()) {
            performSearch()
        } else {
            clearSearch()
        }
    }

    // Interview related functions
    fun loadInterviewFromRepository(interviewId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Try to get the interview from the repository
            val interview = (interviewRepository as? InterviewRepositoryImpl)?.getActiveInterview(interviewId)
            if (interview != null) {
                loadInterview(interview)
                _interviewUiState.value = _interviewUiState.value.copy(
                    isFeedbackModeSelectionNeeded = (interview.currentQuestionIndex == 0 && interview.feedbackMode == FeedbackMode.END_OF_INTERVIEW)
                )
            } else {
                _interviewUiState.value = _interviewUiState.value.copy(
                    isLoading = false,
                    error = null,
                    isFeedbackModeSelectionNeeded = true
                )
            }
        }
    }

    fun loadInterview(interview: Interview) {
        _interviewUiState.value = _interviewUiState.value.copy(
            interview = interview,
            currentQuestion = interview.questions.getOrNull(interview.currentQuestionIndex)?.question ?: "",
            isLoading = false,
            isFeedbackModeSelectionNeeded = false
        )
    }

    fun setInterviewFeedbackMode(interviewId: String, mode: FeedbackMode) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentInterview = _interviewUiState.value.interview

            if (currentInterview == null) {
                _interviewUiState.value = _interviewUiState.value.copy(
                    error = "Cannot set feedback mode: Interview not loaded.",
                    isFeedbackModeSelectionNeeded = false
                )
                return@launch
            }

            interviewRepository.updateInterviewFeedbackMode(interviewId, mode)
                .onSuccess {
                    val updatedInterview = currentInterview.copy(feedbackMode = mode)
                    _interviewUiState.value = _interviewUiState.value.copy(
                        interview = updatedInterview,
                        isFeedbackModeSelectionNeeded = false,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _interviewUiState.value = _interviewUiState.value.copy(
                        isFeedbackModeSelectionNeeded = false,
                        error = exception.message ?: "Failed to set feedback mode"
                    )
                }
        }
    }

    fun submitAnswer(interviewId: String, answer: String) {
        val currentInterview = _interviewUiState.value.interview ?: return
        val currentQuestionId = currentInterview.questions.getOrNull(currentInterview.currentQuestionIndex)?.id ?: return

        viewModelScope.launch(Dispatchers.IO) {
            _interviewUiState.value = _interviewUiState.value.copy(isSubmittingAnswer = true, error = null)

            interviewRepository.submitAnswer(interviewId, currentQuestionId, answer)
                .onSuccess {
                    _interviewUiState.value = _interviewUiState.value.copy(
                        isSubmittingAnswer = false,
                        userAnswer = answer
                    )

                    if (currentInterview.feedbackMode == FeedbackMode.AFTER_EACH_QUESTION) {
                        getFeedbackForCurrentQuestion(interviewId, currentQuestionId)
                    } else {
                        moveToNextQuestion(interviewId)
                    }
                }
                .onFailure { exception ->
                    _interviewUiState.value = _interviewUiState.value.copy(
                        isSubmittingAnswer = false,
                        error = exception.message ?: "Failed to submit answer"
                    )
                }
        }
    }

    fun moveToNextQuestion(interviewId: String) {
        val currentInterview = _interviewUiState.value.interview ?: return

        viewModelScope.launch {
            if (currentInterview.currentQuestionIndex + 1 >= currentInterview.questions.size) {
                completeInterview(interviewId)
            } else {
                val nextIndex = currentInterview.currentQuestionIndex + 1
                val nextQuestion = currentInterview.questions[nextIndex].question

                val updatedInterview = currentInterview.copy(currentQuestionIndex = nextIndex)

                _interviewUiState.value = _interviewUiState.value.copy(
                    currentQuestion = nextQuestion,
                    userAnswer = "",
                    currentFeedback = null,
                    interview = updatedInterview
                )
            }
        }
    }

    fun completeInterview(interviewId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _interviewUiState.value = _interviewUiState.value.copy(isCompletingInterview = true, error = null)

            interviewRepository.completeInterview(interviewId)
                .onSuccess { summary ->
                    _interviewUiState.value = _interviewUiState.value.copy(
                        isCompletingInterview = false,
                        interviewSummary = summary,
                        isCompleted = true
                    )
                }
                .onFailure { exception ->
                    _interviewUiState.value = _interviewUiState.value.copy(
                        isCompletingInterview = false,
                        error = exception.message ?: "Failed to complete interview"
                    )
                }
        }
    }

    fun clearCurrentAnswer() {
        _interviewUiState.value = _interviewUiState.value.copy(userAnswer = "")
    }

    private fun getFeedbackForCurrentQuestion(interviewId: String, questionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _interviewUiState.value = _interviewUiState.value.copy(isLoadingFeedback = true, error = null)

            interviewRepository.getFeedback(interviewId, questionId)
                .onSuccess { feedback ->
                    _interviewUiState.value = _interviewUiState.value.copy(
                        isLoadingFeedback = false,
                        currentFeedback = feedback,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _interviewUiState.value = _interviewUiState.value.copy(
                        isLoadingFeedback = false,
                        error = exception.message ?: "Failed to get feedback",
                        currentFeedback = null
                    )
                }
        }
    }

    fun getCurrentQuestionNumber(): Int {
        val interview = _interviewUiState.value.interview ?: return 0
        return interview.currentQuestionIndex + 1
    }

    fun getTotalQuestions(): Int {
        val interview = _interviewUiState.value.interview ?: return 0
        return interview.questions.size
    }

    // Onboarding related functions
    private fun checkUserStatusOnboarding() {
        viewModelScope.launch(Dispatchers.IO) {
            _onboardingUiState.value = _onboardingUiState.value.copy(isLoading = true)

            userRepository.getCurrentUser()
                .onSuccess { user ->
                    if (user == null) {
                        userRepository.createUser()
                            .onSuccess { newUser ->
                                _onboardingUiState.value = _onboardingUiState.value.copy(
                                    isLoading = false,
                                    currentUser = newUser,
                                    currentStep = OnboardingStep.WELCOME
                                )
                            }
                            .onFailure { exception ->
                                _onboardingUiState.value = _onboardingUiState.value.copy(
                                    isLoading = false,
                                    error = exception.message ?: "Failed to create user"
                                )
                            }
                    } else if (user.isFirstTime) {
                        _onboardingUiState.value = _onboardingUiState.value.copy(
                            isLoading = false,
                            currentUser = user,
                            currentStep = OnboardingStep.WELCOME
                        )
                    } else {
                        _onboardingUiState.value = _onboardingUiState.value.copy(
                            isLoading = false,
                            currentUser = user,
                            onboardingCompleted = true
                        )
                    }
                }
                .onFailure { exception ->
                    _onboardingUiState.value = _onboardingUiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to check user status"
                    )
                }
        }
    }

    fun nextOnboardingStep() {
        val currentStep = _onboardingUiState.value.currentStep
        val nextStep = when (currentStep) {
            OnboardingStep.WELCOME -> OnboardingStep.ROLE_SELECTION
            OnboardingStep.ROLE_SELECTION -> OnboardingStep.SENIORITY_SELECTION
            OnboardingStep.SENIORITY_SELECTION -> OnboardingStep.SKILLS_SELECTION
            OnboardingStep.SKILLS_SELECTION -> OnboardingStep.PROFILE_COMPLETE
            OnboardingStep.PROFILE_COMPLETE -> OnboardingStep.PROFILE_COMPLETE
        }
        _onboardingUiState.value = _onboardingUiState.value.copy(currentStep = nextStep)
    }

    fun previousOnboardingStep() {
        val currentStep = _onboardingUiState.value.currentStep
        val previousStep = when (currentStep) {
            OnboardingStep.WELCOME -> OnboardingStep.WELCOME
            OnboardingStep.ROLE_SELECTION -> OnboardingStep.WELCOME
            OnboardingStep.SENIORITY_SELECTION -> OnboardingStep.ROLE_SELECTION
            OnboardingStep.SKILLS_SELECTION -> OnboardingStep.SENIORITY_SELECTION
            OnboardingStep.PROFILE_COMPLETE -> OnboardingStep.SKILLS_SELECTION
        }
        _onboardingUiState.value = _onboardingUiState.value.copy(currentStep = previousStep)
    }

    fun updateOnboardingRole(role: String) {
        _onboardingUiState.value = _onboardingUiState.value.copy(selectedRole = role)
    }

    fun updateOnboardingSeniority(seniority: ExperienceLevel?) {
        _onboardingUiState.value = _onboardingUiState.value.copy(selectedSeniority = seniority)
    }

    fun updateOnboardingSkills(skills: List<String>) {
        _onboardingUiState.value = _onboardingUiState.value.copy(selectedSkills = skills)
    }

    fun completeOnboarding() {
        val state = _onboardingUiState.value

        if (state.selectedRole.isBlank()) {
            _onboardingUiState.value = _onboardingUiState.value.copy(error = "Please select a role")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _onboardingUiState.value = _onboardingUiState.value.copy(isCompletingOnboarding = true, error = null)

            val profile = UserProfile(
                role = state.selectedRole,
                seniority = state.selectedSeniority,
                skills = state.selectedSkills
            )

            userRepository.updateUserProfile(profile)
                .onSuccess { 
                    userRepository.completeOnboarding()
                        .onSuccess { updatedUser ->
                            _onboardingUiState.value = _onboardingUiState.value.copy(
                                isCompletingOnboarding = false,
                                currentUser = updatedUser,
                                onboardingCompleted = true
                            )
                        }
                        .onFailure { exception ->
                            _onboardingUiState.value = _onboardingUiState.value.copy(
                                isCompletingOnboarding = false,
                                error = exception.message ?: "Failed to complete onboarding"
                            )
                        }
                }
                .onFailure { exception ->
                    _onboardingUiState.value = _onboardingUiState.value.copy(
                        isCompletingOnboarding = false,
                        error = exception.message ?: "Failed to save profile"
                    )
                }
        }
    }

    fun clearOnboardingError() {
        _onboardingUiState.value = _onboardingUiState.value.copy(error = null)
    }

    // Job Detail related functions
    fun loadJobDetail(jobId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _jobDetailUiState.value = _jobDetailUiState.value.copy(isLoading = true, error = null)
            jobRepository.getJobDetail(jobId)
                .onSuccess { job ->
                    _jobDetailUiState.value = _jobDetailUiState.value.copy(
                        isLoading = false,
                        job = job,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _jobDetailUiState.value = _jobDetailUiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load job details"
                    )
                }
        }
    }

    fun startInterview(jobId: String, feedbackMode: FeedbackMode) {
        viewModelScope.launch(Dispatchers.IO) {
            _jobDetailUiState.value = _jobDetailUiState.value.copy(isStartingInterview = true, error = null)
            interviewRepository.startInterview(jobId, feedbackMode)
                .onSuccess { interview ->
                    _jobDetailUiState.value = _jobDetailUiState.value.copy(
                        isStartingInterview = false,
                        startedInterview = interview,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _jobDetailUiState.value = _jobDetailUiState.value.copy(
                        isStartingInterview = false,
                        error = exception.message ?: "Failed to start interview"
                    )
                }
        }
    }

    fun clearStartedInterview() {
        _jobDetailUiState.value = _jobDetailUiState.value.copy(startedInterview = null)
    }

    fun retryLoadJobDetail(jobId: String) {
        loadJobDetail(jobId)
    }
}

data class AppUiState(
    val isLoading: Boolean = true,
    val isFirstTimeUser: Boolean = false,
    val currentUser: User? = null,
    val userStatusChecked: Boolean = false,
    val error: String? = null
)

@Stable
enum class SalaryRange(val displayName: String, val minSalary: Int, val maxSalary: Int) {
    ENTRY_LEVEL("$40K - $70K", 40000, 70000),
    MID_LEVEL("$70K - $120K", 70000, 120000),
    SENIOR_LEVEL("$120K - $180K", 120000, 180000),
    LEAD_LEVEL("$180K - $250K", 180000, 250000),
    EXECUTIVE_LEVEL("$250K+", 250000, Int.MAX_VALUE)
}

@Stable
data class JobFilters(
    val experienceLevels: Set<ExperienceLevel> = emptySet(),
    val jobTypes: Set<JobType> = emptySet(),
    val workEnvironments: Set<WorkEnvironment> = emptySet(),
    val companySizes: Set<CompanySize> = emptySet(),
    val locations: Set<String> = emptySet(),
    val salaryRanges: Set<SalaryRange> = emptySet(),
    val industries: Set<String> = emptySet(),
    val skills: Set<String> = emptySet()
) {
    fun isEmpty(): Boolean {
        return experienceLevels.isEmpty() && 
               jobTypes.isEmpty() && 
               workEnvironments.isEmpty() && 
               companySizes.isEmpty() && 
               locations.isEmpty() && 
               salaryRanges.isEmpty() && 
               industries.isEmpty() &&
               skills.isEmpty()
    }
    
    fun getActiveFilterCount(): Int {
        var count = 0
        if (experienceLevels.isNotEmpty()) count++
        if (jobTypes.isNotEmpty()) count++
        if (workEnvironments.isNotEmpty()) count++
        if (companySizes.isNotEmpty()) count++
        if (locations.isNotEmpty()) count++
        if (salaryRanges.isNotEmpty()) count++
        if (industries.isNotEmpty()) count++
        if (skills.isNotEmpty()) count++
        return count
    }
}

data class JobSearchUiState(
    val searchQuery: String = "",
    val jobs: List<Job> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false,
    val showWelcomeState: Boolean = true,
    val showFilters: Boolean = false
)

data class ProfileUiState(
    val currentUser: User? = null,
    val editedProfile: UserProfile = UserProfile(role = ""),
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val error: String? = null
)

data class InterviewUiState(
    val interview: Interview? = null,
    val currentQuestion: String = "",
    val userAnswer: String = "",
    val currentFeedback: InterviewFeedback? = null,
    val interviewSummary: InterviewSummary? = null,
    val isLoading: Boolean = true,
    val isSubmittingAnswer: Boolean = false,
    val isLoadingFeedback: Boolean = false,
    val isCompletingInterview: Boolean = false,
    val isCompleted: Boolean = false,
    val isFeedbackModeSelectionNeeded: Boolean = false,
    val error: String? = null
)

enum class OnboardingStep {
    WELCOME,
    ROLE_SELECTION,
    SENIORITY_SELECTION,
    SKILLS_SELECTION,
    PROFILE_COMPLETE
}

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val selectedRole: String = "",
    val selectedSeniority: ExperienceLevel? = null,
    val selectedSkills: List<String> = emptyList(),
    val isCompletingOnboarding: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val error: String? = null
)

data class JobDetailUiState(
    val job: Job? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isStartingInterview: Boolean = false,
    val startedInterview: Interview? = null
) 