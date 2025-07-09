package com.example.gymclock.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "workout",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseId: Long,
    val day: String, // e.g., "Monday"
    val sets: Int,
    val reps: Int,
    val weight: Float = 0f,
    val restTimeSeconds: Int = 60,
    val notes: String? = null,
    val orderInWorkout: Int = 0,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null // timestamp
)
