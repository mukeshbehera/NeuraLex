package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.data.state.AppViewModel
import com.example.ui.components.NeuraLexBottomBar
import com.example.ui.navigation.Route
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.NeuraLexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AppViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current

            // 1. Dynamic or manual theme selection mapping
            val darkTheme = when (uiState.themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            val sharedPrefs = remember {
                context.getSharedPreferences("neuralex_prefs", Context.MODE_PRIVATE)
            }
            val startRoute = remember {
                if (sharedPrefs.getBoolean("has_completed_onboarding", false)) Route.Home.path else Route.Welcome.path
            }

            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // 2. Intercept Android back button tap transitions safely based on Jetpack compose navigation state
            BackHandler(enabled = currentRoute != Route.Welcome.path) {
                if (currentRoute == Route.Home.path) {
                    // Let default app activity exit normally
                    this@MainActivity.finish()
                } else {
                    if (!navController.popBackStack()) {
                        navController.navigate(Route.Home.path) {
                            popUpTo(Route.Home.path) { inclusive = true }
                        }
                    }
                }
            }

            NeuraLexTheme(darkTheme = darkTheme) {
                // Determine whether the navigation elements are displayed (Home, Bookmarks, History, Settings)
                val showBottomBar = currentRoute in listOf(
                    Route.Home.path,
                    Route.Bookmarks.path,
                    Route.History.path,
                    Route.Settings.path
                )
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            val activeTabId = when (currentRoute) {
                                Route.Home.path -> "home"
                                Route.Bookmarks.path -> "bookmarks"
                                Route.History.path -> "history"
                                Route.Settings.path -> "settings"
                                else -> "home"
                            }
                            NeuraLexBottomBar(
                                currentTab = activeTabId,
                                onTabSelected = { tabId ->
                                    val targetRoute = when (tabId) {
                                        "home" -> Route.Home.path
                                        "bookmarks" -> Route.Bookmarks.path
                                        "history" -> Route.History.path
                                        "settings" -> Route.Settings.path
                                        else -> Route.Home.path
                                    }
                                    if (currentRoute != targetRoute) {
                                        navController.navigate(targetRoute) {
                                            popUpTo(Route.Home.path) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = startRoute,
                            modifier = Modifier.fillMaxSize(),
                            enterTransition = {
                                fadeIn(animationSpec = tween(400)) + slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(400)
                                )
                            },
                            exitTransition = {
                                fadeOut(animationSpec = tween(400)) + slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(400)
                                )
                            },
                            popEnterTransition = {
                                fadeIn(animationSpec = tween(400)) + slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(400)
                                )
                            },
                            popExitTransition = {
                                fadeOut(animationSpec = tween(400)) + slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(400)
                                )
                            }
                        ) {
                            composable(Route.Welcome.path) {
                                WelcomeScreen(
                                    onGetStarted = {
                                        sharedPrefs.edit().putBoolean("has_completed_onboarding", true).apply()
                                        navController.navigate(Route.Home.path) {
                                            popUpTo(Route.Welcome.path) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(Route.Home.path) {
                                HomeScreen(
                                    uiState = uiState,
                                    onSearch = { query ->
                                        if (query.isNotBlank()) {
                                            viewModel.searchWord(query)
                                            navController.navigate(Route.Detail.createRoute(query))
                                        }
                                    },
                                    onWordSelected = { word ->
                                        viewModel.searchWord(word.word)
                                        navController.navigate(Route.Detail.createRoute(word.word))
                                    },
                                    onToggleFavorite = { word -> viewModel.toggleFavorite(word) },
                                    onClearHistory = { viewModel.clearSearchHistory() },
                                    onQuickAction = { actionId ->
                                        when (actionId) {
                                            "favorites" -> navController.navigate(Route.Bookmarks.path) {
                                                popUpTo(Route.Home.path) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                            "history" -> navController.navigate(Route.History.path) {
                                                popUpTo(Route.Home.path) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                            "random" -> {
                                                viewModel.searchWord("Ephemeral")
                                                navController.navigate(Route.Detail.createRoute("Ephemeral"))
                                            }
                                            "wotd" -> {
                                                viewModel.searchWord("Serendipity")
                                                navController.navigate(Route.Detail.createRoute("Serendipity"))
                                            }
                                            "premium" -> Toast.makeText(context, "Premium features coming soon!", Toast.LENGTH_SHORT).show()
                                            "view_all" -> navController.navigate(Route.Bookmarks.path) {
                                                popUpTo(Route.Home.path) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                            composable(Route.Bookmarks.path) {
                                BookmarksScreen(
                                    favorites = uiState.favorites,
                                    onWordSelected = { word ->
                                        viewModel.searchWord(word.word)
                                        navController.navigate(Route.Detail.createRoute(word.word))
                                    },
                                    onToggleFavorite = { word -> viewModel.toggleFavorite(word) }
                                )
                            }
                            composable(Route.History.path) {
                                HistoryScreen(
                                    historyTerms = uiState.searchHistory,
                                    onTermSelected = { term ->
                                        viewModel.searchWord(term)
                                        navController.navigate(Route.Detail.createRoute(term))
                                    },
                                    onClearHistory = { viewModel.clearSearchHistory() }
                                )
                            }
                            composable(
                                route = Route.Detail.path
                            ) { backStackEntry ->
                                val wordArg = backStackEntry.arguments?.getString("word") ?: ""
                                // Track selected state in ViewModel if we enter directly or deep link
                                androidx.compose.runtime.LaunchedEffect(wordArg) {
                                    if (uiState.selectedWord == null || !uiState.selectedWord?.word.equals(wordArg, ignoreCase = true)) {
                                        viewModel.searchWord(wordArg)
                                    }
                                }
                                DetailScreen(
                                    wordObj = uiState.selectedWord,
                                    onBack = { navController.popBackStack() },
                                    onSearchWord = { word ->
                                        viewModel.searchWord(word)
                                        navController.navigate(Route.Detail.createRoute(word))
                                    },
                                    onToggleFavorite = { word -> viewModel.toggleFavorite(word) }
                                )
                            }
                            composable(Route.Settings.path) {
                                SettingsScreen(
                                    uiState = uiState,
                                    onSetTheme = { theme -> viewModel.setTheme(theme) },
                                    onUpdateAi = { url, key, modelName -> viewModel.updateAiConfig(url, key, modelName) },
                                    onTestConnection = { viewModel.testConnection() },
                                    onClearConnectionTestResult = { viewModel.clearConnectionTestResult() },
                                    onImportFavorites = { backup -> viewModel.importFavoritesFromJson(backup) },
                                    onExportFavorites = { viewModel.exportFavoritesToJson() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
