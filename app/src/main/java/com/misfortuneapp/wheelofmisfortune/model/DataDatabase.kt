package com.misfortuneapp.wheelofmisfortune.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Tato třída představuje Room Database v aplikaci pro ukládání datových entit [DataEntity].
 */
@Database(entities = [DataEntity::class], version = 4, exportSchema = false)
abstract class DataDatabase : RoomDatabase() {

    /**
     * Abstraktní metoda pro získání přístupu k rozhraní pro práci s daty.
     *
     * @return Instance [DataDao] pro provádění operací s daty v databázi.
     */
    abstract fun dataDao(): DataDao

    /**
     * Doprovodný objekt (companion object) umožňující přístup k databázi.
     */
    companion object {
        // Volatile instance databáze pro zajištění správné synchronizace
        @Volatile
        private var instance: DataDatabase? = null

        /**
         * Metoda pro získání instance databáze.
         *
         * @param context Kontext aplikace.
         * @return Instance [DataDatabase] databáze pro ukládání dat.
         */
        fun getInstance(context: Context): DataDatabase {
            // Pokud instance neexistuje, vytvoříme ji s použitím synchronizace
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        /**
         * Metoda pro vytvoření instance databáze.
         *
         * @param context Kontext aplikace.
         * @return Nová instance [DataDatabase] databáze.
         */
        private fun buildDatabase(context: Context): DataDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                DataDatabase::class.java,
                "data_database"
            )
                .fallbackToDestructiveMigration() // Při aktualizaci databáze odstraní všechna data
                .build()
        }
    }
}
