package com.example.gymclock.repository

import com.example.gymclock.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GymClockRepository(private val dao: GymClockDao) {

    // Schedule operations
    fun getScheduleForDay(day: String): Flow<ScheduleEntity?> = dao.getScheduleForDay(day)
    suspend fun upsertSchedule(schedule: ScheduleEntity) = dao.upsertSchedule(schedule)

    // Goal operations
    fun getGoalForDay(day: String): Flow<GoalEntity?> = dao.getGoalForDay(day)
    suspend fun upsertGoal(goal: GoalEntity) = dao.upsertGoal(goal)

    // Exercise operations
    fun getAllExercises(): Flow<List<ExerciseEntity>> = dao.getAllExercises()
    fun searchExercises(query: String): Flow<List<ExerciseEntity>> = dao.searchExercises("%$query%")
    suspend fun insertExercise(exercise: ExerciseEntity) = dao.insertExercise(exercise)

    // Workout operations
    fun getWorkoutsForDay(day: String): Flow<List<WorkoutWithExercise>> = dao.getWorkoutsWithExerciseForDay(day.lowercase())
    suspend fun insertWorkout(workout: WorkoutEntity) = dao.insertWorkout(workout)
    suspend fun updateWorkout(workout: WorkoutEntity) = dao.updateWorkout(workout)
    suspend fun deleteWorkout(workout: WorkoutEntity) = dao.deleteWorkout(workout)

    // Workout log operations
    fun getWorkoutLogsForExercise(exerciseId: Long): Flow<List<WorkoutLogEntity>> = dao.getWorkoutLogsForExercise(exerciseId)
    suspend fun insertWorkoutLog(log: WorkoutLogEntity) = dao.insertWorkoutLog(log)

    // Predefined splits
    fun getAllPredefinedSplits(): Flow<List<PredefinedSplitEntity>> = dao.getAllPredefinedSplits()
    fun getSplitTemplate(splitId: String): Flow<List<SplitTemplateEntity>> = dao.getSplitTemplate(splitId)

    suspend fun initializeDefaultData() {
        // Check if data already exists
        val exercises = dao.getAllExercises().first()
        if (exercises.isEmpty()) {
            initializeExercises()
            initializePredefinedSplits()
        }
    }

    private suspend fun initializeExercises() {
        val exercises = listOf(
            // Push exercises
            ExerciseEntity(name = "Bench Press", category = "Push", muscleGroup = "Chest"),
            ExerciseEntity(name = "Overhead Press", category = "Push", muscleGroup = "Shoulders"),
            ExerciseEntity(name = "Incline Dumbbell Press", category = "Push", muscleGroup = "Chest"),
            ExerciseEntity(name = "Dips", category = "Push", muscleGroup = "Chest", isBodyweight = true),
            ExerciseEntity(name = "Push-ups", category = "Push", muscleGroup = "Chest", isBodyweight = true),
            ExerciseEntity(name = "Lateral Raises", category = "Push", muscleGroup = "Shoulders"),
            ExerciseEntity(name = "Tricep Dips", category = "Push", muscleGroup = "Triceps"),
            ExerciseEntity(name = "Close-Grip Bench Press", category = "Push", muscleGroup = "Triceps"),

            // Pull exercises
            ExerciseEntity(name = "Deadlift", category = "Pull", muscleGroup = "Back"),
            ExerciseEntity(name = "Pull-ups", category = "Pull", muscleGroup = "Back", isBodyweight = true),
            ExerciseEntity(name = "Barbell Rows", category = "Pull", muscleGroup = "Back"),
            ExerciseEntity(name = "Lat Pulldowns", category = "Pull", muscleGroup = "Back"),
            ExerciseEntity(name = "Face Pulls", category = "Pull", muscleGroup = "Rear Delts"),
            ExerciseEntity(name = "Barbell Curls", category = "Pull", muscleGroup = "Biceps"),
            ExerciseEntity(name = "Hammer Curls", category = "Pull", muscleGroup = "Biceps"),
            ExerciseEntity(name = "Chin-ups", category = "Pull", muscleGroup = "Back", isBodyweight = true),

            // Legs exercises
            ExerciseEntity(name = "Squats", category = "Legs", muscleGroup = "Quadriceps"),
            ExerciseEntity(name = "Romanian Deadlift", category = "Legs", muscleGroup = "Hamstrings"),
            ExerciseEntity(name = "Leg Press", category = "Legs", muscleGroup = "Quadriceps"),
            ExerciseEntity(name = "Leg Curls", category = "Legs", muscleGroup = "Hamstrings"),
            ExerciseEntity(name = "Calf Raises", category = "Legs", muscleGroup = "Calves"),
            ExerciseEntity(name = "Lunges", category = "Legs", muscleGroup = "Quadriceps"),
            ExerciseEntity(name = "Bulgarian Split Squats", category = "Legs", muscleGroup = "Quadriceps"),
            ExerciseEntity(name = "Leg Extensions", category = "Legs", muscleGroup = "Quadriceps"),

            // Full body
            ExerciseEntity(name = "Burpees", category = "Full Body", muscleGroup = "Full Body", isBodyweight = true),
            ExerciseEntity(name = "Mountain Climbers", category = "Full Body", muscleGroup = "Full Body", isBodyweight = true),
            ExerciseEntity(name = "Planks", category = "Core", muscleGroup = "Core", isBodyweight = true),
            ExerciseEntity(name = "Russian Twists", category = "Core", muscleGroup = "Core", isBodyweight = true),

            // Arms
            ExerciseEntity(name = "Tricep Pushdown", category = "Push", muscleGroup = "Triceps"),
            ExerciseEntity(name = "Skullcrushers", category = "Push", muscleGroup = "Triceps"),
            ExerciseEntity(name = "Preacher Curl", category = "Pull", muscleGroup = "Biceps"),
            ExerciseEntity(name = "Concentration Curl", category = "Pull", muscleGroup = "Biceps"),
            ExerciseEntity(name = "Cable Curl", category = "Pull", muscleGroup = "Biceps"),
            ExerciseEntity(name = "Reverse Curl", category = "Pull", muscleGroup = "Forearms"),
            ExerciseEntity(name = "Wrist Curl", category = "Pull", muscleGroup = "Forearms"),

            // Core
            ExerciseEntity(name = "Crunches", category = "Core", muscleGroup = "Abs", isBodyweight = true),
            ExerciseEntity(name = "Hanging Leg Raise", category = "Core", muscleGroup = "Abs", isBodyweight = true),
            ExerciseEntity(name = "Cable Crunch", category = "Core", muscleGroup = "Abs"),
            ExerciseEntity(name = "Ab Wheel Rollout", category = "Core", muscleGroup = "Abs"),
            ExerciseEntity(name = "Bicycle Crunch", category = "Core", muscleGroup = "Abs", isBodyweight = true),

            // Machines/Other
            ExerciseEntity(name = "Seated Row Machine", category = "Pull", muscleGroup = "Back"),
            ExerciseEntity(name = "Chest Fly Machine", category = "Push", muscleGroup = "Chest"),
            ExerciseEntity(name = "Leg Adduction Machine", category = "Legs", muscleGroup = "Adductors"),
            ExerciseEntity(name = "Leg Abduction Machine", category = "Legs", muscleGroup = "Abductors"),
            ExerciseEntity(name = "Smith Machine Squat", category = "Legs", muscleGroup = "Quadriceps"),
            ExerciseEntity(name = "Hack Squat", category = "Legs", muscleGroup = "Quadriceps"),
            ExerciseEntity(name = "Hip Thrust", category = "Legs", muscleGroup = "Glutes"),
            ExerciseEntity(name = "Glute Bridge", category = "Legs", muscleGroup = "Glutes", isBodyweight = true)
        )
        dao.insertExercises(exercises)
    }

    private suspend fun initializePredefinedSplits() {
        // Push Pull Legs (PPL)
        val pplSplit = PredefinedSplitEntity(
            id = "ppl",
            name = "Push Pull Legs",
            description = "Popular 6-day split focusing on movement patterns",
            daysPerWeek = 6,
            difficulty = "Intermediate",
            category = "Hypertrophy"
        )

        val pplTemplates = listOf(
            // Push Day
            SplitTemplateEntity(splitId = "ppl", day = "Push", exerciseName = "Bench Press", sets = "4", reps = "6-8", orderInDay = 1, restTime = 180),
            SplitTemplateEntity(splitId = "ppl", day = "Push", exerciseName = "Overhead Press", sets = "3", reps = "8-10", orderInDay = 2, restTime = 120),
            SplitTemplateEntity(splitId = "ppl", day = "Push", exerciseName = "Incline Dumbbell Press", sets = "3", reps = "8-12", orderInDay = 3, restTime = 90),
            SplitTemplateEntity(splitId = "ppl", day = "Push", exerciseName = "Lateral Raises", sets = "3", reps = "12-15", orderInDay = 4, restTime = 60),
            SplitTemplateEntity(splitId = "ppl", day = "Push", exerciseName = "Tricep Dips", sets = "3", reps = "10-12", orderInDay = 5, restTime = 60),

            // Pull Day
            SplitTemplateEntity(splitId = "ppl", day = "Pull", exerciseName = "Deadlift", sets = "3", reps = "5", orderInDay = 1, restTime = 180),
            SplitTemplateEntity(splitId = "ppl", day = "Pull", exerciseName = "Pull-ups", sets = "3", reps = "8-12", orderInDay = 2, restTime = 120),
            SplitTemplateEntity(splitId = "ppl", day = "Pull", exerciseName = "Barbell Rows", sets = "3", reps = "8-10", orderInDay = 3, restTime = 90),
            SplitTemplateEntity(splitId = "ppl", day = "Pull", exerciseName = "Face Pulls", sets = "3", reps = "15-20", orderInDay = 4, restTime = 60),
            SplitTemplateEntity(splitId = "ppl", day = "Pull", exerciseName = "Barbell Curls", sets = "3", reps = "10-12", orderInDay = 5, restTime = 60),

            // Legs Day
            SplitTemplateEntity(splitId = "ppl", day = "Legs", exerciseName = "Squats", sets = "4", reps = "6-8", orderInDay = 1, restTime = 180),
            SplitTemplateEntity(splitId = "ppl", day = "Legs", exerciseName = "Romanian Deadlift", sets = "3", reps = "8-10", orderInDay = 2, restTime = 120),
            SplitTemplateEntity(splitId = "ppl", day = "Legs", exerciseName = "Leg Press", sets = "3", reps = "12-15", orderInDay = 3, restTime = 90),
            SplitTemplateEntity(splitId = "ppl", day = "Legs", exerciseName = "Leg Curls", sets = "3", reps = "10-12", orderInDay = 4, restTime = 60),
            SplitTemplateEntity(splitId = "ppl", day = "Legs", exerciseName = "Calf Raises", sets = "4", reps = "15-20", orderInDay = 5, restTime = 45)
        )

        // Upper Lower Split
        val upperLowerSplit = PredefinedSplitEntity(
            id = "upper_lower",
            name = "Upper Lower",
            description = "4-day split alternating upper and lower body",
            daysPerWeek = 4,
            difficulty = "Beginner",
            category = "Strength"
        )

        val upperLowerTemplates = listOf(
            // Upper Day
            SplitTemplateEntity(splitId = "upper_lower", day = "Upper", exerciseName = "Bench Press", sets = "3", reps = "8-10", orderInDay = 1, restTime = 120),
            SplitTemplateEntity(splitId = "upper_lower", day = "Upper", exerciseName = "Barbell Rows", sets = "3", reps = "8-10", orderInDay = 2, restTime = 120),
            SplitTemplateEntity(splitId = "upper_lower", day = "Upper", exerciseName = "Overhead Press", sets = "3", reps = "8-10", orderInDay = 3, restTime = 90),
            SplitTemplateEntity(splitId = "upper_lower", day = "Upper", exerciseName = "Pull-ups", sets = "3", reps = "8-12", orderInDay = 4, restTime = 90),

            // Lower Day
            SplitTemplateEntity(splitId = "upper_lower", day = "Lower", exerciseName = "Squats", sets = "3", reps = "8-10", orderInDay = 1, restTime = 120),
            SplitTemplateEntity(splitId = "upper_lower", day = "Lower", exerciseName = "Romanian Deadlift", sets = "3", reps = "8-10", orderInDay = 2, restTime = 120),
            SplitTemplateEntity(splitId = "upper_lower", day = "Lower", exerciseName = "Leg Press", sets = "3", reps = "12-15", orderInDay = 3, restTime = 90),
            SplitTemplateEntity(splitId = "upper_lower", day = "Lower", exerciseName = "Calf Raises", sets = "3", reps = "15-20", orderInDay = 4, restTime = 60)
        )

        dao.insertPredefinedSplit(pplSplit)
        dao.insertSplitTemplates(pplTemplates)
        dao.insertPredefinedSplit(upperLowerSplit)
        dao.insertSplitTemplates(upperLowerTemplates)
    }

    suspend fun applySplitToSchedule(splitId: String) {
        val templates = dao.getSplitTemplate(splitId).first()
        val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        when (splitId) {
            "ppl" -> {
                val pplSchedule = mapOf(
                    "Monday" to "Push",
                    "Tuesday" to "Pull",
                    "Wednesday" to "Legs",
                    "Thursday" to "Push",
                    "Friday" to "Pull",
                    "Saturday" to "Legs",
                    "Sunday" to "Rest"
                )
                pplSchedule.forEach { (day, plan) ->
                    dao.upsertSchedule(ScheduleEntity(day, plan))
                }
            }
            "upper_lower" -> {
                val ulSchedule = mapOf(
                    "Monday" to "Upper",
                    "Tuesday" to "Lower",
                    "Wednesday" to "Rest",
                    "Thursday" to "Upper",
                    "Friday" to "Lower",
                    "Saturday" to "Rest",
                    "Sunday" to "Rest"
                )
                ulSchedule.forEach { (day, plan) ->
                    dao.upsertSchedule(ScheduleEntity(day, plan))
                }
            }
        }
    }

    suspend fun getExerciseByName(name: String): ExerciseEntity? = dao.getExerciseByName(name)

    suspend fun applySplit(splitId: String) {
        // 1. Get all split templates for this split
        val templates = dao.getSplitTemplate(splitId).first()
        android.util.Log.d("GymClockRepository", "applySplit: templates size = ${templates.size}")
        if (templates.isEmpty()) {
            android.util.Log.w("GymClockRepository", "applySplit: No templates found for splitId = $splitId")
            return
        }
        // 2. Delete all existing workouts for the week
        val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        for (day in daysOfWeek) {
            val workouts = dao.getWorkoutsWithExerciseForDay(day).first()
            for (workout in workouts) {
                dao.deleteWorkout(WorkoutEntity(
                    id = workout.id,
                    day = workout.day,
                    exerciseId = workout.exerciseId,
                    sets = workout.sets,
                    reps = workout.reps,
                    weight = workout.weight,
                    restTimeSeconds = workout.restTimeSeconds,
                    isCompleted = workout.isCompleted,
                    orderInWorkout = workout.orderInWorkout,
                    notes = workout.notes,
                    completedAt = workout.completedAt
                ))
            }
        }
        // 3. For each template, find the exercise and insert a new workout for the correct day
        var insertedCount = 0
        for (template in templates) {
            val exercise = getExerciseByName(template.exerciseName)
            if (exercise != null) {
                val day = template.day
                val sets = template.sets.toIntOrNull() ?: 3
                val reps = template.reps.split('-').firstOrNull()?.toIntOrNull() ?: 10
                val workout = WorkoutEntity(
                    id = 0L,
                    day = day,
                    exerciseId = exercise.id,
                    sets = sets,
                    reps = reps,
                    weight = if (exercise.isBodyweight == true) 0f else 50f,
                    restTimeSeconds = template.restTime,
                    isCompleted = false,
                    orderInWorkout = template.orderInDay,
                    notes = null,
                    completedAt = null
                )
                dao.insertWorkout(workout)
                insertedCount++
            } else {
                android.util.Log.w("GymClockRepository", "applySplit: Exercise not found for name = ${template.exerciseName}")
            }
        }
        android.util.Log.d("GymClockRepository", "applySplit: Inserted $insertedCount workouts for splitId = $splitId")
    }
}
