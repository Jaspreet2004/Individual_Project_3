package com.example.individualproject3.logic

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents the state of the game at any moment.
 *
 * @property currentPos The player's current grid position.
 * @property collectedCoins Set of coin positions collected so far.
 * @property isGameOver True if the game ends (win or loss).
 * @property isWin True if the game ends in a win.
 * @property error Error message if the game ended in lose.
 */
data class GameState(
    val currentPos: GridPosition,
    val collectedCoins: Set<GridPosition> = emptySet(),
    val isGameOver: Boolean = false,
    val isWin: Boolean = false,
    val error: String? = null
)

/**
 * Core Logic Engine for the game.
 * Simulates movements, checks collisions, and updates game state.
 *
 * @param level The static data for the level being played.
 */
class GameEngine(private val level: LevelData) {
    private val _gameState = MutableStateFlow(
        GameState(currentPos = level.startPos)
    )
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    fun reset() {
        _gameState.value = GameState(currentPos = level.startPos)
    }

    /**
     * Executes a sequence of commands.
     * Updates state after each step and introduces delay for animation.
     *
     * @param commands The list of commands to run.
     * @param onEvent Callback for special events.
     */
    suspend fun executeCommands(commands: List<Command>, onEvent: (String) -> Unit = {}) {
        // Reset state before run
        _gameState.value = GameState(currentPos = level.startPos)
        
        // Waits a few seconds before starting
        delay(500)

        for (cmd in commands) {
            if (_gameState.value.isGameOver) break

            val current = _gameState.value.currentPos
            val nextPos = when (cmd) {
                Command.Up -> current.copy(row = current.row - 1)
                Command.Down -> current.copy(row = current.row + 1)
                Command.Left -> current.copy(col = current.col - 1)
                Command.Right -> current.copy(col = current.col + 1)
            }

            if (isValidMove(nextPos)) {
                val newCoins = _gameState.value.collectedCoins.toMutableSet()
                if (level.coins.contains(nextPos)) {
                    newCoins.add(nextPos)
                    onEvent("coin")
                }

                _gameState.value = _gameState.value.copy(
                    currentPos = nextPos,
                    collectedCoins = newCoins
                )

                // Checks Win
                if (nextPos == level.endPos) {
                    _gameState.value = _gameState.value.copy(
                        isGameOver = true,
                        isWin = true
                    )
                }
            } else {
                // When player hits a wall
                _gameState.value = _gameState.value.copy(
                    isGameOver = true,
                    isWin = false,
                    error = "Crashed!"
                )
            }
            delay(500) // Animation delay
        }
        // If player runs out of commands or doesn't reach the goal
        if (!_gameState.value.isGameOver && _gameState.value.currentPos != level.endPos) {
             _gameState.value = _gameState.value.copy(
                isGameOver = true,
                isWin = false,
                error = "Out of moves!"
            )
        }
    }

    /**
     * Checks if a move to the target position is valid.
     */
    private fun isValidMove(pos: GridPosition): Boolean {
        // Checks the boundary
        if (pos.row < 0 || pos.row >= level.rows || pos.col < 0 || pos.col >= level.cols) return false
        // Checks the walls
        if (level.walls.contains(pos)) return false
        return true
    }
}
