package com.example.gymclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gymclock.data.*
import com.example.gymclock.repository.GymClockRepository
import com.example.gymclock.ui.MainViewModel
import com.example.gymclock.ui.navigation.Screen
import com.example.gymclock.ui.screens.*

class MainActivity : ComponentActivity() {
    private lateinit var db: GymClockDatabase
    private lateinit var repository: GymClockRepository
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database and repository
        db = GymClockDatabase.getInstance(this)
        repository = GymClockRepository(db.gymClockDao())
        viewModel = MainViewModel(repository)

        setContent {
            GymClockApp(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymClockApp(viewModel: MainViewModel) {
    val navController = rememberNavController()

    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val currentRoute = navController.currentBackStackEntry?.destination?.route

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home") },
                        selected = currentRoute == Screen.Home.route,
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                        label = { Text("Planner") },
                        selected = currentRoute == Screen.WorkoutPlanner.route,
                        onClick = {
                            navController.navigate(Screen.WorkoutPlanner.route)
                        }
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Timer, contentDescription = null) },
                        label = { Text("Timer") },
                        selected = currentRoute == Screen.Timer.route,
                        onClick = {
                            navController.navigate(Screen.Timer.route)
                        }
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        label = { Text("Splits") },
                        selected = currentRoute == Screen.Splits.route,
                        onClick = {
                            navController.navigate(Screen.Splits.route)
                        }
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToPlanner = { navController.navigate(Screen.WorkoutPlanner.route) },
                        onNavigateToSplits = { navController.navigate(Screen.Splits.route) },
                        onNavigateToTimer = { navController.navigate(Screen.Timer.route) }
                    )
                }

                composable(Screen.WorkoutPlanner.route) {
                    WorkoutPlannerScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Timer.route) {
                    TimerScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Splits.route) {
                    SplitsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
