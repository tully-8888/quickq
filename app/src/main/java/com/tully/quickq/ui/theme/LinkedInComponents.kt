package com.tully.quickq.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * LINKEDIN-INSPIRED DESIGN SYSTEM 2025
 * 
 * Modern Professional Networking UI Components
 * Built from scratch with existing color palette adaptation
 * 
 * Key Design Principles:
 * - Clean, professional aesthetics
 * - Enhanced readability and accessibility
 * - Micro-interactions for engagement
 * - Mobile-first responsive design
 * - Information hierarchy optimization
 */

// Design System Constants
object LinkedInDesignSystem {
    // Spacing Scale
    val SpaceXXS = 2.dp
    val SpaceXS = 4.dp
    val SpaceS = 8.dp
    val SpaceM = 12.dp
    val SpaceL = 16.dp
    val SpaceXL = 20.dp
    val SpaceXXL = 24.dp
    val SpaceXXXL = 32.dp
    
    // Corner Radius Scale
    val RadiusXS = 4.dp
    val RadiusS = 6.dp
    val RadiusM = 8.dp
    val RadiusL = 12.dp
    val RadiusXL = 16.dp
    
    // Professional Card System
    val CardElevation = 1.dp
    val CardCornerRadius = RadiusM
    val CardPadding = SpaceL
    
    // Professional Button System
    val ButtonHeight = 36.dp
    val ButtonCornerRadius = RadiusXL
    val ButtonPaddingHorizontal = SpaceXL
}

// Stable State Classes
@Stable
@Immutable
data class LinkedInCardState(
    val isPressed: Boolean = false,
    val isHovered: Boolean = false,
    val showEngagementPulse: Boolean = false
)

@Stable
@Immutable
data class NetworkingMetrics(
    val connections: Int = 0,
    val views: Int = 0,
    val engagements: Int = 0
)

/**
 * CORE LINKEDIN CARD COMPONENT
 * 
 * Professional card system inspired by LinkedIn's 2025 design language
 * Features: Subtle elevation, professional spacing, accessibility-first design
 */
@Composable
fun LinkedInCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    elevation: Dp = LinkedInDesignSystem.CardElevation,
    cornerRadius: Dp = LinkedInDesignSystem.CardCornerRadius,
    contentPadding: PaddingValues = PaddingValues(LinkedInDesignSystem.CardPadding),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
    enableInteractions: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    
    var cardState by remember { mutableStateOf(LinkedInCardState()) }
    
    // Professional interaction animations
    val animatedElevation by animateDpAsState(
        targetValue = when {
            cardState.isPressed -> elevation * 0.5f
            cardState.isHovered -> elevation * 1.5f
            else -> elevation
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "cardElevation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (cardState.isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "cardScale"
    )
    
    // Professional click handler
    val clickHandler: (() -> Unit)? = remember(onClick, enabled, enableInteractions) {
        if (onClick != null && enabled && enableInteractions) {
            {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                cardState = cardState.copy(isPressed = true)
                onClick.invoke()
            }
        } else null
    }
    
    // Reset press state
    LaunchedEffect(cardState.isPressed) {
        if (cardState.isPressed) {
            delay(150)
            cardState = cardState.copy(isPressed = false)
        }
    }
    
    Card(
        modifier = modifier
            .then(if (clickHandler != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = clickHandler
                )
            } else Modifier),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = CardDefaults.outlinedCardBorder(enabled = true).copy(
            brush = Brush.linearGradient(listOf(borderColor, borderColor))
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * PROFESSIONAL PROFILE CARD
 * 
 * LinkedIn-style user profile display with professional networking metrics
 */
@Composable
fun LinkedInProfileCard(
    name: String,
    headline: String,
    location: String,
    profileImageUrl: String? = null,
    connectionCount: Int = 0,
    mutualConnections: Int = 0,
    isConnected: Boolean = false,
    onClick: () -> Unit = {},
    onConnectClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LinkedInCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(LinkedInDesignSystem.SpaceL)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
        ) {
            // Profile Header
            Row(
                horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM),
                verticalAlignment = Alignment.Top
            ) {
                // Profile Avatar
                LinkedInAvatar(
                    imageUrl = profileImageUrl,
                    name = name,
                    size = 56.dp
                )
                
                // Profile Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXS)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = headline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (location.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXS),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = "Location",
                                modifier = Modifier.size(14.dp),
                                tint = TextTertiary
                            )
                            Text(
                                text = location,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                        }
                    }
                }
            }
            
            // Professional Metrics
            if (connectionCount > 0 || mutualConnections > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (connectionCount > 0) {
                        LinkedInMetricBadge(
                            icon = Icons.Outlined.People,
                            count = connectionCount,
                            label = "connections"
                        )
                    }
                    
                    if (mutualConnections > 0) {
                        LinkedInMetricBadge(
                            icon = Icons.Outlined.Group,
                            count = mutualConnections,
                            label = "mutual"
                        )
                    }
                }
            }
            
            // Action Button
            LinkedInPrimaryButton(
                text = if (isConnected) "Message" else "Connect",
                icon = if (isConnected) Icons.Outlined.Message else Icons.Outlined.PersonAdd,
                onClick = onConnectClick,
                modifier = Modifier.fillMaxWidth(),
                buttonType = if (isConnected) LinkedInButtonType.Secondary else LinkedInButtonType.Primary
            )
        }
    }
}

