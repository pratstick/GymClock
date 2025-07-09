package com.example.gymclock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymclock.data.*
import com.example.gymclock.repository.GymClockRepository
import com.example.gymclock.timer.WorkoutTimer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

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
    private val today = LocalDate.now().dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }

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

    fun addWorkout(exerciseId: Long, sets: Int, reps: Int, weight: Float, restTime: Int) {
        viewModelScope.launch {
            val currentWorkouts = _uiState.value.todayWorkouts
            val orderInWorkout = currentWorkouts.size
            val workout = WorkoutEntity(
                exerciseId = exerciseId,
                day = today,
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
        viewModelScope.launch {
            repository.applySplitToSchedule(splitId)
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
}
