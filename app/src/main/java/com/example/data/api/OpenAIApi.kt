package com.example.data.api

import androidx.annotation.Keep
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

@Keep
data class ChatMessage(
    val role: String,
    val content: String
)

@Keep
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val response_format: ResponseFormat? = null,
    val temperature: Double = 0.7
)

@Keep
data class ResponseFormat(
    val type: String = "json_object"
)

@Keep
data class ChatResponse(
    val choices: List<Choice>?
)

@Keep
data class Choice(
    val message: ChatMessage?
)

interface OpenAIApi {
    @POST
    suspend fun generateCompletion(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body request: ChatRequest
    ): ChatResponse
}
