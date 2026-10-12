package com.senati.mitiemposemanal.Bflor.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.senati.mitiemposemanal.Bflor.model.Category
import com.senati.mitiemposemanal.Bflor.model.Task

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mitiemposemanal.db"
        private const val DATABASE_VERSION = 1

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

        db.execSQL(createCategoriesTable)
        db.execSQL(createTasksTable)

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
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        onCreate(db)
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
}
