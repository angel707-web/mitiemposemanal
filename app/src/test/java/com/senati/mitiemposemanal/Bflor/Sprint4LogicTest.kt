package com.senati.mitiemposemanal.Bflor

import com.senati.mitiemposemanal.Bflor.model.WeeklyReportItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class Sprint4LogicTest {

    // HU-07: Cerrar la semana automáticamente
    @Test
    fun testPastWeekDetectionForClosure() {
        val currentMonday = LocalDate.of(2026, 10, 12) // Lunes actual

        // Event from previous week: Wednesday 2026-10-07
        val pastEventDate = LocalDate.of(2026, 10, 7)
        val pastMonday = pastEventDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val pastSunday = pastEventDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        assertTrue("Past event Monday must be before current Monday", pastMonday.isBefore(currentMonday))
        assertEquals("2026-10-05", pastMonday.toString())
        assertEquals("2026-10-11", pastSunday.toString())

        // Event from current week: Tuesday 2026-10-13
        val currentWeekEventDate = LocalDate.of(2026, 10, 13)
        val currentWeekMonday = currentWeekEventDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        assertFalse("Current week Monday should not be before current Monday", currentWeekMonday.isBefore(currentMonday))
    }

    @Test
    fun testNoDuplicateReportLogic() {
        val existingReportWeeks = mutableSetOf<String>()
        val weekStart = "2026-10-05"

        val canGenerateFirst = existingReportWeeks.add(weekStart)
        assertTrue("First attempt should be allowed", canGenerateFirst)

        val canGenerateSecond = existingReportWeeks.add(weekStart)
        assertFalse("Duplicate week report generation should be prevented", canGenerateSecond)
    }

    // HU-08: Consultar reportes por rango de fechas
    @Test
    fun testSpinnerDateFormatting() {
        val dbDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        val weekStartStr = "2026-10-05"
        val weekEndStr = "2026-10-11"

        val startFormatted = LocalDate.parse(weekStartStr, dbDateFormatter).format(displayDateFormatter)
        val endFormatted = LocalDate.parse(weekEndStr, dbDateFormatter).format(displayDateFormatter)
        val displayLabel = "$startFormatted – $endFormatted"

        assertEquals("05/10/2026 – 11/10/2026", displayLabel)
    }

    @Test
    fun testWeeklyReportItemFormattingAndCalculations() {
        val reportItem = WeeklyReportItem(
            id = 1,
            weekStart = "2026-10-05",
            weekEnd = "2026-10-11",
            categoryId = 1,
            categoryName = "Estudio",
            categoryColorHex = "#6C5CE7",
            totalSeconds = 21L * 3600L, // 21 horas
            hours = 21.0,
            percentage = 12.5
        )

        assertEquals("21.0 h", reportItem.getFormattedHours())
        assertEquals("12.5 %", reportItem.getFormattedPercentage())
    }

    @Test
    fun testWeeklyReportUnregisteredTimeCalculation() {
        val weekTotalSeconds = 168L * 3600L
        val registeredSeconds = 42L * 3600L // 42 horas registradas

        val unregisteredSeconds = weekTotalSeconds - registeredSeconds
        val unregHours = unregisteredSeconds / 3600.0
        val unregPercent = (unregisteredSeconds.toDouble() / weekTotalSeconds.toDouble()) * 100.0

        assertEquals(126.0, unregHours, 0.001)
        assertEquals(75.0, unregPercent, 0.001)

        val hoursStr = String.format(Locale.US, "%.1f h", unregHours)
        val percentStr = String.format(Locale.US, "%.1f %%", unregPercent)

        assertEquals("126.0 h", hoursStr)
        assertEquals("75.0 %", percentStr)
    }
}
