package com.example.individualproject3.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individualproject3.data.GameDao
import com.example.individualproject3.data.GameSession
import com.example.individualproject3.data.LevelAttemptStats
import com.example.individualproject3.data.UserDao
import com.example.individualproject3.util.FileLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ParentDashboardViewModel(
    private val gameDao: GameDao,
    private val userDao: UserDao,
    private val context: Context // Need context for FileLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentDashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Fetch aggregated stats for the chart
            val stats = gameDao.getLevelAttemptStats()
            
            // Map DB results to a map for quick lookup
            val statsMap = stats.associate { it.levelName to it.attemptCount }
            
            // Remakes the list to include All levels
            val completeStats = com.example.individualproject3.logic.Levels.AllLevels.map { level ->
                LevelAttemptStats(
                    levelName = level.name,
                    attemptCount = statsMap[level.name] ?: 0
                )
            }
            
            _uiState.value = _uiState.value.copy(levelStats = completeStats)
        }
    }
}

data class ParentDashboardUiState(
    val levelStats: List<LevelAttemptStats> = emptyList()
)
