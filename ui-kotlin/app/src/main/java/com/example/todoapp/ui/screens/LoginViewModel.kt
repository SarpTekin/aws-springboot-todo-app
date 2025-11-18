package com.example.todoapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.local.TokenManager
import com.example.todoapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Login ViewModel
 *
 * RESPONSIBILITY:
 * - Manage login UI state
 * - Handle login action (when user clicks "Login")
 * - Call repository to login
 * - Save token on success
 * - Update UI state based on result
 *
 * ARCHITECTURE:
 * LoginScreen (UI) ← LoginViewModel ← UserRepository ← UserApiService ← Backend
 *                         ↓
 *                   TokenManager (saves token)
 */
class LoginViewModel(
    private val repository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Perform login
     *
     * Called when user clicks "Login" button
     *
     * @param username Username entered by user
     * @param password Password entered by user
     */
    fun login(username: String, password: String) {
        // Validation: Check for empty fields
        if (username.isBlank()) {
            _uiState.value = LoginUiState.Error("Username cannot be empty")
            return
        }

        if (password.isBlank()) {
            _uiState.value = LoginUiState.Error("Password cannot be empty")
            return
        }

        // Validation: Username must be at least 3 characters
        if (username.length < 3) {
            _uiState.value = LoginUiState.Error("Username must be at least 3 characters")
            return
        }

        // Validation: Password must be at least 8 characters (backend requirement)
        if (password.length < 8) {
            _uiState.value = LoginUiState.Error("Password must be at least 8 characters")
            return
        }

        // Launch coroutine for async login
        viewModelScope.launch {
            // 1. Show loading state
            _uiState.value = LoginUiState.Loading

            // 2. Call repository to login
            val result = repository.login(username, password)

            // 3. Handle result
            result
                .onSuccess { loginResponse ->
                    // Login successful!
                    // Save token, userId, username
                    tokenManager.saveAuthData(
                        token = loginResponse.token,
                        userId = loginResponse.userId,
                        username = loginResponse.username
                    )

                    // Update UI state to Success
                    _uiState.value = LoginUiState.Success(
                        userId = loginResponse.userId,
                        username = loginResponse.username
                    )
                }
                .onFailure { error ->
                    // Login failed
                    // Parse error message and show to user
                    val errorMessage = when {
                        error.message?.contains("401") == true ->
                            "Invalid username or password"
                        error.message?.contains("Unable to resolve host") == true ->
                            "Cannot connect to server. Check your internet connection."
                        error.message?.contains("timeout") == true ->
                            "Connection timeout. Please try again."
                        else ->
                            error.message ?: "Login failed. Please try again."
                    }

                    _uiState.value = LoginUiState.Error(errorMessage)
                }
        }
    }

    /**
     * Reset state back to Idle
     *
     * Called when user dismisses error or navigates away
     */
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

/**
 * Login UI State
 *
 * Represents all possible states of the Login screen
 */
sealed class LoginUiState {
    /**
     * Idle - Initial state, waiting for user to enter credentials
     */
    object Idle : LoginUiState()

    /**
     * Loading - Login in progress
     * UI should show loading spinner and disable input
     */
    object Loading : LoginUiState()

    /**
     * Success - Login successful
     * @param userId User's ID
     * @param username User's username
     *
     * UI should navigate to home screen
     */
    data class Success(
        val userId: Long,
        val username: String
    ) : LoginUiState()

    /**
     * Error - Login failed
     * @param message Error message to display
     *
     * UI should show error message and allow retry
     */
    data class Error(
        val message: String
    ) : LoginUiState()
}
