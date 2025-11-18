package com.example.todoapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Username Availability Check Screen
 *
 * RESPONSIBILITY:
 * - Manage UI state (loading, success, error)
 * - Handle user actions (check button clicked)
 * - Call repository for data
 * - Update UI state based on results
 *
 * ARCHITECTURE:
 * UI (Compose) ←→ ViewModel ←→ Repository ←→ API Service
 *
 * The ViewModel is the "bridge" between UI and data:
 * - UI observes state changes (StateFlow)
 * - UI triggers actions (checkUsername())
 * - ViewModel updates state
 * - UI reacts to state changes and recomposes
 */
class UsernameCheckViewModel(
    private val repository: UserRepository
) : ViewModel() {

    /**
     * UI State - Represents the current state of the screen
     *
     * StateFlow is like LiveData but for Kotlin Coroutines
     * - UI can "collect" this flow and react to changes
     * - When state changes, UI automatically recomposes (Jetpack Compose)
     */
    private val _uiState = MutableStateFlow<UsernameCheckUiState>(UsernameCheckUiState.Idle)
    val uiState: StateFlow<UsernameCheckUiState> = _uiState.asStateFlow()

    /**
     * Check if a username is available
     *
     * Called when user clicks the "Check" button.
     *
     * @param username The username to check
     */
    fun checkUsername(username: String) {
        // Validation: Don't check empty usernames
        if (username.isBlank()) {
            _uiState.value = UsernameCheckUiState.Error("Username cannot be empty")
            return
        }

        // Validation: Username must be at least 3 characters (backend rule)
        if (username.length < 3) {
            _uiState.value = UsernameCheckUiState.Error("Username must be at least 3 characters")
            return
        }

        // Launch a coroutine (background task)
        viewModelScope.launch {
            // 1. Set state to Loading (shows loading spinner in UI)
            _uiState.value = UsernameCheckUiState.Loading

            // 2. Call repository (suspend function - runs in background)
            val result = repository.checkUsername(username)

            // 3. Handle the result
            result
                .onSuccess { response ->
                    // Success! Update state with the result
                    if (response.available) {
                        _uiState.value = UsernameCheckUiState.Success(
                            available = true,
                            message = "✅ Username '$username' is available!"
                        )
                    } else {
                        _uiState.value = UsernameCheckUiState.Success(
                            available = false,
                            message = "❌ Username '$username' is already taken"
                        )
                    }
                }
                .onFailure { error ->
                    // Error! Show error message
                    _uiState.value = UsernameCheckUiState.Error(
                        error.message ?: "Unknown error occurred"
                    )
                }
        }
    }

    /**
     * Reset the state back to Idle
     *
     * Called when user clears the input or navigates away
     */
    fun resetState() {
        _uiState.value = UsernameCheckUiState.Idle
    }
}

/**
 * UI State sealed class
 *
 * Represents all possible states of the Username Check screen.
 *
 * SEALED CLASS = A class that can only have a fixed set of subclasses
 * Perfect for state management because we can use 'when' and handle all cases.
 */
sealed class UsernameCheckUiState {
    /**
     * Idle - Initial state, nothing has happened yet
     */
    object Idle : UsernameCheckUiState()

    /**
     * Loading - API call is in progress
     * UI should show a loading spinner
     */
    object Loading : UsernameCheckUiState()

    /**
     * Success - API call succeeded
     * @param available true if username is available, false if taken
     * @param message Message to display to user
     */
    data class Success(
        val available: Boolean,
        val message: String
    ) : UsernameCheckUiState()

    /**
     * Error - Something went wrong (network error, server error, etc.)
     * @param message Error message to display
     */
    data class Error(
        val message: String
    ) : UsernameCheckUiState()
}
