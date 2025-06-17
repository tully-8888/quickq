package com.tully.quickq.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tully.quickq.domain.model.ExperienceLevel
import com.tully.quickq.domain.model.RoleSuggestions
import com.tully.quickq.presentation.viewmodel.AppViewModel
import com.tully.quickq.presentation.viewmodel.OnboardingUiState
import com.tully.quickq.presentation.viewmodel.OnboardingStep
import com.tully.quickq.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    onOnboardingComplete: suspend () -> Unit,
    appViewModel: AppViewModel = koinViewModel()
) {
    val uiState by appViewModel.onboardingUiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    
    // Handle onboarding completion
    LaunchedEffect(uiState.onboardingCompleted) {
        if (uiState.onboardingCompleted) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(1000) // Show completion state briefly
            onOnboardingComplete()
        }
    }
    
    // Handle error feedback
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    LinkedInScreenContainer {
        when {
            uiState.isLoading -> {
                LinkedInOnboardingLoadingState()
            }
            
            uiState.error != null -> {
                LinkedInOnboardingErrorState(
                    error = uiState.error ?: "Unknown error",
                    onRetry = { appViewModel.clearOnboardingError() }
                )
            }
            
            else -> {
                LinkedInOnboardingContent(
                    uiState = uiState,
                    appViewModel = appViewModel
                )
            }
        }
    }
}

@Composable
private fun LinkedInOnboardingContent(
    uiState: OnboardingUiState,
    appViewModel: AppViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(LinkedInDesignSystem.SpaceL)
    ) {
        // Progress indicator
        LinkedInOnboardingProgress(
            currentStep = uiState.currentStep,
            modifier = Modifier.padding(bottom = LinkedInDesignSystem.SpaceXL)
        )
        
        // Step content
        when (uiState.currentStep) {
            OnboardingStep.WELCOME -> {
                LinkedInWelcomeStep(
                    onNext = { appViewModel.nextOnboardingStep() }
                )
            }
            
            OnboardingStep.ROLE_SELECTION -> {
                LinkedInRoleSelectionStep(
                    selectedRole = uiState.selectedRole,
                    onRoleSelected = { appViewModel.updateOnboardingRole(it) },
                    onNext = { 
                        if (uiState.selectedRole.isNotBlank()) {
                            appViewModel.nextOnboardingStep()
                        }
                    },
                    onBack = { appViewModel.previousOnboardingStep() },
                    canProceed = uiState.selectedRole.isNotBlank()
                )
            }
            
            OnboardingStep.SENIORITY_SELECTION -> {
                LinkedInSenioritySelectionStep(
                    selectedSeniority = uiState.selectedSeniority,
                    onSenioritySelected = { appViewModel.updateOnboardingSeniority(it) },
                    onNext = { appViewModel.nextOnboardingStep() },
                    onBack = { appViewModel.previousOnboardingStep() }
                )
            }
            
            OnboardingStep.SKILLS_SELECTION -> {
                LinkedInSkillsSelectionStep(
                    selectedSkills = uiState.selectedSkills,
                    onSkillsUpdated = { appViewModel.updateOnboardingSkills(it) },
                    onNext = { appViewModel.nextOnboardingStep() },
                    onBack = { appViewModel.previousOnboardingStep() }
                )
            }
            
            OnboardingStep.PROFILE_COMPLETE -> {
                LinkedInProfileCompleteStep(
                    uiState = uiState,
                    onComplete = { appViewModel.completeOnboarding() },
                    onBack = { appViewModel.previousOnboardingStep() },
                    isCompleting = uiState.isCompletingOnboarding
                )
            }
        }
    }
}

