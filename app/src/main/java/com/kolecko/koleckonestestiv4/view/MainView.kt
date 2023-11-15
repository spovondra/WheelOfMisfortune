package com.kolecko.koleckonestestiv4

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random

interface MainView {
    fun showWheelSpin()
    fun showUpdatedPoints(text : String)
    fun showNumberOfTasks ()
    fun showAllTasks()
    fun showStatistics()
    fun showTaskDialog(task: Task) //tmp
    fun showSetTime()
    fun showBarAndTime(progress: Int, currentCountdownTime: Int)
    fun wheelAbleToTouch ()
}

class MainViewImp : ComponentActivity(), MainView {
    private lateinit var controller: MainControllerImpl
    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var countdownTimerTextView: TextView
    private lateinit var notificationHandler: NotificationHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        circularProgressBar = findViewById(R.id.circularProgressBar)
        countdownTimerTextView = findViewById(R.id.countdownTimerTextView)

        val taskRepository: TaskModel = TaskModelImpl()
        notificationHandler = NotificationHandler(this)

        controller = MainControllerImpl(this, notificationHandler, taskRepository)
        controller.startCountdownTime(10)

        showNumberOfTasks()
        showAllTasks()
        showStatistics()
        showSetTime()

        // Add click listener to the orange button (assuming it has an ID 'newTaskButton')
        val newTaskButton: Button = findViewById(R.id.floatingActionButton)
        newTaskButton.setOnClickListener {
            // Open NewTaskActivity when the button is clicked
            val intent = Intent(this, NewTaskActivity::class.java)
            startActivity(intent)
        }
    }

    override fun showWheelSpin() {
        // Získání reference na ImageView s kolem
        val wheel = findViewById<ImageView>(R.id.wheel_spin)

        // Nastavíme střed otáčení na střed obrázku
        val pivotX = wheel.width / 2f
        val pivotY = wheel.height / 2f
        wheel.pivotX = pivotX
        wheel.pivotY = pivotY

        // Náhodné otočení od 2 do 10 otáček
        val degrees = Random.nextFloat() * 3600 + 720

        // Použijeme animaci rotace kolem středu obrázku
        wheel.animate()
            .rotationBy(degrees)
            .setDuration(3000)
            .setInterpolator(DecelerateInterpolator()) // Plynulý průběh otáčení
            .start()

        controller.doWithTaskDialog()
    }

    override fun showUpdatedPoints(text: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val buttonUp: Button = findViewById(R.id.buttonUp)
            buttonUp.text = text
            // Check if there are tasks available
            showNumberOfTasks()
            showAllTasks()
        }
    }

    override fun showNumberOfTasks() {
        val textNum: TextView = findViewById(R.id.textNum)
        textNum.text = "Vaše úlohy (${controller.getAllTasks().size})"
    }

    override fun showAllTasks() {
        val taskList = findViewById<RecyclerView>(R.id.taskList)
        taskList.layoutManager = LinearLayoutManager(this)
        taskList.adapter = TaskAdapter(controller.getAllTasks())
    }

    override fun showStatistics() {
        val buttonShowStatistics = findViewById<Button>(R.id.buttonUp)
        buttonShowStatistics.setOnClickListener {
            // Po kliknutí na tlačítko zobrazíme statistiky (přejdeme na novou aktivitu)
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }
    }

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

    override fun showSetTime() {
        val buttonSetTime = findViewById<Button>(R.id.buttonSetTime)
        buttonSetTime.setOnClickListener {
            val timePicker =
                TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    // Handle the time set by the user here
                    val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                    countdownTimerTextView.text = selectedTime  // Update the TextView with the selected time
                    var countdown = hourOfDay*60+minute

                    // Reset the countdown and progress bar when a new time is set
                    circularProgressBar.setProgress(100) // Reset the progress bar
                    controller.startCountdownTime(countdown) // Start the countdown
                }, 0, 0, true)
            timePicker.show()
        }
    }

    override fun showBarAndTime(progress: Int, currentCountdownTime: Int) {
        circularProgressBar.setProgress(progress)
        countdownTimerTextView.text = String.format("%02d:%02d", currentCountdownTime/60, currentCountdownTime%60)
    }

    override fun wheelAbleToTouch () {
        val wheel: ImageView = findViewById(R.id.wheel_spin)
        wheel.setOnClickListener {
            if (!controller.getIsWheelSpinning()) {
                controller.setIsWheelSpinning(true)
                showWheelSpin()
            }
        }
    }
}