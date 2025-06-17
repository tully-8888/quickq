You can download the latest release of the QuickQ app from [GitHub Releases](https://github.com/tully-8888/quickq/releases/tag/1.0.0).

# QuickQ: AI-Powered Interview Preparation Assistant

## Introduction

QuickQ is an Android application designed to revolutionize job interview preparation. Leveraging the power of Google's Gemini 2.0 Flash AI, it provides a dynamic platform for users to search for jobs, simulate mock interviews, and receive real-time, personalized feedback. The app aims to equip job seekers with the confidence and skills needed to excel in technical interviews.

## Features

*   **Job Search & Discovery:** Users can search for software development jobs across various technologies (iOS, Android, React, Python, Java, etc.), experience levels, and company sizes. The search includes advanced filtering options.
*   **Detailed Job Information:** View comprehensive details for each job posting, including description, requirements, responsibilities, salary range, company details, and an estimated interview difficulty.
*   **AI-Powered Interview Simulation:** Conduct mock interviews tailored to specific job roles and company profiles. The AI generates dynamic questions based on job requirements and perceived company difficulty.
*   **Personalized Feedback System:**
    *   **After Each Question Mode:** Receive immediate, question-specific feedback on your answers, including a score, strengths, areas for improvement, and actionable suggestions.
    *   **End-of-Interview Mode:** Get a comprehensive summary of your performance across the entire interview, with overall strengths, improvement areas, and general career advice.
*   **Progress Tracking:** (Implicit from `InterviewSummary` and `InterviewHistory`) The app can track interview performance over time.
*   **Dynamic Interview Configuration:** Interview difficulty and question count adapt based on factors like company reputation and the candidate's experience level, offering a realistic simulation.

## Technologies Used

*   **Platform:** Android (API 29+)
*   **Language:** Kotlin 2.1.0
*   **UI Toolkit:** Jetpack Compose (Modern, declarative UI framework)
*   **Architecture:** Clean Architecture with MVVM (Model-View-ViewModel) pattern
*   **Dependency Injection:** Koin
*   **Networking:** Retrofit, OkHttp
*   **AI Integration:** Google Gemini 2.0 Flash API
*   **Data Serialization:** Gson
*   **Asynchronous Programming:** Kotlin Coroutines (for efficient background operations and structured concurrency)
*   **State Management:** Jetpack Compose `StateFlow`, `MutableStateFlow`
*   **Local Storage:** Android SharedPreferences (for interview history)

## Architecture Overview

QuickQ adheres to a Clean Architecture approach, ensuring a clear separation of concerns, high testability, and maintainability.

```
+-------------------+      +-----------------+      +-------------------+
| Presentation Layer| ---->|  Domain Layer   |<-----|    Data Layer     |
|(UI, ViewModels)   |      |(Models, UseCases|      |(APIs, Repositories|
+-------------------+      | Repositories)   |      |DTOs, Persistence) |
         ^                 +-----------------+      +-------------------+
         |                        ^    ^
         |                        |    |
         +------------------------+    +------------------------+
                  Dependency Rule: Inner layers know nothing about outer layers.
```

### 1. Domain Layer (`/domain`)

The core of the application, containing business logic and entities. It is independent of any specific UI, database, or API.

*   **`model/`**: Defines the core business entities such as `Job`, `Interview`, `InterviewQuestion`, `InterviewFeedback`, and various enums (`ExperienceLevel`, `JobType`, `CompanySize`, `WorkEnvironment`, `InterviewDifficulty`, `HiringUrgency`, `FeedbackMode`). These are immutable data classes, optimized for Compose.
*   **`repository/`**: Contains interfaces that define contracts for data operations, such as `JobRepository` and `InterviewRepository`. These interfaces are implemented in the Data Layer, upholding the dependency rule.

### 2. Data Layer (`/data`)

Responsible for retrieving, storing, and managing data from various sources (API, local storage). It implements the repository interfaces defined in the Domain Layer.

*   **`api/`**: Contains `GeminiApiService` using Retrofit for communicating with the Google Gemini API. It includes a `generateContentWithRetry` extension function for robust API calls with exponential backoff.
*   **`dto/`**: Data Transfer Objects (`GeminiRequest`, `GeminiResponse`, `Content`, `Part`, `GenerationConfig`, `Candidate`, `UsageMetadata`) used for structuring API requests and responses.
*   **`constants/`**: Holds application-wide constants, including Gemini API key/URL, SharedPreferences keys, and dynamic interview configuration logic (e.g., `COMPANY_REPUTATION_MAP`, `getInterviewConfig`).
*   **`repository/`**: Provides concrete implementations of the repository interfaces:
    *   **`JobRepositoryImpl`**: Handles searching and fetching job data, interacting with the Gemini API to generate job postings based on search queries and filters. It includes an in-memory caching mechanism (`ConcurrentHashMap`) for performance and efficient JSON parsing.
    *   **`InterviewRepositoryImpl`**: Manages the interview lifecycle, including starting interviews, fetching questions, submitting answers, generating personalized and overall feedback using the Gemini API, and saving interview summaries to SharedPreferences.

### 3. Presentation Layer (`/presentation`)

Handles the UI and its state, interacting with the Domain Layer via ViewModels.

*   **`viewmodel/`**: Contains ViewModels (`JobSearchViewModel`, `JobDetailViewModel`, `InterviewViewModel`) that manage UI-specific state. They expose `StateFlow`s for UI observation and use `viewModelScope` for coroutine-based asynchronous operations. Advanced caching and filtering logic are implemented in `JobSearchViewModel`.
*   **`screen/`**: Composable functions representing different screens of the application:
    *   **`JobSearchScreen`**: Displays job search input, filters, and results. Features include search bar, filter button, loading/error/empty/welcome states, and optimized `LazyColumn` for job listings.
    *   **`JobDetailScreen`**: Shows detailed information for a selected job, including description, requirements, skills, and a button to start an interview.
    *   **`InterviewScreen`**: Manages the AI interview flow, including question display, user answer input, real-time feedback (if enabled), and an overall interview summary. It handles navigation based on interview progress.
*   **`navigation/`**: Sets up the Jetpack Compose Navigation graph (`QuickQNavigation.kt`), defining routes and managing screen transitions (Job Search -> Job Detail -> Interview).
*   **`MainActivity.kt`**: The single Activity entry point for the Jetpack Compose UI.

### 4. UI Layer (`/ui`)

Manages the application's visual design system.

*   **`theme/`**: Implements Material Design 3, defining color schemes (`Theme.kt`, `Color.kt`), typography (`Type.kt`), and custom components/modifiers (e.g., `VisionGlassCard`, `VisionButton`, `VisionChip`) that provide a unique visual style.

### 5. DI Layer (`/di`)

Configures Koin for dependency injection, centralizing the creation and provision of various components across the application.

*   **`AppModule.kt`**: Defines Koin modules for ViewModels, repositories, API services, and other dependencies, ensuring a testable and modular codebase.
