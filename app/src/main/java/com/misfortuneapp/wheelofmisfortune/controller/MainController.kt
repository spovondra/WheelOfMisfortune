package com.misfortuneapp.wheelofmisfortune.controller

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.misfortuneapp.wheelofmisfortune.R
import com.misfortuneapp.wheelofmisfortune.custom.BroadcastService
import com.misfortuneapp.wheelofmisfortune.custom.SwipeHelper
import com.misfortuneapp.wheelofmisfortune.custom.TaskAdapter
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

// Rozhraní definující hlavní funkce řídící aplikaci
interface MainController {
    fun setIsWheelSpinning(isIt: Boolean) // Nastaví příznak, zda se kolo otáčí
    fun getIsWheelSpinning(): Boolean // Vrátí informaci, zda se kolo otáčí
    fun doWithTaskDialog() // Provede akce související s dialogem úlohy
    suspend fun getAllTasks(): List<Task> // Asynchronně získá všechny úlohy
    fun loadPointsFromDatabase() // Načte body z databáze
    fun startTimer(taskId: Int) // Spustí časovač pro úlohu
    fun stopTimer() // Zastaví časovač
    suspend fun setTaskDone(selectedTask: Task) // Nastaví úlohu na splněnou
    suspend fun setTime(selectedTimeInMillis: Long) // Asynchronně nastaví čas úlohy
    suspend fun setFirstTime() // Asynchronně nastaví první čas (při prvním spuštění)
    suspend fun getTime(): TimeRecord // Asynchronně získá časový záznam
    suspend fun getTasksInStates(taskState: TaskState): List<Task> // Asynchronně získá úlohy v daném stavu
    fun openTaskDetailsScreen(task: Task, context: Context)
    fun swipeHelperToDeleteAndEdit(
        recyclerView: RecyclerView,
        enableDone: Boolean,
        context: Context
    )

    suspend fun getDoneTasksForDate(dateString: String): List<Task>
    suspend fun setTaskDeleted(task: Task)
    suspend fun getTimeSetByUserInTriple(): Triple<Long, Long, Long>
}

