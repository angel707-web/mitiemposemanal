package com.senati.mitiemposemanal.Bflor.model

data class Task(
    val id: Long = 0,
    val name: String,
    val categoryId: Long,
    val isCompleted: Boolean = false,
    val categoryName: String = "",
    val categoryColorHex: String = "#5865F2"
)
