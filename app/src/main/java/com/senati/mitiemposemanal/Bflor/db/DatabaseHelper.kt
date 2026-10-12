package com.senati.mitiemposemanal.Bflor.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.senati.mitiemposemanal.Bflor.model.Category
import com.senati.mitiemposemanal.Bflor.model.Event
import com.senati.mitiemposemanal.Bflor.model.Task

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mitiemposemanal.db"
        private const val DATABASE_VERSION = 2

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

        db.execSQL(createCategoriesTable)
        db.execSQL(createTasksTable)
        db.execSQL(createEventsTable)

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
}
