package com.kolecko.koleckonestestiv4

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kolecko.koleckonestestiv4.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.MainScope

// Rozhraní reprezentující pohled (View) hlavní obrazovky
interface MainView {
    fun showWheelSpin()
    fun showUpdatedPoints(text: String)
    suspend fun showNumberOfTasks()
    suspend fun showAllTasks()
    fun showStatistics()
    fun showTaskDialog(task: Task)
    fun showSetTime()
    fun showBarAndTime(progress: Int, currentCountdownTime: Int)
    fun wheelAbleToTouch()
}

// Implementace rozhraní MainView
class MainViewImp : ComponentActivity(), MainView, CoroutineScope by MainScope() {
    private lateinit var controller: MainControllerImpl
    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var countdownTimerTextView: TextView
    private lateinit var notificationHandler: NotificationHandler
    private lateinit var statisticsController: StatisticsController
    private lateinit var dataRepository: DataRepository

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializace DataRepository
        dataRepository = DataRepository(AppDatabase.getInstance(this).dataDao())

        circularProgressBar = findViewById(R.id.circularProgressBar)
        countdownTimerTextView = findViewById(R.id.countdownTimerTextView)

        val taskRepository: TaskModel = TaskModelImpl(this)
        notificationHandler = NotificationHandler(this)
        statisticsController = StatisticsController(dataRepository)

        controller = MainControllerImpl(this, notificationHandler, taskRepository, statisticsController)
        controller.startCountdownTime(10)
        controller.loadPointsFromDatabase()

        GlobalScope.launch {
            showNumberOfTasks()
            showAllTasks()
        }
        showStatistics()
        showSetTime()

        val newTaskButton: Button = findViewById(R.id.floatingActionButton)
        newTaskButton.setOnClickListener {
            val intent = Intent(this, NewTaskActivity::class.java)
            startActivity(intent)
        }
    }

    // Metoda pro zobrazení animace otáčení kolem
    override fun showWheelSpin() {
        val wheel = findViewById<ImageView>(R.id.wheel_spin)
        val pivotX = wheel.width / 2f
        val pivotY = wheel.height / 2f
        wheel.pivotX = pivotX
        wheel.pivotY = pivotY

        runOnUiThread {
            val degrees = Random.nextFloat() * 3600 + 720

            wheel.animate()
                .rotationBy(degrees)
                .setDuration(3000)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        controller.doWithTaskDialog()
    }

    // Metoda pro zobrazení aktualizovaných bodů
    @OptIn(DelicateCoroutinesApi::class)
    override fun showUpdatedPoints(text: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val buttonUp: Button = findViewById(R.id.buttonUp)
            buttonUp.text = text
            showNumberOfTasks()
            showAllTasks()
            updateStatistics()
        }
    }

    // Privátní metoda pro aktualizaci statistik
    @OptIn(DelicateCoroutinesApi::class)
    private fun updateStatistics() {
        GlobalScope.launch {
            showStatistics()
        }
    }

    // Metoda pro zobrazení počtu úkolů
    @SuppressLint("SetTextI18n")
    override suspend fun showNumberOfTasks() {
        val textNum: TextView = findViewById(R.id.textNum)
        textNum.text = "Vaše úlohy (${controller.getAllTasks().size})"
    }

    // Metoda pro zobrazení všech úkolů
    override suspend fun showAllTasks() {
        val taskList = findViewById<RecyclerView>(R.id.taskList)
        taskList.layoutManager = LinearLayoutManager(this)
        taskList.adapter = TaskAdapter(controller.getAllTasks())
    }

    // Metoda pro zobrazení statistik
    override fun showStatistics() {
        val buttonShowStatistics = findViewById<Button>(R.id.buttonUp)
        buttonShowStatistics.setOnClickListener {
            val intent = Intent(this, StatisticsViewImpl::class.java)
            startActivity(intent)
        }
    }

    // Metoda pro zobrazení dialogu s úkolem
    override fun showTaskDialog(task: Task) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Váš úkol")
        dialogBuilder.setMessage("${task.title} - ${task.description}")
        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    // Metoda pro zobrazení dialogu pro nastavení času
    override fun showSetTime() {
        val buttonSetTime = findViewById<Button>(R.id.buttonSetTime)
        buttonSetTime.setOnClickListener {
            val timePicker = TimePickerDialog(this, { _, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                countdownTimerTextView.text = selectedTime
                val countdown = hourOfDay * 60 + minute

                circularProgressBar.setProgress(100)
                controller.startCountdownTime(countdown)
            }, 0, 0, true)
            timePicker.show()
        }
    }

    // Metoda pro zobrazení průběhu a zbývajícího času
    override fun showBarAndTime(progress: Int, currentCountdownTime: Int) {
        circularProgressBar.setProgress(progress)
        countdownTimerTextView.text = String.format("%02d:%02d", currentCountdownTime / 60, currentCountdownTime % 60)
    }

    // Metoda pro povolení otáčení kolem
    @OptIn(DelicateCoroutinesApi::class)
    override fun wheelAbleToTouch() {
        val wheel: ImageView = findViewById(R.id.wheel_spin)
        wheel.setOnClickListener {
            if (!controller.getIsWheelSpinning()) {
                GlobalScope.launch {
                    if (controller.getAllTasks().isNotEmpty()) {
                        controller.setIsWheelSpinning(true)
                        showWheelSpin()
                    }
                }
            }
        }
    }

    // Přepsaná metoda pro obnovení aktivity
    override fun onResume() {
        super.onResume()

        // Využití coroutines scope aktivity + spuštění na hlavním vlákně
        launch {
            showNumberOfTasks()
            showAllTasks()
        }
    }
}
