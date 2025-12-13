package com.example.individualproject3.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individualproject3.data.CustomLevel
import com.example.individualproject3.data.GameDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class LevelListViewModel(gameDao: GameDao) : ViewModel() {
    val customLevels: StateFlow<List<CustomLevel>> = gameDao.getAllCustomLevels()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
