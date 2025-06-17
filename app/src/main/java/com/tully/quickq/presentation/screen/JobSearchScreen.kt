package com.tully.quickq.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tully.quickq.domain.model.Job
import com.tully.quickq.domain.model.ExperienceLevel
import com.tully.quickq.domain.model.JobType
import com.tully.quickq.domain.model.WorkEnvironment
import com.tully.quickq.domain.model.CompanySize
import com.tully.quickq.domain.model.HiringUrgency
import com.tully.quickq.domain.model.InterviewDifficulty
import com.tully.quickq.presentation.viewmodel.AppViewModel
import com.tully.quickq.presentation.viewmodel.JobFilters
import com.tully.quickq.presentation.viewmodel.JobSearchUiState
import com.tully.quickq.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

// PERFORMANCE: Minimal data class for ultra-fast rendering
@Stable
private data class JobCardData(
    val job: Job
) {
    companion object {
        fun from(job: Job): JobCardData = JobCardData(job)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSearchScreen(
    onJobClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    appViewModel: AppViewModel = koinViewModel()
) {
    // Lifecycle-aware state collection for optimal performance
    val uiState by appViewModel.jobSearchUiState.collectAsStateWithLifecycle()
    val filters by appViewModel.jobFilters.collectAsStateWithLifecycle()
    
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    
    // PERFORMANCE: Pre-compute stable job card data with better caching
    val jobCardData = remember(uiState.jobs) {
        uiState.jobs.map { job -> JobCardData.from(job) }
    }
    
    // Stable callbacks memoized once for optimal performance
    val callbacks = remember(appViewModel) {
        JobSearchCallbacks(
            onSearchQueryChange = appViewModel::updateSearchQuery,
            onPerformSearch = { 
                appViewModel.performSearch()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            onClearSearch = { 
                appViewModel.clearSearch()
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            },
            onRetrySearch = appViewModel::retrySearch,
            onToggleFilters = { 
                appViewModel.toggleJobFilters()
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            },
            onFiltersChange = appViewModel::updateJobFilters,
            onClearFilters = { 
                appViewModel.clearJobFilters()
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            },
            onDismissFilters = appViewModel::toggleJobFilters,
            onJobClick = { jobId: String ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onJobClick(jobId)
            },
            onProfileClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onProfileClick()
            }
        )
    }
    
    LinkedInScreenContainer {
        // Main content with optimized LazyColumn
        OptimizedJobList(
            listState = listState,
            uiState = uiState,
            filters = filters,
            jobCardData = jobCardData,
            callbacks = callbacks,
            appViewModel = appViewModel
        )
        
        // Filter Bottom Sheet
        LinkedInFilterSheet(
            isVisible = uiState.showFilters,
            filters = filters,
            onFiltersChange = callbacks.onFiltersChange,
            onDismiss = callbacks.onDismissFilters,
            onClearAll = callbacks.onClearFilters,
            onApplyFilters = { /* Apply filters logic */ }
        )
    }
}

// Stable data class for callbacks to prevent recomposition
@Stable
private data class JobSearchCallbacks(
    val onSearchQueryChange: (String) -> Unit,
    val onPerformSearch: () -> Unit,
    val onClearSearch: () -> Unit,
    val onRetrySearch: () -> Unit,
    val onToggleFilters: () -> Unit,
    val onFiltersChange: (JobFilters) -> Unit,
    val onClearFilters: () -> Unit,
    val onDismissFilters: () -> Unit,
    val onJobClick: (String) -> Unit,
    val onProfileClick: () -> Unit
)

@Composable
private fun OptimizedJobList(
    listState: LazyListState,
    uiState: JobSearchUiState,
    filters: JobFilters,
    jobCardData: List<JobCardData>,
    callbacks: JobSearchCallbacks,
    appViewModel: AppViewModel
) {
    // PERFORMANCE: Static values for maximum efficiency
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp), // Fixed padding
        verticalArrangement = Arrangement.spacedBy(8.dp), // Reduced spacing
        // PERFORMANCE: Critical 120fps optimizations
        userScrollEnabled = true
    ) {
        // Search Bar with Filter Button
        item(key = "search_bar", contentType = "search_bar") {
            LinkedInSearchWithFilters(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = callbacks.onSearchQueryChange,
                onPerformSearch = callbacks.onPerformSearch,
                onClearSearch = callbacks.onClearSearch,
                onToggleFilters = callbacks.onToggleFilters,
                onProfileClick = callbacks.onProfileClick,
                isLoading = uiState.isLoading,
                activeFilterCount = filters.getActiveFilterCount()
            )
        }
        
        // Content based on state - using stable keys
        when {
            uiState.isLoading -> {
                item(key = "loading_state", contentType = "loading") {
                    LinkedInLoadingState(
                        message = "Searching for \"${uiState.searchQuery}\" jobs..."
                    )
                }
            }
            
            uiState.error != null -> {
                item(key = "error_state", contentType = "error") {
                    uiState.error?.let { error ->
                        LinkedInErrorState(
                            error = error,
                            onRetry = callbacks.onRetrySearch,
                            searchQuery = uiState.searchQuery
                        )
                    }
                }
            }
            
            uiState.showWelcomeState -> {
                item(key = "welcome_state", contentType = "welcome") {
                    LinkedInWelcomeSearchState(
                        onSearchSuggestionClick = { suggestion ->
                            callbacks.onSearchQueryChange(suggestion)
                            callbacks.onPerformSearch()
                        }
                    )
                }
            }
            
            uiState.jobs.isEmpty() && uiState.hasSearched -> {
                item(key = "empty_state", contentType = "empty") {
                    LinkedInEmptyState(
                        query = uiState.searchQuery,
                        hasSearched = uiState.hasSearched
                    )
                }
            }
            
            uiState.jobs.isNotEmpty() -> {
                // Search results header
                item(key = "results_header", contentType = "header") {
                    LinkedInSearchResultsHeader(
                        jobCount = uiState.jobs.size,
                        searchQuery = uiState.searchQuery,
                        activeFilterCount = filters.getActiveFilterCount()
                    )
                }
                
                // PERFORMANCE: Ultra-optimized job cards for 120fps
                items(
                    items = jobCardData,
                    key = { jobCard -> jobCard.job.id },
                    contentType = { "job_card" }
                ) { jobCard ->
                    // PERFORMANCE: Direct rendering without wrapper composable
                    LinkedInJobCard(
                        job = jobCard.job,
                        onJobClick = { callbacks.onJobClick(jobCard.job.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkedInSearchWithFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onPerformSearch: () -> Unit,
    onClearSearch: () -> Unit,
    onToggleFilters: () -> Unit,
    onProfileClick: () -> Unit,
    isLoading: Boolean,
    activeFilterCount: Int,
    modifier: Modifier = Modifier
) {
    LinkedInCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
        ) {
            // Main search row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
            ) {
                // Search input field
                LinkedInSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = onPerformSearch,
                    placeholder = "iOS, Android, React...",
                    modifier = Modifier.weight(1f)
                )
                
                // Search button
                LinkedInPrimaryButton(
                    text = if (isLoading) "..." else "Search",
                    onClick = onPerformSearch,
                    enabled = searchQuery.trim().isNotEmpty() && !isLoading,
                    modifier = Modifier.height(48.dp)
                )
            }
            
            // Filter button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinkedInSecondaryButton(
                        text = if (activeFilterCount > 0) "Filters ($activeFilterCount)" else "Filters",
                        icon = Icons.Default.FilterList,
                        onClick = onToggleFilters,
                        modifier = Modifier.height(40.dp)
                    )
                    
                    LinkedInSecondaryButton(
                        text = "Profile",
                        icon = Icons.Default.Person,
                        onClick = onProfileClick,
                        modifier = Modifier.height(40.dp)
                    )
                }
                
                // Active filters indicator
                if (activeFilterCount > 0) {
                    Text(
                        text = "$activeFilterCount active",
                        style = MaterialTheme.typography.labelSmall,
                        color = VisionPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkedInSearchResultsHeader(
    jobCount: Int,
    searchQuery: String,
    activeFilterCount: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXS)
    ) {
        Text(
            text = "Found $jobCount jobs for \"$searchQuery\"",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        
        if (activeFilterCount > 0) {
            Text(
                text = "Filtered by $activeFilterCount criteria",
                style = MaterialTheme.typography.bodySmall,
                color = VisionPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LinkedInWelcomeSearchState(
    onSearchSuggestionClick: (String) -> Unit = {}
) {
    // Stable popular keywords list to prevent recomposition
    val popularKeywords = remember { listOf("iOS", "Android", "React", "Python") }
    
    LinkedInCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
        ) {
            Text(
                text = "🔍",
                style = MaterialTheme.typography.displayMedium
            )
            
            Text(
                text = "Ready to find your dream job?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            
            Text(
                text = "Search for developer positions using keywords like iOS, Android, React, Python, or any technology you're passionate about.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            // Popular search suggestions
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXS)
            ) {
                Text(
                    text = "💡 Popular searches:",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXS)
                ) {
                    popularKeywords.forEach { keyword ->
                        key(keyword) {
                            LinkedInBadge(
                                text = keyword,
                                onClick = { onSearchSuggestionClick(keyword) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkedInLoadingState(message: String) {
    LinkedInCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
        ) {
            CircularProgressIndicator(
                color = VisionPrimary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(40.dp)
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "✨ Great things are coming your way",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun LinkedInErrorState(
    error: String,
    onRetry: () -> Unit,
    searchQuery: String
) {
    LinkedInCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
        ) {
            Text(
                text = "😔",
                style = MaterialTheme.typography.displaySmall
            )
            
            Text(
                text = "Oops! Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            
            if (searchQuery.isNotEmpty()) {
                Text(
                    text = "💡 Try different keywords or check your spelling",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
            
            LinkedInPrimaryButton(
                text = "Try Again",
                onClick = onRetry
            )
        }
    }
}

@Composable
private fun LinkedInEmptyState(
    query: String,
    hasSearched: Boolean
) {
    LinkedInCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
        ) {
            Text(
                text = if (query.isNotEmpty() && hasSearched) "🔍" else "💼",
                style = MaterialTheme.typography.displaySmall
            )
            
            Text(
                text = if (query.isNotEmpty() && hasSearched) {
                    "No jobs found for \"$query\""
                } else {
                    "No jobs available"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            
            Text(
                text = if (query.isNotEmpty() && hasSearched) {
                    "Try different keywords or browse our featured jobs"
                } else {
                    "Check back later for new opportunities"
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            
            if (query.isNotEmpty() && hasSearched) {
                Text(
                    text = "💡 Try keywords like: 'developer', 'engineer', 'iOS', 'Android', 'React'",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }
    }
}

// PERFORMANCE: Global cached mappers to avoid repeated allocations
private val experienceLevelMapper = mapOf(
    ExperienceLevel.JUNIOR to "Junior",
    ExperienceLevel.MID to "Mid-Level", 
    ExperienceLevel.SENIOR to "Senior",
    ExperienceLevel.LEAD to "Lead",
    ExperienceLevel.PRINCIPAL to "Principal"
)

private val jobTypeMapper = mapOf(
    JobType.FULL_TIME to "Full-time",
    JobType.PART_TIME to "Part-time",
    JobType.CONTRACT to "Contract", 
    JobType.INTERNSHIP to "Internship",
    JobType.REMOTE to "Remote"
)

private val workEnvironmentMapper = mapOf(
    WorkEnvironment.REMOTE to "Remote",
    WorkEnvironment.HYBRID to "Hybrid",
    WorkEnvironment.ON_SITE to "On-site"
)

private val companySizeMapper = mapOf(
    CompanySize.STARTUP to "Startup",
    CompanySize.SMALL to "Small",
    CompanySize.MEDIUM to "Medium",
    CompanySize.LARGE to "Large", 
    CompanySize.ENTERPRISE to "Enterprise"
)

@Composable
private fun LinkedInJobCard(
    job: Job,
    onJobClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // PERFORMANCE: Use pre-computed mappers instead of when expressions
    val experienceLevelText = experienceLevelMapper[job.experienceLevel] ?: "Unknown"
    val jobTypeText = jobTypeMapper[job.jobType] ?: "Unknown"
    val workEnvironmentText = workEnvironmentMapper[job.workEnvironment] ?: "Unknown"
    val companySizeText = companySizeMapper[job.companySize] ?: "Unknown"
    // PERFORMANCE: Pre-compute strings to avoid repeated concatenation
    val jobDetailsLine1 = "${job.location} • $experienceLevelText • $jobTypeText"
    val jobDetailsLine2 = "$workEnvironmentText • $companySizeText • ${job.industry}"
    val applicantText = "${job.applicantCount} applicants"
    val ratingText = String.format("%.1f", job.companyRating)
    
    // PERFORMANCE: Pre-compute skills text once
    val skillsText = if (job.skills.isNotEmpty()) {
        if (job.skills.size <= 4) {
            job.skills.joinToString(" • ")
        } else {
            "${job.skills.take(4).joinToString(" • ")} • +${job.skills.size - 4} more"
        }
    } else ""
    
    // PERFORMANCE: Ultra-lightweight Card for 120fps
    Card(
        onClick = onJobClick,
        modifier = modifier
            // PERFORMANCE: Fixed height eliminates layout measurement jitter
            .height(180.dp) // Reduced height for better performance
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp), // Fixed radius for better performance
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp), // Fixed padding for better performance
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Company and Job Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Job Title - PERFORMANCE: Fixed maxLines prevents layout shifts
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        // PERFORMANCE: Disable soft wrap for consistent layout
                        softWrap = true
                    )
                    
                    // Company Name
                    Text(
                        text = job.company,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VisionPrimary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

            }
            
            // PERFORMANCE: Simplified job details with cached text - fixed height
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = jobDetailsLine1,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = jobDetailsLine2,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Salary Range (if available) - PERFORMANCE: No emoji, consistent height
            Box(
                modifier = Modifier.height(20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                job.salaryRange?.let { salary ->
                    Text(
                        text = salary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessGlass.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // PERFORMANCE: Ultra-optimized skills display - fixed height
            Box(
                modifier = Modifier.height(32.dp), // Reduced height
                contentAlignment = Alignment.CenterStart
            ) {
                if (skillsText.isNotEmpty()) {
                    Text(
                        text = skillsText,
                        style = MaterialTheme.typography.bodySmall,
                        color = VisionPrimary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1, // Single line for better performance
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

        }
    }
} 