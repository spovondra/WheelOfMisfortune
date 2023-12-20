package com.misfortuneapp.wheelofmisfortune.view

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.misfortuneapp.wheelofmisfortune.custom.SwipeHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.misfortuneapp.wheelofmisfortune.R
import com.misfortuneapp.wheelofmisfortune.controller.*
import com.misfortuneapp.wheelofmisfortune.custom.*
import com.misfortuneapp.wheelofmisfortune.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import java.util.Calendar

interface MainView {
    fun showUpdatedPoints(text: String)
    suspend fun showAllTasks()
    fun showStatistics()
    suspend fun showDrawnTasks()
    fun showBarAndTime(progress: Int, currentCountdownTime: String)
    fun wheelAbleToTouch()
}

class MainViewImp : ComponentActivity(), MainView, CoroutineScope by MainScope() {
    private lateinit var controller: MainControllerImpl
    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var countdownTimerTextView: TextView
    private lateinit var notificationHandler: NotificationHandler
    private lateinit var statisticsController: StatisticsController
    private lateinit var dataRepository: DataRepository
    private lateinit var taskDao: TaskDao

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val taskList = findViewById<RecyclerView>(R.id.taskList)
        taskList.layoutManager = LinearLayoutManager(this@MainViewImp)

        // Initialize DataRepository
        dataRepository = DataRepositoryImpl(DataDatabase.getInstance(this).dataDao())
        taskDao = TaskDatabase.getDatabase(this).taskDao()

        circularProgressBar = findViewById(R.id.circularProgressBar)
        countdownTimerTextView = findViewById(R.id.countdownTimerTextView)

        val taskRepository: TaskModel = TaskModelImpl(this)
        notificationHandler = NotificationHandler(this)
        statisticsController = StatisticsControllerImp(dataRepository, StatisticsViewImp())

        controller = MainControllerImpl(this,this, notificationHandler, taskRepository, statisticsController)

        //controller.startCountdownTime(10)
        controller.loadPointsFromDatabase()

        GlobalScope.launch {
            controller.getTime().let { controller.startTimer(it.id) }
        }
        showStatistics()
        showSetTime()
        wheelAbleToTouch()
        swipeToDeleteButton()

        val newTaskButton: Button = findViewById(R.id.floatingActionButton)
        newTaskButton.setOnClickListener {
            val intent = Intent(this, NewTaskActivity::class.java)
            startActivity(intent)
        }

