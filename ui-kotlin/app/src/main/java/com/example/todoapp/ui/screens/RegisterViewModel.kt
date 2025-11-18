package com.example.todoapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.model.UserRequest
import com.example.todoapp.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Register ViewModel
 *
 * RESPONSIBILITY:
 * - Manage registration form state (username, email, password, firstName, lastName)
 * - Validate form inputs in real-time
 * - Check username and email availability
 * - Register new user via repository
 * - Handle success/error states
 *
 * FORM VALIDATION:
 * - Username: 3-50 characters, required, must be available
 * - Email: Valid email format, required, must be available
 * - Password: Minimum 8 characters, required
 * - Confirm Password: Must match password
 * - First Name: Max 50 characters, optional
 * - Last Name: Max 50 characters, optional
 *
 * REAL-TIME VALIDATION:
 * - Username availability checked with debounce (500ms delay)
 * - Email availability checked with debounce (500ms delay)
 * - Password strength indicator
 * - Instant validation for other fields
 *
 * STATE FLOW:
 * 1. User enters form data
 * 2. Real-time validation runs
 * 3. User taps "Create Account"
 * 4. Final validation check
 * 5. If valid: call repository.register()
 * 6. If success: navigate to login screen
 * 7. If error: show error message
 *
 * ARCHITECTURE:
 * RegisterScreen → RegisterViewModel → UserRepository → UserApiService
 *       (UI)           (Logic)            (Data)          (Network)
 */
