package com.misfortuneapp.wheelofmisfortune.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
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
    fun setIsWheelSpinning (isIt: Boolean) // Nechat
    fun getIsWheelSpinning () : Boolean // Nechat
    fun doWithTaskDialog() // Nechat
    suspend fun getAllTasks(): List<Task> // Nechat
    suspend fun removeTask(task: Task) // Nechat
    fun loadPointsFromDatabase() // Nechat
    suspend fun addNewTask(
        title: String,
        description: String,
        priority: Int,
        iconResId: Int,
        startTime: Long,
        endTime: Long
    ) // Nechat
    fun startTimer(taskId: Int) // Nechat
    fun stopTimer() // Není implementováno
    suspend fun setTime (selectedTimeInMillis: Long) // Nechat
    suspend fun setFirstTime () // Nechat
    suspend fun getTime (): TimeRecord? // Nechat
    suspend fun getTasksInStates(taskState: TaskState): List<Task> // Nechat
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
                val (hours, minutes, seconds) = calculateRemainingTime(remainingTime)
                Log.d("MainControllerImpl", "Broadcast received. TimerRunning: $timerRunning, TimerFinished: $timerFinished, RemainingTime: $remainingTime")

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

                }
                if (timerFinished) {
                    Log.d("MainControllerImpl", "Timer finished. Text: $text, Calculated Progress: $calculatedProgress")
                    text = "Start"
                    notification.showNotification()
                    calculatedProgress = 100
                    isWheelSpinning = false
                } else if (remainingTime.toInt() == 0) {
                    // Handle the case where remainingTime is 0
                    Log.d("MainControllerImpl", "Remaining time is 0. Handle this case as needed.")
                }

                view.showBarAndTime(calculatedProgress, text)
            }
        }
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

                val tasks = model.getTasksByState(TaskState.AVAILABLE)

                if (tasks.isNotEmpty()) {
                    val selectedTask = tasks.random()
                    setTaskInProgress(selectedTask)

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
                    setTaskDone(selectedTask)

                    // Uloží body do databáze
                    val formattedDate =
                        SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date())
                    statisticsController.insertOrUpdateData(formattedDate, currentPoints.toDouble())

                    view.showTaskDialog(selectedTask)

                    // Odebere vybranou úlohu z databáze
                    //model.removeTask(selectedTask)
                }
            }
            isWheelSpinning = true
        }
    }

    // Metoda pro provedení akcí souvisejících s dialogem úlohy
    override fun doWithTaskDialog() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(3000)
            //view.showAllTasks()
            updatePoints()
            isWheelSpinning = false  // Nastavte na false po skončení dialogu úlohy
            setTime(getTimeSetByUser())
        }
    }

    override fun startTimer(taskId: Int) {
        isWheelSpinning = true

        if (countdownServiceIntent != null) {
            context.stopService(countdownServiceIntent)
        }

        countdownServiceIntent = Intent(context, BroadcastService::class.java)
        countdownServiceIntent?.putExtra(BroadcastService.EXTRA_TIME_ID, taskId)
        countdownServiceIntent?.action = BroadcastService.COUNTDOWN_BR
        context.startService(countdownServiceIntent)
    }

    // Metoda pro asynchronní získání všech úloh
    override suspend fun getAllTasks(): List<Task> {
        return model.getAllTasks()
    }

    override suspend fun removeTask(task: Task) {
        model.removeTask(task)
        view.showAllTasks()
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
            view.showAllTasks()  // Aktualizujte UI po přidání nového úkolu
        }
    }

    override fun stopTimer() {
        if (countdownServiceIntent != null) {
            context.stopService(countdownServiceIntent)
        }
    }

    private fun calculateRemainingTime(remainingTimeMillis: Long): Triple<Long, Long, Long> {
        val remainingSeconds = remainingTimeMillis / 1000
        val remainingMinutes = remainingSeconds / 60
        val remainingHours = remainingMinutes / 60
        return Triple(remainingHours, remainingMinutes % 60, remainingSeconds % 60)
    }

    private suspend fun setTaskInProgress(task: Task) {
        task.taskState = TaskState.IN_PROGRESS
        model.updateTask(task)
    }
    override suspend fun getTasksInStates(taskState: TaskState): List<Task> {
        return model.getTasksByState(taskState)
    }

    private suspend fun setTaskDone (selectedTask: Task) {
        val timeRecords = model.getAllTimeRecords()

        if (timeRecords.isNotEmpty()) {
            val time = timeRecords[0]

            selectedTask.startTime = time.endTime
            selectedTask.endTime = time.endTime
            selectedTask.taskState = TaskState.DONE

            model.updateTask(selectedTask)
        }
    }

    override suspend fun setTime (selectedTimeInMillis: Long) {
        val startTime = System.currentTimeMillis()
        val endTime = System.currentTimeMillis() + selectedTimeInMillis

        model.insertTimeRecord(startTime, endTime)
        val timeId = getTime()!!.id

        startTimer(timeId)
    }

    override suspend fun setFirstTime () {
        model.insertTimeRecord(0, 0)
    }

    override suspend fun getTime(): TimeRecord? {
        val timeRecords = model.getAllTimeRecords()
        return if (timeRecords.isNotEmpty()) {
            timeRecords[0]
        } else {
            null
        }
    }

    private suspend fun getTimeSetByUser (): Long {
        return getTime()!!.endTime- getTime()!!.startTime
    }
}
