package com.misfortuneapp.wheelofmisfortune.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.misfortuneapp.wheelofmisfortune.custom.BroadcastService
import com.misfortuneapp.wheelofmisfortune.model.*
import com.misfortuneapp.wheelofmisfortune.view.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

interface MainController {
    fun setIsWheelSpinning (isIt: Boolean) //nechat
    fun getIsWheelSpinning () : Boolean //nechat
    fun doWithTaskDialog() //nechat
    //fun startCountdownTime(maxCountdown: Int) //nechat
    suspend fun getAllTasks(): List<Task> //nechat
    fun loadPointsFromDatabase() //nechat
    suspend fun addNewTask(
        title: String,
        description: String,
        priority: Int,
        iconResId: Int,
        startTime: Long,
        endTime: Long
    )
    fun startTimer(taskId: Int)
    fun stopTimer()
    fun calculateRemainingTime(remainingTimeMillis: Long): Triple<Long, Long, Long>
    suspend fun setTaskInProgress(task: Task)
    suspend fun getTasksInProgress(): List<Task>
    suspend fun setTimeToTask(selectedTimeInMillis: Long)
    suspend fun selectedTask(): Task?
}

class MainControllerImpl(
    private val context: Context,
    private val view: MainView, // Instance pro interakci s uživatelským rozhraním
    private val notification: Notification, // Instance pro zobrazení oznámení
    private val model: TaskModel, // Instance pro práci s úlohami
    private val statisticsController: StatisticsController // Instance pro správu statistik
) : ComponentActivity(), MainController {
    private var isWheelSpinning = false // Příznak, zda se kolo otáčí
    private var currentPoints = 0 // Aktuální počet bodů
    private var calculatedProgress = 0 // Vypočtený postup odpočtu
    private var lastAddedDate: String = getCurrentDate() // Poslední datum přidání bodů
    private var countdownServiceIntent: Intent? = null

    @OptIn(DelicateCoroutinesApi::class)
    val countdownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BroadcastService.COUNTDOWN_BR) {
                val remainingTime = intent.getLongExtra("countdown", 0)
                val timerRunning = intent.getBooleanExtra("countdownTimerRunning", false)
                val timerFinished = intent.getBooleanExtra("countdownTimerFinished", false)
                var text = ""

                if (timerRunning) {
                    val (hours, minutes, seconds) = calculateRemainingTime(remainingTime)

                    GlobalScope.launch {
                        calculatedProgress = ((getTimeSetByUser() - remainingTime) * 100 / getTimeSetByUser()).toInt()
                    }

                    text = if(hours.toInt() == 0) {
                        String.format("%02d:%02d", minutes, seconds)
                    } else {
                        String.format("%02d:%02d", hours, minutes)
                    }

                } else if (timerFinished) {
                    text = "Start"
                    notification.showNotification()
                    calculatedProgress = 100
                    isWheelSpinning = false  // Nastavte na false po skončení odpočtu
                    view.wheelAbleToTouch()
                }
                view.showBarAndTime(calculatedProgress, text)
            }
        }
    }

    private suspend fun getTimeSetByUser (): Long {
        return selectedTask()!!.endTime-selectedTask()!!.startTime
    }

    // Metoda pro nastavení příznaku, zda se kolo otáčí
    override fun setIsWheelSpinning(isIt: Boolean) {
        isWheelSpinning = isIt
    }

    // Metoda pro získání informace, zda se kolo otáčí
    override fun getIsWheelSpinning(): Boolean {
        return isWheelSpinning
    }

    // Metoda pro aktualizaci bodů na základě vybrané úlohy
    private fun updatePoints() {
        lifecycleScope.launch(Dispatchers.Main) {
            if (!isWheelSpinning) {

                val selectedTask = selectedTask()

                // Zkontroluje, zda začal nový den
                val currentDate = getCurrentDate()
                if (currentDate != lastAddedDate) {
                    currentPoints = 0
                    lastAddedDate = currentDate
                }

                // Zvýší body na základě výchozích bodů úlohy
                currentPoints += selectedTask!!.points

                // Text pro zobrazení v UI
                var text = "bodů"
                if (currentPoints == 1) {
                    text = "bod"
                } else if (currentPoints in 2..4) {
                    text = "body"
                }

                val finalText = "$currentPoints $text"
                view.showUpdatedPoints(finalText)

                // Uloží body do databáze
                val formattedDate =
                    SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date())
                statisticsController.insertOrUpdateData(formattedDate, currentPoints.toDouble())

                view.showTaskDialog(selectedTask)

                // Odebere vybranou úlohu z databáze
                model.removeTask(selectedTask)
            }
            selectedTask()
            isWheelSpinning = true
        }
    }

    // Metoda pro provedení akcí souvisejících s dialogem úlohy
    override fun doWithTaskDialog() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(3000)
            view.showAllTasks()
            updatePoints()
            isWheelSpinning = false  // Nastavte na false po skončení dialogu úlohy
        }
    }

    override fun startTimer(taskId: Int) {
        isWheelSpinning = true

        if (countdownServiceIntent != null) {
            context.stopService(countdownServiceIntent)
        }

        countdownServiceIntent = Intent(context, BroadcastService::class.java)
        countdownServiceIntent?.putExtra(BroadcastService.EXTRA_TASK_ID, taskId)
        countdownServiceIntent?.action = BroadcastService.COUNTDOWN_BR
        context.startService(countdownServiceIntent)
    }

    // Metoda pro asynchronní získání všech úloh
    override suspend fun getAllTasks(): List<Task> {
        return model.getAllTasks()
    }

    // Metoda pro načtení bodů z databáze
    override fun loadPointsFromDatabase() {
        lifecycleScope.launch(Dispatchers.Main) {
            // Získá data z databáze
            val currentDate =
                SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date())
            val dataEntity = statisticsController.getDataByDate(currentDate)

            // Aktualizuje body v UI, pokud existují data
            if (dataEntity != null) {
                currentPoints = dataEntity.value.toInt()
                // Aktualizuje zobrazené body v UI
                val text = if (currentPoints == 1) "bod" else "bodů"
                view.showUpdatedPoints("$currentPoints $text")
            }
        }
    }

    // Metoda pro vrácení aktuálního data
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    override suspend fun addNewTask(
        title: String,
        description: String,
        priority: Int,
        iconResId: Int,
        startTime: Long,
        endTime: Long
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            model.addNewTask(title, description, priority, iconResId, startTime, endTime)
        }
    }


    override fun stopTimer() {
        if (countdownServiceIntent != null) {
            context.stopService(countdownServiceIntent)
        }
    }

    override fun calculateRemainingTime(remainingTimeMillis: Long): Triple<Long, Long, Long> {
        val remainingSeconds = remainingTimeMillis / 1000
        val remainingMinutes = remainingSeconds / 60
        val remainingHours = remainingMinutes / 60
        return Triple(remainingHours, remainingMinutes % 60, remainingSeconds % 60)
    }

    override suspend fun setTaskInProgress(task: Task) {
        task.taskState = TaskState.IN_PROGRESS
        model.updateTask(task)
    }
    override suspend fun getTasksInProgress(): List<Task> {
        return model.getTasksByState(TaskState.IN_PROGRESS)
    }

    override suspend fun setTimeToTask(selectedTimeInMillis: Long) {
        for (task in getTasksInProgress()) {
            task.startTime = System.currentTimeMillis()
            task.endTime = System.currentTimeMillis() + selectedTimeInMillis
            model.updateTask(task)

            startTimer(task.id)
        }
    }

    override suspend fun selectedTask(): Task? {
        val tasks = model.getAllTasks()
        var selectedTask: Task? = null

        if (tasks.isNotEmpty() && model.getTasksByState(TaskState.IN_PROGRESS).isNotEmpty()) {
            selectedTask = getTasksInProgress()[0]
        }

        else if (tasks.isNotEmpty()) {
            selectedTask = tasks.random()
            setTaskInProgress(selectedTask)
        }

        return selectedTask
    }


}
