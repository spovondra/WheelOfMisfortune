package com.kolecko.koleckonestestiv4

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: Task)

    @Query("SELECT * FROM Task")
    suspend fun getAllTasks(): List<Task>

    @Delete
    suspend fun deleteTask(task: Task)
}
