package com.example.data.db

import com.example.data.model.WordObject
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val wordDao: WordDao,
    private val searchHistoryDao: SearchHistoryDao
) {
    val allFavorites: Flow<List<WordObject>> = wordDao.getAllFavorites()
    val allCachedWords: Flow<List<WordObject>> = wordDao.getAllCachedWords()
    val recentHistory: Flow<List<SearchHistoryEntity>> = searchHistoryDao.getRecentHistory()

    suspend fun getWord(word: String): WordObject? = wordDao.getWord(word)
    suspend fun insertWord(word: WordObject) = wordDao.insertWord(word)
    suspend fun insertWords(words: List<WordObject>) {
        words.forEach { word ->
            val existing = wordDao.getWord(word.word)
            if (existing != null) {
                wordDao.insertWord(
                    word.copy(
                        isFavorite = existing.isFavorite,
                        isWordOfTheDay = existing.isWordOfTheDay
                    )
                )
            } else {
                wordDao.insertWord(word)
            }
        }
    }
    suspend fun clearCachedNonFavorites() = wordDao.clearCachedNonFavorites()

    suspend fun insertHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        searchHistoryDao.insertHistory(SearchHistoryEntity(trimmed, System.currentTimeMillis()))
        
        // Prune search history to enforce a hard maximum size limit of 20 elements
        val fullHistory = searchHistoryDao.getAllHistoryDirect()
        if (fullHistory.size > 20) {
            for (i in 20 until fullHistory.size) {
                searchHistoryDao.deleteHistoryEntry(fullHistory[i].query)
            }
        }
    }

    suspend fun clearHistory() = searchHistoryDao.clearHistory()

    suspend fun deleteHistoryEntry(query: String) = searchHistoryDao.deleteHistoryEntry(query)
}
