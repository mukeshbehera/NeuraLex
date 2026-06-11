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
    val connectionTestResult: Pair<Boolean, String>? = null, // (success, message)
    val showWordNotFoundDialog: Boolean = false,
    val lastInvalidWord: String = ""
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

    private var lastRandomWord: String? = null
    private val recentRandomWords = mutableListOf<String>()
    private var cachedWordsList: List<WordObject> = emptyList()
    private val validEnglishWords = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

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

        // Load local English words dataset once
        try {
            application.assets.open("dictionary/english_words.txt").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val word = line.trim().lowercase()
                    if (word.isNotEmpty()) {
                        validEnglishWords.add(word)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        viewModelScope.launch {
            repository.allFavorites.collect { favs ->
                _uiState.update { state ->
                    val currentWordSelected = state.selectedWord
                    val updatedWordSelected = if (currentWordSelected != null) {
                        currentWordSelected.copy(isFavorite = favs.any { it.word.equals(currentWordSelected.word, ignoreCase = true) })
                    } else {
                        null
                    }
                    state.copy(
                        favorites = favs,
                        selectedWord = updatedWordSelected,
                        wordOfTheDay = state.wordOfTheDay.copy(isFavorite = favs.any { it.word.equals(state.wordOfTheDay.word, ignoreCase = true) })
                    )
                }
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

        viewModelScope.launch {
            repository.allCachedWords.collect { words ->
                cachedWordsList = words
                checkAndUpdateWordOfTheDay()
            }
        }
    }

    // Legacy JSON methods for favorites are no longer needed
    // but we leave skeleton functions since they were used locally.

    fun checkAndUpdateWordOfTheDay() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val savedDate = sharedPrefs.getString("wotd_generated_date", null)
        
        if (savedDate == today) {
            val savedWord = sharedPrefs.getString("wotd_word", null)
            if (savedWord != null) {
                val wordObj = WordObject(
                    word = savedWord,
                    pronunciation = sharedPrefs.getString("wotd_pronunciation", "") ?: "",
                    type = sharedPrefs.getString("wotd_type", "") ?: "",
                    meaning = sharedPrefs.getString("wotd_meaning", "") ?: "",
                    exampleSentence = sharedPrefs.getString("wotd_example_sentence", "") ?: "",
                    genZVersion = sharedPrefs.getString("wotd_genz_version", "") ?: "",
                    synonymsListString = sharedPrefs.getString("wotd_synonyms", "") ?: "",
                    isFavorite = sharedPrefs.getBoolean("wotd_is_favorite", false),
                    isWordOfTheDay = true
                )
                _uiState.update { it.copy(wordOfTheDay = wordObj) }
                return
            }
        }
        
        // Generate a new word from list of all words
        val allWords = (cachedWordsList + offlineCache.values).distinctBy { it.word.lowercase() }
        if (allWords.isNotEmpty()) {
            val currentWotdWord = _uiState.value.wordOfTheDay.word
            val candidates = if (allWords.size > 1) {
                allWords.filter { !it.word.equals(currentWotdWord, ignoreCase = true) }
            } else {
                allWords
            }
            val selected = (if (candidates.isNotEmpty()) candidates else allWords).random()
            
            // Save to sharedPrefs
            sharedPrefs.edit()
                .putString("wotd_generated_date", today)
                .putString("wotd_word", selected.word)
                .putString("wotd_pronunciation", selected.pronunciation)
                .putString("wotd_type", selected.type)
                .putString("wotd_meaning", selected.meaning)
                .putString("wotd_example_sentence", selected.exampleSentence)
                .putString("wotd_genz_version", selected.genZVersion)
                .putString("wotd_synonyms", selected.synonymsListString)
                .apply()
            
            _uiState.update { it.copy(wordOfTheDay = selected.copy(isWordOfTheDay = true)) }
        }
    }

    fun getRandomWord(): String? {
        val allWords = (cachedWordsList + offlineCache.values).distinctBy { it.word.lowercase() }
        if (allWords.isEmpty()) {
            android.widget.Toast.makeText(getApplication(), "No words available.", android.widget.Toast.LENGTH_SHORT).show()
            return null
        }
        val candidates = if (allWords.size > 1) {
            val computedHistorySize = ((allWords.size - 1) / 2).coerceAtLeast(1).coerceAtMost(15)
            
            // Trim current history to maximum allowed size before filtering
            while (recentRandomWords.size > computedHistorySize) {
                recentRandomWords.removeAt(0)
            }
            
            val filtered = allWords.filter { wordObj -> 
                !recentRandomWords.any { it.equals(wordObj.word, ignoreCase = true) } 
            }
            if (filtered.isNotEmpty()) filtered else allWords
        } else {
            allWords
        }
        val selected = candidates.random()
        lastRandomWord = selected.word
        
        // Add selected word to history and maintain size
        recentRandomWords.add(selected.word)
        val computedHistorySizeOfNewList = ((allWords.size - 1) / 2).coerceAtLeast(1).coerceAtMost(15)
        while (recentRandomWords.size > computedHistorySizeOfNewList) {
            recentRandomWords.removeAt(0)
        }
        
        return selected.word
    }

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

    fun dismissWordNotFoundDialog() {
        _uiState.update { it.copy(showWordNotFoundDialog = false) }
    }

    fun isValidWord(word: String): Boolean {
        val normalized = word.trim().lowercase()
        if (normalized.isEmpty()) return false
        
        // 1. Direct match
        if (validEnglishWords.contains(normalized)) return true
        if (offlineCache.containsKey(normalized)) return true
        
        // 2. Check simple inflections
        
        // Plurals or 3rd person singular -s / -es
        if (normalized.endsWith("s")) {
            val root1 = normalized.dropLast(1)
            if (validEnglishWords.contains(root1) || offlineCache.containsKey(root1)) return true
            
            if (normalized.endsWith("es")) {
                val root2 = normalized.dropLast(2)
                if (validEnglishWords.contains(root2) || offlineCache.containsKey(root2)) return true
            }
        }
        
        // Past tense -ed
        if (normalized.endsWith("ed")) {
            val root1 = normalized.dropLast(2)
            if (validEnglishWords.contains(root1) || offlineCache.containsKey(root1)) return true
            
            val root2 = normalized.dropLast(1)
            if (validEnglishWords.contains(root2) || offlineCache.containsKey(root2)) return true
            
            if (root1.length > 1 && root1[root1.length - 1] == root1[root1.length - 2]) {
                val rootDouble = root1.dropLast(1)
                if (validEnglishWords.contains(rootDouble) || offlineCache.containsKey(rootDouble)) return true
            }
        }
        
        // Present participle -ing
        if (normalized.endsWith("ing")) {
            val root1 = normalized.dropLast(3)
            if (validEnglishWords.contains(root1) || offlineCache.containsKey(root1)) return true
            
            val root2 = root1 + "e"
            if (validEnglishWords.contains(root2) || offlineCache.containsKey(root2)) return true
            
            if (root1.length > 1 && root1[root1.length - 1] == root1[root1.length - 2]) {
                val rootDouble = root1.dropLast(1)
                if (validEnglishWords.contains(rootDouble) || offlineCache.containsKey(rootDouble)) return true
            }
        }
        
        return false
    }

    fun searchWord(query: String): Boolean {
        val trimmedWord = query.trim().lowercase()
        if (!isValidWord(trimmedWord)) {
            _uiState.update { state ->
                state.copy(
                    showWordNotFoundDialog = true,
                    lastInvalidWord = query
                )
            }
            return false
        }

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

    fun deleteSearchHistoryEntry(query: String) {
        viewModelScope.launch {
            repository.deleteHistoryEntry(query)
        }
    }

    fun deleteSearchHistoryEntries(queries: List<String>) {
        viewModelScope.launch {
            queries.forEach { query ->
                repository.deleteHistoryEntry(query)
            }
        }
    }

    fun importFavoritesFromJson(json: String): Pair<Boolean, String> {
        val trimmedJson = json.trim()
        if (trimmedJson.isEmpty()) {
            return Pair(false, "Import failed. Invalid file")
        }
        return try {
            val rootObj = try {
                JSONObject(trimmedJson)
            } catch (e: Exception) {
                return Pair(false, "Import failed. Invalid file")
            }

            // Expected schema validation
            if (!rootObj.has("appName") || rootObj.optString("appName") != "NeuraLex" || !rootObj.has("favorites")) {
                return Pair(false, "Import failed. Invalid file")
            }

            val jsonArray = rootObj.getJSONArray("favorites")
            val list = mutableListOf<WordObject>()

            for (i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.optJSONObject(i)
                if (jsonObj == null) {
                    return Pair(false, "Import failed. Invalid file") // corrupt record present
                }
                val wordVal = jsonObj.optString("word", "").trim()
                val meaningVal = jsonObj.optString("meaning", "").trim()
                if (wordVal.isEmpty() || meaningVal.isEmpty()) {
                    return Pair(false, "Import failed. Invalid file") // corrupt record present
                }

                // Skip duplicate favorites safely
                val exists = _uiState.value.favorites.any { it.word.equals(wordVal, ignoreCase = true) }
                if (exists) {
                    continue
                }

                val synonymsArray = jsonObj.optJSONArray("synonyms")
                val synonymsListStr = if (synonymsArray != null) {
                    val syns = mutableListOf<String>()
                    for (j in 0 until synonymsArray.length()) {
                        val s = synonymsArray.optString(j, "").trim()
                        if (s.isNotEmpty()) syns.add(s)
                    }
                    syns.joinToString(", ")
                } else {
                    jsonObj.optString("synonymsListString", "").trim()
                }

                list.add(
                    WordObject(
                        word = wordVal,
                        pronunciation = jsonObj.optString("pronunciation", "").trim(),
                        type = jsonObj.optString("type", "noun").trim(),
                        meaning = meaningVal,
                        exampleSentence = jsonObj.optString("exampleSentence", "").trim(),
                        genZVersion = jsonObj.optString("genZVersion", "").trim(),
                        synonymsListString = synonymsListStr,
                        isFavorite = true,
                        isWordOfTheDay = jsonObj.optBoolean("isWordOfTheDay", false),
                        timestampAdded = jsonObj.optLong("timestampAdded", System.currentTimeMillis())
                    )
                )
            }

            if (list.isNotEmpty()) {
                viewModelScope.launch {
                    repository.insertWords(list)
                }
            }

            Pair(true, "Successfully imported")
        } catch (e: Exception) {
            Pair(false, "Import failed. Invalid file")
        }
    }

    fun exportFavoritesToJson(): String {
        val favs = _uiState.value.favorites
        return try {
            val root = JSONObject()
            root.put("appName", "NeuraLex")
            root.put("version", 1)

            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            root.put("exportedAt", sdf.format(java.util.Date()))

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

                    val synsArray = JSONArray()
                    word.synonyms.forEach { synsArray.put(it) }
                    put("synonyms", synsArray)

                    put("isFavorite", word.isFavorite)
                    put("isWordOfTheDay", word.isWordOfTheDay)
                    put("timestampAdded", word.timestampAdded)
                }
                jsonArray.put(jsonObj)
            }
            root.put("favorites", jsonArray)
            root.toString(2)
        } catch (e: Exception) {
            "{}"
        }
    }
}
