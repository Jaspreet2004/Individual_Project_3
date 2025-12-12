package com.example.individualproject3.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.individualproject3.data.AppDatabase

/**
 * Login Screen Composable.
 * Displays the App Title, Tab Selection (Parent/Kid), and Login/Register forms.
 */
@Composable
fun LoginScreen(navController: NavController, onLoginSuccess: (Boolean) -> Unit) {
    val context = LocalContext.current
    com.example.individualproject3.util.FileLogger.log(context, "LoginScreen: Composing")
    val database = AppDatabase.getDatabase(context)
    val viewModel: LoginViewModel = viewModel(
        factory = ViewModelFactory(database.userDao())
    )
    val uiState by viewModel.uiState.collectAsState()

    // Full Screen Content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = "GardenQuest",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Grow Your Skills!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }

            // Login Card
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Custom Tab Row
                    TabRow(
                        selectedTabIndex = if (uiState.isParentTab) 0 else 1,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = {},
                        divider = {}
                    ) {
                        Tab(
                            selected = uiState.isParentTab,
                            onClick = { viewModel.onTabSelected(true) },
                            text = {
                                Text(
                                    "Parent",
                                    fontWeight = if (uiState.isParentTab) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        )
                        Tab(
                            selected = !uiState.isParentTab,
                            onClick = { viewModel.onTabSelected(false) },
                            text = {
                                Text(
                                    "Kid",
                                    fontWeight = if (!uiState.isParentTab) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Username Field
                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = { viewModel.onUsernameChange(it) },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.onRegisterClick(onLoginSuccess) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Register")
                        }
                        Button(
                            onClick = { viewModel.onLoginClick(onLoginSuccess) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Text("Login")
                        }
                    }
                }
            }
        }

        if (uiState.error != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            ) {
                Text(text = uiState.error!!)
            }
        }
    }
}
