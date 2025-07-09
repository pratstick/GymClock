package com.example.gymclock.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GymClockDao {
    // Schedule
    @Query("SELECT * FROM schedule WHERE day = :day LIMIT 1")
    fun getScheduleForDay(day: String): Flow<ScheduleEntity?>

    @Query("SELECT * FROM schedule")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSchedule(schedule: ScheduleEntity)

    // Goal
    @Query("SELECT * FROM goal WHERE day = :day LIMIT 1")
    fun getGoalForDay(day: String): Flow<GoalEntity?>

    @Query("SELECT * FROM goal")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: GoalEntity)

    // Exercise
    @Query("SELECT * FROM exercise ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercise WHERE category = :category ORDER BY name ASC")
    fun getExercisesByCategory(category: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercise WHERE name LIKE :searchQuery ORDER BY name ASC")
    fun searchExercises(searchQuery: String): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    // Workout
    @Query("SELECT * FROM workout WHERE day = :day ORDER BY orderInWorkout ASC")
    fun getWorkoutsForDay(day: String): Flow<List<WorkoutEntity>>

    @Query("SELECT w.*, e.name as exerciseName, e.category, e.muscleGroup FROM workout w INNER JOIN exercise e ON w.exerciseId = e.id WHERE w.day = :day ORDER BY w.orderInWorkout ASC")
    fun getWorkoutsWithExerciseForDay(day: String): Flow<List<WorkoutWithExercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    // Workout Log
    @Query("SELECT * FROM workout_log WHERE exerciseId = :exerciseId ORDER BY completedAt DESC")
    fun getWorkoutLogsForExercise(exerciseId: Long): Flow<List<WorkoutLogEntity>>

    @Query("SELECT * FROM workout_log ORDER BY completedAt DESC LIMIT 50")
    fun getRecentWorkoutLogs(): Flow<List<WorkoutLogEntity>>

    @Insert
    suspend fun insertWorkoutLog(log: WorkoutLogEntity)

    // Predefined Splits
    @Query("SELECT * FROM predefined_split ORDER BY difficulty, name")
    fun getAllPredefinedSplits(): Flow<List<PredefinedSplitEntity>>

    @Query("SELECT * FROM split_template WHERE splitId = :splitId ORDER BY day, orderInDay")
    fun getSplitTemplate(splitId: String): Flow<List<SplitTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPredefinedSplit(split: PredefinedSplitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplitTemplates(templates: List<SplitTemplateEntity>)
}

data class WorkoutWithExercise(
    val id: Long,
    val exerciseId: Long,
    val day: String,
    val sets: Int,
    val reps: Int,
    val weight: Float,
    val restTimeSeconds: Int,
    val notes: String?,
    val orderInWorkout: Int,
    val isCompleted: Boolean,
    val completedAt: Long?,
    val exerciseName: String,
    val category: String,
    val muscleGroup: String
)
