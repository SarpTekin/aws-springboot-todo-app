package com.example.todoapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

/**
 * Login Screen
 *
 * Allows users to login with username and password.
 *
 * FEATURES:
 * - Username input field
 * - Password input field (with show/hide toggle)
 * - Login button
 * - Loading state
 * - Error handling
 * - Success navigation (to home screen)
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: (userId: Long, username: String) -> Unit = { _, _ -> },
    onNavigateToRegister: () -> Unit = {}
) {
    // Observe UI state
    val uiState by viewModel.uiState.collectAsState()

    // Local state for input fields
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Handle success navigation
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            val successState = uiState as LoginUiState.Success
            onLoginSuccess(successState.userId, successState.username)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Title
        Text(
            text = "Todo App",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Login to your account",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Username field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            placeholder = { Text("Enter your username") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = uiState !is LoginUiState.Loading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            placeholder = { Text("Enter your password") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            enabled = uiState !is LoginUiState.Loading,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // Submit login when user presses "Done" on keyboard
                    viewModel.login(username, password)
                }
            ),
            trailingIcon = {
                // Show/Hide password toggle (text-based)
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(
                        text = if (passwordVisible) "Hide" else "Show",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )

        // Login button
        Button(
            onClick = {
                viewModel.login(username, password)
            },
            enabled = uiState !is LoginUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Login", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // State-based content
        when (val state = uiState) {
            is LoginUiState.Idle -> {
                // Show nothing or helpful hint
                Text(
                    text = "Enter your credentials to continue",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            is LoginUiState.Loading -> {
                // Show loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = "Logging in...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            is LoginUiState.Success -> {
                // Success state - navigation handled by LaunchedEffect
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9) // Light green
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✅ Login successful!",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF2E7D32) // Dark green
                        )
                        Text(
                            text = "Welcome, ${state.username}!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            is LoginUiState.Error -> {
                // Show error message
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
                            text = "❌ Login Failed",
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

                // Retry button
                TextButton(
                    onClick = { viewModel.resetState() }
                ) {
                    Text("Try Again")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Don't have an account? Register
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            TextButton(onClick = onNavigateToRegister) {
                Text("Register")
            }
        }
    }
}
