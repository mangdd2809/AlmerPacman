package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.game.GamePhase
import com.example.game.GameUiState
import com.example.game.TileType
import com.example.ui.theme.ArcadeDark

@Composable
fun GameScreen(
    uiState: GameUiState,
    mazeGrid: Array<Array<TileType>>,
    viewModel: GameViewModel,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        if (uiState.phase == GamePhase.PLAYING) {
            viewModel.pauseGame()
        } else {
            onNavigateHome()
        }
    }

    Surface(
        color = ArcadeDark,
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Score & Controls HUD
                TopGameHeader(
                    uiState = uiState,
                    onPauseToggle = {
                        if (uiState.phase == GamePhase.PLAYING) viewModel.pauseGame()
                        else if (uiState.phase == GamePhase.PAUSED) viewModel.resumeGame()
                    },
                    onSoundToggle = { viewModel.toggleSound() }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // The Arcade Maze Canvas (Centerpiece)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AlmerPacmanCanvas(
                        uiState = uiState,
                        mazeGrid = mazeGrid,
                        onSwipeDirection = { dir -> viewModel.setNextDirection(dir) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Bottom HUD (Lives & Stats)
                BottomHud(uiState = uiState)

                // Virtual Arcade D-Pad Controls
                VirtualArcadeDpad(
                    onDirection = { dir -> viewModel.setNextDirection(dir) },
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Overlays & Dialogs based on phase
            when (uiState.phase) {
                GamePhase.READY -> {
                    ReadyCountdownOverlay(count = uiState.readyCountdown)
                }
                GamePhase.LEVEL_CLEAR -> {
                    LevelClearOverlay(level = uiState.level, bonusScore = 500 * uiState.level)
                }
                GamePhase.PAUSED -> {
                    PauseDialog(
                        onResume = { viewModel.resumeGame() },
                        onRestart = { viewModel.restartGame() },
                        onHome = onNavigateHome
                    )
                }
                GamePhase.GAME_OVER -> {
                    GameOverDialog(
                        uiState = uiState,
                        onSaveScore = { name -> viewModel.saveHighScore(name) },
                        onRestart = { viewModel.restartGame() },
                        onHome = onNavigateHome
                    )
                }
                else -> Unit
            }
        }
    }
}
