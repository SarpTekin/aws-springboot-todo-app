package com.microtodo.android.domain.usecase

import com.microtodo.android.data.remote.dto.TaskStatus
import com.microtodo.android.domain.model.Task
import com.microtodo.android.domain.repository.AuthRepository
import com.microtodo.android.domain.repository.TaskRepository

/**
 * Use Case for creating a task
 * Automatically uses current user ID
 */
class CreateTaskUseCase(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String?,
        status: TaskStatus = TaskStatus.PENDING
    ): Result<Task> {
        // Validate
        if (title.isBlank() || title.length < 3) {
            return Result.failure(IllegalArgumentException("Title must be at least 3 characters"))
        }

        val userId = authRepository.getCurrentUserId()
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        return taskRepository.createTask(title, description, userId, status)
    }
}
