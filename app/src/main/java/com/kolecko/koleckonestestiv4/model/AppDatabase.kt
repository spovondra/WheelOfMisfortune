package com.kolecko.koleckonestestiv4

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Tato třída představuje Room Database v aplikaci
@Database(entities = [DataEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Abstraktní metoda pro získání přístupu k rozhraní pro práci s daty
    abstract fun dataDao(): DataDao

    companion object {
        // Volatile instance databáze pro zajištění správné synchronizace
        @Volatile
        private var instance: AppDatabase? = null

        // Metoda pro získání instance databáze
        fun getInstance(context: Context): AppDatabase {
            // Pokud instance neexistuje, vytvoříme ji s použitím synchronizace
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        // Metoda pro vytvoření instance databáze
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            )
                .fallbackToDestructiveMigration() // Při aktualizaci databáze odstraní všechna data
                .build()
        }
    }
}
