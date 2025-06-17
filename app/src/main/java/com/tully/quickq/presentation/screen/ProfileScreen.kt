package com.tully.quickq.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tully.quickq.presentation.viewmodel.AppViewModel
import com.tully.quickq.presentation.viewmodel.ProfileUiState
import com.tully.quickq.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    appViewModel: AppViewModel = koinViewModel()
) {
    val uiState by appViewModel.profileUiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        appViewModel.loadUserProfile()
    }

    LinkedInScreenContainer {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
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
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onBackClick()
                            },
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
                            text = "My Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        ) { paddingValues ->
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = VisionPrimary)
                    }
                }
                
                uiState.currentUser != null -> {
                    val currentUser = uiState.currentUser!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .padding(LinkedInDesignSystem.SpaceL),
                        verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
                    ) {
                        // Profile Header
                        LinkedInCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
                            ) {
                                // Profile Avatar
                                LinkedInAvatar(
                                    imageUrl = null, // No profile image for now
                                    name = currentUser.profile?.role ?: "User",
                                    size = 80.dp
                                )
                                
                                Text(
                                    text = currentUser.profile?.role ?: "Role not set",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                
                                if (!currentUser.profile?.location.isNullOrBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXS)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = VisionPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = currentUser.profile?.location ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Seniority Section
                        if (currentUser.profile?.seniority != null) {
                            LinkedInCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
                                    ) {
                                        Text(
                                            text = "📈",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "Experience Level",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    }
                                    
                                    LinkedInBadge(
                                        text = currentUser.profile?.seniority?.let { 
                                            it.name.replace("_", " ").lowercase()
                                                .replaceFirstChar { char -> char.uppercase() }
                                        } ?: "Not specified",
                                        isSelected = true
                                    )
                                }
                            }
                        }
                        
                        // Skills Section
                        if (!currentUser.profile?.skills.isNullOrEmpty()) {
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
                                        ) {
                                            Text(
                                                text = "🛠️",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "Skills & Technologies",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary
                                            )
                                        }
                                        
                                        Text(
                                            text = "${currentUser.profile?.skills?.size ?: 0}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = VisionPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
                                    ) {
                                        items(currentUser.profile?.skills ?: emptyList()) { skill ->
                                            LinkedInBadge(
                                                text = skill,
                                                isSelected = true
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(LinkedInDesignSystem.SpaceL))
                    }
                }
                
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Profile not found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
} 