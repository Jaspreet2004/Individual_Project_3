package com.example.individualproject3.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object ParentDashboard : Screen("parent_dashboard")
    object KidDashboard : Screen("kid_dashboard")
    object Game : Screen("game/{levelId}") {
        fun createRoute(levelId: String) = "game/$levelId"
    }
    object Home : Screen("home")
    object Splash : Screen("splash")
}
