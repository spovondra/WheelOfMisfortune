package com.misfortuneapp.wheelofmisfortune

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TaskDetailsActivity : AppCompatActivity() {

    private lateinit var taskNameTextView: TextView
    private lateinit var taskDescriptionTextView: TextView
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)

        taskNameTextView = findViewById(R.id.taskName)
        taskDescriptionTextView = findViewById(R.id.taskDescription)
        backButton = findViewById(R.id.buttonBack) // Assuming the ID for your bottom button is buttonBack

        // Retrieve data from the intent
        val taskId = intent.getIntExtra("taskId", -1)

        if (taskId != -1) {
            // Fetch task details from the database
            GlobalScope.launch(Dispatchers.Main) {
                val taskModel = TaskModelImpl(applicationContext)
                val task = taskModel.getTaskById(taskId)
                task?.let {
                    // Update UI with task details
                    Log.d("TaskDetailsActivity", "Task ID!!!!!§: $taskId")
                    taskNameTextView.text = it.title
                    taskDescriptionTextView.text = it.description
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
