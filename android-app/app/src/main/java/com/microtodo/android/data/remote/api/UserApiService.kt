package com.microtodo.android.data.remote.api

import com.microtodo.android.data.remote.dto.LoginRequest
import com.microtodo.android.data.remote.dto.LoginResponse
import com.microtodo.android.data.remote.dto.UserRequest
import com.microtodo.android.data.remote.dto.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * User Service API (Port 8081)
 * Base URL: http://localhost:8081
 */
interface UserApiService {

    /**
     * Authenticate user and receive JWT token
     * Endpoint: POST /api/auth/login
     * Public endpoint (no JWT required)
     */
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    /**
     * Register a new user
     * Endpoint: POST /api/users
     * Public endpoint (no JWT required)
     */
    @POST("/api/users")
    suspend fun registerUser(@Body request: UserRequest): UserResponse

    /**
     * Get user by ID
     * Endpoint: GET /api/users/{id}
     * Requires JWT authentication
     */
    @GET("/api/users/{id}")
    suspend fun getUserById(@Path("id") id: Long): UserResponse
}
