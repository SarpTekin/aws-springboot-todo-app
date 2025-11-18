package com.example.todoapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.local.TokenManager
import com.example.todoapp.data.model.TaskResponse
import com.example.todoapp.data.model.TaskStatus
import com.example.todoapp.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Task List ViewModel
 *
 * RESPONSIBILITY:
 * - Load all tasks for the current user
 * - Filter tasks by status (All, Pending, In Progress, Completed, Cancelled)
 * - Handle task deletion
 * - Handle task status updates (mark as complete, etc.)
 * - Handle pull-to-refresh
 * - Manage UI state (loading, success, error)
 *
 * WHY VIEWMODEL?
 * - Survives configuration changes (screen rotation)
 * - Separates UI logic from UI rendering
 * - Provides data to UI via StateFlow (reactive)
 * - Handles background operations safely (viewModelScope)
 *
 * STATE MANAGEMENT:
 * Uses StateFlow to expose UI state to Composables
 * UI automatically updates when state changes!
 *
 * ARCHITECTURE:
 * TaskListScreen → TaskListViewModel → TaskRepository → TaskApiService
 *       (UI)            (Logic)           (Data)         (Network)
 */
class TaskListViewModel(
    private val repository: TaskRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // Current UI state (loading, success, error)
    private val _uiState = MutableStateFlow<TaskListUiState>(TaskListUiState.Loading)
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    // Current filter selection (default: show all tasks)
    private val _currentFilter = MutableStateFlow<TaskStatus?>(null)
    val currentFilter: StateFlow<TaskStatus?> = _currentFilter.asStateFlow()

    // All tasks (unfiltered)
    private val _allTasks = MutableStateFlow<List<TaskResponse>>(emptyList())

    // Current user ID (from token)
    private var currentUserId: Long? = null

    init {
        // Load tasks when ViewModel is created
        loadTasks()
    }

    /**
     * Load tasks from backend
     *
     * FLOW:
     * 1. Get userId from TokenManager
     * 2. Set state to Loading
     * 3. Call repository.getTasks(userId)
     * 4. Handle success/failure
     * 5. Apply current filter
     *
     * CALLED:
     * - On ViewModel init (screen opens)
     * - On pull-to-refresh
     * - After creating/updating/deleting a task
     */
    fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = TaskListUiState.Loading

            // Get current user ID from token
            val userId = tokenManager.getUserId()
            if (userId == null) {
                _uiState.value = TaskListUiState.Error("User not logged in")
                return@launch
            }
            currentUserId = userId

            // Fetch tasks from backend
            val result = repository.getTasks(userId)

            result
                .onSuccess { tasks ->
                    // Store all tasks
                    _allTasks.value = tasks

                    // Apply current filter and update UI
                    applyFilter(_currentFilter.value)
                }
                .onFailure { error ->
                    _uiState.value = TaskListUiState.Error(
                        error.message ?: "Failed to load tasks"
                    )
                }
        }
    }

    /**
     * Apply filter to task list
     *
     * FILTERS:
     * - null = Show all tasks
     * - TaskStatus.PENDING = Show only pending tasks
     * - TaskStatus.IN_PROGRESS = Show only in-progress tasks
     * - TaskStatus.COMPLETED = Show only completed tasks
     * - TaskStatus.CANCELLED = Show only cancelled tasks
     *
     * KOTLIN FEATURES:
     * - filter { } = Higher-order function (functional programming)
     * - it.status == filter = Lambda expression
     *
     * @param filter TaskStatus to filter by, or null for all
     */
    fun applyFilter(filter: TaskStatus?) {
        _currentFilter.value = filter

        val filteredTasks = if (filter == null) {
            // No filter = show all tasks
            _allTasks.value
        } else {
            // Filter by status
            _allTasks.value.filter { it.status == filter }
        }

        // Update UI state with filtered tasks
        _uiState.value = TaskListUiState.Success(
            tasks = filteredTasks,
            totalCount = _allTasks.value.size,
            filteredCount = filteredTasks.size
        )
    }

    /**
     * Delete a task
     *
     * FLOW:
     * 1. Show loading state
     * 2. Call repository.deleteTask(taskId)
     * 3. If success: reload tasks
     * 4. If failure: show error
     *
     * UI TRIGGER:
     * - Swipe-to-delete in list
     * - Delete button in task detail
     *
     * @param taskId The task ID to delete
     */
    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            _uiState.value = TaskListUiState.Loading

            val result = repository.deleteTask(taskId)

            result
                .onSuccess {
                    // Reload tasks to update UI
                    loadTasks()
                }
                .onFailure { error ->
                    _uiState.value = TaskListUiState.Error(
                        error.message ?: "Failed to delete task"
                    )
                }
        }
    }

    /**
     * Update task status
     *
     * USE CASES:
     * - Mark task as complete
     * - Start working on task (PENDING → IN_PROGRESS)
     * - Cancel task
     *
     * CONVENIENCE:
     * Uses repository.updateTaskStatus() helper
     * Don't need to create full TaskRequest
     *
     * @param task The task to update
     * @param newStatus The new status
     */
    fun updateTaskStatus(task: TaskResponse, newStatus: TaskStatus) {
        viewModelScope.launch {
            _uiState.value = TaskListUiState.Loading

            val result = repository.updateTaskStatus(
                taskId = task.id,
                currentTask = task,
                newStatus = newStatus
            )

            result
                .onSuccess {
                    // Reload tasks to show updated status
                    loadTasks()
                }
                .onFailure { error ->
                    _uiState.value = TaskListUiState.Error(
                        error.message ?: "Failed to update task"
                    )
                }
        }
    }

    /**
     * Refresh tasks (pull-to-refresh)
     *
     * Same as loadTasks() but could add different behavior later
     * (e.g., show different loading indicator)
     */
    fun refreshTasks() {
        loadTasks()
    }

    /**
     * Quick action: Mark task as completed
     *
     * CONVENIENCE FUNCTION:
     * Common action - deserves its own function
     *
     * @param task The task to mark complete
     */
    fun markTaskAsCompleted(task: TaskResponse) {
        updateTaskStatus(task, TaskStatus.COMPLETED)
    }

    /**
     * Quick action: Start task (move to IN_PROGRESS)
     *
     * @param task The task to start
     */
    fun startTask(task: TaskResponse) {
        updateTaskStatus(task, TaskStatus.IN_PROGRESS)
    }

    /**
     * Quick action: Cancel task
     *
     * @param task The task to cancel
     */
    fun cancelTask(task: TaskResponse) {
        updateTaskStatus(task, TaskStatus.CANCELLED)
    }

    /**
     * Get task count by status
     *
     * USAGE IN UI:
     * Show badges on filter chips:
     * "Pending (5)" "In Progress (2)" "Completed (10)"
     *
     * @param status The status to count
     * @return Number of tasks with that status
     */
    fun getTaskCountByStatus(status: TaskStatus): Int {
        return _allTasks.value.count { it.status == status }
    }
}

