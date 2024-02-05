package com.misfortuneapp.wheelofmisfortune.model

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Rozhraní pro manipulaci s daty úkolů.
 */
interface TaskModel {
    /**
     * Suspend funkce pro získání všech úkolů.
     *
     * @return Seznam všech úkolů.
     */

    suspend fun getAllTasks(): List<Task>

    /**
     * Suspend funkce pro získání úkolu podle ID.
     *
     * @param taskId ID úkolu.
     * @return Úkol nebo null, pokud úkol není nalezen.
     */
    suspend fun getTaskById(taskId: Int): Task?

    /**
     * Suspend funkce pro odstranění úkolu.
     *
     * @param task Úkol k odstranění.
     */
    suspend fun removeTask(task: Task)

    /**
     * Suspend funkce pro vložení úkolu.
     *
     * @param task Úkol k vložení.
     */
    suspend fun insertTask(task: Task)

    /**
     * Suspend funkce pro aktualizaci úkolu.
     *
     * @param task Aktualizovaný úkol.
     */
    suspend fun updateTask(task: Task)

    /**
     * Suspend funkce pro vložení nové úlohy s parametry názvu a popisu.
     *
     * @param title Název nové úlohy.
     * @param description Popis nové úlohy.
     * @param priority Priorita nové úlohy.
     * @param iconResId ID ikony pro novou úlohu.
     */
    suspend fun addNewTask(
        title: String,
        description: String,
        priority: Int,
        iconResId: Int
    )

    /**
     * Suspend funkce pro získání úkolů podle stavu.
     *
     * @param taskState Stav úkolů, které mají být získány.
     * @return Seznam úkolů podle zadaného stavu.
     */
    suspend fun getTasksByState(taskState: TaskState): List<Task>

    /**
     * Suspend funkce pro vložení záznamu o čase (např. start a end čas).
     *
     * @param startTime Začátek časového záznamu.
     * @param endTime Konec časového záznamu.
     */
    suspend fun insertTimeRecord(startTime: Long, endTime: Long)

    /**
     * Suspend funkce pro získání posledního záznamu o čase.
     *
     * @return Poslední záznam o čase nebo nový záznam s nulovým začátkem a koncem, pokud neexistuje žádný záznam.
     */
    suspend fun getTimeRecord(): TimeRecord
}

/**
 * Implementace rozhraní TaskModel.
 *
 * @param context Kontext aplikace.
 */
class TaskModelImpl(context: Context) : TaskModel {
    // Získání přístupu k DAO pro úkoly
    private val taskDao = TaskDatabase.getDatabase(context).taskDao()
    private val timeRecordDao = TimeDatabase.getDatabase(context).timeRecordDao()

    /**
     * Suspend funkce pro získání všech úkolů.
     *
     * @return Seznam všech úkolů.
     */
    override suspend fun getAllTasks(): List<Task> = withContext(Dispatchers.IO) {
        return@withContext taskDao.getAllTasks()
    }

    /**
     * Suspend funkce pro získání úkolu podle ID.
     *
     * @param taskId ID úkolu.
     * @return Úkol nebo null, pokud úkol není nalezen.
     */
    override suspend fun getTaskById(taskId: Int): Task? = withContext(Dispatchers.IO) {
        return@withContext taskDao.getTaskById(taskId)
    }

    /**
     * Suspend funkce pro odstranění úkolu.
     *
     * @param task Úkol k odstranění.
     */
    override suspend fun removeTask(task: Task) {
        task.taskState = TaskState.DELETED
        updateTask(task)
    }

    /**
     * Suspend funkce pro vložení úkolu.
     *
     * @param task Úkol k vložení.
     */
    override suspend fun insertTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
    }

    /**
     * Suspend funkce pro aktualizaci úkolu.
     *
     * @param task Aktualizovaný úkol.
     */
    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    /**
     * Suspend funkce pro vložení nové úlohy s parametry názvu a popisu.
     *
     * @param title Název nové úlohy.
     * @param description Popis nové úlohy.
     * @param priority Priorita nové úlohy.
     * @param iconResId ID ikony pro novou úlohu.
     */
    override suspend fun addNewTask(
        title: String,
        description: String,
        priority: Int,
        iconResId: Int
    ) {
        val newTask = Task(
            title = title,
            description = description,
            priority = priority,
            iconResId = iconResId,
            taskState = TaskState.AVAILABLE
        )

        insertTask(newTask)
    }

    /**
     * Suspend funkce pro získání úkolů podle stavu.
     *
     * @param taskState Stav úkolů, které mají být získány.
     * @return Seznam úkolů podle zadaného stavu.
     */
    override suspend fun getTasksByState(taskState: TaskState): List<Task> {
        return taskDao.getTasksByState(taskState)
    }

    /**
     * Suspend funkce pro vložení záznamu o čase (např. start a end čas).
     *
     * @param startTime Začátek časového záznamu.
     * @param endTime Konec časového záznamu.
     */
    override suspend fun insertTimeRecord(startTime: Long, endTime: Long) {
        withContext(Dispatchers.IO) {
            // Vložení nového záznamu
            val timeRecord = TimeRecord(startTime = startTime, endTime = endTime)
            timeRecordDao.insert(timeRecord)
        }
    }

    /**
     * Suspend funkce pro získání posledního záznamu o čase.
     *
     * @return Poslední záznam o čase nebo nový záznam s nulovým začátkem a koncem, pokud neexistuje žádný záznam.
     */
    override suspend fun getTimeRecord(): TimeRecord {
        return withContext(Dispatchers.IO) {
            timeRecordDao.getLastTimeRecord() ?: TimeRecord(startTime = 0, endTime = 0)
        }
    }
}
