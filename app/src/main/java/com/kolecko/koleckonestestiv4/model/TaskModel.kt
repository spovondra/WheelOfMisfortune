package com.kolecko.koleckonestestiv4

import android.content.Context
import com.kolecko.koleckonestestiv4.model.Task
import com.kolecko.koleckonestestiv4.model.TaskDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface TaskModel {
    suspend fun getAllTasks(): List<Task>
    suspend fun removeTask(task: Task)
    suspend fun insertTask(task: Task)
    suspend fun addNewTask(title: String, description: String)
}

class TaskModelImpl(private val context: Context) : TaskModel {
    private val taskDao = TaskDatabase.getDatabase(context).taskDao()

    // Function to insert initial tasks
    private suspend fun insertInitialTasks() {
        /*
        val initialTasks = listOf(
            Task("Task 1", "Description 1"),
            Task("Task 2", "Description 2"),
            // Add more tasks as needed
        )

        initialTasks.forEach { taskDao.insertTask(it) }

         */

    }
    init {
        // Call the function to insert initial tasks when TaskModelImpl is created
        GlobalScope.launch(Dispatchers.IO) {
            insertInitialTasks()
        }
    }

    override suspend fun getAllTasks(): List<Task> = withContext(Dispatchers.IO) {
        return@withContext taskDao.getAllTasks()
    }

    override suspend fun removeTask(task: Task) {
        taskDao.deleteTask(task)
    }

    override suspend fun insertTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
    }

    // Nová metoda pro vložení nové úlohy
    override suspend fun addNewTask(title: String, description: String) {
        val newTask = Task(title = title, description = description)
        insertTask(newTask)
    }
}
