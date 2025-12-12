package com.example.individualproject3.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "game_sessions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GameSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val levelName: String,
    val score: Int,
    val timestamp: Long,
    val isCompleted: Boolean
)
