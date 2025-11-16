package com.microtodo.android.data.repository

import com.microtodo.android.data.remote.api.TaskApiService
import com.microtodo.android.data.remote.dto.TaskRequest
import com.microtodo.android.data.remote.dto.TaskStatus
import com.microtodo.android.domain.model.Task
import com.microtodo.android.domain.repository.TaskRepository

/**
 * Implementation of TaskRepository
 * Handles all task-related API operations
 */
class TaskRepositoryImpl(
    private val taskApiService: TaskApiService
) : TaskRepository {

    override suspend fun createTask(
        title: String,
        description: String?,
        userId: Long,
        status: TaskStatus?
    ): Result<Task> {
        return try {
            val response = taskApiService.createTask(
                TaskRequest(
                    title = title,
                    description = description,
                    userId = userId,
                    status = status
                )
            )

            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllTasks(userId: Long?): Result<List<Task>> {
        return try {
            val response = taskApiService.getAllTasks(userId)
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTaskById(id: Long): Result<Task> {
        return try {
            val response = taskApiService.getTaskById(id)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTask(
        id: Long,
        title: String,
        description: String?,
        userId: Long,
        status: TaskStatus?
    ): Result<Task> {
        return try {
            val response = taskApiService.updateTask(
                id = id,
                request = TaskRequest(
                    title = title,
                    description = description,
                    userId = userId,
                    status = status
                )
            )

            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(id: Long): Result<Unit> {
        return try {
            taskApiService.deleteTask(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Extension function to convert DTO to Domain model
 */
private fun com.microtodo.android.data.remote.dto.TaskResponse.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        status = status,
        userId = userId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
