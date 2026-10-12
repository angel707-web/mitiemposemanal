package com.senati.mitiemposemanal.Bflor.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.senati.mitiemposemanal.Bflor.R
import com.senati.mitiemposemanal.Bflor.model.Event

class EventAdapter(
    private var events: List<Event>
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvEventTaskName: TextView = itemView.findViewById(R.id.tvEventTaskName)
        private val tvEventCategory: TextView = itemView.findViewById(R.id.tvEventCategory)
        private val tvEventTimes: TextView = itemView.findViewById(R.id.tvEventTimes)
        private val tvEventDuration: TextView = itemView.findViewById(R.id.tvEventDuration)
        private val tvEventStatus: TextView = itemView.findViewById(R.id.tvEventStatus)
        private val viewCategoryColor: View = itemView.findViewById(R.id.viewCategoryColor)

        fun bind(event: Event) {
            tvEventTaskName.text = event.taskName
            tvEventCategory.text = "CATEGORÍA: ${event.categoryName.uppercase()}"

            try {
                val color = Color.parseColor(event.categoryColorHex)
                viewCategoryColor.setBackgroundColor(color)
            } catch (e: Exception) {
                viewCategoryColor.setBackgroundColor(Color.parseColor("#5865F2"))
            }

            if (event.isActive) {
                tvEventTimes.text = "Inicio: ${event.startTime}\nFin: En curso..."
                tvEventDuration.text = "En marcha"
                tvEventStatus.text = "● EN CURSO"
                tvEventStatus.setTextColor(Color.parseColor("#2ECC71"))
            } else {
                tvEventTimes.text = "Inicio: ${event.startTime}\nFin: ${event.endTime ?: "-"}"
                tvEventDuration.text = event.getFormattedDuration()
                tvEventStatus.text = "Finalizado"
                tvEventStatus.setTextColor(Color.parseColor("#A0A5BA"))
            }
        }
    }
}
