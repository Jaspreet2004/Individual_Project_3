package com.example.individualproject3.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object ParentDashboard : Screen("parent_dashboard")
    object KidDashboard : Screen("kid_dashboard")
    object Game : Screen("game/{levelId}") {
        fun createRoute(levelId: String) = "game/$levelId"
    }
    object Home : Screen("home")
    object LevelList : Screen("level_list")
    object LevelBuilder : Screen("level_builder?levelId={levelId}") {
        fun createRoute(levelId: Int? = null) = if (levelId != null) "level_builder?levelId=$levelId" else "level_builder"
    }
    object Splash : Screen("splash")
    object QuizTopics : Screen("quiz_topics")
    object Quiz : Screen("quiz/{topicId}") {
        fun createRoute(topicId: String) = "quiz/$topicId"
    }
}
