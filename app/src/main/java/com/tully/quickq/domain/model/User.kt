package com.tully.quickq.domain.model

import androidx.compose.runtime.Stable

@Stable
data class User(
    val id: String,
    val isFirstTime: Boolean = true,
    val profile: UserProfile? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Stable
data class UserProfile(
    val role: String, // required - e.g., "iOS Developer", "Android Developer", "Full Stack Developer"
    val seniority: ExperienceLevel? = null, // optional
    val skills: List<String> = emptyList(), // optional - e.g., ["Swift", "Kotlin", "React"]
    val bio: String = "",
    val location: String = "",
    val preferredJobTypes: Set<JobType> = emptySet(),
    val preferredWorkEnvironments: Set<WorkEnvironment> = emptySet()
)

// Popular developer role suggestions
object RoleSuggestions {
    val popularDeveloperRoles = listOf(
        "iOS Developer",
        "Android Developer", 
        "Full Stack Developer",
        "Frontend Developer",
        "Backend Developer",
        "Mobile Developer",
        "React Developer",
        "React Native Developer",
        "Flutter Developer",
        "Vue.js Developer",
        "Angular Developer",
        "Node.js Developer",
        "Python Developer",
        "Java Developer",
        "JavaScript Developer",
        "TypeScript Developer",
        "Kotlin Developer",
        "Swift Developer",
        "C# Developer",
        "Go Developer",
        "Rust Developer",
        "PHP Developer",
        "Ruby Developer",
        "DevOps Engineer",
        "Cloud Engineer",
        "Site Reliability Engineer",
        "Data Engineer",
        "Machine Learning Engineer",
        "AI Engineer",
        "Software Engineer",
        "Senior Software Engineer",
        "Lead Developer",
        "Technical Lead",
        "Engineering Manager",
        "Solutions Architect",
        "System Architect",
        "Database Developer",
        "Game Developer",
        "Blockchain Developer",
        "Security Engineer",
        "QA Engineer",
        "Test Automation Engineer"
    ).sorted()
    
    val popularSkills = listOf(
        // Mobile
        "Swift", "Kotlin", "React Native", "Flutter", "iOS", "Android",
        // Frontend
        "React", "Vue.js", "Angular", "JavaScript", "TypeScript", "HTML", "CSS",
        // Backend
        "Node.js", "Python", "Java", "C#", "Go", "Ruby", "PHP",
        // Databases
        "PostgreSQL", "MongoDB", "MySQL", "Redis", "SQLite",
        // Cloud & DevOps
        "AWS", "Google Cloud", "Azure", "Docker", "Kubernetes", "CI/CD",
        // Other
        "Git", "REST APIs", "GraphQL", "Microservices", "Agile", "Testing"
    )
} 