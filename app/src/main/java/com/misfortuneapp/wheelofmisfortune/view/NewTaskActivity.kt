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
import com.misfortuneapp.wheelofmisfortune.controller.*
import com.misfortuneapp.wheelofmisfortune.model.*
import kotlinx.coroutines.launch

class NewTaskActivity : AppCompatActivity() {
    private lateinit var mainView: MainView
    private lateinit var notification: Notification
    private lateinit var taskModel: TaskModel
    private var selectedIconResId: Int = R.drawable.ic_launcher_foreground // Default icon
    private var selectedImageView: ImageView? = null
    private lateinit var taskPriority: SeekBar
    private lateinit var textViewProgress: TextView
    private lateinit var taskNameEditText: EditText
    private lateinit var taskDescriptionEditText: EditText
    private var isNewTask: Boolean = true // Flag to track whether it's a new task

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)

        // Set up the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0f
        supportActionBar?.title = "Nová úloha"

        // Initialize instances
        mainView = MainViewImp()
        notification = NotificationHandler(this)
        taskModel = TaskModelImpl(this)

        taskPriority = findViewById(R.id.seekBarPriority)
        textViewProgress = findViewById(R.id.textViewProgress)
        taskNameEditText = findViewById(R.id.editTextTaskName)
        taskDescriptionEditText = findViewById(R.id.editTextTaskDescription)

        // Set up the click listener for the button to add a task
        val addTaskButton: Button = findViewById(R.id.buttonAddTask)
        addTaskButton.setOnClickListener {
            saveTaskAutomatically()
        }

        // Assume you have ImageButtons for icons with IDs icon1, icon2, icon3, icon4
        val icon1: ImageView = findViewById(R.id.icon1)
        val icon2: ImageView = findViewById(R.id.icon2)
        val icon3: ImageView = findViewById(R.id.icon3)
        val icon4: ImageView = findViewById(R.id.icon4)

        // Set up click listeners for icons
        icon1.setOnClickListener { onIconClick(icon1) }
        icon2.setOnClickListener { onIconClick(icon2) }
        icon3.setOnClickListener { onIconClick(icon3) }
        icon4.setOnClickListener { onIconClick(icon4) }

        // Set up a SeekBar change listener
        taskPriority.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textViewProgress.text = "Selected Progress: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not implemented
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not implemented
            }
        })

        // Set up TextWatchers for automatic saving when text changes
        taskNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // Not implemented
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                // Not implemented
            }

            override fun afterTextChanged(editable: Editable?) {
                saveTaskAutomatically()
            }
        })

        taskDescriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // Not implemented
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                // Not implemented
            }

            override fun afterTextChanged(editable: Editable?) {
                saveTaskAutomatically()
            }
        })
    }

    private fun onIconClick(imageView: ImageView) {
        // Deselect the previously selected icon
        selectedImageView?.isSelected = false

        // Select the new icon
        imageView.isSelected = true
        selectedImageView = imageView

        // Update selectedIconResId based on the clicked icon
        selectedIconResId = when (imageView.id) {
            R.id.icon1 -> R.drawable.ic_action_cart
            R.id.icon2 -> R.drawable.ic_action_book
            R.id.icon3 -> R.drawable.ic_action_bell
            R.id.icon4 -> R.drawable.ic_action_box
            else -> R.drawable.ic_launcher_foreground
        }
    }

    private fun saveTaskAutomatically() {
        val taskName = taskNameEditText.text.toString()
        val taskDescription = taskDescriptionEditText.text.toString()
        val priority = taskPriority.progress

        if (taskName.isNotBlank()) {
            lifecycleScope.launch {
                if (isNewTask) {
                    // Create a new task only if it's a new task
                    taskModel.addNewTask(
                        title = taskName,
                        description = taskDescription,
                        priority = priority,
                        iconResId = selectedIconResId,
                        startTime = 0, // Temporary
                        endTime = 0 // Temporary
                    )
                    showToast("Nová úloha vytvořena!")
                    isNewTask = false // Set the flag to false after creating a new task
                } else {
                    // Update an existing task
                    // You need to implement the logic to update the existing task based on your requirements
                    // taskModel.updateTask(taskId, updatedTitle, updatedDescription, updatedPriority, updatedIconResId, updatedStartTime, updatedEndTime)
                    showToast("Úloha aktualizována!")
                }
            }
        }
    }

    private fun showToast(message: String) {
        val context: Context = applicationContext
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
