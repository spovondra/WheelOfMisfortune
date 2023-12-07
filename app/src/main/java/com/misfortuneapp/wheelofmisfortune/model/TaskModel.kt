package com.misfortuneapp.wheelofmisfortune.model

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface TaskModel {
    // Suspend funkce pro získání všech úkolů
    suspend fun getAllTasks(): List<Task>

    // Suspend funkce pro získání úkolu podle ID
    suspend fun getTaskById(taskId: Int): Task?

    // Suspend funkce pro odstranění úkolu
    suspend fun removeTask(task: Task)

    // Suspend funkce pro vložení úkolu
    suspend fun insertTask(task: Task)

    // Suspend funkce pro aktualizaci úkolu
    suspend fun updateTask(task: Task)

    // Suspend funkce pro vložení nové úlohy s parametry názvu a popisu
    suspend fun addNewTask(
        title: String,
        description: String,
        priority: Int,
        iconResId: Int,
        startTime: Long,
        endTime: Long
    )

    // Suspend funkce pro získání úkolů podle stavu
    suspend fun getTasksByState(taskState: TaskState): List<Task>

    // Suspend funkce pro vložení záznamu o čase (např. start a end čas)
    suspend fun insertTimeRecord(startTime: Long, endTime: Long)

    // Suspend funkce pro získání posledního záznamu o čase
    suspend fun getTimeRecord(): TimeRecord
}

// Implementace rozhraní TaskModel
class TaskModelImpl(context: Context) : TaskModel {
    // Získání přístupu k DAO pro úkoly
    private val taskDao = TaskDatabase.getDatabase(context).taskDao()
    private val timeRecordDao = TimeDatabase.getDatabase(context).timeRecordDao()

    // Suspend funkce pro získání všech úkolů
    override suspend fun getAllTasks(): List<Task> = withContext(Dispatchers.IO) {
        return@withContext taskDao.getAllTasks()
    }

    // Suspend funkce pro získání úkolu podle ID
    override suspend fun getTaskById(taskId: Int): Task? = withContext(Dispatchers.IO) {
        return@withContext taskDao.getTaskById(taskId)
    }

    // Suspend funkce pro odstranění úkolu
    override suspend fun removeTask(task: Task) {
        taskDao.deleteTask(task)
    }

    // Suspend funkce pro vložení úkolu
    override suspend fun insertTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
    }

    // Suspend funkce pro aktualizaci úkolu
    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    // Nová suspend funkce pro vložení nové úlohy s parametry názvu a popisu
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

    // Suspend funkce pro získání úkolů podle stavu
    override suspend fun getTasksByState(taskState: TaskState): List<Task> {
        return taskDao.getTasksByState(taskState)
    }

    // Suspend funkce pro vložení záznamu o čase (např. start a end čas) (imeRecord)
    override suspend fun insertTimeRecord(startTime: Long, endTime: Long) {
        withContext(Dispatchers.IO) {
            // Vložení nového záznamu
            val timeRecord = TimeRecord(startTime = startTime, endTime = endTime)
            timeRecordDao.insert(timeRecord)
        }
    }

    // Suspend funkce pro získání posledního záznamu o čase (imeRecord)
    override suspend fun getTimeRecord(): TimeRecord {
        return withContext(Dispatchers.IO) {
            timeRecordDao.getLastTimeRecord()!!
        }
    }
}
