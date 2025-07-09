package com.example.gymclock.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object WorkoutPlanner : Screen("workout_planner")
    object Timer : Screen("timer")
    object Splits : Screen("splits")
    object Progress : Screen("progress")
    object Exercises : Screen("exercises")
}