@Composable
private fun LinkedInOnboardingProgress(
    currentStep: OnboardingStep,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        OnboardingStep.WELCOME,
        OnboardingStep.ROLE_SELECTION,
        OnboardingStep.SENIORITY_SELECTION,
        OnboardingStep.SKILLS_SELECTION,
        OnboardingStep.PROFILE_COMPLETE
    )
    
    val currentIndex = steps.indexOf(currentStep)
    val progress = (currentIndex + 1f) / steps.size
    
    LinkedInCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Setup Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Text(
                    text = "${currentIndex + 1} of ${steps.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = VisionPrimary,
                trackColor = VisionPrimary.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun LinkedInWelcomeStep(
    onNext: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(LinkedInDesignSystem.SpaceXXXL))
        
        LinkedInCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXL)
            ) {
                Text(
                    text = "👋",
                    style = MaterialTheme.typography.displayLarge
                )
                
                Text(
                    text = "Welcome to QuickQ!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Let's set up your profile to find the perfect job opportunities tailored just for you. This will only take a few minutes!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )
                
                LinkedInPrimaryButton(
                    text = "Get Started ✨",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNext()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "🚀 Your journey to the perfect job starts here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LinkedInRoleSelectionStep(
    selectedRole: String,
    onRoleSelected: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    canProceed: Boolean
) {
    val haptic = LocalHapticFeedback.current
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        LinkedInStepHeader(
            title = "What's your developer role?",
            subtitle = "Type in your primary developer role",
            emoji = "💼"
        )
        
        Spacer(modifier = Modifier.height(LinkedInDesignSystem.SpaceXL))
        
        LinkedInCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedRole,
                onValueChange = { onRoleSelected(it) },
                placeholder = { Text("e.g. ML Engineer") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(LinkedInDesignSystem.RadiusM),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { onNext() }
                )
            )
        }
        
        Spacer(modifier = Modifier.height(LinkedInDesignSystem.SpaceXL))
        
        LinkedInStepNavigation(
            onBack = onBack,
            onNext = onNext,
            canProceed = canProceed,
            nextText = "Continue"
        )
    }
}

@Composable
private fun LinkedInStepHeader(
    title: String,
    subtitle: String,
    emoji: String
) {
    LinkedInCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displaySmall
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun LinkedInStepNavigation(
    onBack: () -> Unit,
    onNext: () -> Unit,
    canProceed: Boolean,
    nextText: String
) {
    val haptic = LocalHapticFeedback.current
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
    ) {
        LinkedInSecondaryButton(
            text = "Back",
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onBack()
            },
            modifier = Modifier.weight(1f)
        )
        
        LinkedInPrimaryButton(
            text = nextText,
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onNext()
            },
            enabled = canProceed,
            modifier = Modifier.weight(2f)
        )
    }
}

