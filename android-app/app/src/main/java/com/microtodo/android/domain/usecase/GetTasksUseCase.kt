package com.microtodo.android.domain.usecase

import com.microtodo.android.domain.model.Task
import com.microtodo.android.domain.repository.AuthRepository
import com.microtodo.android.domain.repository.TaskRepository

/**
 * Use Case for fetching tasks
 * Automatically filters by current user
 */
class GetTasksUseCase(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<List<Task>> {
        val userId = authRepository.getCurrentUserId()
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        return taskRepository.getAllTasks(userId)
    }
}
