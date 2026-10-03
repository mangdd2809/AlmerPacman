package com.example.game

enum class GamePhase {
    READY,
    PLAYING,
    DYING,
    LEVEL_CLEAR,
    PAUSED,
    GAME_OVER
}

enum class ControlMode(val title: String) {
    BOTH("D-Pad & Geser (Rekomendasi)"),
    DPAD("Hanya Virtual D-Pad"),
    SWIPE("Hanya Usap Layar (Swipe)")
}

data class GameUiState(
    val phase: GamePhase = GamePhase.READY,
    val score: Int = 0,
    val highScore: Int = 12500,
    val level: Int = 1,
    val lives: Int = 3,
    val dotsRemaining: Int = 0,
    val totalDotsInLevel: Int = 0,
    val pacmanX: Float = 9f,
    val pacmanY: Float = 15f,
    val pacmanDir: Direction = Direction.LEFT,
    val pacmanNextDir: Direction = Direction.LEFT,
    val mouthAngle: Float = 35f,
    val isPowerActive: Boolean = false,
    val powerRemainingMs: Long = 0L,
    val ghosts: List<Ghost> = emptyList(),
    val activeFruit: Fruit? = null,
    val floatingScores: List<FloatingScore> = emptyList(),
    val readyCountdown: Int = 3,
    val difficulty: GameDifficulty = GameDifficulty.KLASIK,
    val soundEnabled: Boolean = true,
    val controlMode: ControlMode = ControlMode.BOTH,
    val ghostsEatenCount: Int = 0,
    val dotsEatenCount: Int = 0
)
