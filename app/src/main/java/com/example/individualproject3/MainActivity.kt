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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.individualproject3.ui.GameScreen
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
            IndividualProject3Theme {
                // Starts the Navigation Graph
                AppNavigation()
            }
        }
    }
}

/**
 * Defines the Navigation Graph for the application.
 * Manages transitions between Login, Parent Dashboard, Kid Dashboard, and Game Screen.
 */
@Composable
fun AppNavigation() {
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
                        navController.navigate(Screen.KidDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
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