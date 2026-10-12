package com.senati.mitiemposemanal.Bflor.adapter

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.senati.mitiemposemanal.Bflor.R
import com.senati.mitiemposemanal.Bflor.model.Task

class TaskAdapter(
    private var tasks: List<Task>,
    private val onToggleCompleted: (Task) -> Unit,
    private val onEditTask: (Task) -> Unit,
    private val onDeleteTask: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTaskName: TextView = itemView.findViewById(R.id.tvTaskName)
        private val tvTaskCategory: TextView = itemView.findViewById(R.id.tvTaskCategory)
        private val viewCategoryColor: View = itemView.findViewById(R.id.viewCategoryColor)
        private val btnToggleCompleted: ImageButton = itemView.findViewById(R.id.btnToggleCompleted)
        private val btnEditTask: ImageButton = itemView.findViewById(R.id.btnEditTask)
        private val btnDeleteTask: ImageButton = itemView.findViewById(R.id.btnDeleteTask)

        fun bind(task: Task) {
            tvTaskName.text = task.name
            // Acceptance criteria 4: La lista muestra cada tarea con su categoría, por ejemplo: "Leer Kotlin · Estudio"
            tvTaskCategory.text = "CATEGORÍA: ${task.categoryName.uppercase()}"

            try {
                val color = Color.parseColor(task.categoryColorHex)
                viewCategoryColor.setBackgroundColor(color)
            } catch (e: Exception) {
                viewCategoryColor.setBackgroundColor(Color.parseColor("#5865F2"))
            }

            if (task.isCompleted) {
                btnToggleCompleted.setImageResource(R.drawable.ic_check_circle)
                tvTaskName.paintFlags = tvTaskName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvTaskName.alpha = 0.6f
            } else {
                btnToggleCompleted.setImageResource(R.drawable.ic_circle_outline)
                tvTaskName.paintFlags = tvTaskName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvTaskName.alpha = 1.0f
            }

            btnToggleCompleted.setOnClickListener {
                onToggleCompleted(task)
            }

            btnEditTask.setOnClickListener {
                onEditTask(task)
            }

            btnDeleteTask.setOnClickListener {
                onDeleteTask(task)
            }
        }
    }
}
