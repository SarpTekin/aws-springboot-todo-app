package com.example.todoapp.data.repository

import com.example.todoapp.data.model.TaskRequest
import com.example.todoapp.data.model.TaskResponse
import com.example.todoapp.data.remote.TaskApiService

/**
 * Task Repository
 *
 * WHAT IS THIS?
 * The "single source of truth" for task-related data operations.
 * Acts as a bridge between ViewModel and API Service.
 *
 * WHY DO WE NEED THIS?
 * 1. Separation of Concerns:
 *    - ViewModel doesn't need to know about Retrofit, HTTP, etc.
 *    - ViewModel just calls: repository.getTasks(userId)
 *
 * 2. Error Handling:
 *    - Wraps API calls in try/catch
 *    - Returns Result<T> for clean error handling
 *    - ViewModel gets either success or failure, no crashes!
 *
 * 3. Testability:
 *    - Easy to create fake repository for testing
 *    - No need to mock Retrofit in tests
 *
 * 4. Future Flexibility:
 *    - Want to add caching? Just modify repository
 *    - Want to add offline support? Just modify repository
 *    - ViewModel code doesn't change!
 *
 * ARCHITECTURE FLOW:
 * UI → ViewModel → Repository → API Service → Backend
 *                      ↑
 *                  YOU ARE HERE
 *
 * RESULT<T> PATTERN:
 * Instead of throwing exceptions, we return:
 * - Result.success(data) when API call succeeds
 * - Result.failure(error) when API call fails
 *
 * ViewModel can handle both cases gracefully:
 * ```kotlin
 * result.onSuccess { tasks -> showTasks(tasks) }
 *       .onFailure { error -> showError(error) }
 * ```
 */
