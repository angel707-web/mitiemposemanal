package com.senati.mitiemposemanal.Bflor.model

import java.util.Locale

data class WeeklyReportItem(
    val id: Long = 0,
    val weekStart: String,            // Fecha lunes "yyyy-MM-dd"
    val weekEnd: String,              // Fecha domingo "yyyy-MM-dd"
    val categoryId: Long,
    val categoryName: String = "",
    val categoryColorHex: String = "#5865F2",
    val totalSeconds: Long,
    val hours: Double,
    val percentage: Double
) {
    fun getFormattedHours(): String = String.format(Locale.US, "%.1f h", hours)
    fun getFormattedPercentage(): String = String.format(Locale.US, "%.1f %%", percentage)
}
