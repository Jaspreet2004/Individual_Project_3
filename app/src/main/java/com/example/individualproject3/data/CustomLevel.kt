package com.example.individualproject3.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_levels")
data class CustomLevel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val rows: Int,
    val cols: Int,
    val wallsJson: String, // Stored as JSON string "[[0,1],[2,2]]"
    val coinsJson: String,
    val startRow: Int,
    val startCol: Int,
    val endRow: Int,
    val endCol: Int
)
