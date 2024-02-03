package com.misfortuneapp.wheelofmisfortune.view

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.misfortuneapp.wheelofmisfortune.R
import com.misfortuneapp.wheelofmisfortune.controller.*
import com.misfortuneapp.wheelofmisfortune.custom.*
import com.misfortuneapp.wheelofmisfortune.custom.GuideView.GuideView
import com.misfortuneapp.wheelofmisfortune.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random

interface MainView {
    fun showUpdatedPoints(text: String)
    suspend fun showAllTasks()
    fun showBarAndTime(progress: Int, currentCountdownTime: String)
    fun scrollToTask()
    fun openTaskDetailsScreen(task: Task, context: Context)
}

class MainViewImp : ComponentActivity(), MainView, CoroutineScope by MainScope() {
    private lateinit var controller: MainControllerImpl
    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var countdownTimerTextView: TextView
    private lateinit var statisticsController: StatisticsController
    private lateinit var dataRepository: DataRepository
    private lateinit var taskDao: TaskDao
    private var helpCounter = 0

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val taskList = findViewById<RecyclerView>(R.id.taskList)
        taskList.layoutManager = LinearLayoutManager(this@MainViewImp)

        // Initialize DataRepository
        dataRepository = DataRepositoryImpl(this)
        taskDao = TaskDatabase.getDatabase(this).taskDao()

        circularProgressBar = findViewById(R.id.circularProgressBar)
        countdownTimerTextView = findViewById(R.id.countdownTimerTextView)

        val taskRepository: TaskModel = TaskModelImpl(this)
        statisticsController = StatisticsControllerImp(dataRepository, StatisticsViewImp())

        controller = MainControllerImpl(this, this, taskRepository, statisticsController)

        //controller.startCountdownTime(10)
        controller.loadPointsFromDatabase()

        GlobalScope.launch {
            controller.getTime().let { controller.startTimer(it.id) }
        }

        setViewSizesBasedOnScreen()
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

    private suspend fun showHelp() {
        val newTaskButton: Button = findViewById(R.id.floatingActionButton)
        val buttonSetTime = findViewById<Button>(R.id.buttonSetTime)
        val drawnList = findViewById<RecyclerView>(R.id.drawnList)

        Log.d("counter", helpCounter.toString())
        if (helpCounter == 0 && controller.getAllTasks().isEmpty()) {
            buildGuideView (newTaskButton, "Přidej úlohu")
        }
        if (helpCounter == 1 && controller.getAllTasks().size == 1) {
            buildGuideView (buttonSetTime, "Nastav čas upozornění")
        }
        if (helpCounter == 3 && controller.getAllTasks().size == 1) {
            buildGuideView (countdownTimerTextView, "Zatoč si kolečkem!")
        }
    }

    private fun buildGuideView (targetView: View?, title: String) {
        val guideView = GuideView.Builder(this)
            .setTitle(title)
            //.setContentText(content)
            .setTargetView(targetView)
            .setContentTextSize(12) // optional
            .setTitleTextSize(14) // optional

        guideView.setGuideListener {
            helpCounter++
        }

        guideView.build().show()
    }

    // Metoda na zobrazení animace otáčení kolečka
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

