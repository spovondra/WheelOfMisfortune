package com.misfortuneapp.wheelofmisfortune.custom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.misfortuneapp.wheelofmisfortune.R
import com.misfortuneapp.wheelofmisfortune.controller.MainController
import com.misfortuneapp.wheelofmisfortune.model.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Adapter pro RecyclerView zobrazující seznam úkolů
class TaskAdapter(
    private val tasks: MutableList<Task>,  // Seznam úkolů
    private val onItemClick: (Task) -> Unit,  // Akce při kliknutí na položku
    private val mainController: MainController,  // Kontrolér pro interakci s daty a UI
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // ViewHolder pro jednotlivé položky v RecyclerView
    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskTitle: TextView = view.findViewById(R.id.taskTitle)
        val taskDescription: TextView = view.findViewById(R.id.taskDescription)
        val taskIcon: ImageView = view.findViewById(R.id.taskIcon)
    }

    // Metoda volaná při vytvoření ViewHolderu
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        // Vytvoření nového ViewHolderu na základě definovaného rozložení
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    // Metoda volaná při vytváření a aktualizaci obsahu ViewHolderu
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        // Získání aktuálního úkolu ze seznamu
        val task = tasks[position]

        // Nastavení textu pro nadpis a popis úkolu
        holder.taskTitle.text = task.title
        holder.taskDescription.text = task.description

        // Nastavení zdroje obrázku pro ImageView
        holder.taskIcon.setImageResource(task.iconResId)

        // Podmínka pro zaoblení horní části prvního úkolu
        when {
            itemCount == 1 -> {
                holder.itemView.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.rounded_corners
                )
            }
            position == itemCount - 1 -> {
                holder.itemView.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.top_rounded_corners
                )
            }
            // Podmínka pro zaoblení spodní části posledního úkolu
            position == 0 -> {
                holder.itemView.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.bottom_rounded_corners
                )
            }
            else -> {
                // Pro všechny ostatní položky nenastavujte zaoblení
                holder.itemView.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.task_shape
                )
            }
        }

        // Získání základní barvy
        val baseColor = ContextCompat.getColor(holder.itemView.context, R.color.colorButton)

// Nastavení barvy podle stavu úkolu s 20% průhledností
        when (task.taskState) {
            TaskState.DONE -> {
                val doneColor = ContextCompat.getColor(holder.itemView.context, R.color.colorButtonDone)
                val blendedColor = ColorUtils.blendARGB(baseColor, doneColor, 0.2f)
                holder.itemView.background?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(blendedColor, BlendModeCompat.SRC_IN)
            }
            TaskState.AVAILABLE -> {
                val availableColor = ContextCompat.getColor(holder.itemView.context, R.color.colorButtonAvailable)
                val blendedColor = ColorUtils.blendARGB(baseColor, availableColor, 0.2f)
                holder.itemView.background?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(blendedColor, BlendModeCompat.SRC_IN)
            }
            TaskState.DELETED -> {
                val deletedColor = ContextCompat.getColor(holder.itemView.context, R.color.colorButtonDeleted)
                val blendedColor = ColorUtils.blendARGB(baseColor, deletedColor, 0.2f)
                holder.itemView.background?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(blendedColor, BlendModeCompat.SRC_IN)
            }
            else -> {
                val defaultColor = ContextCompat.getColor(holder.itemView.context, R.color.colorButtonInProgress)
                holder.itemView.background?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(defaultColor, BlendModeCompat.SRC_IN)
            }
        }

        // Nastavení okraje mezi položkami
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(14, 2,14,2)
        holder.itemView.layoutParams = layoutParams

        // Nastavení akce při kliknutí na položku
        holder.itemView.setOnClickListener {
            onItemClick.invoke(task)
        }
    }

    // Metoda vracející počet položek v seznamu
    override fun getItemCount() = tasks.size

    // Metoda pro odstranění položky z RecyclerView a databáze
    @OptIn(DelicateCoroutinesApi::class)
    fun itemDeleted(position: Int) {
        if (position >= 0 && position < tasks.size) {
            val taskDeleted = tasks[position]
            tasks.removeAt(position)

            GlobalScope.launch {
                mainController.setTaskDeleted(taskDeleted)
            }
        }
    }

    // Metoda pro nastavení položky DONE
    @OptIn(DelicateCoroutinesApi::class)
    fun itemDone (position: Int) {
        if (position >= 0 && position < tasks.size) {
            val taskDone = tasks[position]
            tasks.removeAt(position)

            GlobalScope.launch {
                mainController.setTaskDone(taskDone)
            }
        }
    }
}