class RegisterViewModel(
    private val repository: UserRepository
) : ViewModel() {

    // Form state - editable by UI
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _firstName = MutableStateFlow("")
    val firstName: StateFlow<String> = _firstName.asStateFlow()

    private val _lastName = MutableStateFlow("")
    val lastName: StateFlow<String> = _lastName.asStateFlow()

    // UI state - loading, success, error
    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Validation errors
    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError.asStateFlow()

    private val _firstNameError = MutableStateFlow<String?>(null)
    val firstNameError: StateFlow<String?> = _firstNameError.asStateFlow()

    private val _lastNameError = MutableStateFlow<String?>(null)
    val lastNameError: StateFlow<String?> = _lastNameError.asStateFlow()

    // Availability check states
    private val _usernameAvailability = MutableStateFlow<AvailabilityState>(AvailabilityState.Idle)
    val usernameAvailability: StateFlow<AvailabilityState> = _usernameAvailability.asStateFlow()

    private val _emailAvailability = MutableStateFlow<AvailabilityState>(AvailabilityState.Idle)
    val emailAvailability: StateFlow<AvailabilityState> = _emailAvailability.asStateFlow()

    // Debounce jobs for availability checks
    private var usernameCheckJob: Job? = null
    private var emailCheckJob: Job? = null

    /**
     * Update username field
     *
     * REACTIVE:
     * UI calls this when user types
     * Triggers debounced availability check
     *
     * VALIDATION:
     * Clear error when user starts typing
     */
    fun updateUsername(newUsername: String) {
        _username.value = newUsername
        _usernameError.value = null

        // Cancel previous check
        usernameCheckJob?.cancel()

        // Only check if username meets minimum requirements
        if (newUsername.length >= 3) {
            _usernameAvailability.value = AvailabilityState.Checking

            // Debounce: wait 500ms before checking
            usernameCheckJob = viewModelScope.launch {
                delay(500)
                checkUsernameAvailability(newUsername)
            }
        } else {
            _usernameAvailability.value = AvailabilityState.Idle
        }
    }

    /**
     * Update email field
     *
     * REACTIVE:
     * UI calls this when user types
     * Triggers debounced availability check
     */
    fun updateEmail(newEmail: String) {
        _email.value = newEmail
        _emailError.value = null

        // Cancel previous check
        emailCheckJob?.cancel()

        // Only check if email looks valid
        if (newEmail.contains("@") && newEmail.length > 5) {
            _emailAvailability.value = AvailabilityState.Checking

            // Debounce: wait 500ms before checking
            emailCheckJob = viewModelScope.launch {
                delay(500)
                checkEmailAvailability(newEmail)
            }
        } else {
            _emailAvailability.value = AvailabilityState.Idle
        }
    }

    /**
     * Update password field
     *
     * REACTIVE:
     * UI calls this when user types
     */
    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        _passwordError.value = null

        // Re-validate confirm password if it has a value
        if (_confirmPassword.value.isNotEmpty()) {
            validateConfirmPassword()
        }
    }

    /**
     * Update confirm password field
     *
     * REACTIVE:
     * UI calls this when user types
     */
    fun updateConfirmPassword(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
        _confirmPasswordError.value = null
    }

    /**
     * Update first name field
     */
    fun updateFirstName(newFirstName: String) {
        _firstName.value = newFirstName
        _firstNameError.value = null
    }

    /**
     * Update last name field
     */
    fun updateLastName(newLastName: String) {
        _lastName.value = newLastName
        _lastNameError.value = null
    }

    /**
     * Check username availability
     *
     * ENDPOINT: GET /api/users/check-username?username={username}
     *
     * DEBOUNCED:
     * Only runs 500ms after user stops typing
     */
    private suspend fun checkUsernameAvailability(username: String) {
        try {
            val result = repository.checkUsername(username)

            result
                .onSuccess { response ->
                    _usernameAvailability.value = if (response.available) {
                        AvailabilityState.Available
                    } else {
                        AvailabilityState.Unavailable
                    }
                }
                .onFailure {
                    _usernameAvailability.value = AvailabilityState.Error
                }
        } catch (e: Exception) {
            _usernameAvailability.value = AvailabilityState.Error
        }
    }

    /**
     * Check email availability
     *
     * ENDPOINT: GET /api/users/check-email?email={email}
     *
     * DEBOUNCED:
     * Only runs 500ms after user stops typing
     */
    private suspend fun checkEmailAvailability(email: String) {
        try {
            val result = repository.checkEmail(email)

            result
                .onSuccess { response ->
                    _emailAvailability.value = if (response.available) {
                        AvailabilityState.Available
                    } else {
                        AvailabilityState.Unavailable
                    }
                }
                .onFailure {
                    _emailAvailability.value = AvailabilityState.Error
                }
        } catch (e: Exception) {
            _emailAvailability.value = AvailabilityState.Error
        }
    }

    /**
     * Validate confirm password
     */
    private fun validateConfirmPassword() {
        if (_confirmPassword.value != _password.value) {
            _confirmPasswordError.value = "Passwords do not match"
        } else {
            _confirmPasswordError.value = null
        }
    }

    /**
     * Validate entire form
     *
     * VALIDATION RULES:
     * - Username: Required, 3-50 characters, must be available
     * - Email: Required, valid format, must be available
     * - Password: Required, minimum 8 characters
     * - Confirm Password: Must match password
     * - First Name: Optional, max 50 characters
     * - Last Name: Optional, max 50 characters
     *
     * @return true if valid, false if errors found
     *
     * SIDE EFFECTS:
     * Sets error messages in all error StateFlows
     */
    private fun validateForm(): Boolean {
        var isValid = true

        // Validate username
        when {
            _username.value.isBlank() -> {
                _usernameError.value = "Username is required"
                isValid = false
            }
            _username.value.length < 3 -> {
                _usernameError.value = "Username must be at least 3 characters"
                isValid = false
            }
            _username.value.length > 50 -> {
                _usernameError.value = "Username is too long (max 50 characters)"
                isValid = false
            }
            _usernameAvailability.value != AvailabilityState.Available -> {
                _usernameError.value = "Username is not available"
                isValid = false
            }
            else -> {
                _usernameError.value = null
            }
        }

        // Validate email
        when {
            _email.value.isBlank() -> {
                _emailError.value = "Email is required"
                isValid = false
            }
            !_email.value.contains("@") || !_email.value.contains(".") -> {
                _emailError.value = "Invalid email format"
                isValid = false
            }
            _emailAvailability.value != AvailabilityState.Available -> {
                _emailError.value = "Email is not available"
                isValid = false
            }
            else -> {
                _emailError.value = null
            }
        }

        // Validate password
        when {
            _password.value.isBlank() -> {
                _passwordError.value = "Password is required"
                isValid = false
            }
            _password.value.length < 8 -> {
                _passwordError.value = "Password must be at least 8 characters"
                isValid = false
            }
            else -> {
                _passwordError.value = null
            }
        }

        // Validate confirm password
        if (_confirmPassword.value != _password.value) {
            _confirmPasswordError.value = "Passwords do not match"
            isValid = false
        } else {
            _confirmPasswordError.value = null
        }

        // Validate first name (optional)
        if (_firstName.value.length > 50) {
            _firstNameError.value = "First name is too long (max 50 characters)"
            isValid = false
        } else {
            _firstNameError.value = null
        }

        // Validate last name (optional)
        if (_lastName.value.length > 50) {
            _lastNameError.value = "Last name is too long (max 50 characters)"
            isValid = false
        } else {
            _lastNameError.value = null
        }

        return isValid
    }

    /**
     * Register new user
     *
     * FLOW:
     * 1. Validate form
     * 2. Create UserRequest
     * 3. Call repository.register()
     * 4. Handle success/failure
     *
     * SUCCESS:
     * - UI state becomes Success
     * - Screen navigates to login
     * - User can log in with new credentials
     *
     * FAILURE:
     * - UI state becomes Error with message
     * - User can retry
     *
     * ENDPOINT: POST /api/users
     */
    fun register() {
        // Validate form first
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading

            // Create user request
            val userRequest = UserRequest(
                username = _username.value.trim(),
                email = _email.value.trim(),
                password = _password.value,
                firstName = _firstName.value.trim().ifBlank { null },
                lastName = _lastName.value.trim().ifBlank { null }
            )

            // Call repository
            val result = repository.register(userRequest)

            result
                .onSuccess { userResponse ->
                    // Success! User created
                    _uiState.value = RegisterUiState.Success(
                        userId = userResponse.id,
                        username = userResponse.username
                    )
                }
                .onFailure { error ->
                    _uiState.value = RegisterUiState.Error(
                        error.message ?: "Failed to create account"
                    )
                }
        }
    }

    /**
     * Reset form
     *
     * USAGE:
     * - When user cancels registration
     * - When user wants to start over
     */
    fun resetForm() {
        _username.value = ""
        _email.value = ""
        _password.value = ""
        _confirmPassword.value = ""
        _firstName.value = ""
        _lastName.value = ""

        _usernameError.value = null
        _emailError.value = null
        _passwordError.value = null
        _confirmPasswordError.value = null
        _firstNameError.value = null
        _lastNameError.value = null

        _usernameAvailability.value = AvailabilityState.Idle
        _emailAvailability.value = AvailabilityState.Idle

        _uiState.value = RegisterUiState.Idle
    }

    /**
     * Clear UI state
     *
     * USAGE:
     * After navigation completes, clear the success state
     */
    fun clearUiState() {
        _uiState.value = RegisterUiState.Idle
    }

    /**
     * Reset error state
     *
     * USAGE:
     * After showing error, user can retry
     */
    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}

