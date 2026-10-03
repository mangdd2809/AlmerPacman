package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AboutAlmerScreen
import com.example.ui.GameScreen
import com.example.ui.GameViewModel
import com.example.ui.HowToPlayScreen
import com.example.ui.LeaderboardScreen
import com.example.ui.MenuScreen
import com.example.ui.OpeningSplashScreen
import com.example.ui.theme.ArcadeDark
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    OPENING,
    MENU,
    GAME,
    LEADERBOARD,
    ABOUT_ALMER,
    HOW_TO_PLAY
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    color = ArcadeDark,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AlmerPacmanApp()
                }
            }
        }
    }
}

@Composable
fun AlmerPacmanApp(
    viewModel: GameViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(AppScreen.OPENING) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val highScores by viewModel.highScores.collectAsStateWithLifecycle()
    val topScore by viewModel.topScore.collectAsStateWithLifecycle()

    when (currentScreen) {
        AppScreen.OPENING -> {
            OpeningSplashScreen(
                highScore = topScore ?: 12500,
                soundEnabled = uiState.soundEnabled,
                onToggleSound = { viewModel.toggleSound() },
                onStartGame = {
                    viewModel.soundManager.playIntroJingle()
                    viewModel.initNewGame(uiState.difficulty)
                    currentScreen = AppScreen.GAME
                },
                onOpenMainMenu = {
                    currentScreen = AppScreen.MENU
                }
            )
        }
        AppScreen.MENU -> {
            MenuScreen(
                highScore = topScore ?: 12500,
                selectedDifficulty = uiState.difficulty,
                soundEnabled = uiState.soundEnabled,
                onSelectDifficulty = { diff -> viewModel.setDifficulty(diff) },
                onToggleSound = { viewModel.toggleSound() },
                onStartGame = {
                    viewModel.soundManager.playIntroJingle()
                    viewModel.initNewGame(uiState.difficulty)
                    currentScreen = AppScreen.GAME
                },
                onOpenLeaderboard = { currentScreen = AppScreen.LEADERBOARD },
                onOpenAboutAlmer = { currentScreen = AppScreen.ABOUT_ALMER },
                onOpenHowToPlay = { currentScreen = AppScreen.HOW_TO_PLAY },
                onBackToOpening = { currentScreen = AppScreen.OPENING }
            )
        }
        AppScreen.GAME -> {
            GameScreen(
                uiState = uiState,
                mazeGrid = viewModel.mazeGrid,
                viewModel = viewModel,
                onNavigateHome = {
                    viewModel.pauseGame()
                    currentScreen = AppScreen.OPENING
                }
            )
        }
        AppScreen.LEADERBOARD -> {
            LeaderboardScreen(
                scores = highScores,
                onBack = { currentScreen = AppScreen.MENU },
                onClearLeaderboard = { viewModel.clearLeaderboard() }
            )
        }
        AppScreen.ABOUT_ALMER -> {
            AboutAlmerScreen(
                onBack = { currentScreen = AppScreen.MENU }
            )
        }
        AppScreen.HOW_TO_PLAY -> {
            HowToPlayScreen(
                onBack = { currentScreen = AppScreen.MENU }
            )
        }
    }
}
