package com.example.game

enum class GameDifficulty(
    val title: String,
    val description: String,
    val initialLives: Int,
    val pacmanSpeed: Float,
    val ghostSpeed: Float,
    val frightenedDurationMs: Long,
    val scoreMultiplier: Float
) {
    SANTAI(
        title = "Santai",
        description = "Cocok untuk santai & pemula. Hantu lebih lambat, 4 nyawa.",
        initialLives = 4,
        pacmanSpeed = 0.09f,
        ghostSpeed = 0.07f,
        frightenedDurationMs = 9000L,
        scoreMultiplier = 1.0f
    ),
    KLASIK(
        title = "Klasik",
        description = "Sensasi arcade orisinal. Kecepatan seimbang, 3 nyawa.",
        initialLives = 3,
        pacmanSpeed = 0.105f,
        ghostSpeed = 0.095f,
        frightenedDurationMs = 7000L,
        scoreMultiplier = 1.2f
    ),
    TURBO(
        title = "Tantangan Turbo",
        description = "Kecepatan tinggi untuk pemain pro! Bonus skor 1.5x!",
        initialLives = 3,
        pacmanSpeed = 0.13f,
        ghostSpeed = 0.12f,
        frightenedDurationMs = 4500L,
        scoreMultiplier = 1.5f
    )
}
