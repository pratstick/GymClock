package com.example.gymclock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // e.g., "Push", "Pull", "Legs"
    val muscleGroup: String, // e.g., "Chest", "Back", "Quadriceps"
    val description: String? = null,
    val isBodyweight: Boolean = false
)
