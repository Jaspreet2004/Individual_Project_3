package com.example.individualproject3

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.individualproject3.ui.GameScreen
import com.example.individualproject3.ui.HomeScreen
import com.example.individualproject3.ui.LevelSelectScreen
import com.example.individualproject3.ui.LoginScreen
import com.example.individualproject3.ui.ParentDashboardScreen
import com.example.individualproject3.ui.Screen
import com.example.individualproject3.ui.theme.IndividualProject3Theme

/**
 * Main Entry Point of the Application.
 * Handles the Navigation Graph and core setup.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            val prefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            
            // Load saved preference or default to system
            val savedDarkTheme = remember { prefs.getBoolean("is_dark_theme", isSystemDark) }
            var darkTheme by remember { androidx.compose.runtime.mutableStateOf(savedDarkTheme) }

            IndividualProject3Theme(darkTheme = darkTheme) {
                // Starts the Navigation Graph
                AppNavigation(
                    isDarkTheme = darkTheme,
                    onToggleTheme = { 
                        val newTheme = !darkTheme
                        darkTheme = newTheme
                        prefs.edit().putBoolean("is_dark_theme", newTheme).apply()
                    }
                )
            }
        }
    }
}

/**
 * Defines the Navigation Graph for the application.
 * Manages transitions between Login, Parent Dashboard, Kid Dashboard, and Game Screen.
 */
@Composable
fun AppNavigation(isDarkTheme: Boolean, onToggleTheme: () -> Unit) {
    val navController = rememberNavController()
    // Capture context for logging and intent launching
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route, // Start with Splash Screen
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash Screen Route
            composable(Screen.Splash.route) {
                com.example.individualproject3.ui.SplashScreen(navController = navController)
            }

            // Login Screen Route
            composable(Screen.Login.route) {
                LoginScreen(navController = navController) { isParent ->
                    // Navigate based on role
                    if (isParent) {
                        navController.navigate(Screen.ParentDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            }
            
            // Home Screen
            composable(Screen.Home.route) {
                HomeScreen(
                    navController = navController, 
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onLogout = {
                        com.example.individualproject3.util.FileLogger.log(context, "MainActivity: Home Logout -> Nuclear Restart")
                        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        context.startActivity(intent)
                        Runtime.getRuntime().exit(0)
                    }
                )
            }
            
            // Parent Dashboard Route
            composable(Screen.ParentDashboard.route) {
                ParentDashboardScreen(navController = navController) {
                    com.example.individualproject3.util.FileLogger.log(context, "MainActivity: Parent Logout -> Nuclear Restart")
                    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)
                    Runtime.getRuntime().exit(0) // Kills the process to force s clean state
                }
            }

            // Level List Route
            composable(Screen.LevelList.route) {
                com.example.individualproject3.ui.LevelListScreen(navController = navController, onBack = {
                     navController.popBackStack() // Back to Home
                }, onCreateNew = {
                     navController.navigate(Screen.LevelBuilder.route)
                })
            }

            // Level Builder Route
            composable(
                route = Screen.LevelBuilder.route,
                arguments = listOf(androidx.navigation.navArgument("levelId") {
                    type = NavType.IntType
                    defaultValue = -1 // Use -1 to signify "new level"
                })
            ) { backStackEntry ->
                val levelId = backStackEntry.arguments?.getInt("levelId") ?: -1
                val actualLevelId = if (levelId == -1) null else levelId
                
                com.example.individualproject3.ui.LevelBuilderScreen(
                    navController = navController, 
                    levelId = actualLevelId,
                    onBack = {
                        navController.popBackStack() // Back to List
                    }
                )
            }

            // Kid Dashboard
            composable(Screen.KidDashboard.route) {
                LevelSelectScreen(navController = navController, onLevelSelected = { levelId ->
                    navController.navigate(Screen.Game.createRoute(levelId))
                }, onLogout = {
                    com.example.individualproject3.util.FileLogger.log(context, "MainActivity: Kid Logout -> Nuclear Restart")
                    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)
                    Runtime.getRuntime().exit(0)
                }, onBack = {
                    navController.popBackStack()
                })
            }

            // Game Screen Route with arguments
            composable(
                route = Screen.Game.route,
                arguments = listOf(navArgument("levelId") { type = NavType.StringType })
            ) { backStackEntry ->
                val levelId = backStackEntry.arguments?.getString("levelId") ?: "1-1"
                GameScreen(navController = navController, levelId = levelId)
            }
        }
    }
}