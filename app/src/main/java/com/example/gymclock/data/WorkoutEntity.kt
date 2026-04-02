package com.example.gymclock.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Index on exerciseId satisfies Room's FK index requirement; index on day
    // speeds up the per-day queries that power every screen in the app.
    indices = [
        Index(value = ["exerciseId"]),
        Index(value = ["day"])
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
