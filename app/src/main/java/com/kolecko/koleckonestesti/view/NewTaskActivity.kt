package com.kolecko.koleckonestesti

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

// Aktivita pro přidání nové úlohy
class NewTaskActivity : AppCompatActivity() {
    private lateinit var taskModel: TaskModel

    // Metoda volaná při vytváření aktivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)

        // Nastavení zpětného tlačítka v akčním baru
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0f
        supportActionBar?.title = "Nová úloha"

        // Inicializace instance TaskModel
        taskModel = TaskModelImpl(this)

        // Nastavení posluchače tlačítka pro přidání úlohy
        val addTaskButton: Button = findViewById(R.id.buttonAddTask)
        addTaskButton.setOnClickListener {
            addNewTask()
        }
    }

    // Metoda pro přidání nové úlohy
    private fun addNewTask() {
        val taskNameEditText: EditText = findViewById(R.id.editTextTaskName)
        val taskDescriptionEditText: EditText = findViewById(R.id.editTextTaskDescription)

        // Získání hodnot z editovatelných polí
        val taskName = taskNameEditText.text.toString()
        val taskDescription = taskDescriptionEditText.text.toString()

        // Kontrola, zda jsou vyplněna obě pole
        if (taskName.isNotBlank() && taskDescription.isNotBlank()) {
            lifecycleScope.launch {
                // Přidání nové úlohy pomocí TaskModel
                taskModel.addNewTask(taskName, taskDescription)
                finish() // Ukončení aktivity po úspěšném přidání úlohy
                showToast("Úloha přidána!", Toast.LENGTH_SHORT)
            }
        } else {
            showToast("Prosím vyplňte všechna políčka!", Toast.LENGTH_SHORT) // Toast zpráva
        }
    }

    // Metoda pro zobrazení krátké Toast zprávy
    private fun showToast(message: String, duration: Int) {
        val context: Context = applicationContext
        val toast = Toast.makeText(context, message, duration)
        toast.show()
    }

    // Přepsaná metoda pro reakci na stisk tlačítka zpět v akčním baru
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
