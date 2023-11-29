package com.kolecko.koleckonestesti

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kolecko.koleckonestesti.model.Task


class TaskAdapter(private val tasks: List<Task>, private val onItemClick: (Task) -> Unit) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // ViewHolder for individual items in RecyclerView
    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskTitle: TextView = view.findViewById(R.id.taskTitle)
        val taskDescription: TextView = view.findViewById(R.id.taskDescription)
    }

    // Method to create a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    // Method to populate data for individual items in RecyclerView
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Set title and description of the task to the corresponding TextViews in the RecyclerView item
        holder.taskTitle.text = task.title
        holder.taskDescription.text = task.description

        // Set padding to create space between items
        val spacingInPixels = holder.itemView.resources.getDimensionPixelSize(R.dimen.spacing_between_items)
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(spacingInPixels, spacingInPixels, spacingInPixels, spacingInPixels)
        holder.itemView.layoutParams = layoutParams

        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick.invoke(task)
        }
    }

    // Method returning the total number of items in RecyclerView
    override fun getItemCount() = tasks.size
}