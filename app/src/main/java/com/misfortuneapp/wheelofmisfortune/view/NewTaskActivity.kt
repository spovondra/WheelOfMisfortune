package com.misfortuneapp.wheelofmisfortune.view

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
import com.misfortuneapp.wheelofmisfortune.controller.NewTakController
import com.misfortuneapp.wheelofmisfortune.controller.NewTaskControllerImpl
import com.misfortuneapp.wheelofmisfortune.model.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    @SuppressLint("InflateParams", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)

        // Initialize instances
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
                val allTasks = newTaskController.getAllTasks()
                if (allTasks.isNotEmpty()) {
                    taskId = allTasks.last().id + 1
                    newTaskId = newTaskController.getAllTasks().size + 1
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

        // Set up text change listener for task name
        taskNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                lifecycleScope.launch {
                    delay(500)
                    updateActivityTitle()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Delay the saving by 500 milliseconds to capture continuous changes
                lifecycleScope.launch {
                    delay(500)
                    saveTask()
                }
            }
        })

        // Set up text change listener for task description
        taskDescriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Delay the saving by 500 milliseconds to capture continuous changes
                lifecycleScope.launch {
                    delay(500)
                    saveTask()
                }
            }
        })

        // Set up seek bar change listener for task priority
        taskPriority.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textViewProgress.text = "Selected Progress: $progress"
                saveTask()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val finishButton: Button = findViewById(R.id.finishButton)
        finishButton.setOnClickListener {
            finish()
        }

        // Set up click listeners for icons
        val icon1: ImageView = findViewById(R.id.icon1)
        val icon2: ImageView = findViewById(R.id.icon2)
        val icon3: ImageView = findViewById(R.id.icon3)
        val icon4: ImageView = findViewById(R.id.icon4)

        icon1.setOnClickListener { onIconClick(icon1) }
        icon2.setOnClickListener { onIconClick(icon2) }
        icon3.setOnClickListener { onIconClick(icon3) }
        icon4.setOnClickListener { onIconClick(icon4) }

        // Set up custom action bar button click listener
        val customActionBarButton = layoutInflater.inflate(R.layout.custom_action_bar_button, FrameLayout(this))
        supportActionBar?.customView = customActionBarButton
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.elevation = 0f

        customActionBarButton.findViewById<Button>(R.id.action_delete_task).setOnClickListener {
            if (currentTask != null) {
                lifecycleScope.launch {
                    currentTask?.let { deleteTask(it) }
                }
            }
            else {
                finish()
            }
        }

        // Set up back button click listener
        customActionBarButton.findViewById<CardView>(R.id.action_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        lifecycleScope.launch {
            updateActivityTitle()
        }
    }

    private fun updateActivityTitle() {
        val activityTitleTextView: TextView = findViewById(R.id.activityTitleTextView)
        val finishLayout: FrameLayout = findViewById(R.id.finishLayout)

        if (taskName.isNullOrBlank()) {
            activityTitleTextView.text = getString(R.string.new_task_activity,"$newTaskId")
            finishLayout.visibility = View.GONE
        } else {
            activityTitleTextView.text  = getString(R.string.task_edit)
            finishLayout.visibility = View.VISIBLE
        }
    }

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

    private fun saveTask() {
        if (!isTaskCreated) {
            taskName = taskNameEditText.text.toString()
            val taskDescription = taskDescriptionEditText.text.toString()

            if (taskName!!.isNotBlank()) {
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
                            title = taskName!!,
                            description = taskDescription,
                            priority = taskPriority.progress,
                            iconResId = selectedIconResId
                        )

                        // Nastav aktuální úkol
                        currentTask = newTaskController.getTaskById(newTaskController.getAllTasks().last().id)
                    }

                    isTaskCreated = false
                }
            }
        }
    }

    private fun deleteTask(task: Task) {
        lifecycleScope.launch {
            newTaskController.removeTask(task)
            finish() // nebo naviguj na jinou obrazovku podle potřeby
        }
    }
}
