package com.hautt.playintegrity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hautt.playintegrity.ui.detail.ProofDetailScreen
import com.hautt.playintegrity.ui.history.HistoryScreen
import com.hautt.playintegrity.ui.home.HomeScreen
import com.hautt.playintegrity.ui.theme.PlayIntegrityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlayIntegrityTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                onNavigateToHistory = {
                                    navController.navigate("history")
                                }
                            )
                        }

                        composable("history") {
                            HistoryScreen(
                                onBack = { navController.popBackStack() },
                                onProofTap = { requestId ->
                                    navController.navigate("detail/$requestId")
                                }
                            )
                        }

                        composable(
                            route = "detail/{requestId}",
                            arguments = listOf(
                                navArgument("requestId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
                            ProofDetailScreen(
                                requestId = requestId,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
