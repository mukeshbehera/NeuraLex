package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.WordObject
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE isFavorite = 1 ORDER BY timestampAdded DESC")
    fun getAllFavorites(): Flow<List<WordObject>>

    @Query("SELECT * FROM words WHERE word = :word LIMIT 1")
    suspend fun getWord(word: String): WordObject?

    @Query("SELECT * FROM words ORDER BY timestampAdded DESC")
    fun getAllCachedWords(): Flow<List<WordObject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordObject)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordObject>)

    @Query("DELETE FROM words WHERE isFavorite = 0 AND isWordOfTheDay = 0")
    suspend fun clearCachedNonFavorites()
}
