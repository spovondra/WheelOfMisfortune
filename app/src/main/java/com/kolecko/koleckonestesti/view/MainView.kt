package com.kolecko.koleckonestesti
import  android.util.Log;
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kolecko.koleckonestesti.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.MainScope

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

        // Initialize DataRepository
        dataRepository = DataRepository(DataDatabase.getInstance(this).dataDao())

        circularProgressBar = findViewById(R.id.circularProgressBar)
        countdownTimerTextView = findViewById(R.id.countdownTimerTextView)

        val taskRepository: TaskModel = TaskModelImpl(this)
        notificationHandler = NotificationHandler(this)
        statisticsController = StatisticsControllerImp(dataRepository)

        controller = MainControllerImpl(this, notificationHandler, taskRepository, statisticsController)
        controller.startCountdownTime(10)
        controller.loadPointsFromDatabase()

        GlobalScope.launch {
            showNumberOfTasks()
            showAllTasks()
        }
        showStatistics()
        showSetTime()
        wheelAbleToTouch()

        val newTaskButton: Button = findViewById(R.id.floatingActionButton)
        newTaskButton.setOnClickListener {
            val intent = Intent(this, NewTaskActivity::class.java)
            startActivity(intent)
        }
    }

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

    @OptIn(DelicateCoroutinesApi::class)
    private fun updateStatistics() {
        GlobalScope.launch {
            showStatistics()
        }
    }

    @SuppressLint("SetTextI18n")
    override suspend fun showNumberOfTasks() {
        val textNum: TextView = findViewById(R.id.textNum)
        textNum.text = "Your tasks (${controller.getAllTasks().size})"
    }

    override suspend fun showAllTasks() {
        val taskList = findViewById<RecyclerView>(R.id.taskList)
        taskList.layoutManager = LinearLayoutManager(this)

        val adapter = TaskAdapter(controller.getAllTasks()) { selectedTask ->
            openTaskDetailsScreen(selectedTask)
        }

        taskList.adapter = adapter
    }

    private fun openTaskDetailsScreen(task: Task) {
        val intent = Intent(this, TaskDetailsActivity::class.java)
        intent.putExtra("taskId", task.id)
        Log.d("TaskDetailsActivity", "Task ID: $taskId")
        startActivity(intent)
    }

    override fun showStatistics() {
        val buttonShowStatistics = findViewById<Button>(R.id.buttonUp)
        buttonShowStatistics.setOnClickListener {
            val intent = Intent(this, StatisticsViewImp::class.java)
            startActivity(intent)
        }
    }

    override fun showTaskDialog(task: Task) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Your Task")
        dialogBuilder.setMessage("${task.title} - ${task.description}")
        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

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

    override fun showBarAndTime(progress: Int, currentCountdownTime: Int) {
        circularProgressBar.setProgress(progress)
        countdownTimerTextView.text = String.format("%02d:%02d", currentCountdownTime / 60, currentCountdownTime % 60)
    }

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

    override fun onResume() {
        super.onResume()

        launch {
            showNumberOfTasks()
            showAllTasks()
        }
    }
}
