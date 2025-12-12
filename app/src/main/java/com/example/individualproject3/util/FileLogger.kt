package com.example.individualproject3.util

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {
    private const val FILE_NAME = "game_log.txt"

    fun log(context: Context, message: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val logEntry = "[$timestamp] $message\n"
        
        try {
            val file = File(context.filesDir, FILE_NAME)
            FileOutputStream(file, true).use {
                it.write(logEntry.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readLogs(context: Context): String {
        val file = File(context.filesDir, FILE_NAME)
        return if (file.exists()) file.readText() else "No logs found."
    }
}
