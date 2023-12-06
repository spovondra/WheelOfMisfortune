package com.misfortuneapp.wheelofmisfortune.custom

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.misfortuneapp.wheelofmisfortune.R
import com.misfortuneapp.wheelofmisfortune.controller.MainController
import com.misfortuneapp.wheelofmisfortune.model.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onItemClick: (Task) -> Unit,
    private val onItemDelete: (Task) -> Unit,
    private val mainController: MainController
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // ViewHolder for individual items in RecyclerView
    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskTitle: TextView = view.findViewById(R.id.taskTitle)
        val taskDescription: TextView = view.findViewById(R.id.taskDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        holder.taskTitle.text = task.title
        holder.taskDescription.text = task.description

        val spacingInPixels =
            holder.itemView.resources.getDimensionPixelSize(R.dimen.spacing_between_items)
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(
            spacingInPixels,
            spacingInPixels,
            spacingInPixels,
            spacingInPixels
        )
        holder.itemView.layoutParams = layoutParams

        holder.itemView.setOnClickListener {
            onItemClick.invoke(task)
        }
    }

    override fun getItemCount() = tasks.size

    fun removeItem(position: Int) {
        if (position >= 0 && position < tasks.size) {
            val removedTask = tasks[position]

            // Remove from the list
            tasks.removeAt(position)
            notifyItemRemoved(position)

            // Delete from the database using MainController
            GlobalScope.launch {
                mainController.removeTask(removedTask)
            }
            onItemDelete.invoke(removedTask)
        }
    }

    fun onItemDismiss(position: Int) {
        removeItem(position)
    }
}
