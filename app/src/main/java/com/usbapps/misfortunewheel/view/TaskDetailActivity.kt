package com.usbapps.misfortunewheel.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.usbapps.misfortunewheel.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.usbapps.misfortunewheel.model.TaskModelImpl
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * Activity representing the details of a specific task.
 */
class TaskDetailsActivity : AppCompatActivity() {

    private lateinit var taskNameTextView: TextView
    private lateinit var taskDescriptionTextView: TextView
    private lateinit var backButton: Button

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     * this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)

        // Initialize UI elements
        taskNameTextView = findViewById(R.id.taskName)
        taskDescriptionTextView = findViewById(R.id.taskDescription)
        backButton =
            findViewById(R.id.buttonBack) // Assuming the ID for your bottom button is buttonBack

        // Retrieve data from the intent
        val taskId = intent.getIntExtra("taskId", -1)

        if (taskId != -1) {
            // Fetch task details from the database
            GlobalScope.launch(Dispatchers.Main) {
                val taskModel = TaskModelImpl(applicationContext)
                val task = taskModel.getTaskById(taskId)
                task?.let {
                    // Update UI with task details
                    taskNameTextView.text = it.title
                    taskDescriptionTextView.text = it.toString()
                }
            }
        }

        // Set up a click listener for the back button
        backButton.setOnClickListener {
            // Navigate back to the main page
            val intent = Intent(this, MainViewImp::class.java)
            startActivity(intent)
        }
    }
}
