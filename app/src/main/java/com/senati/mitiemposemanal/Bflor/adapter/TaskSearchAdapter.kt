package com.senati.mitiemposemanal.Bflor.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.senati.mitiemposemanal.Bflor.R
import com.senati.mitiemposemanal.Bflor.db.DatabaseHelper
import com.senati.mitiemposemanal.Bflor.model.Task

class TaskSearchAdapter(
    context: Context,
    private val dbHelper: DatabaseHelper
) : ArrayAdapter<Task>(context, 0) {

    private var suggestions = listOf<Task>()

    override fun getCount(): Int = suggestions.size

    override fun getItem(position: Int): Task? = if (position in suggestions.indices) suggestions[position] else null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_task_search, parent, false)
        val task = getItem(position)

        val tvName: TextView = view.findViewById(R.id.tvSearchTaskName)
        val tvCategory: TextView = view.findViewById(R.id.tvSearchTaskCategory)

        if (task != null) {
            tvName.text = task.name
            tvCategory.text = task.categoryName

            try {
                val color = Color.parseColor(task.categoryColorHex)
                val bg = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 8f
                    setColor(color)
                }
                tvCategory.background = bg
                tvCategory.setTextColor(Color.WHITE)
            } catch (e: Exception) {
                // fallback
            }
        }

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val query = constraint?.toString()?.trim() ?: ""

                // HU-03 Criterio de aceptación: Al escribir 2 letras o más, aparece una lista con las tareas guardadas que contienen ese texto
                // La búsqueda se hace en la base de datos y se actualiza con cada letra.
                if (query.length >= 2) {
                    val matchingTasks = dbHelper.searchTasks(query)
                    results.values = matchingTasks
                    results.count = matchingTasks.size
                } else {
                    results.values = emptyList<Task>()
                    results.count = 0
                }

                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                suggestions = (results?.values as? List<Task>) ?: emptyList()
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as? Task)?.name ?: super.convertResultToString(resultValue)
            }
        }
    }
}
