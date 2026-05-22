package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigationRouter(viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigationRouter(viewModel: MainViewModel) {
    val activeScreen = viewModel.currentScreen.collectAsStateWithLifecycle().value

    // Smooth page transitions
    AnimatedContent(
        targetState = activeScreen,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "navigationTransition"
    ) { screen ->
        when (screen) {
            MainViewModel.Screen.Onboarding -> OnboardingScreen(viewModel)
            MainViewModel.Screen.Dashboard -> DashboardScreen(viewModel)
            MainViewModel.Screen.KeyboardSimulator -> KeyboardSimulatorScreen(viewModel)
            MainViewModel.Screen.ClipboardHistory -> ClipboardHistoryScreen(viewModel)
            MainViewModel.Screen.ShortcutsManager -> ShortcutsManagerScreen(viewModel)
            MainViewModel.Screen.TypingInsights -> TypingInsightsScreen(viewModel)
            MainViewModel.Screen.LanguageConfig -> LanguageConfigScreen(viewModel)
            MainViewModel.Screen.VoiceHelper -> VoiceHelperScreen(viewModel)
            MainViewModel.Screen.PremiumCenter -> PremiumCenterScreen(viewModel)
            MainViewModel.Screen.Settings -> SettingsScreen(viewModel)
        }
    }
}
