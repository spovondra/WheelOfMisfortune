package com.usbapps.misfortunewheel.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.misfortuneapp.wheelofmisfortune.R
import com.usbapps.misfortunewheel.controller.NewTakController
import com.usbapps.misfortunewheel.controller.NewTaskControllerImpl
import com.usbapps.misfortunewheel.model.Task
import com.usbapps.misfortunewheel.model.TaskState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Aktivita pro vytváření nebo úpravu úkolů.
 */
class NewTaskActivity : AppCompatActivity() {

    private lateinit var mainView: MainView
    private lateinit var notification: Notification
    private lateinit var newTaskController: NewTakController
    private var selectedIconResId: Int = R.drawable.icon
    private var selectedImageView: ImageView? = null
    private var isTaskCreated: Boolean = false
    private lateinit var taskPriority: SeekBar
    private lateinit var textViewProgress: TextView
    private lateinit var taskNameEditText: EditText
    private lateinit var taskDescriptionEditText: EditText
    private var currentTask: Task? = null
    private var taskName: String? = null
    private var taskId: Int = 0
    private var newTaskId: Int = 0
    private var taskIdFromIntent = 0

    /**
     * Inicializuje aktivitu pro vytváření nebo úpravu úkolů.
     */
    @SuppressLint("InflateParams", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)

        // Inicializace instancí
        mainView = MainViewImp()
        notification = NotificationHandler(this)
        newTaskController = NewTaskControllerImpl(this)

        lifecycleScope.launch {
            taskIdFromIntent = intent.getIntExtra("taskId", -1)

            if (taskIdFromIntent != -1) {
                // Otevři existující úkol
                currentTask = newTaskController.getTaskById(taskIdFromIntent)
                taskName = currentTask?.title
                taskId = currentTask?.id ?: 0
                newTaskId = newTaskController.getAllTasks().size // můžete upravit podle potřeby
                updateActivityTitle()

                taskNameEditText.setText(taskName)
                taskDescriptionEditText.setText(currentTask?.description)
                taskPriority.progress = currentTask?.priority ?: 0

                // Nastav ikonu podle aktuálního úkolu
                selectedIconResId = currentTask?.iconResId ?: R.drawable.icon

                // Uprav ikonu v závislosti na aktuálním výběru
                selectedImageView?.isSelected = false
                when (selectedIconResId) {
                    R.drawable.ic_action_cart -> selectedImageView = findViewById(R.id.icon1)
                    R.drawable.ic_action_book -> selectedImageView = findViewById(R.id.icon2)
                    R.drawable.ic_action_bell -> selectedImageView = findViewById(R.id.icon3)
                    R.drawable.ic_action_box -> selectedImageView = findViewById(R.id.icon4)
                }
                selectedImageView?.isSelected = true
            } else {
                // Vytvoř nový úkol
                val allTasks =
                    newTaskController.getAllTasks().filter { it.taskState != TaskState.DELETED }
                if (allTasks.isNotEmpty()) {
                    taskId = allTasks.last().id + 1
                    newTaskId = allTasks.size + 1
                } else {
                    taskId = 1
                    newTaskId = 1
                }
                updateActivityTitle()
            }
        }

        taskPriority = findViewById(R.id.seekBarPriority)
        textViewProgress = findViewById(R.id.textViewProgress)
        taskNameEditText = findViewById(R.id.editTextTaskName)
        taskDescriptionEditText = findViewById(R.id.editTextTaskDescription)

