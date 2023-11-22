package com.kolecko.koleckonestestiv4

import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.kolecko.koleckonestestiv4.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface MainController {
    fun setIsWheelSpinning (isIt: Boolean)
    fun getIsWheelSpinning () : Boolean
    fun updatePoints()
    fun doWithTaskDialog()
    fun startCountdownTime(maxCountdown: Int)
    fun showStatistics()
    fun onTimeSet(hourOfDay: Int, minute: Int)
    suspend fun getAllTasks(): List<Task>
}

class MainControllerImpl(private val view: MainView, private val notification: Notification, private val model: TaskModel) : ComponentActivity(), MainController {
    private var isWheelSpinning = false
    private var currentPoints = 0
    private var calculatedProgress = 0
    private var currentCountdownTime = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun setIsWheelSpinning (isIt: Boolean) {
        isWheelSpinning =isIt
    }

    override fun getIsWheelSpinning () : Boolean {
        return isWheelSpinning
    }

    override fun doWithTaskDialog() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(3000)
            currentPoints++
            if (model.getAllTasks().size > 0) {
                val selectedTask = model.getAllTasks().random()
                view.showTaskDialog(selectedTask)
                model.removeTask(selectedTask) // Remove the selected task
                view.showAllTasks()
                updatePoints()
                isWheelSpinning = false
            } else {
                isWheelSpinning = true
                view.showAllTasks()
                updatePoints()
            }
        }
    }

    override fun startCountdownTime(maxCountdown: Int) {
        handler.removeCallbacksAndMessages(null) // Remove any previous callbacks
        val updateInterval = 1000L // 1 second

        handler.post(object : Runnable {
            var currentCountdownTime = maxCountdown
            override fun run() {
                if (currentCountdownTime  > 0) {
                    calculatedProgress = (maxCountdown - currentCountdownTime ) * 100 / maxCountdown
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
                    // Call showBarAndTime to update UI

                }
            }
        })
    }

    override fun updatePoints() {
        lifecycleScope.launch(Dispatchers.Main) {
            isWheelSpinning = true
            var text = ""
            if (currentPoints == 1) {
                text = "bod"
            } else if (currentPoints in 2..4) {
                text = "body"
            } else {
                text = "bodů"
            }

            val finalText = "${currentPoints} $text"
            view.showUpdatedPoints(finalText)
        }
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
}
