package com.senati.mitiemposemanal.Bflor.model

data class Event(
    val id: Long = 0,
    val taskId: Long,
    val startTime: String,             // Formato completo fecha y hora "yyyy-MM-dd HH:mm:ss"
    val endTime: String? = null,        // Formato completo fecha y hora "yyyy-MM-dd HH:mm:ss"
    val durationSeconds: Long = 0,
    val isActive: Boolean = true,
    val taskName: String = "",
    val categoryName: String = "",
    val categoryColorHex: String = "#5865F2"
) {
    fun getFormattedDuration(): String {
        val totalSecs = durationSeconds
        val hours = totalSecs / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