/**
 * Register UI State
 *
 * SEALED CLASS:
 * Represents all possible states of the registration screen
 *
 * STATES:
 * - Idle: Default state, form ready for input
 * - Loading: Creating account (show progress)
 * - Success: Account created (navigate to login)
 * - Error: Something went wrong (show error, allow retry)
 */
sealed class RegisterUiState {
    /**
     * Idle state
     * Form is ready for user input
     */
    object Idle : RegisterUiState()

    /**
     * Loading state
     * Creating account, show progress indicator
     * Disable form inputs to prevent double-submission
     */
    object Loading : RegisterUiState()

    /**
     * Success state
     * Account created successfully!
     *
     * @param userId The ID of the created user (from server)
     * @param username The username of the new account
     *
     * NAVIGATION:
     * When UI sees this state, navigate to login screen
     * Show success message: "Account created! Please log in"
     */
    data class Success(
        val userId: Long,
        val username: String
    ) : RegisterUiState()

    /**
     * Error state
     * Failed to create account
     *
     * @param message Error message to show user
     *
     * USER ACTIONS:
     * - Show error message
     * - Keep form data (user can fix and retry)
     * - Provide retry button
     */
    data class Error(val message: String) : RegisterUiState()
}

/**
 * Availability State
 *
 * Represents the state of username/email availability checks
 *
 * STATES:
 * - Idle: Not checking (field empty or too short)
 * - Checking: API call in progress (show loading indicator)
 * - Available: Username/email is available (show checkmark)
 * - Unavailable: Username/email is taken (show error)
 * - Error: API call failed (show warning)
 */
sealed class AvailabilityState {
    object Idle : AvailabilityState()
    object Checking : AvailabilityState()
    object Available : AvailabilityState()
    object Unavailable : AvailabilityState()
    object Error : AvailabilityState()
}