/**
 * PROFESSIONAL JOB CARD
 * 
 * LinkedIn-style job posting display optimized for mobile browsing
 */
@Composable
fun LinkedInJobCard(
    job: com.tully.quickq.domain.model.Job,
    onJobClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onApplyClick: () -> Unit = {},
    isSaved: Boolean = false,
    modifier: Modifier = Modifier
) {
    LinkedInCard(
        modifier = modifier,
        onClick = onJobClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
        ) {
            // Job Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXS)
                ) {
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = VisionPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = job.company,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "${job.location} • ${job.workEnvironment.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                
                // Save Button
                IconButton(
                    onClick = onSaveClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isSaved) "Unsave job" else "Save job",
                        tint = if (isSaved) VisionPrimary else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Job Details - RESTORED
            Row(
                horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinkedInJobBadge(
                    text = job.experienceLevel.name.lowercase().replaceFirstChar { it.uppercase() },
                    icon = Icons.Outlined.Work
                )
                
                LinkedInJobBadge(
                    text = job.jobType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    icon = Icons.Outlined.Schedule
                )
                
                if (job.salaryRange != null) {
                    LinkedInJobBadge(
                        text = job.salaryRange,
                        icon = Icons.Outlined.Payments
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
            ) {
                LinkedInSecondaryButton(
                    text = "View Details",
                    onClick = onJobClick,
                    modifier = Modifier.weight(1f)
                )
                
                LinkedInPrimaryButton(
                    text = "Easy Apply",
                    onClick = onApplyClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * PROFESSIONAL AVATAR COMPONENT
 * 
 * LinkedIn-style profile picture with fallback initials
 */
@Composable
fun LinkedInAvatar(
    imageUrl: String?,
    name: String,
    size: Dp = 40.dp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val initials = remember(name) {
        name.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .take(2)
    }
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(VisionPrimary, VisionSecondary)
                )
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            // TODO: Replace with AsyncImage when available
            // For now, show initials as fallback
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * BUTTON SYSTEM
 * 
 * Professional button variants for LinkedIn-style interactions
 */
enum class LinkedInButtonType {
    Primary, Secondary, Ghost, Danger
}

@Composable
fun LinkedInPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    buttonType: LinkedInButtonType = LinkedInButtonType.Primary
) {
    val colors = when (buttonType) {
        LinkedInButtonType.Primary -> ButtonDefaults.buttonColors(
            containerColor = VisionPrimary,
            contentColor = Color.White,
            disabledContainerColor = InteractiveDisabled,
            disabledContentColor = TextTertiary
        )
        LinkedInButtonType.Secondary -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = VisionPrimary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = TextTertiary
        )
        LinkedInButtonType.Ghost -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = TextSecondary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = TextTertiary
        )
        LinkedInButtonType.Danger -> ButtonDefaults.buttonColors(
            containerColor = ErrorGlass,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = TextTertiary
        )
    }
    
    Button(
        onClick = onClick,
        modifier = modifier.height(LinkedInDesignSystem.ButtonHeight),
        enabled = enabled,
        colors = colors,
        shape = RoundedCornerShape(LinkedInDesignSystem.ButtonCornerRadius),
        border = if (buttonType == LinkedInButtonType.Secondary) {
            ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.linearGradient(listOf(VisionPrimary, VisionPrimary))
            )
        } else null,
        contentPadding = PaddingValues(horizontal = LinkedInDesignSystem.ButtonPaddingHorizontal)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun LinkedInSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    LinkedInPrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        enabled = enabled,
        buttonType = LinkedInButtonType.Secondary
    )
}

/**
 * SUPPORTING COMPONENTS
 */

@Composable
fun LinkedInBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    textColor: Color = TextSecondary,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    isSelected: Boolean = false
) {
    val finalBackgroundColor = if (isSelected) VisionPrimary.copy(alpha = 0.2f) else backgroundColor
    val finalTextColor = if (isSelected) VisionPrimary else textColor
    
    Surface(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(LinkedInDesignSystem.RadiusS),
        color = finalBackgroundColor
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = LinkedInDesignSystem.SpaceXL,
                vertical = LinkedInDesignSystem.SpaceL
            ),
            horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = finalTextColor
                )
            }
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = finalTextColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LinkedInFilterSheet(
    isVisible: Boolean,
    filters: com.tully.quickq.presentation.viewmodel.JobFilters,
    onFiltersChange: (com.tully.quickq.presentation.viewmodel.JobFilters) -> Unit,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit,
    onApplyFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LinkedInDesignSystem.SpaceL),
                verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
            ) {
                Text(
                    text = "Filter Jobs",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                // Experience Level Filter
                FilterSection(
                    title = "Experience Level",
                    items = com.tully.quickq.domain.model.ExperienceLevel.values().toList(),
                    selectedItems = filters.experienceLevels,
                    onSelectionChange = { selected ->
                        onFiltersChange(filters.copy(experienceLevels = selected))
                    },
                    itemDisplayName = { level ->
                        when (level) {
                            com.tully.quickq.domain.model.ExperienceLevel.JUNIOR -> "Junior"
                            com.tully.quickq.domain.model.ExperienceLevel.MID -> "Mid-Level"
                            com.tully.quickq.domain.model.ExperienceLevel.SENIOR -> "Senior"
                            com.tully.quickq.domain.model.ExperienceLevel.LEAD -> "Lead"
                            com.tully.quickq.domain.model.ExperienceLevel.PRINCIPAL -> "Principal"
                        }
                    }
                )
                
                // Skills Filter (newly added based on API)
                FilterSection(
                    title = "Skills",
                    items = listOf("Kotlin", "Java", "Swift", "Objective-C", "Python", "JavaScript", "TypeScript", "React", "Angular", "Vue", "Android", "iOS", "Backend", "Frontend", "Fullstack", "DevOps", "Cloud", "AWS", "Azure", "GCP", "SQL", "NoSQL", "Git", "Agile", "Scrum", "REST", "GraphQL", "Microservices", "Docker", "Kubernetes"), // Example skills, could be dynamic
                    selectedItems = filters.skills,
                    onSelectionChange = { selected ->
                        onFiltersChange(filters.copy(skills = selected))
                    },
                    itemDisplayName = { skill -> skill }
                )
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
                ) {
                    LinkedInSecondaryButton(
                        text = "Clear All",
                        onClick = {
                            onClearAll()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    LinkedInPrimaryButton(
                        text = "Apply (${filters.getActiveFilterCount()})",
                        onClick = {
                            onApplyFilters()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(LinkedInDesignSystem.SpaceXL))
            }
        }
    }
}

@Composable
private fun <T> FilterSection(
    title: String,
    items: List<T>,
    selectedItems: Set<T>,
    onSelectionChange: (Set<T>) -> Unit,
    itemDisplayName: (T) -> String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = LinkedInDesignSystem.SpaceS)
        )
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS),
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
        ) {
            items.forEach { item ->
                FilterChip(
                    onClick = {
                        val newSelection = if (selectedItems.contains(item)) {
                            selectedItems - item
                        } else {
                            selectedItems + item
                        }
                        onSelectionChange(newSelection)
                    },
                    label = {
                        Text(
                            text = itemDisplayName(item),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    selected = selectedItems.contains(item),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VisionPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = VisionPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun LinkedInMetricBadge(
    icon: ImageVector,
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(14.dp),
            tint = TextTertiary
        )
        
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary
        )
    }
}

@Composable
private fun LinkedInJobBadge(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(LinkedInDesignSystem.RadiusS),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = LinkedInDesignSystem.SpaceS,
                vertical = LinkedInDesignSystem.SpaceXS
            ),
            horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = TextSecondary
            )
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun LinkedInRatingBadge(
    rating: Double,
    maxRating: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXXS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Rating",
            modifier = Modifier.size(14.dp),
            tint = Color(0xFFFFB000)
        )
        
        Text(
            text = "%.1f".format(rating),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

@Composable
private fun LinkedInUrgencyBadge(
    urgency: com.tully.quickq.domain.model.HiringUrgency,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (urgency) {
        com.tully.quickq.domain.model.HiringUrgency.URGENT -> "Urgent" to ErrorGlass
        com.tully.quickq.domain.model.HiringUrgency.HIGH -> "High Priority" to WarningGlass
        com.tully.quickq.domain.model.HiringUrgency.MODERATE -> "Moderate" to InfoGlass
        com.tully.quickq.domain.model.HiringUrgency.LOW -> "Low Priority" to TextTertiary
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(LinkedInDesignSystem.RadiusS),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(
                horizontal = LinkedInDesignSystem.SpaceS,
                vertical = LinkedInDesignSystem.SpaceXXS
            )
        )
    }
}

/**
 * SEARCH AND FILTER COMPONENTS
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkedInSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        placeholder = {
            Text(
                text = placeholder,
                color = TextTertiary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = TextSecondary
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Outlined.Clear,
                        contentDescription = "Clear search",
                        tint = TextSecondary
                    )
                }
            }
        } else null,
        singleLine = true,
        shape = RoundedCornerShape(LinkedInDesignSystem.ButtonCornerRadius),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = VisionPrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}

/**
 * NAVIGATION COMPONENTS
 */

@Composable
fun LinkedInBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navigationItems = listOf(
        LinkedInNavigationItem("home", "Home", Icons.Outlined.Home, Icons.Filled.Home),
        LinkedInNavigationItem("jobs", "Jobs", Icons.Outlined.Work, Icons.Filled.Work),
        LinkedInNavigationItem("network", "Network", Icons.Outlined.People, Icons.Filled.People),
        LinkedInNavigationItem("messages", "Messages", Icons.Outlined.Message, Icons.Filled.Message),
        LinkedInNavigationItem("profile", "Profile", Icons.Outlined.Person, Icons.Filled.Person)
    )
    
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = TextPrimary
    ) {
        navigationItems.forEach { item ->
            val isSelected = currentRoute == item.route
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VisionPrimary,
                    selectedTextColor = VisionPrimary,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = VisionPrimary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

@Stable
@Immutable
private data class LinkedInNavigationItem(
    val route: String,
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
) 