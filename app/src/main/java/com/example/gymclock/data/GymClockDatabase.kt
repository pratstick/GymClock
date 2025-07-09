package com.example.gymclock.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ScheduleEntity::class, 
        GoalEntity::class,
        ExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutLogEntity::class,
        PredefinedSplitEntity::class,
        SplitTemplateEntity::class
    ],
    version = 2
)
abstract class GymClockDatabase : RoomDatabase() {
    abstract fun gymClockDao(): GymClockDao

    companion object {
        @Volatile private var INSTANCE: GymClockDatabase? = null

        fun getInstance(context: Context): GymClockDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GymClockDatabase::class.java,
                    "gymclock.db"
                ).fallbackToDestructiveMigration() // For development
                .build().also { INSTANCE = it }
            }
    }
}
