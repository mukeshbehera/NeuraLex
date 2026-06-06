package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordObject(
    @PrimaryKey val word: String,
    val pronunciation: String,
    val type: String,
    val meaning: String,
    val exampleSentence: String,
    val genZVersion: String,
    val synonymsListString: String, // Comma separated synonyms for easy storing/retrieval
    val isFavorite: Boolean = false,
    val isWordOfTheDay: Boolean = false,
    val timestampAdded: Long = System.currentTimeMillis()
) {
    val synonyms: List<String>
        get() = if (synonymsListString.isEmpty()) emptyList() else synonymsListString.split(",").map { it.trim() }
}