    // Metoda na zobrazení a aktulizaci počtu splněných úloh
    override fun showUpdatedPoints(text: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            val linearLayoutButtonUp: LinearLayout = findViewById(R.id.buttonUp)
            val textView: TextView = linearLayoutButtonUp.findViewById(R.id.score)
            textView.text = text
            showAllTasks()
            showStatistics()
        }
    }

    // Metoda na zobrazení všeh úloh
    @SuppressLint("SetTextI18n", "StringFormatMatches")
    private suspend fun showNumberOfAllTasks() {
        val textNum: TextView = findViewById(R.id.textNum)
        textNum.text = getString(R.string.your_tasks, controller.getAllTasks().size)
    }

    // Metoda na zobrazní všech úloh v RecyclerView
    override suspend fun showAllTasks() {
        showNumberOfAllTasks()
        showDrawnTasks()

        coroutineScope {
            launch(Dispatchers.Main) {
                val taskList = findViewById<RecyclerView>(R.id.taskList)
                taskList.layoutManager = LinearLayoutManager(this@MainViewImp)
                (taskList.layoutManager as LinearLayoutManager).reverseLayout = true
                (taskList.layoutManager as LinearLayoutManager).stackFromEnd = true

                // Získejte aktuální seznam úkolů přímo z kontroléru
                val tasks = controller.getAllTasks()

                updateUIVisibility() // změna zobrazovaných prvků na základě požadavků

                // Vytvořte nový adapter s aktuálním seznamem úkolů
                val adapter = TaskAdapter(
                    tasks.toMutableList(),
                    { selectedTask -> openTaskDetailsScreen(selectedTask, this@MainViewImp) },
                    { removedTask ->
                        launch {
                            controller.removeTask(removedTask,true)
                        }
                    },
                    controller,
                    true
                )

                taskList.adapter = adapter
            }
        }
    }

    // Akce po provedení swipu na úloze
    private fun swipeToDeleteButton () {
        val taskList = findViewById<RecyclerView>(R.id.taskList)
        val drawnList = findViewById<RecyclerView>(R.id.drawnList)
        controller.swipeHelperToDeleteAndEdit(taskList,false, this)
        controller.swipeHelperToDeleteAndEdit(drawnList,true, this)
    }

    // Metoda na zobrazení detailů o úloze
    override fun openTaskDetailsScreen(task: Task, context: Context) {
        val intent = Intent(context, NewTaskActivity::class.java)
        intent.putExtra("taskId", task.id)
        context.startActivity(intent)
    }

    // Metoda na zobrazení statistik
    private fun showStatistics() {
        val linearLayoutButtonUp = findViewById<LinearLayout>(R.id.buttonUp)
        linearLayoutButtonUp.setOnClickListener {
            val intent = Intent(this, StatisticsViewImp::class.java)
            startActivity(intent)
        }
    }

    // Metoda na zobrazení vylosvaných úloh
    @SuppressLint("StringFormatMatches")
    private suspend fun showDrawnTasks() {
        runOnUiThread {
            lifecycleScope.launch {
                val taskList = findViewById<RecyclerView>(R.id.drawnList)
                val textNumDrawn: TextView = findViewById(R.id.textNumDrawn)
                taskList.layoutManager = LinearLayoutManager(this@MainViewImp)
                (taskList.layoutManager as LinearLayoutManager).reverseLayout = true
                (taskList.layoutManager as LinearLayoutManager).stackFromEnd = true

                // Vytvořte nový seznam obsahující pouze úkoly ve stavu IN_PROGRESS
                val inProgressTasks = controller.getTasksInStates(TaskState.IN_PROGRESS)
                textNumDrawn.text = getString(R.string.your_drawn_tasks, inProgressTasks.size)

                // Vytvořte nový adaptér s aktuálním seznamem úkolů ve stavu IN_PROGRESS
                val adapter = TaskAdapter(
                    inProgressTasks.toMutableList(),
                    { selectedTask -> openTaskDetailsScreen(selectedTask, this@MainViewImp) },
                    { removedTask ->
                        launch {
                            controller.removeTask(removedTask, true)
                        }
                    },
                    controller,
                    true
                )

                taskList.adapter = adapter
            }
        }
    }

    // Metoda na zobrazování/skrývání prvků na obrazovce podle aktuálního dění
    @SuppressLint("StringFormatMatches")
    suspend fun updateUIVisibility() {
        runOnUiThread {
            lifecycleScope.launch {
                val taskList = findViewById<RecyclerView>(R.id.taskList)
                val textNum: TextView = findViewById(R.id.textNum)
                val noTasksTextView = findViewById<LinearLayout>(R.id.noTasksTextView)
                val drawnList = findViewById<RecyclerView>(R.id.drawnList)
                val textNumDrawn: TextView = findViewById(R.id.textNumDrawn)
                val drawnTasksTextView = findViewById<LinearLayout>(R.id.noDrawnTasksTextView)

                // Získejte aktuální seznam úkolů přímo z kontroléru
                val tasks = controller.getAllTasks()
                val allTasks = controller.getAllTasks()

                // Vytvořte nový seznam obsahující pouze úkoly ve stavu IN_PROGRESS
                val inDrawnTasks = allTasks.filter { it.taskState == TaskState.IN_PROGRESS }

                if (tasks.isEmpty() && inDrawnTasks.isEmpty()) {
                    // Skrýt taskList, textNum a tocteTextView
                    taskList.visibility = View.GONE
                    textNum.visibility = View.GONE
                    //noTasksTextView.visibility = View.VISIBLE //......... odebrat
                    // Skrýt drawnList, textNumDrawn a drawnTasksTextView
                    drawnList.visibility = View.GONE
                    textNumDrawn.visibility = View.GONE
                    drawnTasksTextView.visibility = View.GONE
                } else {
                    taskList.visibility = View.VISIBLE
                    textNum.visibility = View.VISIBLE
                    noTasksTextView.visibility = View.GONE

                    if (inDrawnTasks.isEmpty()) {
                        drawnList.visibility = View.GONE
                        textNumDrawn.visibility = View.GONE
                        drawnTasksTextView.visibility = View.VISIBLE
                    } else {
                        drawnList.visibility = View.VISIBLE
                        textNumDrawn.visibility = View.VISIBLE
                        drawnTasksTextView.visibility = View.GONE
                    }
                }
            }
        }
    }

    // Metoda na zobrazení dialogu s nastavením času
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
            timePicker.setOnDismissListener {
                helpCounter++
                lifecycleScope.launch {
                    showHelp()
                }
            }
        }
    }

    // Metoda na zobrazení progressbaru a časovače
    override fun showBarAndTime(progress: Int, currentCountdownTime: String) {
        circularProgressBar.setProgress(progress)
        countdownTimerTextView.text = currentCountdownTime
    }

    // Metoda na kontrolu, zda skončil časovač a jsou k dispozici úlohy, aby se mohlo kolečko roztočit
    @OptIn(DelicateCoroutinesApi::class)
    private fun wheelAbleToTouch() {
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

    // Metoda na dynamické nastavení velikostí tlačítek (mazání/splnění úlohy) na základě displeje
    private fun setViewSizesBasedOnScreen() {
        // Získání informací o displeji
        val displayMetrics = Resources.getSystem().displayMetrics

        // Faktor pro zvětšení velikostí o 20%
        val scalingFactor = 1.2f

        // Výpočet rozměrů na základě poměrů a aplikace faktoru zvětšení
        val circularProgressBarSize = (displayMetrics.widthPixels * 0.807 * scalingFactor).toInt()
        val wheelSpinSize = (displayMetrics.widthPixels * 0.7083 * scalingFactor).toInt()
        val wheelStaticSize = (displayMetrics.widthPixels * 0.9 * scalingFactor).toInt()
        val countdownTimerTextSize = (20 + displayMetrics.widthPixels/70)

        // Nastavení vypočítaných rozměrů pro jednotlivé pohledy
        val circularProgressBar = findViewById<CircularProgressBar>(R.id.circularProgressBar)
        val wheelSpin = findViewById<ImageView>(R.id.wheel_spin)
        val wheelStatic = findViewById<ImageView>(R.id.wheel_static)
        val countdownTimerTextView = findViewById<TextView>(R.id.countdownTimerTextView)

        // Nastavení rozměrů pro kruhový průběh
        circularProgressBar.layoutParams.width = circularProgressBarSize
        circularProgressBar.layoutParams.height = circularProgressBarSize

        // Nastavení rozměrů pro otáčivý obrázek
        wheelSpin.layoutParams.width = wheelSpinSize
        wheelSpin.layoutParams.height = wheelSpinSize

        // Nastavení rozměrů pro statický obrázek
        wheelStatic.layoutParams.width = wheelStaticSize
        wheelStatic.layoutParams.height = wheelStaticSize

        // Nastavení velikosti textu pro odpočítávací časovač
        countdownTimerTextView.textSize = countdownTimerTextSize.toFloat()
    }

    // Metoda na zascrollování na vylosovanou úlohu
    override fun scrollToTask () {
        val scrollView: CustomScrollView = findViewById(R.id.scrollView)
        scrollView.post {
            scrollView.scrollToChild(1000)
        }
    }

    // Metoda na zrušení časovače
    override fun onDestroy() {
        unregisterReceiver(controller.countdownReceiver)
        super.onDestroy()
    }

    // Metoda na zovunačtení dat po navrácení se z jiné obrazovky
    override fun onResume() {
        super.onResume()

        launch {
            showAllTasks()
            controller.loadPointsFromDatabase()
            showHelp()
        }
    }
}
