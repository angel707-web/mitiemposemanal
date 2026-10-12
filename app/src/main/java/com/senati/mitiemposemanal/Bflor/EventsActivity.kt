package com.senati.mitiemposemanal.Bflor

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.senati.mitiemposemanal.Bflor.adapter.EventAdapter
import com.senati.mitiemposemanal.Bflor.adapter.TaskSearchAdapter
import com.senati.mitiemposemanal.Bflor.db.DatabaseHelper
import com.senati.mitiemposemanal.Bflor.model.Category
import com.senati.mitiemposemanal.Bflor.model.Event
import com.senati.mitiemposemanal.Bflor.model.Task
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EventsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var eventAdapter: EventAdapter
    private lateinit var searchAdapter: TaskSearchAdapter

    private lateinit var tvTimerDisplay: TextView
    private lateinit var tvActiveStatusBadge: TextView
    private lateinit var tvActiveTaskDetails: TextView
    private lateinit var actvTaskSearch: AutoCompleteTextView
    private lateinit var layoutSelectedTaskCategory: LinearLayout
    private lateinit var tvSelectedCategoryName: TextView
    private lateinit var btnStartEvent: Button
    private lateinit var btnFinishEvent: Button
    private lateinit var rvEvents: RecyclerView
    private lateinit var tvEmptyEvents: TextView

    private var activeEvent: Event? = null
    private var selectedTask: Task? = null

    private val timerHandler = Handler(Looper.getMainLooper())
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private val timerRunnable = object : Runnable {
        override fun run() {
            updateActiveTimer()
            timerHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_events)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.eventsRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)

        initViews()
        setupSearchAutoComplete()
        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        checkActiveEvent()
        loadEventsHistory()
    }

    override fun onPause() {
        super.onPause()
        timerHandler.removeCallbacks(timerRunnable)
    }

    private fun initViews() {
        tvTimerDisplay = findViewById(R.id.tvTimerDisplay)
        tvActiveStatusBadge = findViewById(R.id.tvActiveStatusBadge)
        tvActiveTaskDetails = findViewById(R.id.tvActiveTaskDetails)
        actvTaskSearch = findViewById(R.id.actvTaskSearch)
        layoutSelectedTaskCategory = findViewById(R.id.layoutSelectedTaskCategory)
        tvSelectedCategoryName = findViewById(R.id.tvSelectedCategoryName)
        btnStartEvent = findViewById(R.id.btnStartEvent)
        btnFinishEvent = findViewById(R.id.btnFinishEvent)
        rvEvents = findViewById(R.id.rvEvents)
        tvEmptyEvents = findViewById(R.id.tvEmptyEvents)
    }

    private fun setupSearchAutoComplete() {
        searchAdapter = TaskSearchAdapter(this, dbHelper)
        actvTaskSearch.setAdapter(searchAdapter)

        // HU-03 Criterio de aceptación: Al elegir una tarea de la lista, se muestra su categoría
        actvTaskSearch.setOnItemClickListener { parent, _, position, _ ->
            val task = parent.getItemAtPosition(position) as? Task
            if (task != null) {
                selectedTask = task
                showSelectedCategoryBadge(task.categoryName, task.categoryColorHex)
            }
        }

        // Detect manual edits
        actvTaskSearch.addTextChangedListener { text ->
            val input = text?.toString()?.trim() ?: ""
            if (selectedTask != null && !selectedTask!!.name.equals(input, ignoreCase = true)) {
                // If user changed text away from selected task
                val match = dbHelper.getTaskByName(input)
                if (match != null) {
                    selectedTask = match
                    showSelectedCategoryBadge(match.categoryName, match.categoryColorHex)
                } else {
                    selectedTask = null
                    layoutSelectedTaskCategory.visibility = View.GONE
                }
            } else if (selectedTask == null && input.isNotEmpty()) {
                val match = dbHelper.getTaskByName(input)
                if (match != null) {
                    selectedTask = match
                    showSelectedCategoryBadge(match.categoryName, match.categoryColorHex)
                }
            }
        }
    }

    private fun showSelectedCategoryBadge(categoryName: String, colorHex: String) {
        layoutSelectedTaskCategory.visibility = View.VISIBLE
        tvSelectedCategoryName.text = categoryName.uppercase()
        try {
            val color = Color.parseColor(colorHex)
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8f
                setColor(color)
            }
            tvSelectedCategoryName.background = bg
        } catch (e: Exception) {
            // fallback
        }
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter(emptyList())
        rvEvents.layoutManager = LinearLayoutManager(this)
        rvEvents.adapter = eventAdapter
    }

    private fun setupListeners() {
        val btnBackEvents = findViewById<ImageButton>(R.id.btnBackEvents)
        btnBackEvents.setOnClickListener {
            finish()
        }

        btnStartEvent.setOnClickListener {
            handleStartEvent()
        }

        btnFinishEvent.setOnClickListener {
            handleFinishEvent()
        }
    }

    // HU-05 Criterio de aceptación: Solo puede haber un evento en curso a la vez;
    // Si cierro la app con un evento en curso, al volver a abrirla sigue en curso.
    private fun checkActiveEvent() {
        activeEvent = dbHelper.getActiveEvent()
        if (activeEvent != null) {
            // Event is in progress
            btnStartEvent.isEnabled = false
            btnStartEvent.alpha = 0.5f
            btnFinishEvent.isEnabled = true
            btnFinishEvent.alpha = 1.0f

            tvActiveStatusBadge.text = "● EVENTO EN CURSO"
            tvActiveStatusBadge.setTextColor(Color.parseColor("#2ECC71"))
            tvActiveTaskDetails.text = "${activeEvent!!.taskName.uppercase()} · ${activeEvent!!.categoryName.uppercase()}\nIniciado: ${activeEvent!!.startTime}"

            actvTaskSearch.setText(activeEvent!!.taskName)
            actvTaskSearch.isEnabled = false
            showSelectedCategoryBadge(activeEvent!!.categoryName, activeEvent!!.categoryColorHex)

            timerHandler.removeCallbacks(timerRunnable)
            timerHandler.post(timerRunnable)
        } else {
            // No event in progress
            btnStartEvent.isEnabled = true
            btnStartEvent.alpha = 1.0f
            btnFinishEvent.isEnabled = false
            btnFinishEvent.alpha = 0.5f

            tvActiveStatusBadge.text = "NINGÚN EVENTO EN CURSO"
            tvActiveStatusBadge.setTextColor(Color.parseColor("#A0A5BA"))
            tvActiveTaskDetails.text = "Busca o escribe una tarea abajo para iniciar"
            tvTimerDisplay.text = "00:00:00"

            actvTaskSearch.isEnabled = true

            timerHandler.removeCallbacks(timerRunnable)
        }
    }

    private fun updateActiveTimer() {
        val current = activeEvent ?: return
        try {
            val start = LocalDateTime.parse(current.startTime, dateTimeFormatter)
            val now = LocalDateTime.now()
            val seconds = Duration.between(start, now).seconds.coerceAtLeast(0)

            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60
            tvTimerDisplay.text = String.format("%02d:%02d:%02d", hours, minutes, secs)
        } catch (e: Exception) {
            tvTimerDisplay.text = "00:00:00"
        }
    }

    private fun handleStartEvent() {
        val taskName = actvTaskSearch.text.toString().trim()
        if (taskName.isEmpty()) {
            Toast.makeText(this, "Escribe o selecciona una tarea primero", Toast.LENGTH_SHORT).show()
            actvTaskSearch.requestFocus()
            return
        }

        // Check if task exists in database
        val existingTask = dbHelper.getTaskByName(taskName)
        if (existingTask != null) {
            // Task exists -> start event immediately
            startNewEvent(existingTask)
        } else {
            // HU-04 Criterio de aceptación: Si el nombre escrito no existe y pulso Iniciar, la app me pide elegir una categoría.
            showCreateTaskDialog(taskName)
        }
    }

    // HU-04: Crear la tarea si no existe
    private fun showCreateTaskDialog(taskName: String) {
        val categories = dbHelper.getAllCategories()
        if (categories.isEmpty()) {
            Toast.makeText(this, "No hay categorías disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryNames = categories.map { it.name }.toTypedArray()

        val spinner = Spinner(this).apply {
            val adapter = ArrayAdapter(this@EventsActivity, android.R.layout.simple_spinner_dropdown_item, categoryNames)
            setAdapter(adapter)
            setPadding(32, 16, 32, 16)
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
            addView(TextView(this@EventsActivity).apply {
                text = "La tarea \"$taskName\" no existe.\nElige una categoría para crearla:"
                setTextColor(Color.parseColor("#151C53"))
                textSize = 14f
            })
            addView(spinner)
        }

        AlertDialog.Builder(this)
            .setTitle("Crear nueva tarea")
            .setView(container)
            .setPositiveButton("Confirmar e Iniciar") { _, _ ->
                val selectedCategoryIndex = spinner.selectedItemPosition
                val chosenCategory = categories[selectedCategoryIndex]

                // Guardar la tarea con su categoría (aparece en la pantalla de Tareas del Sprint 1)
                val newTask = Task(
                    name = taskName,
                    categoryId = chosenCategory.id,
                    isCompleted = false,
                    categoryName = chosenCategory.name,
                    categoryColorHex = chosenCategory.colorHex
                )

                val newTaskId = dbHelper.insertTask(newTask)
                if (newTaskId != -1L) {
                    Toast.makeText(this, "Tarea \"$taskName\" creada en ${chosenCategory.name}", Toast.LENGTH_SHORT).show()
                    val createdTask = newTask.copy(id = newTaskId)
                    selectedTask = createdTask
                    showSelectedCategoryBadge(createdTask.categoryName, createdTask.categoryColorHex)
                    startNewEvent(createdTask)
                } else {
                    Toast.makeText(this, "Error al crear la tarea", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun startNewEvent(task: Task) {
        // HU-05 Criterio de aceptación: Al pulsar Iniciar se guarda la fecha y hora actual del celular como inicio del evento
        val now = LocalDateTime.now()
        val startTimeStr = now.format(dateTimeFormatter)

        val eventId = dbHelper.startEvent(task.id, startTimeStr)
        if (eventId != -1L) {
            Toast.makeText(this, "Evento iniciado para \"${task.name}\"", Toast.LENGTH_SHORT).show()
            checkActiveEvent()
            loadEventsHistory()
        } else {
            Toast.makeText(this, "Error al iniciar el evento", Toast.LENGTH_SHORT).show()
        }
    }

    // HU-05 Criterio de aceptación: Al pulsar Finalizar se guarda la fecha y hora de fin y la duración del evento
    private fun handleFinishEvent() {
        val current = activeEvent ?: return

        val endTime = LocalDateTime.now()
        val endTimeStr = endTime.format(dateTimeFormatter)

        val startTime = try {
            LocalDateTime.parse(current.startTime, dateTimeFormatter)
        } catch (e: Exception) {
            endTime
        }

        val durationSeconds = Duration.between(startTime, endTime).seconds.coerceAtLeast(0)

        val rows = dbHelper.finishEvent(current.id, endTimeStr, durationSeconds)
        if (rows > 0) {
            Toast.makeText(this, "Evento finalizado con éxito", Toast.LENGTH_SHORT).show()
            activeEvent = null
            checkActiveEvent()
            loadEventsHistory()
        } else {
            Toast.makeText(this, "Error al finalizar el evento", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadEventsHistory() {
        val events = dbHelper.getAllEvents()
        eventAdapter.updateEvents(events)

        if (events.isEmpty()) {
            rvEvents.visibility = View.GONE
            tvEmptyEvents.visibility = View.VISIBLE
        } else {
            rvEvents.visibility = View.VISIBLE
            tvEmptyEvents.visibility = View.GONE
        }
    }
}