class TaskRepository(
    private val apiService: TaskApiService
) {

    /**
     * Get all tasks for a user
     *
     * ENDPOINT: GET /api/tasks?userId={userId}
     *
     * USAGE IN VIEWMODEL:
     * ```kotlin
     * val result = repository.getTasks(currentUserId)
     * result.onSuccess { tasks ->
     *     _uiState.value = Success(tasks)
     * }.onFailure { error ->
     *     _uiState.value = Error(error.message)
     * }
     * ```
     *
     * @param userId The user's ID to fetch tasks for
     * @return Result with List<TaskResponse> or error
     *
     * POSSIBLE ERRORS:
     * - Network error (no internet connection)
     * - Server error (500 Internal Server Error)
     * - Timeout (server took too long to respond)
     * - 401 Unauthorized (token expired - AuthInterceptor handles this)
     */
    suspend fun getTasks(userId: Long): Result<List<TaskResponse>> {
        return try {
            // Call the API service
            // AuthInterceptor automatically adds JWT token!
            val tasks = apiService.getTasks(userId)

            // Success! Return the tasks
            Result.success(tasks)
        } catch (e: Exception) {
            // Something went wrong (network, server, etc.)
            // Wrap error in Result.failure
            Result.failure(e)
        }
    }

    /**
     * Get a single task by ID
     *
     * ENDPOINT: GET /api/tasks/{id}
     *
     * USE CASE:
     * - User taps on a task to see details
     * - Navigate to TaskDetailScreen and load full data
     *
     * @param taskId The task ID
     * @return Result with TaskResponse or error
     *
     * POSSIBLE ERRORS:
     * - 404 Not Found: Task doesn't exist
     * - 403 Forbidden: Task belongs to different user
     */
    suspend fun getTask(taskId: Long): Result<TaskResponse> {
        return try {
            val task = apiService.getTask(taskId)
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a new task
     *
     * ENDPOINT: POST /api/tasks
     *
     * USAGE IN VIEWMODEL:
     * ```kotlin
     * val newTask = TaskRequest(
     *     userId = currentUserId,
     *     title = "Buy groceries",
     *     description = "Milk, eggs, bread",
     *     status = TaskStatus.PENDING
     * )
     * val result = repository.createTask(newTask)
     * result.onSuccess { createdTask ->
     *     // createdTask has id, createdAt, updatedAt from server
     *     _uiState.value = Success(createdTask)
     * }
     * ```
     *
     * @param request TaskRequest with task details
     * @return Result with created TaskResponse (includes generated id)
     *
     * WHAT HAPPENS:
     * 1. We send TaskRequest (no id)
     * 2. Server creates task in database
     * 3. Server returns TaskResponse (with generated id)
     * 4. We can immediately show the new task in UI
     */
    suspend fun createTask(request: TaskRequest): Result<TaskResponse> {
        return try {
            // Validate before sending
            val validationError = request.validate()
            if (validationError != null) {
                return Result.failure(IllegalArgumentException(validationError))
            }

            val createdTask = apiService.createTask(request)
            Result.success(createdTask)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing task
     *
     * ENDPOINT: PUT /api/tasks/{id}
     *
     * USE CASES:
     * - Mark task as completed
     * - Change task status (PENDING → IN_PROGRESS)
     * - Edit task title/description
     *
     * USAGE IN VIEWMODEL:
     * ```kotlin
     * val updatedTask = task.copy(status = TaskStatus.COMPLETED)
     * val request = TaskRequest(
     *     userId = task.userId,
     *     title = task.title,
     *     description = task.description,
     *     status = TaskStatus.COMPLETED
     * )
     * val result = repository.updateTask(task.id, request)
     * ```
     *
     * @param taskId The task ID to update
     * @param request TaskRequest with updated details
     * @return Result with updated TaskResponse
     *
     * WHAT HAPPENS:
     * 1. We send TaskRequest with new data
     * 2. Server updates task in database
     * 3. Server updates 'updatedAt' timestamp
     * 4. Server returns updated TaskResponse
     */
    suspend fun updateTask(taskId: Long, request: TaskRequest): Result<TaskResponse> {
        return try {
            // Validate before sending
            val validationError = request.validate()
            if (validationError != null) {
                return Result.failure(IllegalArgumentException(validationError))
            }

            val updatedTask = apiService.updateTask(taskId, request)
            Result.success(updatedTask)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a task
     *
     * ENDPOINT: DELETE /api/tasks/{id}
     *
     * USE CASES:
     * - Remove completed tasks
     * - Delete tasks no longer needed
     * - Swipe-to-delete in task list
     *
     * USAGE IN VIEWMODEL:
     * ```kotlin
     * val result = repository.deleteTask(taskId)
     * result.onSuccess {
     *     // Remove from UI
     *     _tasks.value = _tasks.value.filter { it.id != taskId }
     * }
     * ```
     *
     * @param taskId The task ID to delete
     * @return Result with Unit (success) or error
     *
     * NOTE: This is permanent deletion!
     * Alternative: Update status to CANCELLED instead of deleting
     */
    suspend fun deleteTask(taskId: Long): Result<Unit> {
        return try {
            apiService.deleteTask(taskId)
            // DELETE returns nothing (204 No Content)
            // But we return Result.success to indicate it worked
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper: Update task status only
     *
     * CONVENIENCE FUNCTION:
     * Instead of creating full TaskRequest, just change status
     *
     * USAGE:
     * ```kotlin
     * // Mark task as completed
     * repository.updateTaskStatus(
     *     taskId = task.id,
     *     currentTask = task,
     *     newStatus = TaskStatus.COMPLETED
     * )
     * ```
     *
     * @param taskId The task ID
     * @param currentTask Current task data
     * @param newStatus New status to set
     * @return Result with updated TaskResponse
     */
    suspend fun updateTaskStatus(
        taskId: Long,
        currentTask: TaskResponse,
        newStatus: com.example.todoapp.data.model.TaskStatus
    ): Result<TaskResponse> {
        // Create TaskRequest with updated status
        val request = TaskRequest(
            userId = currentTask.userId,
            title = currentTask.title,
            description = currentTask.description,
            status = newStatus
        )
        return updateTask(taskId, request)
    }

    // TODO: Future enhancements
    // - Add caching: Store tasks in Room database
    // - Add offline support: Queue operations when offline
    // - Add pagination: Load tasks in chunks (page 1, page 2, etc.)
    // - Add search: Filter tasks by title/description
}
