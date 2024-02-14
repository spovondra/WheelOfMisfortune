package com.usbapps.misfortunewheel.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Rozhraní pro práci s databází pro časový záznam.
 */
@Dao
interface TimeRecordDao {
    /**
     * Metoda pro vložení nového časového záznamu do databáze.
     *
     * @param timeRecord Časový záznam k vložení.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timeRecord: TimeRecord)

    /**
     * Metoda pro smazání všech časových záznamů z databáze.
     */
    @Query("DELETE FROM TimeRecord")
    suspend fun deleteAll()

    /**
     * Metoda pro získání posledního časového záznamu uloženého v databázi.
     *
     * @return Poslední časový záznam nebo null, pokud nejsou žádné záznamy.
     */
    @Query("SELECT * FROM TimeRecord ORDER BY id DESC LIMIT 1")
    suspend fun getLastTimeRecord(): TimeRecord?

    /**
     * Metoda pro získání časového záznamu z databáze podle jeho ID.
     *
     * @param timeId Identifikační číslo časového záznamu.
     * @return Časový záznam podle ID nebo null, pokud neexistuje.
     */
    @Query("SELECT * FROM TimeRecord WHERE id = :timeId")
    suspend fun getTimeById(timeId: Int): TimeRecord?
}
