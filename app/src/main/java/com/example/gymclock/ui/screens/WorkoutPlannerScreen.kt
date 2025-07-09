package com.example.gymclock.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymclock.data.ExerciseEntity
import com.example.gymclock.ui.MainViewModel
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlannerScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf("Monday") }
    val snackbarHostState = remember { SnackbarHostState() }
    var showSnackbar by remember { mutableStateOf<String?>(null) }
    // Get the plan for the selected day from the schedule
    val schedule by viewModel.getScheduleForDay(selectedDay).collectAsState(initial = null)
    val plan = (schedule?.plan ?: selectedDay).lowercase() // always use lowercase for plan/day
    // Get workouts for the plan (e.g., Push, Pull, Legs)
    val workoutsForPlan by viewModel.getWorkoutsForDay(plan).collectAsState(initial = emptyList())
    val goalForDay by viewModel.getGoalForDay(selectedDay).collectAsState(initial = null)
    // Get workouts for the selected day (not plan)
    val workoutsForSelectedDay by viewModel.getWorkoutsForDay(selectedDay).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Workout Planner") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Day Selector
            item {
                DaySelector(
                    selectedDay = selectedDay,
                    onDaySelected = { selectedDay = it }
                )
            }

            // Today's Workouts
            item {
                TodaysWorkoutsCard(
                    day = selectedDay, // show workouts for the selected day, not the plan
                    workouts = workoutsForSelectedDay,
                    onAddExercise = { showAddExerciseDialog = true },
                    onCompleteWorkout = { viewModel.completeWorkout(it) }
                )
            }

            // Quick Goals Setting
            item {
                QuickGoalsCard(
                    selectedDay = selectedDay,
                    currentGoal = goalForDay,
                    onUpdateGoal = { reps, weight ->
                        viewModel.updateGoal(selectedDay, reps, weight)
                    }
                )
            }
        }
    }

    // Show snackbar message if exists
    if (showSnackbar != null) {
        LaunchedEffect(showSnackbar) {
            snackbarHostState.showSnackbar(showSnackbar!!)
            showSnackbar = null
        }
    }

    // Add Exercise Dialog
    if (showAddExerciseDialog) {
        AddExerciseDialog(
            exercises = uiState.allExercises,
            onDismiss = { showAddExerciseDialog = false },
            onAddExercise = { exercise, sets, reps, weight, restTime, isCustom ->
                android.util.Log.d("WorkoutPlannerScreen", "onAddExercise called: exercise=${exercise.name}, sets=$sets, reps=$reps, weight=$weight, restTime=$restTime, isCustom=$isCustom")
                if (isCustom) viewModel.addCustomExerciseAndWorkout(selectedDay, exercise, sets, reps, weight, restTime)
                else viewModel.addWorkout(selectedDay, exercise.id, sets, reps, weight, restTime)
                showAddExerciseDialog = false
                showSnackbar = if (isCustom) "Custom exercise added!" else "Exercise added!"
            }
        )
    }
}

@Composable
fun DaySelector(
    selectedDay: String,
    onDaySelected: (String) -> Unit
) {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Select Day",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                days.forEach { day ->
                    FilterChip(
                        selected = day == selectedDay,
                        onClick = { onDaySelected(day) },
                        label = { Text(day) }
                    )
                }
            }
        }
    }
}

@Composable
fun TodaysWorkoutsCard(
    day: String,
    workouts: List<com.example.gymclock.data.WorkoutWithExercise>,
    onAddExercise: () -> Unit,
    onCompleteWorkout: (com.example.gymclock.data.WorkoutWithExercise) -> Unit
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
                    "$day's Exercises",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onAddExercise) {
                    Icon(Icons.Default.Add, contentDescription = "Add exercise")
                }
            }

            Spacer(Modifier.height(8.dp))

            if (workouts.isEmpty()) {
                Text(
                    "No exercises planned for $day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(onClick = onAddExercise) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Add First Exercise")
                }
            } else {
                workouts.forEach { workout ->
                    WorkoutItemCard(
                        workout = workout,
                        onComplete = { onCompleteWorkout(workout) }
                    )
                    if (workout != workouts.last()) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutItemCard(
    workout: com.example.gymclock.data.WorkoutWithExercise,
    onComplete: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                Text(
                    "Rest: ${workout.restTimeSeconds}s",
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
                IconButton(onClick = onComplete) {
                    Icon(Icons.Default.Check, contentDescription = "Mark complete")
                }
            }
        }
    }
}

@Composable
fun QuickGoalsCard(
    selectedDay: String,
    currentGoal: com.example.gymclock.data.GoalEntity?,
    onUpdateGoal: (Int, Int) -> Unit
) {
    var showGoalDialog by remember { mutableStateOf(false) }

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
                    "$selectedDay's Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showGoalDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit goal")
                }
            }

            Spacer(Modifier.height(8.dp))

            if (currentGoal != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Flag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${currentGoal.reps} reps @ ${currentGoal.weight} kg",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Text(
                    "No goal set for $selectedDay",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showGoalDialog) {
        GoalDialog(
            currentGoal = currentGoal,
            onDismiss = { showGoalDialog = false },
            onSave = { reps, weight ->
                onUpdateGoal(reps, weight)
                showGoalDialog = false
            }
        )
    }
}

