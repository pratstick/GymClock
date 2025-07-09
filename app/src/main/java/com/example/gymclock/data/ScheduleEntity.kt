package com.example.gymclock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule")
data class ScheduleEntity(
    @PrimaryKey val day: String, // e.g. Monday
    val plan: String // e.g. Push, Pull, Rest
)

