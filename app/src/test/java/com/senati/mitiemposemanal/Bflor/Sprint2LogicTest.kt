package com.senati.mitiemposemanal.Bflor

import com.senati.mitiemposemanal.Bflor.model.Category
import com.senati.mitiemposemanal.Bflor.model.Event
import com.senati.mitiemposemanal.Bflor.model.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Sprint2LogicTest {

    // HU-03: Buscar tarea mientras escribo
    @Test
    fun testTaskSearchThresholdAndMatching() {
        val tasks = listOf(
            Task(id = 1, name = "Leer Kotlin", categoryId = 1, categoryName = "Estudio"),
            Task(id = 2, name = "Crear la base de datos", categoryId = 2, categoryName = "Trabajo"),
            Task(id = 3, name = "Hacer ejercicio", categoryId = 3, categoryName = "Descanso"),
            Task(id = 4, name = "Pasar de nivel", categoryId = 4, categoryName = "Ocio")
        )

        // Threshold check: 2 letters or more
        val queryShort = "k"
        val shouldSearchShort = queryShort.trim().length >= 2
        assertFalse("Single letter should not trigger threshold search", shouldSearchShort)

        val queryValid = "ko"
        val shouldSearchValid = queryValid.trim().length >= 2
        assertTrue("Two letters should trigger search", shouldSearchValid)

        // Matching: "ko" matches "Leer Kotlin"
        val matches = tasks.filter { it.name.contains(queryValid, ignoreCase = true) }
        assertEquals(1, matches.size)
        assertEquals("Leer Kotlin", matches[0].name)
        assertEquals("Estudio", matches[0].categoryName)
    }

    // HU-04: Crear la tarea si no existe
    @Test
    fun testCreateTaskIfNotFound() {
        val existingTasks = listOf(
            Task(id = 1, name = "Leer Kotlin", categoryId = 1)
        )
        val writtenName = "Programar UI"

        val found = existingTasks.find { it.name.equals(writtenName, ignoreCase = true) }
        assertNull("Task does not exist initially", found)

        // Simulating creation with selected category
        val categoryTrabajo = Category(id = 2, name = "Trabajo", colorHex = "#E84393")
        val createdTask = Task(
            id = 5,
            name = writtenName.trim(),
            categoryId = categoryTrabajo.id,
            categoryName = categoryTrabajo.name,
            categoryColorHex = categoryTrabajo.colorHex
        )

        assertEquals("Programar UI", createdTask.name)
        assertEquals(2L, createdTask.categoryId)
        assertEquals("Trabajo", createdTask.categoryName)
    }

    // HU-05: Iniciar y finalizar un evento
    @Test
    fun testEventCreationAndDurationCalculation() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val startDateTime = LocalDateTime.of(2026, 10, 12, 10, 0, 0)
        val endDateTime = LocalDateTime.of(2026, 10, 12, 10, 45, 30)

        val startStr = startDateTime.format(formatter)
        val endStr = endDateTime.format(formatter)

        val durationSeconds = Duration.between(startDateTime, endDateTime).seconds
        assertEquals(2730L, durationSeconds) // 45 * 60 + 30 = 2730s

        val event = Event(
            id = 1,
            taskId = 1,
            startTime = startStr,
            endTime = endStr,
            durationSeconds = durationSeconds,
            isActive = false,
            taskName = "Leer Kotlin",
            categoryName = "Estudio",
            categoryColorHex = "#6C5CE7"
        )

        assertEquals("45:30", event.getFormattedDuration())
        assertFalse(event.isActive)
        assertEquals("2026-10-12 10:00:00", event.startTime)
        assertEquals("2026-10-12 10:45:30", event.endTime)
    }

    @Test
    fun testEventLongDurationFormatting() {
        val event = Event(
            id = 2,
            taskId = 2,
            startTime = "2026-10-12 08:00:00",
            endTime = "2026-10-12 09:15:05",
            durationSeconds = 4505, // 1h 15m 5s
            isActive = false
        )

        assertEquals("01:15:05", event.getFormattedDuration())
    }

    @Test
    fun testSingleActiveEventRule() {
        val events = mutableListOf(
            Event(id = 1, taskId = 1, startTime = "2026-10-12 10:00:00", isActive = false),
            Event(id = 2, taskId = 2, startTime = "2026-10-12 11:00:00", isActive = true)
        )

        val activeEvents = events.filter { it.isActive }
        assertEquals("Only one event can be active", 1, activeEvents.size)
        assertEquals(2L, activeEvents[0].id)
    }
}
