package com.example.individualproject3.logic

data class GridPosition(val row: Int, val col: Int)

enum class CellType {
    EMPTY, WALL, COIN, START, END
}

data class LevelData(
    val id: String,
    val name: String,
    val rows: Int,
    val cols: Int,
    val startPos: GridPosition,
    val endPos: GridPosition,
    val walls: List<GridPosition>,
    val coins: List<GridPosition>
)

object Levels {
    // Level 1: Simple paths
    val Level1_1 = LevelData(
        id = "1-1", name = "Level 1 - Game 1", rows = 5, cols = 5,
        startPos = GridPosition(0, 0), endPos = GridPosition(0, 4),
        walls = listOf(),
        coins = listOf(GridPosition(0, 2))
    )

    val Level1_2 = LevelData(
        id = "1-2", name = "Level 1 - Game 2", rows = 5, cols = 5,
        startPos = GridPosition(2, 0), endPos = GridPosition(2, 4),
        walls = listOf(GridPosition(1, 2), GridPosition(3, 2)),
        coins = listOf(GridPosition(2, 2))
    )

    val Level1_3 = LevelData(
        id = "1-3", name = "Level 1 - Game 3", rows = 5, cols = 5,
        startPos = GridPosition(4, 0), endPos = GridPosition(0, 4),
        walls = listOf(GridPosition(2, 2)),
        coins = listOf(GridPosition(4, 2), GridPosition(2, 4))
    )

    // Level 2: More complex obstacles
    val Level2_1 = LevelData(
        id = "2-1", name = "Level 2 - Game 1", rows = 6, cols = 6,
        startPos = GridPosition(0, 0), endPos = GridPosition(5, 5),
        walls = listOf(GridPosition(0, 1), GridPosition(1, 1), GridPosition(2, 1)),
        coins = listOf(GridPosition(3, 3))
    )

    val Level2_2 = LevelData(
        id = "2-2", name = "Level 2 - Game 2", rows = 6, cols = 6,
        startPos = GridPosition(5, 0), endPos = GridPosition(0, 5),
        walls = listOf(GridPosition(2, 2), GridPosition(2, 3), GridPosition(3, 2), GridPosition(3, 3)),
        coins = listOf(GridPosition(0, 0), GridPosition(5, 5))
    )

    val Level2_3 = LevelData(
        id = "2-3", name = "Level 2 - Game 3", rows = 7, cols = 7,
        startPos = GridPosition(3, 3), endPos = GridPosition(6, 6),
        walls = listOf(GridPosition(4, 4), GridPosition(5, 5)),
        coins = listOf(GridPosition(3, 4), GridPosition(4, 5))
    )

    val AllLevels = listOf(Level1_1, Level1_2, Level1_3, Level2_1, Level2_2, Level2_3)
}
