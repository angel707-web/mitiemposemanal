package com.senati.mitiemposemanal.Bflor.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.senati.mitiemposemanal.Bflor.R
import com.senati.mitiemposemanal.Bflor.model.Category

class CategoryAdapter(
    private var categories: List<Category>,
    private val onEditCategory: (Category) -> Unit,
    private val onDeleteCategory: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val viewCategoryBullet: View = itemView.findViewById(R.id.viewCategoryBullet)
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val btnEditCategory: ImageButton = itemView.findViewById(R.id.btnEditCategory)
        private val btnDeleteCategory: ImageButton = itemView.findViewById(R.id.btnDeleteCategory)

        fun bind(category: Category) {
            tvCategoryName.text = "• ${category.name.uppercase()}"

            try {
                val color = Color.parseColor(category.colorHex)
                val dot = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                }
                viewCategoryBullet.background = dot
            } catch (e: Exception) {
                // fallback
            }

            btnEditCategory.setOnClickListener {
                onEditCategory(category)
            }

            btnDeleteCategory.setOnClickListener {
                onDeleteCategory(category)
            }
        }
    }
}
