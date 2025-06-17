package com.tully.quickq.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tully.quickq.presentation.screen.*
import com.tully.quickq.presentation.viewmodel.*
import com.tully.quickq.ui.theme.*
import org.koin.androidx.compose.koinViewModel

/**
 * LINKEDIN-INSPIRED NAVIGATION SYSTEM
 * 
 * Professional networking navigation structure with:
 * - Home: Professional feed and networking updates
 * - Jobs: Career opportunities and job discovery
 * - Network: Connection management and people discovery
 * - Messages: Professional communication (placeholder)
 * - Profile: Personal professional brand management
 */

@Composable
fun LinkedInNavigation(
    navController: NavHostController = rememberNavController(),
    appViewModel: AppViewModel = koinViewModel()
) {
    val appUiState by appViewModel.uiState.collectAsStateWithLifecycle()
    
    // Show onboarding for first-time users
    if (!appUiState.userStatusChecked) {
        LinkedInLoadingScreen()
        return
    }
    
    if (appUiState.isFirstTimeUser) {
        OnboardingScreen(
            onOnboardingComplete = {
                appViewModel.onOnboardingCompleted()
            }
        )
        return
    }
    
    // Main LinkedIn-style interface
    LinkedInMainScreen(
        navController = navController,
        appViewModel = appViewModel
    )
}

