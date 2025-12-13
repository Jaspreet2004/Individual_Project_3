package com.example.individualproject3.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individualproject3.data.CustomLevel
import com.example.individualproject3.data.GameDao
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

    data class BuilderState(
    val rows: Int = 6,
    val cols: Int = 6,
    val walls: Set<GridPosition> = emptySet(),
    val coins: Set<GridPosition> = emptySet(),
    val startPos: GridPosition = GridPosition(0, 0),
    val endPos: GridPosition = GridPosition(5, 5),
    val selectedTool: CellType = CellType.WALL
)

class LevelBuilderViewModel(private val gameDao: GameDao) : ViewModel() {
    private val _uiState = MutableStateFlow(BuilderState())
    val uiState: StateFlow<BuilderState> = _uiState.asStateFlow()

    private var currentLevelId: Int? = null

    fun loadLevel(levelId: Int) {
        viewModelScope.launch {
            val level = gameDao.getCustomLevelById(levelId)
            level?.let {
                val gson = Gson()
                val wallsType = object : com.google.gson.reflect.TypeToken<List<List<Int>>>() {}.type
                val coinsType = object : com.google.gson.reflect.TypeToken<List<List<Int>>>() {}.type

                val wallsList: List<List<Int>> = gson.fromJson(it.wallsJson, wallsType) ?: emptyList()
                val coinsList: List<List<Int>> = gson.fromJson(it.coinsJson, coinsType) ?: emptyList()

                val walls = wallsList.map { pair -> GridPosition(pair[0], pair[1]) }.toSet()
                val coins = coinsList.map { pair -> GridPosition(pair[0], pair[1]) }.toSet()

                currentLevelId = it.id
                _uiState.value = BuilderState(
                    rows = it.rows,
                    cols = it.cols,
                    walls = walls,
                    coins = coins,
                    startPos = GridPosition(it.startRow, it.startCol),
                    endPos = GridPosition(it.endRow, it.endCol),
                    selectedTool = CellType.WALL // Default tool
                )
            }
        }
    }

    fun onToolSelected(tool: CellType) {
        _uiState.value = _uiState.value.copy(selectedTool = tool)
    }

    fun onCellClicked(row: Int, col: Int) {
        val pos = GridPosition(row, col)
        val currentState = _uiState.value
        
        // Prevent modifying start/end if they are overlapping (basic validation, simplified)
        
        when (currentState.selectedTool) {
            CellType.WALL -> {
                val newWalls = currentState.walls.toMutableSet()
                if (newWalls.contains(pos)) newWalls.remove(pos) else newWalls.add(pos)
                // Remove coin if wall placed
                val newCoins = currentState.coins.toMutableSet().apply { remove(pos) }
                _uiState.value = currentState.copy(walls = newWalls, coins = newCoins)
            }
            CellType.COIN -> {
                val newCoins = currentState.coins.toMutableSet()
                if (newCoins.contains(pos)) newCoins.remove(pos) else newCoins.add(pos)
                // Remove wall if coin placed
                val newWalls = currentState.walls.toMutableSet().apply { remove(pos) }
                _uiState.value = currentState.copy(coins = newCoins, walls = newWalls)
            }
            CellType.START -> {
                _uiState.value = currentState.copy(startPos = pos)
            }
            CellType.END -> {
                _uiState.value = currentState.copy(endPos = pos)
            }
            else -> {} // Empty/Eraser handled by toggling wall/coin off usually, or specific erase tool
        }
    }

    fun saveLevel(name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val gson = Gson()
            
            // Serialize sets to List of Lists [[r,c], [r,c]] or similar JSON format
            // Using logic.GridPosition(r,c)
            
            val wallsList = state.walls.map { listOf(it.row, it.col) }
            val coinsList = state.coins.map { listOf(it.row, it.col) }

            val levelToSave = CustomLevel(
                id = currentLevelId ?: 0, // Use existing ID if updating, else 0 for auto-gen
                name = name,
                rows = state.rows,
                cols = state.cols,
                startRow = state.startPos.row,
                startCol = state.startPos.col,
                endRow = state.endPos.row,
                endCol = state.endPos.col,
                wallsJson = gson.toJson(wallsList),
                coinsJson = gson.toJson(coinsList)
            )
            
            if (currentLevelId != null && currentLevelId != 0) {
                gameDao.updateCustomLevel(levelToSave)
            } else {
                gameDao.insertCustomLevel(levelToSave)
            }
            onSuccess()
        }
    }
}
