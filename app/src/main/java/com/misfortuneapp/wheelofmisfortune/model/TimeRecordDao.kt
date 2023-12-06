package com.misfortuneapp.wheelofmisfortune.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// Rozhraní pro práci s databází
@Dao
interface TimeRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timeRecord: TimeRecord)

    @Query("DELETE FROM TimeRecord")
    suspend fun deleteAll()

    @Query("SELECT * FROM TimeRecord")
    suspend fun getAllTimeRecords(): List<TimeRecord>

    @Query("SELECT * FROM TimeRecord WHERE id = :timeId")
    suspend fun getTimeById(timeId: Int): TimeRecord?
}

