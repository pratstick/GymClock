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
import com.example.gymclock.timer.TimerState
import com.example.gymclock.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val timerState by viewModel.timer.state.collectAsStateWithLifecycle()
    var customTime by remember { mutableStateOf("60") }
    var showCustomDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Workout Timer") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Timer Display
            item {
                MainTimerDisplay(
                    timerState = timerState,
                    onPause = { viewModel.timer.pauseTimer() },
                    onResume = { viewModel.timer.resumeTimer() },
                    onReset = { viewModel.timer.resetTimer() },
                    onStop = { viewModel.timer.stopTimer() },
                    onAddTime = { viewModel.timer.addTime(it) }
                )
            }

            // Quick Start Buttons
            item {
                QuickStartCard(
                    onStartTimer = { seconds ->
                        viewModel.startRestTimer(seconds) {
                            // Timer finished - could add sound/vibration
                        }
                    },
                    onCustomTimer = { showCustomDialog = true }
                )
            }

            // Preset Timers
            item {
                PresetTimersCard(
                    onStartTimer = { seconds ->
                        viewModel.startRestTimer(seconds) {}
                    }
                )
            }

            // Timer History/Stats (placeholder for future feature)
            item {
                TimerStatsCard()
            }
        }
    }

    // Custom Timer Dialog
    if (showCustomDialog) {
        CustomTimerDialog(
            currentValue = customTime,
            onDismiss = { showCustomDialog = false },
            onStart = { time ->
                val seconds = time.toIntOrNull() ?: 60
                viewModel.startRestTimer(seconds) {}
                showCustomDialog = false
            }
        )
    }
}

@Composable
fun MainTimerDisplay(
    timerState: TimerState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onStop: () -> Unit,
    onAddTime: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                timerState.isFinished -> MaterialTheme.colorScheme.errorContainer
                timerState.isPaused -> MaterialTheme.colorScheme.tertiaryContainer
                timerState.isRunning -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Rest Timer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            // Large Timer Display
            Text(
                formatTime(timerState.currentSeconds),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    timerState.isFinished -> MaterialTheme.colorScheme.error
                    timerState.isPaused -> MaterialTheme.colorScheme.tertiary
                    timerState.isRunning -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            if (timerState.isRunning || timerState.isPaused) {
                Text(
                    "/ ${formatTime(timerState.totalSeconds)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(24.dp))

            // Timer Status
            if (timerState.isFinished) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Rest Complete!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (timerState.isPaused) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Paused",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Timer Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    timerState.isPaused -> {
                        Button(onClick = onResume) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Resume")
                        }
                    }
                    timerState.isRunning -> {
                        Button(onClick = onPause) {
                            Icon(Icons.Default.Pause, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Pause")
                        }
                    }
                }

                if (timerState.isRunning || timerState.isPaused || timerState.isFinished) {
                    OutlinedButton(onClick = onReset) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Reset")
                    }
                }

                if (timerState.isRunning || timerState.isPaused) {
                    FilledTonalButton(onClick = { onAddTime(30) }) {
                        Text("+30s")
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStartCard(
    onStartTimer: (Int) -> Unit,
    onCustomTimer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Quick Start",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { onStartTimer(60) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("1min")
                }
                FilledTonalButton(
                    onClick = { onStartTimer(90) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("1.5min")
                }
                FilledTonalButton(
                    onClick = { onStartTimer(120) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("2min")
                }
                FilledTonalButton(
                    onClick = { onStartTimer(180) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("3min")
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = onCustomTimer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Custom Time")
            }
        }
    }
}

@Composable
fun PresetTimersCard(
    onStartTimer: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Exercise-Specific Rest Times",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            val presets = listOf(
                "Compound Lifts" to 180,
                "Isolation Exercises" to 90,
                "Cardio Intervals" to 60,
                "Powerlifting" to 300,
                "Bodyweight" to 45
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { (name, seconds) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "${seconds}s rest",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FilledTonalButton(
                            onClick = { onStartTimer(seconds) }
                        ) {
                            Text("Start")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerStatsCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Timer Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Coming soon: Timer usage statistics and workout duration tracking",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CustomTimerDialog(
    currentValue: String,
    onDismiss: () -> Unit,
    onStart: (String) -> Unit
) {
    var timeInput by remember { mutableStateOf(currentValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Timer") },
        text = {
            Column {
                Text("Enter rest time in seconds:")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = timeInput,
                    onValueChange = { timeInput = it },
                    label = { Text("Seconds") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onStart(timeInput) },
                enabled = timeInput.toIntOrNull() != null && timeInput.toInt() > 0
            ) {
                Text("Start Timer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}
