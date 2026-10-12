package com.senati.mitiemposemanal.Bflor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.senati.mitiemposemanal.Bflor.R
import com.senati.mitiemposemanal.Bflor.model.Task

class TaskSimpleAdapter(
    private var tasks: List<Task>,
    private val onEditTask: (Task) -> Unit,
    private val onDeleteTask: (Task) -> Unit
) : RecyclerView.Adapter<TaskSimpleAdapter.TaskSimpleViewHolder>() {

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskSimpleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task_simple, parent, false)
        return TaskSimpleViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskSimpleViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    inner class TaskSimpleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTaskSimpleText: TextView = itemView.findViewById(R.id.tvTaskSimpleText)
        private val btnEditTaskSimple: ImageButton = itemView.findViewById(R.id.btnEditTaskSimple)
        private val btnDeleteTaskSimple: ImageButton = itemView.findViewById(R.id.btnDeleteTaskSimple)

        fun bind(task: Task) {
            tvTaskSimpleText.text = "• ${task.name.uppercase()} - ${task.categoryName.uppercase()}"

            btnEditTaskSimple.setOnClickListener {
                onEditTask(task)
            }

            btnDeleteTaskSimple.setOnClickListener {
                onDeleteTask(task)
            }
        }
    }
}
