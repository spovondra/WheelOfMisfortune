package com.misfortuneapp.wheelofmisfortune.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.misfortuneapp.wheelofmisfortune.R
import com.misfortuneapp.wheelofmisfortune.model.Task
import com.misfortuneapp.wheelofmisfortune.model.TaskModel
import com.misfortuneapp.wheelofmisfortune.model.TaskModelImpl
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewTaskActivity : AppCompatActivity() {

    private lateinit var mainView: MainView
    private lateinit var notification: Notification
    private lateinit var taskModel: TaskModel
    private var selectedIconResId: Int = R.drawable.icon
    private var selectedImageView: ImageView? = null
    private var addTaskJob: Job? = null
    private var isTaskCreated: Boolean = false
    private lateinit var taskPriority: SeekBar
    private lateinit var textViewProgress: TextView
    private lateinit var taskNameEditText: EditText
    private lateinit var taskDescriptionEditText: EditText

    @SuppressLint("InflateParams")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)

        // Initialize instances
        mainView = MainViewImp()
        notification = NotificationHandler(this)
        taskModel = TaskModelImpl(this)

        taskPriority = findViewById(R.id.seekBarPriority)
        textViewProgress = findViewById(R.id.textViewProgress)
        taskNameEditText = findViewById(R.id.editTextTaskName)
        taskDescriptionEditText = findViewById(R.id.editTextTaskDescription)

        // Set up text change listener for task name
        taskNameEditText.addTextChangedListener(object : TextWatcher {
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
        val customActionBarButton = layoutInflater.inflate(R.layout.custom_action_bar_button, null)
        supportActionBar?.customView = customActionBarButton
        supportActionBar?.setDisplayShowCustomEnabled(true)

        customActionBarButton.findViewById<Button>(R.id.action_delete_task).setOnClickListener {

        }

        lifecycleScope.launch {
            updateActivityTitle(taskModel.getAllTasks().size.toLong() + 1)
        }
    }

    private fun updateActivityTitle(newTaskId: Long) {
        val activityTitleTextView: TextView = findViewById(R.id.activityTitleTextView)
        activityTitleTextView.text = getString(R.string.new_task_activity, newTaskId)
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
            else -> R.drawable.ic_launcher_foreground
        }

        saveTask()
    }

    private fun saveTask() {
        if (!isTaskCreated) {
            val taskName = taskNameEditText.text.toString()
            val taskDescription = taskDescriptionEditText.text.toString()

            if (taskName.isNotBlank() && taskDescription.isNotBlank()) {
                isTaskCreated = true

                lifecycleScope.launch {
                    val existingTasks = taskModel.getAllTasks()
                    val existingTask = existingTasks.find { it.title == taskName }

                    if (existingTask != null) {
                        existingTask.description = taskDescription
                        existingTask.priority = taskPriority.progress
                        existingTask.iconResId = selectedIconResId
                        taskModel.updateTask(existingTask)
                    } else {
                        val newTaskId = existingTasks.size.toLong() + 1
                        updateActivityTitle(newTaskId)

                        taskModel.addNewTask(
                            title = taskName,
                            description = taskDescription,
                            priority = taskPriority.progress,
                            iconResId = selectedIconResId,
                            startTime = 0,
                            endTime = 0
                        )
                    }

                    isTaskCreated = false
                }
            }
        }
    }

    private fun deleteTask(task: Task) {
        lifecycleScope.launch {
            taskModel.removeTask(task)
            finish() // lub nawiguj do innej strony, według potrzeb
        }
    }


}
