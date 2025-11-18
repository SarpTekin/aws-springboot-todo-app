package com.example.todoapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.local.TokenManager
import com.example.todoapp.data.model.TaskRequest
import com.example.todoapp.data.model.TaskStatus
import com.example.todoapp.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Create Task ViewModel
 *
 * RESPONSIBILITY:
 * - Manage form state (title, description, status)
 * - Validate form inputs
 * - Create new task via repository
 * - Handle success/error states
 *
 * FORM VALIDATION:
 * - Title is required (cannot be empty)
 * - Title max length: 200 characters
 * - Description max length: 1000 characters
 * - Status defaults to PENDING
 *
 * STATE FLOW:
 * 1. User enters form data
 * 2. User taps "Create Task"
 * 3. Validate inputs
 * 4. If valid: call repository.createTask()
 * 5. If success: navigate back to task list
 * 6. If error: show error message
 *
 * ARCHITECTURE:
 * CreateTaskScreen → CreateTaskViewModel → TaskRepository → TaskApiService
 *       (UI)              (Logic)             (Data)         (Network)
 */
class CreateTaskViewModel(
    private val repository: TaskRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // Form state - editable by UI
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _selectedStatus = MutableStateFlow(TaskStatus.PENDING)
    val selectedStatus: StateFlow<TaskStatus> = _selectedStatus.asStateFlow()

    // UI state - loading, success, error
    private val _uiState = MutableStateFlow<CreateTaskUiState>(CreateTaskUiState.Idle)
    val uiState: StateFlow<CreateTaskUiState> = _uiState.asStateFlow()

    // Validation errors
    private val _titleError = MutableStateFlow<String?>(null)
    val titleError: StateFlow<String?> = _titleError.asStateFlow()

    private val _descriptionError = MutableStateFlow<String?>(null)
    val descriptionError: StateFlow<String?> = _descriptionError.asStateFlow()

    /**
     * Update title field
     *
     * REACTIVE:
     * UI calls this when user types
     * StateFlow automatically updates UI
     *
     * VALIDATION:
     * Clear error when user starts typing
     */
    fun updateTitle(newTitle: String) {
        _title.value = newTitle
        // Clear error when user starts typing
        if (_titleError.value != null) {
            _titleError.value = null
        }
    }

    /**
     * Update description field
     *
     * REACTIVE:
     * UI calls this when user types
     */
    fun updateDescription(newDescription: String) {
        _description.value = newDescription
        // Clear error when user starts typing
        if (_descriptionError.value != null) {
            _descriptionError.value = null
        }
    }

    /**
     * Update selected status
     *
     * USER INTERACTION:
     * Tap status chip to change initial status
     * Most tasks start as PENDING, but user can choose
     */
    fun updateStatus(newStatus: TaskStatus) {
        _selectedStatus.value = newStatus
    }

    /**
     * Validate form inputs
     *
     * VALIDATION RULES:
     * - Title: Required, 1-200 characters
     * - Description: Optional, max 1000 characters
     *
     * @return true if valid, false if errors found
     *
     * SIDE EFFECTS:
     * Sets error messages in _titleError and _descriptionError
     */
    private fun validateForm(): Boolean {
        var isValid = true

        // Validate title
        when {
            _title.value.isBlank() -> {
                _titleError.value = "Title is required"
                isValid = false
            }
            _title.value.length > 200 -> {
                _titleError.value = "Title is too long (max 200 characters)"
                isValid = false
            }
            else -> {
                _titleError.value = null
            }
        }

        // Validate description
        if (_description.value.length > 1000) {
            _descriptionError.value = "Description is too long (max 1000 characters)"
            isValid = false
        } else {
            _descriptionError.value = null
        }

        return isValid
    }

    /**
     * Create task
     *
     * FLOW:
     * 1. Validate form
     * 2. Create TaskRequest (no userId needed!)
     * 3. Call repository.createTask()
     * 4. Backend extracts userId from JWT token automatically
     * 5. Handle success/failure
     *
     * SUCCESS:
     * - UI state becomes Success
     * - Screen navigates back to task list
     * - Task list automatically refreshes (new task appears!)
     *
     * FAILURE:
     * - UI state becomes Error with message
     * - User can retry
     *
     * ⚠️ IMPORTANT:
     * No need to get userId from TokenManager!
     * Backend extracts it from JWT token automatically
     */
    fun createTask() {
        // Validate form first
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _uiState.value = CreateTaskUiState.Loading

            // Create task request
            // No userId needed - backend extracts from JWT token!
            val taskRequest = TaskRequest(
                title = _title.value.trim(),
                description = _description.value.trim().ifBlank { null },
                status = _selectedStatus.value
            )

            // Call repository
            val result = repository.createTask(taskRequest)

            result
                .onSuccess { createdTask ->
                    // Success! Task created with id from server
                    _uiState.value = CreateTaskUiState.Success(createdTask.id)
                }
                .onFailure { error ->
                    _uiState.value = CreateTaskUiState.Error(
                        error.message ?: "Failed to create task"
                    )
                }
        }
    }

    /**
     * Reset form
     *
     * USAGE:
     * - After successful creation (if user wants to create another)
     * - When user cancels (clear form for next time)
     */
    fun resetForm() {
        _title.value = ""
        _description.value = ""
        _selectedStatus.value = TaskStatus.PENDING
        _titleError.value = null
        _descriptionError.value = null
        _uiState.value = CreateTaskUiState.Idle
    }

    /**
     * Clear UI state
     *
     * USAGE:
     * After navigation completes, clear the success state
     * So if user comes back, they see a fresh form
     */
    fun clearUiState() {
        _uiState.value = CreateTaskUiState.Idle
    }
}

/**
 * Create Task UI State
 *
 * SEALED CLASS:
 * Represents all possible states of the create task screen
 *
 * STATES:
 * - Idle: Default state, form ready for input
 * - Loading: Creating task (show progress)
 * - Success: Task created (navigate back)
 * - Error: Something went wrong (show error, allow retry)
 */
sealed class CreateTaskUiState {
    /**
     * Idle state
     * Form is ready for user input
     */
    object Idle : CreateTaskUiState()

    /**
     * Loading state
     * Creating task, show progress indicator
     * Disable form inputs to prevent double-submission
     */
    object Loading : CreateTaskUiState()

    /**
     * Success state
     * Task created successfully!
     *
     * @param taskId The ID of the created task (from server)
     *
     * NAVIGATION:
     * When UI sees this state, navigate back to task list
     */
    data class Success(val taskId: Long) : CreateTaskUiState()

    /**
     * Error state
     * Failed to create task
     *
     * @param message Error message to show user
     *
     * USER ACTIONS:
     * - Show error message
     * - Keep form data (user can fix and retry)
     * - Provide retry button
     */
    data class Error(val message: String) : CreateTaskUiState()
}
