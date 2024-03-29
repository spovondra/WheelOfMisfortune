package com.usbapps.misfortunewheel.view

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
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
import com.usbapps.misfortunewheel.R
import com.usbapps.misfortunewheel.controller.*
import com.usbapps.misfortunewheel.custom.*
import com.usbapps.misfortunewheel.custom.guide.*
import com.usbapps.misfortunewheel.model.*
import com.usbapps.misfortunewheel.controller.MainControllerImpl
import com.usbapps.misfortunewheel.controller.StatisticsController
import com.usbapps.misfortunewheel.controller.StatisticsControllerImp
import com.usbapps.misfortunewheel.custom.BroadcastService
import com.usbapps.misfortunewheel.custom.CircularProgressBar
import com.usbapps.misfortunewheel.custom.CustomScrollView
import com.usbapps.misfortunewheel.custom.TaskAdapter
import com.usbapps.misfortunewheel.custom.guide.GuideView
import com.usbapps.misfortunewheel.custom.guide.PointerType
import com.usbapps.misfortunewheel.model.DataRepository
import com.usbapps.misfortunewheel.model.DataRepositoryImpl
import com.usbapps.misfortunewheel.model.Task
import com.usbapps.misfortunewheel.model.TaskDao
import com.usbapps.misfortunewheel.model.TaskDatabase
import com.usbapps.misfortunewheel.model.TaskModel
import com.usbapps.misfortunewheel.model.TaskModelImpl
import com.usbapps.misfortunewheel.model.TaskState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random

/**
 * Třída `MainViewImp` představuje implementaci rozhraní `MainView`.
 * Tato třída slouží k ovládání hlavní obrazovky aplikace a zobrazuje seznam úkolů,
 * statistiky a umožňuje uživateli interagovat s funkcemi aplikace.
 *
 * @constructor Inicializuje třídu `MainViewImp`.
 */
interface MainView {
    /**
     * Metoda pro zobrazení aktualizovaného počtu bodů.
     *
     * @param text Text obsahující aktualizovaný počet bodů.
     */
    fun showUpdatedPoints(text: String)

    /**
     * Suspendovaná metoda pro zobrazení všech úkolů.
     */
    suspend fun showAllTasks()

    /**
     * Metoda pro zobrazení stavu průběžného pruhu a aktuálního času odpočtu.
     *
     * @param progress Hodnota postupu pro pruhový prvek.
     * @param currentCountdownTime Aktuální čas odpočtu ve formátu textu.
     */
    fun showBarAndTime(progress: Int, currentCountdownTime: String)

    /**
     * Metoda pro posunutí obrazovky na vybraný úkol.
     */
    fun scrollToTask()

    /**
     * Metoda pro otevření obrazovky s detaily o úkolu.
     *
     * @param task Objekt reprezentující vybraný úkol.
     * @param context Kontext aktivity nebo fragmentu.
     */
    fun openTaskDetailsScreen(task: Task, context: Context)

    /**
     * Suspendovaná metoda na zobrazení nápovědy po prvním startu.
     */
    suspend fun showHelp()
}

/**
 * Implementace rozhraní `MainView` a třída reprezentující hlavní obrazovku aplikace.
 */
class MainViewImp : ComponentActivity(), MainView, CoroutineScope by MainScope() {
    private lateinit var controller: MainControllerImpl
    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var countdownTimerTextView: TextView
    private lateinit var statisticsController: StatisticsController
    private lateinit var dataRepository: DataRepository
    private lateinit var taskDao: TaskDao

    /**
     * Metoda `onCreate` se volá při vytváření aktivity. Inicializuje všechny potřebné
     * komponenty a zobrazuje hlavní obrazovku aplikace.
     *
     * @param savedInstanceState Instance stavu aktivity, pokud je dostupná.
     */
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
        GlobalScope.launch {
            showSetTime()
        }
        showTimeByUser()
        wheelAbleToTouch()
        swipeToDeleteButton()

        val newTaskButton: Button = findViewById(R.id.floatingActionButton)
        newTaskButton.setOnClickListener {
            val intent = Intent(this, NewTaskActivity::class.java)
            startActivity(intent)
        }

