package com.senati.mitiemposemanal.Bflor

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.senati.mitiemposemanal.Bflor.adapter.EventAdapter
import com.senati.mitiemposemanal.Bflor.db.DatabaseHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ReportsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var eventAdapter: EventAdapter

    private lateinit var layoutNoReports: LinearLayout
    private lateinit var layoutReportsContent: LinearLayout
    private lateinit var spReportWeeks: Spinner
    private lateinit var llReportCategoriesContainer: LinearLayout
    private lateinit var rvReportEvents: RecyclerView

    private var reportWeeksList: List<Pair<String, String>> = emptyList()

    private val dbDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reports)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reportsRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)

        layoutNoReports = findViewById(R.id.layoutNoReports)
        layoutReportsContent = findViewById(R.id.layoutReportsContent)
        spReportWeeks = findViewById(R.id.spReportWeeks)
        llReportCategoriesContainer = findViewById(R.id.llReportCategoriesContainer)
        rvReportEvents = findViewById(R.id.rvReportEvents)
        val btnBackReports = findViewById<ImageButton>(R.id.btnBackReports)

        btnBackReports.setOnClickListener {
            finish()
        }

        setupRecyclerView()
        loadWeeksAndReports()
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter(emptyList())
        rvReportEvents.layoutManager = LinearLayoutManager(this)
        rvReportEvents.adapter = eventAdapter
    }

    private fun loadWeeksAndReports() {
        // HU-08 Criterio de aceptación 2: El Spinner se llena con las semanas que tienen reporte,
        // en formato "06/10/2026 – 12/10/2026", de la más reciente a la más antigua.
        reportWeeksList = dbHelper.getDistinctReportWeeks()

        if (reportWeeksList.isEmpty()) {
            // HU-08 Criterio de aceptación 5: Si todavía no hay semanas cerradas, se muestra "Aún no hay reportes"
            layoutNoReports.visibility = View.VISIBLE
            layoutReportsContent.visibility = View.GONE
        } else {
            layoutNoReports.visibility = View.GONE
            layoutReportsContent.visibility = View.VISIBLE

            val displayLabels = reportWeeksList.map { (startStr, endStr) ->
                try {
                    val start = LocalDate.parse(startStr, dbDateFormatter).format(displayDateFormatter)
                    val end = LocalDate.parse(endStr, dbDateFormatter).format(displayDateFormatter)
                    "$start – $end"
                } catch (e: Exception) {
                    "$startStr – $endStr"
                }
            }

            val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, displayLabels)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spReportWeeks.adapter = spinnerAdapter

            spReportWeeks.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position in reportWeeksList.indices) {
                        displayReportForWeek(reportWeeksList[position])
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            // Cargar por defecto la más reciente
            displayReportForWeek(reportWeeksList[0])
        }
    }

    /**
     * HU-08 Criterio de aceptación 3 y 4:
     * - Al elegir una semana se muestran sus categorías con horas y porcentaje sobre 168 horas, y las horas sin registrar.
     * - Debajo se muestra la lista de eventos de esa semana con tarea, categoría, inicio, fin y duración.
     */
    private fun displayReportForWeek(weekRange: Pair<String, String>) {
        val (weekStart, weekEnd) = weekRange
        llReportCategoriesContainer.removeAllViews()

        val reportItems = dbHelper.getReportItemsForWeek(weekStart)
        var totalRegisteredSeconds = 0L

        for (item in reportItems) {
            totalRegisteredSeconds += item.totalSeconds

            val rowView = layoutInflater.inflate(R.layout.item_category_stat, llReportCategoriesContainer, false)

            val bullet: View = rowView.findViewById(R.id.viewCategoryStatBullet)
            val tvName: TextView = rowView.findViewById(R.id.tvCategoryStatName)
            val tvHours: TextView = rowView.findViewById(R.id.tvCategoryStatHours)
            val tvPercent: TextView = rowView.findViewById(R.id.tvCategoryStatPercentage)
            val pbProgress: ProgressBar = rowView.findViewById(R.id.pbCategoryStatProgress)

            tvName.text = item.categoryName
            tvHours.text = item.getFormattedHours()
            tvPercent.text = item.getFormattedPercentage()

            val progressVal = (item.percentage * 10).toInt().coerceIn(0, 1000)
            pbProgress.progress = progressVal

            try {
                val color = Color.parseColor(item.categoryColorHex)
                val dot = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                }
                bullet.background = dot
                pbProgress.progressTintList = ColorStateList.valueOf(color)
            } catch (e: Exception) {
                // fallback
            }

            llReportCategoriesContainer.addView(rowView)
        }

        // Fila "Sin registrar"
        val weekTotalSeconds = 168L * 3600L
        val unregisteredSeconds = (weekTotalSeconds - totalRegisteredSeconds).coerceAtLeast(0L)
        val unregisteredHours = unregisteredSeconds / 3600.0
        val unregisteredPercentage = (unregisteredSeconds.toDouble() / weekTotalSeconds.toDouble()) * 100.0

        val unregRowView = layoutInflater.inflate(R.layout.item_category_stat, llReportCategoriesContainer, false)
        val unregBullet: View = unregRowView.findViewById(R.id.viewCategoryStatBullet)
        val unregName: TextView = unregRowView.findViewById(R.id.tvCategoryStatName)
        val unregHours: TextView = unregRowView.findViewById(R.id.tvCategoryStatHours)
        val unregPercent: TextView = unregRowView.findViewById(R.id.tvCategoryStatPercentage)
        val unregProgress: ProgressBar = unregRowView.findViewById(R.id.pbCategoryStatProgress)

        unregName.text = getString(R.string.unregistered_time_label)
        unregHours.text = String.format(Locale.US, "%.1f h", unregisteredHours)
        unregPercent.text = String.format(Locale.US, "%.1f %%", unregisteredPercentage)

        val unregProgressVal = (unregisteredPercentage * 10).toInt().coerceIn(0, 1000)
        unregProgress.progress = unregProgressVal

        val grayColor = Color.parseColor("#A0A5BA")
        val unregDot = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(grayColor)
        }
        unregBullet.background = unregDot
        unregProgress.progressTintList = ColorStateList.valueOf(grayColor)

        llReportCategoriesContainer.addView(unregRowView)

        // Eventos de esa semana
        val weekStartDateTime = "$weekStart 00:00:00"
        val weekEndDateTime = "$weekEnd 23:59:59"
        val events = dbHelper.getEventsForWeek(weekStartDateTime, weekEndDateTime)
        eventAdapter.updateEvents(events)
    }
}
