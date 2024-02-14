package com.usbapps.misfortunewheel.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Tato třída představuje Room Database pro časové záznamy v aplikaci.
 */
@Database(entities = [TimeRecord::class], version = 1, exportSchema = false)
abstract class TimeDatabase : RoomDatabase() {

    /**
     * Abstraktní metoda pro získání přístupu k DAO pro časové záznamy.
     *
     * @return Instance [TimeRecordDao] pro provádění operací s časovými záznamy v databázi.
     */
    abstract fun timeRecordDao(): TimeRecordDao

    /**
     * Společný přístup k instanci databáze (singleton pattern).
     */
    companion object {
        // Volatile instance databáze pro zajištění správné synchronizace
        @Volatile
        private var INSTANCE: TimeDatabase? = null

        /**
         * Metoda pro získání instance databáze.
         *
         * @param context Kontext aplikace.
         * @return Instance [TimeDatabase] databáze pro časové záznamy.
         */
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
