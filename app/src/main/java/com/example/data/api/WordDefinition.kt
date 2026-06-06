package com.example.data.api

import androidx.annotation.Keep

@Keep
data class WordDefinition(
    val word: String? = null,
    val pronunciation: String? = null,
    val type: String? = null,
    val meaning: String? = null,
    val exampleSentence: String? = null,
    val genZVersion: String? = null,
    val synonymsListString: String? = null
)