        registerReceiver(controller.countdownReceiver, IntentFilter(BroadcastService.COUNTDOWN_BR))
    }

    private fun showWheelSpin() {
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

    override fun showUpdatedPoints(text: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            val linearLayoutButtonUp: LinearLayout = findViewById(R.id.buttonUp)
            val textView: TextView = linearLayoutButtonUp.findViewById(R.id.score) // Replace 'textView' with the actual ID of your TextView
            textView.text = text
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

    @SuppressLint("SetTextI18n", "StringFormatMatches")
    private suspend fun showNumberOfAllTasks() {
        val textNum: TextView = findViewById(R.id.textNum)
        textNum.text = getString(R.string.your_tasks, controller.getAllTasks().size)
    }

    override suspend fun showAllTasks() {
        showNumberOfAllTasks()
        showDrawnTasks()

        // Launch a coroutine for the UI update
        coroutineScope {
            launch(Dispatchers.Main) {
                val taskList = findViewById<RecyclerView>(R.id.taskList)
                taskList.layoutManager = LinearLayoutManager(this@MainViewImp)
                (taskList.layoutManager as LinearLayoutManager).reverseLayout = true
                (taskList.layoutManager as LinearLayoutManager).stackFromEnd = true

                // Získejte aktuální seznam úkolů přímo z kontroléru
                val tasks = controller.getAllTasks()

                // Vytvořte nový adapter s aktuálním seznamem úkolů
                val adapter = TaskAdapter(
                    tasks.toMutableList(),
                    { selectedTask -> openTaskDetailsScreen(selectedTask) },
                    { removedTask ->
                        launch {
                            controller.removeTask(removedTask)
                        }
                    },
                    controller
                )

                taskList.adapter = adapter
            }
        }
    }

    private fun swipeToDeleteButton () {
        val taskList = findViewById<RecyclerView>(R.id.taskList)
        val drawnList = findViewById<RecyclerView>(R.id.drawnList)
        swipeHelperToDeleteAndEdit(taskList,false)
        swipeHelperToDeleteAndEdit(drawnList,true)
    }

    private fun swipeHelperToDeleteAndEdit(recyclerView: RecyclerView, enableDone: Boolean) {
        recyclerView.layoutManager = LinearLayoutManager(this@MainViewImp)

        object : SwipeHelper(this@MainViewImp, recyclerView, true) {
            override fun instantiateUnderlayButton(
                viewHolder: RecyclerView.ViewHolder?,
                underlayButtons: MutableList<UnderlayButton>?
            ) {
                // Delete Button
                underlayButtons?.add(UnderlayButton(
                    AppCompatResources.getDrawable(
                        this@MainViewImp,
                        R.drawable.ic_action_trash
                    ),
                    Color.parseColor("#FF0000"),
                    object : UnderlayButtonClickListener {
                        @SuppressLint("ClickableViewAccessibility")
                        override fun onClick(pos: Int) {
                            Toast.makeText(
                                this@MainViewImp,
                                "Delete clicked at position $pos",
                                Toast.LENGTH_SHORT
                            ).show()
                            (recyclerView.adapter as? TaskAdapter)?.removeItem(pos)
                            recyclerView.adapter?.notifyItemRemoved(pos)
                        }
                    }
                ))

                // Done Button (if enabled)
                if (enableDone) {
                    underlayButtons?.add(UnderlayButton(
                        AppCompatResources.getDrawable(
                            this@MainViewImp,
                            R.drawable.ic_action_tick
                        ),
                        Color.parseColor("#00FF00"),
                        object : UnderlayButtonClickListener {
                            @SuppressLint("ClickableViewAccessibility")
                            override fun onClick(pos: Int) {
                                Toast.makeText(
                                    this@MainViewImp,
                                    "Done clicked at position $pos",
                                    Toast.LENGTH_SHORT
                                ).show()
                                (recyclerView.adapter as? TaskAdapter)?.itemDone(pos)
                                lifecycleScope.launch {
                                    showDrawnTasks()
                                }
                            }
                        }
                    ))
                }
            }
        }
    }

    private fun openTaskDetailsScreen(task: Task) {
        val intent = Intent(this, TaskDetailsActivity::class.java)
        intent.putExtra("taskId", task.id)
        Log.d("TaskDetailsActivity", "Task ID: $taskId")
        startActivity(intent)
    }

    override fun showStatistics() {
        val linearLayoutButtonUp = findViewById<LinearLayout>(R.id.buttonUp)
        linearLayoutButtonUp.setOnClickListener {
            val intent = Intent(this, StatisticsViewImp::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("StringFormatMatches")
    override suspend fun showDrawnTasks() {
        runOnUiThread {
            lifecycleScope.launch {
                val taskList = findViewById<RecyclerView>(R.id.drawnList)
                val textNumDrawn: TextView = findViewById(R.id.textNumDrawn)
                taskList.layoutManager = LinearLayoutManager(this@MainViewImp)
                (taskList.layoutManager as LinearLayoutManager).reverseLayout = true
                (taskList.layoutManager as LinearLayoutManager).stackFromEnd = true

                // Získejte aktuální seznam úkolů přímo z kontroléru
                val allTasks = controller.getAllTasks()

                // Vytvořte nový seznam obsahující pouze úkoly ve stavu IN_PROGRESS
                val inProgressTasks = allTasks.filter { it.taskState == TaskState.IN_PROGRESS }
                textNumDrawn.text = getString(R.string.your_drawn_tasks, inProgressTasks.size)

                // Vytvořte nový adaptér s aktuálním seznamem úkolů ve stavu IN_PROGRESS
                val adapter = TaskAdapter(
                    inProgressTasks.toMutableList(),
                    { selectedTask -> openTaskDetailsScreen(selectedTask) },
                    { removedTask ->
                        launch {
                            controller.removeTask(removedTask)
                        }
                    },
                    controller
                )

                taskList.adapter = adapter
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun showSetTime() {
        val buttonSetTime = findViewById<Button>(R.id.buttonSetTime)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        buttonSetTime.setOnClickListener {
            val timePicker = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val selectedTimeInMillis = (hourOfDay * 60 + minute) * 60 * 1000L

                    GlobalScope.launch {
                        controller.stopTimer()
                        controller.setTime(selectedTimeInMillis)
                    }

                    circularProgressBar.setProgress(100)
                    //controller.startCountdownTime(countdown) //uz neni
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePicker.show()
        }
    }

    override fun showBarAndTime(progress: Int, currentCountdownTime: String) {
        circularProgressBar.setProgress(progress)
        countdownTimerTextView.text = currentCountdownTime
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun wheelAbleToTouch() {
        val wheel: ImageView = findViewById(R.id.wheel_spin)
        wheel.setOnClickListener {
            if (!controller.getIsWheelSpinning()) {
                GlobalScope.launch {
                    if (controller.getAllTasks().isNotEmpty() && controller.getTasksInStates(TaskState.AVAILABLE).isNotEmpty()) {
                        controller.setIsWheelSpinning(true)
                        showWheelSpin()
                    }
                }
            }
        }
    }
    override fun onDestroy() {
        unregisterReceiver(controller.countdownReceiver)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        launch {
            showAllTasks()
            controller.loadPointsFromDatabase()
        }
    }
}
