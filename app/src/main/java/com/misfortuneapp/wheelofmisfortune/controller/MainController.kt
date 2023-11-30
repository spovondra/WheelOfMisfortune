package com.misfortuneapp.wheelofmisfortune.controller

import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.misfortuneapp.wheelofmisfortune.model.*
import com.misfortuneapp.wheelofmisfortune.view.*
import kotlinx.coroutines.Dispatchers
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
    fun startCountdownTime(maxCountdown: Int) //nechat
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
}

class MainControllerImpl(
    private val view: MainView, // Instance pro interakci s uživatelským rozhraním
    private val notification: Notification, // Instance pro zobrazení oznámení
    private val model: TaskModel, // Instance pro práci s úlohami
    private val statisticsController: StatisticsController // Instance pro správu statistik
) : ComponentActivity(), MainController {

    private var isWheelSpinning = false // Příznak, zda se kolo otáčí
    private var currentPoints = 0 // Aktuální počet bodů
    private var calculatedProgress = 0 // Vypočtený postup odpočtu
    private var currentCountdownTime = 0 // Aktuální doba odpočtu
    private var lastAddedDate: String = getCurrentDate() // Poslední datum přidání bodů
    private val handler = Handler(Looper.getMainLooper()) // Handler pro plánování úkolů na hlavním vlákně

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
                // Náhodně vybere úlohu
                val tasks = model.getAllTasks()

                if (tasks.isNotEmpty()) {
                    val selectedTask = tasks.random()

                    // Zkontroluje, zda začal nový den
                    val currentDate = getCurrentDate()
                    if (currentDate != lastAddedDate) {
                        currentPoints = 0
                        lastAddedDate = currentDate
                    }

                    // Zvýší body na základě výchozích bodů úlohy
                    currentPoints += selectedTask.points

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
            }

            isWheelSpinning = true
        }
    }

    // Metoda pro provedení akcí souvisejících s dialogem úlohy
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
            isWheelSpinning = false  // Nastavte na false po skončení dialogu úlohy
        }
    }

    // Metoda pro spuštění odpočtu času
    override fun startCountdownTime(maxCountdown: Int) {
        isWheelSpinning = true  // Nastavte na true na začátku odpočtu
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
                    isWheelSpinning = false  // Nastavte na false po skončení odpočtu
                    view.wheelAbleToTouch()
                }
            }
        })
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
}