        // Nastavení posluchače změn textu pro název úkolu
        taskNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                lifecycleScope.launch {
                    delay(500)
                    updateActivityTitle()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Zpožděte uložení o 500 milisekund, abyste zachytili nepřetržité změny
                lifecycleScope.launch {
                    delay(500)
                    saveTask()
                }
            }
        })

        // Nastavení posluchače změn textu pro popis úkolu
        taskDescriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Zpožděte uložení o 500 milisekund, abyste zachytili nepřetržité změny
                lifecycleScope.launch {
                    delay(500)
                    saveTask()
                }
            }
        })

        // Nastavení posluchače změn seek baru pro prioritu úkolu
        taskPriority.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("StringFormatMatches")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress < 1) {
                    seekBar?.progress = 1
                }
                textViewProgress.text = getString(R.string.selected_priority, seekBar?.progress)
                saveTask()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val finishButton: Button = findViewById(R.id.finishButton)
        finishButton.setOnClickListener {
            finish()
        }

        // Nastavení posluchačů kliknutí pro ikony
        val icon1: ImageView = findViewById(R.id.icon1)
        val icon2: ImageView = findViewById(R.id.icon2)
        val icon3: ImageView = findViewById(R.id.icon3)
        val icon4: ImageView = findViewById(R.id.icon4)

        icon1.setOnClickListener { onIconClick(icon1) }
        icon2.setOnClickListener { onIconClick(icon2) }
        icon3.setOnClickListener { onIconClick(icon3) }
        icon4.setOnClickListener { onIconClick(icon4) }

        // Nastavení posluchače kliknutí pro tlačítko vlastního panelu akcí
        val customActionBarButton =
            layoutInflater.inflate(R.layout.custom_action_bar_button, FrameLayout(this))
        supportActionBar?.customView = customActionBarButton
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.elevation = 0f

        customActionBarButton.findViewById<CardView>(R.id.action_delete_task).setOnClickListener {
            if (currentTask != null) {
                lifecycleScope.launch {
                    currentTask?.let { deleteTask(it) }
                }
            } else {
                finish()
            }
        }

        // Nastavení posluchače kliknutí pro tlačítko zpět
        customActionBarButton.findViewById<CardView>(R.id.action_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        lifecycleScope.launch {
            updateActivityTitle()
        }
    }

    /**
     * Aktualizuje název aktivity podle stavu úkolu.
     */
    internal fun updateActivityTitle() {
        val activityTitleTextView: TextView = findViewById(R.id.activityTitleTextView)
        val finishLayout: FrameLayout = findViewById(R.id.finishLayout)

        if (taskName.isNullOrBlank()) {
            activityTitleTextView.text = getString(R.string.new_task_activity, "$newTaskId")
            finishLayout.visibility = View.GONE
        } else {
            activityTitleTextView.text = getString(R.string.edit_task)
            finishLayout.visibility = View.VISIBLE
        }
    }

    /**
     * Obsluha kliknutí na ikonu.
     */
    private fun onIconClick(imageView: ImageView) {
        selectedImageView?.isSelected = false
        imageView.isSelected = true
        selectedImageView = imageView

        selectedIconResId = when (imageView.id) {
            R.id.icon1 -> R.drawable.ic_action_cart
            R.id.icon2 -> R.drawable.ic_action_book
            R.id.icon3 -> R.drawable.ic_action_bell
            R.id.icon4 -> R.drawable.ic_action_box
            else -> R.drawable.icon
        }

        saveTask()
    }

    /**
     * Uloží úkol.
     */
    internal fun saveTask() {
        if (!isTaskCreated) {
            taskName = taskNameEditText.text.toString()
            val taskDescription = taskDescriptionEditText.text.toString()

            if ((taskName ?: return).isNotBlank()) {
                isTaskCreated = true

                lifecycleScope.launch {
                    val existingTask = currentTask?.let { newTaskController.getTaskById(it.id) }

                    if (existingTask != null) {
                        existingTask.title = taskName as String
                        existingTask.description = taskDescription
                        existingTask.priority = taskPriority.progress
                        existingTask.iconResId = selectedIconResId
                        newTaskController.updateTask(existingTask)
                    } else {
                        updateActivityTitle()

                        newTaskController.addNewTask(
                            title = taskName ?: return@launch,
                            description = taskDescription,
                            priority = taskPriority.progress,
                            iconResId = selectedIconResId
                        )

                        // Nastav aktuální úkol
                        currentTask =
                            newTaskController.getTaskById(newTaskController.getAllTasks().last().id)
                    }

                    isTaskCreated = false
                }
            }
        }
    }

    /**
     * Odstraní úkol.
     */
    private fun deleteTask(task: Task) {
        lifecycleScope.launch {
            newTaskController.removeTask(task)
            finish() // nebo naviguj na jinou obrazovku podle potřeby
        }
    }
}
