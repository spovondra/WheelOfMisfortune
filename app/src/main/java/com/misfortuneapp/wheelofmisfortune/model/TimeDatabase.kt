package com.misfortuneapp.wheelofmisfortune.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Definice databáze pro časové záznamy
@Database(entities = [TimeRecord::class], version = 1)
abstract class TimeDatabase : RoomDatabase() {

    // Abstraktní metoda pro získání přístupu k DAO pro časové záznamy
    abstract fun timeRecordDao(): TimeRecordDao

    // Společný přístup k instanci databáze (singleton pattern)
    companion object {
        @Volatile
        private var INSTANCE: TimeDatabase? = null

        // Metoda pro získání instance databáze
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
