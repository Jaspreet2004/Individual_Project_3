package com.example.individualproject3.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.NavController
import com.example.individualproject3.data.AppDatabase
import com.example.individualproject3.logic.CellType
import com.example.individualproject3.logic.LevelBuilderViewModel
import com.example.individualproject3.ui.theme.GoalColor
import com.example.individualproject3.ui.theme.PathColor
import com.example.individualproject3.ui.theme.WallColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelBuilderScreen(navController: NavController, levelId: Int? = null, onBack: () -> Unit) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val viewModel: LevelBuilderViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LevelBuilderViewModel(database.gameDao()) as T
            }
        }
    )

    LaunchedEffect(levelId) {
        if (levelId != null) {
            viewModel.loadLevel(levelId)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    var showSaveDialog by remember { mutableStateOf(false) }
    var levelName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Level Builder") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { showSaveDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ToolButton(icon = Icons.Default.Stop, label = "Wall", isSelected = uiState.selectedTool == CellType.WALL) { viewModel.onToolSelected(CellType.WALL) }
                ToolButton(icon = Icons.Default.MonetizationOn, label = "Coin", isSelected = uiState.selectedTool == CellType.COIN) { viewModel.onToolSelected(CellType.COIN) }
                ToolButton(icon = Icons.Default.Home, label = "Start", isSelected = uiState.selectedTool == CellType.START) { viewModel.onToolSelected(CellType.START) }
                ToolButton(icon = Icons.Default.Flag, label = "End", isSelected = uiState.selectedTool == CellType.END) { viewModel.onToolSelected(CellType.END) }
            }

            // Grid
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (r in 0 until uiState.rows) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (c in 0 until uiState.cols) {
                                val pos = com.example.individualproject3.logic.GridPosition(r, c)
                                val isWall = uiState.walls.contains(pos)
                                val isCoin = uiState.coins.contains(pos)
                                val isStart = uiState.startPos == pos
                                val isEnd = uiState.endPos == pos

                                val color by animateColorAsState(
                                    targetValue = when {
                                        isWall -> WallColor
                                        isStart -> PathColor.copy(alpha = 0.5f)
                                        isEnd -> PathColor.copy(alpha = 0.5f)
                                        else -> Color.White
                                    }
                                )

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(color)
                                        .clickable { viewModel.onCellClicked(r, c) }
                                        .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCoin) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .background(GoalColor, CircleShape)
                                                .border(2.dp, Color.White, CircleShape)
                                        )
                                    }
                                    if (isStart) {
                                         Icon(Icons.Default.Home, contentDescription = "S", tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                    if (isEnd) {
                                         Icon(Icons.Default.Flag, contentDescription = "E", tint = GoalColor, modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Name Your Level") },
            text = {
                OutlinedTextField(
                    value = levelName,
                    onValueChange = { levelName = it },
                    label = { Text("Level Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (levelName.isNotBlank()) {
                            viewModel.saveLevel(levelName) {
                                showSaveDialog = false
                                onBack() // Go back after save
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ToolButton(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = onClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.size(56.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
        ) {
            Icon(icon, contentDescription = label)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
