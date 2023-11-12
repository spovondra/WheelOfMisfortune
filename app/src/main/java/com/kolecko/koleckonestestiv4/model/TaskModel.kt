package com.kolecko.koleckonestestiv4

import java.util.ArrayList

interface TaskModel {
    fun getAllTasks(): List<Task>
    fun removeTask(task: Task)
}

class TaskModelImpl : TaskModel {
    private val tasks = ArrayList<Task>()

    init {
        tasks.add(Task("Naučit se ke zkoušce", "Zkouška z Elektrotechniky"))
        tasks.add(Task("Úklid chodby", "Zamést a vytřít"))
        tasks.add(Task("Nakoupit", "4x Olomoucké tvarůžky"))
        tasks.add(Task("Vytvořit Android app", "\"Hello World\""))
        tasks.add(Task("Vyprat prádlo", "Praločka a žehlení"))
        tasks.add(Task("Udělat domácí úkoly", "Matematika, čeština, angličtina"))
        tasks.add(Task("Sportovat", "Jít běhat nebo cvičit"))
        tasks.add(Task("Připravit večeři", "Nakoupit a uvařit večeři"))
        tasks.add(Task("Číst knihu", "1 kapitola denně"))
        tasks.add(Task("Udělat prezentaci", "Pro školu nebo práci"))
        tasks.add(Task("Poslouchat hudbu", "Objevovat novou hudbu"))
        tasks.add(Task("Zaměřit se na sebe", "Meditovat nebo jóga"))
        tasks.add(Task("Udělat výlet", "Navštívit nové místo"))
    }

    override fun getAllTasks(): List<Task> {
        return tasks.toList()
    }

    override fun removeTask(task: Task) {
        tasks.remove(task)
    }
}
