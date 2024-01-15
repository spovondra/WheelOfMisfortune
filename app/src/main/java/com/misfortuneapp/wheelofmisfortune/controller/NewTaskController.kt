package com.misfortuneapp.wheelofmisfortune.controller

import com.misfortuneapp.wheelofmisfortune.model.Task
import com.misfortuneapp.wheelofmisfortune.model.TaskModel
import com.misfortuneapp.wheelofmisfortune.model.TaskModelImpl
import com.misfortuneapp.wheelofmisfortune.view.NewTaskActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
class NewTaskControllerImpl(newTaskActivity: NewTaskActivity) : NewTakController {
    private val taskModel: TaskModel = TaskModelImpl(newTaskActivity)

    override suspend fun getAllTasks(): List<Task> = withContext(Dispatchers.IO) {
        taskModel.getAllTasks()
    }

    override suspend fun getTaskByDisplayId(id: Int): Task? = withContext(Dispatchers.IO) {
        taskModel.getTaskByDisplayId(id)
    }

    override suspend fun updateTask(task: Task) {
        taskModel.updateTask(task)
    }

    override suspend fun removeTask(task: Task) {
        taskModel.removeTask(task)
    }

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
