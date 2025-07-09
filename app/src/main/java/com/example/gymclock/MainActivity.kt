package com.example.gymclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberSnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.gymclock.data.*
import io.github.vanpra.composematerialdialogs.MaterialDialog
import io.github.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var db: GymClockDatabase
    private lateinit var dao: GymClockDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = GymClockDatabase.getInstance(this)
        dao = db.gymClockDao()
        setContent {
            GymClockApp(dao)
        }
    }
}

@Composable
fun GymClockApp(dao: GymClockDao) {
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val today = remember { java.time.LocalDate.now().dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() } }
    val scope = rememberCoroutineScope()

    var schedule by remember { mutableStateOf<ScheduleEntity?>(null) }
    var goal by remember { mutableStateOf<GoalEntity?>(null) }
    var timerSeconds by remember { mutableStateOf(60) }
    var timerRunning by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var inputPlan by remember { mutableStateOf("") }
    var inputReps by remember { mutableStateOf("") }
    var inputWeight by remember { mutableStateOf("") }
    val snackbarHostState = rememberSnackbarHostState()
    var inputError by remember { mutableStateOf<String?>(null) }

    // Collect today's schedule and goal
    LaunchedEffect(today) {
        dao.getScheduleForDay(today).collect { schedule = it }
    }
    LaunchedEffect(today) {
        dao.getGoalForDay(today).collect { goal = it }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Today's Plan: ${schedule?.plan ?: "Rest"}", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { showScheduleDialog = true }) { Text("Set Today's Plan") }
                Spacer(Modifier.height(16.dp))
                Text("Goal: ${goal?.let { "${it.reps} reps @ ${it.weight} kg" } ?: "Not set"}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { showGoalDialog = true }) { Text("Set Today's Goal") }
                Spacer(Modifier.height(24.dp))
                Text("Rest Timer: $timerSeconds s", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    if (!timerRunning) {
                        timerRunning = true
                        scope.launch {
                            for (i in timerSeconds downTo 1) {
                                timerSeconds = i
                                delay(1000)
                            }
                            timerRunning = false
                            timerSeconds = 60
                        }
                    }
                }, enabled = !timerRunning) {
                    Text("Start Rest Timer")
                }
                SnackbarHost(hostState = snackbarHostState)
            }
            // Material Dialogs for input
            if (showScheduleDialog) {
                MaterialDialog(dialogState = rememberMaterialDialogState(true), onCloseRequest = { showScheduleDialog = false }) {
                    title("Set Today's Plan")
                    input(
                        label = "Plan (e.g. Push, Pull, Rest)",
                        prefill = schedule?.plan ?: "",
                        onInput = { inputPlan = it }
                    )
                    if (inputError != null) {
                        Text(inputError!!, color = MaterialTheme.colorScheme.error)
                    }
                    buttons {
                        positiveButton("Save") {
                            if (inputPlan.isBlank()) {
                                inputError = "Plan cannot be empty."
                            } else {
                                scope.launch {
                                    dao.upsertSchedule(ScheduleEntity(today, inputPlan))
                                }
                                showScheduleDialog = false
                                inputError = null
                            }
                        }
                        negativeButton("Cancel") {
                            showScheduleDialog = false
                            inputError = null
                        }
                    }
                }
            }
            if (showGoalDialog) {
                MaterialDialog(dialogState = rememberMaterialDialogState(true), onCloseRequest = { showGoalDialog = false }) {
                    title("Set Today's Goal")
                    Column {
                        OutlinedTextField(
                            value = inputReps,
                            onValueChange = { inputReps = it },
                            label = { Text("Reps") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = inputError != null && inputError!!.contains("reps")
                        )
                        OutlinedTextField(
                            value = inputWeight,
                            onValueChange = { inputWeight = it },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = inputError != null && inputError!!.contains("weight")
                        )
                        if (inputError != null && (inputError!!.contains("reps") || inputError!!.contains("weight"))) {
                            Text(inputError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    buttons {
                        positiveButton("Save") {
                            val reps = inputReps.toIntOrNull()
                            val weight = inputWeight.toIntOrNull()
                            when {
                                reps == null || reps <= 0 -> inputError = "Please enter valid reps."
                                weight == null || weight < 0 -> inputError = "Please enter valid weight."
                                else -> {
                                    scope.launch {
                                        dao.upsertGoal(GoalEntity(today, reps, weight))
                                    }
                                    showGoalDialog = false
                                    inputError = null
                                }
                            }
                        }
                        negativeButton("Cancel") {
                            showGoalDialog = false
                            inputError = null
                        }
                    }
                }
            }
        }
    }
}
