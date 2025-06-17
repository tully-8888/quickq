package com.tully.quickq.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tully.quickq.presentation.screen.*
import com.tully.quickq.presentation.viewmodel.*
import com.tully.quickq.ui.theme.VisionPrimary
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

@Composable
fun QuickQNavigation(
    navController: NavHostController = rememberNavController(),
    appViewModel: AppViewModel = koinViewModel()
) {
    val appUiState by appViewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    
    // Show loading while checking user status
    if (!appUiState.userStatusChecked) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = VisionPrimary)
        }
        return
    }
    
    // Determine start destination based on user status
    val startDestination = if (appUiState.isFirstTimeUser) "onboarding" else "job_search"
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onOnboardingComplete = {
                    appViewModel.onOnboardingCompleted()
                    // Ensure navigation is explicitly on the main thread
                    // to prevent IllegalStateException related to lifecycle state.
                    coroutineScope.launch(Dispatchers.Main.immediate) {
                        navController.navigate("job_search") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable("job_search") {
            JobSearchScreen(
                onJobClick = { jobId ->
                    navController.navigate("job_detail/$jobId")
                },
                onProfileClick = {
                    navController.navigate("profile")
                }
            )
        }
        
        composable("profile") {
            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("job_detail/{jobId}") { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: return@composable
            JobDetailScreen(
                jobId = jobId,
                onBackClick = {
                    navController.popBackStack()
                },
                onStartInterview = { interviewId ->
                    navController.navigate("interview/$interviewId")
                }
            )
        }
        
        composable("interview/{interviewId}") { backStackEntry ->
            val interviewId = backStackEntry.arguments?.getString("interviewId") ?: return@composable
            InterviewScreen(
                interviewId = interviewId,
                onCompleted = {
                    navController.popBackStack("job_search", inclusive = false)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
} 