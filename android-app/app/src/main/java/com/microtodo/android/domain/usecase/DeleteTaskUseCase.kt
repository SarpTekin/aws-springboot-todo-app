package com.microtodo.android.domain.usecase

import com.microtodo.android.domain.repository.TaskRepository

/**
 * Use Case for deleting a task
 */
class DeleteTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long): Result<Unit> {
        return taskRepository.deleteTask(taskId)
    }
}
