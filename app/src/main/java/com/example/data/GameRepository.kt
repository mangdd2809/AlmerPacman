package com.example.data

import com.example.model.HighScore
import kotlinx.coroutines.flow.Flow

class GameRepository(private val highScoreDao: HighScoreDao) {

    val allScores: Flow<List<HighScore>> = highScoreDao.getAllScores()
    val highestScore: Flow<Int?> = highScoreDao.getHighestScore()

    suspend fun saveScore(
        playerName: String,
        score: Int,
        level: Int,
        dotsEaten: Int,
        ghostsEaten: Int,
        difficulty: String
    ): Long {
        val entry = HighScore(
            playerName = playerName.ifBlank { "Almer" },
            score = score,
            level = level,
            dotsEaten = dotsEaten,
            ghostsEaten = ghostsEaten,
            difficulty = difficulty
        )
        return highScoreDao.insertScore(entry)
    }

    suspend fun clearLeaderboard() {
        highScoreDao.clearAll()
    }
}
