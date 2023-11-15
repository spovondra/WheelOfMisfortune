// NewTaskActivity.kt
package com.kolecko.koleckonestestiv4

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

// NewTaskActivity.kt
class NewTaskActivity : AppCompatActivity() {
    private lateinit var taskModel: TaskModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0f
        supportActionBar?.title = "Nová úloha"

        taskModel = TaskModelImpl(this)

        val addTaskButton: Button = findViewById(R.id.buttonAddTask)
        addTaskButton.setOnClickListener {
            addNewTask()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun addNewTask() {
        val taskNameEditText: EditText = findViewById(R.id.editTextTaskName)
        val taskDescriptionEditText: EditText = findViewById(R.id.editTextTaskDescription)

        val taskName = taskNameEditText.text.toString()
        val taskDescription = taskDescriptionEditText.text.toString()

        if (taskName.isNotBlank() && taskDescription.isNotBlank()) {
            lifecycleScope.launch {
                taskModel.addNewTask(taskName, taskDescription)
                finish()
                showToast("Úloha přidána!", Toast.LENGTH_SHORT)
            }
        } else {
            showToast("Prosím vyplňte všechna políčka!", Toast.LENGTH_SHORT)
        }
    }

    private fun showToast(message: String, duration: Int) {
        val context: Context = applicationContext
        val toast = Toast.makeText(context, message, duration)
        toast.show()
    }
}
