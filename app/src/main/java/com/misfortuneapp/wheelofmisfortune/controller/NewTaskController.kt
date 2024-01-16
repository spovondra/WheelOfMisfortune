package com.misfortuneapp.wheelofmisfortune.controller

import com.misfortuneapp.wheelofmisfortune.model.Task
import com.misfortuneapp.wheelofmisfortune.model.TaskModel
import com.misfortuneapp.wheelofmisfortune.model.TaskModelImpl
import com.misfortuneapp.wheelofmisfortune.view.NewTaskActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Rozhraní pro kontrolér nové úlohy, který definuje metody pro přidání, získání, aktualizaci a odebrání úkolů.
interface NewTakController {
    suspend fun addNewTask(
        displayId: Int,
        title: String,
        description: String,
        priority: Int,
        iconResId: Int,
        startTime: Long,
        endTime: Long
    )

    suspend fun getAllTasks(): List<Task>
    suspend fun getTaskByDisplayId(id: Int): Task?
    suspend fun updateTask(task: Task)
    suspend fun removeTask(task: Task)
}

// Implementace rozhraní NewTakController, která využívá TaskModel pro manipulaci s úkoly.
class NewTaskControllerImpl(newTaskActivity: NewTaskActivity) : NewTakController {
    private val taskModel: TaskModel = TaskModelImpl(newTaskActivity)

    // Suspendovaná metoda pro získání všech úkolů.
    override suspend fun getAllTasks(): List<Task> = withContext(Dispatchers.IO) {
        taskModel.getAllTasks()
    }

    // Suspendovaná metoda pro získání úkolu podle jeho zobrazeného ID.
    override suspend fun getTaskByDisplayId(id: Int): Task? = withContext(Dispatchers.IO) {
        taskModel.getTaskByDisplayId(id)
    }

    // Suspendovaná metoda pro aktualizaci úkolu.
    override suspend fun updateTask(task: Task) {
        taskModel.updateTask(task)
    }

    // Suspendovaná metoda pro odebrání úkolu.
    override suspend fun removeTask(task: Task) {
        taskModel.removeTask(task)
    }

    // Suspendovaná metoda pro přidání nového úkolu.
    override suspend fun addNewTask(
        displayId: Int,
        title: String,
        description: String,
        priority: Int,
        iconResId: Int,
        startTime: Long,
        endTime: Long
    ) {
        taskModel.addNewTask(displayId, title, description, priority, iconResId, startTime, endTime)
    }
}
