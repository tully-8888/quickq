package com.tully.quickq.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkedInHomeFeedLayout(
    userProfile: LinkedInUserProfile,
    feedPosts: List<LinkedInFeedPost>,
    onProfileClick: () -> Unit = {},
    onCreatePostClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = LinkedInDesignSystem.SpaceL,
            vertical = LinkedInDesignSystem.SpaceM
        ),
        verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
    ) {
        // Professional Header Card
        item {
            LinkedInHomeFeedHeader(
                userProfile = userProfile,
                onProfileClick = onProfileClick,
                onCreatePostClick = onCreatePostClick
            )
        }
        
        // Feed Posts
        items(feedPosts, key = { it.id }) { post ->
            LinkedInFeedPostCard(
                post = post,
                onPostClick = { /* Handle post click */ }
            )
        }
    }
}

/**
 * PROFESSIONAL JOBS DISCOVERY LAYOUT
 * 
 * LinkedIn-style job search and career opportunities interface
 */
@Composable
fun LinkedInJobsLayout(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    jobs: List<com.tully.quickq.domain.model.Job>,
    onJobClick: (String) -> Unit = {},
    onFilterClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Search and Filter Header
        LinkedInJobsHeader(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onFilterClick = onFilterClick,
            jobCount = jobs.size
        )
        
        // Jobs List
        LazyColumn(
            contentPadding = PaddingValues(
                horizontal = LinkedInDesignSystem.SpaceL,
                vertical = LinkedInDesignSystem.SpaceM
            ),
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
        ) {
            items(jobs, key = { it.id }) { job ->
                LinkedInJobCard(
                    job = job,
                    onJobClick = { onJobClick(job.id) }
                )
            }
        }
    }
}

/**
 * PROFESSIONAL NETWORK LAYOUT
 * 
 * LinkedIn-style networking and connection management interface
 */
@Composable
fun LinkedInNetworkLayout(
    connectionRequests: List<LinkedInConnectionRequest>,
    suggestedConnections: List<LinkedInProfileSummary>,
    recentConnections: List<LinkedInProfileSummary>,
    onAcceptRequest: (String) -> Unit = {},
    onDeclineRequest: (String) -> Unit = {},
    onConnectClick: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = LinkedInDesignSystem.SpaceL,
            vertical = LinkedInDesignSystem.SpaceM
        ),
        verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
    ) {
        // Network Summary Card
        item {
            LinkedInNetworkSummaryCard(
                connectionCount = recentConnections.size,
                pendingRequestCount = connectionRequests.size
            )
        }
        
        // Connection Requests
        if (connectionRequests.isNotEmpty()) {
            item {
                LinkedInSectionHeader(
                    title = "Connection Requests",
                    subtitle = "${connectionRequests.size} pending",
                    onSeeAllClick = { /* Navigate to requests */ }
                )
            }
            
            items(connectionRequests, key = { it.id }) { request ->
                LinkedInConnectionRequestCard(
                    request = request,
                    onAccept = { onAcceptRequest(request.id) },
                    onDecline = { onDeclineRequest(request.id) },
                    onProfileClick = { onProfileClick(request.id) }
                )
            }
        }
        
        // Suggested Connections
        if (suggestedConnections.isNotEmpty()) {
            item {
                LinkedInSectionHeader(
                    title = "People you may know",
                    subtitle = "Based on your profile and activity",
                    onSeeAllClick = { /* Navigate to suggestions */ }
                )
            }
            
            items(suggestedConnections, key = { it.id }) { profile ->
                LinkedInProfileCard(
                    name = profile.name,
                    headline = profile.headline,
                    location = profile.location,
                    profileImageUrl = profile.imageUrl,
                    connectionCount = profile.connectionCount,
                    mutualConnections = profile.mutualConnections,
                    onClick = { onProfileClick(profile.id) },
                    onConnectClick = { onConnectClick(profile.id) }
                )
            }
        }
    }
}

/**
 * PROFESSIONAL PROFILE LAYOUT
 * 
 * LinkedIn-style user profile display with career information
 */
@Composable
fun LinkedInProfileLayout(
    profile: LinkedInUserProfile,
    isOwnProfile: Boolean = false,
    isConnected: Boolean = false,
    onEditClick: () -> Unit = {},
    onConnectClick: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = LinkedInDesignSystem.SpaceL,
            vertical = LinkedInDesignSystem.SpaceM
        ),
        verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
    ) {
        // Profile Header
        item {
            LinkedInProfileHeaderCard(
                profile = profile,
                isOwnProfile = isOwnProfile,
                isConnected = isConnected,
                onEditClick = onEditClick,
                onConnectClick = onConnectClick,
                onMessageClick = onMessageClick
            )
        }
        
        // About Section
        if (profile.bio.isNotEmpty()) {
            item {
                LinkedInProfileAboutCard(
                    bio = profile.bio,
                    isOwnProfile = isOwnProfile,
                    onEditClick = onEditClick
                )
            }
        }
        
        // Experience Section
        if (profile.experience.isNotEmpty()) {
            item {
                LinkedInProfileExperienceCard(
                    experience = profile.experience,
                    isOwnProfile = isOwnProfile,
                    onEditClick = onEditClick
                )
            }
        }
        
        // Skills Section
        if (profile.skills.isNotEmpty()) {
            item {
                LinkedInProfileSkillsCard(
                    skills = profile.skills,
                    isOwnProfile = isOwnProfile,
                    onEditClick = onEditClick
                )
            }
        }
    }
}

