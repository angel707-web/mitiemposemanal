package com.senati.mitiemposemanal.Bflor

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.senati.mitiemposemanal.Bflor.adapter.TaskSimpleAdapter
import com.senati.mitiemposemanal.Bflor.db.DatabaseHelper
import com.senati.mitiemposemanal.Bflor.model.Category
import com.senati.mitiemposemanal.Bflor.model.Task

class TaskActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
    }

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var taskSimpleAdapter: TaskSimpleAdapter
    private lateinit var tvTaskTitle: TextView
    private lateinit var etTaskName: EditText
    private lateinit var spCategory: Spinner
    private lateinit var btnSaveTask: Button
    private lateinit var rvTasksSimple: RecyclerView

    private var categoriesList: List<Category> = emptyList()
    private var editingTaskId: Long = -1L
    private var editingTaskCompleted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_task)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.taskRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)

        tvTaskTitle = findViewById(R.id.tvTaskTitle)
        etTaskName = findViewById(R.id.etTaskName)
        spCategory = findViewById(R.id.spCategory)
        btnSaveTask = findViewById(R.id.btnSaveTask)
        rvTasksSimple = findViewById(R.id.rvTasksSimple)
        val btnBackTask = findViewById<ImageButton>(R.id.btnBackTask)

        setupCategoriesSpinner()
        setupRecyclerView()

        val initialId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (initialId != -1L) {
            setupEditMode(initialId)
        } else {
            setupCreateMode()
        }

        btnSaveTask.setOnClickListener {
            handleSaveTask()
        }

        btnBackTask.setOnClickListener {
            finish()
        }
    }

    private fun setupCategoriesSpinner() {
        // HU-02 Criterio de aceptación 2: Cada tarea se crea con una categoría elegida de una lista
        categoriesList = dbHelper.getAllCategories()
        val categoryNames = categoriesList.map { it.name }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = spinnerAdapter
    }

    private fun setupRecyclerView() {
        taskSimpleAdapter = TaskSimpleAdapter(
            tasks = emptyList(),
            onEditTask = { task ->
                setupEditMode(task.id)
            },
            onDeleteTask = { task ->
                confirmDeleteTask(task)
            }
        )

        rvTasksSimple.layoutManager = LinearLayoutManager(this)
        rvTasksSimple.adapter = taskSimpleAdapter
        loadTasks()
    }

    private fun loadTasks() {
        val tasks = dbHelper.getAllTasks()
        taskSimpleAdapter.updateTasks(tasks)
    }

    private fun setupCreateMode() {
        editingTaskId = -1L
        editingTaskCompleted = false
        tvTaskTitle.text = getString(R.string.add_task).uppercase()
        btnSaveTask.text = getString(R.string.add)
        etTaskName.text.clear()
        if (categoriesList.isNotEmpty()) {
            spCategory.setSelection(0)
        }
    }

    private fun setupEditMode(taskId: Long) {
        val task = dbHelper.getTaskById(taskId)
        if (task != null) {
            editingTaskId = task.id
            editingTaskCompleted = task.isCompleted
            tvTaskTitle.text = getString(R.string.edit_task).uppercase()
            btnSaveTask.text = getString(R.string.save)
            etTaskName.setText(task.name)
            etTaskName.setSelection(task.name.length)

            val categoryIndex = categoriesList.indexOfFirst { it.id == task.categoryId }
            if (categoryIndex >= 0) {
                spCategory.setSelection(categoryIndex)
            }
        }
    }

    private fun handleSaveTask() {
        val taskName = etTaskName.text.toString().trim()

        // HU-02 Criterio de aceptación 1: No se puede guardar una tarea sin nombre
        if (taskName.isEmpty()) {
            Toast.makeText(this, "El nombre de la tarea no puede estar vacío", Toast.LENGTH_SHORT).show()
            etTaskName.requestFocus()
            return
        }

        if (categoriesList.isEmpty()) {
            Toast.makeText(this, "Debe existir al menos una categoría", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCategoryPos = spCategory.selectedItemPosition
        if (selectedCategoryPos < 0 || selectedCategoryPos >= categoriesList.size) {
            Toast.makeText(this, "Debe seleccionar una categoría", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedCategory = categoriesList[selectedCategoryPos]

        // HU-02 Criterio de aceptación 3: No se pueden repetir nombres de tareas
        if (dbHelper.taskExists(taskName, editingTaskId)) {
            Toast.makeText(this, "Ya existe una tarea con ese nombre", Toast.LENGTH_SHORT).show()
            return
        }

        if (editingTaskId == -1L) {
            // Create task
            val newTask = Task(
                name = taskName,
                categoryId = selectedCategory.id,
                isCompleted = false
            )
            val id = dbHelper.insertTask(newTask)
            if (id != -1L) {
                Toast.makeText(this, "Tarea añadida con éxito", Toast.LENGTH_SHORT).show()
                setupCreateMode()
                loadTasks()
            } else {
                Toast.makeText(this, "Error al guardar la tarea", Toast.LENGTH_SHORT).show()
            }
        } else {
            // HU-02 Criterio de aceptación 5: Puedo editar el nombre o la categoría de una tarea
            val updated = Task(
                id = editingTaskId,
                name = taskName,
                categoryId = selectedCategory.id,
                isCompleted = editingTaskCompleted
            )
            val rows = dbHelper.updateTask(updated)
            if (rows > 0) {
                Toast.makeText(this, "Tarea actualizada con éxito", Toast.LENGTH_SHORT).show()
                setupCreateMode()
                loadTasks()
            } else {
                Toast.makeText(this, "Error al actualizar la tarea", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDeleteTask(task: Task) {
        // HU-02 Criterio de aceptación 5: ...y eliminarla
        AlertDialog.Builder(this)
            .setTitle("Eliminar tarea")
            .setMessage("¿Estás seguro de que deseas eliminar la tarea \"${task.name}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                val rows = dbHelper.deleteTask(task.id)
                if (rows > 0) {
                    Toast.makeText(this, "Tarea eliminada con éxito", Toast.LENGTH_SHORT).show()
                    if (editingTaskId == task.id) {
                        setupCreateMode()
                    }
                    loadTasks()
                } else {
                    Toast.makeText(this, "Error al eliminar la tarea", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