/**
 * Task List UI State
 *
 * SEALED CLASS:
 * Represents all possible states of the task list screen
 * Type-safe: Compiler ensures we handle all cases
 *
 * KOTLIN WHEN EXPRESSION:
 * In Composable, we can do:
 * ```kotlin
 * when (val state = uiState) {
 *     is Loading -> ShowLoadingSpinner()
 *     is Success -> ShowTaskList(state.tasks)
 *     is Error -> ShowErrorMessage(state.message)
 * }
 * ```
 */
sealed class TaskListUiState {
    /**
     * Loading state
     * Shown when:
     * - Initial load
     * - Refreshing
     * - Deleting task
     * - Updating task
     */
    object Loading : TaskListUiState()

    /**
     * Success state
     * Tasks loaded successfully
     *
     * @param tasks The filtered task list to display
     * @param totalCount Total number of tasks (unfiltered)
     * @param filteredCount Number of tasks after filter
     *
     * EXAMPLE:
     * - totalCount = 20 (user has 20 total tasks)
     * - filteredCount = 5 (filter shows 5 pending tasks)
     */
    data class Success(
        val tasks: List<TaskResponse>,
        val totalCount: Int,
        val filteredCount: Int
    ) : TaskListUiState() {
        /**
         * Helper: Is the list empty?
         * Used to show "No tasks" empty state
         */
        val isEmpty: Boolean
            get() = tasks.isEmpty()

        /**
         * Helper: Is filter active?
         * Used to show "No tasks match filter" vs "No tasks at all"
         */
        val isFiltered: Boolean
            get() = filteredCount < totalCount
    }

    /**
     * Error state
     * Something went wrong
     *
     * @param message Error message to show user
     *
     * POSSIBLE ERRORS:
     * - Network error
     * - Server error
     * - User not logged in
     * - Failed to delete/update
     */
    data class Error(val message: String) : TaskListUiState()
}