/**
 * SUPPORTING COMPONENT IMPLEMENTATIONS
 */

@Composable
private fun LinkedInHomeFeedHeader(
    userProfile: LinkedInUserProfile,
    onProfileClick: () -> Unit,
    onCreatePostClick: () -> Unit
) {
    LinkedInCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
        ) {
            // User Info Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinkedInAvatar(
                    imageUrl = userProfile.imageUrl,
                    name = userProfile.name,
                    size = 48.dp,
                    onClick = onProfileClick
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXXS)
                ) {
                    Text(
                        text = "Welcome back, ${userProfile.firstName}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    Text(
                        text = userProfile.headline,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Create Post Button
            LinkedInSecondaryButton(
                text = "Share an update...",
                icon = Icons.Outlined.Edit,
                onClick = onCreatePostClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LinkedInFeedPostCard(
    post: LinkedInFeedPost,
    onPostClick: () -> Unit
) {
    LinkedInCard(
        onClick = onPostClick
    ) {
        Column {
            // Post Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LinkedInDesignSystem.SpaceL),
                horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinkedInAvatar(
                    imageUrl = post.authorImageUrl,
                    name = post.authorName,
                    size = 40.dp
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXXS)
                ) {
                    Text(
                        text = post.authorName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    Text(
                        text = post.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
            
            // Post Content
            if (post.content.isNotEmpty()) {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = LinkedInDesignSystem.SpaceL),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun LinkedInJobsHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    jobCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(LinkedInDesignSystem.SpaceL),
        verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
    ) {
        // Search Bar
        Row(
            horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinkedInSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = { /* Handle search */ },
                placeholder = "Search jobs, companies...",
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(LinkedInDesignSystem.RadiusL)
                    )
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = "Filter jobs",
                    tint = VisionPrimary
                )
            }
        }
        
        // Results Summary
        Text(
            text = "$jobCount jobs found",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun LinkedInSectionHeader(
    title: String,
    subtitle: String? = null,
    onSeeAllClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXXS)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        
        onSeeAllClick?.let {
            TextButton(onClick = it) {
                Text(
                    text = "See all",
                    style = MaterialTheme.typography.labelMedium,
                    color = VisionPrimary
                )
            }
        }
    }
}

@Composable
private fun LinkedInQuickStat(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXS)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = VisionPrimary
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// Data Classes for LinkedIn-style Components
@Stable
@Immutable
data class LinkedInUserProfile(
    val id: String,
    val name: String,
    val firstName: String,
    val headline: String,
    val bio: String,
    val location: String,
    val connectionCount: Int,
    val experience: List<LinkedInExperience>,
    val skills: List<String>,
    val imageUrl: String? = null
)

@Stable
@Immutable
data class LinkedInProfileSummary(
    val id: String,
    val name: String,
    val headline: String,
    val location: String,
    val imageUrl: String? = null,
    val connectionCount: Int = 0,
    val mutualConnections: Int = 0
)

@Stable
@Immutable
data class LinkedInFeedPost(
    val id: String,
    val authorName: String,
    val authorImageUrl: String? = null,
    val content: String,
    val timestamp: String
)

@Stable
@Immutable
data class LinkedInConnectionRequest(
    val id: String,
    val name: String,
    val headline: String,
    val imageUrl: String? = null,
    val mutualConnections: Int = 0,
    val message: String? = null
)

@Stable
@Immutable
data class LinkedInExperience(
    val id: String,
    val title: String,
    val company: String,
    val startDate: String,
    val endDate: String? = null,
    val description: String? = null,
    val location: String? = null
)

// Additional Supporting Components
@Composable
private fun LinkedInMiniProfileCard(
    profile: LinkedInProfileSummary,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(140.dp),
        shape = RoundedCornerShape(LinkedInDesignSystem.RadiusM),
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder(enabled = true)
    ) {
        Column(
            modifier = Modifier.padding(LinkedInDesignSystem.SpaceM),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
        ) {
            LinkedInAvatar(
                imageUrl = profile.imageUrl,
                name = profile.name,
                size = 40.dp
            )
            
            Text(
                text = profile.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = profile.headline,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            LinkedInSecondaryButton(
                text = "Connect",
                onClick = onConnectClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LinkedInNetworkSummaryCard(
    connectionCount: Int,
    pendingRequestCount: Int,
    modifier: Modifier = Modifier
) {
    LinkedInCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LinkedInQuickStat(
                label = "Connections",
                value = connectionCount.toString(),
                icon = Icons.Outlined.People
            )
            
            if (pendingRequestCount > 0) {
                LinkedInQuickStat(
                    label = "Pending",
                    value = pendingRequestCount.toString(),
                    icon = Icons.Outlined.PersonAdd
                )
            }
        }
    }
}

@Composable
private fun LinkedInConnectionRequestCard(
    request: LinkedInConnectionRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LinkedInCard(
        modifier = modifier,
        onClick = onProfileClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
        ) {
            // Request Header
            Row(
                horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM),
                verticalAlignment = Alignment.Top
            ) {
                LinkedInAvatar(
                    imageUrl = request.imageUrl,
                    name = request.name,
                    size = 48.dp
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXS)
                ) {
                    Text(
                        text = request.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    Text(
                        text = request.headline,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (request.mutualConnections > 0) {
                        Text(
                            text = "${request.mutualConnections} mutual connections",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
            }
            
            // Message if present
            request.message?.let { message ->
                Text(
                    text = "\"$message\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
            ) {
                LinkedInSecondaryButton(
                    text = "Decline",
                    onClick = onDecline,
                    modifier = Modifier.weight(1f)
                )
                
                LinkedInPrimaryButton(
                    text = "Accept",
                    onClick = onAccept,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LinkedInProfileHeaderCard(
    profile: LinkedInUserProfile,
    isOwnProfile: Boolean,
    isConnected: Boolean,
    onEditClick: () -> Unit,
    onConnectClick: () -> Unit,
    onMessageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LinkedInCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
        ) {
            // Profile Info
            Row(
                horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL),
                verticalAlignment = Alignment.Top
            ) {
                LinkedInAvatar(
                    imageUrl = profile.imageUrl,
                    name = profile.name,
                    size = 80.dp
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
                ) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    Text(
                        text = profile.headline,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    
                    if (profile.location.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceXS),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = "Location",
                                modifier = Modifier.size(16.dp),
                                tint = TextTertiary
                            )
                            Text(
                                text = profile.location,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextTertiary
                            )
                        }
                    }
                    
                    Text(
                        text = "${profile.connectionCount}+ connections",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VisionPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
            ) {
                if (isOwnProfile) {
                    LinkedInPrimaryButton(
                        text = "Edit Profile",
                        icon = Icons.Outlined.Edit,
                        onClick = onEditClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    if (isConnected) {
                        LinkedInPrimaryButton(
                            text = "Message",
                            icon = Icons.Outlined.Message,
                            onClick = onMessageClick,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        LinkedInPrimaryButton(
                            text = "Connect",
                            icon = Icons.Outlined.PersonAdd,
                            onClick = onConnectClick,
                            modifier = Modifier.weight(1f)
                        )
                        
                        LinkedInSecondaryButton(
                            text = "Message",
                            icon = Icons.Outlined.Message,
                            onClick = onMessageClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkedInProfileAboutCard(
    bio: String,
    isOwnProfile: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LinkedInCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                if (isOwnProfile) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit about",
                            tint = TextSecondary
                        )
                    }
                }
            }
            
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun LinkedInProfileExperienceCard(
    experience: List<LinkedInExperience>,
    isOwnProfile: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LinkedInCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceL)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Experience",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                if (isOwnProfile) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add experience",
                            tint = TextSecondary
                        )
                    }
                }
            }
            
            experience.forEach { exp ->
                LinkedInExperienceItem(experience = exp)
            }
        }
    }
}

@Composable
private fun LinkedInProfileSkillsCard(
    skills: List<String>,
    isOwnProfile: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LinkedInCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skills",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                if (isOwnProfile) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add skill",
                            tint = TextSecondary
                        )
                    }
                }
            }
            
            // Skills Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS),
                contentPadding = PaddingValues(horizontal = LinkedInDesignSystem.SpaceXS)
            ) {
                items(skills) { skill ->
                    LinkedInSkillChip(skill = skill)
                }
            }
        }
    }
}

@Composable
private fun LinkedInExperienceItem(
    experience: LinkedInExperience,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(LinkedInDesignSystem.SpaceS)
    ) {
        Text(
            text = experience.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        
        Text(
            text = experience.company,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        
        val dateRange = if (experience.endDate != null) {
            "${experience.startDate} - ${experience.endDate}"
        } else {
            "${experience.startDate} - Present"
        }
        
        Text(
            text = dateRange,
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary
        )
        
        experience.location?.let { location ->
            Text(
                text = location,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
        
        experience.description?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun LinkedInSkillChip(
    skill: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(LinkedInDesignSystem.RadiusXL),
        color = VisionPrimary.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = VisionPrimary.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = skill,
            style = MaterialTheme.typography.bodySmall,
            color = VisionPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(
                horizontal = LinkedInDesignSystem.SpaceM,
                vertical = LinkedInDesignSystem.SpaceS
            )
        )
    }
} 