        registerReceiver(controller.countdownReceiver, IntentFilter(BroadcastService.COUNTDOWN_BR))
    }

    override suspend fun showHelp() {
        lifecycleScope.launch {
            val newTaskButton: Button = findViewById(R.id.floatingActionButton)
            val buttonSetTime = findViewById<Button>(R.id.buttonSetTime)
            val itemTask = findViewById<LinearLayout>(R.id.itemTask)
            val linearLayoutButtonUp = findViewById<LinearLayout>(R.id.buttonUp)

            val displayHeight = Resources.getSystem().displayMetrics.heightPixels

            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            // Načtení hodnoty helpCounter z SharedPreferences
            val helpCounter = sharedPreferences.getInt("helpCounter", 0)

            if (helpCounter == 0 && controller.getAllTasks().isEmpty()) {
                buildGuideView(
                    newTaskButton,
                    getString(R.string.guide_1_add_task),
                    (displayHeight * 0.1).toFloat()
                )
                editor.putInt("helpCounter", 1) // Uložení hodnoty helpCounter do SharedPreferences
                editor.apply()
            }
            if (helpCounter == 1 && controller.getAllTasks().size == 1) {
                buildGuideView(
                    buttonSetTime,
                    getString(R.string.guide_2_set_time),
                    (displayHeight * 0.07).toFloat()
                )
                editor.putInt(
                    "helpCounter",
                    helpCounter + 1
                ) // Uložení hodnoty helpCounter do SharedPreferences
                editor.apply()
            }
            if (helpCounter == 2 && controller.getAllTasks().size == 1) {
                buildGuideView(
                    countdownTimerTextView,
                    getString(R.string.guide_3_spin_the_wheel),
                    (displayHeight * 0.05).toFloat()
                )
                editor.putInt(
                    "helpCounter",
                    helpCounter + 1
                ) // Uložení hodnoty helpCounter do SharedPreferences
                editor.apply()
            }
            if (helpCounter == 3 && controller.getTasksInStates(TaskState.IN_PROGRESS).size == 1) {
                buildGuideView(
                    itemTask,
                    getString(R.string.guide_4_task_open_swipe),
                    (displayHeight * 0.1).toFloat()
                )
                editor.putInt(
                    "helpCounter",
                    helpCounter + 1
                ) // Uložení hodnoty helpCounter do SharedPreferences
                editor.apply()
            }
            if (helpCounter == 4 && controller.getTasksInStates(TaskState.DONE).size == 1) {
                scrollUp()
                delay(200)
                buildGuideView(
                    linearLayoutButtonUp,
                    getString(R.string.guide_5_show_statistics),
                    (displayHeight * 0.15).toFloat()
                )
                editor.putInt(
                    "helpCounter",
                    helpCounter + 1
                ) // Uložení hodnoty helpCounter do SharedPreferences
                editor.apply()
            }
            if (helpCounter == 5) {
                scrollToTask()
                editor.putInt(
                    "helpCounter",
                    helpCounter + 1
                ) // Uložení hodnoty helpCounter do SharedPreferences
                editor.apply()
                delay(300)
            }
            Log.d("helpCounter", helpCounter.toString())
        }
    }

    private fun buildGuideView(targetView: View?, title: String, indicatorHeight: Float) {
        val guideView = GuideView.Builder(this)
            .setTitle(title)
            //.setContentText(content)
            .setTargetView(targetView)
            .setContentTextSize(0) // optional
            .setTitleTextSize(30) // optional
            .setPointerType(PointerType.None)
            .setIndicatorHeight(indicatorHeight)
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
        textNum.text = getString(
            R.string.all_tasks,
            controller.getAllTasks().filter
            { it.taskState != TaskState.DELETED && it.taskState != TaskState.DONE}.size
        )
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

                // Získejte aktuální seznam úkolů přímo z kontroléru s filtrováním podle taskState
                val tasks = controller.getAllTasks().filter { it.taskState != TaskState.DELETED && it.taskState != TaskState.DONE}

                updateUIVisibility() // změna zobrazovaných prvků na základě požadavků

                // Vytvořte nový adaptér s aktuálním seznamem úkolů
                val adapter = TaskAdapter(
                    tasks.toMutableList(),
                    { selectedTask -> openTaskDetailsScreen(selectedTask, this@MainViewImp) },
                    controller,
                )

                taskList.adapter = adapter
            }
        }
    }

    // Akce po provedení swipu na úloze
    private fun swipeToDeleteButton() {
        val taskList = findViewById<RecyclerView>(R.id.taskList)
        val drawnList = findViewById<RecyclerView>(R.id.drawnList)
        controller.swipeHelperToDeleteAndEdit(taskList, false, this)
        controller.swipeHelperToDeleteAndEdit(drawnList, true, this)
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

                // Nový seznam obsahující pouze úkoly ve stavu IN_PROGRESS a není DELETED, seřazený podle endTime
                val inProgressTasks = controller.getTasksInStates(TaskState.IN_PROGRESS)
                    .filter { it.taskState != TaskState.DELETED }
                    .sortedBy { it.endTime }

                textNumDrawn.text = getString(R.string.drawn_tasks, inProgressTasks.size)

                // Vytvořte nový adaptér s aktuálním seznamem úkolů ve stavu IN_PROGRESS
                val adapter = TaskAdapter(
                    inProgressTasks.toMutableList(),
                    { selectedTask -> openTaskDetailsScreen(selectedTask, this@MainViewImp) },
                    controller,
                )

                taskList.adapter = adapter
            }
        }
    }


    /**
     * Metoda na zobrazování/skrývání prvků na obrazovce podle aktuálního dění
     */
    @SuppressLint("StringFormatMatches")
    suspend fun updateUIVisibility() {
        runOnUiThread {
            lifecycleScope.launch {
                val taskList = findViewById<RecyclerView>(R.id.taskList)
                val textNum: TextView = findViewById(R.id.textNum)
                val drawnList = findViewById<RecyclerView>(R.id.drawnList)
                val textNumDrawn: TextView = findViewById(R.id.textNumDrawn)
                val drawnSpace: View = findViewById(R.id.drawnSpace)

                val tasks = controller.getAllTasks().filter {it.taskState != TaskState.DELETED && it.taskState != TaskState.DONE}

                // Vytvořit nový seznam obsahující pouze úkoly ve stavu IN_PROGRESS
                val inDrawnTasks = tasks.filter { it.taskState == TaskState.IN_PROGRESS }

                if (tasks.isEmpty() && inDrawnTasks.isEmpty()) {
                    // Skrýt taskList, textNum a tocteTextView
                    taskList.visibility = View.GONE
                    textNum.visibility = View.GONE
                    // Skrýt drawnList, textNumDrawn a drawnTasksTextView
                    drawnList.visibility = View.GONE
                    textNumDrawn.visibility = View.GONE
                } else {
                    taskList.visibility = View.VISIBLE
                    textNum.visibility = View.VISIBLE

                    if (inDrawnTasks.isEmpty()) {
                        drawnList.visibility = View.GONE
                        textNumDrawn.visibility = View.GONE
                        drawnSpace.visibility = View.GONE
                    } else {
                        drawnList.visibility = View.VISIBLE
                        textNumDrawn.visibility = View.VISIBLE
                        drawnSpace.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    // Metoda na zobrazení dialogu s nastavením času
    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun showSetTime() {
        val buttonSetTime = findViewById<Button>(R.id.buttonSetTime)
        val calendar = Calendar.getInstance()
        if(controller.getAllTasks().isEmpty()) {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 1)
        }
        else {
            val hours = controller.getTimeSetByUserInTriple().first
            val minutes = controller.getTimeSetByUserInTriple().second
            calendar.set(Calendar.HOUR_OF_DAY, hours.toInt())
            calendar.set(Calendar.MINUTE, minutes.toInt())
        }
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
                lifecycleScope.launch {
                    showHelp()
                    showTimeByUser()
                }
            }
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun showTimeByUser() {
        lifecycleScope.launch {
            val changeNotificationTime = findViewById<TextView>(R.id.buttonSetTime)

            val hours = controller.getTimeSetByUserInTriple().first
            val minutes = controller.getTimeSetByUserInTriple().second

            val selectedTime = buildString {
                if (hours > 0) {
                    append("$hours" + "h")
                    if (minutes > 0) {
                        append(" ")
                    }
                }
                if (minutes > 0) {
                    append("$minutes" + "m")
                }
            }

            if (controller.getTimeSetByUser().toInt() == 0) {
                changeNotificationTime.text = getString(R.string.set_notification_time)
            } else {
                changeNotificationTime.text =
                    getString(R.string.change_notification_time, selectedTime)
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
                    if (controller.getAllTasks().isNotEmpty() && controller.getTasksInStates(
                            TaskState.AVAILABLE
                        ).isNotEmpty()
                    ) {
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

        // Faktor pro zmenšení velikostí na tabletech
        val tabletScalingFactor = 0.7f

        // Výpočet rozměrů na základě poměrů a aplikace faktoru zvětšení nebo zmenšení
        val circularProgressBarSize = (displayMetrics.widthPixels * 0.807 * scalingFactor).toInt()
        val wheelSpinSize = (displayMetrics.widthPixels * 0.7083 * scalingFactor).toInt()
        val wheelStaticSize = (displayMetrics.widthPixels * 0.9 * scalingFactor).toInt()
        val countdownTimerTextSize = (20 + displayMetrics.widthPixels / 70)

        // Pokud je zařízení tablet, použijte zmenšený faktor
        val scaleFactor = if (isTablet()) tabletScalingFactor else 1.0f

        // Nastavení vypočítaných rozměrů pro jednotlivé pohledy
        val circularProgressBar = findViewById<CircularProgressBar>(R.id.circularProgressBar)
        val wheelSpin = findViewById<ImageView>(R.id.wheel_spin)
        val wheelStatic = findViewById<ImageView>(R.id.wheel_static)
        val countdownTimerTextView = findViewById<TextView>(R.id.countdownTimerTextView)

        // Nastavení rozměrů pro kruhový průběh
        circularProgressBar.layoutParams.width = (circularProgressBarSize * scaleFactor).toInt()
        circularProgressBar.layoutParams.height = (circularProgressBarSize * scaleFactor).toInt()

        // Nastavení rozměrů pro otáčivý obrázek
        wheelSpin.layoutParams.width = (wheelSpinSize * scaleFactor).toInt()
        wheelSpin.layoutParams.height = (wheelSpinSize * scaleFactor).toInt()

        // Nastavení rozměrů pro statický obrázek
        wheelStatic.layoutParams.width = (wheelStaticSize * scaleFactor).toInt()
        wheelStatic.layoutParams.height = (wheelStaticSize * scaleFactor).toInt()

        // Nastavení velikosti textu pro odpočítávací časovač
        countdownTimerTextView.height = (countdownTimerTextSize * scaleFactor * 10).toInt()
        countdownTimerTextView.width = (countdownTimerTextSize * scaleFactor * 10).toInt()
        countdownTimerTextView.textSize = (countdownTimerTextSize * scaleFactor)
    }

    // Funkce pro zjištění, zda je zařízení tablet
    private fun isTablet(): Boolean {
        val configuration = resources.configuration
        return configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }


    // Metoda na zascrollování na vylosovanou úlohu
    override fun scrollToTask() {
        val scrollView: CustomScrollView = findViewById(R.id.scrollView)
        scrollView.post {
            scrollView.scrollToChild(1000)
        }
    }

    private fun scrollUp() {
        val scrollView: CustomScrollView = findViewById(R.id.scrollView)
        scrollView.post {
            scrollView.scrollToChild(0)
        }
    }

    /**
     * Metoda na zrušení časovače
     */
    override fun onDestroy() {
        unregisterReceiver(controller.countdownReceiver)
        super.onDestroy()
    }

    /**
     * Metoda na zovunačtení dat po navrácení se z jiné obrazovky
     */
    override fun onResume() {
        super.onResume()

        launch {
            showAllTasks()
            controller.loadPointsFromDatabase()
            showHelp()
        }
    }
}
