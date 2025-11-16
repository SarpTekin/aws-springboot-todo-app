package com.microtodo.android.domain.repository

import com.microtodo.android.data.remote.dto.TaskStatus
import com.microtodo.android.domain.model.Task

/**
 * Repository interface for task operations
 * Domain layer defines the contract, data layer implements it
 */
interface TaskRepository {

    /**
     * Create a new task
     */
    suspend fun createTask(
        title: String,
        description: String?,
        userId: Long,
        status: TaskStatus? = null
    ): Result<Task>

    /**
     * Get all tasks, optionally filtered by user ID
     */
    suspend fun getAllTasks(userId: Long? = null): Result<List<Task>>

    /**
     * Get a specific task by ID
     */
    suspend fun getTaskById(id: Long): Result<Task>

    /**
     * Update an existing task
     */
    suspend fun updateTask(
        id: Long,
        title: String,
        description: String?,
        userId: Long,
        status: TaskStatus?
    ): Result<Task>

    /**
     * Delete a task
     */
    suspend fun deleteTask(id: Long): Result<Unit>
}
