package com.tully.quickq.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.tully.quickq.presentation.navigation.QuickQNavigation
import com.tully.quickq.ui.theme.BackgroundStart
import com.tully.quickq.ui.theme.LinkedInScreenContainer
import com.tully.quickq.ui.theme.QuickQTheme
import com.tully.quickq.ui.theme.LocalVisionDesignSystem

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge for immersive VisionOS experience
        enableEdgeToEdge()
   
        setContent {
            QuickQTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Professional LinkedIn-style container for all screens
                    LinkedInScreenContainer {
                        QuickQNavigation()
                    }
                }
            }
        }
    }
}