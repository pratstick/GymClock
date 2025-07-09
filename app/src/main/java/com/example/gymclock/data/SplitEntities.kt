package com.example.gymclock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "predefined_split")
data class PredefinedSplitEntity(
    @PrimaryKey val id: String, // e.g., "ppl", "starting_strength"
    val name: String, // e.g., "Push Pull Legs"
    val description: String,
    val source: String = "thefitness.wiki",
    val daysPerWeek: Int,
    val difficulty: String, // "Beginner", "Intermediate", "Advanced"
    val category: String // "Strength", "Hypertrophy", "Powerlifting"
)

@Entity(tableName = "split_template")
data class SplitTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val splitId: String,
    val day: String, // e.g., "Day1", "Day2" or "Monday", "Wednesday"
    val exerciseName: String,
    val sets: String, // e.g., "3", "3-4", "3x5"
    val reps: String, // e.g., "8-12", "5", "AMRAP"
    val weight: String? = null, // e.g., "bodyweight", "75% 1RM"
    val restTime: Int = 90, // seconds
    val orderInDay: Int = 0,
    val notes: String? = null
)
