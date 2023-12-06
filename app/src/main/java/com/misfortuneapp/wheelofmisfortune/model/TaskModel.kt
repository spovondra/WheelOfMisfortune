package com.misfortuneapp.wheelofmisfortune.model

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Rozhraní reprezentující model úkolů
interface TaskModel {
    suspend fun getAllTasks(): List<Task>
    suspend fun getTaskById(taskId: Int): Task?
    suspend fun removeTask(task: Task)
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun addNewTask(
        title: String,
        description: String,
        priority: Int,
        iconResId: Int,
        startTime: Long,
        endTime: Long
    )
    suspend fun getTasksByState(taskState: TaskState): List<Task>
}

// Implementace rozhraní TaskModel
class TaskModelImpl(context: Context) : TaskModel {
    // Získání přístupu k DAO pro úkoly
    private val taskDao = TaskDatabase.getDatabase(context).taskDao()

    // Metoda pro získání všech úkolů (implementace z rozhraní TaskModel)
    override suspend fun getAllTasks(): List<Task> = withContext(Dispatchers.IO) {
        return@withContext taskDao.getAllTasks()
    }

    // Metoda pro získání úkolu podle ID (implementace z rozhraní TaskModel)
    override suspend fun getTaskById(taskId: Int): Task? = withContext(Dispatchers.IO) {
        return@withContext taskDao.getTaskById(taskId)
    }

    // Metoda pro odstranění úkolu (implementace z rozhraní TaskModel)
    override suspend fun removeTask(task: Task) {
        taskDao.deleteTask(task)
    }

    // Metoda pro vložení úkolu (implementace z rozhraní TaskModel)
    override suspend fun insertTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
    }

    // Metoda pro aktualizaci úkolu (implementace z rozhraní TaskModel)
    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    // Nová metoda pro vložení nové úlohy s parametry názvu a popisu
    override suspend fun addNewTask(
        title: String,
        description: String,
        priority: Int,
        iconResId: Int,
        startTime: Long,
        endTime: Long
    ) {
        val lastTask = taskDao.getLastTask()
        val newDisplayId = lastTask?.displayId?.plus(1) ?: 1

        val newTask = Task(
            displayId = newDisplayId,
            title = title,
            description = description,
            priority = priority,
            iconResId = iconResId,
            startTime = startTime,
            taskState = TaskState.AVAILABLE,
            endTime = endTime
        )

        insertTask(newTask)
    }
    override suspend fun getTasksByState(taskState: TaskState): List<Task> {
        return taskDao.getTasksByState(taskState)
    }
}
