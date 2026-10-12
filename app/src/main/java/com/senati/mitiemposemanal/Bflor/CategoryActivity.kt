package com.senati.mitiemposemanal.Bflor

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.senati.mitiemposemanal.Bflor.adapter.CategoryAdapter
import com.senati.mitiemposemanal.Bflor.db.DatabaseHelper
import com.senati.mitiemposemanal.Bflor.model.Category

class CategoryActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CATEGORY_ID = "extra_category_id"
    }

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var tvCategoryTitle: TextView
    private lateinit var etCategoryName: EditText
    private lateinit var btnSaveCategory: Button
    private lateinit var rvCategories: RecyclerView

    private var editingCategoryId: Long = -1L
    private var editingCategoryColor: String = "#5865F2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_category)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.categoryRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)

        tvCategoryTitle = findViewById(R.id.tvCategoryTitle)
        etCategoryName = findViewById(R.id.etCategoryName)
        btnSaveCategory = findViewById(R.id.btnSaveCategory)
        rvCategories = findViewById(R.id.rvCategories)
        val btnBackCategory = findViewById<ImageButton>(R.id.btnBackCategory)

        setupRecyclerView()

        // Check if opened directly with an edit ID
        val initialId = intent.getLongExtra(EXTRA_CATEGORY_ID, -1L)
        if (initialId != -1L) {
            setupEditMode(initialId)
        } else {
            setupCreateMode()
        }

        btnSaveCategory.setOnClickListener {
            handleSaveCategory()
        }

        btnBackCategory.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            categories = emptyList(),
            onEditCategory = { category ->
                setupEditMode(category.id)
            },
            onDeleteCategory = { category ->
                confirmDeleteCategory(category)
            }
        )

        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.adapter = categoryAdapter
        loadCategories()
    }

    private fun loadCategories() {
        val categories = dbHelper.getAllCategories()
        categoryAdapter.updateCategories(categories)
    }

    private fun setupCreateMode() {
        editingCategoryId = -1L
        editingCategoryColor = "#5865F2"
        tvCategoryTitle.text = getString(R.string.add_category).uppercase()
        btnSaveCategory.text = getString(R.string.add)
        etCategoryName.text.clear()
    }

    private fun setupEditMode(categoryId: Long) {
        val category = dbHelper.getCategoryById(categoryId)
        if (category != null) {
            editingCategoryId = category.id
            editingCategoryColor = category.colorHex
            tvCategoryTitle.text = getString(R.string.edit_category).uppercase()
            btnSaveCategory.text = getString(R.string.save)
            etCategoryName.setText(category.name)
            etCategoryName.setSelection(category.name.length)
        }
    }

    private fun handleSaveCategory() {
        val name = etCategoryName.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "El nombre de la categoría no puede estar vacío", Toast.LENGTH_SHORT).show()
            etCategoryName.requestFocus()
            return
        }

        // HU-01 Criterio de aceptación 3: Si el nombre ya existe, la app no la guarda y muestra un mensaje
        if (dbHelper.categoryExists(name, editingCategoryId)) {
            Toast.makeText(this, "Ya existe una categoría con ese nombre", Toast.LENGTH_SHORT).show()
            return
        }

        if (editingCategoryId == -1L) {
            // HU-01 Criterio de aceptación 2: Puedo crear una categoría nueva y aparece en la lista
            // Assign a color based on category count
            val colors = listOf("#6C5CE7", "#E84393", "#FF7675", "#00CEC9", "#0984E3", "#FD79A8", "#00B894", "#FDCB6E")
            val existingCount = dbHelper.getAllCategories().size
            val colorHex = colors[existingCount % colors.size]

            val newCategory = Category(name = name, colorHex = colorHex)
            val id = dbHelper.insertCategory(newCategory)
            if (id != -1L) {
                Toast.makeText(this, "Categoría creada con éxito", Toast.LENGTH_SHORT).show()
                setupCreateMode()
                loadCategories()
            } else {
                Toast.makeText(this, "Error al crear la categoría", Toast.LENGTH_SHORT).show()
            }
        } else {
            // HU-01 Criterio de aceptación 4: Puedo editar el nombre de una categoría
            val updated = Category(id = editingCategoryId, name = name, colorHex = editingCategoryColor)
            val rows = dbHelper.updateCategory(updated)
            if (rows > 0) {
                Toast.makeText(this, "Categoría actualizada con éxito", Toast.LENGTH_SHORT).show()
                setupCreateMode()
                loadCategories()
            } else {
                Toast.makeText(this, "Error al actualizar la categoría", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDeleteCategory(category: Category) {
        // HU-01 Criterio de aceptación 6: Si la categoría tiene tareas, la app no permite eliminarla y me avisa
        if (dbHelper.hasTasksForCategory(category.id)) {
            AlertDialog.Builder(this)
                .setTitle("Acción no permitida")
                .setMessage("No se puede eliminar la categoría \"${category.name}\" porque tiene tareas asociadas.")
                .setPositiveButton("Entendido", null)
                .show()
            return
        }

        // HU-01 Criterio de aceptación 5: Puedo eliminar una categoría, pidiendo confirmación
        AlertDialog.Builder(this)
            .setTitle("Eliminar categoría")
            .setMessage("¿Estás seguro de que deseas eliminar la categoría \"${category.name}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                val rows = dbHelper.deleteCategory(category.id)
                if (rows > 0) {
                    Toast.makeText(this, "Categoría eliminada", Toast.LENGTH_SHORT).show()
                    if (editingCategoryId == category.id) {
                        setupCreateMode()
                    }
                    loadCategories()
                } else {
                    Toast.makeText(this, "Error al eliminar la categoría", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
