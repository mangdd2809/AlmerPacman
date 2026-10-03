package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.HighScore
import kotlinx.coroutines.flow.Flow

@Dao
interface HighScoreDao {
    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 50")
    fun getAllScores(): Flow<List<HighScore>>

    @Query("SELECT MAX(score) FROM high_scores")
    fun getHighestScore(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: HighScore): Long

    @Query("DELETE FROM high_scores")
    suspend fun clearAll()
}
