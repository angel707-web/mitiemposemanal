package com.senati.mitiemposemanal.Bflor.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.senati.mitiemposemanal.Bflor.model.Category
import com.senati.mitiemposemanal.Bflor.model.CategoryWeeklyStat
import com.senati.mitiemposemanal.Bflor.model.Event
import com.senati.mitiemposemanal.Bflor.model.Task
import com.senati.mitiemposemanal.Bflor.model.WeeklyReportItem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mitiemposemanal.db"
        private const val DATABASE_VERSION = 4

        // Table categories
        const val TABLE_CATEGORIES = "categories"
        const val COL_CAT_ID = "id"
        const val COL_CAT_NAME = "name"
        const val COL_CAT_COLOR = "color_hex"

        // Table tasks
        const val TABLE_TASKS = "tasks"
        const val COL_TASK_ID = "id"
        const val COL_TASK_NAME = "name"
        const val COL_TASK_CATEGORY_ID = "category_id"
        const val COL_TASK_COMPLETED = "is_completed"

        // Table events (Sprint 2)
        const val TABLE_EVENTS = "events"
        const val COL_EVENT_ID = "id"
        const val COL_EVENT_TASK_ID = "task_id"
        const val COL_EVENT_START_TIME = "start_time"
        const val COL_EVENT_END_TIME = "end_time"
        const val COL_EVENT_DURATION = "duration_seconds"
        const val COL_EVENT_IS_ACTIVE = "is_active"

        // Table weekly_reports (Sprint 4)
        const val TABLE_WEEKLY_REPORTS = "weekly_reports"
        const val COL_REPORT_ID = "id"
        const val COL_REPORT_WEEK_START = "week_start"
        const val COL_REPORT_WEEK_END = "week_end"
        const val COL_REPORT_CATEGORY_ID = "category_id"
        const val COL_REPORT_TOTAL_SECONDS = "total_seconds"
        const val COL_REPORT_HOURS = "hours"
        const val COL_REPORT_PERCENTAGE = "percentage"
        const val COL_REPORT_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createCategoriesTable = """
            CREATE TABLE $TABLE_CATEGORIES (
                $COL_CAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CAT_NAME TEXT NOT NULL UNIQUE COLLATE NOCASE,
                $COL_CAT_COLOR TEXT NOT NULL
            )
        """.trimIndent()

        val createTasksTable = """
            CREATE TABLE $TABLE_TASKS (
                $COL_TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TASK_NAME TEXT NOT NULL UNIQUE COLLATE NOCASE,
                $COL_TASK_CATEGORY_ID INTEGER NOT NULL,
                $COL_TASK_COMPLETED INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY ($COL_TASK_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COL_CAT_ID)
            )
        """.trimIndent()

        val createEventsTable = """
            CREATE TABLE $TABLE_EVENTS (
                $COL_EVENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_EVENT_TASK_ID INTEGER NOT NULL,
                $COL_EVENT_START_TIME TEXT NOT NULL,
                $COL_EVENT_END_TIME TEXT,
                $COL_EVENT_DURATION INTEGER NOT NULL DEFAULT 0,
                $COL_EVENT_IS_ACTIVE INTEGER NOT NULL DEFAULT 1,
                FOREIGN KEY ($COL_EVENT_TASK_ID) REFERENCES $TABLE_TASKS($COL_TASK_ID)
            )
        """.trimIndent()

        val createWeeklyReportsTable = """
            CREATE TABLE $TABLE_WEEKLY_REPORTS (
                $COL_REPORT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_REPORT_WEEK_START TEXT NOT NULL,
                $COL_REPORT_WEEK_END TEXT NOT NULL,
                $COL_REPORT_CATEGORY_ID INTEGER NOT NULL,
                $COL_REPORT_TOTAL_SECONDS INTEGER NOT NULL DEFAULT 0,
                $COL_REPORT_HOURS REAL NOT NULL DEFAULT 0.0,
                $COL_REPORT_PERCENTAGE REAL NOT NULL DEFAULT 0.0,
                $COL_REPORT_CREATED_AT TEXT NOT NULL,
                FOREIGN KEY ($COL_REPORT_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COL_CAT_ID),
                UNIQUE($COL_REPORT_WEEK_START, $COL_REPORT_CATEGORY_ID)
            )
        """.trimIndent()

        db.execSQL(createCategoriesTable)
        db.execSQL(createTasksTable)
        db.execSQL(createEventsTable)
        db.execSQL(createWeeklyReportsTable)

        // Seed default 4 categories (HU-01: Criterio de aceptación 1)
        val defaultCategories = listOf(
            Pair("Estudio", "#6C5CE7"),
            Pair("Trabajo", "#E84393"),
            Pair("Descanso", "#FF7675"),
            Pair("Ocio", "#00CEC9")
        )

        for ((name, color) in defaultCategories) {
            val values = ContentValues().apply {
                put(COL_CAT_NAME, name)
                put(COL_CAT_COLOR, color)
            }
            db.insert(TABLE_CATEGORIES, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Sprint 2: No borrar datos del Sprint 1, solo agregar la tabla events si no existe
        if (oldVersion < 2) {
            val createEventsTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_EVENTS (
                    $COL_EVENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_EVENT_TASK_ID INTEGER NOT NULL,
                    $COL_EVENT_START_TIME TEXT NOT NULL,
                    $COL_EVENT_END_TIME TEXT,
                    $COL_EVENT_DURATION INTEGER NOT NULL DEFAULT 0,
                    $COL_EVENT_IS_ACTIVE INTEGER NOT NULL DEFAULT 1,
                    FOREIGN KEY ($COL_EVENT_TASK_ID) REFERENCES $TABLE_TASKS($COL_TASK_ID)
                )
            """.trimIndent()
            db.execSQL(createEventsTable)
        }
        // Sprint 3: Migración sin pérdida de datos de versiones anteriores
        if (oldVersion < 3) {
            // Tablas e índices de Sprint 3
        }
        // Sprint 4: Crear tabla de reportes semanales sin perder datos previos
        if (oldVersion < 4) {
            val createWeeklyReportsTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_WEEKLY_REPORTS (
                    $COL_REPORT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_REPORT_WEEK_START TEXT NOT NULL,
                    $COL_REPORT_WEEK_END TEXT NOT NULL,
                    $COL_REPORT_CATEGORY_ID INTEGER NOT NULL,
                    $COL_REPORT_TOTAL_SECONDS INTEGER NOT NULL DEFAULT 0,
                    $COL_REPORT_HOURS REAL NOT NULL DEFAULT 0.0,
                    $COL_REPORT_PERCENTAGE REAL NOT NULL DEFAULT 0.0,
                    $COL_REPORT_CREATED_AT TEXT NOT NULL,
                    FOREIGN KEY ($COL_REPORT_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COL_CAT_ID),
                    UNIQUE($COL_REPORT_WEEK_START, $COL_REPORT_CATEGORY_ID)
                )
            """.trimIndent()
            db.execSQL(createWeeklyReportsTable)
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // ================= CATEGORY OPERATIONS =================

    fun getAllCategories(): List<Category> {
        val categories = mutableListOf<Category>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CATEGORIES,
            arrayOf(COL_CAT_ID, COL_CAT_NAME, COL_CAT_COLOR),
            null, null, null, null,
            "$COL_CAT_NAME ASC"
        )

        cursor.use {
            val idIndex = cursor.getColumnIndexOrThrow(COL_CAT_ID)
            val nameIndex = cursor.getColumnIndexOrThrow(COL_CAT_NAME)
            val colorIndex = cursor.getColumnIndexOrThrow(COL_CAT_COLOR)

            while (cursor.moveToNext()) {
                categories.add(
                    Category(
                        id = cursor.getLong(idIndex),
                        name = cursor.getString(nameIndex),
                        colorHex = cursor.getString(colorIndex)
                    )
                )
            }
        }
        return categories
    }

    fun getCategoryById(id: Long): Category? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CATEGORIES,
            arrayOf(COL_CAT_ID, COL_CAT_NAME, COL_CAT_COLOR),
            "$COL_CAT_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        return cursor.use {
            if (cursor.moveToFirst()) {
                Category(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CAT_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_NAME)),
                    colorHex = cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_COLOR))
                )
            } else null
        }
    }

    fun categoryExists(name: String, excludeId: Long = -1): Boolean {
        val db = readableDatabase
        val selection = if (excludeId != -1L) {
            "TRIM($COL_CAT_NAME) = ? COLLATE NOCASE AND $COL_CAT_ID != ?"
        } else {
            "TRIM($COL_CAT_NAME) = ? COLLATE NOCASE"
        }
        val selectionArgs = if (excludeId != -1L) {
            arrayOf(name.trim(), excludeId.toString())
        } else {
            arrayOf(name.trim())
        }

        val cursor = db.query(
            TABLE_CATEGORIES,
            arrayOf(COL_CAT_ID),
            selection,
            selectionArgs,
            null, null, null
        )

        return cursor.use { it.count > 0 }
    }

    fun insertCategory(category: Category): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CAT_NAME, category.name.trim())
            put(COL_CAT_COLOR, category.colorHex)
        }
        return db.insert(TABLE_CATEGORIES, null, values)
    }

    fun updateCategory(category: Category): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CAT_NAME, category.name.trim())
            put(COL_CAT_COLOR, category.colorHex)
        }
        return db.update(TABLE_CATEGORIES, values, "$COL_CAT_ID = ?", arrayOf(category.id.toString()))
    }

    fun deleteCategory(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_CATEGORIES, "$COL_CAT_ID = ?", arrayOf(id.toString()))
    }

    fun hasTasksForCategory(categoryId: Long): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_TASKS WHERE $COL_TASK_CATEGORY_ID = ?",
            arrayOf(categoryId.toString())
        )
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) > 0 else false
        }
    }

    // ================= TASK OPERATIONS =================

    fun getAllTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase
        val query = """
            SELECT t.$COL_TASK_ID, t.$COL_TASK_NAME, t.$COL_TASK_CATEGORY_ID, t.$COL_TASK_COMPLETED,
                   c.$COL_CAT_NAME AS cat_name, c.$COL_CAT_COLOR AS cat_color
            FROM $TABLE_TASKS t
            INNER JOIN $TABLE_CATEGORIES c ON t.$COL_TASK_CATEGORY_ID = c.$COL_CAT_ID
            ORDER BY t.$COL_TASK_ID DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.use {
            val idIndex = cursor.getColumnIndexOrThrow(COL_TASK_ID)
            val nameIndex = cursor.getColumnIndexOrThrow(COL_TASK_NAME)
            val catIdIndex = cursor.getColumnIndexOrThrow(COL_TASK_CATEGORY_ID)
            val completedIndex = cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)
            val catNameIndex = cursor.getColumnIndexOrThrow("cat_name")
            val catColorIndex = cursor.getColumnIndexOrThrow("cat_color")

            while (cursor.moveToNext()) {
                tasks.add(
                    Task(
                        id = cursor.getLong(idIndex),
                        name = cursor.getString(nameIndex),
                        categoryId = cursor.getLong(catIdIndex),
                        isCompleted = cursor.getInt(completedIndex) == 1,
                        categoryName = cursor.getString(catNameIndex),
                        categoryColorHex = cursor.getString(catColorIndex)
                    )
                )
            }
        }
        return tasks
    }

    // HU-03: Buscar tarea mientras escribo
    fun searchTasks(query: String): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase
        val sql = """
            SELECT t.$COL_TASK_ID, t.$COL_TASK_NAME, t.$COL_TASK_CATEGORY_ID, t.$COL_TASK_COMPLETED,
                   c.$COL_CAT_NAME AS cat_name, c.$COL_CAT_COLOR AS cat_color
            FROM $TABLE_TASKS t
            INNER JOIN $TABLE_CATEGORIES c ON t.$COL_TASK_CATEGORY_ID = c.$COL_CAT_ID
            WHERE t.$COL_TASK_NAME LIKE ? COLLATE NOCASE
            ORDER BY t.$COL_TASK_NAME ASC
        """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf("%${query.trim()}%"))
        cursor.use {
            val idIndex = cursor.getColumnIndexOrThrow(COL_TASK_ID)
            val nameIndex = cursor.getColumnIndexOrThrow(COL_TASK_NAME)
            val catIdIndex = cursor.getColumnIndexOrThrow(COL_TASK_CATEGORY_ID)
            val completedIndex = cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)
            val catNameIndex = cursor.getColumnIndexOrThrow("cat_name")
            val catColorIndex = cursor.getColumnIndexOrThrow("cat_color")

            while (cursor.moveToNext()) {
                tasks.add(
                    Task(
                        id = cursor.getLong(idIndex),
                        name = cursor.getString(nameIndex),
                        categoryId = cursor.getLong(catIdIndex),
                        isCompleted = cursor.getInt(completedIndex) == 1,
                        categoryName = cursor.getString(catNameIndex),
                        categoryColorHex = cursor.getString(catColorIndex)
                    )
                )
            }
        }
        return tasks
    }

    // HU-04: Buscar tarea exacta por nombre
    fun getTaskByName(name: String): Task? {
        val db = readableDatabase
        val query = """
            SELECT t.$COL_TASK_ID, t.$COL_TASK_NAME, t.$COL_TASK_CATEGORY_ID, t.$COL_TASK_COMPLETED,
                   c.$COL_CAT_NAME AS cat_name, c.$COL_CAT_COLOR AS cat_color
            FROM $TABLE_TASKS t
            INNER JOIN $TABLE_CATEGORIES c ON t.$COL_TASK_CATEGORY_ID = c.$COL_CAT_ID
            WHERE TRIM(t.$COL_TASK_NAME) = ? COLLATE NOCASE
            LIMIT 1
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(name.trim()))
        return cursor.use {
            if (cursor.moveToFirst()) {
                Task(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_NAME)),
                    categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_CATEGORY_ID)),
                    isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                    categoryName = cursor.getString(cursor.getColumnIndexOrThrow("cat_name")),
                    categoryColorHex = cursor.getString(cursor.getColumnIndexOrThrow("cat_color"))
                )
            } else null
        }
    }

    fun getTaskById(id: Long): Task? {
        val db = readableDatabase
        val query = """
            SELECT t.$COL_TASK_ID, t.$COL_TASK_NAME, t.$COL_TASK_CATEGORY_ID, t.$COL_TASK_COMPLETED,
                   c.$COL_CAT_NAME AS cat_name, c.$COL_CAT_COLOR AS cat_color
            FROM $TABLE_TASKS t
            INNER JOIN $TABLE_CATEGORIES c ON t.$COL_TASK_CATEGORY_ID = c.$COL_CAT_ID
            WHERE t.$COL_TASK_ID = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(id.toString()))
        return cursor.use {
            if (cursor.moveToFirst()) {
                Task(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_NAME)),
                    categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_CATEGORY_ID)),
                    isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                    categoryName = cursor.getString(cursor.getColumnIndexOrThrow("cat_name")),
                    categoryColorHex = cursor.getString(cursor.getColumnIndexOrThrow("cat_color"))
                )
            } else null
        }
    }

    fun taskExists(name: String, excludeId: Long = -1): Boolean {
        val db = readableDatabase
        val selection = if (excludeId != -1L) {
            "TRIM($COL_TASK_NAME) = ? COLLATE NOCASE AND $COL_TASK_ID != ?"
        } else {
            "TRIM($COL_TASK_NAME) = ? COLLATE NOCASE"
        }
        val selectionArgs = if (excludeId != -1L) {
            arrayOf(name.trim(), excludeId.toString())
        } else {
            arrayOf(name.trim())
        }

        val cursor = db.query(
            TABLE_TASKS,
            arrayOf(COL_TASK_ID),
            selection,
            selectionArgs,
            null, null, null
        )

        return cursor.use { it.count > 0 }
    }

    fun insertTask(task: Task): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TASK_NAME, task.name.trim())
            put(COL_TASK_CATEGORY_ID, task.categoryId)
            put(COL_TASK_COMPLETED, if (task.isCompleted) 1 else 0)
        }
        return db.insert(TABLE_TASKS, null, values)
    }

    fun updateTask(task: Task): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TASK_NAME, task.name.trim())
            put(COL_TASK_CATEGORY_ID, task.categoryId)
            put(COL_TASK_COMPLETED, if (task.isCompleted) 1 else 0)
        }
        return db.update(TABLE_TASKS, values, "$COL_TASK_ID = ?", arrayOf(task.id.toString()))
    }

    fun deleteTask(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_TASKS, "$COL_TASK_ID = ?", arrayOf(id.toString()))
    }

    fun toggleTaskCompleted(id: Long, completed: Boolean): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TASK_COMPLETED, if (completed) 1 else 0)
        }
        return db.update(TABLE_TASKS, values, "$COL_TASK_ID = ?", arrayOf(id.toString()))
    }

    // ================= EVENT OPERATIONS (SPRINT 2) =================

    // HU-05: Solo puede haber un evento en curso a la vez; y si cierro la app sigue en curso
    fun getActiveEvent(): Event? {
        val db = readableDatabase
        val query = """
            SELECT e.$COL_EVENT_ID, e.$COL_EVENT_TASK_ID, e.$COL_EVENT_START_TIME, e.$COL_EVENT_END_TIME,
                   e.$COL_EVENT_DURATION, e.$COL_EVENT_IS_ACTIVE,
                   t.$COL_TASK_NAME AS task_name, c.$COL_CAT_NAME AS cat_name, c.$COL_CAT_COLOR AS cat_color
            FROM $TABLE_EVENTS e
            INNER JOIN $TABLE_TASKS t ON e.$COL_EVENT_TASK_ID = t.$COL_TASK_ID
            INNER JOIN $TABLE_CATEGORIES c ON t.$COL_TASK_CATEGORY_ID = c.$COL_CAT_ID
            WHERE e.$COL_EVENT_IS_ACTIVE = 1
            LIMIT 1
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        return cursor.use {
            if (cursor.moveToFirst()) {
                Event(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_EVENT_ID)),
                    taskId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_EVENT_TASK_ID)),
                    startTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_START_TIME)),
                    endTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_END_TIME)),
                    durationSeconds = cursor.getLong(cursor.getColumnIndexOrThrow(COL_EVENT_DURATION)),
                    isActive = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENT_IS_ACTIVE)) == 1,
                    taskName = cursor.getString(cursor.getColumnIndexOrThrow("task_name")),
                    categoryName = cursor.getString(cursor.getColumnIndexOrThrow("cat_name")),
                    categoryColorHex = cursor.getString(cursor.getColumnIndexOrThrow("cat_color"))
                )
            } else null
        }
    }

    // HU-05: Iniciar evento
    fun startEvent(taskId: Long, startTime: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_EVENT_TASK_ID, taskId)
            put(COL_EVENT_START_TIME, startTime)
            putNull(COL_EVENT_END_TIME)
            put(COL_EVENT_DURATION, 0)
            put(COL_EVENT_IS_ACTIVE, 1)
        }
        return db.insert(TABLE_EVENTS, null, values)
    }

    // HU-05: Finalizar evento
    fun finishEvent(eventId: Long, endTime: String, durationSeconds: Long): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_EVENT_END_TIME, endTime)
            put(COL_EVENT_DURATION, durationSeconds)
            put(COL_EVENT_IS_ACTIVE, 0)
        }
        return db.update(TABLE_EVENTS, values, "$COL_EVENT_ID = ?", arrayOf(eventId.toString()))
    }

    // HU-05: Lista de todos los eventos registrados
    fun getAllEvents(): List<Event> {
        val events = mutableListOf<Event>()
        val db = readableDatabase
        val query = """
            SELECT e.$COL_EVENT_ID, e.$COL_EVENT_TASK_ID, e.$COL_EVENT_START_TIME, e.$COL_EVENT_END_TIME,
                   e.$COL_EVENT_DURATION, e.$COL_EVENT_IS_ACTIVE,
                   t.$COL_TASK_NAME AS task_name, c.$COL_CAT_NAME AS cat_name, c.$COL_CAT_COLOR AS cat_color
            FROM $TABLE_EVENTS e
            INNER JOIN $TABLE_TASKS t ON e.$COL_EVENT_TASK_ID = t.$COL_TASK_ID
            INNER JOIN $TABLE_CATEGORIES c ON t.$COL_TASK_CATEGORY_ID = c.$COL_CAT_ID
            ORDER BY e.$COL_EVENT_ID DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.use {
            val idIndex = cursor.getColumnIndexOrThrow(COL_EVENT_ID)
            val taskIdIndex = cursor.getColumnIndexOrThrow(COL_EVENT_TASK_ID)
            val startTimeIndex = cursor.getColumnIndexOrThrow(COL_EVENT_START_TIME)
            val endTimeIndex = cursor.getColumnIndexOrThrow(COL_EVENT_END_TIME)
            val durationIndex = cursor.getColumnIndexOrThrow(COL_EVENT_DURATION)
            val activeIndex = cursor.getColumnIndexOrThrow(COL_EVENT_IS_ACTIVE)
            val taskNameIndex = cursor.getColumnIndexOrThrow("task_name")
            val catNameIndex = cursor.getColumnIndexOrThrow("cat_name")
            val catColorIndex = cursor.getColumnIndexOrThrow("cat_color")

            while (cursor.moveToNext()) {
                events.add(
                    Event(
                        id = cursor.getLong(idIndex),
                        taskId = cursor.getLong(taskIdIndex),
                        startTime = cursor.getString(startTimeIndex),
                        endTime = cursor.getString(endTimeIndex),
                        durationSeconds = cursor.getLong(durationIndex),
                        isActive = cursor.getInt(activeIndex) == 1,
                        taskName = cursor.getString(taskNameIndex),
                        categoryName = cursor.getString(catNameIndex),
                        categoryColorHex = cursor.getString(catColorIndex)
                    )
                )
            }
        }
        return events
    }

    // ================= SPRINT 3: ESTADÍSTICAS SEMANALES =================

    /**
     * HU-06 Requisito técnico:
     * Los totales por categoría se calculan con una consulta SQL que agrupe los eventos
     * (SUM y GROUP BY), no sumando en un ciclo.
     * Incluye todas las categorías existentes mediante LEFT JOIN.
     */
    fun getWeeklyCategoryStats(weekStartDateTime: String, weekEndDateTime: String): List<CategoryWeeklyStat> {
        val stats = mutableListOf<CategoryWeeklyStat>()
        val db = readableDatabase
        val query = """
            SELECT c.$COL_CAT_ID AS cat_id,
                   c.$COL_CAT_NAME AS cat_name,
                   c.$COL_CAT_COLOR AS cat_color,
                   COALESCE(SUM(e.$COL_EVENT_DURATION), 0) AS total_seconds
            FROM $TABLE_CATEGORIES c
            LEFT JOIN $TABLE_TASKS t ON c.$COL_CAT_ID = t.$COL_TASK_CATEGORY_ID
            LEFT JOIN $TABLE_EVENTS e ON t.$COL_TASK_ID = e.$COL_EVENT_TASK_ID
                 AND e.$COL_EVENT_IS_ACTIVE = 0
                 AND e.$COL_EVENT_START_TIME >= ?
                 AND e.$COL_EVENT_START_TIME <= ?
            GROUP BY c.$COL_CAT_ID, c.$COL_CAT_NAME, c.$COL_CAT_COLOR
            ORDER BY c.$COL_CAT_NAME ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(weekStartDateTime, weekEndDateTime))
        cursor.use {
            val idIndex = cursor.getColumnIndexOrThrow("cat_id")
            val nameIndex = cursor.getColumnIndexOrThrow("cat_name")
            val colorIndex = cursor.getColumnIndexOrThrow("cat_color")
            val secondsIndex = cursor.getColumnIndexOrThrow("total_seconds")

            while (cursor.moveToNext()) {
                stats.add(
                    CategoryWeeklyStat(
                        categoryId = cursor.getLong(idIndex),
                        categoryName = cursor.getString(nameIndex),
                        categoryColorHex = cursor.getString(colorIndex),
                        totalSeconds = cursor.getLong(secondsIndex)
                    )
                )
            }
        }
        return stats
    }

    // ================= SPRINT 4: CIERRE SEMANAL Y REPORTES =================

    /**
     * HU-07: Comprobar si ya existe un reporte guardado para un rango de fechas.
     * "Un reporte ya guardado no se vuelve a generar ni se duplica."
     */
    fun hasWeeklyReport(weekStart: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_WEEKLY_REPORTS WHERE $COL_REPORT_WEEK_START = ?",
            arrayOf(weekStart)
        )
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) > 0 else false
        }
    }

    /**
     * HU-07: Guardar fila de reporte semanal.
     */
    fun insertWeeklyReportRow(
        weekStart: String,
        weekEnd: String,
        categoryId: Long,
        totalSeconds: Long,
        hours: Double,
        percentage: Double,
        createdAt: String
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_REPORT_WEEK_START, weekStart)
            put(COL_REPORT_WEEK_END, weekEnd)
            put(COL_REPORT_CATEGORY_ID, categoryId)
            put(COL_REPORT_TOTAL_SECONDS, totalSeconds)
            put(COL_REPORT_HOURS, hours)
            put(COL_REPORT_PERCENTAGE, percentage)
            put(COL_REPORT_CREATED_AT, createdAt)
        }
        return db.insertWithOnConflict(TABLE_WEEKLY_REPORTS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    /**
     * HU-07 Criterio de aceptación:
     * Al abrir la app, si hay semanas terminadas sin reporte, la app guarda el reporte de cada una.
     * Si el usuario no abrió la app durante varias semanas, se generan los reportes de todas las semanas pendientes.
     * Los eventos de semanas terminadas no se borran.
     */
    fun closePastWeeksAutomatically(currentMondayDate: LocalDate) {
        val db = readableDatabase
        val datePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val currentMondayStr = currentMondayDate.format(datePattern) + " 00:00:00"

        // Buscar fechas de inicio de todos los eventos finalizados de semanas pasadas
        val query = """
            SELECT DISTINCT SUBSTR($COL_EVENT_START_TIME, 1, 10) AS event_date
            FROM $TABLE_EVENTS
            WHERE $COL_EVENT_IS_ACTIVE = 0 AND $COL_EVENT_START_TIME < ?
            ORDER BY event_date ASC
        """.trimIndent()

        val pastEventDates = mutableListOf<String>()
        val cursor = db.rawQuery(query, arrayOf(currentMondayStr))
        cursor.use {
            val dateIndex = it.getColumnIndexOrThrow("event_date")
            while (it.moveToNext()) {
                pastEventDates.add(it.getString(dateIndex))
            }
        }

        // Determinar semanas únicas a cerrar
        val pastWeeksToClose = mutableSetOf<Pair<LocalDate, LocalDate>>()
        for (dateStr in pastEventDates) {
            try {
                val date = LocalDate.parse(dateStr, datePattern)
                val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val sunday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

                // Solo semanas que ya terminaron (su domingo es anterior al lunes actual)
                if (monday.isBefore(currentMondayDate)) {
                    pastWeeksToClose.add(Pair(monday, sunday))
                }
            } catch (e: Exception) {
                // ignorar fechas inválidas
            }
        }

        val nowStr = LocalDate.now().format(datePattern)
        val fullDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        for ((monday, sunday) in pastWeeksToClose) {
            val weekStart = monday.format(datePattern)
            val weekEnd = sunday.format(datePattern)

            // Si todavía no tiene reporte generado
            if (!hasWeeklyReport(weekStart)) {
                val weekStartDateTime = monday.atStartOfDay().format(fullDateTimeFormatter)
                val weekEndDateTime = sunday.atTime(23, 59, 59).format(fullDateTimeFormatter)

                val stats = getWeeklyCategoryStats(weekStartDateTime, weekEndDateTime)
                for (stat in stats) {
                    insertWeeklyReportRow(
                        weekStart = weekStart,
                        weekEnd = weekEnd,
                        categoryId = stat.categoryId,
                        totalSeconds = stat.totalSeconds,
                        hours = stat.totalHours,
                        percentage = stat.percentage,
                        createdAt = nowStr
                    )
                }
            }
        }
    }

    /**
     * HU-08 Criterio de aceptación:
     * El Spinner se llena con una consulta a la tabla de reportes, no con fechas escritas en el código.
     * Retorna pares (weekStart, weekEnd) de la más reciente a la más antigua.
     */
    fun getDistinctReportWeeks(): List<Pair<String, String>> {
        val weeks = mutableListOf<Pair<String, String>>()
        val db = readableDatabase
        val query = """
            SELECT DISTINCT $COL_REPORT_WEEK_START, $COL_REPORT_WEEK_END
            FROM $TABLE_WEEKLY_REPORTS
            ORDER BY $COL_REPORT_WEEK_START DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.use {
            val startIndex = it.getColumnIndexOrThrow(COL_REPORT_WEEK_START)
            val endIndex = it.getColumnIndexOrThrow(COL_REPORT_WEEK_END)
            while (it.moveToNext()) {
                weeks.add(Pair(it.getString(startIndex), it.getString(endIndex)))
            }
        }
        return weeks
    }

    /**
     * HU-08 Criterio de aceptación:
     * Al elegir una semana se muestran sus categorías con horas y porcentaje sobre 168 horas.
     */
    fun getReportItemsForWeek(weekStart: String): List<WeeklyReportItem> {
        val items = mutableListOf<WeeklyReportItem>()
        val db = readableDatabase
        val query = """
            SELECT wr.$COL_REPORT_ID, wr.$COL_REPORT_WEEK_START, wr.$COL_REPORT_WEEK_END,
                   wr.$COL_REPORT_CATEGORY_ID, wr.$COL_REPORT_TOTAL_SECONDS, wr.$COL_REPORT_HOURS,
                   wr.$COL_REPORT_PERCENTAGE, c.$COL_CAT_NAME AS cat_name, c.$COL_CAT_COLOR AS cat_color
            FROM $TABLE_WEEKLY_REPORTS wr
            INNER JOIN $TABLE_CATEGORIES c ON wr.$COL_REPORT_CATEGORY_ID = c.$COL_CAT_ID
            WHERE wr.$COL_REPORT_WEEK_START = ?
            ORDER BY c.$COL_CAT_NAME ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(weekStart))
        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(COL_REPORT_ID)
            val startIndex = it.getColumnIndexOrThrow(COL_REPORT_WEEK_START)
            val endIndex = it.getColumnIndexOrThrow(COL_REPORT_WEEK_END)
            val catIdIndex = it.getColumnIndexOrThrow(COL_REPORT_CATEGORY_ID)
            val secondsIndex = it.getColumnIndexOrThrow(COL_REPORT_TOTAL_SECONDS)
            val hoursIndex = it.getColumnIndexOrThrow(COL_REPORT_HOURS)
            val percentIndex = it.getColumnIndexOrThrow(COL_REPORT_PERCENTAGE)
            val catNameIndex = it.getColumnIndexOrThrow("cat_name")
            val catColorIndex = it.getColumnIndexOrThrow("cat_color")

            while (it.moveToNext()) {
                items.add(
                    WeeklyReportItem(
                        id = it.getLong(idIndex),
                        weekStart = it.getString(startIndex),
                        weekEnd = it.getString(endIndex),
                        categoryId = it.getLong(catIdIndex),
                        categoryName = it.getString(catNameIndex),
                        categoryColorHex = it.getString(catColorIndex),
                        totalSeconds = it.getLong(secondsIndex),
                        hours = it.getDouble(hoursIndex),
                        percentage = it.getDouble(percentIndex)
                    )
                )
            }
        }
        return items
    }

    /**
     * HU-08 Criterio de aceptación:
     * Debajo se muestra la lista de eventos de esa semana con tarea, categoría, inicio, fin y duración.
     */
    fun getEventsForWeek(weekStartDateTime: String, weekEndDateTime: String): List<Event> {
        val events = mutableListOf<Event>()
        val db = readableDatabase
        val query = """
            SELECT e.$COL_EVENT_ID, e.$COL_EVENT_TASK_ID, e.$COL_EVENT_START_TIME, e.$COL_EVENT_END_TIME,
                   e.$COL_EVENT_DURATION, e.$COL_EVENT_IS_ACTIVE,
                   t.$COL_TASK_NAME AS task_name, c.$COL_CAT_NAME AS cat_name, c.$COL_CAT_COLOR AS cat_color
            FROM $TABLE_EVENTS e
            INNER JOIN $TABLE_TASKS t ON e.$COL_EVENT_TASK_ID = t.$COL_TASK_ID
            INNER JOIN $TABLE_CATEGORIES c ON t.$COL_TASK_CATEGORY_ID = c.$COL_CAT_ID
            WHERE e.$COL_EVENT_IS_ACTIVE = 0
              AND e.$COL_EVENT_START_TIME >= ?
              AND e.$COL_EVENT_START_TIME <= ?
            ORDER BY e.$COL_EVENT_START_TIME DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(weekStartDateTime, weekEndDateTime))
        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(COL_EVENT_ID)
            val taskIdIndex = it.getColumnIndexOrThrow(COL_EVENT_TASK_ID)
            val startTimeIndex = it.getColumnIndexOrThrow(COL_EVENT_START_TIME)
            val endTimeIndex = it.getColumnIndexOrThrow(COL_EVENT_END_TIME)
            val durationIndex = it.getColumnIndexOrThrow(COL_EVENT_DURATION)
            val activeIndex = it.getColumnIndexOrThrow(COL_EVENT_IS_ACTIVE)
            val taskNameIndex = it.getColumnIndexOrThrow("task_name")
            val catNameIndex = it.getColumnIndexOrThrow("cat_name")
            val catColorIndex = it.getColumnIndexOrThrow("cat_color")

            while (it.moveToNext()) {
                events.add(
                    Event(
                        id = it.getLong(idIndex),
                        taskId = it.getLong(taskIdIndex),
                        startTime = it.getString(startTimeIndex),
                        endTime = it.getString(endTimeIndex),
                        durationSeconds = it.getLong(durationIndex),
                        isActive = it.getInt(activeIndex) == 1,
                        taskName = it.getString(taskNameIndex),
                        categoryName = it.getString(catNameIndex),
                        categoryColorHex = it.getString(catColorIndex)
                    )
                )
            }
        }
        return events
    }
}


