package com.example.data.state

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.AppRepository
import com.example.data.model.WordObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class Screen {
    WELCOME,
    HOME,
    BOOKMARKS,
    HISTORY,
    DETAIL,
    SETTINGS
}

data class AppUiState(
    val currentScreen: Screen = Screen.WELCOME,
    val screenHistory: List<Screen> = listOf(Screen.WELCOME),
    val themeMode: String = "light", // "light", "dark", "system"
    val apiKey: String = "",
    val baseUrl: String = "https://api.openai.com/v1",
    val modelName: String = "gpt-4o-mini",
    val selectedWord: WordObject? = null,
    val searchHistory: List<String> = listOf("Euphoria", "Liminal", "Pragmatic", "Ephemeral"),
    val wordOfTheDay: WordObject = WordObject(
        word = "Serendipity",
        pronunciation = "/ˌserənˈdipədē/",
        type = "noun",
        meaning = "The occurrence of events by chance in a happy or beneficial way.",
        exampleSentence = "We found the charming little restaurant by pure serendipity.",
        genZVersion = "The universe cooked up something magical for you when you weren't even looking. Pure main character energy.",
        synonymsListString = "Chance, Happystance, Coincidence, Fluke, Luck"
    ),
    val favorites: List<WordObject> = emptyList(),
    val isTestingConnection: Boolean = false,
    val connectionTestResult: Pair<Boolean, String>? = null // (success, message)
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("neuralex_prefs", Context.MODE_PRIVATE)
    private val database = AppDatabase.getDatabase(application)
    private val repository = AppRepository(database.wordDao(), database.searchHistoryDao())

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    // Preloaded robust static dictionary cache
    private val offlineCache = mapOf(
        "resilient" to WordObject(
            word = "Resilient",
            pronunciation = "/rɪˈzɪliənt/",
            type = "adjective",
            meaning = "Able to withstand or recover quickly from difficult conditions.",
            exampleSentence = "Despite facing many challenges, she remained resilient and never gave up.",
            genZVersion = "She's a total comeback queen — no matter what happens, she bounces back. 😎",
            synonymsListString = "Strong, Tough, Hardy, Tenacious, Enduring, Unbreakable, Robust",
            isFavorite = false
        ),
        "serendipity" to WordObject(
            word = "Serendipity",
            pronunciation = "/ˌserənˈdipədē/",
            type = "noun",
            meaning = "The occurrence of events by chance in a happy or beneficial way.",
            exampleSentence = "We found the charming little restaurant by pure serendipity.",
            genZVersion = "The universe cooked up something magical for you when you weren't even looking. Pure main character energy.",
            synonymsListString = "Chance, Happystance, Coincidence, Fluke, Luck"
        ),
        "euphoria" to WordObject(
            word = "Euphoria",
            pronunciation = "/juːˈfɔːriə/",
            type = "noun",
            meaning = "A feeling or state of intense excitement and happiness.",
            exampleSentence = "A sudden sense of euphoria engulfed her upon receiving the victory.",
            genZVersion = "That feeling of listening to your favorite song at midnight, on absolute cloud nine. Pure vibes! ✨",
            synonymsListString = "Ecstasy, Elation, Rapture, Bliss, Joy"
        ),
        "liminal" to WordObject(
            word = "Liminal",
            pronunciation = "/ˈlɪmɪnl/",
            type = "adjective",
            meaning = "Relating to a transitional or initial stage of a process; occupying a position at, or on both sides of, a boundary.",
            exampleSentence = "The empty airport terminal felt like a liminal space suspended in time.",
            genZVersion = "That eerie, nostalgic, in-between space where you're not where you were, but not where you're going. It's kinda sus. 😶‍🌫️",
            synonymsListString = "Transitional, Intermediate, Threshold, Borderline"
        ),
        "pragmatic" to WordObject(
            word = "Pragmatic",
            pronunciation = "/præɡˈmætɪk/",
            type = "adjective",
            meaning = "Dealing with things sensibly and realistically in a way that is based on practical rather than theoretical considerations.",
            exampleSentence = "We must take a pragmatic approach to solve this infrastructure problem.",
            genZVersion = "Being a realist who actually gets things done instead of overthinking theoretical scenarios. Big brain behavior. 🧠",
            synonymsListString = "Practical, Realistic, Sensible, Down-to-earth"
        ),
        "ephemeral" to WordObject(
            word = "Ephemeral",
            pronunciation = "/ɪˈfemərəl/",
            type = "adjective",
            meaning = "Lasting for a very short time.",
            exampleSentence = "The beautiful cherry blossom blooms are famous for their ephemeral nature.",
            genZVersion = "Blink and you miss it. Just like a Snapchat streak or a viral TikTok trend — gone in 24 hours. ⏱️",
            synonymsListString = "Brief, Fleeting, Short-lived, Transient, Passing"
        )
    )

    init {
        val savedTheme = sharedPrefs.getString("theme_mode", "light") ?: "light"
        val savedBaseUrl = sharedPrefs.getString("base_url", "https://api.openai.com/v1") ?: "https://api.openai.com/v1"
        val savedApiKey = sharedPrefs.getString("api_key", "") ?: ""
        val savedModelName = sharedPrefs.getString("model_name", "gpt-4o-mini") ?: "gpt-4o-mini"

        _uiState.update { state ->
            state.copy(
                themeMode = savedTheme,
                baseUrl = savedBaseUrl,
                apiKey = savedApiKey,
                modelName = savedModelName
            )
        }

        viewModelScope.launch {
            repository.allFavorites.collect { favs ->
                _uiState.update { it.copy(favorites = favs) }
            }
        }
        
        viewModelScope.launch {
            repository.recentHistory.collect { history ->
                _uiState.update { it.copy(searchHistory = history.map { entity -> entity.query }) }
            }
        }
        
        // Populate offline cache into database
        viewModelScope.launch {
            repository.insertWords(offlineCache.values.toList())
        }
    }

    // Legacy JSON methods for favorites are no longer needed
    // but we leave skeleton functions since they were used locally.

    fun navigateTo(screen: Screen) {
        _uiState.update { state ->
            val newHistory = state.screenHistory + screen
            state.copy(currentScreen = screen, screenHistory = newHistory)
        }
    }

    fun navigateBack(): Boolean {
        var handled = false
        _uiState.update { state ->
            if (state.screenHistory.size > 1) {
                val newHistory = state.screenHistory.dropLast(1)
                val prevScreen = newHistory.last()
                handled = true
                state.copy(currentScreen = prevScreen, screenHistory = newHistory)
            } else {
                state
            }
        }
        return handled
    }

    fun setTheme(theme: String) {
        sharedPrefs.edit().putString("theme_mode", theme).apply()
        _uiState.update { it.copy(themeMode = theme) }
    }

    fun updateAiConfig(baseUrl: String, key: String, model: String) {
        sharedPrefs.edit()
            .putString("base_url", baseUrl)
            .putString("api_key", key)
            .putString("model_name", model)
            .apply()
        _uiState.update { it.copy(baseUrl = baseUrl, apiKey = key, modelName = model) }
    }

    fun testConnection() {
        val currentBaseUrl = _uiState.value.baseUrl
        val currentApiKey = _uiState.value.apiKey
        val currentModel = _uiState.value.modelName

        viewModelScope.launch {
            _uiState.update { it.copy(isTestingConnection = true, connectionTestResult = null) }

            if (currentBaseUrl.isBlank()) {
                _uiState.update { it.copy(isTestingConnection = false, connectionTestResult = Pair(false, "Connection failed: Base URL is required")) }
                return@launch
            }
            if (currentApiKey.isBlank()) {
                _uiState.update { it.copy(isTestingConnection = false, connectionTestResult = Pair(false, "Connection failed: API Key is required")) }
                return@launch
            }
            if (currentModel.isBlank()) {
                _uiState.update { it.copy(isTestingConnection = false, connectionTestResult = Pair(false, "Connection failed: Model Name is required")) }
                return@launch
            }

            try {
                val url = if (currentBaseUrl.endsWith("/")) {
                    currentBaseUrl + "chat/completions"
                } else {
                    currentBaseUrl + "/chat/completions"
                }
                
                val request = com.example.data.api.ChatRequest(
                    model = currentModel,
                    messages = listOf(
                        com.example.data.api.ChatMessage(role = "user", content = "Hi, reply 'yes'")
                    )
                )
                
                com.example.data.api.RetrofitClient.api.generateCompletion(
                    url = url,
                    authHeader = "Bearer $currentApiKey",
                    request = request
                )
                
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        connectionTestResult = Pair(true, "Successfully connected to $currentModel at $currentBaseUrl!")
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        connectionTestResult = Pair(false, "Connection failed: ${e.message}")
                    )
                }
            }
        }
    }

    fun clearConnectionTestResult() {
        _uiState.update { it.copy(connectionTestResult = null) }
    }

    fun searchWord(query: String): Boolean {
        viewModelScope.launch {
            repository.insertHistory(query)
            
            val dbWord = repository.getWord(query.trim().lowercase().replaceFirstChar { it.uppercase() })
                         ?: offlineCache[query.trim().lowercase()]

            if (dbWord != null) {
                _uiState.update { state ->
                    val isFav = state.favorites.any { it.word.equals(dbWord.word, ignoreCase = true) }
                    state.copy(selectedWord = dbWord.copy(isFavorite = isFav))
                }
                navigateTo(Screen.DETAIL)
            } else {
                val capitalizedQuery = query.trim().replaceFirstChar { it.uppercase() }
                val loadingWord = WordObject(
                    word = capitalizedQuery,
                    pronunciation = "/.../",
                    type = "...",
                    meaning = "Fetching definition from AI...",
                    exampleSentence = "Please wait...",
                    genZVersion = "Hold up, let AI cook... \uD83D\uDC68\u200D\uD83C\uDF73",
                    synonymsListString = "..."
                )
                
                _uiState.update { state -> state.copy(selectedWord = loadingWord) }
                navigateTo(Screen.DETAIL)

                try {
                    val prompt = """
                        Provide the definition of the word "$capitalizedQuery".
                        Respond ONLY with a JSON object exactly matching this structure (no markdown tags, no extra text):
                        {
                          "word": "$capitalizedQuery",
                          "pronunciation": "(the pronunciation)",
                          "type": "(part of speech, e.g. noun/verb)",
                          "meaning": "(clear and comprehensive meaning)",
                          "exampleSentence": "(a good example sentence)",
                          "genZVersion": "(explain it using Gen Z slang/vibes)",
                          "synonymsListString": "(comma separated list of synonyms)"
                        }
                    """.trimIndent()
                    
                    val url = if (_uiState.value.baseUrl.endsWith("/")) {
                        _uiState.value.baseUrl + "chat/completions"
                    } else {
                        _uiState.value.baseUrl + "/chat/completions"
                    }
                    
                    val request = com.example.data.api.ChatRequest(
                        model = _uiState.value.modelName,
                        messages = listOf(
                            com.example.data.api.ChatMessage(role = "user", content = prompt)
                        )
                    )
                    
                    val response = com.example.data.api.RetrofitClient.api.generateCompletion(
                        url = url,
                        authHeader = "Bearer ${_uiState.value.apiKey}",
                        request = request
                    )
                    
                    val responseText = response.choices?.firstOrNull()?.message?.content
                    if (responseText != null) {
                        val moshi = com.squareup.moshi.Moshi.Builder()
                            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                            .build()
                        val adapter = moshi.adapter(com.example.data.api.WordDefinition::class.java)
                        
                        val cleanText = responseText.replace("```json", "").replace("```", "").trim()
                        val def = adapter.fromJson(cleanText)
                        
                        if (def != null) {
                            val newWord = WordObject(
                                word = def.word ?: capitalizedQuery,
                                pronunciation = def.pronunciation ?: "",
                                type = def.type ?: "",
                                meaning = def.meaning ?: "",
                                exampleSentence = def.exampleSentence ?: "",
                                genZVersion = def.genZVersion ?: "",
                                synonymsListString = def.synonymsListString ?: ""
                            )
                            repository.insertWord(newWord)
                            _uiState.update { state -> state.copy(selectedWord = newWord) }
                        } else {
                             _uiState.update { state -> 
                                 state.copy(selectedWord = loadingWord.copy(meaning = "Failed to parse AI response."))
                             }
                        }
                    } else {
                         _uiState.update { state -> 
                             state.copy(selectedWord = loadingWord.copy(meaning = "AI returned an empty response."))
                         }
                    }
                } catch (e: Exception) {
                    _uiState.update { state -> 
                        state.copy(selectedWord = loadingWord.copy(meaning = "Error: ${e.message}"))
                    }
                }
            }
        }
        return true
    }

    fun toggleFavorite(word: WordObject) {
        viewModelScope.launch {
            val isAlreadyFav = _uiState.value.favorites.any { it.word.equals(word.word, ignoreCase = true) }
            val newWord = word.copy(isFavorite = !isAlreadyFav)
            repository.insertWord(newWord)
            
            _uiState.update { state ->
                val currentWordSelected = state.selectedWord
                val updatedWordSelected = if (currentWordSelected != null && currentWordSelected.word.equals(word.word, ignoreCase = true)) {
                    newWord
                } else {
                    currentWordSelected
                }
                
                state.copy(selectedWord = updatedWordSelected)
            }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun importFavoritesFromJson(json: String): Pair<Boolean, String> {
        val trimmedJson = json.trim()
        if (trimmedJson.isEmpty()) {
            return Pair(false, "Import failed: Backup data is blank or empty.")
        }
        return try {
            val jsonArray = if (trimmedJson.startsWith("{")) {
                val jsonObj = JSONObject(trimmedJson)
                if (jsonObj.has("favorites")) {
                    jsonObj.getJSONArray("favorites")
                } else if (jsonObj.has("words")) {
                    jsonObj.getJSONArray("words")
                } else {
                    return Pair(false, "Import failed: JSON object root must contain a 'favorites' or 'words' array.")
                }
            } else {
                JSONArray(trimmedJson)
            }

            if (jsonArray.length() == 0) {
                return Pair(false, "Import failed: Backup JSON array is empty.")
            }

            val list = mutableListOf<WordObject>()
            var skippedCount = 0
            for (i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.optJSONObject(i)
                if (jsonObj == null) {
                    skippedCount++
                    continue
                }
                val wordVal = jsonObj.optString("word", "").trim()
                if (wordVal.isEmpty()) {
                    skippedCount++
                    continue
                }

                list.add(
                    WordObject(
                        word = wordVal,
                        pronunciation = jsonObj.optString("pronunciation", "").trim(),
                        type = jsonObj.optString("type", "noun").trim(),
                        meaning = jsonObj.optString("meaning", "").trim(),
                        exampleSentence = jsonObj.optString("exampleSentence", "").trim(),
                        genZVersion = jsonObj.optString("genZVersion", "").trim(),
                        synonymsListString = jsonObj.optString("synonymsListString", "").trim(),
                        isFavorite = true, // Ensure favorite status is persistent when imported
                        isWordOfTheDay = jsonObj.optBoolean("isWordOfTheDay", false),
                        timestampAdded = jsonObj.optLong("timestampAdded", System.currentTimeMillis())
                    )
                )
            }

            if (list.isEmpty()) {
                return Pair(false, "Import failed: No valid favorite words found in the backup file structure.")
            }

            viewModelScope.launch {
                repository.insertWords(list)
            }

            val msg = if (skippedCount > 0) {
                "${list.size} favorite words imported successfully ($skippedCount invalid items skipped)."
            } else {
                "All ${list.size} favorite words imported successfully."
            }
            Pair(true, msg)
        } catch (e: Exception) {
            Pair(false, "Import failed: ${e.localizedMessage ?: "Invalid JSON syntax context"}")
        }
    }

    fun exportFavoritesToJson(): String {
        val favs = _uiState.value.favorites
        return try {
            val jsonArray = JSONArray()
            favs.forEach { word ->
                val jsonObj = JSONObject().apply {
                    put("word", word.word)
                    put("pronunciation", word.pronunciation)
                    put("type", word.type)
                    put("meaning", word.meaning)
                    put("exampleSentence", word.exampleSentence)
                    put("genZVersion", word.genZVersion)
                    put("synonymsListString", word.synonymsListString)
                    put("isFavorite", word.isFavorite)
                    put("isWordOfTheDay", word.isWordOfTheDay)
                    put("timestampAdded", word.timestampAdded)
                }
                jsonArray.put(jsonObj)
            }
            jsonArray.toString(2)
        } catch (e: Exception) {
            "[]"
        }
    }
}