@Composable
private fun LinkedInMainScreen(
    navController: NavHostController,
    appViewModel: AppViewModel
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "home"
    
    Scaffold(
        bottomBar = {
            LinkedInBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        // Pop up to start destination to avoid building up a large stack
                        popUpTo("home") {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when navigating back to a previously selected tab
                        restoreState = true
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // HOME - Professional Feed
            composable("home") {
                LinkedInHomeScreen(
                    onNavigateToProfile = { navController.navigate("profile") },
                    onNavigateToJobs = { navController.navigate("jobs") },
                    onNavigateToNetwork = { navController.navigate("network") }
                )
            }
            
            // JOBS - Career Opportunities
            composable("jobs") {
                LinkedInJobsScreen(
                    onJobClick = { jobId ->
                        navController.navigate("job_detail/$jobId")
                    },
                    onNavigateToProfile = { navController.navigate("profile") }
                )
            }
            
            // NETWORK - Professional Connections
            composable("network") {
                LinkedInNetworkScreen(
                    onProfileClick = { profileId ->
                        navController.navigate("profile_detail/$profileId")
                    },
                    onNavigateToMessages = { navController.navigate("messages") }
                )
            }
            
            // MESSAGES - Professional Communication (Placeholder)
            composable("messages") {
                LinkedInMessagesScreen()
            }
            
            // PROFILE - Personal Professional Brand
            composable("profile") {
                LinkedInProfileScreen(
                    isOwnProfile = true,
                    onNavigateToEdit = { navController.navigate("profile_edit") }
                )
            }
            
            // JOB DETAIL - Individual Job Posting
            composable("job_detail/{jobId}") { backStackEntry ->
                val jobId = backStackEntry.arguments?.getString("jobId") ?: return@composable
                LinkedInJobDetailScreen(
                    jobId = jobId,
                    onBackClick = { navController.popBackStack() },
                    onStartInterview = { interviewId ->
                        navController.navigate("interview/$interviewId")
                    },
                    onApplyClick = {
                        // Handle job application
                    }
                )
            }
            
            // PROFILE DETAIL - Other User's Profile
            composable("profile_detail/{profileId}") { backStackEntry ->
                val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
                LinkedInProfileScreen(
                    profileId = profileId,
                    isOwnProfile = false,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToMessages = { navController.navigate("messages") }
                )
            }
            
            // INTERVIEW - Interview Simulation (Existing Feature)
            composable("interview/{interviewId}") { backStackEntry ->
                val interviewId = backStackEntry.arguments?.getString("interviewId") ?: return@composable
                InterviewScreen(
                    interviewId = interviewId,
                    onCompleted = {
                        navController.popBackStack("jobs", inclusive = false)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            // PROFILE EDIT - Edit Personal Profile
            composable("profile_edit") {
                LinkedInProfileEditScreen(
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun LinkedInLoadingScreen() {
    LinkedInScreenContainer {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
            ) {
                CircularProgressIndicator(
                    color = VisionPrimary,
                    strokeWidth = 3.dp,
                    modifier = androidx.compose.ui.Modifier.size(48.dp)
                )
                
                Text(
                    text = "Loading your professional network...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        }
    }
}

// PLACEHOLDER SCREENS FOR LINKEDIN-STYLE INTERFACE
// These will be implemented with full LinkedIn-style components

@Composable
private fun LinkedInHomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToJobs: () -> Unit,
    onNavigateToNetwork: () -> Unit
) {
    LinkedInScreenContainer {
        // TODO: Implement with LinkedInHomeFeedLayout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LinkedInDesignSystem.SpaceL),
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
        ) {
            Text(
                text = "Professional Home Feed",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            
            LinkedInCard {
                Text(
                    text = "Welcome to your professional network! This is where you'll see updates from your connections, job recommendations, and industry insights.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
            ) {
                LinkedInPrimaryButton(
                    text = "View Jobs",
                    onClick = onNavigateToJobs,
                    modifier = androidx.compose.ui.Modifier.weight(1f)
                )
                
                LinkedInSecondaryButton(
                    text = "Grow Network",
                    onClick = onNavigateToNetwork,
                    modifier = androidx.compose.ui.Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LinkedInJobsScreen(
    onJobClick: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    LinkedInScreenContainer {
        // TODO: Implement with LinkedInJobsLayout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LinkedInDesignSystem.SpaceL),
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
        ) {
            Text(
                text = "Career Opportunities",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            
            LinkedInSearchBar(
                query = "",
                onQueryChange = { /* TODO: Implement search */ },
                onSearch = { /* TODO: Implement search */ },
                placeholder = "Search jobs, companies, locations..."
            )
            
            LinkedInCard {
                Text(
                    text = "Discover your next career opportunity! Browse personalized job recommendations based on your profile and preferences.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun LinkedInNetworkScreen(
    onProfileClick: (String) -> Unit,
    onNavigateToMessages: () -> Unit
) {
    LinkedInScreenContainer {
        // TODO: Implement with LinkedInNetworkLayout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LinkedInDesignSystem.SpaceL),
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
        ) {
            Text(
                text = "Professional Network",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            
            LinkedInCard {
                Text(
                    text = "Grow your professional network! Connect with colleagues, industry leaders, and discover new opportunities through meaningful relationships.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun LinkedInMessagesScreen() {
    LinkedInScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LinkedInDesignSystem.SpaceL),
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
        ) {
            Text(
                text = "Professional Messages",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            
            LinkedInCard {
                Text(
                    text = "Your professional conversations will appear here. Connect with recruiters, colleagues, and industry professionals.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun LinkedInProfileScreen(
    profileId: String? = null,
    isOwnProfile: Boolean = true,
    onBackClick: (() -> Unit)? = null,
    onNavigateToEdit: (() -> Unit)? = null,
    onNavigateToMessages: (() -> Unit)? = null
) {
    LinkedInScreenContainer {
        // TODO: Implement with LinkedInProfileLayout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LinkedInDesignSystem.SpaceL),
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
        ) {
            Text(
                text = if (isOwnProfile) "Your Professional Profile" else "Professional Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            
            LinkedInCard {
                Text(
                    text = if (isOwnProfile) {
                        "Manage your professional brand and showcase your career achievements. A complete profile helps you get discovered by recruiters and connections."
                    } else {
                        "View this professional's career background, skills, and experience. Connect to expand your network."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
            
            if (isOwnProfile && onNavigateToEdit != null) {
                LinkedInPrimaryButton(
                    text = "Edit Profile",
                    onClick = onNavigateToEdit,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LinkedInJobDetailScreen(
    jobId: String,
    onBackClick: () -> Unit,
    onStartInterview: (String) -> Unit,
    onApplyClick: () -> Unit
) {
    LinkedInScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LinkedInDesignSystem.SpaceL),
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
        ) {
            Text(
                text = "Job Details",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            
            LinkedInCard {
                Text(
                    text = "Detailed job information will be displayed here, including company details, requirements, benefits, and application instructions.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
            ) {
                LinkedInSecondaryButton(
                    text = "Practice Interview",
                    onClick = { onStartInterview("sample_interview") },
                    modifier = androidx.compose.ui.Modifier.weight(1f)
                )
                
                LinkedInPrimaryButton(
                    text = "Easy Apply",
                    onClick = onApplyClick,
                    modifier = androidx.compose.ui.Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LinkedInProfileEditScreen(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    LinkedInScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LinkedInDesignSystem.SpaceL),
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
        ) {
            Text(
                text = "Edit Your Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            
            LinkedInCard {
                Text(
                    text = "Update your professional information, add new experiences, and showcase your skills to potential connections and employers.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
            ) {
                LinkedInSecondaryButton(
                    text = "Cancel",
                    onClick = onBackClick,
                    modifier = androidx.compose.ui.Modifier.weight(1f)
                )
                
                LinkedInPrimaryButton(
                    text = "Save Changes",
                    onClick = onSaveClick,
                    modifier = androidx.compose.ui.Modifier.weight(1f)
                )
            }
        }
    }
} 