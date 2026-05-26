package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Track::class, DjsHistory::class, GoalMilestone::class], version = 1, exportSchema = false)
abstract class DjDatabase : RoomDatabase() {
    abstract fun djDao(): DjDao

    companion object {
        @Volatile
        private var INSTANCE: DjDatabase? = null

        fun getDatabase(context: Context): DjDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DjDatabase::class.java,
                    "dj_deck_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
