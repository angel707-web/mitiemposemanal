package com.senati.mitiemposemanal.Bflor

import com.senati.mitiemposemanal.Bflor.model.CategoryWeeklyStat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class Sprint3LogicTest {

    // HU-06: Ver el porcentaje semanal por categoría
    // Regla de negocio: Porcentaje de una categoría = minutos de la categoría en la semana ÷ 10 080 × 100
    // Ejemplo del documento: 21 horas de Estudio en la semana -> 21 ÷ 168 × 100 = 12.5 %
    @Test
    fun testWeeklyCategoryPercentageCalculationExample() {
        val twentyOneHoursInSeconds = 21L * 3600L // 21 horas

        val stat = CategoryWeeklyStat(
            categoryId = 1,
            categoryName = "Estudio",
            categoryColorHex = "#6C5CE7",
            totalSeconds = twentyOneHoursInSeconds
        )

        assertEquals(21.0, stat.totalHours, 0.001)
        assertEquals(12.5, stat.percentage, 0.001)
        assertEquals("21.0 h", stat.getFormattedHours())
        assertEquals("12.5 %", stat.getFormattedPercentage())
    }

    @Test
    fun testUnregisteredHoursAndPercentageCalculation() {
        val weekTotalSeconds = 168L * 3600L // 168 horas = 604,800 segundos
        val registeredSeconds = 21L * 3600L

        val unregisteredSeconds = weekTotalSeconds - registeredSeconds
        val unregisteredHours = unregisteredSeconds / 3600.0
        val unregisteredPercentage = (unregisteredSeconds.toDouble() / weekTotalSeconds.toDouble()) * 100.0

        assertEquals(147.0, unregisteredHours, 0.001)
        assertEquals(87.5, unregisteredPercentage, 0.001)

        val formattedHours = String.format(Locale.US, "%.1f h", unregisteredHours)
        val formattedPercent = String.format(Locale.US, "%.1f %%", unregisteredPercentage)

        assertEquals("147.0 h", formattedHours)
        assertEquals("87.5 %", formattedPercent)
        assertEquals(100.0, 12.5 + unregisteredPercentage, 0.001)
    }

    @Test
    fun testWeekRangeCalculationMondayToSunday() {
        // Test date: Monday 2026-10-12
        val mondayDate = LocalDate.of(2026, 10, 12)
        assertEquals(DayOfWeek.MONDAY, mondayDate.dayOfWeek)

        val monday = mondayDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = mondayDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val rangeString = "Semana ${monday.format(formatter)} – ${sunday.format(formatter)}"

        assertEquals("Semana 12/10/2026 – 18/10/2026", rangeString)
    }

    @Test
    fun testDynamicCategoriesHandling() {
        // If a new category is added, each category receives its calculation without hardcoded names
        val categoryStats = listOf(
            CategoryWeeklyStat(1, "Estudio", "#6C5CE7", 36000L),    // 10 h
            CategoryWeeklyStat(2, "Trabajo", "#E84393", 72000L),    // 20 h
            CategoryWeeklyStat(3, "Descanso", "#FF7675", 180000L),  // 50 h
            CategoryWeeklyStat(4, "Ocio", "#00CEC9", 36000L),       // 10 h
            CategoryWeeklyStat(5, "Universidad", "#0984E3", 18000L) // 5 h (nueva categoría Sprint 1)
        )

        assertEquals(5, categoryStats.size)
        val totalSecs = categoryStats.sumOf { it.totalSeconds }
        val totalHours = totalSecs / 3600.0
        assertEquals(95.0, totalHours, 0.001)

        val totalPercentage = categoryStats.sumOf { it.percentage }
        assertTrue("Total percentage should be <= 100%", totalPercentage <= 100.0)
    }
}
