package ru.zytracker.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Main : Screen("main")
    object Calendar : Screen("calendar")
    object Settings : Screen("settings")
}
