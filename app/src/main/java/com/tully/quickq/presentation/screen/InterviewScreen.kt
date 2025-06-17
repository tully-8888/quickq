package com.tully.quickq.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tully.quickq.domain.model.FeedbackMode
import com.tully.quickq.domain.model.Interview
import com.tully.quickq.domain.model.InterviewFeedback
import com.tully.quickq.domain.model.InterviewSummary
import com.tully.quickq.presentation.viewmodel.AppViewModel
import com.tully.quickq.presentation.viewmodel.InterviewUiState
import com.tully.quickq.ui.theme.*
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewScreen(
    interviewId: String,
    onCompleted: () -> Unit,
    onBackClick: () -> Unit,
    appViewModel: AppViewModel = koinViewModel()
) {
    val uiState by appViewModel.interviewUiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    var currentAnswer by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    val isLastQuestion = uiState.interview?.let { it.currentQuestionIndex == it.questions.size - 1 } ?: false

    // Load interview from repository on screen entry
    LaunchedEffect(interviewId) {
        appViewModel.loadInterviewFromRepository(interviewId)
    }

    LinkedInScreenContainer {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                LinkedInInterviewTopBar(
                    uiState = uiState,
                    appViewModel = appViewModel,
                    interviewId = interviewId,
                    onBackClick = onBackClick
                )
            }
        ) { paddingValues ->
            when {
                uiState.isLoading -> {
                    LinkedInInterviewLoadingState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        message = "🤖 Preparing your interview..."
                    )
                }
                
                uiState.isFeedbackModeSelectionNeeded -> {
                    LinkedInFeedbackModeSelectionView(
                        onModeSelected = { mode ->
                            appViewModel.setInterviewFeedbackMode(interviewId, mode)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
                
                uiState.isCompleted && uiState.interviewSummary != null -> {
                    uiState.interviewSummary?.let { summary ->
                        LinkedInInterviewCompleteScreen(
                            summary = summary,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            onCompleted = onCompleted
                        )
                    }
                }
                
                uiState.isCompletingInterview -> {
                    LinkedInInterviewLoadingState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        message = "🤖 Completing interview..."
                    )
                }
                
                uiState.interview != null -> {
                    LinkedInInterviewChatContent(
                        uiState = uiState,
                        currentAnswer = currentAnswer,
                        onAnswerChange = { currentAnswer = it },
                        onSubmitAnswer = {
                            if (currentAnswer.isNotBlank()) {
                                appViewModel.submitAnswer(interviewId, currentAnswer.trim())
                                currentAnswer = ""
                            }
                        },
                        onNextQuestion = { 
                            appViewModel.moveToNextQuestion(interviewId)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        listState = listState,
                        coroutineScope = coroutineScope,
                        isLastQuestion = isLastQuestion,
                        currentQuestionNumber = appViewModel.getCurrentQuestionNumber()
                    )
                }
                
                uiState.error != null -> {
                    uiState.error?.let { error ->
                        LinkedInInterviewErrorState(
                            error = error,
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

@Composable
private fun LinkedInFeedbackModeSelectionView(
    onModeSelected: (FeedbackMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(LinkedInDesignSystem.SpaceL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LinkedInCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎯",
                        style = MaterialTheme.typography.displaySmall
                    )
                }
                
                Text(
                    text = "Choose Feedback Mode",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Text(
                    text = "How would you like to receive feedback during the interview?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
                ) {
                    LinkedInFeedbackOptionCard(
                        title = "After Each Question",
                        description = "Get immediate feedback after every answer",
                        emoji = "⚡",
                        onClick = { onModeSelected(FeedbackMode.AFTER_EACH_QUESTION) }
                    )
                    
                    LinkedInFeedbackOptionCard(
                        title = "End of Interview",
                        description = "Receive comprehensive feedback at the end",
                        emoji = "📊",
                        onClick = { onModeSelected(FeedbackMode.END_OF_INTERVIEW) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkedInFeedbackOptionCard(
    title: String,
    description: String,
    emoji: String,
    onClick: () -> Unit
) {
    LinkedInCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = LinkedInDesignSystem.SpaceXS)
                )
            }
        }
    }
}

@Composable
private fun LinkedInInterviewTopBar(
    uiState: InterviewUiState,
    appViewModel: AppViewModel,
    interviewId: String,
    onBackClick: () -> Unit
) {
    LinkedInCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LinkedInDesignSystem.SpaceL, vertical = LinkedInDesignSystem.SpaceS)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
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
                
                Spacer(modifier = Modifier.width(LinkedInDesignSystem.SpaceM))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.interview?.interviewerName ?: "Interview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    if (uiState.interview != null) {
                        Text(
                            text = "Question ${appViewModel.getCurrentQuestionNumber()} of ${appViewModel.getTotalQuestions()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            // Progress Bar
            if (uiState.interview != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "${uiState.interview.currentQuestionIndex + 1} / ${uiState.interview.questions.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = VisionPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                val progress = (uiState.interview.currentQuestionIndex + 1f) / uiState.interview.questions.size.toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = VisionPrimary,
                    trackColor = VisionPrimary.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun LinkedInInterviewChatContent(
    uiState: InterviewUiState,
    currentAnswer: String,
    onAnswerChange: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState,
    coroutineScope: CoroutineScope,
    isLastQuestion: Boolean,
    currentQuestionNumber: Int
) {
    Column(
        modifier = modifier
    ) {
        LaunchedEffect(uiState.currentQuestion, uiState.userAnswer, uiState.currentFeedback) {
            if (listState.layoutInfo.totalItemsCount > 0) {
                coroutineScope.launch {
                    listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
                }
            }
        }

        // Chat Content
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL),
            contentPadding = PaddingValues(
                horizontal = LinkedInDesignSystem.SpaceL,
                vertical = LinkedInDesignSystem.SpaceM
            ),
            state = listState
        ) {
            // Current Question
            if (uiState.currentQuestion.isNotEmpty()) {
                item {
                    LinkedInInterviewerQuestionCard(
                        question = uiState.currentQuestion,
                        interviewerName = uiState.interview?.interviewerName ?: "Interviewer"
                    )
                }
            }
            
            // User Answer
            if (uiState.userAnswer.isNotEmpty()) {
                item {
                    LinkedInUserAnswerCard(answer = uiState.userAnswer)
                }
            }
            
            // Feedback Loading
            if (uiState.isLoadingFeedback) {
                item {
                    LinkedInFeedbackLoadingCard()
                }
            }
            
            // Feedback
            if (uiState.currentFeedback != null) {
                item {
                    LinkedInFeedbackCard(
                        feedback = uiState.currentFeedback,
                        onNextQuestion = onNextQuestion,
                        isLastQuestion = isLastQuestion,
                        currentQuestionNumber = currentQuestionNumber
                    )
                }
            }
        }
        
        // Answer Input Section - Fixed positioning directly above keyboard
        if (uiState.userAnswer.isEmpty() && uiState.currentFeedback == null && !uiState.isLoadingFeedback) {
            LinkedInAnswerInputSection(
                answer = currentAnswer,
                onAnswerChange = onAnswerChange,
                onSubmit = onSubmitAnswer,
                isSubmitting = uiState.isSubmittingAnswer,
                enabled = !uiState.isSubmittingAnswer,
                questionNumber = uiState.interview?.currentQuestionIndex?.plus(1) ?: 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LinkedInDesignSystem.SpaceL)

            )
        }
    }
}

@Composable
private fun LinkedInInterviewerQuestionCard(
    question: String,
    interviewerName: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        LinkedInCard(
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXS)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = VisionSecondary,
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                    Text(
                        text = interviewerName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = VisionSecondary
                    )
                }
                
                Text(
                    text = question,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
        }
    }
}

@Composable
private fun LinkedInUserAnswerCard(
    answer: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        LinkedInCard(
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXS)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = VisionPrimary,
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                    Text(
                        text = "You",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = VisionPrimary
                    )
                }
                
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
        }
    }
}

@Composable
private fun LinkedInFeedbackCard(
    feedback: InterviewFeedback,
    onNextQuestion: () -> Unit,
    isLastQuestion: Boolean,
    currentQuestionNumber: Int
) {
    LinkedInCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Feedback (Question $currentQuestionNumber)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            FeedbackDetailCard(
                feedback = feedback,
                modifier = Modifier.fillMaxWidth()
            )
            
            LinkedInPrimaryButton(
                text = if (isLastQuestion) "Finish Interview" else "Continue to Next Question",
                onClick = onNextQuestion,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FeedbackDetailCard(
    feedback: InterviewFeedback,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
    ) {
        // Overall Comment
        if (feedback.overallComment.isNotBlank()) {
            MarkdownText(
                markdown = feedback.overallComment,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LinkedInAnswerInputSection(
    answer: String,
    onAnswerChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isSubmitting: Boolean,
    enabled: Boolean,
    questionNumber: Int,
    modifier: Modifier = Modifier
) {
    LinkedInCard(
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Answer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Text(
                    text = "Question $questionNumber",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
            
            OutlinedTextField(
                value = answer,
                onValueChange = onAnswerChange,
                placeholder = { 
                    Text(
                        "Share your thoughts here...",
                        color = TextTertiary
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                enabled = enabled,
                maxLines = 4,
                shape = RoundedCornerShape(LinkedInDesignSystem.RadiusM),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (enabled && answer.isNotBlank()) {
                            onSubmit()
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = VisionPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            LinkedInPrimaryButton(
                text = if (isSubmitting) "Submitting..." else "Submit Answer",
                icon = if (!isSubmitting) Icons.AutoMirrored.Filled.Send else null,
                onClick = onSubmit,
                enabled = enabled && answer.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LinkedInInterviewCompleteScreen(
    summary: InterviewSummary,
    modifier: Modifier = Modifier,
    onCompleted: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(LinkedInDesignSystem.SpaceL)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(LinkedInDesignSystem.SpaceXXXL))
        
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎉",
                style = MaterialTheme.typography.displayMedium
            )
        }
        
        Spacer(modifier = Modifier.height(LinkedInDesignSystem.SpaceL))
        
        LinkedInCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
            ) {
                Text(
                    text = "Interview Complete!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Text(
                    text = "🌟 Congratulations! You've successfully completed the interview",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                LinkedInCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Allow it to take up available space
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(LinkedInDesignSystem.SpaceL),
                        verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
                    ) {
                        item {
                            Text(
                                text = "Your Performance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                LinkedInSummaryItem(
                                    label = "Questions",
                                    value = "${summary.totalQuestions}",
                                    emoji = "✅"
                                )
                            }
                        }
                        
                        item {
                            Text(
                                text = "Overall Feedback",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        
                        item {
                            FeedbackDetailCard(
                                feedback = summary.overallFeedback,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(LinkedInDesignSystem.SpaceM))
                
                LinkedInPrimaryButton(
                    text = "Done",
                    onClick = onCompleted,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LinkedInSummaryItem(
    label: String,
    value: String,
    emoji: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXS)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = VisionPrimary
            )
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleSmall
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LinkedInInterviewLoadingState(
    modifier: Modifier = Modifier,
    message: String
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LinkedInCard(
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(LinkedInDesignSystem.SpaceL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "✨ This will be great practice!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LinkedInInterviewErrorState(
    error: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(LinkedInDesignSystem.SpaceL),
        contentAlignment = Alignment.Center
    ) {
        LinkedInCard {
            Column(
                modifier = Modifier.padding(LinkedInDesignSystem.SpaceXL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "😔",
                        style = MaterialTheme.typography.displaySmall
                    )
                }
                
                Text(
                    text = "Interview Error",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                
                Text(
                    text = "💪 Don't worry, we'll get this sorted!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
private fun LinkedInFeedbackLoadingCard(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        LinkedInCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(LinkedInDesignSystem.SpaceM),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
            ) {
                CircularProgressIndicator(
                    color = VisionPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "🤖 Analyzing your answer...",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
} 