package com.microtodo.android.domain.usecase

import com.microtodo.android.data.remote.dto.TaskStatus
import com.microtodo.android.domain.model.Task
import com.microtodo.android.domain.repository.AuthRepository
import com.microtodo.android.domain.repository.TaskRepository

/**
 * Use Case for updating a task
 */
class UpdateTaskUseCase(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        taskId: Long,
        title: String,
        description: String?,
        status: TaskStatus
    ): Result<Task> {
        if (title.isBlank() || title.length < 3) {
            return Result.failure(IllegalArgumentException("Title must be at least 3 characters"))
        }

        val userId = authRepository.getCurrentUserId()
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        return taskRepository.updateTask(taskId, title, description, userId, status)
    }
}
