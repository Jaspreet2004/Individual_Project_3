package com.example.individualproject3.logic

sealed class Command(val name: String) {
    object Up : Command("Up")
    object Down : Command("Down")
    object Left : Command("Left")
    object Right : Command("Right")
}
