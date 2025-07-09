package com.example.gymclock.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "workout_log",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseId: Long,
    val sets: Int,
    val reps: Int,
    val weight: Float,
    val completedAt: Long, // timestamp
    val duration: Int? = null, // seconds
    val notes: String? = null,
    val isPersonalRecord: Boolean = false
)