@Composable
fun AddExerciseDialog(
    exercises: List<ExerciseEntity>,
    onDismiss: () -> Unit,
    onAddExercise: (ExerciseEntity, Int, Int, Float, Int, Boolean) -> Unit
) {
    var selectedExercise by remember { mutableStateOf<ExerciseEntity?>(null) }
    var sets by remember { mutableStateOf("3") }
    var reps by remember { mutableStateOf("10") }
    var weight by remember { mutableStateOf("") }
    var restTime by remember { mutableStateOf("90") }
    var searchQuery by remember { mutableStateOf("") }

    var setsError by remember { mutableStateOf<String?>(null) }
    var repsError by remember { mutableStateOf<String?>(null) }
    var weightError by remember { mutableStateOf<String?>(null) }
    var restTimeError by remember { mutableStateOf<String?>(null) }

    fun validateInputs() {
        setsError = if (sets.toIntOrNull() == null || sets.toIntOrNull()!! <= 0) "Invalid" else null
        repsError = if (reps.toIntOrNull() == null || reps.toIntOrNull()!! <= 0) "Invalid" else null
        weightError = if (selectedExercise?.isBodyweight != true && (weight.toFloatOrNull() == null || weight.toFloatOrNull()!! < 0f)) "Invalid" else null
        restTimeError = if (restTime.toIntOrNull() == null || restTime.toIntOrNull()!! < 0) "Invalid" else null
    }

    // Move LaunchedEffect here, outside LazyColumn
    LaunchedEffect(selectedExercise) {
        if (selectedExercise?.isBodyweight == true) {
            weight = ""
        }
    }

    val filteredExercises = exercises.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    val maxVisible = 20
    var visibleCount by remember { mutableStateOf(maxVisible) }
    val showLoadMore = filteredExercises.size > visibleCount
    val visibleExercises = filteredExercises.take(visibleCount)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Exercise") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search exercises") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                items(visibleExercises) { exercise ->
                    FilterChip(
                        selected = selectedExercise == exercise,
                        onClick = { selectedExercise = exercise },
                        label = { Text(exercise.name) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (showLoadMore) {
                    item {
                        Button(onClick = { visibleCount += maxVisible }) {
                            Text("Load more")
                        }
                    }
                }
                // Remove LaunchedEffect(selectedExercise) from inside LazyColumn
                if (selectedExercise != null) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("Exercise Details", fontWeight = FontWeight.Bold)
                    }
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = sets,
                                onValueChange = {
                                    sets = it
                                    validateInputs()
                                },
                                label = { Text("Sets") },
                                isError = setsError != null,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = reps,
                                onValueChange = {
                                    reps = it
                                    validateInputs()
                                },
                                label = { Text("Reps") },
                                isError = repsError != null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (selectedExercise?.isBodyweight != true) {
                                OutlinedTextField(
                                    value = weight,
                                    onValueChange = {
                                        weight = it
                                        validateInputs()
                                    },
                                    label = { Text("Weight (kg)") },
                                    isError = weightError != null,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            OutlinedTextField(
                                value = restTime,
                                onValueChange = {
                                    restTime = it
                                    validateInputs()
                                },
                                label = { Text("Rest (s)") },
                                isError = restTimeError != null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                if (filteredExercises.isEmpty() && searchQuery.isNotBlank()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("No exercises found. Add a custom exercise:", fontWeight = FontWeight.Bold)
                        var customName by remember { mutableStateOf(searchQuery) }
                        var customCategory by remember { mutableStateOf("") }
                        var customMuscleGroup by remember { mutableStateOf("") }
                        var customIsBodyweight by remember { mutableStateOf(false) }
                        Column {
                            OutlinedTextField(
                                value = customName,
                                onValueChange = { customName = it },
                                label = { Text("Exercise Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = customCategory,
                                onValueChange = { customCategory = it },
                                label = { Text("Category (Push/Pull/Legs/Core/Other)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = customMuscleGroup,
                                onValueChange = { customMuscleGroup = it },
                                label = { Text("Muscle Group") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = customIsBodyweight,
                                    onCheckedChange = { customIsBodyweight = it }
                                )
                                Text("Bodyweight Exercise")
                            }
                            Button(
                                onClick = {
                                    val newExercise = ExerciseEntity(
                                        name = customName,
                                        category = customCategory,
                                        muscleGroup = customMuscleGroup,
                                        isBodyweight = customIsBodyweight
                                    )
                                    onAddExercise(newExercise, sets.toIntOrNull() ?: 3, reps.toIntOrNull() ?: 10, weight.toFloatOrNull() ?: 50f, restTime.toIntOrNull() ?: 90, true)
                                },
                                enabled = customName.isNotBlank() && customCategory.isNotBlank() && customMuscleGroup.isNotBlank()
                            ) {
                                Text("Add Custom Exercise")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedExercise?.let { exercise ->
                        onAddExercise(
                            exercise,
                            sets.toIntOrNull() ?: 3,
                            reps.toIntOrNull() ?: 10,
                            weight.toFloatOrNull() ?: 50f,
                            restTime.toIntOrNull() ?: 90,
                            false
                        )
                    }
                },
                enabled = selectedExercise != null && setsError == null && repsError == null && weightError == null && restTimeError == null
            ) {
                Text("Add Exercise")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GoalDialog(
    currentGoal: com.example.gymclock.data.GoalEntity?,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var reps by remember { mutableStateOf(currentGoal?.reps?.toString() ?: "10") }
    var weight by remember { mutableStateOf(currentGoal?.weight?.toString() ?: "50") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Goal") },
        text = {
            Column {
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Target Reps") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Target Weight (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        reps.toIntOrNull() ?: 10,
                        weight.toIntOrNull() ?: 50
                    )
                }
            ) {
                Text("Save Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getCurrentDay(): String {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val calendar = java.util.Calendar.getInstance()
    val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
    // Calendar.SUNDAY = 1, so shift to 0-based index for our list
    return days[(dayOfWeek + 5) % 7]
}
