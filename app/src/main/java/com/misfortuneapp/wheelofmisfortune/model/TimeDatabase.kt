package com.misfortuneapp.wheelofmisfortune.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TimeRecord::class], version = 1)
abstract class TimeDatabase : RoomDatabase() {

    abstract fun timeRecordDao(): TimeRecordDao

    companion object {
        @Volatile
        private var INSTANCE: TimeDatabase? = null

        fun getDatabase(context: Context): TimeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimeDatabase::class.java,
                    "time_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