// Implementace rozhraní MainController
class MainControllerImpl(
    private val context: Context,
    private val view: MainView, // Instance pro interakci s uživatelským rozhraním
    private val model: TaskModel, // Instance pro práci s úlohami
    private val statisticsController: StatisticsController // Instance pro správu statistik
) : ComponentActivity(), MainController {
    private var isWheelSpinning = false // Příznak, zda se kolo otáčí
    private var calculatedProgress = 0 // Vypočtený postup odpočtu
    private var currentPoints = 0
    private var lastAddedDate: String = getCurrentDate() // Poslední datum přidání bodů
    private var countdownServiceIntent: Intent? = null

    // Přijímač pro zachycení událostí z BroadcastService (např. časovač)
    @OptIn(DelicateCoroutinesApi::class)
    val countdownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BroadcastService.COUNTDOWN_BR) {
                // Zpracování příchozích informací z BroadcastService
                val remainingTime = intent.getLongExtra("countdown", 0)
                val timerRunning = intent.getBooleanExtra("countdownTimerRunning", false)
                val timerFinished = intent.getBooleanExtra("countdownTimerFinished", false)

                var text = ""

                // Zpracování běžícího časovače
                if (timerRunning) {
                    val (hours, minutes, seconds) = calculateRemainingTime(remainingTime)

                    GlobalScope.launch {
                        val timeSetByUser = getTimeSetByUser()
                        calculatedProgress = if (timeSetByUser != 0L) {
                            ((timeSetByUser - remainingTime) * 100 / timeSetByUser).toInt()
                        } else {
                            0
                        }
                    }

                        // Formátování zbývajícího času
                    text = if (hours.toInt() == 0) {
                        String.format("%02d:%02d", minutes, seconds)
                    } else {
                        String.format("%02d:%02d", hours, minutes)
                    }

                }
                // Zpracování ukončeného časovače
                if (timerFinished) {
                    text = "Start"
                    calculatedProgress = 100
                    isWheelSpinning = false
                }

                // Aktualizace UI s informacemi o časovači
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
                // Získání dostupných úloh seřazených podle priority
                val tasks = model.getTasksByState(TaskState.AVAILABLE).sortedByDescending { it.priority }

                if (tasks.isNotEmpty()) {
                    // Vytvoření seznamu úkolů, kde každý úkol bude opakován podle své priority
                    val weightedTaskList = tasks.flatMap { task ->
                        List(task.priority) { task }
                    }

                    // Náhodný výběr úkolu z nově vytvořeného seznamu
                    val selectedTask = weightedTaskList.random()

                    setTaskInProgress(selectedTask)

                    val finalText = "$currentPoints"
                    view.showUpdatedPoints(finalText)
                    //setTaskDone(selectedTask)

                    //view.showDrawnTasks()

                    // Odebrání vybrané úlohy z databáze
                    // model.removeTask(selectedTask)
                }
            }
            isWheelSpinning = true
        }
    }

    // Metoda pro provedení akcí souvisejících s dialogem úlohy
    override fun doWithTaskDialog() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(3500)
            view.scrollToTask()
            delay(250)
            updatePoints()
            isWheelSpinning = false  // Nastavení na false po skončení dialogu úlohy
            setTime(getTimeSetByUser())
        }
    }

    // Metoda pro spuštění časovače pro úlohu
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

    // Metoda pro asynchronní odstranění úlohy
    override suspend fun setTaskDeleted(task: Task) {
        model.removeTask(task)
        view.showAllTasks()
    }

    // Metoda pro načtení bodů z databáze
    override fun loadPointsFromDatabase() {
        lifecycleScope.launch(Dispatchers.Main) {
            currentPoints = 0
            // Získání dat z databáze
            val currentDate =
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
            val dataEntity = statisticsController.getDataByDate(currentDate)

            if (dataEntity != null) {
                currentPoints = dataEntity.value.toInt()
            }
            // Aktualizace zobrazených bodů v UI
            view.showUpdatedPoints("$currentPoints")
        }
    }

    // Metoda pro vrácení aktuálního data
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    // Metoda pro zastavení časovače (nepoužíváno)
    override fun stopTimer() {
        if (countdownServiceIntent != null) {
            context.stopService(countdownServiceIntent)
            countdownServiceIntent = null
        }
    }

    // Metoda pro výpočet zbývajícího času z millis
    private fun calculateRemainingTime(remainingTimeMillis: Long): Triple<Long, Long, Long> {
        val remainingSeconds = remainingTimeMillis / 1000
        val remainingMinutes = remainingSeconds / 60
        val remainingHours = remainingMinutes / 60
        return Triple(remainingHours, remainingMinutes % 60, remainingSeconds % 60)
    }

    // Metoda pro asynchronní nastavení úlohy do stavu "IN_PROGRESS"
    private suspend fun setTaskInProgress(task: Task) {
        val timeRecord = model.getTimeRecord()

        task.startTime = timeRecord.startTime
        task.endTime = timeRecord.endTime
        task.taskState = TaskState.IN_PROGRESS

        model.updateTask(task)
    }

    // Metoda pro asynchronní získání úloh v daném stavu
    override suspend fun getTasksInStates(taskState: TaskState): List<Task> {
        return model.getTasksByState(taskState)
    }

    override fun openTaskDetailsScreen(task: Task, context: Context) {
        view.openTaskDetailsScreen(task,context)
    }

    // Provedení akcí po swipu
    override fun swipeHelperToDeleteAndEdit(recyclerView: RecyclerView, enableDone: Boolean, context: Context) {
        recyclerView.layoutManager = LinearLayoutManager(context)

        object : SwipeHelper(context, recyclerView, true) {
            override fun instantiateUnderlayButton(
                ignoredViewHolder: RecyclerView.ViewHolder?,
                underlayButtons: MutableList<UnderlayButton>?
            ) {
                // Delete Button
                underlayButtons?.add(UnderlayButton(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_action_trash
                    ),
                    Color.parseColor("#FF0000"),
                    object : UnderlayButtonClickListener {
                        @SuppressLint("ClickableViewAccessibility")
                        override fun onClick(pos: Int) {
                            (recyclerView.adapter as? TaskAdapter)?.itemDeleted(pos)
                            recyclerView.adapter?.notifyItemRemoved(pos)
                        }
                    }
                ))

                // Done Button (pokud je aktivní)
                if (enableDone) {
                    underlayButtons?.add(UnderlayButton(
                        AppCompatResources.getDrawable(
                            context,
                            R.drawable.ic_action_tick
                        ),
                        Color.parseColor("#00FF00"),
                        object : UnderlayButtonClickListener {
                            @SuppressLint("ClickableViewAccessibility")
                            override fun onClick(pos: Int) {
                                (recyclerView.adapter as? TaskAdapter)?.itemDone(pos)
                            }
                        }
                    ))
                }
            }
        }
    }

    // Metoda pro asynchronní nastavení úlohy do stavu "DONE"
    override suspend fun setTaskDone(selectedTask: Task) {
        selectedTask.taskState = TaskState.DONE
        selectedTask.completionTime = System.currentTimeMillis()

        model.updateTask(selectedTask)

        // Kontrola, zda začal nový den
        val currentDate = getCurrentDate()
        if (currentDate != lastAddedDate) {
            currentPoints = 0
            lastAddedDate = currentDate
        }

        // Zvýšení bodů na základě výchozích bodů úlohy
        currentPoints += selectedTask.points


        val finalText = "$currentPoints"
        view.showUpdatedPoints(finalText)

        // Uložení bodů do databáze
        val formattedDate =
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        statisticsController.insertOrUpdateData(formattedDate, currentPoints.toDouble())
    }

    // Metoda pro asynchronní nastavení času úlohy
    override suspend fun setTime(selectedTimeInMillis: Long) {
        val startTime = System.currentTimeMillis()
        val endTime = System.currentTimeMillis() + selectedTimeInMillis

        model.insertTimeRecord(startTime, endTime)
        val timeId = getTime().id

        startTimer(timeId)
    }

    // Metoda pro asynchronní nastavení prvního času (při prvním spuštění)
    override suspend fun setFirstTime() {
        model.insertTimeRecord(0, 0)
    }

    // Metoda pro asynchronní získání časového záznamu
    override suspend fun getTime(): TimeRecord {
        return model.getTimeRecord()
    }

    // Metoda pro získání seznamu splněných úkolů v daný den
    override suspend fun getDoneTasksForDate(dateString: String): List<Task> {
        // Získání formátu pro datum
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        // Získání seznamu všech splněných úkolů
        val allDoneAndDeletedTasks = model.getTasksByState(TaskState.DONE) + model.getTasksByState(TaskState.DELETED)

        // Filtrujeme úkoly podle vybraného data
        return allDoneAndDeletedTasks.filter { task ->
            val taskCompletionDate = Date(task.completionTime)
            dateFormat.format(taskCompletionDate) == dateString
        }
    }

    // Metoda pro asynchronní získání délky úlohy nastavené uživatelem
    private suspend fun getTimeSetByUser(): Long {
        return getTime().endTime - getTime().startTime
    }

    override suspend fun getTimeSetByUserInTriple(): Triple<Long, Long, Long> {
        return calculateRemainingTime(getTimeSetByUser())
    }
}
