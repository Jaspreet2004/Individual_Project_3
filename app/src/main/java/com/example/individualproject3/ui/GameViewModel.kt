package com.example.individualproject3.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.individualproject3.data.GameDao
import com.example.individualproject3.data.GameSession
import com.example.individualproject3.logic.Command
import com.example.individualproject3.logic.GameEngine
import com.example.individualproject3.logic.LevelData
import com.example.individualproject3.logic.Levels
import com.example.individualproject3.util.FileLogger
import com.example.individualproject3.util.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Game Screen.
 * Connects the UI with the GameEngine, manages command queue state, and handles data persistence.
 *
 * @property levelId The ID of the loaded level.
 * @param gameDao DAO for saving game results.
 * @param context Application context for SoundManager.
 */
class GameViewModel(
    private val levelId: String,
    private val gameDao: GameDao,
    context: Context
) : ViewModel() {
    
    // logic setup
    private val level = Levels.AllLevels.find { it.id == levelId } ?: Levels.Level1_1
    private val engine = GameEngine(level)
    private val soundManager = SoundManager(context)
    
    // State exposed to UI
    val gameState = engine.gameState
    
    // Command Queue State
    private val _commandQueue = MutableStateFlow<List<Command>>(emptyList())
    val commandQueue = _commandQueue.asStateFlow()

    // Execution State
    private val _isExecuting = MutableStateFlow(false)
    val isExecuting = _isExecuting.asStateFlow()

    /**
     * Adds a command to the queue if not currently executing.
     */
    fun addCommand(command: Command) {
        if (!_isExecuting.value) {
            _commandQueue.value = _commandQueue.value + command
        }
    }

    /**
     * Removes a command from the queue at a specific index.
     */
    fun removeCommand(index: Int) {
        if (!_isExecuting.value) {
            val list = _commandQueue.value.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
                _commandQueue.value = list
            }
        }
    }

    /**
     * Restarts the game and clears the command queue.
     */
    fun resetGame() {
        if (!_isExecuting.value) {
            engine.reset()
            _commandQueue.value = emptyList()
        }
    }

    /**
     * Starts execution of the current command queue.
     * Handles sound effects and result logging upon completion.
     */
    fun executeCommands() {
        if (_isExecuting.value) return
        _isExecuting.value = true
        
        viewModelScope.launch {
            engine.executeCommands(_commandQueue.value) { event ->
                if (event == "coin") {
                    soundManager.playCoinSound()
                }
            }
            
            // Check result after execution
            val finalState = gameState.value
            if (finalState.isWin) {
                soundManager.playWinSound()
                logResult(true)
            } else if (finalState.isGameOver) {
                soundManager.playLoseSound()
                logResult(false)
            }
            _isExecuting.value = false
        }
    }

    private fun logResult(success: Boolean) {
        viewModelScope.launch {
             try {
                 // DB Log to Room Database
                 gameDao.insertSession(
                    GameSession(
                        userId = 1,
                        levelName = level.name,
                        score = if (success) 100 else 0,
                        timestamp = System.currentTimeMillis(),
                        isCompleted = success
                    )
                )
             } catch (e: Exception) {
                 e.printStackTrace()
             }
        }
        // File Log for debugging
        FileLogger.log(
            context = getContextForLogger(),
            message = "Level ${level.name} - ${if (success) "Win" else "Fail"}"
        )
    }
    
    // Workaround for logging context
    private fun getContextForLogger(): Context {
        return (soundManager.javaClass.getDeclaredField("context").apply { isAccessible = true }.get(soundManager) as Context)
    }

    fun playMoveSound() {
        soundManager.playMoveSound()
    }

    fun playClickSound() {
        soundManager.playClickSound()
    }
}

class GameViewModelFactory(
    private val levelId: String,
    private val gameDao: GameDao,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(levelId, gameDao, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
