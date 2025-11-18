package com.example.todoapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

/**
 * Username Check Screen
 *
 * A simple screen that lets users check if a username is available.
 *
 * ARCHITECTURE:
 * - This is the UI layer (Compose)
 * - Observes state from ViewModel (StateFlow)
 * - Calls ViewModel functions on user actions
 * - Automatically recomposes when state changes
 *
 * COMPOSE CONCEPTS:
 * - @Composable = Function that describes UI
 * - remember = Keeps state across recompositions
 * - collectAsState() = Observes StateFlow and triggers recomposition
 * - Recomposition = UI updates when state changes
 */
@Composable
fun UsernameCheckScreen(
    viewModel: UsernameCheckViewModel = koinViewModel()
) {
    // Observe UI state from ViewModel
    // When viewModel.uiState changes, this triggers recomposition
    val uiState by viewModel.uiState.collectAsState()

    // Local state for the text field (what user types)
    // 'remember' keeps this value across recompositions
    var username by remember { mutableStateOf(TextFieldValue("")) }

    // Main container
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Check Username Availability",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Username input field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            placeholder = { Text("Enter username") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = uiState !is UsernameCheckUiState.Loading
        )

        // Check button
        Button(
            onClick = {
                // Call ViewModel function when button clicked
                viewModel.checkUsername(username.text)
            },
            enabled = uiState !is UsernameCheckUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text("Check Availability")
        }

        // Result area - changes based on UI state
        when (val state = uiState) {
            is UsernameCheckUiState.Idle -> {
                // Initial state - show nothing or helpful text
                Text(
                    text = "Enter a username and click 'Check Availability'",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            is UsernameCheckUiState.Loading -> {
                // Loading state - show spinner
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Checking...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            is UsernameCheckUiState.Success -> {
                // Success state - show result
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.available) {
                            Color(0xFFE8F5E9) // Light green
                        } else {
                            Color(0xFFFFEBEE) // Light red
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (state.available) {
                                Color(0xFF2E7D32) // Dark green
                            } else {
                                Color(0xFFC62828) // Dark red
                            }
                        )
                    }
                }
            }

            is UsernameCheckUiState.Error -> {
                // Error state - show error message
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE) // Light red
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFC62828), // Dark red
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }
        }

        // Reset button (if not in Idle state)
        if (uiState !is UsernameCheckUiState.Idle) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    viewModel.resetState()
                    username = TextFieldValue("")
                }
            ) {
                Text("Reset")
            }
        }
    }
}
