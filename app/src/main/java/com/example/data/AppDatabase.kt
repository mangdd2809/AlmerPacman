package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.HighScore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [HighScore::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun highScoreDao(): HighScoreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "almer_pacman.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.highScoreDao()?.let { dao ->
                                    dao.insertScore(
                                        HighScore(
                                            playerName = "Almer (Juara)",
                                            score = 12500,
                                            level = 4,
                                            dotsEaten = 360,
                                            ghostsEaten = 14,
                                            difficulty = "Klasik"
                                        )
                                    )
                                    dao.insertScore(
                                        HighScore(
                                            playerName = "Papa Almer",
                                            score = 8400,
                                            level = 3,
                                            dotsEaten = 280,
                                            ghostsEaten = 8,
                                            difficulty = "Klasik"
                                        )
                                    )
                                    dao.insertScore(
                                        HighScore(
                                            playerName = "Mama Almer",
                                            score = 6200,
                                            level = 2,
                                            dotsEaten = 190,
                                            ghostsEaten = 5,
                                            difficulty = "Santai"
                                        )
                                    )
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
