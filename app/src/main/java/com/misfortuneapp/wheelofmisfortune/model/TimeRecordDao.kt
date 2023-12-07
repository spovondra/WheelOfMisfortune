package com.misfortuneapp.wheelofmisfortune.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// Rozhraní pro práci s databází pro časový záznam
@Dao
interface TimeRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timeRecord: TimeRecord)  // Metoda pro vložení nového časového záznamu

    @Query("DELETE FROM TimeRecord")
    suspend fun deleteAll()  // Metoda pro smazání všech časových záznamů

    @Query("SELECT * FROM TimeRecord ORDER BY id DESC LIMIT 1")
    suspend fun getLastTimeRecord(): TimeRecord?  // Metoda pro získání posledního časového záznamu

    @Query("SELECT * FROM TimeRecord WHERE id = :timeId")
    suspend fun getTimeById(timeId: Int): TimeRecord?  // Metoda pro získání časového záznamu podle ID
}
