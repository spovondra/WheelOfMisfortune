package com.kolecko.koleckonestestiv4

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.kolecko.koleckonestestiv4.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

interface MainController {
    fun setIsWheelSpinning (isIt: Boolean)
    fun getIsWheelSpinning () : Boolean
    fun updatePoints()
    fun doWithTaskDialog()
    fun startCountdownTime(maxCountdown: Int)
    fun showStatistics()
    fun onTimeSet(hourOfDay: Int, minute: Int)
    suspend fun getAllTasks(): List<Task>
    fun loadPointsFromDatabase()
    fun getCurrentDate(): String
}

class MainControllerImpl(
    private val view: MainView,
    private val notification: Notification,
    private val model: TaskModel,
    private val statisticsController: StatisticsController,
    private val dataRepository: DataRepository
) : ComponentActivity(), MainController {

    private var isWheelSpinning = false
    private var currentPoints = 0
    private var calculatedProgress = 0
    private var currentCountdownTime = 0
    private var lastAddedDate: String = getCurrentDate()
    private val handler = Handler(Looper.getMainLooper())

    override fun setIsWheelSpinning(isIt: Boolean) {
        isWheelSpinning = isIt
    }

    override fun getIsWheelSpinning(): Boolean {
        return isWheelSpinning
    }

    override fun updatePoints() {
        lifecycleScope.launch(Dispatchers.Main) {
            if (!isWheelSpinning) {
                // Randomly select a task
                val tasks = model.getAllTasks()

                if (tasks.isNotEmpty()) {
                    val selectedTask = tasks.random()

                    // Check if a new day has started
                    val currentDate = getCurrentDate()
                    if (currentDate != lastAddedDate) {
                        currentPoints = 0
                        lastAddedDate = currentDate
                    }

                    // Increment points based on the default points in the task
                    currentPoints += selectedTask.points

                    var text = ""
                    if (currentPoints == 1) {
                        text = "bod"
                    } else if (currentPoints in 2..4) {
                        text = "body"
                    } else {
                        text = "bodů"
                    }

                    val finalText = "$currentPoints $text"
                    view.showUpdatedPoints(finalText)

                    // Ukládání bodů do databáze
                    val formattedDate = SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date())
                    statisticsController.insertOrUpdateData(formattedDate, currentPoints.toDouble())

                    view.showTaskDialog(selectedTask)

                    // Remove the selected task from the database
                    model.removeTask(selectedTask)
                }
            }

            isWheelSpinning = true
        }
    }

    override fun doWithTaskDialog() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(3000)
            if (model.getAllTasks().isNotEmpty()) {
                isWheelSpinning = true
                view.showAllTasks()
                updatePoints()

            } else {
                view.showAllTasks()
                updatePoints()
            }
            isWheelSpinning = false
        }
    }

    override fun startCountdownTime(maxCountdown: Int) {
        handler.removeCallbacksAndMessages(null)
        val updateInterval = 1000L
        handler.post(object : Runnable {
            var currentCountdownTime = maxCountdown
            override fun run() {
                if (currentCountdownTime > 0) {
                    calculatedProgress = (maxCountdown - currentCountdownTime) * 100 / maxCountdown
                    view.showBarAndTime(calculatedProgress, currentCountdownTime)
                    currentCountdownTime--
                    handler.postDelayed(this, updateInterval)

                } else {
                    notification.showNotification()
                    calculatedProgress = 100
                    currentCountdownTime = 0
                    view.showBarAndTime(calculatedProgress, currentCountdownTime)
                    isWheelSpinning = false

                    view.wheelAbleToTouch()
                }
            }
        })
    }

    override fun showStatistics() {
        view.showStatistics()
    }

    override fun onTimeSet(hourOfDay: Int, minute: Int) {
        view.showBarAndTime(calculatedProgress, currentCountdownTime)
        val countdown = hourOfDay * 60 + minute

        isWheelSpinning = true
        startCountdownTime(countdown)
    }

    override suspend fun getAllTasks(): List<Task> {
        return model.getAllTasks()
    }

    override fun loadPointsFromDatabase() {
        var storedPoints: Int = 0
        lifecycleScope.launch(Dispatchers.Main) {
            // Získání dat z databáze
            val currentDate = SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date())
            val dataEntity = statisticsController.getDataByDate(currentDate)

            // Aktualizace bodů ve view, pokud existují data
            if (dataEntity != null) {
                currentPoints = dataEntity.value.toInt()
                // Aktualizace zobrazených bodů ve view
                val text = if (currentPoints == 1) "bod" else "bodů"
                view.showUpdatedPoints("$currentPoints $text")
            }
        }
    }
    override fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}