package com.example.individualproject3.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.individualproject3.data.AppDatabase
import com.example.individualproject3.logic.Command
import com.example.individualproject3.logic.GameState
import com.example.individualproject3.logic.LevelData
import com.example.individualproject3.logic.Levels
import com.example.individualproject3.ui.theme.GoalColor
import com.example.individualproject3.ui.theme.PathColor
import com.example.individualproject3.ui.theme.PlayerColor
import com.example.individualproject3.ui.theme.WallColor
import kotlin.math.roundToInt

/**
 * The Main Game Screen.
 * Handles the game loop, user input, and rendering the level.
 *
 * @param navController Navigation controller for screen transitions.
 * @param levelId The ID of the current level to load.
 */
@Composable
fun GameScreen(navController: NavController, levelId: String) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    // Finds the level data
    val level = Levels.AllLevels.find { it.id == levelId } ?: Levels.Level1_1

    val viewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(levelId, database.gameDao(), context)
    )

    // Observables from ViewModel
    val gameState by viewModel.gameState.collectAsState()
    val commandQueue by viewModel.commandQueue.collectAsState()
    val isExecuting by viewModel.isExecuting.collectAsState()

    // Tracks which command is being dragged
    var draggedCommand by remember { mutableStateOf<Command?>(null) }
    // Tracks the current touch position for the ghost icon
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    // Stores the bounds of the "Command Queue" box to detect drop events
    var queueBounds by remember { mutableStateOf(Rect.Zero) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "GardenQuest", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(text = level.name, style = MaterialTheme.typography.headlineSmall)
                }
                
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Exit")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game Board
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    GameBoard(level, gameState)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Queue
            Text("Your Program (Drag Commands Here):", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            Box(
                 modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        queueBounds = coordinates.boundsInRoot()
                    }
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .border(
                            width = 2.dp, 
                            color = if (draggedCommand != null && queueBounds.contains(dragPosition)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(commandQueue) { cmd ->
                        CommandIcon(cmd, isQueueItem = true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DraggableCommandButton(Command.Up, onDragStart = { off -> draggedCommand = Command.Up; dragPosition = off; viewModel.playClickSound() }, onDrag = { change -> dragPosition += change }, onDragEnd = { 
                    if (queueBounds.contains(dragPosition)) { viewModel.addCommand(Command.Up); viewModel.playClickSound() }
                    draggedCommand = null 
                })
                DraggableCommandButton(Command.Down, onDragStart = { off -> draggedCommand = Command.Down; dragPosition = off; viewModel.playClickSound() }, onDrag = { change -> dragPosition += change }, onDragEnd = { 
                    if (queueBounds.contains(dragPosition)) { viewModel.addCommand(Command.Down); viewModel.playClickSound() }
                     draggedCommand = null 
                })
                DraggableCommandButton(Command.Left, onDragStart = { off -> draggedCommand = Command.Left; dragPosition = off; viewModel.playClickSound() }, onDrag = { change -> dragPosition += change }, onDragEnd = { 
                    if (queueBounds.contains(dragPosition)) { viewModel.addCommand(Command.Left); viewModel.playClickSound() }
                     draggedCommand = null 
                })
                DraggableCommandButton(Command.Right, onDragStart = { off -> draggedCommand = Command.Right; dragPosition = off; viewModel.playClickSound() }, onDrag = { change -> dragPosition += change }, onDragEnd = { 
                    if (queueBounds.contains(dragPosition)) { viewModel.addCommand(Command.Right); viewModel.playClickSound() }
                     draggedCommand = null 
                })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.executeCommands() },
                enabled = !isExecuting && commandQueue.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RUN PROGRAM", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Ghost Icon Overlay
        if (draggedCommand != null) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(dragPosition.x.roundToInt() - 80, dragPosition.y.roundToInt() - 80) } // Center on finger
                    .zIndex(10f)
                    .shadow(8.dp, CircleShape)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                 CommandIcon(draggedCommand!!, isGhost = true)
            }
        }

        if (gameState.isGameOver) {
            AlertDialog(
                onDismissRequest = { /* No dismiss */ },
                confirmButton = {
                    Button(onClick = { navController.popBackStack() }) { // Exit to menu
                        Text("Exit")
                    }
                },
                dismissButton = {
                    Button(onClick = { viewModel.resetGame() }) { // Try Again
                        Text("Reattempt")
                    }
                },
                title = {
                    Text(
                        text = if (gameState.isWin) "MISSION ACCOMPLISHED!" else "TRY AGAIN",
                        color = if (gameState.isWin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                },
                text = { Text(if (gameState.isWin) "Great coding! You reached the goal." else "Oops! ${gameState.error}") },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            )
        }
    }
}

/**
 * Renders the Game Grid based on the Level Data and current Game State.
 *
 * @param level Static data for the levels walls, start, and goal.
 * @param state Dynamic state (player position, collected coins).
 */
@Composable
fun GameBoard(level: LevelData, state: GameState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (r in 0 until level.rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (c in 0 until level.cols) {
                    val pos = com.example.individualproject3.logic.GridPosition(r, c)
                    val isWall = level.walls.contains(pos)
                    val isStart = level.startPos == pos
                    val isEnd = level.endPos == pos
                    val isCoin = level.coins.contains(pos) && !state.collectedCoins.contains(pos)
                    val isPlayer = state.currentPos == pos

                    // Animated Color
                    val cellColor by animateColorAsState(
                         targetValue = when {
                            isWall -> WallColor
                            isStart -> PathColor.copy(alpha = 0.5f)
                            isEnd -> PathColor.copy(alpha = 0.5f)
                            else -> PathColor.copy(alpha = 0.1f)
                        }
                    )

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(cellColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCoin) {
                             Box(modifier = Modifier.size(16.dp).background(GoalColor, CircleShape).border(2.dp, Color.White, CircleShape))
                        }
                        if (isEnd) {
                             Box(modifier = Modifier.size(24.dp).border(4.dp, GoalColor, RoundedCornerShape(4.dp)))
                        }
                        if (isPlayer) {
                             Image(
                                 painter = androidx.compose.ui.res.painterResource(id = com.example.individualproject3.R.drawable.bug),
                                 contentDescription = "Player",
                                 modifier = Modifier.size(32.dp)
                             )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A Button that starts a drag gesture.
 *
 * @param cmd The specific command (Up, Down, Left, Right) this button represents.
 * @param onDragStart Callback when drag begins.
 * @param onDrag Callback during drag movement.
 * @param onDragEnd Callback when drag is released.
 */
@Composable
fun DraggableCommandButton(
    cmd: Command,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    var globalPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .onGloballyPositioned { globalPosition = it.positionInRoot() }
            .size(70.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                         onDragStart(globalPosition + offset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                    onDragEnd = { onDragEnd() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        CommandIcon(cmd)
    }
}

/**
 * Renders the icon for a command.
 *
 * @param cmd The command to render.
 * @param isQueueItem If true, styles the icon for the execution queue.
 * @param isGhost If true, renders as a white "ghost" icon during dragging.
 */
@Composable
fun CommandIcon(cmd: Command, isQueueItem: Boolean = false, isGhost: Boolean = false) {
    val icon = when (cmd) {
        Command.Up -> Icons.Default.ArrowUpward
        Command.Down -> Icons.Default.ArrowDownward
        Command.Left -> Icons.Default.ArrowBack
        Command.Right -> Icons.Default.ArrowForward
    }
    
    val tint = when {
        isGhost -> Color.White
        isQueueItem -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    
    Icon(
        imageVector = icon, 
        contentDescription = cmd.name, 
        tint = tint,
        modifier = Modifier.size(if (isQueueItem || isGhost) 32.dp else 24.dp) 
    )
}
