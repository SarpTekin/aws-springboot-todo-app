package com.example.todoapp.data.model

import kotlinx.serialization.Serializable

/**
 * Task Request
 *
 * WHAT IS THIS?
 * The data structure we SEND to the backend when:
 * - Creating a task: POST /api/tasks
 * - Updating a task: PUT /api/tasks/{id}
 *
 * WHY SEPARATE FROM RESPONSE?
 * When creating/updating, we only provide:
 * - Data the user can control
 * - No server-generated fields (id, createdAt, updatedAt)
 *
 * The backend generates:
 * - id (database auto-increment)
 * - createdAt (current timestamp)
 * - updatedAt (current timestamp)
 *
 * ARCHITECTURE PATTERN:
 * Request = Input to API
 * Response = Output from API
 *
 * EXAMPLE USAGE:
 *
 * Creating a task:
 * ```kotlin
 * val newTask = TaskRequest(
 *     userId = currentUserId,
 *     title = "Buy groceries",
 *     description = "Milk, eggs, bread",
 *     status = TaskStatus.PENDING
 * )
 * repository.createTask(newTask)
 * ```
 *
 * Updating a task (change status):
 * ```kotlin
 * val updateTask = TaskRequest(
 *     userId = currentUserId,
 *     title = "Buy groceries",
 *     description = "Milk, eggs, bread",
 *     status = TaskStatus.COMPLETED  // Mark as done!
 * )
 * repository.updateTask(taskId, updateTask)
 * ```
 *
 * EXAMPLE JSON SENT TO BACKEND:
 * {
 *   "userId": 5,
 *   "title": "Learn Kotlin",
 *   "description": "Complete Android TODO app",
 *   "status": "PENDING"
 * }
 */
@Serializable
data class TaskRequest(
    /**
     * Task Title (Required)
     * What needs to be done?
     *
     * VALIDATION:
     * Should not be empty (validate in ViewModel before sending)
     */
    val title: String,

    /**
     * Task Description (Optional)
     * Additional details about the task
     *
     * KOTLIN NULL SAFETY:
     * String? = can be null
     * Default value = null (if not provided)
     *
     * User can leave this blank in the UI
     */
    val description: String? = null,

    /**
     * Task Status
     * Current state of the task
     *
     * DEFAULT VALUE:
     * When creating a new task, usually set to PENDING
     * When updating, can change to IN_PROGRESS, COMPLETED
     *
     * TYPE SAFETY:
     * Can only use TaskStatus enum values
     * Compiler prevents typos like "pending" or "done"
     *
     * ⚠️ IMPORTANT CHANGE:
     * userId is NO LONGER in this request!
     * Backend automatically extracts userId from JWT token
     * This is more secure and prevents users from creating tasks for others
     */
    val status: TaskStatus = TaskStatus.PENDING
) {
    /**
     * Validation: Check if request is valid
     *
     * BUSINESS RULES:
     * - Title cannot be blank
     * - Title should have reasonable length
     *
     * Returns: Error message if invalid, null if valid
     *
     * USAGE IN VIEWMODEL:
     * ```kotlin
     * val error = taskRequest.validate()
     * if (error != null) {
     *     _uiState.value = Error(error)
     *     return
     * }
     * ```
     */
    fun validate(): String? {
        return when {
            title.isBlank() -> "Title cannot be empty"
            title.length > 200 -> "Title is too long (max 200 characters)"
            description?.length ?: 0 > 1000 -> "Description is too long (max 1000 characters)"
            else -> null  // Valid!
        }
    }

    /**
     * Helper: Create a copy with updated status
     *
     * KOTLIN DATA CLASS FEATURE:
     * Data classes auto-generate copy() function
     * Makes it easy to create modified copies
     *
     * EXAMPLE:
     * ```kotlin
     * val task = TaskRequest(...)
     * val completedTask = task.withStatus(TaskStatus.COMPLETED)
     * ```
     */
    fun withStatus(newStatus: TaskStatus): TaskRequest {
        return copy(status = newStatus)
    }
}
