package com.kolecko.koleckonestestiv4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kolecko.koleckonestestiv4.model.Task

class TaskAdapter(private val tasks: List<Task>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // ViewHolder pro jednotlivé položky v RecyclerView
    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskTitle: TextView = view.findViewById(R.id.taskTitle)
        val taskDescription: TextView = view.findViewById(R.id.taskDescription)
    }

    // Metoda pro vytvoření nového ViewHolderu
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    // Metoda pro naplnění daty jednotlivých položek v RecyclerView
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Nastavení titulu a popisu úkolu do odpovídajících TextView v položce RecyclerView
        holder.taskTitle.text = task.title
        holder.taskDescription.text = task.description

        // Nastavení mezery mezi položkami pomocí paddingu
        val spacingInPixels = holder.itemView.resources.getDimensionPixelSize(R.dimen.spacing_between_items)
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(spacingInPixels, spacingInPixels, spacingInPixels, spacingInPixels)
        holder.itemView.layoutParams = layoutParams
    }

    // Metoda vracející celkový počet položek v RecyclerView
    override fun getItemCount() = tasks.size
}
