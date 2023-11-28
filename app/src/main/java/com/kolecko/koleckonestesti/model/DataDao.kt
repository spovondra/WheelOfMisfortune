package com.kolecko.koleckonestesti

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// Rozhraní pro přístup k databázi prostřednictvím Room
@Dao
interface DataDao {

    // Metoda pro vložení nových dat do databáze
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(dataEntity: DataEntity)

    // Metoda pro získání všech dat z databáze, seřazených podle dne
    @Query("SELECT * FROM data_table ORDER BY day ASC")
    suspend fun getAllData(): List<DataEntity>

    // Metoda pro získání dat z databáze podle zadaného data
    @Query("SELECT * FROM data_table WHERE day = :date LIMIT 1")
    suspend fun getDataByDate(date: Int): DataEntity?

    // Metoda pro získání formátovaných dat pro osu X grafu
    @Query("SELECT formattedDate FROM data_table")
    suspend fun getFormattedDates(): Array<String>

    // Metoda pro smazání všech dat z databáze
    @Query("DELETE FROM data_table")
    suspend fun deleteAllData()
}
