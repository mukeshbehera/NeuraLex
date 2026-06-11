package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.WordObject
import com.example.data.state.AppViewModel
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NeuraLex", appName)
  }

  @Test
  fun `launch main activity`() {
    ActivityScenario.launch(MainActivity::class.java).use { scenario ->
      assertNotNull(scenario)
    }
  }

  @Test
  fun `export complies with requested schema`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = AppViewModel(app)

    val exported = viewModel.exportFavoritesToJson()
    val jsonObj = JSONObject(exported)
    assertEquals("NeuraLex", jsonObj.getString("appName"))
    assertEquals(1, jsonObj.getInt("version"))
    assertTrue(jsonObj.has("exportedAt"))
    assertTrue(jsonObj.has("favorites"))
    assertEquals(0, jsonObj.getJSONArray("favorites").length())
  }

  @Test
  fun `import validates invalid json files`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = AppViewModel(app)

    // Case 1: Empty JSON
    val resultEmpty = viewModel.importFavoritesFromJson("")
    assertFalse(resultEmpty.first)
    assertEquals("Import failed. Invalid file", resultEmpty.second)

    // Case 2: Invalid JSON formatting
    val resultInvalidFormat = viewModel.importFavoritesFromJson("{invalid}")
    assertFalse(resultInvalidFormat.first)
    assertEquals("Import failed. Invalid file", resultInvalidFormat.second)

    // Case 3: Missing appName or favorites array
    val resultMissingFields = viewModel.importFavoritesFromJson("""
        {
          "version": 1
        }
    """.trimIndent())
    assertFalse(resultMissingFields.first)
    assertEquals("Import failed. Invalid file", resultMissingFields.second)

    // Case 4: Wrong appName
    val resultWrongApp = viewModel.importFavoritesFromJson("""
        {
          "appName": "WrongApp",
          "version": 1,
          "favorites": []
        }
    """.trimIndent())
    assertFalse(resultWrongApp.first)
    assertEquals("Import failed. Invalid file", resultWrongApp.second)
  }

  @Test
  fun `import validates and accepts correct json files`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = AppViewModel(app)

    val validJson = """
        {
          "appName": "NeuraLex",
          "version": 1,
          "exportedAt": "2026-06-07T18:30:22Z",
          "favorites": [
            {
              "word": "Resilient",
              "meaning": "Able to withstand or recover quickly from difficult conditions.",
              "pronunciation": "/rɪˈzɪliənt/",
              "type": "adjective",
              "exampleSentence": "Test example sentence",
              "genZVersion": "Test gen z style",
              "synonyms": ["Strong", "Tough"]
            }
          ]
        }
    """.trimIndent()

    val result = viewModel.importFavoritesFromJson(validJson)
    assertTrue(result.first)
    assertEquals("Successfully imported", result.second)
  }

  @Test
  fun `random word selector returns non-consecutive results`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = AppViewModel(app)

    val word1 = viewModel.getRandomWord()
    assertNotNull(word1)
    
    val word2 = viewModel.getRandomWord()
    assertNotNull(word2)
    
    assertNotEquals(word1, word2)

    val word3 = viewModel.getRandomWord()
    assertNotNull(word3)
    assertNotEquals(word2, word3)
  }

  @Test
  fun `same day shows same word`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = AppViewModel(app)

    // Initially checks/loads a word of the day
    val originalWotd = viewModel.uiState.value.wordOfTheDay
    assertNotNull(originalWotd.word)

    // Call it again on the same day - should return the identical word
    viewModel.checkAndUpdateWordOfTheDay()
    val secondWotd = viewModel.uiState.value.wordOfTheDay
    assertEquals(originalWotd.word, secondWotd.word)
    assertEquals(originalWotd.meaning, secondWotd.meaning)
  }

  @Test
  fun `new day generates new word`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = AppViewModel(app)

    val wotdDay1 = viewModel.uiState.value.wordOfTheDay
    assertNotNull(wotdDay1.word)

    // Manually push back the saved date in SharedPreferences to simulate the next day
    val sharedPrefs = app.getSharedPreferences("neuralex_prefs", Context.MODE_PRIVATE)
    sharedPrefs.edit().putString("wotd_generated_date", "2012-12-12").apply()

    // Call checkAndUpdateWordOfTheDay - should generate a new day's word
    viewModel.checkAndUpdateWordOfTheDay()
    val wotdDay2 = viewModel.uiState.value.wordOfTheDay

    assertNotEquals(wotdDay1.word, wotdDay2.word)
  }

  @Test
  fun `app restart preserves daily word`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel1 = AppViewModel(app)

    val wotd1 = viewModel1.uiState.value.wordOfTheDay
    assertNotNull(wotd1.word)

    // Instantiate a new ViewModel simulating an app restart
    val viewModel2 = AppViewModel(app)
    val wotd2 = viewModel2.uiState.value.wordOfTheDay

    assertEquals(wotd1.word, wotd2.word)
    assertEquals(wotd1.meaning, wotd2.meaning)
  }

  @Test
  fun `local word validation check`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = AppViewModel(app)

    // Valid words
    assertTrue(viewModel.isValidWord("resilient"))
    assertTrue(viewModel.isValidWord("dictionary"))
    assertTrue(viewModel.isValidWord("computer"))
    assertTrue(viewModel.isValidWord("beautiful"))
    assertTrue(viewModel.isValidWord("vocabulary"))
    assertTrue(viewModel.isValidWord("euphoria"))
    
    // Test case-insensitivity & trimming
    assertTrue(viewModel.isValidWord("  ReSiLiEnT  "))

    // Inflected words (optionals)
    assertTrue(viewModel.isValidWord("running")) // running -> run
    assertTrue(viewModel.isValidWord("worked"))  // worked -> work
    assertTrue(viewModel.isValidWord("runs"))    // runs -> run

    // Invalid words
    assertFalse(viewModel.isValidWord("asdfghjkl"))
    assertFalse(viewModel.isValidWord("zzzzzzzabc"))
    assertFalse(viewModel.isValidWord("qwertyuiop123"))
    assertFalse(viewModel.isValidWord("abcxyzrandom"))
    assertFalse(viewModel.isValidWord("xyz123"))
    assertFalse(viewModel.isValidWord("testabcxyz"))
  }

  @Test
  fun `high performance word validation`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = AppViewModel(app)

    val startTime = System.currentTimeMillis()
    for (i in 1..10000) {
      viewModel.isValidWord("resilient")
      viewModel.isValidWord("asdfghjkl")
    }
    val endTime = System.currentTimeMillis()
    val duration = endTime - startTime
    println("Time taken for 20000 lookups: ${duration}ms")
    assertTrue("Lookups should be instant (under 500ms)", duration < 500)
  }
}

