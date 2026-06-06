package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Modern Jetpack Navigation Compose type-safe Route definition system.
 * Simulates Group routing and Stack navigation matching modern design standards.
 */
sealed class Route(val path: String) {
    
    // Welcome / Onboarding Group (Hidden Headers & Bottom Bars)
    object Welcome : Route("welcome")

    // Main Tab Group (Bottom Tab Navigation)
    object Home : Route("home")
    object Bookmarks : Route("bookmarks")
    object History : Route("history")
    object Settings : Route("settings")

    // Inner Stack Details (Hides bottom bar dynamically)
    object Detail : Route("detail/{word}") {
        fun createRoute(word: String): String = "detail/$word"
    }
}

/**
 * Tab item structure containing design presentation metadata.
 */
data class TabItem(
    val label: String,
    val route: String,
    val icon: ImageVector,
    val testTag: String
)

/**
 * Complete list of primary tabs rendered inside the bottom navigation bar.
 */
val bottomNavigationItems = listOf(
    TabItem(
        label = "Home",
        route = Route.Home.path,
        icon = Icons.Default.Home,
        testTag = "tab_home"
    ),
    TabItem(
        label = "Bookmarks",
        route = Route.Bookmarks.path,
        icon = Icons.Default.Bookmarks,
        testTag = "tab_bookmarks"
    ),
    TabItem(
        label = "History",
        route = Route.History.path,
        icon = Icons.Default.History,
        testTag = "tab_history"
    ),
    TabItem(
        label = "Settings",
        route = Route.Settings.path,
        icon = Icons.Default.Settings,
        testTag = "tab_settings"
    )
)
