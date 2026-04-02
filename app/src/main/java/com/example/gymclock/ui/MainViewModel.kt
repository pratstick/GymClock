package com.example.gymclock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymclock.data.*
import com.example.gymclock.repository.GymClockRepository
import com.example.gymclock.timer.WorkoutTimer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
        viewModelScope.launch {
            repository.initializeDefaultData()
            loadData()
        }
    }

    private fun loadData() {
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
        viewModelScope.launch {
            repository.upsertSchedule(ScheduleEntity(day, plan))
        }
    }

    fun updateGoal(day: String, reps: Int, weight: Int) {
        viewModelScope.launch {
            repository.upsertGoal(GoalEntity(day, reps, weight))
        }
    }

    fun addWorkout(day: String, exerciseId: Long, sets: Int, reps: Int, weight: Float, restTime: Int) {
        val normDay = normalizeDay(day)
        viewModelScope.launch {
            // Use the target day's workout count for ordering, not today's count.
            val orderInWorkout = repository.getWorkoutsForDay(normDay).first().size
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
        }
    }

    fun completeWorkout(workout: WorkoutWithExercise) {
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

            // Log the completed workout for progress tracking
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

    fun deleteWorkout(workout: WorkoutWithExercise) {
        viewModelScope.launch {
            repository.deleteWorkout(
                WorkoutEntity(
                    id = workout.id,
                    exerciseId = workout.exerciseId,
                    day = workout.day,
                    sets = workout.sets,
                    reps = workout.reps,
                    weight = workout.weight,
                    restTimeSeconds = workout.restTimeSeconds,
                    notes = workout.notes,
                    orderInWorkout = workout.orderInWorkout,
                    isCompleted = workout.isCompleted,
                    completedAt = workout.completedAt
                )
            )
        }
    }

    fun applySplit(splitId: String) {
        viewModelScope.launch {
            repository.applySplit(splitId)
            // Room's reactive flows emit automatically after the insert/delete/update
            // operations inside applySplit, so an explicit loadData() call is not needed.
        }
    }

    fun startRestTimer(seconds: Int, onFinished: () -> Unit) {
        timer.startTimer(seconds, onFinished)
    }

    fun showError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun getWorkoutsForDay(day: String): StateFlow<List<WorkoutWithExercise>> {
        val normDay = normalizeDay(day)
        return repository.getWorkoutsForDay(normDay).stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )
    }

    fun getGoalForDay(day: String): StateFlow<GoalEntity?> {
        val normDay = normalizeDay(day)
        return repository.getGoalForDay(normDay).stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )
    }

    fun getScheduleForDay(day: String): StateFlow<ScheduleEntity?> {
        val normDay = normalizeDay(day)
        return repository.getScheduleForDay(normDay).stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )
    }

    fun addCustomExerciseAndWorkout(day: String, exercise: ExerciseEntity, sets: Int, reps: Int, weight: Float, restTime: Int) {
        val normDay = normalizeDay(day)
        viewModelScope.launch {
            val exerciseId = repository.insertExercise(exercise)
            // Use the target day's workout count for ordering, not today's count.
            val orderInWorkout = repository.getWorkoutsForDay(normDay).first().size
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
        }
    }

    /** Returns the most recent workout log entries for the progress screen. */
    fun getRecentWorkoutLogs(): StateFlow<List<WorkoutLogEntity>> {
        return repository.getRecentWorkoutLogs().stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )
    }
}

/** Factory that lets [ViewModelProvider] construct [MainViewModel] with its dependency. */
class MainViewModelFactory(private val repository: GymClockRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