@Composable
private fun LinkedInSenioritySelectionStep(
    selectedSeniority: ExperienceLevel?,
    onSenioritySelected: (ExperienceLevel?) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        LinkedInStepHeader(
            title = "Experience level?",
            subtitle = "Optional - helps us match you with suitable positions",
            emoji = "📈"
        )
        
        Spacer(modifier = Modifier.height(LinkedInDesignSystem.SpaceXL))
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
        ) {
            items(ExperienceLevel.entries.toTypedArray()) { level ->
                LinkedInBadge(
                    text = level.displayName,
                    isSelected = selectedSeniority == level,
                    onClick = { onSenioritySelected(level) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(LinkedInDesignSystem.SpaceXL))
        
        LinkedInStepNavigation(
            onBack = onBack,
            onNext = onNext,
            canProceed = true,
            nextText = "Continue"
        )
    }
}

@Composable
private fun LinkedInSkillsSelectionStep(
    selectedSkills: List<String>,
    onSkillsUpdated: (List<String>) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var customSkillInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    val filteredSkills = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            RoleSuggestions.popularSkills
        } else {
            RoleSuggestions.popularSkills.filter {
                it.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        LinkedInStepHeader(
            title = "Your skills & technologies",
            subtitle = "Optional - search and select the technologies you work with",
            emoji = "🛠️"
        )
        
        Spacer(modifier = Modifier.height(LinkedInDesignSystem.SpaceXL))
        
        LinkedInCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
            ) {
                LinkedInSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { /* No action needed here */ },
                    placeholder = "Search for skills (e.g. React)",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(LinkedInDesignSystem.SpaceL))
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (filteredSkills.isNotEmpty() && searchQuery.isBlank()) {
                item {
                    Text(
                        text = "💡 Popular technologies:",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            items(filteredSkills) { skill ->
                LinkedInChip(
                    text = skill,
                    onClick = {
                        val newSkills = if (selectedSkills.contains(skill)) {
                            selectedSkills - skill
                        } else {
                            selectedSkills + skill
                        }
                        onSkillsUpdated(newSkills.distinct())
                        coroutineScope.launch { delay(200); searchQuery = "" }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    isSelected = selectedSkills.contains(skill)
                )
            }
            
            if (searchQuery.isNotBlank() && !filteredSkills.any { it.equals(searchQuery, ignoreCase = true) }) {
                item {
                    LinkedInCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)) {
                            Text(
                                text = "Add Custom Skill",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            OutlinedTextField(
                                value = customSkillInput,
                                onValueChange = { customSkillInput = it },
                                placeholder = { Text("Enter your custom skill") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(LinkedInDesignSystem.RadiusM),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (customSkillInput.isNotBlank()) {
                                        val newSkills = selectedSkills + customSkillInput.trim()
                                        onSkillsUpdated(newSkills.distinct())
                                        customSkillInput = ""
                                        searchQuery = ""
                                    }
                                })
                            )
                            LinkedInPrimaryButton(
                                text = "Add Skill",
                                onClick = {
                                    if (customSkillInput.isNotBlank()) {
                                        val newSkills = selectedSkills + customSkillInput.trim()
                                        onSkillsUpdated(newSkills.distinct())
                                        customSkillInput = ""
                                        searchQuery = ""
                                    }
                                },
                                enabled = customSkillInput.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(LinkedInDesignSystem.SpaceXL))
        
        LinkedInStepNavigation(
            onBack = onBack,
            onNext = onNext,
            canProceed = true,
            nextText = "Continue"
        )
    }
}

@Composable
private fun LinkedInProfileCompleteStep(
    uiState: OnboardingUiState,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    isCompleting: Boolean
) {
    // Implementation using LinkedIn components...
    Column(modifier = Modifier.fillMaxWidth()) {
        LinkedInStepHeader(
            title = "Profile complete!",
            subtitle = "Review your information and start your job search",
            emoji = "🎉"
        )
        // ... profile summary cards
        LinkedInStepNavigation(
            onBack = onBack,
            onNext = onComplete,
            canProceed = !isCompleting,
            nextText = if (isCompleting) "Setting up..." else "Start Job Search 🚀"
        )
    }
}

@Composable
private fun LinkedInOnboardingLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LinkedInCard {
            Column(
                modifier = Modifier.padding(LinkedInDesignSystem.SpaceXXL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
            ) {
                CircularProgressIndicator(
                    color = VisionPrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(48.dp)
                )
                
                Text(
                    text = "🚀 Preparing your profile...",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun LinkedInOnboardingErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LinkedInCard {
            Column(
                modifier = Modifier.padding(LinkedInDesignSystem.SpaceXL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
            ) {
                Text(
                    text = "😔",
                    style = MaterialTheme.typography.displaySmall
                )
                
                Text(
                    text = "Setup Error",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                
                LinkedInPrimaryButton(
                    text = "Try Again",
                    onClick = onRetry
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinkedInChip(
    text: String,
    onClick: () -> Unit,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text, style = MaterialTheme.typography.bodyMedium) },
        modifier = modifier,
        leadingIcon = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize),
                    tint = VisionPrimary
                )
            }
        },
        trailingIcon = {
            if (onClose != null) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Remove skill",
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                        tint = VisionPrimary
                    )
                }
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isSelected) CardBackground else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            labelColor = if (isSelected) TextPrimary else TextSecondary,
            leadingIconContentColor = if (isSelected) VisionPrimary else TextSecondary,
            trailingIconContentColor = if (isSelected) VisionPrimary else TextSecondary
        )
    )
} 