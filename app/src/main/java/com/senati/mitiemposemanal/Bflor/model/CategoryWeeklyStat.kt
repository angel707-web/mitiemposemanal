package com.senati.mitiemposemanal.Bflor.model

import java.util.Locale

data class CategoryWeeklyStat(
    val categoryId: Long,
    val categoryName: String,
    val categoryColorHex: String,
    val totalSeconds: Long
) {
    val totalHours: Double
        get() = totalSeconds / 3600.0

    // Porcentaje sobre las 168 horas de la semana (168 * 3600 = 604,800 segundos = 10,080 minutos)
    val percentage: Double
        get() = (totalSeconds.toDouble() / 604800.0) * 100.0

    fun getFormattedHours(): String {
        return String.format(Locale.US, "%.1f h", totalHours)
    }

    fun getFormattedPercentage(): String {
        return String.format(Locale.US, "%.1f %%", percentage)
    }
}
