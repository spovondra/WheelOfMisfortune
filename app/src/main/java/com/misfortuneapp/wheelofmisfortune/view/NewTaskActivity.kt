package com.misfortuneapp.wheelofmisfortune.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
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
    private lateinit var taskController: MainController
    private lateinit var mainView: MainView
    private lateinit var notification: Notification
    private lateinit var taskModel: TaskModel
    private lateinit var statisticsController: StatisticsController
    private var selectedIconResId: Int = R.drawable.icon // Výchozí ikona
    private var selectedImageView: ImageView? = null
    private lateinit var taskPriority: SeekBar
    private lateinit var textViewProgress: TextView

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
            context = mainView as MainViewImp,
            view = mainView,
            notification = notification,
            model = taskModel,
            statisticsController = statisticsController
        )

        taskPriority = findViewById(R.id.seekBarPriority)
        textViewProgress = findViewById(R.id.textViewProgress)

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

        taskPriority.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Aktualizace textu v TextView při posunutí SeekBar
                textViewProgress.text = "Selected Progress: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not implemented
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not implemented
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

    private fun addNewTask() {
        val taskNameEditText: EditText = findViewById(R.id.editTextTaskName)
        val taskDescriptionEditText: EditText = findViewById(R.id.editTextTaskDescription)

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
                    startTime = 0, // Docasne
                    endTime = 0 // Docasne
                )

                // Optionally, you can update the UI or perform other actions after adding a new task

                finish()
                showToast("Úloha přidána!")
            }
        } else {
            showToast("Prosím vyplňte všechna políčka!")
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
