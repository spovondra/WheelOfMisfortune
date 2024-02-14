package com.usbapps.misfortunewheel.controller

import com.usbapps.misfortunewheel.model.Task
import com.usbapps.misfortunewheel.model.TaskModel
import com.usbapps.misfortunewheel.model.TaskModelImpl
import com.usbapps.misfortunewheel.view.NewTaskActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Rozhraní pro kontrolér nové úlohy, který definuje metody pro přidání, získání, aktualizaci a odebrání úkolů.
 */
interface NewTakController {
    /**
     * Suspendovaná metoda pro přidání nové úlohy.
     * @param title Název úlohy.
     * @param description Popis úlohy.
     * @param priority Priorita úlohy.
     * @param iconResId ID ikony úlohy.
     */
    suspend fun addNewTask(
        title: String,
        description: String,
        priority: Int,
        iconResId: Int
    )

    /**
     * Suspendovaná metoda pro získání všech úkolů.
     * @return Seznam všech úkolů.
     */
    suspend fun getAllTasks(): List<Task>

    /**
     * Suspendovaná metoda pro aktualizaci úkolu.
     * @param task Aktualizovaný úkol.
     */
    suspend fun updateTask(task: Task)

    /**
     * Suspendovaná metoda pro odebrání úkolu.
     * @param task Odebíraný úkol.
     */
    suspend fun removeTask(task: Task)

    /**
     * Suspendovaná metoda pro získání úkolu podle ID.
     * @param id ID úkolu.
     * @return [Task] nebo null, pokud úkol s daným ID neexistuje.
     */
    suspend fun getTaskById(id: Int): Task?
}

/**
 * Implementace rozhraní [NewTakController], která využívá [TaskModel] pro manipulaci s úkoly.
 */
class NewTaskControllerImpl(newTaskActivity: NewTaskActivity) : NewTakController {
    private val taskModel: TaskModel = TaskModelImpl(newTaskActivity)

    /**
     * Suspendovaná metoda pro získání všech úkolů.
     * @return Seznam všech úkolů.
     */
    override suspend fun getAllTasks(): List<Task> = withContext(Dispatchers.IO) {
        taskModel.getAllTasks()
    }

    /**
     * Suspendovaná metoda pro získání úkolu podle ID.
     * @param id ID úkolu.
     * @return [Task] nebo null, pokud úkol s daným ID neexistuje.
     */
    override suspend fun getTaskById(id: Int): Task? = withContext(Dispatchers.IO) {
        taskModel.getTaskById(id)
    }

    /**
     * Suspendovaná metoda pro aktualizaci úkolu.
     * @param task Aktualizovaný úkol.
     */
    override suspend fun updateTask(task: Task) {
        taskModel.updateTask(task)
    }

    /**
     * Suspendovaná metoda pro odebrání úkolu.
     * @param task Odebíraný úkol.
     */
    override suspend fun removeTask(task: Task) {
        taskModel.removeTask(task)
    }

    /**
     * Suspendovaná metoda pro přidání nové úlohy.
     * @param title Název úlohy.
     * @param description Popis úlohy.
     * @param priority Priorita úlohy.
     * @param iconResId ID ikony úlohy.
     */
    override suspend fun addNewTask(
        title: String,
        description: String,
        priority: Int,
        iconResId: Int
    ) {
        taskModel.addNewTask(title, description, priority, iconResId)
    }
}
