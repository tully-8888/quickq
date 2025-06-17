package com.tully.quickq.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tully.quickq.presentation.viewmodel.AppViewModel
import com.tully.quickq.presentation.viewmodel.JobDetailUiState
import com.tully.quickq.domain.model.FeedbackMode
import com.tully.quickq.domain.model.Job
import com.tully.quickq.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JobDetailScreen(
    jobId: String,
    onBackClick: () -> Unit,
    onStartInterview: (String) -> Unit,
    appViewModel: AppViewModel = koinViewModel()
) {
    val uiState by appViewModel.jobDetailUiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    
    // Handle interview start navigation
    LaunchedEffect(uiState.startedInterview) {
        uiState.startedInterview?.let { interview ->
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onStartInterview(interview.id)
            appViewModel.clearStartedInterview()
        }
    }

    // Load job detail from repository on screen entry
    LaunchedEffect(jobId) {
        appViewModel.loadJobDetail(jobId)
    }
    
    // Enhanced click handlers with feedback
    val onStartInterviewWithFeedback = remember(jobId, appViewModel) {
        {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            appViewModel.startInterview(jobId, FeedbackMode.END_OF_INTERVIEW)
        }
    }
    
    val onBackClickWithFeedback = remember(onBackClick) {
        {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onBackClick()
        }
    }

    LinkedInScreenContainer {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                LinkedInTopAppBar(
                    title = "Job Details",
                    onBackClick = onBackClickWithFeedback
                )
            }
        ) { paddingValues ->
            when {
                uiState.isLoading -> {
                    LinkedInLoadingContent(
                        message = "Loading your perfect opportunity...",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
                
                uiState.error != null -> {
                    uiState.error?.let { error ->
                        LinkedInErrorContent(
                            error = error,
                            onRetry = { appViewModel.retryLoadJobDetail(jobId) },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        )
                    }
                }
                
                uiState.job != null -> {
                    uiState.job?.let { job ->
                        LinkedInJobDetailContent(
                            job = job,
                            isStartingInterview = uiState.isStartingInterview,
                            onStartInterview = onStartInterviewWithFeedback,
                            scrollState = scrollState,
                            modifier = Modifier
                                .fillMaxSize() 
                                .padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinkedInTopAppBar(
    title: String,
    onBackClick: () -> Unit
) {
    LinkedInCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LinkedInDesignSystem.SpaceL, vertical = LinkedInDesignSystem.SpaceS)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(
                        color = VisionPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(LinkedInDesignSystem.RadiusS)
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = VisionPrimary
                )
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun LinkedInJobDetailContent(
    job: Job,
    isStartingInterview: Boolean,
    onStartInterview: () -> Unit,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(LinkedInDesignSystem.SpaceL),
        verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
    ) {
        // Job Header
        LinkedInJobHeader(job = job)
        
        // Job Description
        LinkedInJobDescriptionSection(job = job)
        
        // Requirements
       // LinkedInRequirementsSection(requirements = job.requirements)
        
        // Skills
        LinkedInSkillsSection(skills = job.skills)
        
        Spacer(modifier = Modifier.height(LinkedInDesignSystem.SpaceS))
        
        // Start Interview Button
        LinkedInStartInterviewButton(
            isLoading = isStartingInterview,
            onClick = onStartInterview
        )
        
        Spacer(modifier = Modifier.height(LinkedInDesignSystem.SpaceL))
    }
}

@Composable
private fun LinkedInJobHeader(
    job: Job
) {
    LinkedInCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
        ) {
            Text(
                text = job.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Text(
                text = job.company,
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS),
                modifier = Modifier.fillMaxWidth()
            ) {
                LinkedInInfoChip(
                    icon = Icons.Default.LocationOn,
                    text = job.location,
                    modifier = Modifier.weight(1f, false)
                )
                
                LinkedInInfoChip(
                    icon = Icons.Default.Work,
                    text = job.jobType.name.replace("_", " "),
                    modifier = Modifier.weight(1f, false)
                )
            }
            
            if (job.salaryRange != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(LinkedInDesignSystem.RadiusM),
                    color = VisionPrimary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(LinkedInDesignSystem.SpaceM),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXS)
                    ) {
                        Text(
                            text = job.salaryRange,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VisionPrimary
                        )
                        Text(
                            text = "💰",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkedInInfoChip(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(LinkedInDesignSystem.RadiusL),
        color = VisionPrimary.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(LinkedInDesignSystem.SpaceM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = VisionPrimary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LinkedInJobDescriptionSection(job: Job) {
    LinkedInSectionCard(title = "About the Role") {
        MarkdownText(
            markdownText = job.description,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            color = TextSecondary
        )
    }
}

@Composable
private fun LinkedInRequirementsSection(requirements: List<String>) {
    LinkedInSectionCard(title = "Requirements") {
        Column(
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
        ) {
            requirements.forEach { requirement ->
                LinkedInRequirementItem(text = requirement)
            }
        }
    }
}

@Composable
private fun LinkedInRequirementItem(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = VisionPrimary,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(top = LinkedInDesignSystem.SpaceS)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f),
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}

@Composable
private fun LinkedInSkillsSection(skills: List<String>) {
    LinkedInSectionCard(title = "Required Skills") {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS),
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
        ) {
            skills.forEach { skill ->
                Surface(
                    shape = RoundedCornerShape(LinkedInDesignSystem.RadiusM),
                    color = VisionPrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = skill,
                        style = MaterialTheme.typography.bodySmall,
                        color = VisionPrimary,
                        modifier = Modifier.padding(
                            horizontal = LinkedInDesignSystem.SpaceS,
                            vertical = LinkedInDesignSystem.SpaceXS
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkedInSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    LinkedInCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            content()
        }
    }
}

@Composable
private fun LinkedInStartInterviewButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    LinkedInPrimaryButton(
        text = if (isLoading) "🚀 Starting Your Interview..." else "Start Interview ✨",
        icon = if (!isLoading) Icons.Default.PlayArrow else null,
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    )
}

@Composable
private fun LinkedInLoadingContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(LinkedInDesignSystem.SpaceXL),
        contentAlignment = Alignment.Center
    ) {
        LinkedInCard {
            Column(
                modifier = Modifier.padding(LinkedInDesignSystem.SpaceXXL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXL)
            ) {
                CircularProgressIndicator(
                    color = VisionPrimary,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(56.dp)
                )
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "✨ This looks like a great opportunity!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
private fun LinkedInErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(LinkedInDesignSystem.SpaceXL),
        contentAlignment = Alignment.Center
    ) {
        LinkedInCard {
            Column(
                modifier = Modifier.padding(LinkedInDesignSystem.SpaceXXL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
            ) {
                Text(
                    text = "😔",
                    style = MaterialTheme.typography.displayMedium
                )
                
                Text(
                    text = "Failed to load job details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                LinkedInPrimaryButton(
                    text = "Try Again",
                    onClick = onRetry
                )
                
                Text(
                    text = "💪 Don't worry, we'll get this sorted!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
private fun LinkedInScreenContainer(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundGradientStart, BackgroundGradientEnd)
                )
            )
    ) {
        content()
    }
}

@Composable
fun MarkdownText(
    markdownText: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified
) {
    val annotatedString = remember(markdownText) {
        parseSimpleMarkdown(markdownText)
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        style = style,
        color = color,
        lineHeight = lineHeight
    )
}

private fun parseSimpleMarkdown(text: String): AnnotatedString {
    var cleanedText = text

    // Fix specific malformed duplicated titles with single asterisk in between
    cleanedText = cleanedText.replace("Weaknesses:*Weaknesses:**", "**Weaknesses:**")
    cleanedText = cleanedText.replace("Improvements:*Improvements:**", "**Improvements:**")

    // Fix specific malformed duplicated "Error Handling" pattern
    cleanedText = cleanedText.replace("Error Handling:*Error Handling:*Error Handling:*Error Handling:", "**Error Handling:**")

    // Remove empty feedback sections (Strengths, Weaknesses, Improvements)
    cleanedText = removeEmptyFeedbackSections(cleanedText)

    return buildAnnotatedString {
        var currentIndex = 0
        // Regex for bold text (e.g., **text**)
        val boldRegex = "\\*\\*(.*?)\\*\\*".toRegex(RegexOption.DOT_MATCHES_ALL)
        // Regex for H2 headings (e.g., ## text)
        val h2Regex = "^##\\s*(.*)".toRegex(RegexOption.MULTILINE)
        // Regex for bullet points (e.g., * text)
        val bulletRegex = "^\\*\\s*(.*)".toRegex(RegexOption.MULTILINE)

        // Combine and sort all matches by their starting position
        val matches = (boldRegex.findAll(cleanedText) + h2Regex.findAll(cleanedText) + bulletRegex.findAll(cleanedText))
            .sortedBy { it.range.first }
            .toList()

        matches.forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            val fullMatchText = matchResult.value

            // Append any text before the current match
            if (start > currentIndex) {
                append(cleanedText.substring(currentIndex, start))
            }

            when {
                // Handle H2 headings
                fullMatchText.startsWith("##") -> {
                    val headingText = matchResult.groupValues[1]
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                        append(headingText.trim()) // Trim heading text
                    }
                    append("\n") // Add newline after heading for better formatting
                }
                // Handle bold text
                fullMatchText.startsWith("**") && fullMatchText.endsWith("**") -> {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(matchResult.groupValues[1].trim()) // Trim bold text
                    }
                }
                // Handle bullet points
                fullMatchText.startsWith("*") && !fullMatchText.startsWith("**") -> {
                    val bulletText = matchResult.groupValues[1]
                    append("\u2022\t") // Unicode bullet character
                    append(bulletText.trim()) // Trim bullet text
                }
            }
            currentIndex = end
        }

        // Append any remaining text after the last match
        if (currentIndex < cleanedText.length) {
            append(cleanedText.substring(currentIndex))
        }
    }
}

private fun removeEmptyFeedbackSections(text: String): String {
    val lines = text.split("\n").toMutableList()
    val sectionsToRemove = mutableListOf<Int>()

    val sectionHeaders = listOf(
        "**Strengths:**",
        "**Weaknesses:**",
        "**Improvements:**"
    )

    for (i in lines.indices) {
        val line = lines[i].trim()
        if (line in sectionHeaders) {
            // Check if the next line is either another header or the end of the string, or just whitespace
            var contentFound = false
            for (j in i + 1 until lines.size) {
                val nextLine = lines[j].trim()
                if (nextLine.isNotBlank() && nextLine !in sectionHeaders) {
                    contentFound = true
                    break
                }
                if (nextLine in sectionHeaders) { // Next line is a header, so current section is empty
                    break
                }
            }
            if (!contentFound) {
                sectionsToRemove.add(i)
            }
        }
    }

    // Remove sections from bottom up to avoid index issues
    sectionsToRemove.sortedDescending().forEach { index ->
        lines.removeAt(index)
    }

    return lines.joinToString("\n")
} 
