package com.example.gymclock.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymclock.data.WorkoutWithExercise
import com.example.gymclock.timer.TimerState
import com.example.gymclock.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToPlanner: () -> Unit,
    onNavigateToSplits: () -> Unit,
    onNavigateToTimer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timerState by viewModel.timer.state.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "GymClock",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Your Workout Companion",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Today's Plan
        item {
            TodaysPlanCard(
                schedule = uiState.todaySchedule?.plan ?: "Rest",
                goal = uiState.todayGoal?.let { "${it.reps} reps @ ${it.weight} kg" } ?: "No goal set",
                onPlannerClick = onNavigateToPlanner
            )
        }

        // Quick Timer
        item {
            QuickTimerCard(
                timerState = timerState,
                onStartTimer = { seconds ->
                    viewModel.startRestTimer(seconds) {
                        // Timer finished - could add vibration/sound here
                    }
                },
                onPauseTimer = { viewModel.timer.pauseTimer() },
                onResumeTimer = { viewModel.timer.resumeTimer() },
                onResetTimer = { viewModel.timer.resetTimer() },
                onNavigateToTimer = onNavigateToTimer
            )
        }

        // Today's Workouts
        item {
            WorkoutListCard(
                workouts = uiState.todayWorkouts,
                onCompleteWorkout = { workout -> viewModel.completeWorkout(workout) },
                onStartRestTimer = { seconds ->
                    viewModel.startRestTimer(seconds) {}
                }
            )
        }

        // Quick Actions
        item {
            QuickActionsCard(
                onNavigateToSplits = onNavigateToSplits,
                onNavigateToPlanner = onNavigateToPlanner
            )
        }
    }
}

@Composable
fun TodaysPlanCard(
    schedule: String,
    goal: String,
    onPlannerClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Today's Plan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onPlannerClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit plan")
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Today,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    schedule,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Flag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    goal,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuickTimerCard(
    timerState: TimerState,
    onStartTimer: (Int) -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onNavigateToTimer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Quick Timer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNavigateToTimer) {
                    Icon(Icons.Default.Timer, contentDescription = "Advanced timer")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Timer Display
            if (timerState.isRunning || timerState.isPaused || timerState.isFinished) {
                Text(
                    "${timerState.currentSeconds / 60}:${String.format("%02d", timerState.currentSeconds % 60)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        timerState.isFinished -> MaterialTheme.colorScheme.error
                        timerState.isPaused -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Timer Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (timerState.isPaused) {
                        FilledTonalButton(onClick = onResumeTimer) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Resume")
                        }
                    } else if (timerState.isRunning) {
                        FilledTonalButton(onClick = onPauseTimer) {
                            Icon(Icons.Default.Pause, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Pause")
                        }
                    }

                    OutlinedButton(onClick = onResetTimer) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Reset")
                    }
                }
            } else {
                // Quick start buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(onClick = { onStartTimer(60) }) {
                        Text("1min")
                    }
                    FilledTonalButton(onClick = { onStartTimer(90) }) {
                        Text("1.5min")
                    }
                    FilledTonalButton(onClick = { onStartTimer(120) }) {
                        Text("2min")
                    }
                    FilledTonalButton(onClick = { onStartTimer(180) }) {
                        Text("3min")
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutListCard(
    workouts: List<WorkoutWithExercise>,
    onCompleteWorkout: (WorkoutWithExercise) -> Unit,
    onStartRestTimer: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Today's Exercises (${workouts.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            if (workouts.isEmpty()) {
                Text(
                    "No exercises planned for today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                workouts.forEach { workout ->
                    WorkoutItem(
                        workout = workout,
                        onComplete = { onCompleteWorkout(workout) },
                        onStartRest = { onStartRestTimer(workout.restTimeSeconds) }
                    )
                    if (workout != workouts.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutItem(
    workout: WorkoutWithExercise,
    onComplete: () -> Unit,
    onStartRest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                workout.exerciseName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                "${workout.sets} sets × ${workout.reps} reps @ ${workout.weight}kg",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (workout.isCompleted) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            Row {
                IconButton(onClick = onStartRest) {
                    Icon(Icons.Default.Timer, contentDescription = "Start rest timer")
                }
                IconButton(onClick = onComplete) {
                    Icon(Icons.Default.Check, contentDescription = "Mark complete")
                }
            }
        }
    }
}

@Composable
fun QuickActionsCard(
    onNavigateToSplits: () -> Unit,
    onNavigateToPlanner: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onNavigateToSplits,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Browse Splits")
                }

                FilledTonalButton(
                    onClick = onNavigateToPlanner,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Plan Workout")
                }
            }
        }
    }
}
