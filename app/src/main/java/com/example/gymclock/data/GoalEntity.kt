package com.example.gymclock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal")
data class GoalEntity(
    @PrimaryKey val day: String, // e.g. Monday
    val reps: Int,
    val weight: Int
)

