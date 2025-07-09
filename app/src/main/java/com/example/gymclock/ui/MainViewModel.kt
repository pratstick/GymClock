package com.example.gymclock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymclock.data.*
import com.example.gymclock.repository.GymClockRepository
import com.example.gymclock.timer.WorkoutTimer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class MainUiState(
    val todaySchedule: ScheduleEntity? = null,
    val todayGoal: GoalEntity? = null,
    val todayWorkouts: List<WorkoutWithExercise> = emptyList(),
    val allExercises: List<ExerciseEntity> = emptyList(),
    val predefinedSplits: List<PredefinedSplitEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class MainViewModel(private val repository: GymClockRepository) : ViewModel() {
    // Use Calendar for compatibility with minSdkVersion 24
    private val today: String
        get() {
            val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            val calendar = java.util.Calendar.getInstance()
            val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
            // Calendar.SUNDAY = 1, so shift to 0-based index for our list
            return days[(dayOfWeek + 5) % 7]
        }

    val timer = WorkoutTimer(viewModelScope)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        android.util.Log.d("MainViewModel", "init called") // DEBUG
        viewModelScope.launch {
            repository.initializeDefaultData()
            loadData()
        }
    }

    private fun loadData() {
        android.util.Log.d("MainViewModel", "loadData called") // DEBUG
        viewModelScope.launch {
            combine(
                repository.getScheduleForDay(today),
                repository.getGoalForDay(today),
                repository.getWorkoutsForDay(today),
                repository.getAllExercises(),
                repository.getAllPredefinedSplits()
            ) { schedule, goal, workouts, exercises, splits ->
                MainUiState(
                    todaySchedule = schedule,
                    todayGoal = goal,
                    todayWorkouts = workouts,
                    allExercises = exercises,
                    predefinedSplits = splits,
                    isLoading = false
                )
            }.collect {
                _uiState.value = it
            }
        }
    }

    private fun normalizeDay(day: String): String {
        return day.lowercase().replaceFirstChar { it.uppercase() }
    }

    fun updateSchedule(day: String, plan: String) {
        android.util.Log.d("MainViewModel", "updateSchedule called with day = $day, plan = $plan") // DEBUG
        viewModelScope.launch {
            repository.upsertSchedule(ScheduleEntity(day, plan))
        }
    }

    fun updateGoal(day: String, reps: Int, weight: Int) {
        android.util.Log.d("MainViewModel", "updateGoal called with day = $day, reps = $reps, weight = $weight") // DEBUG
        viewModelScope.launch {
            repository.upsertGoal(GoalEntity(day, reps, weight))
        }
    }

    fun addWorkout(day: String, exerciseId: Long, sets: Int, reps: Int, weight: Float, restTime: Int) {
        val normDay = normalizeDay(day)
        android.util.Log.d("MainViewModel", "addWorkout called with day = $normDay, exerciseId = $exerciseId, sets = $sets, reps = $reps, weight = $weight, restTime = $restTime") // DEBUG
        viewModelScope.launch {
            val currentWorkouts = _uiState.value.todayWorkouts
            val orderInWorkout = currentWorkouts.size
            val workout = WorkoutEntity(
                exerciseId = exerciseId,
                day = normDay,
                sets = sets,
                reps = reps,
                weight = weight,
                restTimeSeconds = restTime,
                orderInWorkout = orderInWorkout
            )
            repository.insertWorkout(workout)
            loadData() // Refresh UI state after adding workout
        }
    }

    fun completeWorkout(workout: WorkoutWithExercise) {
        android.util.Log.d("MainViewModel", "completeWorkout called for workoutId = ${workout.id}") // DEBUG
        viewModelScope.launch {
            val updatedWorkout = WorkoutEntity(
                id = workout.id,
                exerciseId = workout.exerciseId,
                day = workout.day,
                sets = workout.sets,
                reps = workout.reps,
                weight = workout.weight,
                restTimeSeconds = workout.restTimeSeconds,
                notes = workout.notes,
                orderInWorkout = workout.orderInWorkout,
                isCompleted = true,
                completedAt = System.currentTimeMillis()
            )
            repository.updateWorkout(updatedWorkout)

            // Log the workout
            val log = WorkoutLogEntity(
                exerciseId = workout.exerciseId,
                sets = workout.sets,
                reps = workout.reps,
                weight = workout.weight,
                completedAt = System.currentTimeMillis()
            )
            repository.insertWorkoutLog(log)
        }
    }

    fun applySplit(splitId: String) {
        android.util.Log.d("MainViewModel", "applySplit called with splitId = $splitId") // DEBUG
        viewModelScope.launch {
            repository.applySplit(splitId)
            loadData() // Refresh UI after applying split
        }
    }

    fun startRestTimer(seconds: Int, onFinished: () -> Unit) {
        android.util.Log.d("MainViewModel", "startRestTimer called with seconds = $seconds") // DEBUG
        timer.startTimer(seconds, onFinished)
    }

    fun showError(message: String) {
        android.util.Log.d("MainViewModel", "showError called with message = $message") // DEBUG
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    fun clearError() {
        android.util.Log.d("MainViewModel", "clearError called") // DEBUG
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun getWorkoutsForDay(day: String): StateFlow<List<WorkoutWithExercise>> {
        val normDay = normalizeDay(day)
        android.util.Log.d("MainViewModel", "getWorkoutsForDay called with day = $normDay") // DEBUG
        return repository.getWorkoutsForDay(normDay).stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )
    }

    fun getGoalForDay(day: String): StateFlow<GoalEntity?> {
        val normDay = normalizeDay(day)
        android.util.Log.d("MainViewModel", "getGoalForDay called with day = $normDay") // DEBUG
        return repository.getGoalForDay(normDay).stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )
    }

    fun getScheduleForDay(day: String): StateFlow<ScheduleEntity?> {
        val normDay = normalizeDay(day)
        android.util.Log.d("MainViewModel", "getScheduleForDay called with day = $normDay") // DEBUG
        return repository.getScheduleForDay(normDay).stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )
    }

    fun addCustomExerciseAndWorkout(day: String, exercise: ExerciseEntity, sets: Int, reps: Int, weight: Float, restTime: Int) {
        val normDay = normalizeDay(day)
        android.util.Log.d("MainViewModel", "addCustomExerciseAndWorkout called with day = $normDay, exercise = ${exercise.name}, sets = $sets, reps = $reps, weight = $weight, restTime = $restTime") // DEBUG
        viewModelScope.launch {
            val exerciseId = repository.insertExercise(exercise)
            val currentWorkouts = _uiState.value.todayWorkouts
            val orderInWorkout = currentWorkouts.size
            val workout = WorkoutEntity(
                exerciseId = exerciseId,
                day = normDay,
                sets = sets,
                reps = reps,
                weight = weight,
                restTimeSeconds = restTime,
                orderInWorkout = orderInWorkout
            )
            repository.insertWorkout(workout)
            loadData()
        }
    }
}
