package com.example.todoapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

/**
 * Register Screen
 *
 * Allows new users to create an account.
 *
 * FEATURES:
 * - Username input with real-time availability check
 * - Email input with real-time availability check
 * - Password input with show/hide toggle
 * - Confirm password with matching validation
 * - Optional first name and last name fields
 * - Real-time validation feedback
 * - Loading state during registration
 * - Error handling
 * - Success navigation (to login screen)
 *
 * FORM VALIDATION:
 * - Username: 3-50 chars, must be available
 * - Email: Valid format, must be available
 * - Password: Minimum 8 characters
 * - Confirm Password: Must match password
 * - First/Last Name: Optional, max 50 chars
 *
 * UX ENHANCEMENTS:
 * - Availability indicators (✓ available, ✗ taken, ⟳ checking)
 * - Inline validation errors
 * - Disabled submit until form is valid
 * - Scrollable form for smaller screens
 */
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = koinViewModel(),
    onRegisterSuccess: (username: String) -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    // Observe UI state
    val uiState by viewModel.uiState.collectAsState()

    // Observe form fields
    val username by viewModel.username.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()

    // Observe validation errors
    val usernameError by viewModel.usernameError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()
    val firstNameError by viewModel.firstNameError.collectAsState()
    val lastNameError by viewModel.lastNameError.collectAsState()

    // Observe availability states
    val usernameAvailability by viewModel.usernameAvailability.collectAsState()
    val emailAvailability by viewModel.emailAvailability.collectAsState()

    // Local UI state
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Focus manager for keyboard navigation
    val focusManager = LocalFocusManager.current

    // Handle success navigation
    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.Success) {
            val successState = uiState as RegisterUiState.Success
            onRegisterSuccess(successState.username)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // App Title
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Sign up to get started",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Username field with availability indicator
        OutlinedTextField(
            value = username,
            onValueChange = { viewModel.updateUsername(it) },
            label = { Text("Username") },
            placeholder = { Text("Choose a username") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            enabled = uiState !is RegisterUiState.Loading,
            isError = usernameError != null,
            supportingText = {
                usernameError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                AvailabilityIndicator(usernameAvailability)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        // Email field with availability indicator
        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("Email") },
            placeholder = { Text("your.email@example.com") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            enabled = uiState !is RegisterUiState.Loading,
            isError = emailError != null,
            supportingText = {
                emailError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                AvailabilityIndicator(emailAvailability)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text("Password") },
            placeholder = { Text("At least 8 characters") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            enabled = uiState !is RegisterUiState.Loading,
            isError = passwordError != null,
            supportingText = {
                passwordError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                } ?: Text(
                    text = "Minimum 8 characters",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(
                        text = if (passwordVisible) "Hide" else "Show",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )

        // Confirm Password field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { viewModel.updateConfirmPassword(it) },
            label = { Text("Confirm Password") },
            placeholder = { Text("Re-enter your password") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            enabled = uiState !is RegisterUiState.Loading,
            isError = confirmPasswordError != null,
            supportingText = {
                confirmPasswordError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            trailingIcon = {
                TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Text(
                        text = if (confirmPasswordVisible) "Hide" else "Show",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )

        // Divider
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Optional Information",
            style = MaterialTheme.typography.titleSmall,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // First Name field (optional)
        OutlinedTextField(
            value = firstName,
            onValueChange = { viewModel.updateFirstName(it) },
            label = { Text("First Name (Optional)") },
            placeholder = { Text("Your first name") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            enabled = uiState !is RegisterUiState.Loading,
            isError = firstNameError != null,
            supportingText = {
                firstNameError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        // Last Name field (optional)
        OutlinedTextField(
            value = lastName,
            onValueChange = { viewModel.updateLastName(it) },
            label = { Text("Last Name (Optional)") },
            placeholder = { Text("Your last name") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            enabled = uiState !is RegisterUiState.Loading,
            isError = lastNameError != null,
            supportingText = {
                lastNameError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    viewModel.register()
                }
            )
        )

        // Create Account button
        Button(
            onClick = {
                focusManager.clearFocus()
                viewModel.register()
            },
            enabled = uiState !is RegisterUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Create Account", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // State-based content
        when (val state = uiState) {
            is RegisterUiState.Idle -> {
                // Show helpful hint
                Text(
                    text = "Fill in the form to create your account",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            is RegisterUiState.Loading -> {
                // Show loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = "Creating your account...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            is RegisterUiState.Success -> {
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
                            text = "✅ Account created!",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF2E7D32), // Dark green
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Welcome, ${state.username}!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "Redirecting to login...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            is RegisterUiState.Error -> {
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
                            text = "❌ Registration Failed",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFC62828), // Dark red
                            fontWeight = FontWeight.Bold,
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

        // Already have an account? Login
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            TextButton(onClick = onNavigateToLogin) {
                Text("Log In")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Availability Indicator
 *
 * Shows visual feedback for username/email availability checks
 *
 * STATES:
 * - Idle: Nothing shown
 * - Checking: Circular progress indicator (⟳)
 * - Available: Green checkmark (✓)
 * - Unavailable: Red X (✗)
 * - Error: Warning indicator (⚠)
 */
@Composable
fun AvailabilityIndicator(state: AvailabilityState) {
    when (state) {
        is AvailabilityState.Idle -> {
            // Show nothing
        }

        is AvailabilityState.Checking -> {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        }

        is AvailabilityState.Available -> {
            Text(
                text = "✓",
                color = Color(0xFF2E7D32), // Dark green
                style = MaterialTheme.typography.titleLarge
            )
        }

        is AvailabilityState.Unavailable -> {
            Text(
                text = "✗",
                color = Color(0xFFC62828), // Dark red
                style = MaterialTheme.typography.titleLarge
            )
        }

        is AvailabilityState.Error -> {
            Text(
                text = "⚠",
                color = Color(0xFFF57C00), // Orange
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
