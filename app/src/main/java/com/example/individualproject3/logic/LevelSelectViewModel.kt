package com.example.individualproject3.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individualproject3.data.GameDao
import com.google.gson.Gson
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class LevelSelectViewModel(gameDao: GameDao) : ViewModel() {
    
    val allLevels: StateFlow<List<LevelData>> = gameDao.getAllCustomLevels()
        .map { customLevels ->
            val convertedCustomLevels = customLevels.map { custom ->
                val gson = Gson()
                val wallsType = object : com.google.gson.reflect.TypeToken<List<List<Int>>>() {}.type
                val coinsType = object : com.google.gson.reflect.TypeToken<List<List<Int>>>() {}.type

                val wallsList: List<List<Int>> = gson.fromJson(custom.wallsJson, wallsType) ?: emptyList()
                val coinsList: List<List<Int>> = gson.fromJson(custom.coinsJson, coinsType) ?: emptyList()

                LevelData(
                    id = "custom_${custom.id}",
                    name = custom.name,
                    rows = custom.rows,
                    cols = custom.cols,
                    startPos = GridPosition(custom.startRow, custom.startCol),
                    endPos = GridPosition(custom.endRow, custom.endCol),
                    walls = wallsList.map { GridPosition(it[0], it[1]) },
                    coins = coinsList.map { GridPosition(it[0], it[1]) }
                )
            }
            Levels.AllLevels + convertedCustomLevels
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Levels.AllLevels
        )
}
