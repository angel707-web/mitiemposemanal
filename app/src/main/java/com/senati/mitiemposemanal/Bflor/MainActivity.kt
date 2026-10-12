package com.senati.mitiemposemanal.Bflor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.senati.mitiemposemanal.Bflor.adapter.TaskAdapter
import com.senati.mitiemposemanal.Bflor.db.DatabaseHelper
import com.senati.mitiemposemanal.Bflor.model.Task

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var rvTasks: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)

        rvTasks = findViewById(R.id.rvTasks)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        val btnAddTask = findViewById<Button>(R.id.btnAddTask)
        val btnAddCategory = findViewById<Button>(R.id.btnAddCategory)

        setupRecyclerView()

        btnAddTask.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
        }

        btnAddCategory.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            tasks = emptyList(),
            onToggleCompleted = { task ->
                val newStatus = !task.isCompleted
                dbHelper.toggleTaskCompleted(task.id, newStatus)
                loadTasks()
            },
            onEditTask = { task ->
                val intent = Intent(this, TaskActivity::class.java).apply {
                    putExtra(TaskActivity.EXTRA_TASK_ID, task.id)
                }
                startActivity(intent)
            },
            onDeleteTask = { task ->
                confirmDeleteTask(task)
            }
        )

        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = taskAdapter
    }

    private fun loadTasks() {
        val tasks = dbHelper.getAllTasks()
        taskAdapter.updateTasks(tasks)

        if (tasks.isEmpty()) {
            rvTasks.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            rvTasks.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
        }
    }

    private fun confirmDeleteTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar tarea")
            .setMessage("¿Estás seguro de que deseas eliminar la tarea \"${task.name}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                val rows = dbHelper.deleteTask(task.id)
                if (rows > 0) {
                    Toast.makeText(this, "Tarea eliminada correctamente", Toast.LENGTH_SHORT).show()
                    loadTasks()
                } else {
                    Toast.makeText(this, "Error al eliminar la tarea", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}