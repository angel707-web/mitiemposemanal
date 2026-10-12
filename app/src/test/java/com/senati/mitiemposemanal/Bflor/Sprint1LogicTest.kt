package com.senati.mitiemposemanal.Bflor

import com.senati.mitiemposemanal.Bflor.model.Category
import com.senati.mitiemposemanal.Bflor.model.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Sprint1LogicTest {

    // HU-01: Gestionar categorías
    @Test
    fun testDefaultCategoriesData() {
        val defaultCategories = listOf(
            Category(1, "Estudio", "#6C5CE7"),
            Category(2, "Trabajo", "#E84393"),
            Category(3, "Descanso", "#FF7675"),
            Category(4, "Ocio", "#00CEC9")
        )

        assertEquals(4, defaultCategories.size)
        assertEquals("Estudio", defaultCategories[0].name)
        assertEquals("Trabajo", defaultCategories[1].name)
        assertEquals("Descanso", defaultCategories[2].name)
        assertEquals("Ocio", defaultCategories[3].name)
    }

    @Test
    fun testCategoryCreationAndValidation() {
        val categoryName = " Universidad "
        assertTrue("Category name cannot be blank", categoryName.trim().isNotEmpty())

        val category = Category(id = 5, name = categoryName.trim(), colorHex = "#0984E3")
        assertEquals("Universidad", category.name)
        assertEquals("#0984E3", category.colorHex)
        assertEquals("Universidad", category.toString())
    }

    @Test
    fun testCategoryDuplicateDetectionLogic() {
        val existingCategories = listOf("Estudio", "Trabajo", "Descanso", "Ocio")
        val newNameInput = "estudio" // Case insensitive check

        val isDuplicate = existingCategories.any { it.equals(newNameInput.trim(), ignoreCase = true) }
        assertTrue("Duplicate category name must be detected", isDuplicate)

        val uniqueNameInput = "Deportes"
        val isUniqueDuplicate = existingCategories.any { it.equals(uniqueNameInput.trim(), ignoreCase = true) }
        assertFalse("Unique category should not be duplicate", isUniqueDuplicate)
    }

    // HU-02: Gestionar tareas con categoría
    @Test
    fun testTaskCreationAndValidation() {
        val emptyTaskName = "   "
        assertTrue("Empty task name should be rejected", emptyTaskName.trim().isEmpty())

        val validTaskName = "Leer Kotlin"
        val categoryId = 1L
        val task = Task(
            id = 1,
            name = validTaskName.trim(),
            categoryId = categoryId,
            isCompleted = false,
            categoryName = "Estudio",
            categoryColorHex = "#6C5CE7"
        )

        assertEquals("Leer Kotlin", task.name)
        assertEquals(1L, task.categoryId)
        assertEquals("Estudio", task.categoryName)
        assertFalse(task.isCompleted)
    }

    @Test
    fun testTaskDuplicateDetectionLogic() {
        val existingTasks = listOf("Leer Kotlin", "Crear la base de datos", "Hacer ejercicio")
        val duplicateInput = "leer kotlin"

        val isDuplicate = existingTasks.any { it.equals(duplicateInput.trim(), ignoreCase = true) }
        assertTrue("Duplicate task name must be detected", isDuplicate)

        val newUniqueTask = "Pasar de nivel"
        val isUniqueDuplicate = existingTasks.any { it.equals(newUniqueTask.trim(), ignoreCase = true) }
        assertFalse("Unique task should not be duplicate", isUniqueDuplicate)
    }

    @Test
    fun testTaskCategoryFormattingCriteria() {
        // Criterio de aceptación 4: La lista muestra cada tarea con su categoría, por ejemplo: "Leer Kotlin · Estudio"
        val task = Task(
            id = 1,
            name = "Leer Kotlin",
            categoryId = 1,
            categoryName = "Estudio"
        )

        val formattedDisplay = "${task.name} · ${task.categoryName}"
        assertEquals("Leer Kotlin · Estudio", formattedDisplay)
    }
}
