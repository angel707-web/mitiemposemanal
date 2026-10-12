package com.senati.mitiemposemanal.Bflor.model

data class Category(
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#5865F2"
) {
    override fun toString(): String {
        return name
    }
}
