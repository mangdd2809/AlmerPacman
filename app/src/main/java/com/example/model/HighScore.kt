package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "high_scores")
data class HighScore(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playerName: String,
    val score: Int,
    val level: Int,
    val dotsEaten: Int,
    val ghostsEaten: Int,
    val difficulty: String,
    val timestamp: Long = System.currentTimeMillis()
)
