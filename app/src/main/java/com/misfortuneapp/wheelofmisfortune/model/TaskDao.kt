package com.misfortuneapp.wheelofmisfortune.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

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

    // Metoda pro získání úkolu podle jména
    @Query("SELECT * FROM Task WHERE displayId = :displayId")
    suspend fun displayId(displayId: Int): Task?

    // Metoda pro aktualizaci úkolu v databázi
    @Update
    suspend fun updateTask(task: Task)

    // Metoda pro nastavení startTime a endTime pro úkol
    @Query("UPDATE Task SET startTime = :startTime, endTime = :endTime, taskState = :taskState WHERE id = :taskId")
    suspend fun setTaskTimeFrame(taskId: Int, startTime: Long, endTime: Long, taskState: TaskState)

    // Metoda pro získání posledního úkolu
    @Query("SELECT * FROM Task ORDER BY id DESC LIMIT 1")
    suspend fun getLastTask(): Task?

    @Query("SELECT * FROM Task WHERE taskState = :taskState")
    suspend fun getTasksByState(taskState: TaskState): List<Task>

}
