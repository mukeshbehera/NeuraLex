package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

            var showSplash by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                // Keep splash displayed for a short period to allow database load & look beautiful
                kotlinx.coroutines.delay(1800)
                showSplash = false
            }

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

            // 2. Intercept Android back button tap transitions safely based on select navigation and loading state
            BackHandler(enabled = currentRoute != Route.Welcome.path && !showSplash) {
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
                Box(modifier = Modifier.fillMaxSize()) {
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
                        if (uiState.showWordNotFoundDialog) {
                            androidx.compose.material3.AlertDialog(
                                onDismissRequest = { viewModel.dismissWordNotFoundDialog() },
                                title = {
                                    androidx.compose.material3.Text(
                                        text = "Word Not Found",
                                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    )
                                },
                                text = {
                                    androidx.compose.material3.Text(
                                        text = "No dictionary entry exists for this word.",
                                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                                    )
                                },
                                confirmButton = {
                                    androidx.compose.material3.TextButton(
                                        onClick = {
                                            viewModel.dismissWordNotFoundDialog()
                                        }
                                    ) {
                                        androidx.compose.material3.Text(
                                            text = "Search Again",
                                            color = com.example.ui.theme.PrimaryPurple,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }
                                },
                                dismissButton = {
                                    androidx.compose.material3.TextButton(
                                        onClick = { viewModel.dismissWordNotFoundDialog() }
                                    ) {
                                        androidx.compose.material3.Text(
                                            text = "Cancel",
                                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            )
                        }
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
                                            if (viewModel.searchWord(query)) {
                                                navController.navigate(Route.Detail.createRoute(query))
                                            }
                                        }
                                    },
                                    onWordSelected = { word ->
                                        if (viewModel.searchWord(word.word)) {
                                            navController.navigate(Route.Detail.createRoute(word.word))
                                        }
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
                                                val randWord = viewModel.getRandomWord()
                                                if (randWord != null) {
                                                    viewModel.searchWord(randWord)
                                                    navController.navigate(Route.Detail.createRoute(randWord))
                                                }
                                            }
                                            "wotd" -> {
                                                val wotdWord = viewModel.uiState.value.wordOfTheDay.word
                                                viewModel.searchWord(wotdWord)
                                                navController.navigate(Route.Detail.createRoute(wotdWord))
                                            }
                                            "premium" -> Toast.makeText(context, "Premium features coming soon!", Toast.LENGTH_SHORT).show()
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
                                    onClearHistory = { viewModel.clearSearchHistory() },
                                    onDeleteSingle = { term -> viewModel.deleteSearchHistoryEntry(term) },
                                    onDeleteMultiple = { terms -> viewModel.deleteSearchHistoryEntries(terms) }
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
                } // Close Scaffold
                
                // Branded splash/loading screen overlay with high-fidelity fade animation
                AnimatedVisibility(
                    visible = showSplash,
                    enter = fadeIn(animationSpec = tween(400)),
                    exit = fadeOut(animationSpec = tween(600))
                ) {
                    SplashScreen()
                }
            } // Close outer Box
        } // Close NeuraLexTheme
    } // Close setContent
} // Close onCreate
} // Close MainActivity class

val SafeEaseOutBack = Easing { fraction ->
    if (fraction <= 0f) return@Easing 0f
    if (fraction >= 0.99f) return@Easing 1f
    try {
        EaseOutBack.transform(fraction)
    } catch (e: IllegalArgumentException) {
        1f
    }
}

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier
) {
    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateIn = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutQuad),
        label = "splashAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0.85f,
        animationSpec = tween(durationMillis = 1000, easing = SafeEaseOutBack),
        label = "splashScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        com.example.ui.theme.GradientStart,
                        com.example.ui.theme.GradientEnd
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .graphicsLayer {
                    this.alpha = alpha
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            // Branded App Logo
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_neuralex_logo_vector),
                contentDescription = "NeuraLex Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(28.dp),
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.25f),
                        spotColor = Color.Black.copy(alpha = 0.35f)
                    )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "NeuraLex",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp,
                    letterSpacing = (-0.5).sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tagline or loading feedback
            Text(
                text = "Learn. Speak. Elevate.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                color = Color.White.copy(alpha = 0.5f),
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
