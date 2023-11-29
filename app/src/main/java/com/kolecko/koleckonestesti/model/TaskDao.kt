package com.kolecko.koleckonestesti.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

// Rozhraní pro přístup k datům úkolů v databázi
@Dao
interface TaskDao {
    // Metoda pro vložení úkolu do databáze
    @Insert
    suspend fun insertTask(task: Task)

    // Metoda pro získání všech úkolů uložených v databázi
    @Query("SELECT * FROM Task")
    suspend fun getAllTasks(): List<Task>

    // Metoda pro smazání úkolu z databáze
    @Delete
    suspend fun deleteTask(task: Task)

    // Metoda pro získání úkolu podle ID
    @Query("SELECT * FROM Task WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

}

