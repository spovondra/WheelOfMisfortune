package com.kolecko.koleckonestesti.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Abstraktní třída reprezentující databázi úkolů
@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {
    // Abstraktní metoda pro získání přístupu k DAO pro úkoly
    abstract fun taskDao(): TaskDao

    // Doprovodný objekt (companion object) umožňující přístup k databázi
    companion object {
        // Volatile proměnná pro správnou synchronizaci přístupu k databázi z více vláken
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        // Metoda pro získání instance databáze
        fun getDatabase(context: Context): TaskDatabase {
            // Pokud INSTANCE je null, provede se synchronizovaný blok k vytvoření nové instance
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
