package com.tully.quickq.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember

// VisionOS-Inspired Dark Color Scheme
private val VisionDarkColorScheme = darkColorScheme(
    primary = VisionPrimary,
    onPrimary = Color.White,
    primaryContainer = GlassPrimary,
    onPrimaryContainer = TextPrimary,
    
    secondary = VisionSecondary,
    onSecondary = Color.White,
    secondaryContainer = GlassSecondary,
    onSecondaryContainer = TextPrimary,
    
    tertiary = VisionTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0x80AF52DE),
    onTertiaryContainer = TextPrimary,
    
    background = BackgroundStart,
    onBackground = TextPrimary,
    surface = Color(0x1A1A1A1A),
    onSurface = TextPrimary,
    surfaceVariant = Color(0x2A2A2A2A),
    onSurfaceVariant = TextSecondary,
    
    error = ErrorGlass,
    onError = Color.White,
    errorContainer = Color(0x40FF3B30),
    onErrorContainer = TextPrimary,
    
    outline = Color.White.copy(alpha = 0.3f),
    outlineVariant = Color(0x10FFFFFF),
    scrim = Color(0x80000000),
    inverseSurface = Color(0xFFF5F5F5),
    inverseOnSurface = Color(0xFF1A1A1A),
    inversePrimary = VisionPrimaryVariant,
    surfaceTint = VisionPrimary
)

// Enhanced Light Color Scheme for VisionOS
private val VisionLightColorScheme = lightColorScheme(
    primary = VisionPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0x20007AFF),
    onPrimaryContainer = Color(0xFF003D7A),
    
    secondary = VisionSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0x205856D6),
    onSecondaryContainer = Color(0xFF2C2B6B),
    
    tertiary = VisionTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0x20AF52DE),
    onTertiaryContainer = Color(0xFF57296F),
    
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFAFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF666666),
    
    error = Color(0xFFFF3B30),
    onError = Color.White,
    errorContainer = Color(0x20FF3B30),
    onErrorContainer = Color(0xFF8B0000),
    
    outline = Color.Black.copy(alpha = 0.3f),
    outlineVariant = Color(0x20000000),
    scrim = Color(0x80000000),
    inverseSurface = Color(0xFF1A1A1A),
    inverseOnSurface = Color(0xFFF5F5F5),
    inversePrimary = Color(0xFF5AC8FA),
    surfaceTint = VisionPrimary
)

// Glassmorphism and Spatial Design System
data class VisionDesignSystem(
    val cornerRadius: Float = 24f,
    val elevationShadow: Float = 8f
)

val LocalVisionDesignSystem = staticCompositionLocalOf { VisionDesignSystem() }

@Composable
fun QuickQTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled for consistent VisionOS experience
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> VisionDarkColorScheme
        else -> VisionLightColorScheme
    }

    val visionDesignSystem = VisionDesignSystem()

    CompositionLocalProvider(
        LocalVisionDesignSystem provides visionDesignSystem
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = VisionTypography,
            content = content
        )
    }
}

/**
 * Professional LinkedIn-style screen container for consistent app experience
 * Provides proper system bar handling and professional background styling
 */
@Composable
fun LinkedInScreenContainer(
    modifier: Modifier = Modifier,
    includeStatusBarPadding: Boolean = true,
    includeNavigationBarPadding: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .then(
                if (includeStatusBarPadding && includeNavigationBarPadding) {
                    Modifier.windowInsetsPadding(WindowInsets.statusBars)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                } else if (includeStatusBarPadding) {
                    Modifier.windowInsetsPadding(WindowInsets.statusBars)
                } else if (includeNavigationBarPadding) {
                    Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}