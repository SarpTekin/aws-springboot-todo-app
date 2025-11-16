package com.microtodo.android.data.remote.api

import com.microtodo.android.data.remote.dto.TaskRequest
import com.microtodo.android.data.remote.dto.TaskResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Task Service API (Port 8082)
 * Base URL: http://localhost:8082
 */
interface TaskApiService {

    /**
     * Create a new task
     * Endpoint: POST /api/tasks
     * Requires JWT authentication
     */
    @POST("/api/tasks")
    suspend fun createTask(@Body request: TaskRequest): TaskResponse

    /**
     * Get all tasks with optional user filter
     * Endpoint: GET /api/tasks?userId={userId}
     * Requires JWT authentication
     */
    @GET("/api/tasks")
    suspend fun getAllTasks(@Query("userId") userId: Long? = null): List<TaskResponse>

    /**
     * Get task by ID
     * Endpoint: GET /api/tasks/{id}
     * Requires JWT authentication
     */
    @GET("/api/tasks/{id}")
    suspend fun getTaskById(@Path("id") id: Long): TaskResponse

    /**
     * Update a task
     * Endpoint: PUT /api/tasks/{id}
     * Requires JWT authentication
     */
    @PUT("/api/tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: Long,
        @Body request: TaskRequest
    ): TaskResponse

    /**
     * Delete a task
     * Endpoint: DELETE /api/tasks/{id}
     * Requires JWT authentication
     * Returns: 204 No Content
     */
    @DELETE("/api/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Long)
}
