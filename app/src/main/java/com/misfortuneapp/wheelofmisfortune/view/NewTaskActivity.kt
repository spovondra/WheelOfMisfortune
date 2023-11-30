package com.misfortuneapp.wheelofmisfortune.view

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.misfortuneapp.wheelofmisfortune.R
import com.misfortuneapp.wheelofmisfortune.controller.*
import com.misfortuneapp.wheelofmisfortune.model.*
import kotlinx.coroutines.launch

class NewTaskActivity : AppCompatActivity() {
    private lateinit var taskController: MainController
    private lateinit var mainView: MainView
    private lateinit var notification: Notification
    private lateinit var taskModel: TaskModel
    private lateinit var statisticsController: StatisticsController
    private var selectedIconResId: Int = R.drawable.icon1 // Výchozí ikona
    private var selectedImageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)

        // Nastavení zpětného tlačítka v akčním baru
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0f
        supportActionBar?.title = "Nová úloha"

        // Initialize instances
        mainView = MainViewImp()
        notification = NotificationHandler(this)
        taskModel = TaskModelImpl(this)
        statisticsController = StatisticsControllerImp(DataRepository(DataDatabase.getInstance(this).dataDao()))

        // Inicializace instance TaskController
        taskController = MainControllerImpl(
            view = mainView,
            notification = notification,
            model = taskModel,
            statisticsController = statisticsController
        )
        // Nastavení posluchače tlačítka pro přidání úlohy
        val addTaskButton: Button = findViewById(R.id.buttonAddTask)
        addTaskButton.setOnClickListener {
            addNewTask()
        }

        // Předpokládáme, že máte ImageButton pro ikony s ID icon1, icon2, icon3, icon4
        val icon1: ImageView = findViewById(R.id.icon1)
        val icon2: ImageView = findViewById(R.id.icon2)
        val icon3: ImageView = findViewById(R.id.icon3)
        val icon4: ImageView = findViewById(R.id.icon4)

        // Nastavení posluchačů kliknutí pro ikony
        icon1.setOnClickListener { onIconClick(icon1) }
        icon2.setOnClickListener { onIconClick(icon2) }
        icon3.setOnClickListener { onIconClick(icon3) }
        icon4.setOnClickListener { onIconClick(icon4) }
    }

    private fun onIconClick(imageView: ImageView) {
        // Zrušení zvýraznění předchozí ikony
        selectedImageView?.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))

        // Nastavení nové ikony
        selectedIconResId = when (imageView.id) {
            R.id.icon1 -> R.drawable.icon1
            R.id.icon2 -> R.drawable.icon2
            R.id.icon3 -> R.drawable.icon3
            R.id.icon4 -> R.drawable.icon4
            else -> R.drawable.icon1 // Výchozí ikona, můžete změnit podle potřeby
        }

        // Zvýraznění nové ikony změnou barvy pozadí pomocí ColorFilter
        imageView.setBackgroundColor(ContextCompat.getColor(this, R.color.selectedIconColor))

        // Uložení aktuální ImageView pro další použití
        selectedImageView = imageView
        selectedImageView?.setBackgroundResource(R.drawable.icon_shape)
    }

    private fun addNewTask() {
        val taskNameEditText: EditText = findViewById(R.id.editTextTaskName)
        val taskDescriptionEditText: EditText = findViewById(R.id.editTextTaskDescription)
        val taskPriority: SeekBar = findViewById(R.id.seekBarPriority)

        val taskName = taskNameEditText.text.toString()
        val taskDescription = taskDescriptionEditText.text.toString()
        val priority = taskPriority.progress

        if (taskName.isNotBlank() && taskDescription.isNotBlank()) {
            lifecycleScope.launch {
                // Use TaskModel methods to add a new task
                taskModel.addNewTask(
                    title = taskName,
                    description = taskDescription,
                    priority = priority,
                    iconResId = selectedIconResId,
                    startTime = 22, // Docasne
                    endTime = 22 // Docasne
                )

                // Optionally, you can update the UI or perform other actions after adding a new task

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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
